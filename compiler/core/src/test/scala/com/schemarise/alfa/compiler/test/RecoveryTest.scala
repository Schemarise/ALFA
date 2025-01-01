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

class RecoveryTest extends AnyFunSuite {

  test("enclosed resolve error") {
    TestCompiler.compileInvalidScript(
      "@3:28 Syntax error : extraneous input '<'",
      """
        |annotation References( entity ) {
        |    EntityRef : list[ typeof< entity > ]
        |}
        |
      """)
  }

  test("Top level breakages - fields ") {
    TestCompiler.compileInvalidScript(
      "@4:0 mismatched input '}'",
      """
        |fields {
        |    sd
        |}
        """)
  }

  test("Top level breakages - typedefs ") {
    TestCompiler.compileInvalidScript("@4:0 mismatched input '}'",
      """
        |typedefs {
        |    sd
        |}
      """)
  }

  test("Top level breakages - include error") {
    TestCompiler.compileInvalidScript("@2:8 Syntax error in ''Fo'",
      """
        |include 'Foo.Bar.s'
         """)
  }

  test("Break in comment") {
    val cua1 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#  Comment about record A
        |                sdf
        |              #/
        |record B {
        |}""")
    assert(cua1.hasWarnings)

    val cua2 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#  Comment about record A
        |                 sdf
        |              #/
        |record B {
        |}""")
    assert(!cua2.hasWarnings)


    val cua3 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#  Comment about record A
        |                  sdf
        |              #/
        |record B {
        |}""")
    assert(!cua3.hasWarnings)


    val cua4 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#
        |             Comment about record A
        |             sdf
        |              #/
        |record B {
        |}""")
    assert(!cua4.hasWarnings)


    val cua5 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#
        |             Comment about record A
        |              sdf
        |              #/
        |record B {
        |}""")
    assert(!cua5.hasWarnings)

    val cua6 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#
        |             Comment about record A
        |            sdf
        |              #/
        |record B {
        |}""")
    assert(cua6.hasWarnings)


    val cua7 = TestCompiler.compileValidScript(
      """
        |record A { }
        |
        |             /#
        |             Comment about record A
        |             sdf #/
        |record B {
        |}""")
    assert(!cua7.hasWarnings)
  }


  test("Quick test n") {
    TestCompiler.compileValidScript(
      """
      """)
  }
}
