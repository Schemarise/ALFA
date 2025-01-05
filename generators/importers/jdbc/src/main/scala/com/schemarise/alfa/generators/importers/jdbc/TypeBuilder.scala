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

package com.schemarise.alfa.generators.importers.jdbc

import java.sql.{Connection, ResultSet}

import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, IDataTypeSizeRange, IUdtDataType, Scalars}
import com.schemarise.alfa.compiler.ast.model.{NoOpNodeVisitor, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.utils.TokenImpl
import com.schemarise.alfa.compiler.{AlfaCompiler, CompilationUnitArtifact, Context}

import scala.collection.immutable.ListMap
import scala.collection.mutable

class TypeBuilder(conn: Connection, namespace: NamespaceNode, tableNames: Seq[String]) {
  val ctx = new Context()

  val cu = ctx.readScript(None, AlfaCompiler.builtinAnnotations)

  val udts = new mutable.HashMap[String, Record]()

  val cua = makeCompUnit()

  def makeCompUnit() = {
    val udts = tableNames.map(tn => makeUdts(tn)).flatten
    val nn = new NamespaceNode(collectedUdts = udts)
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    new CompilationUnitArtifact(ctx, cu)
  }

  class ColDef(rs: ResultSet) {
    val name = rs.getString("COLUMN_NAME")
    val dataType = rs.getInt("DATA_TYPE")
    val size = rs.getInt("COLUMN_SIZE")
    val nullable = rs.getString("IS_NULLABLE").equals("YES")
    val decimalDigits = rs.getInt("DECIMAL_DIGITS")
    val comments = rs.getString("REMARKS")
    val autoInc = rs.getString("IS_AUTOINCREMENT")
    val generate = rs.getString("IS_GENERATEDCOLUMN")
  }

  class KeyColDef(cols: Map[String, ColDef], rs: ResultSet) {
    val name = rs.getString("COLUMN_NAME")
    val seq = rs.getShort("KEY_SEQ")
    val colDef = cols.get(name).get
  }

  def dtSize(size: Int): Option[IDataTypeSizeRange[_]] = {
    val in = IntRangeNode(minNode = IntNode(number = None)(), maxNode = IntNode(number = Some(size))())
    Some(in)
  }

  def toDataType(c: ColDef): DataType = {
    val dt = c.dataType match {
      case java.sql.Types.CHAR => new ScalarDataType(TokenImpl.empty, Scalars.string, sizeRange = dtSize(c.size))
      case java.sql.Types.INTEGER => ScalarDataType.intType
      case java.sql.Types.NUMERIC =>
        if (c.decimalDigits > 0)
          ScalarDataType.doubleType
        else if (c.size < 5) ScalarDataType.shortType
        else if (c.size < 10) ScalarDataType.intType
        else ScalarDataType.decimalType

      case java.sql.Types.VARCHAR => ScalarDataType.stringType
      case java.sql.Types.DOUBLE => ScalarDataType.doubleType
      case java.sql.Types.DECIMAL => ScalarDataType.decimalType
      case java.sql.Types.BIGINT => ScalarDataType.longType
      case java.sql.Types.TIMESTAMP_WITH_TIMEZONE => ScalarDataType.datetimetzType
      case java.sql.Types.BOOLEAN => ScalarDataType.booleanType
      case java.sql.Types.BLOB => ScalarDataType.binaryType
      case java.sql.Types.DATE => ScalarDataType.dateType
      case java.sql.Types.TIMESTAMP => ScalarDataType.datetimeType
      case java.sql.Types.TIME => ScalarDataType.timeType
      case _ => throw new RuntimeException("" + c.dataType)
    }

    if (c.nullable)
      EnclosingDataType(encType = Enclosed.opt, declComponentType = dt)
    else
      dt
  }

  def toFieldRefOrDef(c: ColDef): FieldOrFieldRef = {
    val an = makeAnnotation("alfa.db.Column", ListMap("name" -> c.name))
    val nm = NodeMeta(annotations = Seq(an))
    val f = new Field(rawNodeMeta = nm, nameNode = StringNode.create(beanName(c.name)), declDataType = toDataType(c))
    new FieldOrFieldRef(f)
  }

  def makeAnnotation(annName: String, fieldValues: ListMap[String, String]): Annotation = {
    new Annotation(nameNode = StringNode.create(annName))
  }

  def toKeyFieldRefOrDef(c: KeyColDef): FieldOrFieldRef = {
    val an = makeAnnotation("alfa.db.Column", ListMap("name" -> c.name))
    val nm = NodeMeta(annotations = Seq(an))
    val f = new Field(rawNodeMeta = nm, nameNode = StringNode.create(beanName(c.name)), declDataType = toDataType(c.colDef))
    new FieldOrFieldRef(f)
  }

  def getKeyCols(tn: String): Set[String] = {
    val primaryKeysRs = conn.getMetaData.getPrimaryKeys(null, null, tn)
    val primaryCols = new mutable.ListBuffer[String]()
    while (primaryKeysRs.next()) {
      primaryCols += primaryKeysRs.getString("COLUMN_NAME")
    }

    primaryCols.toSet
  }

  def makeUdts(tn: String): Seq[UdtBaseNode] = {

    val decapName = beanName(tn)

    val allCols = new mutable.HashMap[String, ColDef]()
    val rs = conn.getMetaData.getColumns(null, null, tn, null)
    while (rs.next()) {
      val cd = new ColDef(rs)
      allCols += cd.name -> cd
    }

    val keyColNames = getKeyCols(tn)

    val keyColDefs = allCols.filter(e => keyColNames.contains(e._1))
    val valColDefs = allCols.filter(e => !keyColNames.contains(e._1))

    val an = makeAnnotation("alfa.db.Table", ListMap("name" -> tn))
    val nm = NodeMeta(annotations = Seq(an))

    val entityFields = valColDefs.values.map(e => toFieldRefOrDef(e))
    val ent = new Entity(
      nodeMeta = nm,
      namespace = namespace,
      ctx = Some(ctx),
      name = StringNode.create(decapName),
      fields = entityFields.toSeq,
      declKeyDataType = Some(UdtDataType(name = StringNode.create(namespace.name + "." + decapName + "Key"))),
      imports = Seq.empty
    )

    val primaryCols = new mutable.HashMap[String, KeyColDef]()
    val prs = conn.getMetaData.getPrimaryKeys(null, null, tn)
    while (prs.next()) {
      val kd = new KeyColDef(keyColDefs.toMap, prs)
      primaryCols += kd.name -> kd
    }

    val keyFields = primaryCols.values.map(e => toKeyFieldRefOrDef(e))
    val k = new Key(namespace = namespace, ctx = Some(ctx), nameNode = StringNode.create(decapName + "Key"), fieldsNode = keyFields.toSeq, imports = Seq.empty)

    ctx.registry.registerUdt(k)
    ctx.registry.registerUdt(ent)

    Seq(k, ent)
  }


  class OnlyScalarOrVecOfScalars extends NoOpNodeVisitor {
    var ignore = false

    override def enter(e: IUdtDataType): Mode = {
      ignore = true
      NodeVisitMode.Continue
    }
  }

  def beanName(n: String): String = {
    n.split("_").map(e => decapilize(e)).mkString("")
  }

  def decapilize(n: String): String = {
    val lc = n.toLowerCase()
    lc.head.toUpper + lc.tail
  }

}