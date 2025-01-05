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
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}

class SyntheticUnion(token: IToken,
                     namespace: NamespaceNode,
                     nodeMeta: NodeMeta,
                     modifiers: Seq[ModifierNode],
                     name: StringNode,
                     versionNo: Option[IntNode],
                     typeParams: Option[Seq[TypeParameter]],
                     typeArguments: Option[Map[String, DataType]],
                     includes: Seq[UdtDataType],
                     fields: Seq[FieldOrFieldRef],
                     val hasGeneratedFieldNames: Boolean,
                     untaggedUnion: Boolean,
                     imports: Seq[ImportDef]
                    )
  extends Union(
    None, token, namespace, nodeMeta, modifiers, name, versionNo, typeParams, typeArguments,
    None, includes, fields, Seq.empty, untaggedUnion, imports) {

  override def nodeType: Nodes.NodeType = Nodes.SyntheticUnionNode

  //  override def templateInstantiated : SyntheticUnion = ???

  override val isSynthetic: Boolean = true

}
