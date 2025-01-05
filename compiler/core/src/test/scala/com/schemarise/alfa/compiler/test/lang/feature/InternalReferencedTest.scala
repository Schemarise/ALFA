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
package com.schemarise.alfa.compiler.test.lang.feature

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class InternalReferencedTest extends AnyFunSuite {
  test("Internal dependency test 1") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace test.deps
        |
        |record A {
        |}
        |
        |internal record B {
        |}
        |
      """)
    assert(cua.getErrors.size == 0)
  }

  test("Internal dependency test 2") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:10 Internal type 'test.deps.B' referenced from non-internal type 'test.deps.A'",
      """
        |namespace test.deps
        |
        |record A {
        |   Data : B
        |}
        |
        |internal record B {
        |}
        |
      """)
  }

  test("Internal dependency test 3") {

    TestCompiler.compileValidScript(
      """
        |namespace test.deps
        |
        |record A {
        |}
        |
        |internal service Srv {
        |  f(a : A) : void
        |}
        |
      """.stripMargin
    )

    TestCompiler.compileInvalidScript(
      "@8:8 Internal type 'test.deps.A' referenced from non-internal type 'test.deps.Srv'",
      """
        |namespace test.deps
        |
        |internal record A {
        |}
        |
        |service Srv {
        |  f(a : A) : void
        |}
        |
      """.stripMargin
    )
  }
}

