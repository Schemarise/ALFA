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

object RaiseCategory extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type RaiseCategoryType = Value

  val Accuracy = Value("Accuracy")
  val Completeness = Value("Completeness")
  val Conformity = Value("Conformity")
  val Consistency = Value("Consistency")
  val Coverage = Value("Currency")
  val Integrity = Value("Integrity")
  val Provenance = Value("Provenance")
  val Timeliness = Value("Timeliness")
  val Uniqueness = Value("Uniqueness")
  val Validity = Value("Validity")

  val Unclassified = Value("Unclassified")

  private val cache = RaiseCategory.values.map(_.toString)

  def contains(value: String): Boolean = {
    cache.contains(value)
  }
}