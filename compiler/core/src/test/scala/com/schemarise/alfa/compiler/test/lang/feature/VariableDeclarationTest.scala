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

import com.schemarise.alfa.compiler.ast.nodes.Library
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class VariableDeclarationTest extends AnyFunSuite {
  test("let usage") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |library Foo {
        |    f() : void {
        |       let x = 10
        |    }
        |}
      """)
  }

  test("var usage") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |library Bar {
        |    f() : void {
        |       var x : int
        |       x = 10
        |    }
        |}
      """)
  }

  test("obj assign usage") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |trait T {
        |   Ts : int?
        |}
        |
        |record A {
        |    i : int
        |}
        |
        |record B includes T {
        |   a : A?
        |}
        |
        |record C {
        |   b : B
        |}
        |
        |library Bar {
        |    f() : void {
        |       var c : C
        |       c.b.a.i = 10
        |    }
        |}
      """)
  }

  test("var assign check 3") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |record A {
        |    i : int
        |}
        |
        |record B {
        |   a : A?
        |}
        |
        |library Bar {
        |    f() : void {
        |       var a = 10
        |       var b : B
        |       b.a.i = 10
        |    }
        |}
      """)
  }

  test("obj assign check 3") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |record A {
        |    i : int
        |}
        |
        |record B {
        |   a : A?
        |}
        |
        |library Bar {
        |    f() : void {
        |       var b : B
        |       b.a.i = 10
        |    }
        |}
      """)
  }
}

