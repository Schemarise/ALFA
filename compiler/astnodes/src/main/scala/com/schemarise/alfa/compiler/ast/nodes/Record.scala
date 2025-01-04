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

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IRecord, IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.utils.TokenImpl

class Record(ctx: Option[Context] = None,
             token: IToken = TokenImpl.empty,
             namespace: NamespaceNode = NamespaceNode.empty,
             nodeMeta: NodeMeta = NodeMeta.empty,
             modifiersNode: Seq[ModifierNode] = Seq.empty,
             nameNode: StringNode,
             versionNo: Option[IntNode] = None,
             typeParamsNode: Option[Seq[TypeParameter]] = None,
             typeArgumentsNode: Option[Map[String, DataType]] = None,
             extendedNode: Option[UdtDataType] = None,
             includesNode: Seq[UdtDataType] = Seq.empty,
             fields: Seq[FieldOrFieldRef] = Seq.empty,
             assertNode: Seq[AssertDeclaration] = Seq.empty,
             linkageNodes: Seq[LinkageDeclaration] = Seq.empty,
             imports: Seq[ImportDef] = Seq.empty
            )
  extends UdtBaseNode(
    ctx, token, namespace: NamespaceNode, nodeMeta, modifiersNode, nameNode,
    versionNo, typeParamsNode, typeArgumentsNode, extendedNode, includesNode,
    fields, Seq.empty, assertNode, linkageNodes, imports) with IRecord {

  override def nodeType: Nodes.NodeType = Nodes.Record

  //  override def templateInstantiated : UdtBaseNode = {
  //    if ( ! typeParamsNode.isDefined ) {
  //      this
  //    } else {
  //      val resolveCtx = ctx.get
  //      val typeArguments = typeArgumentsNode.get
  //
  //      val params = filterParamsFromArgs
  //
  //      val t = new Record(
  //        ctx, token,
  //        namespace,
  //        nodeMeta,
  //        modifiersNode,
  //        nameNode.templatedName(params, typeArguments.values),
  //        versionNo,
  //        params,
  //        None,
  //        templateInstantiate( extendedNode, resolveCtx, typeArguments ),
  //        includesNode.map(e => e.templateInstantiate(resolveCtx, typeArguments).asInstanceOf[UdtDataType] ),
  //        fieldsNode.map(e => e.templateInstantiate(resolveCtx, typeArguments)),
  //        methodDecls.map( e => e.templateInstantiate(resolveCtx, typeArguments))
  //      )
  //      t
  //    }
  //  }

  override def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode = {
    assertTrue(typeParamsNode.isDefined && !typeArguments.isEmpty)

    new Record(
      ctx, token,
      namespace,
      nodeMeta,
      modifiersNode,
      nameNode,
      versionNo,
      params,
      Some(typeArguments),
      templateInstantiate(extendedNode, resolveCtx, typeArguments),
      includesNode.map(e => e.templateInstantiate(resolveCtx, typeArguments).asInstanceOf[UdtDataType]),
      fields.map(e => e.templateInstantiate(resolveCtx, typeArguments)),
      assertNode,
      linkageNodes,
      imports
    )
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override val isSynthetic: Boolean = false

  override def udtType: UdtType = UdtType.record
}
