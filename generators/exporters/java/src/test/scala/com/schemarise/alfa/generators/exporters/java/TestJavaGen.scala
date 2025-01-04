package com.schemarise.alfa.generators.exporters.java

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Collections

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
import com.schemarise.alfa.generators.common.AlfaExporterParams
import org.scalatest.funsuite.AnyFunSuite

class TestJavaGen extends AnyFunSuite {


//  test("Rosetta") {
//    val cua = TestCompiler.compileScriptOnly(Paths.get("/Users/sadia/IdeaProjects/alfa-core/generators/importers/rosetta/target/generated-test-sources/alfa"))
//
//    println(cua.getErrors.mkString("\n"))
//
//  }

  test("obj assign nested") {
    genJava(
      """
        |namespace demo.foo
        |
        |record A {
        |    i : int
        |}
        |
        |record B {
        |   a : A
        |}
        |
        |library Bar {
        |    f() : void {
        |       var b : B
        |       b.a.i = 10
        |    }
        |}
      """)
  }

  test("Optional chaining ") {

    val s =
      """
        |namespace foo.bar
        |
        |record Bar {
        |    value : int
        |}
        |
        |record Foo {
        |    B : Bar?
        |
        |    assert Checker {
        |        let x = B?.value
        |    }
        |}
        |
        |""".stripMargin

    genJava(s)
  }

  test("nested create") {
    genJava(
      s"""
         |model-id "cdm-1.2321"
         |
         |namespace multilayer
         |
         |trait T {
         |   Ts : int?
         |}
         |
         |record A {
         |    i : int
         |}
         |
         |record B includes T {
         |   a : A
         |}
         |
         |record C {
         |   b : B
         |   ds1 : list< int >
         |   ds2 : set< int >
         |   ds3 : map< int, int >
         |}
         |
         |library L {
         |  makeC() : void {
         |    // let c = new C( b = new B( a = new A( 10 ) ) )
         |
         |    var aa : A
         |    aa.i = 50
         |
         |    var bb : B
         |    bb.a.i = 100
         |    bb.a = aa
         |    bb.Ts = some(100)
         |
         |    var cc : C
         |    cc.b.a.i = 10
         |  }
         |}
         |
         |""".stripMargin
    )
  }

  test("test stream api") {
    genJava(
      """
        |namespace ext.tests
        |
        |service SAPI {
        |  test( data : stream< string > ) : void
        |}
        |
        """)
  }

  test("test extends") {
    genJava(
      """
        |namespace ext.tests
        |
        |trait Foo {
        |   A : string
        |}
        |
        |record Bar includes Foo {
        |   B : int
        |}
        |
        |record Baz extends Bar {
        |   C : int
        |}
        """)
  }

  test("Multi-include for builder test") {
    genJava(
      """
        |namespace cdm.`record`.other
        |
        |trait Foo {
        |    F : string
        |}
        |
        |trait Bar {
        |   B : int
        |}
        |
        |record Baz includes Foo, Bar {
        |    B2 : date
        |}
        |
        |
      """)
  }

  test("With keyword") {
    genJava(
      """
        |namespace cdm.`record`.other
        |
        |record Person {
        |    d : list< Person >
        |    assert X {
        |
        |       let x = toFormattedTable( d )
        |       // let x = toString( d )
        |    }
        |}
        |
        |
      """)
  }

  test("ToStringTable") {
    genJava(
      """
        |namespace Sample
        |
        |record Person {
        |    d : list< Person >
        |    assert X {
        |
        |       let x = toFormattedTable( d )
        |       // let x = toString( d )
        |    }
        |}
        |
        |
      """)
  }

  test("GenericsGen") {
    genJava(
      """
        |namespace xx.yy
        |
        |record Foo<T> {
        |    s : T
        |}
        |
        |service A<T> {
        |    f( a : Foo<T> ) : int
        |}
        |
      """.stripMargin)
  }


  test( "Query expression") {
    genJava(
      """
        |namespace querying
        |
        |entity Trade key( id : string ) {
        |    email : string
        |    price : double
        |}
        |
        |entity QuerySource key( id : string ) {
        |  srcEmail : string
        |
        |  assert ValidOrder {
        |    let orders4 = query( Trade, e => e.email == srcEmail )
        |    let orders1 = query( Trade, e => e.email == srcEmail, s => { s.price : -1 }, 1000 )
        |    let orders2 = query( Trade, e => e.email == srcEmail, s => { s.price : -1 }, 0, "db" )
        |  }
        |}
      """)
  }

  test("Modifier test") {
    genJava(
      """
        |namespace modifier
        |
        |internal entity Trade key( id : string ) {
        |    email : string
        |    price : double
        |}
      """)
  }

  test( "Lookup expression") {
    genJava(
      """
        |entity Order key( id : string ) {
        |}
        |
        |entity Customer key( id : string ) {
        |  ok : OrderKey
        |
        |  assert Adult {
        |    let o1 = lookup(Order, ok, "static-db")
        |    let o2 = lookup(Order, ok)
        |  }
        |}
      """)
  }

