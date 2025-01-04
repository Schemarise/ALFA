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
package com.schemarise.alfa.compiler.ast

import com.schemarise.alfa.compiler.ast.model.{IAnnotation, INamespaceNode, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.types.Nodes.NodeType
import com.schemarise.alfa.compiler.utils.TokenImpl

import scala.collection.mutable.ListBuffer

case class Namespace private(name: String) extends IdentifiableNode with INamespaceNode with DocumentableNode {
  override def toString: String = name

  val nodeId = new LocatableNodeIdentity(getClass.getSimpleName, name)(TokenImpl.empty)

  def isEmpty: Boolean = name.length == 0

  override def traverse(v: NodeVisitor): Unit = {}

  override def docs: Seq[IDocumentation] = ???

  override def annotations: Seq[IAnnotation] = ???

  override def parentNamespaces: Seq[INamespaceNode] = {
    val c: Seq[String] = name.split('.')

    val b = new ListBuffer[Namespace]()

    c.zipWithIndex.map(e => {
      val ns = c.take(e._2)
      b += Namespace(ns.mkString("."))
    })

    b
  }

  override def docNodes: Seq[IDocumentation] = docs

  override val location: IToken = null
}

object Namespace {
  val empty: Namespace = Namespace("")

  def apply(text: String): Namespace = {
    new Namespace(text)
  }
}
