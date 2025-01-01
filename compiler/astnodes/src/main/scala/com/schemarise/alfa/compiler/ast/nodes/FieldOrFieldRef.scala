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

import com.schemarise.alfa.compiler.ast.model.{IToken, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.{IToken, _}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.ast.{BaseNode, LocatableNodeIdentity, ResolvableNode, TemplateableNode}

class FieldOrFieldRef private(fieldRef: Option[StringNode], rawfield: Option[Field])
  extends BaseNode with ResolvableNode with TemplateableNode with IdentifiableNode {
  val location: IToken = if (fieldRef.isDefined) fieldRef.get.location else rawfield.get.location

  var field: Option[Field] = None

  def this(fieldRef: StringNode) {
    this(Some(fieldRef), None)
  }

  def this(f: Field) {
    this(None, Some(f))
    field = rawfield
  }

  override def nodeType: Nodes.NodeType = Nodes.FieldOrRefNode

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    field = if (fieldRef.isDefined) {
      val of = ctx.registry.getField(fieldRef.get)
      if (of.isDefined) {
        val f = of.get.templateInstantiate(ctx, Map.empty)
        Some(f)
      }
      else
        None
    }
    else
      rawfield

    if (field.isDefined)
      field.get.startPreResolve(ctx, this)
  }

  override def resolvableInnerNodes() =
    if (field.isDefined)
      Seq(field.get)
    else
      Seq.empty

  override def toString: String = if (fieldRef.isDefined) fieldRef.get.toString else rawfield.get.toString

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): FieldOrFieldRef = {
    if (fieldRef.isDefined)
      this
    else {
      val f = rawfield.get.templateInstantiate(resolveCtx, templateArgs)
      new FieldOrFieldRef(None, Some(f))
    }
  }


  override def nodeId = {
    val sn = if (fieldRef.isDefined)
      fieldRef.get
    else
      rawfield.get.nameNode

    new LocatableNodeIdentity(getClass.getSimpleName, sn.text)(sn.location)
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (field.isDefined)
      field.get.traverse(v)
  }
}
