package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.tools.tabular.UdtFlattener
import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.utils.testing.AlfaFunSuite


// TODO combine all flatten tests here
class UdtFlattenerTest extends AlfaFunSuite {

  test("Udt visit") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.AllVectors.Map
        |
        |record MapOfTraits {
        |    F1 : map< string, Base >
        |}
        |
        |trait Base {
        |    Data : string
        |}
        |
        |record Impl1 includes Base {}
        |record Impl2 includes Base {}
        |record Impl3 includes Base {}
        |
        |
    """)

    val f = new UdtFlattener(cua, cua.getUdt("Feature.AllVectors.Map.MapOfTraits").get)
    println(f.table)

  }

  def compare(l: List[String], r: List[String]): Unit = {
    if (!l.equals(r))
      println(r)
    assert(l.equals(r))
  }

  test("scalar") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  Scalar : int
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    equalsIgnoringWhitespace("Scalar : int", f.table.allColumns.map(_.toString).mkString(""))
  }

  test("opt scalar") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : int?
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    equalsIgnoringWhitespace("a$Set : boolean a : int", f.table.allColumns.map(_.toString).mkString(""))
  }

  test("seq of scalar") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : list< int >
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    equalsIgnoringWhitespace("a$Idx : long a : int", f.table.allColumns.map(_.toString).mkString(""))
  }


  test("seq of UDT") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : list< B >
        |}
        |
        |record B { b : int }
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a$Idx : long", "b : int"), (f.table.allColumns.map(_.toString)))
  }

  test("seq of opt int") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : list< int? >
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a$Idx : long", "entry$Set : boolean", "entry : int"), (f.table.allColumns.map(_.toString)))
  }

  test("opt seq of int") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : list< int >?
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a$Set : boolean", "a$Idx : long", "a : int"), (f.table.allColumns.map(_.toString)))
  }

  test("set of scalar") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : set< string >
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a$Idx : long", "a : string"), (f.table.allColumns.map(_.toString)))
  }

  test("map of scalars") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : map< string, long >
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("key : string", "value : long"), (f.table.allColumns.map(_.toString)))
  }

  test("map scalar to udt") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : map< string, B >
        |}
        |
        |record B { b : int }
        |
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("key : string", "b : int"), (f.table.allColumns.map(_.toString)))
  }

  test("map of udt to udt") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : map< C, B >
        |}
        |
        |record B { b : int }
        |
        |record C { c : string }
        |
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("c : string", "b : int"), (f.table.allColumns.map(_.toString)))
  }

  test("tuple") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : tuple< F1 : int, F2 : C >
        |}
        |
        |record C { c : int }
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("F1 : int", "c : int"), (f.table.allColumns.map(_.toString)))
  }

  test("in-place-union") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : union< F1 : int, F2 : C >
        |}
        |
        |record C { c : int }
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a$Case : string", "F1 : int", "c : int"), (f.table.allColumns.map(_.toString)))
  }

  test("in-place-enum") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |  a : enum< X, Y, Z >
        |}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    compare(List("a : string"), (f.table.allColumns.map(_.toString)))
  }


  test("trait field") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        |       a : Base
        |    }
        |
        |    record B includes Base {
        |       b : int
        |    }
        |
        |    record C includes Base {
        |       c : int
        |    }
        |
        |    trait Base {}
      """)
    val f = new UdtFlattener(cua, cua.getUdt("A").get)
    this.equalsIgnoringWhitespace("a$TImpl : string B.b : int C.c : int", f.table.allColumns.map(_.toString).mkString(""))
  }

}
