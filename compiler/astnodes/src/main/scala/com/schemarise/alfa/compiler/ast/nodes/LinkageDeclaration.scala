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
import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.ast.model.types.{IUdtDataType, Nodes}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{UdtDataType}
import com.schemarise.alfa.compiler.utils.TokenImpl

class LinkageDeclaration(val location: IToken = TokenImpl.empty,
                         nodeMeta: NodeMeta = NodeMeta.empty,
                         val linkageNameNode: StringNode,
                         val isList: Boolean,
                         val isOptional: Boolean,
                         rawSourceExpressions: Seq[Expression],
                         rawTargetType: UdtDataType,
                         rawTargetExpressions: Seq[Expression]
                        )
  extends BaseNode
    with ResolvableNode
    with TraversableNode
    with DocumentableNode
    with ILinkageDeclaration {

  private var _asMethodDecl: MethodDeclaration = null

  override def nodeType: Nodes.NodeType = Nodes.AssertNode

  override def name: String = linkageNameNode.text

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      sourceExpressions.foreach(e => e.traverse(v))
      rawTargetType.traverse(v)
      targetExpressions.foreach(e => e.traverse(v))
    }
    v.exit(this)
  }

  def asMethodDeclaration() = {
    _asMethodDecl
  }

  def parentUdt() = locateUdtParent()

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

  }

  override protected def resolve(ctx: Context): Unit = {

    rawTargetType.startResolve(ctx)

    if (rawTargetType.resolvedType.isDefined) {
      val d = rawTargetType.resolvedType.get
      ctx.registry.pushUdtFields(ctx, d)
      ctx.registry.popUdtFields()
    }

    ctx.registry.pushUdtFields(ctx, this.parentUdt())
    super.resolve(ctx)
    ctx.registry.popUdtFields()
  }

  override protected def postResolve(ctx: Context): Unit = {
    ctx.registry.pushUdtFields(ctx, this.parentUdt())
    super.postResolve(ctx)
    ctx.registry.popUdtFields()
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append(s"\n      linkage ${linkageNameNode.text} \n")
    sb.append(indent(sourceExpressions.mkString("(", ",", ")"), "      "))
    sb.append(" => ")
    if (isList)
      sb.append("list<")

    sb.append(targetType.fullyQualifiedName)

    if (isList)
      sb.append(">")

    if (isOptional)
      sb.append("?")

    sb.append(indent(targetExpressions.mkString("(", ",", ")"), "      "))
    sb.append("\n ")
    sb.toString()
  }

  override def docs: Seq[IDocumentation] = nodeMeta.docs

  override def annotations: Seq[IAnnotation] = nodeMeta.annotations

  override def docNodes: Seq[IDocumentation] = docs

  override def sourceExpressions: Seq[IExpression] = rawSourceExpressions

  override def targetType: IUdtDataType = rawTargetType

  override def targetExpressions: Seq[IExpression] = rawTargetExpressions
}
