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

import com.schemarise.alfa.compiler.ast.nodes.Service
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class ServiceTest extends AnyFunSuite {
  test("Service resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |entity Item key( Id : uuid ) { Name : string }
        |entity Order key( Id : uuid ) { Items : list<Item> }
        |
        |service Store( Division : string ) {
        | createOrder( items : list<Item> ) : Order
        |}
        |
      """)

    val o = cua.getUdt("com.acme.Store").get
    assert(o.isInstanceOf[Service])
    val i = o.asInstanceOf[Service]

    assert(i.versionedName.fullyQualifiedName.equals("com.acme.Store"))
    assert(i.versionedName.name.equals("Store"))
    assert(i.getMethodSignatures.size == 1)

    val method = i.getMethodSignatures.get("createOrder").get
    assert(method.name.fullyQualifiedName.equals("createOrder"))
    assert(method.formals.contains("items"))
  }

  test("Service method dups") {
    val cua = TestCompiler.compileInvalidScript("@4:4 Duplicate function 'f'",
      """
        |service Calc( seed : double ) {
        |    f() : double
        |    f() : double
        |}
      """)
  }

  test("Service referencing $meta types") {
    val cua = TestCompiler.compileInvalidScript(
      "@12:8 Service 'Calc' references meta types through method arguments or return types",
      """
        |namespace foo
        |
        |record Base {
        |   a : $udt
        |}
        |
        |record Rec {
        |   b : Base
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)
  }


  test("Service referencing non-concrete trait types") {
    TestCompiler.compileInvalidScript(
      "@11:8 Service 'Calc' references trait 'foo.Base' which does not define a scope",
      """
        |namespace foo
        |
        |trait Base {
        |}
        |
        |record Rec {
        |   b : Base
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)

    TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |// trait is unrelated to Calc
        |trait Base {
        |}
        |
        |record Rec {
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)

    TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |trait Base scope Rec {
        |}
        |
        |record Rec includes Base {
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)

    TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |trait Base {
        |}
        |
        |record Rec includes Base {
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)
  }

  test("Includes for service") {
    TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |trait Base {
        |}
        |
        |// an include reference to a trait is fine
        |record Rec includes Base {
        |}
        |
        |service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)
  }


  test("Includes for service internal") {
    TestCompiler.compileValidScript(
      """
        |namespace foo
        |
        |trait Base {
        |}
        |
        |record Rec {
        |  F: Base
        |}
        |
        |internal service Calc( seed : double ) {
        |    f() : Rec
        |}
      """)
  }
}
