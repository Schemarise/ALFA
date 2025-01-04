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
package com.schemarise.alfa.compiler.ast.nodes.datatypes

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, ITupleDataType, Vectors}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err._
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl


object TupleDataType {
  private var toStringLevel = 0

  def apply(f: Seq[Field], tupleNodeMeta: NodeMeta): TupleDataType = {
    new TupleDataType(TokenImpl.empty, f.map(_.dataType), f.map(_.nameNode), f.map(_.rawNodeMeta), tupleNodeMeta)
  }
}

case class TupleDataType(location: IToken = TokenImpl.empty,
                         declComponentTypes: Seq[DataType],
                         fieldNames: Seq[StringNode] = Seq.empty,
                         fieldNodeMetas: Seq[NodeMeta] = Seq.empty,
                         rawNodeMeta: NodeMeta = NodeMeta.empty
                        )
  extends VectorDataType(None) with ITupleDataType {

  private var _syntheticRecord: Option[SyntheticRecord] = None
  private var _componentTypes: Seq[DataType] = declComponentTypes

  override val vectorType: VectorType = Vectors.tuple

  override def resolvableInnerNodes() = asSeq(rawNodeMeta) ++ _componentTypes ++ _syntheticRecord

  override def fieldDataType() = AllFieldTypes.tuple

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    TupleDataType(location, _componentTypes.map(e => e.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType]), fieldNames, fieldNodeMetas, rawNodeMeta)

  override def annotations = if (rawNodeMeta != NodeMeta.empty) rawNodeMeta.annotations else Seq.empty

  override def annotationsMap = if (rawNodeMeta != NodeMeta.empty) rawNodeMeta.annotationsMap else Map.empty

  override def componentTypes = _componentTypes

  def getAsFields() = {

    val m = if (fieldNodeMetas.isEmpty) Seq.fill(fieldNames.size)(NodeMeta.empty) else fieldNodeMetas

    fieldNames.zip(declComponentTypes).zip(m).map(fs => {
      new Field(rawNodeMeta = fs._2, nameNode = fs._1._1, declDataType = fs._1._2)
    })
  }


  override def syntheticRecord = {
    assertPreResolved(None)
    _syntheticRecord.get
  }

  override def toString: String = {
    TupleDataType.toStringLevel += 1

    val indent = "    " * TupleDataType.toStringLevel

    val s = new StringBuilder
    val sep = if (fieldNames.size > 0 || _componentTypes.size > 0)
      s"\n$indent"
    else
      ""

    val innerIndent = "    "

    val ann = if (rawNodeMeta != NodeMeta.empty)
      rawNodeMeta.annotations.map(e => "@" + e.versionedName.fullyQualifiedName + " ").mkString(" ")
    else
      ""

    s.append(s"${ann}tuple< $sep$innerIndent")

    if (fieldNames.length > 0 && !fieldNames.head.text.startsWith(SynthNames.TupleField)) {
      val metas = fieldNames.zip(fieldNodeMetas).map(e => e._1.text -> e._2).toMap

      val zip = fieldNames.zip(_componentTypes).map(f => {
        val m = metas.get(f._1.text)

        val ds = if (m.isDefined) {
          val d = m.get.topDocsToString("")
          if (d.trim.length == 0)
            ""
          else {
            s"${d.dropRight(1)}$sep$innerIndent"
          }
        }
        else
          ""

        val optName = if (f._1.text.startsWith("TupleField__")) "" else f._1.text + " : "
        ds + optName + f._2.toString
      }
      )

      s.append(zip.mkString("", s",$sep$innerIndent", ""))
    } else {
      s.append(_componentTypes.map(e => s":$e").mkString("", s", $sep$innerIndent", ""))
    }

    s.append(s"$sep>")

    TupleDataType.toStringLevel -= 1

    s.toString
  }

  def itemsAsFields() = {
    fieldNames.zip(componentTypes).map(f => new Field(nameNode = f._1, declDataType = f._2))
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    if (!other.isInstanceOf[TupleDataType])
      false
    else {
      val othert = other.asInstanceOf[TupleDataType]

      fieldNames.equals(othert.fieldNames) &&
        _componentTypes.size == othert._componentTypes.size &&
        _componentTypes.zip(othert._componentTypes).map(e => e._1.isAssignableFrom(e._2)).foldLeft(true)(_ && _)
    }

  override def preResolve(ctx: Context): Unit = {
    val mismatch = fieldNames.size > 0 && _componentTypes.size != fieldNames.size
    if (mismatch) {
      ctx.addResolutionError(ResolutionMessage(fieldNames(0).location, TupleNamesSizeMismatch)(None, List.empty, fieldNames(0).text))
    }

    val idxs = (1 to _componentTypes.size).zip(_componentTypes)
    val names = if (fieldNames.size == 0 || mismatch)
      idxs.map(e => StringNode(e._2.location, SynthNames.TupleField + e._1))
    else
      fieldNames

    val zipped: Seq[(StringNode, DataType)] = names.zip(_componentTypes)

    val fields: Seq[FieldOrFieldRef] = zipped.map(e => new Field(e._1.location, NodeMeta.empty, false,
      e._1, e._2)).map(new FieldOrFieldRef(_))

    val parentUdt: UdtBaseNode = locateUdtParent()
    val parentField = locateFieldParent(false)
    val fieldNameId = if (parentField.isDefined) parentField.get._2.nameNode.text + parentField.get._1 else location.getStartLine

    val loc = if (fields.size > 0)
      fields(0).location
    else
      TokenImpl.empty

    _syntheticRecord = Some(new SyntheticRecord(
      loc, parentUdt.namespaceNode, rawNodeMeta, Seq.empty,
      StringNode(location, parentUdt.versionedName.name + SyntheticTypeFieldSeparator + fieldNameId),
      None, None, None, None, Seq.empty, fields, Some(locateUdtParent()), Seq.empty))

    _syntheticRecord.get.startPreResolve(ctx, this)

    ctx.registry.registerUdt(_syntheticRecord.get)

    super.preResolve(ctx)
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      if (_syntheticRecord.isDefined)
        _syntheticRecord.get.traverse(v)
      //    _componentTypes.foreach(_.traverse(v))
      v.exit(this)
    }
  }

  override def unwrapTypedef: DataType = {
    _componentTypes = _componentTypes.map(_.unwrapTypedef)
    this
  }
}
