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

object UdtType extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type UdtType = Value

  val `trait` = Value("trait")
  val entity = Value("entity")
  val record = Value("record")
  val nativeUdt = Value("nativeUdt")
  val key = Value("key")
  val annotation = Value("annotation")
  val enum = Value("enum")
  val union = Value("union")
  val untaggedUnion = Value("untaggedUnion")
  val service = Value("service")
  val extension = Value("extension")
  val extensionInstance = Value("extensionInstance")
  val methodSig = Value("methodSig")
  val library = Value("library")
  val testcase = Value("testcase")
  val typeParam = Value("typeParam")
  val transform = Value("transform")
  val dataproduct = Value("dataproduct")
}
