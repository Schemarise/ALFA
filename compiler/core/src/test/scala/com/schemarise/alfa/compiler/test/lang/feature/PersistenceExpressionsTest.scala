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

class PersistenceExpressionsTest extends AnyFunSuite {

  //  test ("Unknown expression ")  {
  //    val cua = TestCompiler.compileInvalidScript(
  //      """@12:18 Failed in expression. Unknown builtin method 'queryxx'. Did you mean 1. query(target:E, where:() => true)
  //        |2. query(target:E, where:() => true, orderBy:(E) => map<$any,int>, limit:int, storeName:string)
  //        |3. query(target:E, where:() => true, orderBy:(E) => map<$any,int>, limit:int)?""".stripMargin,
  //      """
  //        |namespace querying
  //        |
  //        |entity Trade key( id : string ) {
  //        |   email : string
  //        |}
  //        |
  //        |entity QuerySource key( id : string ) {
  //        |  srcEmail : string
  //        |
  //        |  assert ValidOrder {
  //        |    let orders1 = queryxx( Trade, e => e.email == srcEmail )
  //        |  }
  //        |}
  //      """)
  //  }

  test("Query expression") {
    val cua = TestCompiler.compileValidScript(
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
        |    let orders1 = query( Trade, e => e.email == srcEmail, s => { s.price : -1 }, 0 )
        |    let orders2 = query( Trade, e => e.email == srcEmail, s => { s.price : -1 }, 0, "db" )
        |    let orders4 = query( Trade, e => e.email == srcEmail )
        |
        |  }
        |}
      """)
  }

  test("Lookup expression") {
    val cua = TestCompiler.compileValidScript(
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

  test("Exists expression") {
    val cua = TestCompiler.compileValidScript(
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

  test("Persist expression") {
    val cua = TestCompiler.compileValidScript(
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
}

