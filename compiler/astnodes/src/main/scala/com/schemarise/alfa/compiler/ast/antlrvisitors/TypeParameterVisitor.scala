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
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType

class TypeParameterVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[TypeParameter](resolveCtx) {
  override def visitTypeParam(ctx: AlfaParser.TypeParamContext): TypeParameter = {
    val name = readStringNode(ctx.paramName)


    //    val fullyQualfiedName = StringNode.create(name.location, if (namespace.isEmpty()) name.text
    //                            else namespace.nameNode.text + "." + name.text )

    val derivedFrom: Option[DataType] =
      if (ctx.derrivedFromType == null)
        None
      else
        Some(readFieldType(resolveCtx, ctx.derrivedFromType, namespace))

    new TypeParameter(readToken(ctx), name, derivedFrom)
  }
}
