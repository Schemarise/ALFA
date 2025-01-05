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
import com.schemarise.alfa.compiler.antlr.AlfaParser.FieldDeclContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}

class FieldVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[Field](resolveCtx) {
  override def visitFieldDecl(ctx: FieldDeclContext): Field = {
    val name: StringNode = readStringNode(ctx.fieldName)
    val fieldType: DataType = if (ctx.fieldType() != null)
      readFieldType(resolveCtx, ctx.fieldType(), namespace)
    else {
      new UdtDataType(name = StringNode(readToken(ctx.separatorColon), ""))
    }

    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())

    val isConst: Boolean = ctx.isConstant != null

    new Field(name.location, meta, isConst, name, fieldType, None, None)
  }
}
