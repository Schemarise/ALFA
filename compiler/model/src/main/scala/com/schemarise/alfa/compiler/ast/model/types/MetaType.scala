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

object MetaType extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type MetaFieldType = Value

  val Entity = Value("$entity")
  val Key = Value("$key")
  val Trait = Value("$trait")
  val Union = Value("$union")
  val Record = Value("$record")
  val Annotation = Value("$annotation")
  val Enum = Value("$enum")
  val Udt = Value("$udt")
  val Service = Value("$service")

  val EntityName = Value("$entityName")
  val KeyName = Value("$keyName")
  val TraitName = Value("$traitName")
  val UnionName = Value("$unionName")
  val RecordName = Value("$recordName")
  val EnumName = Value("$enumName")
  val UdtName = Value("$udtName")
  val ServiceName = Value("$serviceName")
  val FieldName = Value("$fieldName")
}
