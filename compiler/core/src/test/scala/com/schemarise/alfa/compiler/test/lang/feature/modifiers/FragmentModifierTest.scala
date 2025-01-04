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
package com.schemarise.alfa.compiler.test.lang.feature.modifiers

import com.schemarise.alfa.compiler.utils.{TestCompiler, VFS}
import org.scalatest.funsuite.AnyFunSuite

class FragmentModifierTest extends AnyFunSuite {

  test("Fragment test namespace mismatch") {
    val cua = TestCompiler.compileInvalidScript(
      "@6:0 Fragment has no matching user-defined type DataX",
      """|
         |record A.Data {
         | F1 : int
         |}
         |
         |fragment record DataX {
         | F2 : string
         |}
      """)

    val udt = cua.getUdt("A.Data").get

    assert(udt.allFields.get("F1") isDefined)
    //    assert( udt.allFields.get("F2")isDefined)
  }

  test("Fragment test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |
        |trait TgtBase {
        |   TB : int
        |}
        |
        |trait FragBase {
        |   FB : int
        |}
        |
        |# Doc in record
        |trait Tgt includes TgtBase {
        |   T : int
        |
        |   assert TgtAssert {
        |   }
        |}
        |
        |# Doc in fragment
        |fragment trait Tgt includes FragBase {
        |   FT : string
        |
        |   assert TgtFragAssert {
        |   }
        |}
      """)

    val udt = cua.getUdt("A.Tgt").get

    //    println(udt)

    assert(udt.allFields.get("T") isDefined)
    assert(udt.allFields.get("FT") isDefined)
    assert(udt.allFields.get("FT") isDefined)
    assert(udt.docs.size == 2)

    val cus = cua.graph.topologicalOrPermittedOrdered().get

    println(cus(1))
  }

  test("Fragment duplicate field") {
    val cua = TestCompiler.compileInvalidScript("@9:1 Duplicate field 'F1'",
      """
        |namespace A
        |
        |record Data {
        | F1 : int
        |}
        |
        |fragment record Data {
        | F1 : string
        |}
      """)
  }

  test("Fragment from files") {
    val fs = VFS.create()
    VFS.mkdir(fs, "src")


    VFS.write(fs.getPath("src", "A.alfa"),
      s"""
         |fragment trait Example.Fragment.TypeA {
         |   F1 : int
         |   F2 : string
         |}
        """.stripMargin)

    VFS.write(fs.getPath("src", "A1.alfa"),
      s"""
         |@alfa.rt.BindClass( jaxb ="io.alfa.applibs.fpml511.FxAccrualDigitalOption" )
         |trait Example.Fragment.TypeA {
         |   F3 : date
         |}
        """.stripMargin)


    //    VFS.printFileSystemContents( fs.getPath("src"))
    val cua = TestCompiler.compileScriptOnly(fs.getPath("src"))
    val udt = cua.getUdt("Example.Fragment.TypeA")

    println(udt.get)

    if (cua.hasErrors)
      println(cua.getErrors)
  }
}
