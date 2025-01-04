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
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IDataTypeSizeRange, IListDataType, Vectors}
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl

case class ListDataType(val declComponentType: DataType)(val location: IToken = TokenImpl.empty,
                                                         override val sizeRange: Option[IntRangeNode] = None
) extends VectorDataType(sizeRange) with IListDataType {
  override val vectorType: VectorType = Vectors.list

  private var _componentType: DataType = declComponentType

  override def componentType: DataType = _componentType

  override def resolvableInnerNodes() = Seq(componentType)

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    ListDataType(componentType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])(location, sizeRange)

  override def fieldDataType() = AllFieldTypes.seq

  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    other.isInstanceOf[ListDataType] &&
      //      sizeRange.equals( other.asInstanceOf[ListDataType].sizeRange ) &&
      componentType.isAssignableFrom(other.asInstanceOf[ListDataType].componentType)

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      componentType.traverse(v)
      v.exit(this)
    }
  }

  override def unwrapTypedef: DataType = {
    _componentType = componentType.unwrapTypedef
    this
  }

  override def toString: String = {
    val r1 = if (sizeRange.isDefined) {
      s"(${sizeRange.get.minNode.toString},${sizeRange.get.maxNode.toString})"
    }
    else ""

    val r2 = if (r1.equals("(0,*)")) "" else r1

    "list< " + componentType.toString + s" >$r2"
  }
}
