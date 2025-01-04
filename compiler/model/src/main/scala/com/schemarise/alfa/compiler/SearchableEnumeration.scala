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
package com.schemarise.alfa.compiler

class SearchableEnumeration extends Enumeration {
  def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)

  def names = {
    values.map(_.toString).mkString(", ")
  }

  def withEnumName(s: String) = {
    try {
      super.withName(s)
    } catch {
      case w: Exception =>
        throw new AlfaInternalException(s"Unknown enum constant '${s}' for ${getClass.getName}; accepted values '${names.mkString(",")}' ")
    }
  }

}


