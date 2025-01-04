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
import com.schemarise.alfa.compiler.ast.{NodeMeta, TraversableNode}
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ScalarDataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{ExpressionError, TestCaseNamingError}
import com.schemarise.alfa.compiler.utils.BuiltinModelTypes

class Testcase(ctx: Option[Context], token: IToken,
               namespace: NamespaceNode,
               nodeMeta: NodeMeta,
               name: StringNode,
               versionNo: Option[IntNode],
               methodDeclarations: Seq[MethodDeclaration],
               modifiers: Seq[ModifierNode], imports: Seq[ImportDef]
              )
  extends MethodBodyContainer(ctx, token, namespace, nodeMeta, name, versionNo, methodDeclarations, modifiers, imports)
    with TraversableNode with ITestcase {

  override def nodeType: Nodes.NodeType = Nodes.Testcase

  private var _testTarget: Option[UdtBaseNode] = None

  def targetUdt = _testTarget

  override def udtType: UdtType = UdtType.testcase

  override def traverse(v: NodeVisitor): Unit = {
  }
}
