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

class MethodCallingTest extends AnyFunSuite {

  test("Chained method calls") {
    val cua = TestCompiler.compileValidScript(
      s"""
         |namespace MethodCalling
         |
         |library Chains {
         |   fn() : void {
         |       let l = [1,2,3]
         |
         |       let a = filter( l, x => x > 1 )
         |
         |       let b = l | filter( x => x > 1 )
         |       let c = l | filter( x => x > 1 ) | map( x => x + 1 )
         |       let d = filter( l, x => x > 1 ) |
         |               map( x => x + 1 ) |
         |               map( x => x + 2 )
         |   }
         |}
      """)

    println(cua.getUdt("MethodCalling.Chains").get)
  }


  test("Library usage") {
    val cua = TestCompiler.compileValidScript(
      s"""
         |namespace MethodCalling
         |
         |record Data {
         |  country : string(2,2)
         |
         |  assert IsCorrect {
         |     let lib = Validators::isCorrect()
         |     let s1 = new NoArgSrv()
         |     let r1 = s1::isCorrect()
         |     let s2 = new ArgSrv( "a" )
         |     let r2 = s2::isCorrect()
         |  }
         |}
         |
         |library Validators {
         |   isCorrect() : string? {
         |       return none
         |   }
         |}
         |
         |service NoArgSrv {
         |   isCorrect() : int
         |}
         |
         |service ArgSrv( tok : string ) {
         |   isCorrect() : int
         |}
         |
      """)
  }

  test("Chained method call") {
    val cua = TestCompiler.compileValidScript(
      s"""
         |namespace NestedMethods
         |
         |record A {
         |   F3 : int?
         |}
         |
         |record B {
         |   F2 : A ?
         |}
         |
         |record C {
         |   F1 : B ?
         |
         |   assert isValid {
         |       let a = get(get(get(F1).F2).F3)
         |       // let a = get(F1).F2
         |       let b = a + 1
         |   }
         |}
         """.stripMargin)

    println(cua.getUdt("NestedMethods.C").get)
  }

  //  test("FlatMap") {
  //    val cua = TestCompiler.compileValidScript(
  //      s"""
  //          |namespace NestedMethods
  //          |
  //          |record A {
  //          |   F3 : int?
  //          |}
  //          |
  //          |record B {
  //          |   F2 : try< A >
  //          |}
  //          |
  //          |record C {
  //          |   F1 : B ?
  //          |
  //          |   assert isValid {
  //          |       let a = flatMap( F1.F2.F3 )
  //          |       let b = get( a ) + 10
  //          |
  //          |       return none
  //          |   }
  //          |}
  //         """.stripMargin)
  //
  //    println(cua.getUdt("NestedMethods.C").get)
  //  }


  test("void method result") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Example
        |
        |entity Bar {
        |  Name : string
        |}
        |
        |library test {
        |    foo( ) : void {
        |       let a = 1
        |       test::bar( a = 10, b = 44 )
        |       let x1 = query( Bar, e => e.Name == "" )
        |       let x3 = query( target = Bar, where = e => e.Name == "" )
        |    }
        |
        |    bar( a : int, b : int ) : void {
        |    }
        |}
        |
      """)
  }
}

