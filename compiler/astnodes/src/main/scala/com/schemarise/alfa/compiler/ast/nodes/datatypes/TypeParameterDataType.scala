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
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, ITypeParameterDataType}
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes.TypeParameter
import com.schemarise.alfa.compiler.ast.ResolvableNode
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType

case class TypeParameterDataType(tp: TypeParameter) extends DataType with ITypeParameterDataType {
  override def fieldDataType(): FieldType = AllFieldTypes.typeParameter

  override def unwrapTypedef: DataType = this

  override def traverse(v: NodeVisitor): Unit = {}

  override def resolvableInnerNodes(): Seq[ResolvableNode] = Seq.empty

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = ???

  override val location: IToken = tp.location

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType = {
    val o = templateArgs.get(tp.nameNode.text)
    if (o.isDefined)
      o.get.asTemplateDerived
    else
      this
  }

  override def toString: String = tp.toString

  override def parameterName: String = tp.name.name
}
