package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios._
import org.scalatest.funsuite.AnyFunSuite


class ApiUpsertTest extends AnyFunSuite {

  test("BreakingApiChange") {
    val v1 =
      """
        |namespace Change
        |
        |service S1 {}
        |
        |library L1 {
        |   f1() : void {}
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |service S1 {}
        |service S2 {}
        |
        |library L1 {
        |   f1() : void {}
        |   f2() : void {}
        |}
        |
        |library L2 {}
        |
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = ApiUpsert.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
