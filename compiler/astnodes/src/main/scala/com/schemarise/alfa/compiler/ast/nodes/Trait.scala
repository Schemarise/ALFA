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
import com.schemarise.alfa.compiler.ast.model.types.{IUdtDataType, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IToken, ITrait, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{IncludedByNeedsToBeTransitive, IncludedByTypeDoesNotIncludesThisTrait}
import com.schemarise.alfa.compiler.utils.{TextUtils, TokenImpl}

class Trait(ctx: Option[Context] = None, token: IToken = TokenImpl.empty,
            namespace: NamespaceNode = NamespaceNode.empty,
            nodeMeta: NodeMeta = NodeMeta.empty,
            modifiers: Seq[ModifierNode] = Seq.empty,
            nameNode: StringNode,
            version: Option[IntNode] = None,
            typeParams: Option[Seq[TypeParameter]] = None,
            typeArguments: Option[Map[String, DataType]] = None,
            includes: Seq[UdtDataType] = Seq.empty,
            fields: Seq[FieldOrFieldRef] = Seq.empty,
            assertNode: Seq[AssertDeclaration] = Seq.empty,
            linkageNodes: Seq[LinkageDeclaration] = Seq.empty,
            imports: Seq[ImportDef] = Seq.empty,
            val rawScope: Option[Seq[UdtDataType]] = None,
           )
  extends UdtBaseNode(
    ctx, token, namespace, nodeMeta, modifiers, nameNode, version, typeParams, typeArguments, None,
    includes, fields, Seq.empty, assertNode, linkageNodes, imports) with ITrait {

  override def scope: Seq[UdtDataType] = if (rawScope.isDefined) rawScope.get else Seq.empty

  override def nodeType: Nodes.NodeType = Nodes.Trait

  override def udtType: UdtType = UdtType.`trait`

  override def hasScope = scope.size > 0

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    scope ++ super.resolvableInnerNodes()
  }

  //  override def templateInstantiated : Trait = {
  //    if ( ! typeArguments.isDefined ) {
  //      this
  //    } else {
  //      val resolveCtx = ctx.get
  //      val tmpls = typeArgumentsNode.get
  //
  //      val params = filterParamsFromArgs
  //
  //      val t = new Trait(
  //        ctx, token,
  //        namespace,
  //        nodeMeta,
  //        modifiers,
  //        nameNode.templatedName(params, tmpls.values),
  //        version,
  //        params,
  //        None,
  //        includes.map(e => e.templateInstantiate(resolveCtx, tmpls).asInstanceOf[UdtDataType]),
  //        fields.map(e => e.templateInstantiate(resolveCtx, tmpls)),
  //        methodDecls.map(e => e.templateInstantiate(resolveCtx, tmpls)),
  //        methodSignatures.map(e => e.templateInstantiated)
  //      )
  //
  //      t
  //    }
  //  }

  override def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                    typeArguments: Map[String, DataType]): UdtBaseNode = {

    assertTrue(typeParamsNode.isDefined && !typeArguments.isEmpty)

    new Trait(
      ctx, token,
      namespace,
      nodeMeta,
      modifiers,
      nameNode,
      version,
      params,
      Some(typeArguments),
      includes.map(e => e.templateInstantiate(resolveCtx, typeArguments).asInstanceOf[UdtDataType]),
      fields.map(e => e.templateInstantiate(resolveCtx, typeArguments)),
      assertNode
    )
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)
  }

  override def resolve(ctx: Context): Unit = {
    super.resolve(ctx)

    scope.filter(e => !e.hasErrors && e.resolvedType.isDefined).foreach(x => {
      val e = x.resolvedType.get;
      val sz = e.includes.filter(i => i.fullyQualifiedName == this.name.fullyQualifiedName).size

      if (sz == 0) {
        ctx.addResolutionError(x, IncludedByTypeDoesNotIncludesThisTrait, e.name.name, this.nameNode.text)
      }
    })

    if (rawScope.isEmpty) {
      includes.filter(e => !e.hasErrors && e.resolvedType.isDefined).map(_.resolvedType.get).foreach(i => {
        if (i.asInstanceOf[Trait].rawScope.isDefined) {
          ctx.addResolutionError(this, IncludedByNeedsToBeTransitive, i.name.name, this.nameNode.text)
        }
      })
    }
  }

  override def toStringBeforeBody() = {
    if (scope.size > 0)
      " scope " + scope.mkString("", ", ", "")
    else
      ""
  }


  def traverseScopes(v: NodeVisitor): Unit = {
    scope.foreach(s => s.traverse(v))
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseScopes(v)
      traverseBody(v)
    }
    v.exit(this)
  }
}
