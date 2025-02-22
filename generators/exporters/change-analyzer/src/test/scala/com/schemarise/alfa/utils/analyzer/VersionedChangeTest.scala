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
 
package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import org.scalatest.funsuite.AnyFunSuite


class VersionedChangeTest extends AnyFunSuite {

  // TODO
  test("Versioned") {
    val v1 =
      """namespace Sample
        |
        |record A { }
        |
        |record B { }
        |
        |service C {
        |    f( a : string ) : void
        |}
        |
        |dataproduct DP {
        |    publish { A C }
        |}
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Sample
        |
        |record A {
        |   A : int
        |}
        |
        |service C {
        |    f( a : string, b : string ) : void
        |}
        |
        |record B { }
        |
        |dataproduct DP {
        |    publish { A B C }
        |}
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val ca = new ChangeAnalyzer()

    val mods = ca.analyzeVersions(cs)
    println(Alfa.jsonCodec().toFormattedJson(mods))
  }
}
