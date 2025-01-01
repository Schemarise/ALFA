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

import com.schemarise.alfa.compiler.ast.Namespace
import com.schemarise.alfa.compiler.ast.nodes.Trait
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class NamespaceTest extends AnyFunSuite {

  test("Multi-namespace") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait X { }
        |
        |namespace B.C
        |trait Y { }
        |
        |namespace A.B
        |trait Z { }
      """)

    assert(cua.getUdt("A.X").isDefined)
    assert(cua.getUdt("B.C.Y").isDefined)
    assert(cua.getUdt("A.B.Z").isDefined)
  }

  test("Namespace in UDT declaration") {
    val cua = TestCompiler.compileValidScript("trait A.B.C { }")

    val udt = cua.getUdt("A.B.C")
    assert(udt.isDefined)

    val tr: Trait = udt.get.asInstanceOf[Trait]

    assert(tr.versionedName.namespace.equals(Namespace("A.B")))
    assert(tr.versionedName.name.equals("C"))
  }

  test("Dot within namespace UDTs") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait B.C { }
        |
        |record Foo {
        |    F1 : A.B.C // fully qualified
        |    F2 : B.C   // relative
        |}
      """)

    assert(cua.getUdt("A.B.C").isDefined)
    assert(cua.getUdt("A.Foo").isDefined)
  }


  test("Namespace conflict with name") {
    TestCompiler.compileInvalidScript(
      "@3:0 The user defined type 'A.B.C' conflicts with a namespace by the same name",
      """
        |namespace A.B
        |record C {
        |   F1 : string
        |}
        |
        |namespace A.B.C
        |
        |record D {
        |   F2 : int
        |}
      """)
  }


  test("Relative and absolute UDT names") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A.B
        |record C {
        |   F1 : string
        |}
        |
        |record A.X.Y {
        |}
      """)

    val c = cua.getUdt("A.B.C").get
    val y = cua.getUdt("A.X.Y").get
  }

  test("Keyword in namespace") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace foo.record.bar
        |
        |record Buffer {
        |   data : binary
        |}
      """)
  }

}

