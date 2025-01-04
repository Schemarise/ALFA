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

class IncludeDiamond extends AnyFunSuite {
  test("Simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Level0 {
        |   L0 : int
        |}
        |
        |trait Level1A includes Level0 {
        |   L1A : date
        |}
        |
        |trait Level1B includes Level0 {
        |   L1B : string
        |}
        |
        |trait Level2 includes Level1A, Level1B {
        |   L2 : long
        |}
        |
        |
      """)

    val fields = cua.getUdt("Level2").get.allFields

    super.assert(fields.size == 4)

    super.assert(fields.contains("L0"))
    super.assert(fields.contains("L1A"))
    super.assert(fields.contains("L1B"))
    super.assert(fields.contains("L2"))
  }

  test("Simple type redefined") {
    TestCompiler.compileInvalidScript(
      "@10:0 Includes cause incompatible types for field 'F' with types 'int' and 'string'.",
      """
        |trait A {
        |   F : string
        |}
        |
        |trait B {
        |   F : int
        |}
        |
        |trait C includes A, B {
        |}
        |
        |
      """)

    TestCompiler.compileInvalidScript(
      "@10:0 Includes cause incompatible types for field 'f' with types 'int' and 'string'.",
      """
        |trait A {
        |   F : string
        |}
        |
        |trait B {
        |   f : int
        |}
        |
        |trait C includes A, B {
        |}
        |
        |
      """)

    val cua = TestCompiler.compileValidScript(
      """
        |trait A {
        |   F : int
        |}
        |
        |trait B {
        |   F : int ## test
        |}
        |
        |trait C includes A, B {
        |}
      """)
    assert(cua.getWarnings.head.toString.equals("@10:0 Include defines field 'F' 2 times with different descriptions. It will be collapsed into 1 definition"))
  }

  test("No diamond warning") {

    val cua = TestCompiler.compileValidScript(
      """
        |trait A {
        |   F : int
        |}
        |
        |trait B {
        |   F : int
        |}
        |
        |trait C includes A, B {
        |}
      """)

    assert(cua.getWarnings.isEmpty)

  }
}
