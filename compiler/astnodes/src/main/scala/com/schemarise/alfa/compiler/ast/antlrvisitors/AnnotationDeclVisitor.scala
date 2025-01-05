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
import com.schemarise.alfa.compiler.antlr.AlfaParser.AnnotationDeclContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.err.{IncorrectParametersToType, ResolutionMessage, UnknownAnnotationTarget}
import com.schemarise.alfa.compiler.types.AnnotationTargetType
import com.schemarise.alfa.compiler.types.AnnotationTargetType.TargetType

class AnnotationDeclVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[AnnotationDecl](resolveCtx) {

  override def visitAnnotationDecl(ctx: AnnotationDeclContext): AnnotationDecl = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val name: StringNode = readStringNode(ctx.idOrQid())
    val fields: Seq[FieldOrFieldRef] = readFieldOrFieldRefs(resolveCtx, namespace, ctx.children)
    val includes: Seq[UdtDataType] = readIncludes(resolveCtx, ctx.optIncludesList(), namespace)

    val targets: Seq[TargetType] =
      if (ctx.annotationTargets() != null) {
        val empty = j2sNoParseExcpStream(ctx.annotationTargets().idOnly()).filter(act => AnnotationTargetType.withNameOpt(act.getText).isEmpty)
        val exists = j2sNoParseExcpStream(ctx.annotationTargets().idOnly()).filter(act => AnnotationTargetType.withNameOpt(act.getText).isDefined)

        empty.foreach(e => {
          val t = readToken(e)
          val rm = new ResolutionMessage(t, UnknownAnnotationTarget)(None, List.empty, t.getText)
          resolveCtx.addResolutionError(rm)
        })

        exists.map(act => AnnotationTargetType.withEnumName(act.getText))
      }
      else
        Seq.empty

    val o = new AnnotationDecl(Some(resolveCtx), token, namespace, meta, name, fields, targets, includes, imports)
    resolveCtx.registry.registerUdt(o)
  }
}
