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
package com.schemarise.alfa.compiler.test.lang.feature

import com.schemarise.alfa.compiler.TraverseUtils
import com.schemarise.alfa.compiler.ast.model.IField
import com.schemarise.alfa.compiler.ast.nodes.Union
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class LiteralAndValueExpressionsTest extends AnyFunSuite {

  test("MAth expr test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Example
        |
        |library A {
        |    f() : void {
        |        let x = ( 2 + 3 ) / 2
        |    }
        |}
        |
      """)
  }

  test("Implict get") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Example
        |
        |record Data {
        |    Yob : int?
        |
        |    assert Valid {
        |        let x =
        |        if ( isSome( Yob ) )
        |            Yob + 10
        |        else
        |            0
        |
        |    }
        |}
        |
        |library A {
        |    f() : void {
        |        let x : int? = some(10)
        |        let y : int = 4
        |        let z1 = x + y
        |        let z2 = x > y
        |        let z3 = x == y
        |    }
        |}
        |
      """)
  }

  test("Expression literals") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |   ShortField : int = 1000
        |   IntField : int = 1000000
        |   LongField : long = 10000000000000
        |   DoubleField1 : double = NaN
        |   DoubleField2 : double = Infinity
        |   DoubleField3 : double = -Infinity
        |   DoubleField4 : double = 2900.4034
        |   BooleanField : boolean = true
        |   // CharField : char = 'F'
        |   DateField : date = toDate("2018-10-14")
        |}
      """)
  }

  def exp(f: Option[IField]) = f.get.expression.get

  test("Expression sequence") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |   IntSeqField : list< int > = [ 1, 2, 3 ]
        |
        |  //  NestedSeqField : list< list < int > > = [ [ 0, 1, 2 ], [ 3, 4, 5 ], [ 6, 7, 8 ] ]
        |}
      """)

    val fields = cua.getUdt("Data").get.allFields
  }

  test("Expression tuple") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |   TupleField : tuple< Name : string, Age : int > = ( "Bob", 30 )
        |}
      """)
  }

  test("Expression map") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |   F : map< Name : string, Age : int > = {"John":30, "Paul":24, "Ringo":33, "George":22}
        |}
      """)
  }

  test("Expression object") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Person {
        |   Name : string
        |   Age : int
        |}
        |
        |record Data {
        |   F : Person = new Person ( Name="Joe", Age=21 )
        |}
      """)
  }

  test("Expression enum resolve") {
    TestCompiler.compileValidScript(
      """
        |enum Level {
        |  High Medium Low
        |}
        |
        |record Data {
        |   Gauge : Level = Level.Low
        |}
      """)
  }

  test("Expression empty value with braces ") {
    TestCompiler.compileValidScript(
      """
        |record Person {
        |}
        |
        |record Data {
        |   F1 : set< int > = {}
        |   F2 : map< string, date > = {}
        |   F3 : Person = new Person( )
        |}
      """)
  }

  test("Expression with scalar defaults") {
    val script =
      """
        |typedefs {
        |   email = string("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        |}
        |
        |union AllSupportedScalars {
        | 	fDecimal : decimal = "189076.987"
        | 	fFixedLengthDecimal : decimal(12,8)
        | 	fDate : date = "2021-10-20"
        | 	fDatetime : datetime = "2017-12-03T10:15:30"
        | 	fTime : time = "10:15:30"
        |   fDuration : duration = "PT10H30M"
        |   fPeriod : period = "P1Y"
        | 	fUuid : uuid = "123e4567-e89b-12d3-a456-426655440000"
        |   // fUrl1 : uri = "http://www.acme.com"
        |   // fUrl2 : uri("http","https") = "https://www.acme.com"
        |   fPattern : email = "joe@acme.com"
        |   fBinary : binary = "f263575e7b00a977a8e9a37e08b9c215feb9bfb2f992b2b8f11e"
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script)

    assert(TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered.get).size == 1)
    assert(!cua.hasErrors && !cua.hasWarnings)

    val u = cua.getUdt("AllSupportedScalars").get
    assert(u.isInstanceOf[Union])
  }

  test("Expression for UDT key") {
    val cua = TestCompiler.compileValidScript(
      """
        |key Id {
        |  UTR : string
        |}
        |
        |record Design {
        |   Key : Id = new Id( UTR = "lkj34w34lkj" )
        |}
        |
      """.stripMargin)
  }

  test("Expression for UDT entity") {
    TestCompiler.compileValidScript(
      """
        |entity Person key ( Id : string ) {
        |  Name : string
        |  Age : int?
        |}
        |
        |record Property {
        |   Resident : Person = new Person ( $key = new PersonKey( Id = "identifier123" ), Name = "Bob" )
        |}
        |
      """.stripMargin)
  }


  test("Dollar key access") {
    TestCompiler.compileValidScript(
      """
        |namespace Test
        |
        |entity Person key ( Id : string ) {
        |  Name : string?
        |  Age : int?
        |
        |
        |  assert Dollar {
        |      let x = $key.Id
        |      let p = new Person( new PersonKey("t") )
        |      let y = p.$key.Id
        |  }
        |}
        |
        |
      """.stripMargin)
  }

  test("AST for expression - value not required if defaulted") {
    TestCompiler.compileValidScript(
      """
        |record Other {
        | X : int = 2
        | Y : int = 4
        |}
        |
        |record Data {
        | A : Other = new Other( X = 10 )
        |}
      """)

  }


  test("Expression field") {
    val cu = TestCompiler.compileValidScript(
      """
        |record Demo.Trade {
        |   Bid : double
        |   Ask : double
        |   Mid : double = ( Bid + Ask ) / 2
        |}
      """)

    println(cu.getUdt("Demo.Trade"))
  }

}

