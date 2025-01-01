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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.Nodes.NodeType
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, IUdtDataType, Nodes}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType

case class AnnotatedUdtVersionedName(versionedName: UdtVersionedName, nm: NodeMeta)
  extends IAnnotatedUdtVersionName with ResolvableNode {
  override def annotations: Seq[IAnnotation] = nm.annotations

  val x = 10

  override def docs: Seq[IDocumentation] = nm.docs

  override def traverse(v: NodeVisitor): Unit = {
  }

  override val fullyQualifiedName: String = versionedName.fullyQualifiedName
  override val name: String = versionedName.name

  override def version: Option[Int] = versionedName.version

  override def udtType: UdtType = versionedName.udtType

  /**
   * The < L, R > returned as L -> 0, R -> 1 > from -
   * record Pair< L, R > {
   * Left : L
   * Right : R
   * }
   */
  override def typeParameters: Map[ITypeParameter, Int] = versionedName.typeParameters

  /**
   * The < double, double > returned as 0 -> double, 1 -> double from -
   * record Data {
   * Point : Pair< double, double >
   * }
   */
  override def typeArguments: Map[Int, IDataType] = versionedName.typeArguments

  override def asUdtDataType: IUdtDataType = if (udtDataType.isDefined)
    udtDataType.get
  else
    versionedName.asUdtDataType

  override def namespace: INamespaceNode = versionedName.namespace

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    val d = if (udtDataType.isDefined) Seq(udtDataType.get) else Seq.empty
    Seq(nm) ++ d
  }

  override def hasErrors: Boolean = {
    if (super.hasErrors)
      true
    else if (udtDataType.isDefined)
      udtDataType.get.hasErrors
    else
      false
  }

  override def nodeType: NodeType = Nodes.Type

  override val location: IToken = versionedName.location

  var udtDataType: Option[UdtDataType] = None

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    udtDataType = Some(versionedName.asUdtDataType.asInstanceOf[UdtDataType])
    udtDataType.get.startPreResolve(ctx, this)
  }

  override def resolve(ctx: Context): Unit = {
    super.resolve(ctx)
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb ++= nm.annotations.mkString("", "\n", "")

    if (docs.size > 0)
      sb ++= indent(docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n"), "        ")

    sb ++= versionedName.fullyQualifiedName

    sb.toString()
  }

  override val fullyQualifiedNameAndVersion: String = versionedName.fullyQualifiedNameAndVersion
}
