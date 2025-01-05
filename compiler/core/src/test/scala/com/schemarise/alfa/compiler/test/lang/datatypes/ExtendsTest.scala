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

class ExtendsTest extends AnyFunSuite {

  test("Wrong extends 1") {
    val cua = TestCompiler.compileInvalidScript(
      "@8:0 record Bar cannot extend a key - Foo",
      """
        |namespace redefine
        |
        |key Foo {
        |   A : int
        |}
        |
        |record Bar extends Foo {
        |}
        |
    """.stripMargin)

    TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |record Foo {
        |}
        |
        |record Bar extends Foo {
        |}
        |
    """.stripMargin)
  }

  test("Field redefined") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |record Foo {
        |   A : int
        |}
        |
        |record Bar extends Foo {
        |   A : int
        |}
        |
    """.stripMargin)

    assert(cua.hasWarnings)

    TestCompiler.compileInvalidScript(
      "@9:3 Duplicate field 'A'",
      """
        |namespace redefine
        |
        |record Foo {
        |   A : string
        |}
        |
        |record Bar extends Foo {
        |   A : int
        |}
        |
    """.stripMargin)
  }

  test("Wrong extends 2") {
    val cua = TestCompiler.compileInvalidScript(
      "@4:0 Multiple extends defined for 'Bar' via fragments",
      """
        |namespace redefine
        |
        |record Foo1 {
        |}
        |
        |record Foo2 {
        |}
        |
        |fragment record Bar extends Foo1 {
        |}
        |
        |record Bar extends Foo2 {
        |}
        |
    """.stripMargin)
  }

  test("Extended fields") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace redefine
        |
        |record Foo {
        |   F1 : int
        |   F2 : int
        |}
        |
        |record Bar extends Foo {
        |   B : int
        |}
        |
    """.stripMargin)

    assert(cua.getUdt("redefine.Bar").get.allFields.size == 3)
    assert(cua.getUdt("redefine.Bar").get.localFieldNames.size == 1)
  }

  test("test extends fields") {
    val s =
      """
        |namespace NS1
        |
        |trait T { t : int }
        |
        |record A {  a : int }
        |
        |record B extends A includes T { b : int }
        |
        |record C extends B { c : int }
        |
      """.stripMargin

    val cua = TestCompiler.compileValidScript(s)

    val fields = cua.getUdt("NS1.C").get.allFields
    assert(fields.size == 4)
    assert(fields.keys.toList.equals(List("a", "t", "b", "c")))
  }

  test("test extends entity") {
    val s =
      """
        |namespace NS1
        |
        |entity A {
        |   a : int
        |}
        |
        |entity B extends A {
        |   b : int
        |}
        |
        |entity C extends B {
        |   c : int
        |}
        |
      """.stripMargin

    val cua = TestCompiler.compileValidScript(s)

    val af = cua.getUdt("NS1.C").get.allFields
    val aaf = cua.getUdt("NS1.C").get.allAddressableFields

    assert(af.size == 3)
    assert(aaf.size == 3)
    assert(af.keys.toList.equals(List("a", "b", "c")))
  }

}
