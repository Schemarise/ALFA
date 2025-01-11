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

class ToStringTest extends AnyFunSuite {
  test("ToString Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.ToString
        |
        |trait SampleTrait {
        |}
        |
        |record SampleRec includes SampleTrait {
        |      F0 : int(*, 100)
        |      F1 : int(100,1000)
        |
        |      F2 : string(1, 10)
        |      F3 : string(".*")
        |
        |      F4 : date(*, "2021-01-01")
        |      F5 : date("2020-01-01", "2021-01-01")
        |      F6 : date(*, *, "YYYY-MM")
        |
        |      F7 : datetime(*, "2021-01-01T08:42:32.232", "DD-MM-YYYY" )
        |      F9 : time("12:42:21.321", *, "HH:mm:SS")
        |
        |      F10 : decimal(12,10)
        |      F11 : decimal(12,10, 0.0, *)
        |      F12 : decimal(12,2, *, 100.0)
        |}
        |
      """.stripMargin)

    val rec = cua.getUdt("Feature.ToString.SampleRec").get
    val asStr = rec.toString

  }
}
