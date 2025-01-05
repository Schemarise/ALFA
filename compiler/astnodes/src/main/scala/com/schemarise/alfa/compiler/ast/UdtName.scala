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
package com.schemarise.alfa.compiler.ast

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.TextUtils

case class UdtName private(fullyQualifiedName: String,
                           typeArguments: Option[Seq[DataType]] // Eg. For usages - Result : Data<int>
                          )(
                            val location: IToken,
                            val typeParameters: Option[Seq[TypeParameter]], // Eg. For declarations - trait Data<T> {}
                            val declaredName: Option[StringNode]
                          )
  extends Locatable {
  if (typeParameters.isDefined && typeParameters.get.size == 0)
    throw new com.schemarise.alfa.compiler.AlfaInternalException("Internal Error - Invalid node")

  override def toString: String = {
    val tmpl = if (typeArguments.isDefined) TextUtils.mkString(typeArguments.get) else ""
    val n = if (declaredName.isDefined) declaredName.get.text else fullyQualifiedName
    n + tmpl
  }
}

object UdtName {
  def apply(namespace: NamespaceNode,
            name: StringNode,
            typeParameters: Option[Seq[TypeParameter]] = None,
            typeArguments: Option[Seq[DataType]] = None): UdtName = {
    val fqn = if (namespace.isEmpty)
      name.text
    // name is ASSUMED TO BE fully qualified if it starts with first part of namespace
    // May need way to indicate name is relative in future?
    else if (name.text.indexOf('.') > 0 && name.text.startsWith(namespace.first + "."))
      name.text
    else
      namespace.nameNode.text + "." + name.text

    new UdtName(fqn, typeArguments
    )(name.location, typeParameters, Some(name))
  }

  def create(namespace: NamespaceNode,
             name: StringNode,
             typeParameters: Option[Seq[TypeParameter]] = None,
             typeArguments: Option[Map[String, DataType]] = None): UdtName = {
    val ta = if (typeArguments.isDefined) Some(typeArguments.get.map(_._2).toSeq) else None
    apply(namespace, name, typeParameters, ta)
  }

}