  test( "Exists expression") {
    genJava(
      """
        |namespace querying
        |
        |entity Trade key( id : string ) {
        |  email : string
        |
        |  assert ValidOrder {
        |    let ex1 = exists( Trade, e => e.email == email )
        |    let ex2 = keyExists( Trade, new TradeKey( $key.id ) )
        |
        |    let ex1a = exists( Trade, e => e.email == email, "dbbb" )
        |    let ex2a = keyExists( Trade, new TradeKey( $key.id ), "dsfd" )
        |  }
        |
        |  assertAll Unique( rows ) {
        |    let d : list< pair<string, list<Trade> > > = duplicates(rows, r => r.email )
        |  }
        |}
      """)
  }

  test( "Persist expression") {
    genJava(
      """
        |namespace Persist
        |
        |entity Stats key( id : string ) {
        |  type : string
        |}
        |
        |entity Customer key( id : string ) {
        |
        |  assert Timeliness {
        |    let s = new Stats( new StatsKey("2349"), "Delay" )
        |    save(s)
        |    save(s, "mydb")
        |    publish("Q", s)
        |  }
        |}
      """)
  }

  test("2 key fields in keys") {
    genJava(
      """
        |namespace Example
        |
        |key KF {
        |   id : uuid
        |}
        |
        |entity Ord key( K1 : KF, K2 : KF ) {
        |   Name : string
        |}
        |
        |""")

//    val k1 = KF.builder().setId(UUID.randomUUID()).build()
//    val k2 = KF.builder().setId(UUID.randomUUID()).build()
//
//    val k = OrdKey.builder().setK1(k1).setK2(k2).build();
//    val ds = Example.Ord.builder().set$key( k ).setName("abc").build();
//
//    println(ds)
  }


  test("keyed entity") {
    genJava(
      """
        |namespace Example
        |
        |trait T {
        |   Name : string
        |}
        |
        |key K {
        |   Name : string
        |}
        |
        |entity E key K {
        |}
        |
        |
        |""")
  }

  test("Expression cols on ctor") {
    genJava(
      """
        |namespace Example
        |
        |record Order {
        |    A : double
        |    B : long
        |    C : double = B * A
        |    D : string
        |}
        |
        |
        |""")
  }


  test("Enum inheritence") {
    genJava(
      """
        |namespace Example
        |
        |enum Base includes A, B {
        |}
        |
        |enum A {
        |  Y OTHER
        |}
        |
        |enum B {
        |  X OTHER
        |}
        |
        |
        |""")
  }

  test("Test library raise excp") {
    genJava(
      """
        |namespace Example
        |
        |record Foos {
        |   assert Bar {
        |       RaiseExcpLib::f()
        |   }
        |}
        |
        |library RaiseExcpLib {
        |    f() : void {
        |       raise error("Sd")
        |    }
        |}
        |
        |""")
  }

  test("Optional chaining expressions") {
    genJava(
      """namespace Opt
        |
        |record Chain1 {
        |   c1 : Chain2?
        |   d1 : Chain2
        |   e1 : Chain2?
        |
        |   assert Accessor {
        |       let x = c1?.c2?.c3
        |       let y = d1?.d2?.d3
        |       let z = e1?.e2?.e3
        |   }
        |}
        |
        |record Chain2 {
        |   c2 : Chain3?
        |   d2 : Chain3?
        |   e2 : Chain3
        |}
        |
        |record Chain3 {
        |   c3 : int
        |   d3 : int?
        |   e3 : int
        |}
        |
      """.stripMargin)
  }

  test("Documented expressions") {
    genJava(
      """
        |# This is a library that does stuff
        |library Foo.BarExprDoc {
        |    # This method will do stuff
        |    baz() : int {
        |       # Assign x to 10
        |       let x = 10

        |       # Print a debug message
        |       debug("debug stmt")

        |       # When x is 10 do stuff
        |       if ( x == 10 )
        |           # This when true
        |           debug( "x is 10")
        |       else
        |           # This when false
        |           debug( "x is not 10")
        |
        |       # return the result
        |       return 10
        |    }
        |}
      """.stripMargin)
  }

  test("Tuple annotation") {
    genJava(
      """
        |@alfa.rt.SkipUnknownFields
        |@alfa.db.Table(Name="Foo",
        |               ClusterFields={name},
        |               Options={
        |               "friendly_name='hello'"
        |               }
        |               )
        |record Foo.Bar1 {
        |    name : string
        |
        |    t : @alfa.rt.SkipUnknownFields tuple< a : string >
        |}
      """.stripMargin)
  }
//
//  test("QUICK TEST") {
//
//     testGenFromPath(Paths.get("/Users/sadia/dev/alfa-suade-fire-demo/model"))
//  }


  test("Test Tuple max len") {
    genJava(
      """
        |namespace Example
        |
        |record TupleStrLen {
        |    payload : tuple< lastMod : datetime?, level1 : string(11,*), traders : list< tuple < Name : string(21,*) > > >?
        |}
        |
        |""")
  }

