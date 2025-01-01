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

import com.schemarise.alfa.compiler.ast.model.{IToken, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.{IToken, _}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.TokenImpl

/**
 * Models the :
 * < T > in record Foo< T >
 * < T : Bar > in record Foo< T : Bar >
 */
class TypeParameter(location: IToken = TokenImpl.empty, val nameNode: StringNode, derivedFromNode: Option[DataType] = None)
  extends UdtBaseNode(None, location, NamespaceNode.empty,
    NodeMeta.empty, Seq.empty, nameNode, None, None,
    None, None, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
    with ResolvableNode with IdentifiableNode with ITypeParameter {
  override def nodeType: Nodes.NodeType = Nodes.TemplateParameterType

  override def equals(obj: Any): Boolean = {
    obj match {
      case x: TypeParameter => x.name.equals(name)
      case _ => false
    }
  }

  override def udtType: UdtType = UdtType.typeParam


  override def resolvableInnerNodes() =
    if (derivedFromNode.isDefined) Seq(derivedFromNode.get)
    else Seq.empty

  override def toString: String = nameNode.text

  //  override def templateInstantiated  = this

  override def traverse(v: NodeVisitor): Unit = {}

  override def derivedFrom: Option[IDataType] = derivedFromNode

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode = this
}