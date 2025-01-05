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

import com.schemarise.alfa.compiler.antlr.AlfaParser.ExpressionSequenceContext
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.BaseNode
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.utils.TokenImpl


class QuotedStringNode(sn: StringNode) extends StringNode(sn.text)(sn.location, sn.origString) {
  override def toString: String = "\"" + text + "\""
}

case class StringNode(text: String)(val location: IToken, val origString: String) extends BaseNode {

  private def flattenedName(a: DataType): String = {

    a match {
      case t: ScalarDataType => t.scalarType.toString
      case t: MapDataType => "Map" + flattenedName(t.keyType) + flattenedName(t.valueType)
      case t: SetDataType => "Set" + flattenedName(t)
      case t: ListDataType => "List" + flattenedName(t)
      case t: EnclosingDataType => t.encType.toString + flattenedName(t.componentType)
      case t: UdtDataType => t.fullyQualifiedName.replace('.', '_')
      case t: UdtOrTypeDefedDataType => if (t.target.isDefined) flattenedName(t.target.get) else "undefined"
      case t: TypeParameterDataType => t.tp.nameNode.text
    }
  }

  def wasEscaped = text.length != origString.length

  def templatedName(tp: Option[Seq[TypeParameter]], args: Iterable[DataType]): StringNode = {
    val n =
      if (tp.isDefined)
        text
      else if (args.size > 0) {
        text + "_" + args.map(a => {
          flattenedName(a)
        }).mkString("_")
      }
      else
        text

    StringNode.create(n)
  }

  override def nodeType: Nodes.NodeType = Nodes.StringNode

  override def toString: String = origString
}

object StringNode {
  //  private def escapable(s: String): Boolean = s.startsWith("`") && s.endsWith("`")

  def apply(token: IToken, text: String): StringNode =
    StringNode(unescape(text))(token, text)

  def create(esc: ExpressionSequenceContext): StringNode = {
    create(esc.toString)
  }


  def create(e: Expression): StringNode = {
    StringNode.create(e.toString)
  }

  def createEscaped(text: String): StringNode =
    StringNode(text)(TokenImpl.empty, text)

  def create(text: String): StringNode =
    StringNode(unescape(text))(TokenImpl.empty, text)

  def create(loc: IToken, text: String): StringNode =
    StringNode(unescape(text))(loc, text)

  def unescape(s: String): String = s.replaceAll("`", "")

  val empty: StringNode = create("")
}