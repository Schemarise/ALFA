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

object Scalars extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type ScalarType = Value

  // order matters for precedence
  val short = Value("short")
  val int = Value("int")
  val long = Value("long")
  val double = Value("double")

  val string = Value("string")

  val boolean = Value("boolean")

  val binary = Value("binary")

  val date = Value("date")
  val datetime = Value("datetime")
  val datetimetz = Value("datetimetz")
  val time = Value("time")
  val duration = Value("duration")
  val period = Value("period")

  val decimal = Value("decimal")
  val void = Value("void")
  val uuid = Value("uuid")

  //  val char = Value("char")
  //  val byte = Value("byte")
  //  val float = Value("float")
  //  val uri = Value("uri")
  //  val pattern = Value("pattern")

}
