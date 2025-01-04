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

import com.schemarise.alfa.compiler.ast.nodes.Trait
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class IncludesTest extends AnyFunSuite {
  test("Annotated includes") {
    val cua = TestCompiler.compileValidScript(
      """
        |annotation Foo (includes) {
        |}
        |
        |trait B { }
        |trait C { }
        |
        |@alfa.db.Table
        |entity A includes @Foo B, C {
        |}
        |
    """.stripMargin)
  }

  test("Includes redefines vars") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |trait A {
        |    Name : string
        |}
        |
        |trait B includes A {
        |    Name : string = ""
        |}
        |
    """.stripMargin)

    assert(cua.hasWarnings)
  }

  test("Field redefined") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |trait Foo {
        |   A : int
        |}
        |
        |record Bar includes Foo {
        |   A : int
        |}
        |
    """.stripMargin)

    assert(cua.hasWarnings)
  }


  test("scope usage 1") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |trait Base scope A, B, C, R { }
        |
        |trait A includes Base scope T { }
        |
        |trait B includes Base scope T { }
        |
        |trait C includes Base scope T { }
        |
        |record R includes Base { }
        |
        |record T includes A, B, C  { }
        |
    """.stripMargin)

    val t = cua.getUdt("redefine.Base").get.asInstanceOf[Trait]
    assert(t.scope.size == 4)
    assert(t.scope.head.fullyQualifiedName == "redefine.A")

    TestCompiler.compileInvalidScript(
      "@4:26 Type used in scope 'R' does not include 'Base'",
      """
        |namespace redefine
        |
        |trait Base scope A, B, C, R { }
        |
        |trait A includes Base { }
        |
        |trait B includes Base { }
        |
        |trait C includes Base { }
        |
        |record R { }
        |
    """.stripMargin)
  }

  test("scope usage 2") {
    TestCompiler.compileInvalidScript(
      "@8:17 Cannot use 'A' as includes unless 'C' is listed in scope of 'A'",
      """
        |namespace redefine
        |
        |trait A scope B { }
        |
        |trait B includes A { }
        |
        |trait C includes A { }
        |
    """.stripMargin)
  }


  test("scope usage 3") {
    TestCompiler.compileInvalidScript(
      "@6:0 Includes of trait 'A' which defines scope, requires 'B' also to define scope",
      """
        |namespace redefine
        |
        |trait A scope B { }
        |
        |trait B includes A { }
    """.stripMargin)

    //    TestCompiler.compileValidScript(
    //      """
    //        |namespace redefine
    //        |
    //        |trait A scope B { }
    //        |
    //        |trait B includes A scope C { }
    //        |
    //        |record C includes B { }
    //    """.stripMargin)
  }

  test("Scope cycles") {
    TestCompiler.compileInvalidScript(
      "@4:6 Cycle detected in Trait included from A   foo.bar.D > foo.bar.C > foo.bar.B > foo.bar.A > foo.bar.B",
      """
        |namespace foo.bar
        |
        |trait A includes B scope B {}
        |
        |trait B includes A scope C {}
        |
        |trait C includes B scope D {}
        |
        |record D includes C {}
        |
    """.stripMargin)
  }

  test("Scope empty") {
    TestCompiler.compileValidScript(
      """
        |namespace foo.bar
        |
        |trait A scope B {}
        |
        |trait B includes A scope {}
        |
    """.stripMargin)
  }
}
