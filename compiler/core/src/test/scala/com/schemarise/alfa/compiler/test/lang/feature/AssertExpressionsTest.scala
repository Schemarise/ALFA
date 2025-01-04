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

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class AssertExpressionsTest extends AnyFunSuite {

  test("Raise test") {
    val cua = TestCompiler.compileValidScript(
      """
        |entity RaiseTest key( id : string ) {
        |  a : int
        |  b : int
        |
        |  assert IsValid {
        |    if ( a > 10 )
        |      raise error(Accuracy, "a is over 10")
        |  }
        |}
        |
      """)
  }

  test("Decision table expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |entity DecisionTabled key( id : string ) {
        |  a : int
        |  b : string
        |
        |  assert IsValid {
        |    let dt = ( a * 3, b) match first
        |    {
        |        ( > 5          , "b" ) => "Good"
        |        ( 15           , "c" ) => "Good"
        |        ( < 5          , "e" ) => "Good"
        |        ( != 5         , "d" ) => "Good"
        |        ( [55 .. 60]   , "e" ) => "Good"
        |    }
        |  }
        |}
      """)
  }


  test("Decision table USD EUR expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |record DecisionData {
        |  Limits : map< string, int > = { "GBP" : 5, "USD" : 10, "EUR" : 16 }
        |  SNLP   : map< string, int > = { "GBP" : 4, "USD" : 3,  "EUR" : 10 }
        |}
        |
        |record DecisionSignals {
        |  VolatilityOver20pct : map< string, boolean > = { "USD" : true, "EUR" : false }
        |  MktParticipants     : map< string, int > = { "USD" : 3000, "EUR" : 2500 }
        |  MktPerformancePct   : map< string, int > = { "USD" : 40, "EUR" : 20 }
        |}
        |
        |library Offsetter {
        |  makeDecision( data : DecisionData, signals : DecisionSignals ) : string {
        |
        |     let ccys = ["EUR", "USD"]
        |
        |     let ratings : list< tuple< Ccy : string, Rating : int > > = map(ccys, ccy =>
        |     {
        |         let snlp = get( get(data.SNLP, ccy) )
        |         let limit = get( get(data.Limits, ccy) )
        |
        |         let vol = get( get(signals.VolatilityOver20pct, ccy) )
        |         let parts = get( get(signals.MktParticipants, ccy) )
        |         let perf = get( get(signals.MktPerformancePct, ccy) )
        |
        |         let score  = ( snlp        , limit , vol  , parts  , perf ) match {
        |                      ( [0 .. 50]   , < 50  , true , > 1000 , > 50 ) => 10
        |                      ( [50 .. 100] , >= 50 , true , > 500  , > 10 ) => 5
        |                      ( *           , *     , *    , *      , *    ) => 0
        |         }
        |
        |         return ( Ccy = ccy, Rating = get( score ) )
        |     } )
        |
        |     // let highestRated = max( get( ratings ), ( l, r ) => l.Rating > r.Rating )
        |
        |     return "USD"
        |  }
        |}
      """)
  }

  //  test( "In expression") {
  //    val cua = TestCompiler.compileValidScript(
  //      """
  //        |entity InExpression key( id : string ) {
  //        |
  //        |  assert Adult {
  //        |    let a = [1,2,3,4,5]
  //        |    let b = [1,3,5]
  //        |    let filtered = filter( a, e => e in b )
  //        |    return none
  //        |  }
  //        |}
  //      """)
  //  }

  test("Assert expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Customer {
        |
        |  name : string
        |  dob : date
        |
        |  assert Adult {
        |    let age : int = 10 // year( datediff( today(), dob ) )
        |    if ( age < 18 )
        |      raise error("Customer ${name} is aged ${age} so is not an adult")
        |  }
        |}
      """)

    val d = cua.getUdt("Customer")
  }

  test("Assert fragments") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Foo
        |
        |record Customer {
        |  name : string
        |  dob : date
        |}
        |
        |fragment record Customer {
        |  assert Adult {
        |    let s = new ExtValidator()
        |    let x = s::isOk()
        |  }
        |}
        |
        |library Validator {
        |    isOk() : boolean {
        |       return true
        |    }
        |}
        |
        |service ExtValidator {
        |    isOk() : boolean
        |}
        |
      """)
  }


  test("Sort expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace exprs.sort
        |
        |record Book {
        |   Name : string
        |}
        |
        |entity Customer key( id : string ) {
        |  Books : list< Book >
        |
        |  assert Adult {
        |    let sorted = sort(Books,
        |                      (l, r)
        |                      =>
        |                      compare( l.Name, r.Name ) )
        |  }
        |}
      """)
  }

  test("Min expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace exprs.min
        |
        |record Book {
        |   Name : string
        |}
        |
        |entity Customer key( id : string ) {
        |  Books : list< Book >
        |
        |  assert Adult {
        |    let vectorLamMin = min(Books, (l, r) => compare( l.Name, r.Name )  )
        |    let listMin = min([ 1, 3, 4, 5, 2 ] )
        |    let setMin = min({ 1, 3, 4, 5, 2 } )
        |    //let mapMin = min({ 1: "A", 3: "V", 4: "E" } )
        |    //let map2Min = min({ 1: "A", 3: "V", 4: "E" }, ( lk, rk) => compare( lk, rk ) )
        |    let numMin = min(100, 102)
        |  }
        |}
      """)
  }


  test("Max expression") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace exprs.min
        |
        |record Book {
        |   Name : string
        |}
        |
        |entity Customer key( id : string ) {
        |  Books : list< Book >
        |
        |  assert Adult {
        |    let vectorLamMin = max(Books, (l, r) => compare( l.Name, r.Name ) )
        |    let listMin = max([ 1, 3, 4, 5, 2 ] )
        |    let setMin = max({ 1, 3, 4, 5, 2 } )
        |    //let mapMin = max({ 1: "A", 3: "V", 4: "E" } )
        |    //let map2Min = max({ 1: "A", 3: "V", 4: "E" }, ( lk, rk) => compare( lk, rk ) )
        |    let numMin = max(100, 102)
        |  }
        |}
      """)
  }
}

