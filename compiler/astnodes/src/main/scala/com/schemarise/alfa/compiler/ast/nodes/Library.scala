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

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.TokenImpl

class Library(ctx: Option[Context], token: IToken = TokenImpl.empty,
              namespace: NamespaceNode,
              nodeMeta: NodeMeta = NodeMeta.empty,
              name: StringNode,
              versionNo: Option[IntNode] = None,
              methodDeclarations: Seq[MethodDeclaration],
              modifiers: Seq[ModifierNode] = Seq.empty,
              imports: Seq[ImportDef] = Seq.empty
             )
  extends MethodBodyContainer(ctx, token, namespace, nodeMeta, name, versionNo, methodDeclarations, modifiers, imports)
    with TraversableNode with ILibrary {

  override def nodeType: Nodes.NodeType = Nodes.Library

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override def udtType: UdtType = UdtType.library
}