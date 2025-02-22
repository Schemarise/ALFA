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
 
package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.scenarios.BreakingDataStructureChange
import org.scalatest.funsuite.AnyFunSuite
import schemarise.alfa.runtime.model.diff._

import java.nio.file.Paths

class BreakingDataStructureChangeTest extends AnyFunSuite {

  test("BreakingDataStructureChange 1") {
    val v1 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int ## Field doc
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }

  test("BreakingDataStructureChange 4") {
    val v1 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : list< int >
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : list< int(10,100) >
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }

  test("BreakingDataStructureChange 2") {
    val v1 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int
        |}
        |
        |record Rec2 {
        |    F1 : int
        |}
        |
        |enum En {
        |    A B C
        |}
        |
        |entity Ent1 key( k : string ) { }
        |
        |service Srv {
        |    f( a : Rec1 ) : void
        |}
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |record Rec1 {
        |    F1 : int
        |    F2 : string
        |}
        |
        |enum En {
        |    A B C D
        |}
        |
        |entity Ent1 key( k : long ) { }
        |
        |service Srv {
        |    f( a : Rec1 ) : void
        |}
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    val srv = res.filter(e => e.getTargetUdt.getUdtName == "Change.Srv").head

    assert(srv.getChangeCategory == ChangeCategoryType.BreakingApiChange && srv.getMessage.get() == "Structure of type 'Change.Rec1' for parameter 'a' has been modified")
    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }

  test("BreakingDataStructureChange 5") {
    val v1 =
      """
        |namespace Change
        |
        |trait T1 scope Rec1 { }
        |
        |record Rec1 includes T1 {
        |}
        |
        |service Srv {
        |    f( a : T1 ) : void
        |}
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |trait T1 scope Rec1, Rec2 { }
        |
        |record Rec1 includes T1 { }
        |record Rec2 includes T1 { }
        |
        |service Srv {
        |    f( a : T1 ) : void
        |}
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }


  test("Indirect breaking 3") {
    val v1 =
      """
        |namespace Change
        |
        |record A { }
        |record B { a : A }
        |record C { b : B }
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |record A { a : int }
        |record B { a : A }
        |record C { b : B }
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    assert(res.size == 3)

    val added = res.filter(e => e.getChangeCategory == ChangeCategoryType.BreakingDataStructureChange).head
    assert(added.getTargetUdt.getUdtName == "Change.A")

    val change2b = res.filter(e => e.getTargetUdt.getUdtName == "Change.B").head
    assert(change2b.getMessage.get() == "Change.B > Change.A" && change2b.getChangeCategory == ChangeCategoryType.IndirectBreakingDataStructureChange)

    val change2c = res.filter(e => e.getTargetUdt.getUdtName == "Change.C").head
    assert(change2c.getMessage.get() == "Change.C > Change.B > Change.A" && change2c.getChangeCategory == ChangeCategoryType.IndirectBreakingDataStructureChange)
  }

  test("Indirect breaking - non breaking - only trait reachable ") {
    val v1 =
      """
        |namespace Change
        |
        |trait A { }
        |trait B includes A { }
        |record C { a : A }
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |trait A { }
        |trait B includes A { F : int }
        |record C { a : A }
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    assert(res.size == 1)
    val added = res.filter(e => e.getChangeCategory == ChangeCategoryType.BreakingDataStructureChange).head
    assert(added.getTargetUdt.getUdtName == "Change.B" && added.getEditType == EditType.Added)
  }

  test("Indirect breaking - breaking - trait in scope changed ") {
    val v1 =
      """
        |namespace Change
        |
        |trait Ax scope Bx { }
        |trait Bx includes Ax scope { }
        |record Cx { a : Ax }
        |
      """.stripMargin

    val cua1 = TestCompiler.compileValidScript(v1)

    val v2 =
      """
        |namespace Change
        |
        |trait Ax scope Bx { }
        |trait Bx includes Ax scope { F : int }
        |record Cx { a : Ax }
        |
      """.stripMargin

    val cua2 = TestCompiler.compileValidScript(v2)

    val cs = CompilationUnitChangeSet(cua1, cua2)
    val res = BreakingDataStructureChange.run(cs)

    assert(res.size == 3)

    val added = res.filter(e => e.getChangeCategory == ChangeCategoryType.BreakingDataStructureChange).head
    assert(added.getTargetUdt.getUdtName == "Change.Bx")

    val change2b = res.filter(e => e.getTargetUdt.getUdtName == "Change.Ax").head
    assert(change2b.getMessage.get() == "Change.Ax > Change.Bx" && change2b.getChangeCategory == ChangeCategoryType.IndirectBreakingDataStructureChange)

    val change2c = res.filter(e => e.getTargetUdt.getUdtName == "Change.Cx").head
    assert(change2c.getMessage.get() == "Change.Cx > Change.Ax > Change.Bx" && change2c.getChangeCategory == ChangeCategoryType.IndirectBreakingDataStructureChange)

    res.foreach(e => println(Alfa.jsonCodec().toFormattedJson(e)))
  }
}
