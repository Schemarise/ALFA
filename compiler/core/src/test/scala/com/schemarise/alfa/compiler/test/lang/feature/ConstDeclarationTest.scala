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
import com.schemarise.alfa.compiler.utils.{AlfaTestPaths, TestCompiler}
import org.scalatest.funsuite.AnyFunSuite

class ConstDeclarationTest extends AnyFunSuite with AlfaTestPaths {

  test("dup const") {
    val s =
      """
        |const AA = "a very long expression"
        |const AA = 10
        |
      """.stripMargin

    TestCompiler.compileInvalidScript("@2:6 Duplicate const 'AA'", s)
  }

  test("simple const") {
    val s =
      """
        |
        |const AA = "a very long expression"
        |const dN = Direction.N
        |
        |enum Direction { N S E W }
        |
        |library X {
        |  fn() : string {
        |     let a = $AA
        |     let d = $dN
        |
        |     return a
        |  }
        |}
        |
      """.stripMargin

    val cua = TestCompiler.compileValidScript(s)

    val b = cua.getUdt("X").get.asInstanceOf[Library].getMethodDecls().get("fn")
    //    val v = b.statements.head.asInstanceOf[VariableDeclarationStatement]

    println(b)
  }
}