  test("Test Tuple access 2") {
    genJava(
      """
        |namespace Example
        |
        |record TupleAccess {
        |    payload : tuple< lastMod : datetime?, traderName : string? >?
        |
        |    assert A {
        |        let x = get(get(payload).traderName)
        |    }
        |}
        |
        |""")
  }


  test("Test library nested") {
    genJava(
      """
        |namespace Example
        |
        |library MultiCondLib {
        |    f() : int {
        |        if ( true )
        |            debug("test1")
        |        else if ( true )
        |            debug("test2")
        |        else
        |            debug("test3")
        |
        |        return 0
        |    }
        |}
        |
        |""")
  }

  test("Test Tuple access") {
    genJava(
      """
        |namespace Example
        |
        |record TupleAccess {
        |    P : tuple< Name : string, Age : int?, Address : tuple< Line1 : string, Line2 : string >  >
        |    O : tuple< Name : string? >?
        |
        |    assert A {
        |        let x = P.Name
        |        let y = P.Address.Line1
        |        let z = len(get(get(O).Name))
        |
        |        if ( get(P.Age) > 10 )
        |            debug("tst")
        |    }
        |}
        |
        |""")
  }

  test("Test Pair And Either") {
    genJava(
      """
        |namespace Example
        |
        |record EitherPair {
        |    P : pair< int, string >
        |    E : either< int, string >
        |
        |    assert A {
        |        //let x = left(P)
        |        let y = left(E)
        |    }
        |}
        |
        |""")
  }

  test("Test DQ Gen") {
    genJava(
      """
        |namespace Example
        |
        |record DQTest {
        |    intVal : int(10, 100)
        |    // MappablePair : pair< string, int >
        |}
        |
        |""")
  }

  test("Key get in assert") {
    genJava(
      """
        |namespace Sample
        |
        |
        |entity RefData key( id : string(3,3) ) {
        |    Description : string(20,20)
        |}
        |
        |entity BasicSample key(cusId:string) {
        |    StrFld : string(3,3)
        |    IntFld : int(10, 100)
        |    DateFld : date(*,*,"yyyy")
        |    DatetimeFld : datetime(*,*,"yyyy")
        |
        |    assert Three {
        |        let x = lookup( RefData, new RefDataKey(StrFld) )
        |        let y = get(x)
        |        let z = y.$key.id + "..." + y.Description
        |
        |        debug(z)
        |    }
        |}
      """.stripMargin
    )
  }

  test("Test lookup") {
    genJava(
      """
        |namespace Example
        |
        |entity Other key ( id : int ) {
        |  otherId : string
        |}
        |
        |entity LT key ( id : int ) {
        |  name : string
        |
        |  assert Val {
        |    let l = lookup( Other, new OtherKey(23) )
        |
        |    let x =  get(l).otherId
        |  }
        |}
        |
        |""")
  }

  test("Test or expr printer") {
    genJava(
      """
        |namespace Example
        |
        |library LibFilter {
        |
        |    fn() : void {
        |      let l = [1, 2, 3, 4, 5]
        |
        |      let f = filter( l, e => e > 1 || e < 10 )
        |    }
        |}
        |
        |""")
  }

  test("Operators comparison with precedence") {
    genJava(
      """
        |namespace Example
        |
        |library LibFilter {
        |
        |    fn() : void {
        |      let d : double = 21.0
        |      let i : int = 20
        |
        |      if ( i < d )
        |          debug("hello")
        |
        |      if ( d < i )
        |          debug("hello")
        |    }
        |}
        |
        |""")
  }


  test("Test Enum keyword") {
    genJava(
      """
        |namespace Example
        |
        |
        |record RecWIthInlineEnum {
        |    EnField : enum< `long`, `short` >
        |}
        |
        |""")
  }

  test("Test Enum Lex") {
    genJava(
      """
        |namespace Example
        |
        |enum EnumWithLexValue {
        |    `Raw Material`, Food, `N/A`
        |}
        |
        |record RecWIthInlineEnum {
        |    EnField : enum< `Raw Material`, Food, `N/A` >
        |}
        |
        |""")
  }

  test("Test AssertAll") {
    genJava(
      """
        |namespace Example
        |
        |record AssertTest {
        |   Name : string(0,402)
        |
        |   assertAll AT1(rows) {
        |   }
        |
        |   assertAll AT2(rows) {
        |   }
        |}
        |
        |""")
  }

  test("To Enum function") {
    genJava(
      """
        |namespace Example
        |
        |enum Colour { R G B }
        |
        |library AggregateLib {
        |    f() : void {
        |      let li : list<int> = [1, 2, 3, 6]
        |      let sdi = stddev(li)
        |      let vi = variance(li)
        |
        |      let ld : list<double> = [234.23, 345.12]
        |      let sd = stddev(ld)
        |      let vd = variance(ld)
        |
        |      let q1 = quartile(li, 1)
        |    }
        |}
        |
        |""")
  }


  test("Versioned UDT") {
    genJava(
      """
        |namespace VerExample
        |
        |record Account {
        |   A : int
        |   B : string
        |}
        |
        |record Account @ 1 {
        |   A : int
        |}
        |
        |transform( in : Account @ 1) : Account {
        |    let x = in
        |    let a = new Account(B = "test") with in
        |    return a
        |}
        |
        |library Lib {
        |    fn( a : Account @ 1 ) : int {
        |        return 10
        |    }
        |}
        |
        |
        |""")
  }

