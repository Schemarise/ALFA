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

class ExpressionsTest extends AnyFunSuite {
  test("Number k m b") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Plain {
        | F : int = 30k
        |}
      """)
  }

  test("colon in string") {
    TestCompiler.compileValidScript(
      """
        |namespace foo.bar
        |
        |library Lib {
        | fn() : void {
        |   let x = "ForeignExchange:NDO"
        | }
        |}
        |""".stripMargin)
  }

  test("Todo code") {
    TestCompiler.compileValidScript(
      """
        |namespace foo.bar
        |
        |library Lib {
        | fn1() : void {
        |   ???
        | }
        |
        | fn2() : int {
        |   ???
        | }
        |}
        |""".stripMargin)
  }
}

