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
package com.schemarise.alfa.compiler.types

import com.schemarise.alfa.compiler.SearchableEnumeration

object AnnotationTargetType extends SearchableEnumeration {
  type TargetType = Value

  val Namespace = Value("namespace")
  val Record = Value("record")
  val Entity = Value("entity")
  val Includes = Value("includes")
  val Trait = Value("trait")
  val Key = Value("key")
  val Enum = Value("enum")
  val Union = Value("union")
  val Field = Value("field")
  val KeyField = Value("keyfield")
  val Method = Value("method")
  val Service = Value("service")
  val Library = Value("library")
  val Annotation = Value("annotation")
  val Tuple = Value("tuple")
  val Type = Value("type")
  val Dataproduct = Value("dataproduct")
}
