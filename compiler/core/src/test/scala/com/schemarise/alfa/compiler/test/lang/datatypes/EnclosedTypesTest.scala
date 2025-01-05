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

import com.schemarise.alfa.compiler.ast.model.types.Scalars
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{EitherDataType, ScalarDataType, UnionDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class EnclosedTypesTest extends AnyFunSuite {
  test("Compressed type test") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |    F : compressed< string >
        |}
      """)
    val t = cua.getUdt("Data").get
    val edt = t.allFields.get("F").get.dataType
  }

  test("Future type test") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |record Y.Data {
        |    // F1 : X.future
        |    F2 : future<int>
        |    type : enum<  forward , cds , mtm_swap , vanilla_swap , swaption , nds , option , xccy , spot ,
        |    cap_floor , `future` , fra , ndf , ccds , variance_swap , ois  >?
        |
        |}
      """)
  }

  test("either type test") {
    val cua = TestCompiler.compileValidScript(
      """
        |record Data {
        |    F : either< int, string >
        |}
      """)
    val t = cua.getUdt("Data").get
    val edt = t.allFields.get("F").get.dataType.asInstanceOf[EitherDataType]
    val l = edt.left.asInstanceOf[ScalarDataType]
    val r = edt.right.asInstanceOf[ScalarDataType]
    assert(l.scalarType == Scalars.int)
    assert(r.scalarType == Scalars.string)
  }


  test("enclosed union resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |fields {
        |    x : int
        |}
        |
        |record Data {
        |    F : union< x, y:string, Val :double >
        |}
      """)

    val t = cua.getUdt("Data").get
    val rf = t.asInstanceOf[UdtBaseNode].fields.mkString
    assert(rf.trim.equals("F : union< x, y : string, Val : double >"))

    val dt = t.allFields.get("F").get.dataType.asInstanceOf[UnionDataType].syntheticUnion

    assert(dt.allFields.get("x").isDefined)
    assert(dt.allFields.get("y").isDefined)
    assert(dt.allFields.get("Val").isDefined)
  }
}
