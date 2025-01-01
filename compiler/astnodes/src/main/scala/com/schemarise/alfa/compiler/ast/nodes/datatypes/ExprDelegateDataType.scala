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
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IDataType, IExprDelegateType}
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.types._

case class ExprDelegateDataType(location: IToken) extends DataType with IExprDelegateType {

  private var delegate: Option[DataType] = None

  override def fieldDataType(): FieldType = AllFieldTypes.exprDelegate

  override def toString: String = {
    if (delegate.isDefined)
      delegate.get.toString
    else
      "NonDelegated"
  }

  override def unwrapTypedef: DataType = {
    if (delegate.isEmpty)
      // throw new com.schemarise.alfa.compiler.AlfaInternalException("Delegate type not assigned")
      this
    else
      delegate.get
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = {
    if (delegate.isEmpty)
      false
    else
      unwrapTypedef.isAssignableFrom(other.asInstanceOf[DataType].unwrapTypedef)
  }

  override def traverse(v: NodeVisitor): Unit = {}

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = ???

  override def resolvableInnerNodes(): Seq[ResolvableNode] = ???

  def assignDelegateType(t: IDataType): Unit = {
    delegate = Some(t.asInstanceOf[DataType])
  }
}
