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
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IAnnotationDecl, IToken, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.types.AnnotationTargetType.TargetType

class ExtensionDecl(ctx: Option[Context], token: IToken,
                    nodeMeta: NodeMeta,
                    val nameNode: StringNode,
                    fields: Seq[FieldOrFieldRef],
                    imports: Seq[ImportDef]
                   )
  extends UdtBaseNode(ctx, token, NamespaceNode.empty, nodeMeta, List.empty, nameNode,
    rawFieldRefs = fields, assertNodes = Seq.empty, imports = imports) with IAnnotationDecl {

  override def nodeType: Nodes.NodeType = Nodes.Extension

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override def preResolve(ctx: Context): Unit =
    super.preResolve(ctx)

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = ???

  override def udtType: UdtType = UdtType.extension
}
