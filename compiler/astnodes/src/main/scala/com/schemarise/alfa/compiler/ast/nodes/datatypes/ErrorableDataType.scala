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

import com.schemarise.alfa.compiler.ast.model.NodeVisitor
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IErrorDataType}
import com.schemarise.alfa.compiler.ast.nodes.Locatable
import com.schemarise.alfa.compiler.ast.{ResolvableNode, TemplateableNode}
import com.schemarise.alfa.compiler.err.{ExpressionError, ParserError, ResolutionMessage}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.utils.TokenImpl

case class ErrorableDataType(cause: Either[ResolutionMessage, Locatable]) extends DataType with IErrorDataType {
  override def fieldDataType(): FieldType = AllFieldTypes.udt

  override def unwrapTypedef: DataType = this

  override def resolvableInnerNodes(): Seq[ResolvableNode] = Seq.empty

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = false

  override val location: IToken = TokenImpl.empty

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = this

  override def traverse(v: NodeVisitor): Unit = {}

  override def preResolve(ctx: Context): Unit = {
    // if there are error, another would have caused this
    if (ctx.getErrors().size == 0) {
      if (cause.isRight) {
        val r = cause.right.get
        if (r.isInstanceOf[ResolvableNode] && !r.asInstanceOf[ResolvableNode].hasErrors)
          ctx.addResolutionError(r.asInstanceOf[ResolvableNode], ExpressionError, "Failed to resolve datatype")
        else
          ctx.addResolutionError(cause.right.get.location, ExpressionError, "Failed to resolve datatype for expression at location")
      }
    }

    super.preResolve(ctx)
  }

  override def toString: String = {
    "errored-data-type"
  }
}