  test("Service versions") {
    genJava(
      """
        |namespace Example
        |
        |service Calculator {
        |    latest(a:string) : int
        |}
        |
        |service Calculator @ 1 {
        |    old(a:string) : int
        |}
        |
        |""")
  }

  test("Union field access") {
    var d = """
               namespace FuncsTest
              |record TestUnionAccess {
              |  d : TestUnion
              |
              |  assert Valid {
              |    let z = isSet( d.A )
              |    let v = d.A
              |  }
              |}
              |union TestUnion {
              |  A : int
              |  B : string
              |  C : date
              |}
              |
            """

    genJava(d)
  }

  test("Debug") {
    var d = """
              |testcase FuncsTest.DebugTestSuite {
              |    testPartial( srv : schemarise.alfa.test.Scenario ) : void {
              |       debug(toString([1,2,3,4,5,6,7]))
              |       debug(toString(3920))
              |       //debug("3920")
              |       //debug({ "3920" } )
              |    }
              |}
            """

    genJava(d)
  }

  test("Map getOrElse") {
    var d = """
              |library FuncsTest.MapGetOrElse {
              |    getTest() : void {
              |       let m : map<int, int> = { 1 : 2, 4 : 6, 7 : 4 }
              |       let x = getOrElse(m, 3, 99)
              |    }
              |}
            """

    genJava(d)
  }


  test("Matches") {
    var d = """
      |record FuncsTest.TestMatches {
      |
      |  assert Valid {
      |    let n = "3920"
      |    let matched = matches("\\b[0-9]{0,3}\\b", n)
      |     if ( ! matched ) raise error("err")
      |  }
      |}
    """

    genJava(d)
  }

  test("To String ") {
    genJava(
      """
        |typedefs {
        |   storage = native acme.types.Storage
        |}
        |
        |namespace com.test
        |
        |enum Col {
        |   N("1"), K("2"), I("3")
        |}
        |
        |record PC {
        |    ST : storage
        |    C : Col
        |
        |    assert SType {
        |        let x = ST == "SDD"
        |        let xx : storage = "20"
        |        let s = toString(ST)
        |        let e = toString(C)
        |        let w = toPeriod("P1Y")
        |    }
        |}
        |
    """.stripMargin
    )
  }

  test("Java period types") {
    genJava(
      """
        |namespace Example
        |
        |record scalars.JavaPeriod {
        |    fPeriod : period
        |    fPeriodWithDefault : period = "P5W"
        |}
        |
        |record scalars.constraints.range.JavaPeriod {
        |	   fDuration : period( "P1Y", "P6Y" )
        |}
        |
        |""")
  }

  test("transform java gen") {
    genJava(
      """
        |namespace com.acme.transform
        |
        |record Input {
        |   Name : string
        |}
        |
        |record Static {
        |   Name : string
        |}
        |
        |record Output1 {
        |   Name : string
        |}
        |
        |record Output2 {
        |   Name : string
        |}
        |
        |transform( i : Input ) : Output1
        |{
        |    return new Output1(i.Name)
        |}
        |
        |transform( i : Input, s : Static ) : Output2
        |{
        |    return new Output2(i.Name)
        |}
        |
        |transform( i : Output1 ) : Output2
        |{
        |    return new Output2(i.Name)
        |}
        |
        |library Foo {
        |    test() : void {
        |        let i = new Input("A")
        |        let o : Output1 = transform(i)
        |    }
        |}
      """.stripMargin)
  }

  test("Java transformer nested") {
    genJava(
      """
        |namespace com.acme.trnexted
        |
        |record A1 {
        |   Name : string
        |}
        |
        |record B1 {
        |   a1 : A1
        |}
        |
        |record A2 {
        |    Name : list< string >
        |}
        |
        |record B2 {
        |    a2 : A2
        |}
        |
        |transform( i : A1 ) : A2
        |{
        |    return new A2( [ i.Name ] )
        |}
        |
        |transform( i : B1 ) : B2
        |{
        |    return new B2( transform<A2>(i.a1) )
        |}
        |
      """)
  }

  test("Java TestCaseExample") {
    genJava(
      """
        |namespace Example
        |
        |record TestCaseExample {
        |    F1 : string
        |    F2 : int
        |}
        |
        |testcase TestCaseExampleTest {
        |    testPartial( srv : schemarise.alfa.test.Scenario ) : void {
        |       let a = partial TestCaseExample( F1 = "abc" )
        |    }
        |
        |    testVal( srv : schemarise.alfa.test.Scenario ) : void {
        |       // let a : TestCaseExample = srv::loadObjectFromCsv( Example.TestCaseExample, "src/test/data/SinglePosition.csv")
        |       srv::fails("expect to fail ", => 10 / 0 )
        |    }
        |}
        |
        |""")

    //    val d = Example.IntOrShort.builder().setF(LocalDate.now()).build()

    //    println(JsonCodec.toFormattedJson(d))


  }

