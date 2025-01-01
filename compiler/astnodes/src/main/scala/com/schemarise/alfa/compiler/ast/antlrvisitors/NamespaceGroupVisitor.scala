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
import com.schemarise.alfa.compiler.antlr.AlfaParser.NamespaceGroupContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.{ParserError, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.TokenImpl

class NamespaceGroupVisitor(resolveCtx: Context, imports: Seq[ImportDef])
  extends WithContextVisitor[NamespaceNode](resolveCtx) {

  override def visitNamespaceGroup(ctx: NamespaceGroupContext): NamespaceNode = {

    val name = if (ctx.namespaceName == null)
      StringNode.empty
    else
      readStringNode(ctx.namespaceName)

    val meta: NodeMeta = readNodeMeta(resolveCtx, NamespaceNode.empty, ctx.docAndAnnotations(), ctx.sameline_docstrings())

    val namespace = new NamespaceNode(name, true, readToken(ctx), meta)

    resolveCtx.registry.registerNamespace(namespace)

    val uv = new UdtVisitor(resolveCtx, namespace, imports)
    val udts: Seq[UdtBaseNode] = j2sNoParseExcpStream(ctx.udt()).map(x => {

      try {
        uv.visitUdt(x)
      } catch {
        case excp: Throwable =>
          if (resolveCtx.getErrors().size > 0)
            None
          else {
            if (x != null)
              resolveCtx.addResolutionError(ResolutionMessage(readToken(x), ParserError)(None, List.empty, "Error reading UDT"))
            else
              resolveCtx.addResolutionError(ResolutionMessage(TokenImpl.empty, ParserError)(None, List.empty, "Error reading ALFA file"))

            None
          }
      }

    }).filter(_.isDefined).map(_.get)

    val keys = udts.filter(e => e.isEntity && e.asInstanceOf[Entity].key.isDefined).map(e => e.asInstanceOf[Entity].key.get)

    namespace.addAll(udts ++ keys)

    namespace
  }

}
