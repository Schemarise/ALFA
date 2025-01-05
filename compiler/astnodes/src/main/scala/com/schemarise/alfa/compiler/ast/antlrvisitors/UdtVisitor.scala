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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser
import com.schemarise.alfa.compiler.ast.nodes._

class UdtVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[Option[UdtBaseNode]](resolveCtx) {

  private val rv = new RecordVisitor(resolveCtx, namespace, imports)
  private val dpv = new DataproductVisitor(resolveCtx, namespace, imports)
  private val tv = new TraitVisitor(resolveCtx, namespace, imports)
  private val kv = new KeyVisitor(resolveCtx, namespace, imports)
  private val uv = new UnionVisitor(resolveCtx, namespace, imports)
  private val sv = new ServiceVisitor(resolveCtx, namespace, imports)
  private val pv = new LibraryOrTestcaseVisitor(resolveCtx, namespace, imports)
  private val ev = new EntityVisitor(resolveCtx, namespace, imports)
  private val env = new EnumDeclVisitor(resolveCtx, namespace, imports)
  private val av = new AnnotationDeclVisitor(resolveCtx, namespace, imports)
  private val extv = new ExtensionVisitor(resolveCtx, namespace, imports)

  override def visitUdt(ctx: AlfaParser.UdtContext): Option[UdtBaseNode] = {
    val res =
      if (ctx.`trait`() != null && ctx.`trait`().exception == null)
        tv.visitTrait(ctx.`trait`())

      else if (ctx.annotationDecl() != null && ctx.annotationDecl().exception == null)
        av.visitAnnotationDecl(ctx.annotationDecl())

      else if (ctx.entity() != null && ctx.entity().exception == null)
        ev.visitEntity(ctx.entity())

      else if (ctx.serviceDecl() != null && ctx.serviceDecl().exception == null)
        sv.visitServiceDecl(ctx.serviceDecl())

      else if (ctx.libraryDecl() != null && ctx.libraryDecl().exception == null)
        pv.visitLibraryDecl(ctx.libraryDecl())

      else if (ctx.key() != null && ctx.key().exception == null)
        kv.visitKey(ctx.key())

      else if (ctx.record() != null && ctx.record().exception == null)
        rv.visitRecord(ctx.record())

      else if (ctx.union() != null && ctx.union().exception == null)
        uv.visitUnion(ctx.union())

      else if (ctx.enumDecl() != null && ctx.enumDecl().exception == null)
        env.visitEnumDecl(ctx.enumDecl())

      else if (ctx.extension() != null && ctx.extension().exception == null)
        extv.visitExtension(ctx.extension())

      else if (ctx.dataproduct() != null && ctx.dataproduct().exception == null)
        dpv.visitDataproduct(ctx.dataproduct())

      else
        null

    if (res == null) None
    else Some(res)
  }
}