  test("Java int or short") {
    genJava(
      """
        |namespace Example
        |
        |record IntOrShort {
        |
        |    F1 : string
        |
        |    assert Fd {
        |        let a = [ 1, 2, 3 ]
        |
        |        let z = newTryValue(10)
        |        let zz = get(z)
        |        let zzz : try< int > = newTryFailure( "aaa" )
        |
        |        let b = get(a, 1)
        |        let c = filter( a, e => e > 2 )
        |        // let c = contains( a, 2 )
        |        // let dd = some( 20 )
        |
        |        //let ds = reduce( a, 0, ( acc, e ) => acc + e )
        |        //let ma : list< string > = map( a, e => toString(e) )
        |
        |        let xa = { 1 : "A", 2 : "B", 3 : "C" }
        |        let mapfilter = filter( xa, (k, v) => k > 1 )
        |        let bz = get(xa, 1)
        |        let ksum = reduce( xa, 0, (acc, k, v ) => acc + k )
        |        let kashifted : map< int, string > = map( xa, ( k, v ) => k+1, ( k, v ) => v + "AAA" )
        |
        |        let s = min(3, 4)
        |    }
        |}
        |
        |testcase IntOrShortTest {
        |    testVal( srv : schemarise.alfa.test.Scenario ) : void {
        |        let r : IntOrShort = srv::random(Example.IntOrShort)
        |    }
        |}
        |
        |""")

    //    val d = Example.IntOrShort.builder().setF(LocalDate.now()).build()

    //    println(JsonCodec.toFormattedJson(d))


  }


  test("Java meta types") {
    genJava(
      """
        |namespace Example
        |
        |record WithMetaReference {
        |    T : $record
        |}
        |""")
  }

  test("Java trait bind test") {
    genJava(
      """
        |namespace Example
        |
        |trait BindAnnotation {
        |    F : decimal(12,8, 0, 1)
        |}
        |""")
  }

  test("Java fields with all literals") {
    genJava(
      """
        |namespace Example
        |
        |record FieldWithAllLiterals {
        |    C : int = ( A + B ) / 2
        |    B : int = 20
        |    A : int = 10
        |}
        |""")
  }

  test("Conflicting field names") {
    genJava(
      """
        |namespace Example
        |
        |enum RequestType {
        |  New Cancel Other("OTH")
        |}
        |
        |record Request {
        |  RequestType : RequestType = RequestType.New
        |}
      """.stripMargin)
  }

  test("Constrained types and function args") {
    genJava(
      """
        |namespace Example
        |
        |library Request {
        |   test() : void {
        |       let x : decimal(20,6) = 3032.124
        |
        |       let s = toString( x )
        |   }
        |}
      """.stripMargin)
  }

  test("Java fields enum literal value") {
    genJava(
      """
        |namespace Example
        |
        |enum Direction { N S E W }
        |
        |record FieldWithEnumLiteral {
        |    C : Direction = Direction.S
        |}
        |""")
  }

  test("Java fields vector literal") {
    genJava(
      """
        |namespace Example
        |
        |record FieldWithVectorLiteral {
        |    C : list<int> = [ 1, 2, 3 ]
        |}
        |""")
  }

  //  test( "Java fields with literals" ) {
  //    genJava(
  //      """
  //        |namespace Example
  //        |
  //        |record FieldWithLiterals {
  //        |    C : int = ( A + B ) / 2.0
  //        |    B : int
  //        |    A : int
  //        |}
  //        |""")
  //  }
  //
  //
  test("Java fields with function result") {
    genJava(
      """
        |namespace Example
        |
        |record FieldWithFuncResult {
        |    C : double = Calculator::calcMid( Bid, Mid )
        |    Bid : double
        |    Mid : double
        |}
        |
        |service Calculator {
        |    @alfa.rt.http.Get
        |    calcMid( l : double, r : double ) : double
        |
        |    @alfa.rt.http.Get
        |    calcFibo( upto : int ) : list< int >
        |}
        |
        |""")
  }

  test("Java ListOfCompressedString") {
    genJava(
      """
        |namespace Example
        |
        |record ListOfCompressedString {
        |    A : compressed< string >
        |    C : list< compressed< string > >
        |}
        |""")
  }

  test("Java Assert Expr") {
    genJava(
      s"""
         |namespace Example
         |
         |record Person {
         |    Name : string
         |    Age : int(10,100)
         |
         |    assert CheckIfIsAdult {
         |       let AgeInMonths : int = Age * ( 12 + Age )
         |       if ( Age < 18 ) raise error("'$${Name}' not an adult")
         |    }
         |}
         |""")
  }

  test("Java Assert List Exprs") {
    genJava(
      s"""
         |namespace Example
         |
         |record Data {
         |    L1 : list< int >
         |
         |    assert List {
         |       let t : string = toString( 10 )
         |
         |       let a : int = len( L1 )
         |       let b : boolean = isEmpty( L1 )
         |       let c : int = reduce( L1, 0, ( acc, ex ) => {
         |         let i = acc + 2
         |         return i + ex
         |       } )
         |
         |       let d : int = reduce( L1, 0, ( acc, ex ) => acc + ex )
         |       let e : list< string > = map( L1, ( ex ) => toString( ex ) )
         |       let f : list< int > = filter( L1, ( ex ) => ex > 100  )
         |    }
         |}
         |""")
  }

