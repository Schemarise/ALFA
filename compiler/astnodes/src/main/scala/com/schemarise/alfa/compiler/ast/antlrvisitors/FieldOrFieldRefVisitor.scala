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
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.model.types.Scalars
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, ScalarDataType}
import com.schemarise.alfa.compiler.err.ExpressionError

class FieldOrFieldRefVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[FieldOrFieldRef](resolveCtx) {
  override def visitField(ctx: AlfaParser.FieldContext): FieldOrFieldRef = {
    val id = ctx.idOnly()
    if (id != null) {
      val sn = readStringNode(id)
      new FieldOrFieldRef(sn)
    }
    else {
      val v = new FieldVisitor(resolveCtx, namespace)
      val fd = v.visitFieldDecl(ctx.fieldDecl())
      new FieldOrFieldRef(fd)
    }
  }

  override def visitEnumField(ctx: AlfaParser.EnumFieldContext): FieldOrFieldRef = {
    val token: IToken = readToken(ctx)
    val name: StringNode = readStringNode(ctx.idOnly())

    val lexical =
      if (name.wasEscaped) {
        Some(name)
      }
      else
        readOptionalStringNode(ctx.lexical)

    val fieldType: DataType = new ScalarDataType(token, Scalars.void, Seq.empty, Option.empty, None)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val f = new Field(name.location, meta, false, name, fieldType, lexical)
    new FieldOrFieldRef(f)

  }
}
