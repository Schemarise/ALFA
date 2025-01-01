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
package com.schemarise.alfa.compiler.test.lang.feature.modifiers

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class InternalModifierTest extends AnyFunSuite {

  test("Modifier test") {
    val cua1 = TestCompiler.compileValidScript(
      """|
         |internal record A.Data {
         |  F1 : int
         |}
         |
      """.stripMargin)

    val udt1 = cua1.getUdt("A.Data").get
    assert(udt1.isInternal)

    val cua2 = TestCompiler.compileValidScript(
      """|
         |record A.Data {
         |  F1 : int
         |}
         |
      """.stripMargin)

    val udt2 = cua2.getUdt("A.Data").get
    assert(!udt2.isInternal)
  }


  test("Modifier test service") {
    val cua1 = TestCompiler.compileValidScript(
      """|
         |internal service A.Service1 {
         |}
         |
      """.stripMargin)

    val udt2 = cua1.getUdt("A.Service1").get
    assert(udt2.isInternal)
  }

  test("Modifier test lib") {
    val cua1 = TestCompiler.compileValidScript(
      """|
         |internal library A.LibA {
         |}
         |
         |library A.LibB {
         |}
      """.stripMargin)

    assert(cua1.getUdt("A.LibA").get.isInternal)
    assert(!cua1.getUdt("A.LibB").get.isInternal)
  }

}