  test("Java Entity external key") {
    genJava(
      """
        |namespace udts
        |
        |key ExtKey {
        |    F1 : int
        |    F2 : string
        |}
        |
        |entity MyEntity2 key ExtKey {
        |    F3 : int
        |    F4 : string?
        |}
        |""")
  }

  test("Java Entity key") {
    genJava(
      """
        |
        |entity udts.MyEntity1 key ( Id : uuid ) {
        |    F1 : int
        |    F2 : string
        |}
        |""")
  }


  test("Java ALFA Key") {
    genJava(
      """
        |
        |key udts.MyKey {
        |    F1 : int
        |    F2 : string
        |}
        |""")
  }

  test("Java validate method constraits") {
    genJava(
      """
        |namespace Example
        |
        |trait Foo<T> {
        |   A : int
        |   B : T
        |}
        |
        |record Bar includes Foo<int> {
        |    C : date
        |}
        |record Baz includes Foo<string> {
        |    D : short
        |}
        |""")
  }

  test("Codegen Union") {
    genJava(
      """
        |namespace Demo
        |
        |entity E key ( id : uuid ) { F : int }
        |
        |record R {
        |  ValOfE : E
        |}
        |
        |record ValTest {
        |  A : int
        |  BOpt : int?
        |  C : string
        |}
        |
    """.stripMargin)
  }

  test("Java8Gen Entity ") {
    genJava(
      s"""
         |namespace Feature
         |
         |entity Sample key ( id : uuid ) {
         |  name : string
         |}
         |
         |record OtherSample {
         |}
         |
       """.stripMargin)

  }

  test("Java8Gen test generic") {
    genJava(
      s"""
         |namespace Feature.Generics
         |
         |key SomeKey< T, R > {
         |    F1 : T
         |    F2 : R
         |}
         |
         |entity SomeKeyFromEntity key SomeKey< string, date > {
         |}
         |
         |
         |//trait Templated< T > {
         |//    Value : T
         |//}
         |//
         |//record A includes Templated< int > {}
         |// record B includes Templated< string > {}
         |//
         |// record TmplImpl2 includes SeqOfTemplates< TmplImpl1 > {}
         |
       """.stripMargin)

  }

  test("Java8Gen Untagged Union") {
    genJava(
      """
        |namespace Example
        |
        |record SampleRecWithUntaggedUnionField {
        |   F : SampleUntaggedUnion
        |}
        |
        |union SampleUntaggedUnion = int | string | date
      """)
  }


  test("Java8Gen Untagged Union Datatype") {
    genJava(
      """
        |namespace Example
        |
        |record SampleRecWithUntaggedUnionDataType {
        |   F : union< int | string | date >
        |}
        |
      """)
  }

  test("Java8Gen Union") {
    genJava(
      """
        |namespace Example
        |
        |union SampleUnion {
        |  F1 : int
        |  F2 : set< string >
        |  F3 : map< int, string >
        |  F4 : list< date >(1,100)
        |}
      """)
  }

  test("Java8Gen key and enum") {
    genJava(
      """
        |namespace Example
        |
        |enum SampleEnum {
        |  Red Blue Green
        |}
        |
        |key ScalarsDataKey {
        |   A : string
        |   B : SampleEnum
        |   C : long
        |}
      """)
  }

  test("Java8Gen record and vectors") {
    genJava(
      """
        |namespace Example
        |
        |record VectorsData {
        |   A : map< string, long >
        |   B : set< date >(*,10)
        |   C : list< date >(*,100)
        |   D : map< int, list< short? > >
        |   E : map< int, list< short? > >
        |}
      """)
  }

  test("Java8Gen trait and implements") {
    genJava(
      """
        |namespace Example
        |
        |trait AnInterface2 {
        |   A : int
        |}
        |record ObjImplementingIntf includes AnInterface2 {
        |   B : int
        |}
      """)
  }


  test("Java8Gen trait only") {
    genJava(
      """
        |namespace Example
        |
        |trait AnInterface {
        |   A : int
        |}
      """)
  }

  test("Java8Gen record") {
    genJava(
      """
        |namespace Example
        |
        |union Address {
        |   A : int
        |   B : string
        |   PC : PostCode
        |}
        |
        |record PostCode {
        |   Code : string
        |}
      """)
  }

  test("Java8Gen entity") {
    genJava(
      """
        |namespace Example
        |
        |entity KeyedEntity key ( name : string ) {
        |   A : int
        |   B : string
        |}
      """)
  }


  test("Generic big decimal ") {
    genJava(
      """
        |record Rec  {
        |    Result : decimal(20, 12, 0.0, 101000.0)
        |}
      """.stripMargin)
  }

  test("Generic union ") {
    genJava(
      """
        |union Try<T> {
        |    Result : T
        |}
      """.stripMargin)
  }

