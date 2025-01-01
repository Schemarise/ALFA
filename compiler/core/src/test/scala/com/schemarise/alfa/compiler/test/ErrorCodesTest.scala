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
package com.schemarise.alfa.compiler.test

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class ErrorCodesTest extends AnyFunSuite {
  test("Duplicate field") {
    val cua = TestCompiler.compileInvalidScript("@6:3 Duplicate field 'F1'",
      """
        |trait A {
        |   F1 : int
        |   F2 : double
        |   F3 : date
        |   F1 : string
        |}
      """)
  }

  test("Duplicate field from ignore case / case sensitive") {
    val cua = TestCompiler.compileInvalidScript("@4:3 Duplicate field 'f1'",
      """
        |trait A {
        |   F1 : int
        |   f1 : string
        |}
      """)
  }

  test("Narrowing errors test") {
    val s =
      """
        |namespace Sample
        |
        |record Bar {
        |}
        |
        |record Foo {
        |   A : int
        |   B : int
        |
        |   assert F {
        |       return if ( A > 10 )
        |                 some("err")
        |              else if ( B < 10 )
        |                 some("err")
        |              else
        |                 none
        |       return none
        |   }
        |}
      """.stripMargin

    val cua = TestCompiler.compileScriptOnly(s, false)

    cua.getErrors.foreach(e => {
      val l = e.location

      println(s"${l.getStartLine}:${l.getStartColumn} -> ${l.getEndLine}:${l.getEndColumn} ${e.formattedMessage}")

    })

  }


}