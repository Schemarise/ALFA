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


class DataProductChangeTest extends AnyFunSuite {

  test("DataProductChange") {
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

  test("DataProductChange item added to DP") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Sample
        |
        |record A { }
        |
        |dataproduct DP {
        |    publish { A }
        |}
      """.stripMargin
    )

    val cua2 = TestCompiler.compileValidScript(
      """
        |namespace Sample
        |
        |record A { }
        |
        |record B { }
        |
        |dataproduct DP {
        |    publish { A B }
        |}
      """.stripMargin
    )

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val mods = new ChangeAnalyzer().analyzeVersions(cs)

    val expected =
      """
        |{
        |  "$type" : "schemarise.alfa.runtime.model.diff.Modifications",
        |  "Results" : {
        |    "DataStructureUpsert" : [ {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtModification",
        |      "EditType" : "Added",
        |      "ChangeCategory" : "DataStructureUpsert",
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Record",
        |        "UdtName" : "Sample.B"
        |      }
        |    } ],
        |    "DataProductChange" : [ {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtEntryModification",
        |      "EditType" : "Added",
        |      "ChangeCategory" : "DataProductChange",
        |      "Message" : "Additional types published",
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Dataproduct",
        |        "UdtName" : "Sample.DP"
        |      },
        |      "EntryName" : "Sample.B",
        |      "EntryType" : "Publish"
        |    } ]
        |  }
        |}
        |""".stripMargin

    assert(Alfa.jsonCodec().fromJsonString(expected).equals(mods))
  }

  test("DataProductChange service dependent changed ") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Sample
        |
        |record A { }
        |
        |record B { a : A }
        |
        |service S {
        |    f() : B
        |}
        |
        |dataproduct DP {
        |    publish { S }
        |}
      """.stripMargin
    )

    val cua2 = TestCompiler.compileValidScript(
      """namespace Sample
        |
        |record A { x : string }
        |
        |record B { a : A }
        |
        |service S {
        |    f() : B
        |}
        |
        |dataproduct DP {
        |    publish { S }
        |}
      """.stripMargin
    )

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val mods = new ChangeAnalyzer().analyzeVersions(cs)

    val expected =
      """
        |{
        |  "$type" : "schemarise.alfa.runtime.model.diff.Modifications",
        |  "Results" : {
        |    "BreakingDataStructureChange" : [ {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtEntryModification",
        |      "EditType" : "Added",
        |      "ChangeCategory" : "BreakingDataStructureChange",
        |      "AfterSnippet" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.Snippet",
        |        "Code" : "x : string"
        |      },
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Record",
        |        "UdtName" : "Sample.A"
        |      },
        |      "EntryName" : "x",
        |      "EntryType" : "Field"
        |    } ],
        |    "IndirectBreakingDataStructureChange" : [ {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtModification",
        |      "EditType" : "Updated",
        |      "ChangeCategory" : "IndirectBreakingDataStructureChange",
        |      "Message" : "Sample.B > Sample.A",
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Record",
        |        "UdtName" : "Sample.B"
        |      }
        |    }, {
        |      "$type" : "schemarise.alfa.runtime.model.diff.DataproductModifications",
        |      "EditType" : "Updated",
        |      "ChangeCategory" : "IndirectBreakingDataStructureChange",
        |      "DataproductName" : "Sample.DP",
        |      "PublishImpactPaths" : [ "Sample.S", "Sample.B" ],
        |      "ConsumeImpactPaths" : [ ]
        |    } ],
        |    "BreakingApiChange" : [ {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtEntryModification",
        |      "EditType" : "Updated",
        |      "ChangeCategory" : "BreakingApiChange",
        |      "Message" : "Path: Sample.B",
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Service",
        |        "UdtName" : "Sample.S"
        |      },
        |      "EntryName" : "Sample.B",
        |      "EntryType" : "ReachableType"
        |    }, {
        |      "$type" : "schemarise.alfa.runtime.model.diff.UdtEntryModification",
        |      "EditType" : "Updated",
        |      "ChangeCategory" : "BreakingApiChange",
        |      "Message" : "Path: Sample.B, a",
        |      "TargetUdt" : {
        |        "$type" : "schemarise.alfa.runtime.model.diff.UdtReference",
        |        "UdtType" : "Service",
        |        "UdtName" : "Sample.S"
        |      },
        |      "EntryName" : "Sample.A",
        |      "EntryType" : "ReachableType"
        |    } ]
        |  }
        |}
        |
        |""".stripMargin

    println(Alfa.jsonCodec().toFormattedJson(mods))

    assert(Alfa.jsonCodec().fromJsonString(expected).equals(mods))
  }

  test("DataProduct Versions") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Sample
        |
        |record A { }
        |
        |record B { a : A }
        |
        |service S {
        |    f() : B
        |}
        |
        |dataproduct DP {
        |    publish { S }
        |}
      """.stripMargin
    )

    val cua2 = TestCompiler.compileValidScript(
      """namespace Sample
        |
        |record A@1 { }
        |
        |record A { x : string }
        |
        |record B@1 { a : A@1 }
        |
        |record B { a : A }
        |
        |service S {
        |    f() : B
        |}
        |
        |service S @1 {
        |    f() : B@1
        |}
        |
        |dataproduct DP@1 {
        |    publish { S@1 }
        |}
        |
        |
        |dataproduct DP {
        |    publish { S }
        |}
      """.stripMargin
    )

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val mods = new ChangeAnalyzer().analyzeVersions(cs)

    println(mods)
  }
}
