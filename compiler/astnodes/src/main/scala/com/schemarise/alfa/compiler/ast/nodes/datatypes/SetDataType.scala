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

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.{ResolutionMessage, SetCannotContainCollection}
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl

case class SetDataType(declComponentType: DataType)(val location: IToken = TokenImpl.empty,
                                                    override val sizeRange: Option[IntRangeNode] = None,
                                                    uniqueFieldsOverride: Seq[StringNode] = Nil) extends VectorDataType(sizeRange) with ISetDataType {
  override val vectorType: VectorType = Vectors.set

  private var _componentType: DataType = declComponentType

  override def componentType: DataType = _componentType

  def sizeMin = if (sizeRange.isDefined) Some(sizeRange.get.min) else None

  def sizeMax = if (sizeRange.isDefined) Some(sizeRange.get.max) else None

  override def resolvableInnerNodes() = Seq(componentType)

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    SetDataType(componentType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])(location, sizeRange, uniqueFieldsOverride)

  override def fieldDataType() = AllFieldTypes.set

  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    other.isInstanceOf[SetDataType] &&
      // sizeRange.equals( other.asInstanceOf[SetDataType].sizeRange ) &&
      componentType.isAssignableFrom(other.asInstanceOf[SetDataType].componentType)
  //uniqueFieldsOverride.equals( other.asInstanceOf[SetDataType].uniqueFieldsOverride


  override def unwrapTypedef: DataType = {
    _componentType = componentType.unwrapTypedef
    this
  }

  override def preResolve(ctx: Context): Unit = {
    if (declComponentType.isInstanceOf[IVectorDataType]) {
      ctx.addResolutionError(ResolutionMessage(declComponentType.location, SetCannotContainCollection)(None, List.empty))
    }

    super.preResolve(ctx)
  }

  override def toString: String = {
    val r1 = if (sizeRange.isDefined) {
      s"(${sizeRange.get.minNode.toString},${sizeRange.get.maxNode.toString})"
    }
    else ""

    val r2 = if (r1.equals("(0,*)")) "" else r1

    "set< " + componentType.toString + s" >$r2"

  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      componentType.traverse(v)
      v.exit(this)
    }
  }


}
