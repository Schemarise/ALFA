package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler}
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig
import org.scalatest.funsuite.AnyFunSuite
import schemarise.alfa.runtime.model._

import scala.collection.JavaConverters._

class CompilerToRuntimeTypesTest extends AnyFunSuite {

  val logger = new StdoutLogger

  test("type defs") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |  money = string
        |}
        |
        |record NS.Trans {
        |  F1 : money
        |  F2 : map< string, money >
        |  F3 : list< money ? >
        |}
      """)

    val c2r = CompilerToRuntimeTypes.create(new StdoutLogger(), cua, true)
    val u = c2r.getUdtDetails("NS.Trans").getResult.asInstanceOf[UdtBaseNode]
    val f1 = u.getAllFields.get("F1").getDataType.getTypeDefName.get()
    assert(f1.equals("money"))

    val f2 = u.getAllFields.get("F2").getDataType.asInstanceOf[MapDataType].getValueType.getTypeDefName.get()
    assert(f2.equals("money"))

    val f3 = u.getAllFields.get("F3").getDataType.asInstanceOf[ListDataType].
      getComponentType.asInstanceOf[EnclosingDataType].getComponentType.getTypeDefName.get()

    assert(f3.equals("money"))
  }

  test("Enum sig") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Ann
        |annotation Marker ( field ) {
        |}
        |
        |namespace Plain
        |
        |enum Consts {
        |   D A B C
        |}
      """)

    val c2r = CompilerToRuntimeTypes.create(new StdoutLogger(), cua)
    val u = c2r.getUdtDetails("Plain.Consts").getResult

    assert(u.getChecksum.length > 0)
  }


  test("Transitive sig") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Ann
        |annotation Marker ( field ) {
        |}
        |
        |namespace Model
        |
        |trait Foo {
        |   F1 : int
        |}
        |
        |record Bar1 includes Foo {
        |    B1 : int
        |}
        |
        |record Bar2 includes Foo {
        |    B1 : int
        |}
        |
        |record Baz {
        |    F1 : int
        |    F2 : Foo
        |    F3 : Consts
        |}
        |
        |enum Consts {
        |   D A B C
        |}
      """)

    val c2r = CompilerToRuntimeTypes.create(new StdoutLogger(), cua)
    val u = c2r.getUdtDetails("Model.Baz").getResult

    println(u.getChecksum)
    assert(u.getChecksum.length > 0)
  }

  test("Test model-id is set") {
    val cua = TestCompiler.compileValidScript(
      """
        |language-version 3
        |model-id "iso-123:456"
        |
        |namespace Demo
        |
        |trait Plain {
        |}
        """)

    val c2r = CompilerToRuntimeTypes.create(new StdoutLogger(), cua)
    val u = c2r.getUdtDetails("Demo.Plain").getResult
    assert(u.getModelId.get().equals("iso-123:456"))
  }

  test("Annotation across namespace") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Ann
        |annotation Marker ( field ) {
        |}
        |
        |namespace Model
        |
        |trait Plain {
        |   @Ann.Marker
        |   F1 : int
        |}
        """)


    val c2r = CompilerToRuntimeTypes.create(new StdoutLogger(), cua)
    val u = c2r.getUdtDetails("Model.Plain").getResult

    val hasAnn = u.asInstanceOf[UdtBaseNode].getAllFields.get("F1").getAnnotations.get().containsKey("Ann.Marker")

    assert(hasAnn)
  }

  test("Test Meta field ") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Foo
        |
        |# test
        |record L1 {
        |  F1 : $trait
        |}
      """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)

    println(c2rt.getUdtDetails("Foo.L1"))
  }

  test("Test Synthetic field ") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Foo
        |
        |# test
        |record L1 {
        |  F1 : enum < A , B >
        |}
      """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)

    c2rt.getAllUdts

    val v = c2rt.getUdtDetails("Foo.L1")
    println(v)
  }

  test("Test nested hierarchy ") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |# Namespace Foo doc ...
        |namespace Foo
        |
        |# test
        |record L1 {
        |  # ftest
        |  a1 : L2
        |}
        |
        |record L2 {
        |  b2 : L3
        |}
        |
        |record L3 {
        |  c3 : L4
        |}
        |
        |record L4 { }
        |
        |
        |
    """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)

    val l1 = cua.getUdt("Foo.L1").get

    Console.println(l1)

    val a1 = c2rt.getUdtDetails("Foo.L4").getResult
    assert(a1.getReferencedInFieldTypeFrom.get().size() == 1)
    assert(a1.getReferencedInFieldTypeFrom.get().get(0).getFullyQualifiedName.equals("Foo.L3"))
  }


  test("Test hierarchy newBuilder") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |# Namespace Foo doc ...
        |namespace Foo
        |record Level1.Level2.Level3.A {
        |   a1 : Bar.B
        |   a2 : Baz.C
        |}
        |
        |record RecordOnFoo {}
        |
        |# Namespace Level1.Level2 docs
        |namespace Level1.Level2
        |
        |namespace Bar
        |record B {
        |    b1 : Baz.C
        |}
        |
        |namespace Baz
        |record C {}
        |
    """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val h = c2rt.getCompleteHierarchy()


    val nh = h.getNsHierarchy.get("")

    assert(nh.contains("Bar"))
    assert(nh.contains("Foo"))
    assert(nh.contains("Baz"))
    assert(nh.contains("Level1.Level2"))

    assert(h.getNsHierarchy.get("Foo").toString.equals("[Foo.Level1.Level2.Level3]"))

    val ks = h.getNsUdts.keySet()
    assert(ks.contains("Foo"))
    assert(ks.contains("Foo.Level1.Level2.Level3"))
    assert(ks.contains("Bar"))
    assert(ks.contains("Baz"))

    val hierarchyAsJson = Alfa.jsonCodec.toFormattedJson(h)
    println(">>> Complete Hierarchy")
    print(hierarchyAsJson)

    println("\n\n>>> Root Hierarchy")
    val subH = c2rt.getImmediateHierarchy("")
    val rootHierarchyAsJson = Alfa.jsonCodec.toFormattedJson(subH)
    print(rootHierarchyAsJson)

    println("\n\n>>> Foo Hierarchy")
    val fooH = c2rt.getImmediateHierarchy("Foo")
    val fooHierarchyAsJson = Alfa.jsonCodec.toFormattedJson(fooH)
    assert(fooH.getNsUdts.get("Foo").iterator().next().getName.getFullyQualifiedName.equals("Foo.RecordOnFoo"))
    print(fooHierarchyAsJson)

    println("\n\n>>> NS Info")
    val nsd = c2rt.getAllNamespaces().getNamespaces
    val nsAllDetails = c2rt.getAllNamespaceSummaries

    val nsDetails = nsd.get("Level1.Level2")
    val nsAsJson = Alfa.jsonCodec.toFormattedJson(JsonCodecConfig.builder().build(), nsDetails)

    assert(nsDetails.getDoc.get().trim.equals("Namespace Level1.Level2 docs"))
    print(nsAsJson)
  }

  test("Test compile to runtime parse tree") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace comp2rt
        |
        |# Sample record
        |record Sample {
        |   F1 : int(10, 100) ## Number 10 to 100
        |   F2 : int?
        |   F3 : list< string >
        |   F4 : map< int, double? >
        |   F5 : set< Other >
        |   F6 : table< Other >
        |   F7 : encrypted< string >
        |}
        |
        |record Other {
        |   F1 : try< double >
        |   F2 : either< date, string >
        |}
        |
        |service Foo( a : int ) {
        |   bar(b:string) : string
        |}
        |
        |key K {
        |  id : uuid
        |}
        |
        |entity Ent key( id : uuid ) {
        |}
        |
        |entity Ent1 key K {
        |}
        |
        |
    """)
    val udt = cua.getUdt("comp2rt.Sample").get

    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val pt = c2rt.convert(udt).asInstanceOf[UdtBaseNode]
    val json = Alfa.jsonCodec.toFormattedJson(JsonCodecConfig.builder().build(), pt)
    //    println(json)


    val srv = cua.getUdt("comp2rt.Ent1").get
    val isrv = c2rt.convert(srv)
    val jsonSrv = Alfa.jsonCodec.toFormattedJson(JsonCodecConfig.builder().build(), isrv)
    println(jsonSrv)

    assert(pt.getDoc.get().trim.equals("Sample record"))
    assert(pt.getAllFields.get("F1").getDoc.get().trim.equals("Number 10 to 100"))
  }

  test("Test hierarchy for Angular UI") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |# This is the accounting namespace
        |namespace com.acme
        |
        |record accounts.Account {
        | Number : string
        | Holder : string
        |}
        |
        |record accounts.Customer {
        | Name : string
        | Address1 : string
        | Address2 : string
        | PostCode : string
        | Accounts : list< accounts.Account >
        |}
        |
        |record shipping.Shipment {
        |  ShipName : string
        |  Dispatched : date
        |  Expected : date
        |}
        |
    """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val h = c2rt.getCompleteHierarchy()

    val hierarchyAsJson = Alfa.jsonCodec.toFormattedJson(h)
    print(hierarchyAsJson)

  }
  test("Test hierarchy of Traits") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |trait A includes B1, B2 { }
        |
        |trait B1 { }
        |trait B2 includes C1 { }
        |
        |trait C1 includes D { }
        |
        |trait D { }
        |
        |record E includes D { }
        |record F includes D { }
        |
        |record G {
        |    a : D
        |}
        |
    """)
    val udt = cua.getUdt("foo.D").get

    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val pt = c2rt.convert(udt).asInstanceOf[UdtBaseNode]

    val deps = pt.getIncludedFrom
    assert(deps.get.size() == 3)

    val hierarchyAsJson = Alfa.jsonCodec.toFormattedJson(pt)
    print(hierarchyAsJson)
  }

  test("Test expression parse tree") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Foo.Expr
        |
        |entity DataSet key( id : int ) {
        |  F2 : int
        |
        |  assert ThisIsTheAssertName {
        |      let anIntField = 10
        |      let aStringField = "abc"
        |      let aMap = { 1 : "a", 2 : "b", 3 : "c" }
        |      let aList = [ 1, 2, 3 ]
        |      let aSet = { 1, 2, 3 }
        |      let aSrvCall = Srv::foo( 100 )
        |      let total = reduce( aList, 0, (acc, e) => acc + e )
        |      let found = contains( aList, 1 )
        |
        |      let results = query( Foo.Expr.DataSet, e => true )
        |      let queryFiltered = query( Foo.Expr.DataSet, ety => ety.F2 > 10 )
        |  }
        |}
        |
        |service Srv {
        |    foo( someLong : long ) : date
        |}
        |
        |library MyLib {
        |    bar( a : int, b : int ) : int {
        |        let c = a * 2
        |        return c + b
        |    }
        |}
      """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val ent = c2rt.getUdtDetails("Foo.Expr.DataSet").getResult.asInstanceOf[Entity]

    val srv = c2rt.getUdtDetails("Foo.Expr.Srv").getResult.asInstanceOf[Service]

    val lib = c2rt.getUdtDetails("Foo.Expr.MyLib").getResult.asInstanceOf[Library]
  }

  test("Test expression decision tree") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Foo.Expr
        |
        |entity DecisionTreeTest key( id : int ) {
        |  F1 : int
        |  F2 : string
        |
        |  assert DecisionTreeAssert {
        |      let a = ( F1, F2 ) match {
        |          ( 10, "A" ) => 100
        |          ( <= 20, "B" ) => 90
        |          ( != 20, "B" ) => 90
        |          ( [20 .. 100], "B" ) => 90
        |          ( *, * )    => 0
        |      }
        |  }
        |}
        |
      """)
    val c2rt = CompilerToRuntimeTypes.create(logger, cua)
    val ent = c2rt.getUdtDetails("Foo.Expr.DecisionTreeTest").getResult.asInstanceOf[Entity]
  }
}
