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
package com.schemarise.alfa.compiler.ast.nodes

import com.schemarise.alfa.compiler.ast.model.{IEnum, IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IEnum, IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.utils.{LangKeywords, LexerUtils, TextUtils, TokenImpl}

object EnumDecl {
  def escapedString(str: String): String = {
    val shouldEscape = !Character.isJavaIdentifierStart(str.head) ||
      !str.filter(c => !Character.isJavaIdentifierPart(c)).isEmpty ||
      LangKeywords.isTargetLangKeyword(str) ||
      LexerUtils.keywords.contains(str)

    if (shouldEscape)
      "`" + str + "`"
    else
      str
  }
}

class EnumDecl(ctx: Option[Context] = None,
               token: IToken = TokenImpl.empty,
               namespace: NamespaceNode = NamespaceNode.empty,
               enNodeMeta: NodeMeta = NodeMeta.empty,
               modifiers: Seq[ModifierNode] = Nil,
               nameNode: StringNode,
               versionNo: Option[IntNode] = None,
               includesNode: Seq[UdtDataType] = Nil,
               fieldsNode: Seq[FieldOrFieldRef] = Nil,
               imports: Seq[ImportDef] = Seq.empty
              )
  extends UdtBaseNode(
    ctx, token, namespace, enNodeMeta, modifiers, nameNode, versionNo, None, None,
    None, includesNode, fieldsNode, List.empty, Seq.empty, Seq.empty, imports) with IEnum {

  override def nodeType: Nodes.NodeType = Nodes.Enum

  //  override def templateInstantiated : EnumDecl = this

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }


  override def udtType: UdtType = UdtType.enum

  override def toString: String = {
    val sb = new StringBuilder

    val alldocs = super.docNodes
    if (alldocs.size > 0)
      sb ++= alldocs.map(_.toString).mkString("\n/#\n ", "\n", "\n #/")

    sb ++= "\nenum " + versionedName.fullyQualifiedName + TextUtils.mkString(typeParamsNode)

    if (includesNode.size > 0) {
      sb ++= " includes "
      includesNode.foreach(e => sb ++= includesNode.mkString("", ",", ""))
    }

    sb ++= " {\n"

    val hasAnyLexicals = fieldsNode.filter(e => e.field.get.enumLexical.isDefined).size > 0

    sb ++= fieldsNode.map(fr => {
      val f = fr.field.get
      val startDoc = f.rawNodeMeta.topDocsToString("    ")
      val sameLnDoc = f.rawNodeMeta.samelineDocsToString()

      val lex = if (hasAnyLexicals) {
        if (f.enumLexical.isDefined)
          "( \"" + f.enumLexical.get + "\" )"
        else
          "( \"" + f.name + "\" )"
      }
      else
        ""

      val const = EnumDecl.escapedString(f.nameNode.text) + lex

      startDoc + "    " + const + " " + sameLnDoc
    }).mkString("\n")

    sb ++= "\n}\n"

    sb.toString()
  }


  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = this
}
