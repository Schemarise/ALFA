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

import com.schemarise.alfa.compiler.test.lang.AlfaCoreFunSuite
import com.schemarise.alfa.compiler.utils.TestCompiler

class DocCommentsTest extends AlfaCoreFunSuite {
  test("Doc comments test") {

    val cua = TestCompiler.compileValidScript(
      """
        |# Comment on top
        |record R1 {
        |}
        |
        |/#
        |   Comment on
        |   multiple lines
        | #/
        |record R2 {
        |}
        |
        |record R3 ## Comment on same line
        |{
        |    F1 : int     ## comments on F1
        |    F2 : string  ## comments on F2
        |}
        |
      """)

    val r1 = cua.getUdt("R1").get
    val r2 = cua.getUdt("R2").get
    val r3 = cua.getUdt("R3").get
    val f1 = r3.allFields.get("F1").get
    val f2 = r3.allFields.get("F2").get

    assert(r1.docs.head.text.equals("Comment on top"))
    val ml = r2.docs.head.text
    assert(ml.trim.equals("Comment on\nmultiple lines") || ml.equals("Comment on\r\nmultiple lines"))
    assert(r3.docs.head.text.equals("Comment on same line"))

    assert(f1.docs.head.text.equals("comments on F1"))
    assert(f2.docs.head.text.equals("comments on F2"))
  }

  test("Doc comments error 1") {

    val cua = TestCompiler.compileValidScript(
      """
        |
        |record R3
        |{
        |    F1 : int     ## comments on F1
        |    # Comment on F2
        |    F2 : string
        |
        |    F3 : int
        |}
      """)

    val r3 = cua.getUdt("R3").get
    val f1 = r3.allFields.get("F1").get
    val f2 = r3.allFields.get("F2").get

    println(r3)

    // assert( f1.nodeMeta.docs.head.text.equals("comments on F1") )
    assert(f1.docs.head.text.equals("comments on F1"))
  }


  test("Doc comments error 2") {
    val cua = TestCompiler.compileValidScript(
      """
        |# Test comment
        |record Test.R3
        |{
        |    F2 : string
        |}
      """)

    val r3 = cua.getUdt("Test.R3").get

    val h = r3.docs.head
    assert(r3.docs.head.text.equals("Test comment"))
  }
}

