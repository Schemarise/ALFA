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
import com.schemarise.alfa.compiler.antlr.AlfaParser.TypeDefsContext
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{TypeDefedDataType, UdtDataType}

class TypeDefsVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[TypeDefs](resolveCtx) {
  override def visitTypeDefs(ctx: TypeDefsContext): TypeDefs = {
    val v = new TypeDefDeclVisitor
    val s = j2sNoParseExcpStream(ctx.typeDefDecl()).map(v.visitTypeDefDecl(_))
    new TypeDefs(readToken(ctx), s)
  }

  class TypeDefDeclVisitor extends WithContextVisitor[TypeDefedDataType](resolveCtx) {
    override def visitTypeDefDecl(ctx: AlfaParser.TypeDefDeclContext): TypeDefedDataType = {
      val v = new DataTypeVisitor(resolveCtx, namespace)
      val tmplArgRefs = readTypeParameters(resolveCtx, ctx.typeParameters(), namespace)

      val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())

      val newType = readStringNode(ctx.newType)

      if (ctx.nativeImplClass != null) {
        val nativeTypeName = readStringNode(ctx.nativeImplClass)

        val nudt = new NativeUdt(Some(resolveCtx), readToken(ctx), meta, nativeTypeName, newType)
        resolveCtx.registry.registerUdt(nudt)

        val nativeUdtType = new UdtDataType(location = nativeTypeName.location, name = nativeTypeName, synthUdtReference = true)

        val result = new TypeDefedDataType(meta, readStringNode(ctx.newType), tmplArgRefs, nativeUdtType)
        resolveCtx.registry.register(result)
        result
      }
      else {
        val result = new TypeDefedDataType(meta, newType, tmplArgRefs, v.visitFieldType(ctx.currType))
        resolveCtx.registry.register(result)
        result
      }
    }
  }
}
