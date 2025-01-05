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

import com.schemarise.alfa.compiler.ast.nodes.EnumDecl
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class EnumTest extends AnyFunSuite {
  test("Enum resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace  Example
        |enum Colour { R G B("Blue") `10Days` }
      """)
    val o = cua.getUdt("Example.Colour").get
    assert(o.isInstanceOf[EnumDecl])
    val i = o.asInstanceOf[EnumDecl]

    assert(i.versionedName.fullyQualifiedName.equals("Example.Colour"))
    assert(i.versionedName.name.equals("Colour"))

    println(i)

    assert(i.allFields.contains("R"))
  }

  test("Enum escaped") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace  Example
        |
        |enum Colour { `1` `3` A }
        |
        |record Rec {
        |    C : Colour = Colour.A
        |
        |    assert CVal {
        |        let x = C == Colour.`1`
        |    }
        |}
        |
      """)

    println(cua.getUdt("Example.Rec"))
  }

}