  test("Java8Gen keyless entity") {
    genJava(
      """
        |namespace Example
        |
        |entity IndexSingleton {
        |}
      """)
  }


  test("db query") {

    genJava(
      """
         |namespace querying
         |
         |entity Trade key( id : string ) {
         |    email : string
         |    Price : double
         |}
         |
         |entity QuerySource key( id : string ) {
         |  srcEmail : string
         |
         |  assert ValidOrder {
         |    let orders1 = query( Trade, e => e.email == srcEmail )
         |    let orders2 = query( Trade, e => e.email == srcEmail, s => { s.Price : 1 }, 0 )
         | //   let total = len( orders )
         |  }
         |}
      """.stripMargin)
  }


  test("db fns") {
    genJava(
      """
        |namespace querying
        |
        |entity Trade key( id : string ) {
        |  email : string
        |
        |  assert ValidOrder {
        |//    let ex1 = exists( Trade, e => e.email == email )
        |//    let ex2 = keyExists( Trade, new TradeKey( $key.id ) )
        |  }
        |
        |    assertAll ValidOrders1(rows) {
        |      let unique : list< string > = distinct(rows, r => r.email)
        |    }
        |
        |    assertAll ValidOrders2(rows) {
        |      let dups : list< pair<string, list<Trade> > > = duplicates(rows, r => r.email)
        |  }
        |
        |}
      """.stripMargin)
  }

  test("aggregatefn") {
    genJava(
      """
        |namespace Aggr
        |
        |entity Trade key( id : string ) {
        |  trader : string
        |  value : double
        |
        |  assertAll ValidOrders(rows) {
        |    let dups : list<pair<string,double>> = aggregate(rows, r => r.trader, 0.0, (acc, e) => acc + e.value )
        |  }
        |
        |}
      """.stripMargin)
  }



  test("Java8Gen service") {
    genJava(
      """
        |namespace Example
        |
        |service Query() {
        |  send( num : int, name : string ) : list< string >
        |  save( num : int, name : string ) : try< int >
        |}
      """)
  }

  test("Java8Gen service complex types") {
    genJava(
      """
        |namespace Example
        |
        |service LoginService( publicKey : string ) {
        |  login( name : string, password : encrypted< string > ) : future< string >
        |  save( name : string, essay : compressed< string > ) : try< int >
        |}
      """)
  }


  //  test( "Java8Gen int range" ) {
  //    genJava(
  //      """
  //        |namespace Example
  //        |
  //        |record Range {
  //        |  // F1 : uri("ftp","sftp")
  //        |  F2 : string("dflk")
  //        |}
  //      """)
  //  }

  test("Java8Gen valtest range") {
    genJava(
      """
        |namespace Example
        |
        |record Range {
        |//  F1 : int(10, 11 )
        |//  F1 : uri("ftp","sftp")
        |   P1 : int(10, 1000)
        |
        |   L1 : list<string>(5,10)
        |   L2 : list<string(5,10)>
        |   L3 : list< map< int, long(10,100) > >
        |
        |   M1 : map< string,int >(5,10)
        |   M2 : map< string(5,10),int >
        |   M3 : map< int, string(5,10) >
        |
        |   S1 : set<string>(5,10)
        |   S2 : set<string(5,10)>
        |}
      """)
  }


  test("Java8Gen descriptor val range") {
    genJava(
      """
        |namespace Example
        |
        |record Val {
        |  matrixField : list< list< int(88,300) >(3,3) >(3,3)
        |}
      """)
  }

  test("Java8Gen test native") {
    genJava(
      """
        |typedefs {
        |    storage = native acme.types.Storage
        |}
        |
        |record store.Database {
        |    Name : string
        |    Capacity : storage
        |}
        |
      """)
  }

  test("Java8Gen Javadoc test") {
    genJava(
      """
        |namespace Example
        |
        |# Book services here
        |service BookingSrv(
        |     a : int ## ctor arg
        |) {
        |    # Run this to calculate prices
        |    price(
        |        # single line comment
        |        arg1 : string
        |    ) : int
        |}
        |
        |# Enum of Salutations
        |enum Salutations
        |{
        |   # Aaaaa
        |   Mr
        |   # Bbbbb
        |   Mrs
        |}
        |
        |# Record comment
        |record Val {
        |  # Field F1 comment
        |  F1 : int
        |}
        |
      """)
  }

  test("Gen fragment from files") {
    val fs = VFS.create()
    VFS.mkdir(fs, "src")

    VFS.write(fs.getPath("src", "A.alfa"),
      s"""
         |fragment trait Example.Fragment.TypeA {
         |   F1 : int
         |   F2 : string
         |}
        """.stripMargin)

    VFS.write(fs.getPath("src", "A1.alfa"),
      s"""
         |trait Example.Fragment.TypeA {
         |   F3 : date
         |}
        """.stripMargin)

    testGenFromPath(fs.getPath("src"))
  }

  //  test("genFromScript" ) {
  //    val cua = TestCompiler.compileScriptOnly(
  //      Paths.get("/Users/sadia/IdeaProjects/alfa-applibs-fpml/fpml-5.11/projects/confirmation/target/generated-sources/alfa"))
  //  }

