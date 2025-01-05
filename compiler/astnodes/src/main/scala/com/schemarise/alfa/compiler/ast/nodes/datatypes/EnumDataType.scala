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
package com.schemarise.alfa.compiler.ast.nodes.datatypes

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IEnumDataType, Vectors}
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl

/**
 * F : enum< Red, Blue, Green >
 */
case class EnumDataType(location: IToken = TokenImpl.empty,
                        val fields: Seq[Field]) extends VectorDataType(None) with IEnumDataType {

  private var _syntheticEnum: Option[AlfaSyntheticEnum] = None

  override val vectorType: VectorType = Vectors.enum

  override def resolvableInnerNodes() = Seq.empty ++ _syntheticEnum

  override def fieldDataType() = AllFieldTypes.enum

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType = this

  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    if (!other.isInstanceOf[EnumDataType])
      false
    else {
      val othert = other.asInstanceOf[EnumDataType]

      _syntheticEnum.equals(othert._syntheticEnum)
    }

  override def syntheticEnum() = {
    assertPreResolved(None)
    _syntheticEnum.get
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {

      if (_syntheticEnum.isDefined)
        _syntheticEnum.get.traverse(v)

      v.exit(this)
    }
  }

  override def toString: String = {

    val fieldStr = fields.map(f => {
      val startDoc = f.rawNodeMeta.topDocsToString("    ")
      val sameLnDoc = f.rawNodeMeta.samelineDocsToString()
      startDoc + " " + EnumDecl.escapedString(f.name) + " " + sameLnDoc // + "\n"
    }).mkString(",")

    val s = new StringBuilder
    s.append("enum< ")
    s.append(fieldStr)
    s.append(" >")
    s.toString()
  }

  override def preResolve(ctx: Context): Unit = {
    val fieldRefs = fields.map(new FieldOrFieldRef(_))

    val parentUdt: UdtBaseNode = locateUdtParent()
    val parentField = locateFieldParent(true).get

    val sEnum = new AlfaSyntheticEnum(
      fields(0).location, parentUdt.namespaceNode, NodeMeta.empty, Seq.empty,
      StringNode(parentField._2.location, parentUdt.versionedName.name + SyntheticTypeFieldSeparator + parentField._2.nameNode.text + parentField._1),
      None, Seq.empty, fieldRefs, Seq.empty)

    _syntheticEnum = Some(sEnum)

    ctx.registry.registerUdt(sEnum)

    super.preResolve(ctx)
  }

  override def unwrapTypedef: DataType = this
}
