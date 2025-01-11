package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios.QualityImprovements
import org.scalatest.funsuite.AnyFunSuite

class QualityImprovementsTest extends AnyFunSuite {

  test("QualityImprovements") {
    val v1 =
      """
        |namespace Change
        |
        |# Doc for entity
        |entity B1 key( a : int ) {
        |    G1 : int
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |# Doc for entity3
        |entity B1 key( a : int ) {
        |
        |    G1 : int// ## This is a comment
        |}
        |
        """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = QualityImprovements.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
