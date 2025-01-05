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
package com.schemarise.alfa.compiler.ast.nodes

import com.schemarise.alfa.compiler.ast.model.NodeVisitor
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.expr.IBlockExpression
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.{IMethodDeclaration, IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.TokenImpl

class MethodDeclaration(val location: IToken = TokenImpl.empty, val signature: MethodSignature, val block: IBlockExpression)
  extends BaseNode with ResolvableNode with TemplateableNode with TraversableNode with IMethodDeclaration {
  override def nodeType: Nodes.NodeType = Nodes.MethodDeclarationNode

  override def toString: String = {
    val sb = new StringBuilder

    sb.append(signature.toString + " ")
    sb.append(block.toString)
    sb.toString()
  }

  def toGraph() = {
  }

  override def resolvableInnerNodes(): Seq[ResolvableNode] = Seq.empty

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = ???

  override def traverse(v: NodeVisitor): Unit = {}
}
