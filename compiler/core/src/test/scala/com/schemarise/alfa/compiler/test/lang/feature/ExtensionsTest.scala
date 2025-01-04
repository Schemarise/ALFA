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

class ExtensionsTest extends AnyFunSuite {
  test("Extension simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |extension task {
        |    Variations : map< string, string >?
        |}
        |namespace Db
        |
        |task Foo (
        |)
        |
      """)
  }


  test("Extension type check 2") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |extension task {
        |    a1 : map< string, int >
        |}
        |namespace Db
        |
        |task Foo (
        |   a1 = { "sdf" : 21 }
        |)
        |
      """)
  }

  test("Extension err 1") {
    val cua = TestCompiler.compileInvalidScript(
      "@7:0 Unknown top level declaration or extension 'tasks'",
      """
        |
        |extension task {
        |  s : int
        |}
        |
        |tasks Foo (
        |   s = 1
        |)
        |
      """)
  }

}

