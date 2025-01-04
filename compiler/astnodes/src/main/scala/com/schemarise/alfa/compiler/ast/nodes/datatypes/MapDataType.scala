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

import com.schemarise.alfa.compiler.ast.model.{IToken, NoOpNodeVisitor, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.{NotPermittedAsMapKey, ResolutionMessage}
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl
import scala.collection.JavaConverters._

case class MapDataType(val declKeyType: DataType,
                       val declValueType: DataType)(
                        val location: IToken = TokenImpl.empty,
                        override val sizeRange: Option[IntRangeNode] = None,
                        val keyNameNode: Option[StringNode] = None,
                        val valueNameNode: Option[StringNode] = None) extends VectorDataType(sizeRange) with IMapDataType {
  override val vectorType: VectorType = Vectors.map

  def keyType = _keyType

  def valueType = _valueType

  def keyName = if (keyNameNode.isDefined) Some(keyNameNode.get.text) else None

  def valueName = if (valueNameNode.isDefined) Some(valueNameNode.get.text) else None

  private var _keyType: DataType = declKeyType
  private var _valueType: DataType = declValueType

  override def resolvableInnerNodes() = Seq(_keyType, _valueType)

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    keyType.traverse(new NoOpNodeVisitor() {
      val visited = new java.util.Stack[String]()

      override def enter(e: IUdtDataType): Mode = {
        val u = e.asInstanceOf[UdtDataType]

        if (!u.hasErrors && u.isPreResolved()) {
          val v = e.asInstanceOf[UdtDataType].udt

          e.asInstanceOf[UdtDataType].parent.isInstanceOf[UdtBaseNode]

          if (v.isTrait && !v.isTraitWithScope && e.referencedFromField) {
            ctx.addResolutionWarning(new ResolutionMessage(keyType.location, NotPermittedAsMapKey)(None, List.empty, "trait " + v.name.fullyQualifiedName, visited.asScala.mkString(" > ")))
          }
          if (!visited.contains(v.name.fullyQualifiedName)) {
            visited.push(v.name.fullyQualifiedName)
            v.traverse(this)
            visited.pop()
          }
        }
        super.enter(e)
      }

      override def enter(e: IScalarDataType): Mode = {
        if (e.isScalarDouble) {
          ctx.addResolutionWarning(new ResolutionMessage(keyType.location, NotPermittedAsMapKey)(None, List.empty, e, visited.asScala.mkString(" > ")))
        }
        super.enter(e)
      }

      override def exit(e: IEnclosingDataType): Unit = {
        super.exit(e)
        if (e.isStream || e.isFuture || e.isTabular || e.isTry)
          ctx.addResolutionError(new ResolutionMessage(e.asInstanceOf[Locatable].location, NotPermittedAsMapKey)(None, List.empty, e.encType.toString, visited.asScala.mkString(" > ")))
      }
    })

  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    MapDataType(
      _keyType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType],
      _valueType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])(
      location, sizeRange,
      keyNameNode,
      valueNameNode
    )

  override def fieldDataType() = AllFieldTypes.map

  // enough for K & V to be assignable, other fields are not structural
  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    other.isInstanceOf[MapDataType] &&
      _keyType.isAssignableFrom(other.asInstanceOf[MapDataType]._keyType) &&
      _valueType.isAssignableFrom(other.asInstanceOf[MapDataType]._valueType)

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      _keyType.traverse(v)
      _valueType.traverse(v)
      v.exit(this)
    }
  }

  override def unwrapTypedef: DataType = {
    _keyType = _keyType.unwrapTypedef
    _valueType = _valueType.unwrapTypedef
    this
  }

  override def toString: String = {
    val s = new StringBuilder
    s.append("map< ")

    if (keyNameNode.isDefined) s.append(keyNameNode.get + " : ")
    s.append(_keyType.toString)
    s.append(" , ")

    if (valueNameNode.isDefined) s.append(valueNameNode.get + " : ")
    s.append(_valueType.toString)
    s.append(" >")
    s.toString
  }
}
