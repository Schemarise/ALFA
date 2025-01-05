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

import com.schemarise.alfa.compiler.TraverseUtils
import com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType
import com.schemarise.alfa.compiler.ast.nodes.{IntRangeNode, LongRangeNode, Union}
import com.schemarise.alfa.compiler.utils.{PathUtils, TestCompiler, VFS}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class ScalarsTest extends AnyFunSuite {

  test("all scalars") {
    val script =
      """
        |union AllSupportedScalars
        |{
        |	fString : string
        |	fFixedLengthString : string(1,20)
        |	fShort  : short
        |	fInt  : int
        |	fIntWithRange  : int(1,10000)
        |	fLong  : long
        |	fDecimal : decimal
        |	fFixedLengthDecimal : decimal(12,8)
        |	fBoolean  : boolean = false
        |	fDate : date("2000-01-01","2100-01-01")
        |	fDatetime : datetime
        |	fDatetimetz : datetimetz
        |	fTime : time
        |	fDouble : double
        |	fBinary : binary
        |	fFixedLengthBinary : binary(0,128)
        |	fVoid : void
        | fDuration : duration
        |	fUuid : uuid
        |
        |	// fByte : byte
        |	// fChar : char
        | //  fUrl1 : uri
        | // fUrl2 : uri("ftp","sftp")
        | fPattern : string("^(\\([0-9]{3}\\))?[0-9]{3}-[0-9]{4}$")
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script)

    assert(TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered.get).size == 1)
    assert(!cua.hasErrors && !cua.hasWarnings)

    val u = cua.getUdt("AllSupportedScalars").get
    assert(u.isInstanceOf[Union])
    assert(u.allFields.size == 20)
  }


  test("escaped regex test") {

    val p = Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "/../../src/test/alfa/regex.alfa").normalize()

    val script = VFS.read(p)

    val cua = TestCompiler.compileValidScript(script)

    val rec = cua.getUdt("demo.TestRegExTest").get
    val dt = rec.allFields.get("RegExTest").get.dataType

    val fmt = dt.asInstanceOf[ScalarDataType].formatArg.get.text

    val expected = """^[0-9a-zA-Z~!@#$%^&*()_+\-=\[\]\{}|;':",./<>?]*$"""
    assert(expected.equals(fmt))
  }

  test("int range test") {
    val script =
      """
        |record TestIntRange
        |{
        |	    OrderID : long(100000000,99999999999)
        |}
      """.stripMargin
    val cua = TestCompiler.compileValidScript(script)
    val dt = cua.getUdt("TestIntRange").get.allFields.get("OrderID").get.dataType

    val sdt = dt.asInstanceOf[ScalarDataType]

    assert(sdt.sizeRange.get.min.get.toString.equals("100000000"))
    assert(sdt.sizeRange.get.max.get.toString.equals("99999999999"))
  }

  test("date range test") {
    val script =
      """
        |record TestRange
        |{
        |	fDate : date("2000-01-01","2100-01-01")
        |}
      """.stripMargin
    val cua = TestCompiler.compileValidScript(script)
    val dt = cua.getUdt("TestRange").get.allFields.get("fDate").get.dataType

    val sdt = dt.asInstanceOf[ScalarDataType]

    assert(sdt.sizeRange.get.min.get.toString.equals("2000-01-01"))
    assert(sdt.sizeRange.get.max.get.toString.equals("2100-01-01"))
  }

  test("string range test") {
    val script =
      """
        |record TestRange
        |{
        |	f1 : string(10, 20)
        | f2 : string("^(\\([0-9]{3}\\))?[0-9]{3}-[0-9]{4}$")
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script)
    val f1dt = cua.getUdt("TestRange").get.allFields.get("f1").get.dataType
    val f1sdt = f1dt.asInstanceOf[ScalarDataType]

    assert(f1sdt.sizeRange.get.min.get == 10)
    assert(f1sdt.sizeRange.get.max.get == 20)

    val f2dt = cua.getUdt("TestRange").get.allFields.get("f2").get.dataType
    val f2sdt = f2dt.asInstanceOf[ScalarDataType]

    assert(f2sdt.stringPattern.isDefined)
  }


  test("date format test") {
    //    val script =
    //      """
    //        |record TestRange
    //        |{
    //        | 	f1 : date(*, *, "YYYY-MM")
    //        |}
    //      """.stripMargin
    //
    //    val cua = TestCompiler.compileValidScript(script)
    //    val f1dt = cua.getUdt("TestRange").get.allFields.get("f1").get.dataType
    //    val f1sdt = f1dt.asInstanceOf[ScalarDataType]
    //
    //    assert( f1sdt.dateFormat.isDefined )


    TestCompiler.compileInvalidScript(
      "@3:18 Invalid pattern format 'x'. Illegal pattern character 'x'.",
      """
        |record TestDateFormat {
        |  f1 : date(*, *, "x")
        |}
      """.stripMargin)

  }


  test("decimal range test") {
    val script =
      """
        |record TestRange
        |{
        |	f1 : decimal(20, 8)
        | f2 : decimal(*, *, 100.0, 1000.0 )
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(script)

    //    println(cua.getUdt("TestRange"))

    val f1dt = cua.getUdt("TestRange").get.allFields.get("f1").get.dataType
    val f1sdt = f1dt.asInstanceOf[ScalarDataType]

    assert(f1sdt.scale.get == 8)
    assert(f1sdt.precision.get == 20)

    val f2dt = cua.getUdt("TestRange").get.allFields.get("f2").get.dataType
    val f2sdt = f2dt.asInstanceOf[ScalarDataType]

    assert(f2sdt.scale.isEmpty)
    assert(f2sdt.precision.isEmpty)

    assert(f2sdt.sizeRange.get.min.get == 100.0)
    assert(f2sdt.sizeRange.get.max.get == 1000.0)
  }


  test("Int range test") {
    val bad =
      """
        |record IntRangedRecord
        |{
        |	fInt  : int(10,8)
        |}
      """.stripMargin
    TestCompiler.compileInvalidScript("@4:13 Start of range 10 is not smaller than or equal to end of range 8", bad)

    val good =
      """
        |record IntRangedRecord
        |{
        |	fInt    : int(-10,*)
        |	fString : string(1,255)
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(good)

    val dt1 = cua.getUdt("IntRangedRecord").get.allFields.get("fInt").get.dataType
    val range1 = dt1.asInstanceOf[ScalarDataType].sizeRange.get.asInstanceOf[IntRangeNode]
    assert(range1.min.get.asInstanceOf[Int] == -10)
    assert(range1.max.isEmpty)

    val dt2 = cua.getUdt("IntRangedRecord").get.allFields.get("fString").get.dataType
    val range2 = dt2.asInstanceOf[ScalarDataType].sizeRange.get.asInstanceOf[LongRangeNode]
    assert(range2.min.get.asInstanceOf[Int] == 1)
  }

  test("Date range") {

    TestCompiler.compileInvalidScript(
      "@4:13 Failed to parse date value expected in format YYYY-MM-DD. Text '1990-10-50' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 50",
      """
        |record Rec
        |{
        |    F : date("1990-10-50","01.01.1990")
        |}
      """.stripMargin)
  }

  test("Void test") {

    TestCompiler.compileInvalidScript("@4:8 Void can only be a field type of a union field or a return type of a service method",
      """
        |record Rec
        |{
        |    F : void
        |}
      """.stripMargin)

    TestCompiler.compileInvalidScript("@4:14 Void can only be a field type of a union field or a return type of a service method",
      """
        |record Rec
        |{
        |    F : list< void >
        |}
      """.stripMargin)

    TestCompiler.compileInvalidScript("@4:12 Void can only be a field type of a union field or a return type of a service method",
      """
        |service Srv
        |{
        |    fn( a : void ) : int
        |}
      """.stripMargin)

    TestCompiler.compileValidScript(
      """
        |service Srv
        |{
        |    fn( a : int ) : void
        |}
      """.stripMargin)
  }

  test("Type presedence test") {

    TestCompiler.compileValidScript(
      """
        |namespace nstest
        |
        |library FooLib
        |{
        |  bar() : void {
        |     var yearFrac : double
        |     yearFrac = 1 / 10
        |
        |  }
        |}
      """.stripMargin)
  }


}
