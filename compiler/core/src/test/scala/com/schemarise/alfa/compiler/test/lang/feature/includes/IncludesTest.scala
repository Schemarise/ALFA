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
package com.schemarise.alfa.compiler.test.lang.feature.includes

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class IncludesTest extends AnyFunSuite {
  test("Enum includes") {
    val cua = TestCompiler.compileValidScript(
      """
        |enum A {
        |  x y
        |}
        |
        |enum B includes A {
        |  z
        |}
        |
      """)

    val f1 = cua.getUdt("A").get.allFields.keySet.toList
    assert(f1.equals(List("x", "y")))

    val f = cua.getUdt("B").get.allFields.keySet.toList
    assert(f.equals(List("x", "y", "z")))
  }

  test("Includes not allowed") {
    val cua = TestCompiler.compileInvalidScript(
      "@6:0 Only traits can be included, 'A' is a record",
      """
        |record A {
        |  x : int
        |}
        |
        |record B includes A {
        |  y : string
        |}
        |
      """)
  }
}
