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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.{INamespaceNode, NoOpNodeVisitor, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, IUdtDataType}
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.ast.nodes.{FieldOrFieldRef, _}
import com.schemarise.alfa.compiler.utils.TokenImpl
import com.schemarise.alfa.compiler.{AlfaInternalException, CompilationUnitArtifact, Context}

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.{HashMap, MultiMap, Set}

class JsonBasedTypeBuilder(ctx: Context, cfg: StructureImportSettings, jn: JsonNode) extends TypeBuilder {
  val namespaceNode = NamespaceNode(StringNode.create(cfg.namespace))
  private val udtGenNames = new mutable.HashMap[String, String]()
  private var recCounter = 1

  override val udts: mutable.HashMap[String, Record] = new mutable.HashMap[String, Record]()

  override val cua: CompilationUnitArtifact = makeCompUnit()


  def makeCompUnit(): CompilationUnitArtifact = {
    val r = createOrGetRecord(jn)

    udts.values.foreach(rd => ctx.registry.registerUdt(rd))

    val nn = new NamespaceNode(nameNode = StringNode.create(cfg.namespace), collectedUdts = udts.values.toSeq)
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    val cua = new CompilationUnitArtifact(ctx, cu)

    cua
  }


  def createOrGetRecord(jn: JsonNode): Record = {

    if ( jn.isArray ) {
      var a = jn.asInstanceOf[ArrayNode]
      return createOrGetRecord(a.get(0))
    }

    if (!jn.isObject)
      throw new AlfaInternalException("Can decode Object based nodes only")

    val fnames = jn.fieldNames().asScala.toArray

    val fs = fnames.
      filter(e => cfg.typenameField.isEmpty || !cfg.typenameField.get.equals(e)).
      map(e => e -> makeFieldInfo(e, jn.get(e))).
      filter(e => e._2.isDefined).map(e => e._2.get).
      map(e => new FieldOrFieldRef(e))

    val recName = if (cfg.typenameField.isDefined && jn.get(cfg.typenameField.get) != null) {
      jn.get(cfg.typenameField.get).textValue()
    }
    else {
      makeUdtName(fnames)
    }

    val fqn = cfg.namespace + "." + recName

    var r = udts.get(fqn)

    if (r.isEmpty) {
      r = Some(new Record(namespace = namespaceNode, nameNode = StringNode.create(recName), fields = fs, imports = Seq.empty))
      udts.put(fqn, r.get)
    }
    r.get
  }

  class OnlyScalarOrVecOfScalars extends NoOpNodeVisitor {
    var ignore = false

    override def enter(e: IUdtDataType): Mode = {
      ignore = true
      NodeVisitMode.Continue
    }
  }

  def makeFieldInfo(name: String, n: JsonNode): Option[Field] = {
    val dt = makeDataType(n)

    if (dt.isDefined) {

      val v = new OnlyScalarOrVecOfScalars()
      dt.get.traverse(v)

      val nm = if (v.ignore) NodeMeta.empty else NodeMeta(samelineDocs = Seq(new Documentation(TokenImpl.empty, "" + n)))

      Some(new Field(nameNode = StringNode.create(name), declDataType = dt.get, rawNodeMeta = nm))
    }
    else
      None
  }

  def makeUdtName(n: Array[String]): String = {
    val nameFromFields = n.mkString("_")
    if (udtGenNames.contains(nameFromFields))
      udtGenNames.get(nameFromFields).get
    else {
      val rs = "Rec" + recCounter
      recCounter += 1
      udtGenNames.put(nameFromFields, rs)
      rs
    }
  }

  def makeDataType(n: JsonNode): Option[DataType] = {
    if (n.isArray) {
      val an = n.asInstanceOf[ArrayNode]

      val arrTypes = an.elements().asScala.map(e => makeDataType(e)).toSet
      if (arrTypes.size == 0)
        None
      else if (arrTypes.size > 1) {

        val nonUdts = arrTypes.map(_.get).filter(!_.isInstanceOf[UdtDataType])

        if (nonUdts.size > 0)
          throw new com.schemarise.alfa.compiler.AlfaInternalException("Multiple array element types unhandled " + arrTypes)
        else {
          val arrayRecs = arrTypes.map(_.get).map(e => e.asInstanceOf[UdtDataType].name).map(n => udts.get(n.text).get)

          val allFieldNames = arrayRecs.map(r => r.rawDeclaredFields.map(_.field.get.name)).flatten

          val notInSomeRecs = arrayRecs.map(r => {
            val recflds = r.rawDeclaredFields.map(_.field.get.name).toSet
            allFieldNames.filter(f => !recflds.contains(f))
          }).flatten

          val fields = arrayRecs.map(r => r.rawDeclaredFields.map(f => {
            val field = f.field.get

            val t = if (!field.dataType.isEncOptional() && notInSomeRecs.contains(field.name))
              EnclosingDataType(encType = Enclosed.opt, declComponentType = field.dataType)
            else
              field.dataType

            new Field(nameNode = field.nameNode, declDataType = t, rawNodeMeta = field.rawNodeMeta)
          })).flatten.toSeq

          val nn = StringNode.create(makeUdtName(fields.map(_.name).toArray))
          val fqn = cfg.namespace + "." + nn.text

          val r = new Record(namespace = namespaceNode, nameNode = nn, fields = fields.map(f => new FieldOrFieldRef(f)), imports = Seq.empty)
          udts.put(fqn, r)
          //          ctx.registry.registerUdt(r)

          Some(new ListDataType(declComponentType = r.asDataType)())
        }
      }
      else
        Some(new ListDataType(declComponentType = arrTypes.head.get)())
    }
    else if (n.isObject) {
      // is it a map or object
      val on = n.asInstanceOf[ObjectNode]

      // if all values are the same type, infer as map. Otherwise object
      val objOrMapValTypes = on.fields().asScala.map(e => makeDataType(e.getValue)).toSet
      if (objOrMapValTypes.size == 1) {
        return Some(new MapDataType(
          declKeyType = ScalarDataType.stringType,
          declValueType = objOrMapValTypes.head.get)())
      }
      else {
        val rec = createOrGetRecord(on)
        return Some(rec.asDataType)
      }
    }
    else if (n.isTextual) {
      try {
        cfg.dateFormat.parse(n.asText())
        return Some(ScalarDataType.dateType)
      } catch {
        case _ =>
      }

      try {
        cfg.datetimeFormat.parse(n.asText())
        return Some(ScalarDataType.datetimeType)
      } catch {
        case _ =>
      }

      try {
        cfg.timeFormat.parse(n.asText())
        return Some(ScalarDataType.timeType)
      } catch {
        case _ =>
      }

      try {
        UUID.fromString(n.asText())
        return Some(ScalarDataType.uuidType)
      } catch {
        case _ =>
      }

      Some(ScalarDataType.stringType)

    }
    else if (n.isInt) {
      Some(ScalarDataType.intType)
    }
    else if (n.isLong) {
      Some(ScalarDataType.longType)
    }
    else if (n.isFloat) {
      Some(ScalarDataType.doubleType)
    }
    else if (n.isDouble) {
      Some(ScalarDataType.doubleType)
    }
    else if (n.isBigDecimal) {
      Some(ScalarDataType.decimalType)
    }
    else if (n.isBinary) {
      Some(ScalarDataType.binaryType)
    }
    else if (n.isShort) {
      Some(ScalarDataType.shortType)
    }
    else if (n.isNull) {
      Some(ScalarDataType.stringType)
    }
    else
      Some(ScalarDataType.stringType)
  }
}