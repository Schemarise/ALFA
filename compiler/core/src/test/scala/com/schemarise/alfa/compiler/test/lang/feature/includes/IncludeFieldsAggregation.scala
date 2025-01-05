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
package com.schemarise.alfa.compiler.test.lang.feature.includes

import com.schemarise.alfa.compiler.TraverseUtils
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class IncludeFieldsAggregation extends AnyFunSuite {
  test("IncludeFieldsAggregation") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait A {
        |   AF1 : int
        |   AF2 : string
        |}
        |
        |trait B includes A {
        |   BF1 : date
        |   BF2 : long
        |}
        |
        |record C includes B {
        |   CF1 : datetime
        |   CF2 : uuid
        |}
        """)

    assert(TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered().get).size == 3)

    assert(List("AF1", "AF2").equals(cua.getUdt("A").get.allFields.map(e => e._1)))
    assert(List("AF1", "AF2", "BF1", "BF2").equals(cua.getUdt("B").get.allFields.map(e => e._1)))
    assert(List("AF1", "AF2", "BF1", "BF2", "CF1", "CF2").equals(cua.getUdt("C").get.allFields.map(e => e._1)))

  }
}
