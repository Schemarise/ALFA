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

import com.schemarise.alfa.compiler.ast.model.{IToken, IUdtVersionName}
import com.schemarise.alfa.compiler.ast.model.types.IUdtDataType
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.utils.TextUtils

case class UdtVersionedName private(private val udtName: UdtName,
                                    versionNode: Option[IntNode])(_udtType: Option[UdtType]) extends Locatable with IUdtVersionName {
  def flattenedName: String = {
    if (typeArgumentsNode.isDefined) {
      StringNode.create(fullyQualifiedName).templatedName(None, typeArgumentsNode.get).text
    }
    else {
      fullyQualifiedName
    }
  }

  override def toString: String = {

    val n = if (udtName.declaredName.isDefined) udtName.declaredName.get.text else udtName.fullyQualifiedName
    val v = if (versionNode.isDefined) "@" + versionNode.get else ""
    val tmpl = if (udtName.typeParameters.isDefined) TextUtils.mkString(udtName.typeParameters.get) else ""

    n + v + tmpl
  }

  def asUnixPath(withExtension: String = ""): String = fullyQualifiedName.replace('.', '/') + withExtension

  override val fullyQualifiedName = udtName.fullyQualifiedName

  //  override val nameOnly = fullyQualifiedName.split('.').last

  val isVersioned = versionNode.isDefined
  //  def versionNoNode = version.get

  override def version: Option[Int] = if (isVersioned) Some(versionNode.get.number.get.intValue()) else None

  override val name = fullyQualifiedName.split('.').last

  val namespace: Namespace = if (fullyQualifiedName.indexOf('.') > 0)
    Namespace(fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.')))
  else
    Namespace.empty

  val typeArgumentsNode = udtName.typeArguments
  val typeParametersNode = udtName.typeParameters

  val typeParameters = if (typeParametersNode.isDefined) typeParametersNode.get.zipWithIndex.map(e => e._1 -> e._2).toMap
  else Map.empty

  val typeArguments = if (typeArgumentsNode.isDefined) typeArgumentsNode.get.zipWithIndex.map(e => e._2 -> e._1.unwrapTypedef).toMap
  else Map.empty

  override val location: IToken = udtName.location

  override def asUdtDataType: IUdtDataType = {
    new UdtDataType(location, NamespaceNode.empty, StringNode.create(fullyQualifiedName), versionNode, udtName.typeArguments)
  }

  override def udtType: UdtType = _udtType.get

  override val fullyQualifiedNameAndVersion: String = fullyQualifiedName + (
    if (version.isDefined)
      "@" + version.get
    else
      ""
    )
}

object UdtVersionedName {
  def apply(namespace: NamespaceNode = NamespaceNode.empty,
            name: StringNode,
            templateParams: Option[Seq[TypeParameter]] = None,
            typeArguments: Option[Seq[DataType]] = None,
            version: Option[IntNode] = None,
            udtType: Option[UdtType] = None): UdtVersionedName = {
    UdtVersionedName(UdtName(namespace, name, templateParams, typeArguments), version)(udtType)
  }
}
