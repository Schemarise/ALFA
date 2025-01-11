package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios.DataStructureUpsert
import org.scalatest.funsuite.AnyFunSuite

class DataStructureUpsertTest extends AnyFunSuite {

  test("DataStructureUpsert") {
    val v1 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change2
        |
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int
        |    F2 : string ?
        |}
        |
        |record Rec2 {
        |    F1 : int
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = DataStructureUpsert.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
