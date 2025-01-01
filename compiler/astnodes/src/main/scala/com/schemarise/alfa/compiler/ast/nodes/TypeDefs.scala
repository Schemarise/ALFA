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

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.nodes.datatypes.TypeDefedDataType
import com.schemarise.alfa.compiler.ast.{BaseNode, ResolvableNode}
import com.schemarise.alfa.compiler.utils.TokenImpl

class TypeDefs(val location: IToken = TokenImpl.empty, val typeDefs: Seq[TypeDefedDataType]) extends BaseNode with ResolvableNode {
  override def nodeType: Nodes.NodeType = Nodes.TypeDefsNode

  override def resolvableInnerNodes() = typeDefs

  override def toString: String = {
    val defs = typeDefs.map(e => {
      indent(e.toString, "    ")
    }).mkString("")

    if (typeDefs.size > 0) {
      s"""typedefs {$defs
         |}
         |""".stripMargin
    }
    else {
      ""
    }
  }
}

