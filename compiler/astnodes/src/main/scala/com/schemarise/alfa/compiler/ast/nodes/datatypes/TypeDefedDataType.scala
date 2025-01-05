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

import com.schemarise.alfa.compiler.ast.model.{IDocumentation, IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.IAssignable
import com.schemarise.alfa.compiler.ast.nodes.{StringNode, TypeParameter}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.utils.TextUtils

case class TypeDefedDataType(val meta: NodeMeta,
                             val newType: StringNode,
                             val typeParams: Option[Seq[TypeParameter]],
                             val referencedType: DataType
                            ) extends DataType {
  val location: IToken = newType.location

  override def resolvableInnerNodes() = Seq(referencedType)

  override def toString: String = {
    val sb = new StringBuilder

    sb.append("\n")
    if (meta.docs.size > 0)
      sb ++= meta.docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")

    sb.append(newType)

    if (typeParams.size > 0)
      sb.append(typeParams.mkString("<", ", ", ">"))

    sb.append(" = ")
    sb.append(referencedType.toString)

    sb.toString()
  }

  override def fieldDataType() = AllFieldTypes.typedef

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    TypeDefedDataType(meta, newType, typeParams, referencedType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])

  override def traverse(v: NodeVisitor): Unit = {}

  override def preResolve(ctx: Context): Unit = {
    ctx.registry.pushTypeParameters(typeParams)
    super.preResolve(ctx)
    ctx.registry.popTypeParameters(typeParams)
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) = {
    if (!other.isInstanceOf[TypeDefedDataType])
      false
    else {
      val othert = other.asInstanceOf[TypeDefedDataType]

      newType.equals(othert.newType) &&
        typeParams.equals(othert.typeParams)
      referencedType.equals(othert.referencedType)
    }
  }

  override def docs: Seq[IDocumentation] = {
    meta.docs
  }

  override def unwrapTypedef: DataType = this
}
