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
package com.schemarise.alfa.compiler.ast.antlrvisitors

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser.EnumDeclContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType

class EnumDeclVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[EnumDecl](resolveCtx) {
  override def visitEnumDecl(ctx: EnumDeclContext): EnumDecl = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val modifiers: Seq[ModifierNode] = readModifierNodes(ctx.modifiers())
    val name: StringNode = readStringNode(ctx.idOrQid())
    val versionNo: Option[IntNode] = readOptVersion(ctx.versionMarker())
    val includes: Seq[UdtDataType] = readIncludes(resolveCtx, ctx.optIncludesList(), namespace)
    val fields: Seq[FieldOrFieldRef] = readFieldOrFieldRefs(resolveCtx, namespace, ctx.children)

    val o = new EnumDecl(Some(resolveCtx), token, namespace, meta, modifiers, name, versionNo, includes, fields, imports)
    resolveCtx.registry.registerUdt(o)
  }
}
