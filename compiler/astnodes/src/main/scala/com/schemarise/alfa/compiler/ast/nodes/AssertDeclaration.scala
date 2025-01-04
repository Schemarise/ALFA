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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr.IBlockExpression
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, Nodes}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, EnclosingDataType, ListDataType, ScalarDataType}
import com.schemarise.alfa.compiler.utils.TokenImpl

import scala.collection.mutable

class AssertDeclaration(val location: IToken = TokenImpl.empty,
                        nodeMeta: NodeMeta = NodeMeta.empty,
                        val assertNameNode: StringNode,
                        val block: IBlockExpression,
                        arg: Option[StringNode] = None, imports: Seq[ImportDef] = Seq.empty
                       )
  extends BaseNode
    with ResolvableNode
    with TemplateableNode
    with TraversableNode
    with DocumentableNode
    with IAssertDeclaration {

  private var _asMethodDecl: MethodDeclaration = null

  override def nodeType: Nodes.NodeType = Nodes.AssertNode

  override def name: String = assertNameNode.text

  override def resolvableInnerNodes() = {
    Seq.empty
  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): AssertDeclaration = ???

  override def traverse(v: NodeVisitor): Unit = {
  }

  def asMethodDeclaration() = {
    _asMethodDecl
  }

  def parentUdt() = locateUdtParent()

  override def toString: String = {
    val sb = new StringBuilder

    if (docs.size > 0)
      sb ++= docs.map("" + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")

    sb.append(s"\nassert ${assertNameNode.text} \n")
    sb.append(block.toString)
    sb.append("\n")
    sb.toString()
  }

  override def docs: Seq[IDocumentation] = nodeMeta.docs

  override def annotations: Seq[IAnnotation] = nodeMeta.annotations

  override def argName: Option[String] = if (arg.isDefined) Some(arg.get.text) else None

  override def collectionAssert: Boolean = arg.isDefined

  override def docNodes: Seq[IDocumentation] = docs
}
