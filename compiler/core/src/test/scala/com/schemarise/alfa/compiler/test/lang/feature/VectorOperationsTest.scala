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

import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ListDataType, MapDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class VectorOperationsTest extends AnyFunSuite {

  test("stddev and variance simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Demo
        |
        |library AggregateLib {
        |    f() : void {
        |      let li : list<int> = [1, 2, 3, 6, 8, 8, 10]
        |      let sdi = stddev(li)
        |      let vi = variance(li)
        |
        |      let ld : list<long> = [234]
        |      let sd = stddev(ld)
        |      let vd = variance(ld)
        |
        |      let q1 = quartile(li, 1)
        |      let q3 = quartile(li, 3)
        |
        |      let p3 = percentile(li, 90)
        |
        |      let iqr = q3 - q1
        |    }
        |}
      """)
  }

  test("Aggregate simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Demo
        |
        |record Person {
        |  rank : string
        |  name : string
        |  salary : double
        |}
        |
        |library AggregateLib {
        |    f( l : list< Person > ) : int {
        |         let g : map< string, double >  = aggregate( l, e => e.rank, 0.0, (acc, e) => acc + e.salary )
        |         return 0
        |    }
        |}
      """)
  }

  test("Groupby simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Demo
        |
        |record Person {
        |  rank : string
        |  name : string
        |  salary : double
        |}
        |
        |library Lib {
        |    cal( v : list< Person > ) : double {
        |        return reduce(v, 0.0, (acc,e) => acc + e.salary )
        |    }
        |
        |    f( l : list< Person > ) : int {
        |
        |         let g : map< string, list< Person > >  = groupBy( l, e => e.rank )
        |         let d : map< string, double > = map( g, (k,v) => k, (k,v) => Lib::cal(v) )
        |
        |         return 0
        |    }
        |}
      """)
  }


  test("reduce on list") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Demo
        |
        |record Person {
        |}
        |
        |library Lib {
        |    cal( v : list< Person > ) : double {
        |        let x :  list< Person > = reduce(v, (list< Person >) [], (acc,e) => add(acc, e) )
        |
        |        return 0.0
        |    }
        |}
      """)
  }

  test("flatten on list") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Demo
        |
        |library Lib {
        |    cal( v : list< list< int > > ) : int {
        |        let x = flatten( v )
        |        let total = reduce( x, 0, ( acc, e ) => acc + e )
        |
        |        return total
        |    }
        |}
      """)
  }


}

