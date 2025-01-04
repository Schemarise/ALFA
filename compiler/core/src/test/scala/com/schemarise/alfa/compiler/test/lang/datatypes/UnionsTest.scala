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

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.schemarise.alfa.compiler.ast.model.types.AstUnionType
import com.schemarise.alfa.compiler.ast.nodes.Union
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ScalarDataType, UnionDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class UnionsTest extends AnyFunSuite {

  val mapper = new ObjectMapper() with ClassTagExtensions
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  test("Union extends") {
    val script =
      """
        |namespace a.b
        |
        |record A {
        |   y : B
        |}
        |
        |record B extends A {
        |
        |}
        |
      """.stripMargin

    //      """
    //        |namespace a.b
    //        |
    //        |trait A {
    //        |   y : B
    //        |}
    //        |
    //        |trait B includes A {
    //        |}
    //        |
    //      """.stripMargin
    val cua = TestCompiler.compileValidScript(script)

  }

  test("Union datatype tagged") {
    val script =
      """
        |record TaggedUnionTest {
        |    Source : union< loc : Location, Contents : string >
        |}
        |
        |record Location { }
        |
      """.stripMargin
    val cua = TestCompiler.compileValidScript(script)

    val udt = cua.getUdt("TaggedUnionTest").get.allFields.get("Source").get.dataType.asInstanceOf[UnionDataType]
    assert(udt.syntheticUnion.allFields.contains("loc"))
  }

  test("Union datatype untagged") {
    val script =
      """
        |record UntaggedUnionTest {
        |  F : union< int | strisng >
        |}
        |
      """.stripMargin
    TestCompiler.compileInvalidScript("@3:19 Unknown type 'strisng'", script)

    val script1 =
      """
        |record UntaggedUnionTest {
        |  F : union< int | string | double >
        |}
        |
      """.stripMargin
    val cua = TestCompiler.compileValidScript(script1)

    val t = cua.getUdt("UntaggedUnionTest").get.allFields.get("F").get.dataType

    assert(t.isInstanceOf[UnionDataType])

    val types = t.asInstanceOf[UnionDataType].untaggedTypes.toSet
    assert(types.contains(ScalarDataType.intType))
    assert(types.contains(ScalarDataType.stringType))
    assert(types.contains(ScalarDataType.doubleType))
  }

  test("Union untagged") {
    val script =
      """
        |union AllSupportedScalars = int | stringx
        |
      """.stripMargin
    TestCompiler.compileInvalidScript("@2:34 Unknown type 'stringx'", script)

    val script1 =
      """
        |union AllSupportedScalars = int | string
        |
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script1)
    val u = cua.getUdt("AllSupportedScalars").get.asInstanceOf[Union]
    val ut = u.untaggedTypes

    assert(u.unionType == AstUnionType.Untagged)
    assert(ut.size == 2)


    val script2 =
      """
        |union AllSupportedScalars = int | int(20,30)
        |
      """.stripMargin

    TestCompiler.compileInvalidScript("@2:34 Duplicate union component type int( 20, 30 ). Conflicts with int.", script2)
  }

  test("Union no optional") {
    val script =
      """
        |union AllSupportedScalars {
        |  A : int
        |  B : string?
        |}
        |
      """.stripMargin

    TestCompiler.compileInvalidScript("@4:2 Union cannot have an optional field - 'B'", script)
  }

  test("Union sdf") {
    val script =
      """
        |union U {
        |  A : int
        |  B : string
        |}
        |
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script)


    val f = cua.getUdt("U").get.allFields.get("A").get

    //    println( toJson( f ))

  }

  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map {
      case (k, v) => k.name -> v
    })
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }

}
