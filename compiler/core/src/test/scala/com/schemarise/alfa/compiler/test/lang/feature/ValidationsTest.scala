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

class ValidationsTest extends AnyFunSuite {
  test("Simple Validation") {
    val script =
      """
        |record A {
        |  m : map< string, B >
        |  s : set< C >
        |
        |  mapOnList<T,R>( v : list< T >, f : func<( T ), R > ) : set<R> {
        |  }
        |
        |  visitSet<T,R>( v : set< T >, f : func<( T ), R > ) : set<R> {
        |  }
        |
        |  visitMap<K, V, R>( v : map< K, V >, f : func<( K, V ), R > ) : set<R> {
        |  }
        |
        |/*
        |  // Validate A.m.values.Ccy exists in A.s.Ccy
        |  validate() : void {
        |    let ccys : set< string > = map( s, e => ed.Ccy )
        |    apply( m,
        |           e => {
        |             if ( ! contains( ccys, e.value.Ccy ) )
        |               raise( )
        |           }
        |         )
        |  }
        |*/
        |}
        |
        |record B {
        |  Ccy : string
        |  Price : double
        |}
        |
        |record C {
        |  Ccy : string
        |}
        |
      """.stripMargin

    val cua = TestCompiler.compileScriptOnly(script, false)
    if (cua.hasErrors)
      print(cua.getErrors)
  }
}
