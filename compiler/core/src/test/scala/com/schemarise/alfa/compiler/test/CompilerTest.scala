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

import com.schemarise.alfa.compiler.settings.AllSettings
import com.schemarise.alfa.compiler.utils.{StdoutLogger, VFS}
import com.schemarise.alfa.compiler.{AlfaCompiler, CompilationUnitArtifact}
import org.scalatest.funsuite.AnyFunSuite

class CompilerTest extends AnyFunSuite {

  val compiler = new AlfaCompiler()
  val logger = new StdoutLogger

  test("compile as text") {
    val cua = compiler.compile("record Data { F : int }")
    assert(cua.getUdt("Data").isDefined)
  }

  test("language-version test") {
    val cua1 = compiler.compile("language-version 2")
    assert(cua1.getErrors.size == 0)

    val cua2 = compiler.compile("language-version 4")
    assert(cua2.getErrors.size == 1)
    assert(cua2.getErrors.head.formattedMessage == "Unsupported version specified - 4. ALFA Compiler in use supports up-to 3")

  }


  test("model-id test") {
    val cua1 = compiler.compile(
      """
        |model-id "iso-123:1"
        |""".stripMargin)
    assert(cua1.getErrors.size == 0)

    println(cua1.asInstanceOf[CompilationUnitArtifact].compilationUnit)

    assert(cua1.asInstanceOf[CompilationUnitArtifact].compilationUnit.modelVersion.get.text.equals("iso-123:1"))
  }



  //  test("compile as external path file") {
  //    val p = Paths.get("/Users/sadia/IdeaProjects/smt/libs/runtime/ast/src/main/alfa/Test.alfa")
  //    val cua = compiler.compile(p, new AllSettings())
  //
  //    val c1 = cua.getUdt("Sample.C1").get
  //    assert( c1.allFields.map( f => f._1).equals( List("b3", "b2", "b1", "cref", "a", "fa1", "fa2") ) )
  //  }

  test("compile as path file") {
    val fs = VFS.create()
    val p = fs.getPath("sample.alfa")
    VFS.write(logger, p, "record Data { F : int }")
    val cua = compiler.compile(p, new AllSettings())
    assert(cua.getUdt("Data").isDefined)
  }

  test("compile as path directory") {
    val fs = VFS.create()
    VFS.mkdir(fs, "model/definitions")
    val p = fs.getPath("model/definitions/sample.alfa")
    VFS.write(logger, p, "record Data { F : int }")

    val cua = compiler.compile(fs.getPath("model"), new AllSettings())
    assert(cua.getUdt("Data").isDefined)

    //    VFS.printFileSystemContents(fs)
  }

  test("compile multiple paths") {
    val fs = VFS.create()
    VFS.mkdir(fs, "model/base")
    VFS.mkdir(fs, "model/app")
    VFS.write(logger, fs.getPath("model/base/sample1.alfa"), "record Base { F : int }")
    VFS.write(logger, fs.getPath("model/app/sample1.alfa"), "record App { F : int }")

    val cua = compiler.compile(fs.getPath("model"), new AllSettings())
    assert(cua.getUdt("App").isDefined)
    assert(cua.getUdt("Base").isDefined)

    //    VFS.printFileSystemContents(fs)
  }

}
