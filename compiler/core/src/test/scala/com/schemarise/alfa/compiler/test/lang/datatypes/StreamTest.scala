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
package com.schemarise.alfa.compiler.test.lang.datatypes

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class StreamTest extends AnyFunSuite {
  test("Invalid streams") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:6 Invalid enclosed object. Stream of stream is not supported.",
      """
        |namespace com.acme
        |
        |record Data {
        | S1 : stream< stream< int > >
        |}
        |
      """)
  }

  test("No stream outside function") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:6 Invalid enclosed object. Use of stream outside of function parameter or result is not supported.",
      """
        |namespace com.acme
        |
        |record Data {
        | S1 : stream< int >
        |}
        |
      """)
  }

  test("Stream only in functions, cant nest") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:9 Invalid enclosed object. Stream of stream is not supported.",
      """
        |namespace com.acme
        |
        |service DataSrv() {
        | f( in : stream< stream< int > > ) : void
        |}
        |
      """)
  }

  test("Stream only in functions") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |service DataSrv() {
        | f( in : stream< int > ) : stream< int >
        |}
        |
      """)
  }

}
