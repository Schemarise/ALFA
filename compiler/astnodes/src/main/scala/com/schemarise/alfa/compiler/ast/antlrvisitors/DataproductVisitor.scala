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
import com.schemarise.alfa.compiler.antlr.AlfaParser.{DataproductContext, DocumentedVersionedIdOrQidContext}
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.{AnnotatedUdtVersionedName, NodeMeta, UdtName, UdtVersionedName}

import scala.collection.mutable.ListBuffer

class DataproductVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[Dataproduct](resolveCtx) {

  override def visitDataproduct(ctx: DataproductContext): Dataproduct = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val name: StringNode = readStringNode(ctx.name)
    val versionNo: Option[IntNode] = readOptVersion(ctx.versionMarker())

    val published = j2sNoParseExcpStream(ctx.publishDecls()).map(e => readAnnotationedVersionedName(e.documentedVersionedIdOrQid()))

    val namedConsumers = new ListBuffer[(UdtVersionedName, AnnotatedUdtVersionedName)]()

    j2sNoParseExcpStream(ctx.consumeDecls()).foreach(e => {
      val prodName = readStringNode(e.consumeDPs.idOrQid())
      UdtVersionedName.apply(namespace = namespace, name = prodName)

      j2sNoParseExcpStream(e.consumeDPs.documentedVersionedIdOrQid()).foreach(v => {
        val an = readAnnotationedVersionedName(v)
        val da = (UdtVersionedName.apply(namespace = namespace, name = prodName), an)
        namedConsumers.append(da)
      })
    })

    val o = new Dataproduct(Some(resolveCtx), token, namespace, meta, name, published.toList, namedConsumers.toList, versionNo, imports)
    resolveCtx.registry.registerUdt(o)
    o
  }

  private def readAnnotationedVersionedName(w: DocumentedVersionedIdOrQidContext): AnnotatedUdtVersionedName = {
    val nm = readNodeMeta(resolveCtx, namespace, w.docAndAnnotations(), w.sameline_docstrings())
    val name = readStringNode(w.idOrQid())

    val sd = UdtName.create(namespace, name, None)
    val ver = readOptVersion(w.versionMarker())

    val vn = UdtVersionedName(sd, ver)(None)

    AnnotatedUdtVersionedName(vn, nm)
  }
}

