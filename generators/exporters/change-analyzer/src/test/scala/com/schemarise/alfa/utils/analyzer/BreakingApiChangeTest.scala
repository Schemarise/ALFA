package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios._
import org.scalatest.funsuite.AnyFunSuite


class BreakingApiChangeTest extends AnyFunSuite {

  test("BreakingApiChange 1") {
    val v1 =
      """
        |namespace Change
        |
        |service Srv {
        |    unchanged( a : string, b : date ) : long
        |    willChange( a : int ) : void
        |    willDelete( a : datetime ) : uuid
        |}
        |
        |service SrvDel { }
        |
        |library LibDel { }
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |service Srv {
        |    unchanged( a : string, b : date ) : long
        |    willChange( a : int, b : string ) : void
        |    willAdd( b : duration ) : period
        |}
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingApiChanges.run(cs, List.empty)
    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }

  test("BreakingApiChange 2 ") {
    val v1 =
      """
        |namespace Change
        |
        |trait Trade scope Equity {}
        |
        |record Equity includes Trade {}
        |
        |record Portfolio {
        |    Trades : list<Trade>
        |}
        |
        |service Srv {
        |    getPortfolio() : Portfolio
        |}
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |trait Trade scope Equity, FX {}
        |
        |record Equity includes Trade {
        |    stock : string
        |}
        |record FX includes Trade {}
        |
        |record Portfolio {
        |    Trades : list<Trade>
        |}
        |
        |service Srv {
        |    getPortfolio() : Portfolio
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)

    val breakingDataStructs = BreakingDataStructureChange.run(cs)

    val res = BreakingApiChanges.run(cs, breakingDataStructs)
    assert(res.size == 4)
  }

  test("BreakingApiChange 3") {
    val v1 =
      """
        |namespace Change
        |
        |record A { }
        |record B { a : A }
        |record C { b : B }
        |record D { c : C }
        |
        |service Srv {
        |    f( d : D ) : void
        |}
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |record A { a : int }
        |record B { a : A }
        |record C { b : B }
        |record D { c : C }
        |
        |service Srv {
        |    f( d : D ) : void
        |}
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)

    val breakingDataStructs = BreakingDataStructureChange.run(cs)

    val res = BreakingApiChanges.run(cs, breakingDataStructs)
    assert(res.size == 4)

    assert(res.head.getMessage.get == "Path: Change.D, d")
  }
}
