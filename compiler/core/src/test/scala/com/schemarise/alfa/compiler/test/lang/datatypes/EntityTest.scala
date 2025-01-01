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

import com.schemarise.alfa.compiler.ast.nodes.{Entity, Key}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class EntityTest extends AnyFunSuite {
  test("Key resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |key OrderId { Id : uuid }
        |entity Order key OrderId {}
      """)
    assert(cua.getUdt("com.acme.OrderId").get.isInstanceOf[Key])
    val e = cua.getUdt("com.acme.Order").get
    assert(e.isInstanceOf[Entity])
    assert(e.toString.contains("entity com.acme.Order key com.acme.OrderId"))
  }

  test("Key resolution error") {
    TestCompiler.compileInvalidScript("@5:17 Unknown type 'com.acme.OrderId'",
      """
        |namespace com.acme
        |
        |key OrderID { Id : uuid }
        |entity Order key OrderId {}
      """)
  }

  test("Singleton entity") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |entity Settings {  }
      """)

    var e: Entity = cua.getUdt("com.acme.Settings").get.asInstanceOf[Entity]
    assert(e.isSingleton)
  }


  test("Emmbedded key resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace embedded.keyref
        |
        |record Ref {
        |  Id : ObjKey
        |}
        |
        |entity Obj key ( ObjKeyId : int ) {
        |}
      """)

    val e = cua.getUdt("embedded.keyref.Obj").get.asInstanceOf[Entity]
    val k = cua.getUdt("embedded.keyref.ObjKey").get.asInstanceOf[Key]

    val id = k.allFields("ObjKeyId")
    assert(id.dataType.isScalarNumeric)
  }

  test("Field and body field duplicate") {
    TestCompiler.compileInvalidScript("@8:0 Following fields are duplicated in the key and body - Id",
      """
        |namespace com.acme
        |
        |trait Base {
        |  Id : int
        |}
        |
        |entity Order key ( Id : int ) includes Base {
        |}
      """)
  }


  test("Field and body field duplicate from key trait") {
    TestCompiler.compileInvalidScript("@11:0 Following fields are duplicated in the key and body - Id",
      """
        |namespace com.acme
        |
        |trait Base {
        |  Id : int
        |}
        |
        |key SomeKey includes Base {
        |}
        |
        |entity Order key SomeKey includes Base {
        |}
      """)
  }

}
