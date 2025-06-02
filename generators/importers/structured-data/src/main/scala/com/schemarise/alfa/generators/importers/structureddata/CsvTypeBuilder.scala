/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.generators.importers.structureddata
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{AnyDataType, DataType, EnclosingDataType, EnumDataType, ScalarDataType}
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, Context}
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, Field, FieldOrFieldRef, NamespaceNode, Record, StringNode}
import com.schemarise.alfa.compiler.utils.{ILogger, TextUtils}
import com.schemarise.alfa.generators.importers.structureddata.CsvTypeBuilder.optionalType
import com.schemarise.alfa.runtime.AlfaRuntimeException
import com.univocity.parsers.common.{ParsingContext, ResultIterator}
import com.univocity.parsers.csv._

import java.nio.file.{Files, Path}
import scala.collection.mutable
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.apache.commons.lang3.math.NumberUtils

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable.{HashMap, ListBuffer, MultiMap, Set}

object CsvTypeBuilder {
  private val optionalType = EnclosingDataType.optional(compType=AnyDataType.anyType)
}
class CsvTypeBuilder(logger: ILogger,
                     ctx: Context,
                     csvFilePath : Path,
                     settings : StructureImportSettings) extends TypeBuilder {

  override val udts: mutable.HashMap[String, Record] = new mutable.HashMap[String, Record]()
  override val cua: CompilationUnitArtifact = makeCompUnit()

  private def makeCompUnit() : CompilationUnitArtifact = {

    val csvf = new CsvFormat()
    csvf.setDelimiter(settings.csvDelimiter)

    val ps = new CsvParserSettings()
    ps.setFormat( csvf )
    ps.setMaxColumns(settings.csvMaxColumns)
    ps.setMaxCharsPerColumn(settings.csvMaxCharsPerColumn)

    val p = new CsvParser( ps )
    val it : ResultIterator[Array[String], ParsingContext] = p.iterate(Files.newInputStream(csvFilePath)).iterator()

    val rowsRead: AtomicLong = new AtomicLong

    val colnames = ListBuffer[String]()
    val colTypes = new mutable.LinkedHashMap[String, Set[DataType]] with MultiMap[String, DataType]
    val strColValues = new mutable.LinkedHashMap[String, Set[String]] with MultiMap[String, String]

    val notColTypes = new HashMap[String, Set[DataType]] with MultiMap[String, DataType]

    var typeNameFromField : Option[String] = None

    def processLine(rowNo : Long, l: (String, Int)): Unit = {

      val cell = l._1
      val colNo = l._2

      try {
        val colName = colnames.lift(colNo).get

        if (settings.typenameField.isDefined && colName.equals(settings.typenameField.get)) {
          typeNameFromField = Some(cell)
        }

        var cellType: DataType = null

        if (NumberUtils.isCreatable(cell)) {
          val n = NumberUtils.createNumber(cell)

          if (Math.ceil(n.doubleValue()) == Math.floor(n.doubleValue())) {
            if (n.longValue() > Integer.MAX_VALUE) {
              cellType = ScalarDataType.longType
            }
            else {
              cellType = ScalarDataType.intType
            }
          }
          else {
            cellType = ScalarDataType.doubleType
          }
        }
        else if (cell == null) {
          cellType = CsvTypeBuilder.optionalType
        }
        else if (cell.toLowerCase() == "true" || cell.toLowerCase() == "false") {
          cellType = ScalarDataType.booleanType
        }
        else {
          if (notColTypes.contains(colName) &&
            !notColTypes.get(colName).get.contains(ScalarDataType.datetimeType)) {
            try {
              val ignore = settings.datetimeFormat.parse(cell)
              cellType = ScalarDataType.datetimeType
            } catch {
              case _ =>
                notColTypes.addBinding(colName, ScalarDataType.datetimeType)
            }
          }

          if (!notColTypes.get(colName).get.contains(ScalarDataType.dateType)) {
            try {
              settings.dateFormat.parse(cell)
              cellType = ScalarDataType.dateType
            } catch {
              case _ =>
                notColTypes.addBinding(colName, ScalarDataType.dateType)
            }
          }

          if (!notColTypes.get(colName).get.contains(ScalarDataType.timeType)) {
            try {
              settings.timeFormat.parse(cell)
              cellType = ScalarDataType.timeType
            } catch {
              case _ =>
                notColTypes.addBinding(colName, ScalarDataType.timeType)
            }
          }

          if (!notColTypes.get(colName).get.contains(ScalarDataType.uuidType)) {
            try {
              UUID.fromString(cell)
              cellType = ScalarDataType.uuidType
            } catch {
              case _ =>
                notColTypes.addBinding(colName, ScalarDataType.uuidType)
            }
          }
        }

        if (cellType == null) {
          cellType = ScalarDataType.stringType
          strColValues.addBinding(colName, cell)
        }

        colTypes.addBinding(colName, cellType)
      } catch {
        case e:Exception =>
          val msg = s"Skipping line:$rowNo col:$colNo value: $cell. " + e.getMessage
          logger.warn(msg)
          if ( logger.isDebugEnabled ) {
            logger.debug(msg + logger.stacktraceToString(e))
          }
      }
    }

    while (it.hasNext()) {
      val csvline = it.next()
      val rowNo = rowsRead.incrementAndGet()

      if (rowNo == 1) {
        colnames.appendAll( csvline.filter( e => e != null) )
        colnames.foreach( e => notColTypes.addBinding(e, AnyDataType.anyType) )
        logger.debug(s"Found ${colnames.size} column names - ${colnames.mkString(", ")}")
      }
      else {
        csvline.zipWithIndex.filter( l => l._2 < colnames.length ).foreach( l => processLine(rowNo, l) )
      }
    }

    val fields =
      colTypes.map(ct => {
        val cn = TextUtils.validAlfaIdentifier(ct._1)
        val st = ct._2.filter(x => x.isScalar).
          map(x => x.asInstanceOf[ScalarDataType]).toList.
          sortBy(x => x.scalarType).lastOption.getOrElse(ScalarDataType.stringType)

        val finalType =
          if ( st == ScalarDataType.stringType && strColValues.get(ct._1).isDefined ) {
            val uniqueStrValues = strColValues.get(ct._1).get
            if ( uniqueStrValues.size <= settings.enumUniqueValueLimit && rowsRead.get() > 100 ) {
              EnumDataType( fields = uniqueStrValues.map(v => new Field(nameNode=StringNode.create(v), declDataType=ScalarDataType.stringType)).toSeq )
            }
            else {
              st
            }
          }
          else {
            st
          }

        val isOpt = ct._2.contains(optionalType)
        val t = if (isOpt) EnclosingDataType.optional(compType = finalType) else finalType

        val f = new Field(nameNode = StringNode.create(cn), declDataType = t)
        new FieldOrFieldRef(f)
      })

    val recName = typeNameFromField.getOrElse(settings.typename)

    val rec = new Record(nameNode = StringNode.create(recName), fields = fields.toSeq)

    ctx.registry.registerUdt(rec)

    val nn = new NamespaceNode(nameNode = StringNode.create(settings.namespace), collectedUdts = Seq(rec))
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    val cua = new CompilationUnitArtifact(ctx, cu)

    cua
  }
}
