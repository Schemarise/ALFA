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
package com.schemarise.alfa.compiler.test

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class TypeVarianceTest extends AnyFunSuite {

  test("TypeVariance Test") {
    val s =
      """
        |namespace Example1
        |
        |
        |record Foo {
        |}
        |
        |record Bar extends Foo {
        |}
        |
        |library LIB {
        |    f() : void {
        |       let x : list< Bar > = []
        |       LIB::g( x )
        |    }
        |
        |    g( a : list< Foo > ) : void {
        |    }
        |}
        |""".stripMargin

    TestCompiler.compileValidScript(s)
  }
}
