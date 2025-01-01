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
import com.schemarise.alfa.compiler.antlr.AlfaParser.{ExpressionSequenceContext, ExpressionUnitContext}
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.expr.IObjectExpression
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.err.ExpressionError

class Extension(resctx: Context, location: IToken, namespace: NamespaceNode, meta: NodeMeta,
                val extensionTypeNode: StringNode,
                identifier: StringNode,
                val attribs: Seq[(StringNode, ExpressionUnitContext)],
                imports: Seq[ImportDef]

               )
  extends UdtBaseNode(ctx = Some(resctx), location = location,
    declaredRawNamespace = namespace,
    declaredRawName = identifier,
    assertNodes = Seq.empty,
    imports = imports
  ) {

  var objectExpression: Option[IObjectExpression] = None
  var resolvedType: Option[UdtBaseNode] = None

  override def nodeType: Nodes.NodeType = Nodes.ExtensionInstance

  override def preResolve(ctx: Context): Unit = {
    resolvedType = ctx.registry.getExtension(Some(this), new UdtDataType(location = extensionTypeNode.location, namespaceNode = namespace, name = extensionTypeNode))

    attribs.map(_._1.text).groupBy(e => e).filter(e => e._2.size > 1).foreach(e => {
      ctx.addResolutionError(this, ExpressionError, s"Duplicate attribute '${e._1}'")
    })

    if (resolvedType.isDefined) {
      val ext = resolvedType.get.asInstanceOf[ExtensionDecl]

      ext.startPreResolve(ctx, ext.namespaceNode)
    }
    else {
      ctx.addResolutionError(this, ExpressionError, s"Unknown extension '${extensionTypeNode.text}'")
    }

    super.preResolve(ctx)
  }

  /**
   * Includes and Extends are template instantiated, fields should be left-as-is - i.e. templates if they are.
   */
  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = this

  override def udtType: UdtType = UdtType.extensionInstance

  override def traverse(v: NodeVisitor): Unit = {}
}
