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
package com.schemarise.alfa.compiler.tools.graph

import com.schemarise.alfa.compiler.ast.model.types.IDataType
import com.schemarise.alfa.compiler.ast.model.{IDocumentation, IdentifiableNode}
import com.schemarise.alfa.compiler.tools.graph.MethodBuildVertexType.VariableType

class MethodGraphVertex(node: IdentifiableNode,
                        data: Option[IdentifiableNode],
                        val vtype: VariableType,
                        val doc: Seq[IDocumentation] = Seq.empty,
                        val dataType: Option[IDataType] = None
                       ) extends Vertex(node, data) {
  override def toString: String = {
    super.toString + " : " + vtype
  }

  def name = node.nodeId.id.replace(".", "::")
}

object MethodBuildVertexType extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type VariableType = Value

  val Formal = Value("Formal")

  val Field = Value("FldDecl")

  val Type = Value("Type")

  val FieldRef = Value("FldRef")

  val NewObjField = Value("NewObjField")

  val Variable = Value("VarDecl")

  val VariableRef = Value("VarRef")

  val Return = Value("Return")

}
