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

// TODO remove - unused
object AllFieldTypes extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type FieldType = Value

  val string = Value("string")
  val short = Value("short")
  val int = Value("int")
  val long = Value("long")
  val ushort = Value("ushort")
  val uint = Value("uint")
  val ulong = Value("ulong")
  val boolean = Value("boolean")
  val date = Value("date")
  val datetime = Value("datetime")
  val time = Value("time")
  val duration = Value("duration")
  val double = Value("double")
  val float = Value("float")
  val binary = Value("binary")
  val byte = Value("byte")
  val decimal = Value("decimal")
  val void = Value("void")
  val uuid = Value("uuid")
  val char = Value("char")

  val map = Value("map")
  val set = Value("set")
  val seq = Value("seq")
  val lambda = Value("lambda")
  val tuple = Value("tuple")
  val union = Value("union")
  val enum = Value("enum")

  val metaType = Value("metaType")

  val udt = Value("udt")
  val typedef = Value("typedef")

  val udtOrTypedef = Value("udtOrTypedef")
  val typeParameter = Value("typeParameter")
  val exprDelegate = Value("expressionDelegate")

  val opt = Value("opt")
  val key = Value("key")
  val typeof = Value("typeof")

}
