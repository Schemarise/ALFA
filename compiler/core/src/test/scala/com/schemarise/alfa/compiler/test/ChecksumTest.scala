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
package com.schemarise.alfa.compiler.test

import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class ChecksumTest extends AnyFunSuite {
  test("Checksum Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.Checksum
        |
        |trait SampleTrait {
        |}
        |
        |record Foo {
        |    F : string
        |}
        |
        |record SampleRec includes SampleTrait {
        |      F1 : int
        |      F2 : string
        |      F3 : date
        |      F4 : Foo
        |      F5 : map< int, Foo >
        |      F6 : list< long >
        |      F7 : set< Foo >
        |      F8 : enum< A, B, C >
        |      F9 : union< AA : string, BB: int >
        |      F10 : tuple< A : int, B : Foo >
        |      F11 : pair< int, string>
        |      F12 : either< Foo, string>
        |      F13 : int?
        |      F14 : SampleRec
        |}
        |
      """.stripMargin)

    val rec = cua.getUdt("Feature.Checksum.SampleRec").get
    val asStr = rec.toString

    val checksumCalcStr = rec.asInstanceOf[UdtBaseNode].checksumCalcString(true, false)

    val expected = "Feature.Checksum.SampleRec{F1:int;F10:tuple<A:int;B:Feature.Checksum.Foo[84455aa0];>;F11:pair<int;string;>;F12:either<Feature.Checksum.Foo[84455aa0];string;>;F13:optional<int;>;F14:Feature.Checksum.SampleRec[68b595d0];F2:string;F3:date;F4:Feature.Checksum.Foo[84455aa0];F5:map<int,int>;F6:list<long>;F7:set<Feature.Checksum.Foo[84455aa0]>;F8:enum<A,B,C>;F9:union<AA:string;BB:int;>;}"
    assert(checksumCalcStr == expected)

    val cs = rec.asInstanceOf[UdtBaseNode].checksum()
    assert(cs == "68b595d0:8838b4ef")
  }

  test("Checksum Test 2 - All Mandatory") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.Checksum
        |
        |trait SampleTrait {
        |}
        |
        |record Foo {
        |    F : string
        |}
        |
        |record SampleRec includes SampleTrait {
        |      F1 : int
        |      F2 : string
        |      F3 : date
        |      F4 : Foo
        |}
        |
      """.stripMargin)

    val rec = cua.getUdt("Feature.Checksum.SampleRec").get
    val asStr = rec.toString

    val checksumCalcStrAll = rec.asInstanceOf[UdtBaseNode].checksumCalcString(true, false)
    val checksumCalcStrMand = rec.asInstanceOf[UdtBaseNode].checksumCalcString(true, true)

    val expected = "Feature.Checksum.SampleRec{F1:int;F2:string;F3:date;F4:Feature.Checksum.Foo[84455aa0];}"

    assert(checksumCalcStrAll == expected)
    assert(checksumCalcStrMand == expected)

    val cs = rec.asInstanceOf[UdtBaseNode].checksum()
    // no optional fields
    assert(cs == "f7597642:")
  }

  test("Checksum Test 2 - Some Opt") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.Checksum
        |
        |trait SampleTrait {
        |}
        |
        |record Foo {
        |    F1 : string
        |    F2 : int?
        |}
        |
        |record SampleRec includes SampleTrait {
        |      F1 : int
        |      F2 : string
        |      F3 : date
        |      F4 : Foo
        |}
        |
      """.stripMargin)

    val rec = cua.getUdt("Feature.Checksum.SampleRec").get
    val asStr = rec.toString

    val checksumCalcStr1 = rec.asInstanceOf[UdtBaseNode].checksumCalcString(true, false)
    val checksumCalcStr2 = rec.asInstanceOf[UdtBaseNode].checksumCalcString(true, true)

    val expected = "Feature.Checksum.SampleRec{F1:int;F2:string;F3:date;F4:Feature.Checksum.Foo[c7034549];}"
    assert(checksumCalcStr1 == expected)

    val cs = rec.asInstanceOf[UdtBaseNode].checksum()
    // no optional fields
    assert(cs == "13c001be:c812e4b6")
  }

  test("Simple checksum 1") {
    val cua = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |record R {
        |    F1 : string
        |    F2 : int?
        |}
      """.stripMargin)

    val r = cua.getUdt("Feature.Checksum.R").get
    assert(r.checksum() == "b47c6436:eb926cbe")
  }

  test("Simple checksum order doesnt matter") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |trait C {
        |    C1 : int
        |    C2 : string
        |}
        |
        |record R includes C {
        |    F1 : string
        |    F2 : int
        |}
      """.stripMargin)

    val r1 = cua1.getUdt("Feature.Checksum.R").get
    assert(r1.checksum() == "503f4f0:")

    val cua2 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |trait C {
        |    C2 : string
        |    C1 : int
        |}
        |
        |record R includes C {
        |    F2 : int
        |    F1 : string
        |}
      """.stripMargin)

    val r2 = cua2.getUdt("Feature.Checksum.R").get
    assert(r2.checksum() == r1.checksum())
  }


  test("Simple checksum same mandatory") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |record R {
        |    F1 : string
        |    F2 : int
        |    F3 : date?
        |}
      """.stripMargin)

    val r1 = cua1.getUdt("Feature.Checksum.R").get.checksum().split(":")

    val cua2 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |record R  {
        |    F2 : int
        |    F1 : string
        |}
      """.stripMargin)

    val r2 = cua2.getUdt("Feature.Checksum.R").get.checksum().split(":")

    // mand parts match - r2 has only left part so take head
    assert(r2.head == r1.last)
  }

  test("Simple checksum expressions") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |record R {
        |    F1 : string
        |    F2 : int = 10
        |}
      """.stripMargin)
    val r1 = cua1.getUdt("Feature.Checksum.R").get.checksum()
    assert(r1 == "93bcc3f7:")
  }


  test("Simple checksum trait field") {
    val cua1 = TestCompiler.compileValidScript(
      """namespace Feature.Checksum
        |
        |trait T {
        |   T1 : string
        |}
        |
        |record R0 includes T {
        |   R1 : int
        |}
        |
        |record R {
        |    F1 : string
        |    F2 : T
        |}
      """.stripMargin)
    val r1 = cua1.getUdt("Feature.Checksum.R").get.checksum()
    assert(r1 == "aa5eefdd:")
  }
}
