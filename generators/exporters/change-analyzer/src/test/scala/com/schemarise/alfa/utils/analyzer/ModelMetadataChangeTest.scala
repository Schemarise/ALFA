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
import com.schemarise.alfa.utils.analyzer.scenarios.ModelMetadataChange
import org.scalatest.funsuite.AnyFunSuite

class ModelMetadataChangeTest extends AnyFunSuite {

  test("BreakingApiChange") {
    val v1 =
      """
        |namespace Change
        |
        |annotation AnnR ( record ) { }
        |annotation AnnS ( service ) { }
        |annotation AnnM ( method ) { }
        |annotation AnnF ( field ) { }
        |annotation AnnN ( namespace ) { }
        |        |
        |service S1 {
        |    f1() : void
        |    f2() : void
        |}
        |
        |record R1 {
        |   F : int
        |   G : string
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |@Change.AnnN
        |namespace Change
        |
        |annotation AnnR ( record ) { }
        |annotation AnnS ( service ) { }
        |annotation AnnM ( method ) { }
        |annotation AnnF ( field ) { }
        |annotation AnnN ( namespace ) { }
        |
        |@AnnS
        |service S1 {
        |    f() : void
        |    @AnnM
        |    f2() : void
        |}
        |
        |@AnnR
        |record R1 {
        |   @AnnF
        |   F : int
        |   G : string
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = ModelMetadataChange.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
