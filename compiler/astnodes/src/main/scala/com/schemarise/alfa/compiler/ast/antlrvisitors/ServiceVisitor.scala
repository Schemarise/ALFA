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
import com.schemarise.alfa.compiler.antlr.AlfaParser.ServiceDeclContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._

class ServiceVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef])
  extends WithContextVisitor[Service](resolveCtx) {
  override def visitServiceDecl(ctx: ServiceDeclContext): Service = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val name: StringNode = readStringNode(ctx.name)
    val versionNo: Option[IntNode] = readOptVersion(ctx.versionMarker())
    val modifiers: Seq[ModifierNode] = readModifierNodes(ctx.modifiers())

    val typeParams = readTypeParameters(resolveCtx, ctx.typeParameters(), namespace)

    val v = new FieldOrFieldRefVisitor(resolveCtx, namespace)
    val ctorParams = if (ctx.optFunctionParams() == null || ctx.optFunctionParams().field() == null) Seq.empty
    else j2sNoParseExcpStream(ctx.optFunctionParams().field()).map(v.visitField(_))


    val sigs: Seq[MethodSignature] = j2sNoParseExcpStream(ctx.methodSignature).map(e => readMethodSigs(namespace, e, imports))
    val o = new Service(Some(resolveCtx), token, namespace, meta, name, versionNo, typeParams, None, ctorParams, sigs, modifiers, imports)

    resolveCtx.registry.registerUdt(o)
  }
}
