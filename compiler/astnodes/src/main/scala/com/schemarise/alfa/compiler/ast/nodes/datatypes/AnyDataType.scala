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
import com.schemarise.alfa.compiler.ast.TemplateableNode
import com.schemarise.alfa.compiler.ast.model.types.{IAnyDataType, IAssignable, Scalars}
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.err.ExpressionError
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.utils.TokenImpl

object AnyDataType {
  val anyType = new AnyDataType(TokenImpl.empty)
}

class AnyDataType(val location: IToken = TokenImpl.empty) extends DataType with IAnyDataType {

  override def resolvableInnerNodes() = Seq.empty

  override def toString: String = {
    "$any"
  }

  override def unwrapTypedef: DataType = {
    this
  }

  override def traverse(v: NodeVisitor): Unit = {
    v.enter(this)
    v.exit(this)
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)
    val parent = locateUdtParent()
    if (!parent.isService && !parent.isTestcase && !parent.isMethodSig && !parent.isAnnotation) {
      ctx.addResolutionError(this, ExpressionError, "$any can only be used within a service, annotation or testcase. Not in a " + parent.udtType)
    }
  }

  override def fieldDataType(): FieldType = ???

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = true

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = this
}
