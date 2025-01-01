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
package com.schemarise.alfa.compiler.ast.model.types

import com.schemarise.alfa.compiler.ast.model.types.Nodes.NodeType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType

object EnumToString {
  def udtTypeToString(u: UdtType) = {
    u match {
      case UdtType.`trait` => "trait"
      case UdtType.union => "union"
      case UdtType.untaggedUnion => "untaggedUnion"
      case UdtType.entity => "entity"
      case UdtType.record => "record"
      case UdtType.key => "key"
      case UdtType.annotation => "annotation"
      case UdtType.enum => "enum"
      case UdtType.service => "service"
      case UdtType.testcase => "testcase"
      case UdtType.library => "library"
      case UdtType.extension => "extension"
      case UdtType.extensionInstance => "extensionInstance"
      case UdtType.methodSig => "method"
      case UdtType.nativeUdt => "nativeUdt"
      case UdtType.transform => "transform"
    }
  }

  def nodeTypeToString(n: NodeType) = {
    n match {
      case Nodes.Namespace => "namespace"
      case Nodes.Dataproduct => "dataproduct"

      case Nodes.Extension => "extension"
      case Nodes.Annotation => "annotation"
      case Nodes.Service => "service"
      case Nodes.Library => "library"
      case Nodes.Testcase => "testcase"
      case Nodes.Trait => "trait"
      case Nodes.Enum => "enum"
      case Nodes.Record => "record"
      case Nodes.Entity => "entity"
      case Nodes.Union => "union"
      case Nodes.Key => "key"
      case Nodes.NativeUdt => "native"

      case Nodes.Field => "field"
      case Nodes.ExpressionNode => "expression"
      case Nodes.Method => "method"

      case Nodes.AssertNode => "assert"

      case _ => n.toString
    }
  }

}
