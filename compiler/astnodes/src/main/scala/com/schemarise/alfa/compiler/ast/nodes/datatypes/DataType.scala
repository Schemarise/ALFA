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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IDataType, Nodes}
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.types._

trait DataType extends BaseNode
  with ResolvableNode
  with TemplateableNode
  with IAssignable
  with TraversableNode with IDataType {

  private var typedefFrom: Option[UdtDataType] = None

  def typeDefedFrom(udtReference: UdtDataType) = {
    typedefFrom = Some(udtReference)
  }

  def asTemplateDerived: DataType = this

  def wasTemplateDerived = false

  override def nodeType: Nodes.NodeType = Nodes.DataTypeNode

  def fieldDataType(): FieldType

  lazy val isNullable: Boolean = {
    isEncOptional()
  }

  def unwrapTypedef: DataType

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    if (!hasErrors)
      unwrapTypedef
  }
}