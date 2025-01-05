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

import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.ast.nodes.{Key, Record}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class KeyTest extends AnyFunSuite {
  test("Key optional warning") {
    TestCompiler.compileInvalidScript(
      "@4:14 A key (OrderId) having an optional field - Id, is not permitted",
      """
        |namespace com.acme
        |
        |key OrderId { Id : uuid? }
      """)
  }

  test("Key<> usage") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |key K1 { Id : uuid }
        |
        |entity E1 key K1 {
        |}
        |
        |entity E2 key (Id:uuid) {
        |}
        |
        |record R {
        |  F1 : E1Key
        |  F2 : E2Key
        |  F3 : K1
        |}
      """)

    val k = cua.getUdt("com.acme.R").get.asInstanceOf[Record]

    val f1d = k.allFields.get("F1").get.dataType
    val f2d = k.allFields.get("F2").get.dataType
    val f3d = k.allFields.get("F3").get.dataType

    //    assert( f1d.isEncKeyOf() )
    //    assert( f2d.isEncKeyOf() )

    assert(f1d.asInstanceOf[UdtDataType].fullyQualifiedName == "com.acme.E1Key")
    assert(f2d.asInstanceOf[UdtDataType].fullyQualifiedName == "com.acme.E2Key")
    assert(f3d.asInstanceOf[UdtDataType].fullyQualifiedName == "com.acme.K1")

    assert(f3d.asInstanceOf[UdtDataType].udt.asInstanceOf[Key].isAbstract)


    //    assert(k.key.get.name.fullyQualifiedName == "com.acme.OrderKey")
  }

  test("Key usage") {
    TestCompiler.compileInvalidScript(
      "@6:17 The defined key com.acme.OrderKey conflicts with the implied key for entity Order",
      """
        |namespace com.acme
        |
        |key OrderKey { Id : uuid }
        |
        |entity Order key OrderKey {
        |}
      """)
  }

  test("Synth key found") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |key OrderId { Id : uuid }
        |
        |entity Order key ( Name : string ) {
        |}
      """)

    val k = cua.getUdt("com.acme.OrderKey")

    assert(k.isDefined)
  }

  test("Key with lists") {
    TestCompiler.compileInvalidScript(
      "@5:2 Failed in expression. A list field type is reachable via field Ids",
      """
        |namespace com.acme
        |
        |key OrderId {
        |  Ids : list<string>
        |}
      """)
  }


  test("Key with double") {
    TestCompiler.compileInvalidScript(
      "@5:2 Failed in expression. A double field type is reachable via field Ids",
      """
        |namespace com.acme
        |
        |key OrderId {
        |  Ids : double
        |}
      """)
  }


  test("Key reference") {
    TestCompiler.compileInvalidScript(
      "@7:7 The declaration of 'OrderKey' in @7:7 was previously declared in @4:0",
      """
        |namespace com.acme
        |
        |key OrderKey {
        |}
        |
        |entity Order key ( u : uuid ) {
        |}
      """)

    TestCompiler.compileInvalidScript(
      "@8:13 Unknown type 'Orders'",
      """
        |namespace com.acme
        |
        |entity Order key ( u : uuid ) {
        |}
        |
        |record Foo {
        |    k : key< Orders >
        |}
      """)


    TestCompiler.compileInvalidScript(
      "@8:13 Invalid key<> usage - com.acme.Bar not an entity",
      """
        |namespace com.acme
        |
        |record Bar {
        |}
        |
        |record Foo {
        |    k : key< Bar >
        |}
      """)

    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |entity Order key ( u : uuid ) {
        |}
        |
        |record Foo {
        |    k : key< Order >
        |}
      """)

    val z = cua.getUdt("com.acme.Foo").get.allFields.get("k").get.dataType.unwrapTypedef
    assert(z.toString == "com.acme.OrderKey")

    val cua2 = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |key ID {
        |}
        |entity Order key ID {
        |}
        |
        |record Foo {
        |    k : key< Order >
        |}
      """)

    val kf = cua2.getUdt("com.acme.Foo").get.allFields.get("k").get
    val z2 = kf.dataType.unwrapTypedef
    assert(z2.toString == "com.acme.OrderKey")

    assert(kf.toString == "k : key< com.acme.Order >")
  }

  test("Key with concept field") {
    TestCompiler.compileInvalidScript(
      "@12:2 Failed in expression. A trait field type is reachable via field F1",
      """
        |namespace com.acme
        |
        |trait T {
        |}
        |
        |record R {
        |  F1 : T
        |}
        |
        |key OrderId {
        |  F1 : R
        |}
      """)

    TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record R {
        |  F : int
        |}
        |
        |key OrderId {
        |  F1 : R
        |}
      """)
  }

}