  def testGenFromPath(p: Path) {
    val cua = TestCompiler.compileScriptOnly(p)

    if (cua.hasErrors) {
      println(cua.getErrors.mkString("\n"))
      throw new RuntimeException("Errors encountered")
    }
    val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
    //val targetDir = VFS.create().getPath("target")

    val javaGen = Paths.get(targetDir + "generated-test-sources/java")

    val j = new JavaExporter( AlfaExporterParams( new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
    j.exportSchema()
  }

  private def genJava(script: String) = {
    val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
    //val targetDir = VFS.create().getPath("target")

    val javaGen = Paths.get(targetDir + "generated-test-sources/java")

    VFS.mkdir(javaGen)

    val cua = TestCompiler.compileValidScript(script)

    val j = new JavaExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
    j.exportSchema()

    val jt = new JavaTestExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
    jt.exportSchema()

    val rt = new JavaRestEndpointExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
    rt.exportSchema()
  }

  test("enum lookups and values" ) {
    val s =
      """
        |namespace Model
        |
        |record commoditiesGeneral {
        |    baseProduct : BaseProductType
        |    subProduct : SubProductType
        |    furtherSubProduct : FurtherSubProductType
        |}
        |
        |fragment record commoditiesGeneral {
        |
        |    # Validate the 3 fields contain values which are contextually valid
        |    assert CheckClassificationHierarchy {
        |
        |        // Build a hierarchy of baseProduct -> subProduct -> furtherSubProduct based on the enum naming
        |
        |        let hierarchy : map< string, map< string, set< string  > > > =
        |                toMap( toList(enumValues( Model.BaseProductType )),
        |                       k1 => k1,
        |                       k1 => toMap< map< string, set< string  > > >( toList(enumValues( "Model." + k1 + "_SubProductType", ( list<string> ) [ ] )),
        |                                    k2 => k2,
        |                                    k2 => enumValues( "Model." + k1 + "_" + k2 + "_FurtherSubProductType", ( list<string> ) [ ] )
        |                                  )
        |                     )
        |
        |         // Check if the sub-product is defined under the base product
        |
        |         let subProdsMap = get ( get( hierarchy, toString( baseProduct ) ) )
        |         if ( ! contains( keys( subProdsMap ), toString(subProduct) ) )
        |             raise warning("...")
        |
        |         // Check if the sub-product is defined under the further-sub-product
        |
        |         let furtherSubProdsSet = get( get( subProdsMap, toString( furtherSubProduct ) ) )
        |         if ( ! contains( furtherSubProdsSet, toString(furtherSubProduct) ) )
        |             raise warning("...")
        |    }
        |}
        |
        |enum BaseProductType {
        |    METL
        |}
        |
        |enum SubProductType includes METL_SubProductType { }
        |
        |enum FurtherSubProductType includes METL_FurtherSubProductType { }
        |
        |enum METL_SubProductType {
        |    NPRM, PRME
        |}
        |
        |enum METL_FurtherSubProductType includes METL_NPRM_FurtherSubProductType, METL_PRME_FurtherSubProductType {
        |
        |}
        |
        |enum METL_NPRM_FurtherSubProductType {
        |    ALUM ## Aluminium
        |    ALUA ## Aluminium Alloy
        |    CBLT ## Cobalt
        |    COPR ## Copper
        |    IRON ## Iron ore
        |    LEAD ## Lead
        |    MOLY ## Molybdenum
        |    NASC ## NASAAC
        |    NICK ## Nickel
        |    STEL ## Steel
        |    TINN ## Tin
        |    ZINC ## Zinc
        |    OTHR ## Other
        |}
        |
        |# Metal precious product types
        |enum METL_PRME_FurtherSubProductType {
        |    GOLD ## Gold
        |    SLVR ## Silver
        |    PTNM ## Platinum
        |    PLDM ## Palladium
        |    OTHR ## Other
        |}
        |
      """.stripMargin

    genJava(s)
  }

  test("TestDatautilsGen test 1") {
    genJava(
      """
        |namespace Example
        |
        |record Range {
        |   P1 : int
        |
        |   L1 : list<string>
        |   L2 : list<string>
        |   L3 : list< map< int, long > >
        |
        |   M1 : map< string,int >
        |   M2 : map< string,int >
        |   M3 : map< int, string >
        |
        |   S1 : set<string>
        |   S2 : set<string>
        |}
      """)
  }

  test("TestDatautilsGen test 2") {
    genJava(
      """
        |namespace Example
        |
        |entity Ran key( Id : uuid ) {
        |  Name : string
        |  Dob : date
        |}
      """)
  }

//  test("Gen huge script") {
//    val sb = new StringBuilder()
//
//    sb.append("record com.acme.Sample {\n")
//    for (a <- 1 to 252) {
//      sb.append(
//        s"""
//           |        F$a :int
//           |        """.stripMargin);
//    }
//
//    sb.append(
//      """    assert Foo {
//        |        let d = 10
//        |    }
//        |""".stripMargin)
//    sb.append("}")
//
//    genJava(sb.toString())
//  }
}
