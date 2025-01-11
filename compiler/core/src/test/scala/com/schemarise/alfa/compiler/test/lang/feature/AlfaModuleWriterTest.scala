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
package com.schemarise.alfa.compiler.test.lang.feature

import com.schemarise.alfa.compiler.CompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}
import com.schemarise.alfa.compiler.utils.{AlfaTestPaths, VFS}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{FileSystems, Files}

class AlfaModuleWriterTest extends AnyFunSuite with AlfaTestPaths {
  private val compiler = new com.schemarise.alfa.compiler.AlfaCompiler()

  var settings: AllSettings = new AllSettings(
    projectId = Some(ArtifactReference(None, "com.acme.models:Example"))
  )

  test("Simple test") {
    val s =
      """
        |typedefs {
        |  ccy = string
        |}
        |
        |fields {
        |  cost : double(0.0, *)
        |}
        |
        |namespace ziptest
        |
        |record A {
        | F1 : B
        | cost
        |}
        |
        |// @alfa.db.Table("X")
        |record B {
        | F2 : int
        | F3 : ccy
        |}
        |
        |record NS.C {
        | F4 : int
        | F5 : string
        |}
        |
        |service Srv {
        |    Fn() : int
        |}
        |
        """.stripMargin

    val cua = compiler.compile(s, settings)
  }

  def build(cua: ICompilationUnitArtifact, asserts: Boolean = true) {

    val fs = VFS.create()
    val zipTest = fs.getPath("/as-zip")

    cua.asInstanceOf[CompilationUnitArtifact].writeAsZipModule(logger, zipTest, true)

    val p = fs.getPath("/")

    val zip = fs.getPath("/as-zip/com.acme.models-Example.zip")

    assert(Files.exists(zip))

    val zipfs = FileSystems.newFileSystem(zip, null.asInstanceOf[ClassLoader])


    //    VFS.ls(zip)
    VFS.printFileSystemContents(zip)

    if (asserts) {
      assert(Files.exists(zipfs.getPath("ziptest", "A.alfa")))
      assert(Files.exists(zipfs.getPath("ziptest", "Srv.alfa")))
      assert(Files.exists(zipfs.getPath("ziptest", "B.alfa")))
      assert(Files.exists(zipfs.getPath("ziptest", "NS", "C.alfa")))
      assert(Files.exists(zipfs.getPath(".alfa-meta", "zip-index.txt")))
      assert(Files.exists(zipfs.getPath(".alfa-meta", "settings.alfa-proj.yaml")))
    }
    // VFS.printFileSystemContents(zipfs)

    val fs2 = VFS.create()
    val dirTest = fs2.getPath("/as-dir")

    cua.asInstanceOf[CompilationUnitArtifact].writeAsFileSystemModule(logger, dirTest)

    //    VFS.printFileSystemContents(fs2)

  }
}
