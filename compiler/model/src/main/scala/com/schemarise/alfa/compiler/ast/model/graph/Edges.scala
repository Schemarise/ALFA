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
package com.schemarise.alfa.compiler.ast.model.graph

object Edges extends com.schemarise.alfa.compiler.SearchableEnumeration {

  type EdgeType = Value

  val NamespaceToNamespace = Value("NamespaceToNamespace")
  val NamespaceToUdt = Value("NamespaceToUdt")
  val Includes = Value("Include")
  val Extends = Value("Extends")
  val Scope = Value("Scope")
  val UdtToFieldDataType = Value("UdtToFieldDataType")
  val EntityToUDTKey = Value("EntityToKey")
  val EntityToDirectKey = Value("EntityDirectKey")
  val TestTarget = Value("TestTarget")
  val ConcreteToTemplateUdt = Value("ConcreteToTemplateUdt")

  val MethodToAnnotation = Value("MethodToAnnotation")
  val FieldToAnnotation = Value("FieldToAnnotation")
  val UdtToAnnotation = Value("UdtToAnnotation")

  val TransformerOutput = Value("TransformerOutput")

  val ExpressionInput = Value("ExpressionInput")
}
