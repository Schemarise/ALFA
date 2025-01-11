package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios._
import org.scalatest.funsuite.AnyFunSuite


class PotentialApiChangeTest extends AnyFunSuite {

  test("PotentialApiChange") {
    val v1 =
      """
        |namespace Change
        |
        |library Lib {
        |    unchanged( a : string, b : date ) : void { }
        |    willChange( a : int ) : void { }
        |    willDelete( a : datetime ) : void { }
        |}
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |library Lib {
        |    unchanged( a : string, b : date ) : void { }
        |    willChange( a : int, b : string ) : void { }
        |    willAdd( b : duration ) : void { }
        |}
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val ca = new ChangeAnalyzer()

    val res = PotentialApiChange.run(cs)
    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
