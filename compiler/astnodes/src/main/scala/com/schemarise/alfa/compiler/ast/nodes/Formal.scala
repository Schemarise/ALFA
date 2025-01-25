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

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.FormalScopeType.FormalScope
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.{IFormal, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.TokenImpl

class Formal(location: IToken = TokenImpl.empty,
             nodeMeta: NodeMeta = NodeMeta.empty,
             isConst: Boolean = false,
             name: StringNode,
             declDataType: DataType,
             val scope : Option[FormalScope] = None
            ) extends Field(location, nodeMeta, isConst, name, declDataType) with IFormal {
  override def nodeType: Nodes.NodeType = Nodes.FormalNode

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      dataType.traverse(v)
      v.exit(this)
    }
  }
}

object Formal {
  def from(f: Field, formalScope: Option[FormalScope] = None) = {
    if ( f.isInstanceOf[Formal] ) {
      f.asInstanceOf[Formal]
    }
    else {
      new Formal(f.location, f.rawNodeMeta, f.isConst, f.nameNode, f.dataType, formalScope)
    }
  }
}
