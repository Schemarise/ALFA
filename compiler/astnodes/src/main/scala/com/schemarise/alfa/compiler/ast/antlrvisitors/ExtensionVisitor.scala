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
import com.schemarise.alfa.compiler.antlr.AlfaParser.{ExtensionContext}
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes._
import scala.collection.JavaConverters._

class ExtensionVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[Extension](resolveCtx) {

  override def visitExtension(ctx: ExtensionContext): Extension = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), null)
    val extTypeName: StringNode = readStringNode(ctx.extType)

    val exps = ctx.extensionAttrib().asScala.map(e => {
      if (e.name == null || e.expr == null)
        (null, null)
      else {
        val n = readStringNode(e.name)
        (n, e.expr)
      }
    }).filter(_._1 != null)

    val name = readStringNode(ctx.fullname)

    val o = new Extension(resolveCtx, token, namespace, meta, extTypeName, name, exps, imports)
    resolveCtx.registry.registerUdt(o)
    o
  }
}
