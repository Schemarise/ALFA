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

object Enclosed extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type EnclosedType = Value

  val try_ = Value("try")
  val key = Value("key")
  val either = Value("either")
  val pair = Value("pair")
  val opt = Value("optional")

  //  val typeof = Value("typeof")
  val table = Value("table")
  val stream = Value("stream")
  val future = Value("future")
  val encrypt = Value("encrypted")
  val compress = Value("compressed")
}