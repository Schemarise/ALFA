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
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType

abstract class MethodBodyContainer(ctx: Option[Context], token: IToken,
                                   namespace: NamespaceNode,
                                   nodeMeta: NodeMeta,
                                   name: StringNode,
                                   versionNo: Option[IntNode],
                                   methodDeclarations: Seq[MethodDeclaration],
                                   modifiers: Seq[ModifierNode],
                                   imports: Seq[ImportDef]
                                  )
  extends UdtBaseNode(
    ctx, token, namespace, nodeMeta, modifiers, name, versionNo, None, None, None, Seq.empty, Seq.empty,
    methodDeclarations, Seq.empty, Seq.empty, imports)
    with TraversableNode {

  override def nodeType: Nodes.NodeType = Nodes.Library

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    super.resolvableInnerNodes() ++ methodDeclarations
  }

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode = ???

  override def udtType: UdtType = UdtType.library

  override def getMethodDecls() = {
    allAccessibleMethods
  }

  override def getMethodSignatures: Map[String, MethodSignature] = {
    allAccessibleMethods.map(m => m._1 -> m._2.signature)
  }
}