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
