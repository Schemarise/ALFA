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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType

class NativeUdt(ctx: Option[Context], token: IToken,
                nodeMeta: NodeMeta,
                nameNode: StringNode,
                newTypeName: StringNode
               )
  extends UdtBaseNode(ctx = ctx, location = token, declaredRawName = nameNode, imports = Seq.empty) with INativeUdt {

  override def nodeType: Nodes.NodeType = Nodes.NativeUdt

  override def udtType: UdtType = UdtType.nativeUdt

  override def traverse(v: NodeVisitor): Unit = {}

  override val isSynthetic = true

  override def toString: String = {
    s"""
       |typedefs {
       |  ${newTypeName.text} = external ${nameNode.text}
       |}
       """.stripMargin
  }

  /**
   * Includes and Extends are template instantiated, fields should be left-as-is - i.e. templates if they are.
   */
  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context,
                                                              params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode =
    this

  override val aliasedName: String = newTypeName.text
}
