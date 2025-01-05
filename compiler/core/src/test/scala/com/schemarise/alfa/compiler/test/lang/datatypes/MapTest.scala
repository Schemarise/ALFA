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
package com.schemarise.alfa.compiler.test.lang.datatypes

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class MapTest extends AnyFunSuite {
  test("Map double key test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record R {
        |    E : map< A, string >
        |}
        |
        |record A {
        |    f : B
        |}
        |
        |record B {
        |    f : double
        |}
      """)

    assertResult("@5:13 The type 'double' is not permitted in a map key. com.acme.A > com.acme.B")(cua.getWarnings.head.toString)
  }

  test("Map trait key test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record R {
        |    E : map< A, string >
        |}
        |
        |record A {
        |    f : B
        |}
        |
        |trait B {
        |}
      """)
    assertResult("@5:13 The type 'trait com.acme.B' is not permitted in a map key. com.acme.A")(cua.getWarnings.head.toString)

  }


  test("Map trait scope key test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record R {
        |    E : map< A, string >
        |}
        |
        |record A {
        |    f : B
        |}
        |
        |trait B scope C {}
        |
        |record C includes B {}
      """)
  }

  test("Map trait includes key test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record R {
        |    E : map< A, string >
        |}
        |
        |record A includes B {
        |    f : string
        |}
        |
        |trait B {
        |}
      """)
  }

  test("Map type test") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:13 The type 'future' is not permitted in a map key. ",
      """
        |namespace com.acme
        |
        |record R {
        |    E : map< future< int >, string >
        |}
      """)
  }
}
