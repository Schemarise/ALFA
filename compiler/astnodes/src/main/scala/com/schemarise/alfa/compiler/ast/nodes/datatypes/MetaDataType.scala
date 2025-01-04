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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.MetaType.MetaFieldType
import com.schemarise.alfa.compiler.ast.{ResolvableNode, TemplateableNode}
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IMetaDataType, MetaType}
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.utils.TokenImpl

case class MetaDataType(location: IToken = TokenImpl.empty, val metaType: MetaFieldType) extends DataType with IMetaDataType {
  override def wasTemplateDerived: Boolean = false

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = {

    if (other.isInstanceOf[MetaDataType]) {
      val mt = other.asInstanceOf[MetaDataType]
      mt.metaType == this.metaType
    }

    else if (!other.isInstanceOf[UdtDataType]) {
      false
    }

    else {
      val udt = other.asInstanceOf[UdtDataType]

      if (udt.hasErrors && udt.resolvedType.isEmpty) {
        false
      }
      else {
        val n = udt.udt

        metaType match {
          case MetaType.Udt =>
            n.isEntity || n.isEnum || n.isTrait || n.isRecord || n.isUnion || n.isKey

          case MetaType.UdtName =>
            n.isEntity || n.isEnum || n.isTrait || n.isRecord || n.isUnion || n.isKey

          case MetaType.Trait =>
            n.isTrait

          case MetaType.Key =>
            n.isKey

          case MetaType.Annotation =>
            n.isAnnotation

          case MetaType.Enum =>
            n.isEnum

          case MetaType.Record =>
            n.isRecord

          case MetaType.Entity =>
            n.isEntity

          case MetaType.Union =>
            n.isUnion

          case MetaType.Service =>
            n.isService

          case _ =>
            false
        }
      }
    }

  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      v.exit(this)
    }
  }

  override def fieldDataType(): FieldType = AllFieldTypes.metaType

  override def unwrapTypedef: DataType = this

  override def resolvableInnerNodes(): Seq[ResolvableNode] = Seq.empty

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = this

  override def toString: String = {
    val s = metaType.toString
    s
  }
}
