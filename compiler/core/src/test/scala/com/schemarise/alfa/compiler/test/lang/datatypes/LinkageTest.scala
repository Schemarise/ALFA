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

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class LinkageTest extends AnyFunSuite {
  test("linkage 1") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait A {
        |    a : int
        |}
        |
        |trait B {
        |    b : int
        |
        |    linkage link1 (b + 100) => A(a)
        |    linkage link2 (b + 100) => A?(a)
        |    linkage link3 (b + 100) => list<A>(a)
        |    linkage link4 (b + 100) => list<A>?(a)
        |}
        |
    """.stripMargin)
  }

  test("linkage 3") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Trade {
        |    Tid : int
        |
        |    # This links to audit records
        |    linkage auditRecords (Tid) => TradeAudit(TAid)
        |}
        |
        |trait TradeAudit {
        |    TAid : int
        |}
        |
    """.stripMargin)
  }
}
