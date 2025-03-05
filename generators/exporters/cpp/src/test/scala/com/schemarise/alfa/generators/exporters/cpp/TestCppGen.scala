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
 
package com.schemarise.alfa.generators.exporters.cpp

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
import com.schemarise.alfa.generators.common.AlfaExporterParams
import com.schemarise.alfa.utils.testing.TestUtils
import org.apache.commons.io.FileUtils
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Collections
import scala.collection.JavaConverters._

class TestCppGen extends AnyFunSuite {

  test("TestTrait") {
    genCpp("TestTrait",
      """
        |namespace Demo.TestTrait
        |
        |trait SimpleTrait {
        |  Name : string
        |  Dob : date
        |  Marks : list< int >
        |}
    """.stripMargin)
  }

  test("TestEnum") {
    genCpp("TestEnum",
      """
        |namespace Demo.TestEnum
        |
        |enum EnValueType {
        |   X Y Z("zee") `A&B`
        |}
    """.stripMargin)
  }


  test("TestRecordEqualsTest") {
    genCpp("TestRecordEqualsTest",
      """
        |namespace Demo.TestRecordEqualsTest
        |
        |record OtherRec {
        |    Name : string
        |}
        |
        |record SimpleRecord {
        |  Others : list< OtherRec >
        |}
    """.stripMargin)
  }

  test("TestRecord") {
    genCpp("TestRecord",
      """
        |namespace Demo.TestRecord
        |
        |record OtherRec {
        |    Name : string
        |}
        |
        |enum DirectionType {
        |   N S W E
        |}
        |
        |record SimpleRecord {
        |  Name : string
        |  OptOther : OtherRec?
        |  Other : OtherRec
        |  EnumVal : DirectionType
        |  Others : list< OtherRec >
        |  Numbers : list< int >
        |}
        |
        |record EmptyRec {
        |}
    """.stripMargin)
  }

  test("TestService") {
    genCpp("TestService",
      """
        |namespace Demo.TestService
        |
        |service DataSrv() {
        |  foo(a : int, b: list< string > ) : void
        |  bar(a : int) : list< string >
        |}
    """.stripMargin)
  }

  test("TestTraitUse") {
    genCpp("TestTraitUse",
      """
        |namespace Demo.TestTraitUse
        |
        |trait TraitA {
        |    A : int
        |}
        |
        |trait TraitB {
        |    B : string
        |}
        |
        |record EmptyRec includes TraitA, TraitB {
        |}
        |
    """.stripMargin)
  }

  test("TestCollections") {
    genCpp("TestCollections",
      """
        |namespace Demo.TestCollections
        |
        |record TestCollections {
        |  l : list< int >
        |  s : set< int >
        |  m : map< int, int >
        |}
    """.stripMargin)
  }

  test("TestConstraints") {
    genCpp("TestConstraints",
      """
        |namespace Demo.TestConstraints
        |
        |record SimpleRec {
        |  Name : string 
        |}
        |
        |record RecTest {
        |  A : int(0, 100)
        |  B : date?
        |  C : string(5, 50)
        |  D : double(0, 100)?
        |  E : date
        |  F : map< long, int >(1, 10)
        |  G : list< string >(1, 10)
        |  H : set< int >(1, 5)
        |  I : SimpleRec
        |  J : datetime
        |  K : time
        |  M : map< int, date >
        |}
        |
    """.stripMargin)
  }

  private def genCpp(genDir: String, script: String) = {

    val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
    val CppGen = Paths.get(targetDir + "generated-sources/cpp/" + genDir + "/")

    VFS.mkdir(CppGen)

    val cua = TestCompiler.compileValidScript(script)

    val j = new CppExporter(AlfaExporterParams(new StdoutLogger(), CppGen, cua, Collections.emptyMap()))
    j.exportSchema()

//    compileCpp(CppGen, genDir)
  }

  def compileCpp(p: Path, genDir : String): Unit = {
    if (!p.getFileName.toString.startsWith("__")) {
      val hFiles = FileUtils.listFiles(p.toFile, Array("h"), true).asScala.toList
      val cppFiles = FileUtils.listFiles(p.toFile, Array("cpp"), true).asScala.toList
        
      // g++ -g -std=c++17 *.cpp
      val fs = cppFiles.map(_.toString)

      if (fs.size > 0) {
        val base =  TestUtils.getLocalProjectRootPath(classOf[TestCppGen])
        val incs = base + "src/main/resources/cpp"

        val headers = base + "target/generated-sources/cpp/" + genDir

        val impls = VFS.listFileForExtension(Paths.get(incs), ".cpp").map(e => e.toAbsolutePath.toString)

        val args = (List("g++", "-I" + incs, "-I" + headers, "-g", "-std=c++17", "-c") ++ fs.toSet ++ impls)

        println("Running C++:\n" + args.mkString(" "))
        val processBuilder = new ProcessBuilder(args.asJava)

        processBuilder.redirectErrorStream(true)
        processBuilder.directory(new File("target"));

        val process = processBuilder.start()
        val results = scala.io.Source.fromInputStream(process.getInputStream()).mkString("")

        val exitCode = process.waitFor()

        if (exitCode == 0)
          println(s"        SUCCESS file:${p.getFileName.toString} Output:$results ExitCode:$exitCode")
        else
          println(s"        ERROR file:${p.getFileName.toString} Output:$results ExitCode:$exitCode")

        assert(exitCode == 0)
      }
    }
  }
}
