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

class LibrarySupportTest extends AnyFunSuite {
  test("Library call chain") {
    val code =
      """
        |namespace NS
        |
        |library LIB {
        |   fn1() : void {
        |       // LIB::fn2(10)
        |       LIB::_get(10)
        |   }
        |
        |   _get(a :int) : void {
        |   }
        |}
      """

    TestCompiler.compileValidScript(code)
  }
}