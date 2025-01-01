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

class WarningsTest extends AnyFunSuite {
  test("Underscores in UDT name") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |record Request___Error {
        |}
        |
      """)
    val w = cua.getWarnings.head

    this.assert(w.toString.equals("@3:7 Triple underscore in name 'Request___Error' should be avoided as it may conflict with generated code"))
  }

  test("Underscores in Field name") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |record Request {
        |    Field___Name : string
        |}
        |
      """)
    val w = cua.getWarnings.head

    this.assert(w.toString.equals("@4:4 Triple underscore in name 'Field___Name' should be avoided as it may conflict with generated code"))
  }

  test("Tuple name no warn") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |record Request {
        |    A : tuple< IVal :int, SVal :string >
        |    B : union< IVal :int, SVal :string >
        |}
        |
      """)

    this.assert(cua.getWarnings.size == 0)
  }

}
