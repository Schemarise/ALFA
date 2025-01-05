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
import com.schemarise.alfa.compiler.antlr.AlfaParser.FieldsContext
import com.schemarise.alfa.compiler.ast.nodes.{Fields, NamespaceNode}

class FieldsVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[Fields](resolveCtx) {
  override def visitFields(ctx: FieldsContext): Fields = {
    val v = new FieldVisitor(resolveCtx, namespace)
    val fs = new Fields(readToken(ctx), j2sNoParseExcpStream(ctx.fieldDecl()).map(
      v.visitFieldDecl(_)))

    resolveCtx.registry.registerFields(fs)

    fs
  }
}
