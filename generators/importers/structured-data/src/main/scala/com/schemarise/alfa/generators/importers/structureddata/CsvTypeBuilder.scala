package com.schemarise.alfa.generators.importers.structureddata
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{AnyDataType, DataType, EnclosingDataType, ScalarDataType}
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, Context}
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, Field, FieldOrFieldRef, NamespaceNode, Record, StringNode}
import com.schemarise.alfa.compiler.utils.TextUtils
import com.schemarise.alfa.generators.importers.structureddata.CsvTypeBuilder.optionalType
import com.univocity.parsers.common.{ParsingContext, ResultIterator}

import java.nio.file.{Files, Path}
import scala.collection.mutable
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.apache.commons.lang3.math.NumberUtils

import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable.{HashMap, ListBuffer, MultiMap, Set}

object CsvTypeBuilder {
  private val optionalType = EnclosingDataType.optional(compType=AnyDataType.anyType)
}
class CsvTypeBuilder(ctx: Context,
                     csvFilePath : Path,
                     namespace: String,
                     typename :String,
                     dateFormat : String,
                     datetimeFormat : String) extends TypeBuilder {

  private val dateFmt = DateTimeFormatter.ofPattern(dateFormat)
  private val dateTimeFmt = DateTimeFormatter.ofPattern(datetimeFormat)

  override val udts: mutable.HashMap[String, Record] = new mutable.HashMap[String, Record]()
  override val cua: CompilationUnitArtifact = makeCompUnit()

  private def makeCompUnit() : CompilationUnitArtifact = {

    val p = new CsvParser( new CsvParserSettings() )
    val it : ResultIterator[Array[String], ParsingContext] = p.iterate(Files.newInputStream(csvFilePath)).iterator()

    val rowsRead: AtomicLong = new AtomicLong

    val colnames = ListBuffer[String]()
    val colTypes = new HashMap[String, Set[DataType]] with MultiMap[String, DataType]
    val notColTypes = new HashMap[String, Set[DataType]] with MultiMap[String, DataType]

    while (it.hasNext()) {
      val csvline = it.next()
      val rowNo = rowsRead.incrementAndGet()

      if (rowNo == 1) {
        colnames.appendAll( csvline.filter( e => e != null) )
        colnames.foreach( e => notColTypes.addBinding(e, AnyDataType.anyType) )
      }
      else {
        csvline.zipWithIndex.filter( l => l._2 < colnames.length ).foreach( l => {
          val cell = l._1
          val i = l._2
          val colName = colnames.lift(i).get

          var cellType : DataType = null

          if ( NumberUtils.isCreatable(cell) ) {
            val n = NumberUtils.createNumber(cell)

            if ( Math.ceil(n.doubleValue()) == Math.floor(n.doubleValue()) )  {
              if ( n.longValue() > Integer.MAX_VALUE ) {
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
          else if ( cell == null ) {
            cellType = CsvTypeBuilder.optionalType
          }
          else if ( cell.toLowerCase() == "true" || cell.toLowerCase() == "false" ) {
            cellType = ScalarDataType.booleanType
          }
          else {
            if ( notColTypes.contains(colName) &&
                 !notColTypes.get(colName).get.contains(ScalarDataType.datetimeType)) {
              try {
                dateTimeFmt.parse(cell)
                cellType = ScalarDataType.datetimeType
              } catch {
                case _ =>
                  notColTypes.addBinding(colName, ScalarDataType.datetimeType )
              }
            }

            if ( !notColTypes.get(colName).get.contains(ScalarDataType.dateType)) {
              try {
                dateFmt.parse(cell)
                cellType = ScalarDataType.dateType
              } catch {
                case _ =>
                  notColTypes.addBinding(colName, ScalarDataType.dateType )
              }
            }

            if ( !notColTypes.get(colName).get.contains(ScalarDataType.uuidType) ) {
              try {
                UUID.fromString(cell)
                cellType = ScalarDataType.uuidType
              } catch {
                case _ =>
                  notColTypes.addBinding(colName, ScalarDataType.uuidType )
              }
            }
          }

          if ( cellType == null ) {
            cellType = ScalarDataType.stringType
          }

          colTypes.addBinding(colName, cellType)
        })
      }

    }

    val fields = {
      colTypes.map(ct => {
        val cn = TextUtils.validAlfaIdentifier(ct._1)
        val st = ct._2.filter(x => x.isScalar).
          map(x => x.asInstanceOf[ScalarDataType]).toList.
          sortBy(x => x.scalarType).headOption.getOrElse(ScalarDataType.stringType)

        val isOpt = !ct._2.filter(x => x == optionalType).isEmpty

        val t = if (isOpt) EnclosingDataType.optional(compType = st) else st

        val f = new Field(nameNode = StringNode.create(cn), declDataType = t)
        new FieldOrFieldRef(f)
      })
    }

    val rec = new Record(namespace = new NamespaceNode(nameNode = StringNode.create(namespace)),
      nameNode = StringNode.create(typename),
      fields = fields.toSeq
    )

    ctx.registry.registerUdt(rec)

    val nn = new NamespaceNode(nameNode = StringNode.create(namespace), collectedUdts = Seq(rec))
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    val cua = new CompilationUnitArtifact(ctx, cu)

    cua
  }
}
