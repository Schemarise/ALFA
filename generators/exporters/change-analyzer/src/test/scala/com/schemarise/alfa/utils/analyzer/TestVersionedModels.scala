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

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.utils.StdoutLogger
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class TestVersionedModels extends AnyFunSuite {
  val htmlDir = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).resolve("../generated-sources/html")
  val loc =     Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).resolve("../../src/test/alfa/")

  test("v1-v2") {

    val ca = new ChangeAnalyzer()

    val c = new AlfaCompiler()
    val cua1 = c.compile(loc.resolve("testv1.alfa"))
    val cua2 = c.compile(loc.resolve("testv2.alfa"))
    val cua3 = c.compile(loc.resolve("testv3.alfa"))

    if (cua3.hasErrors) {
      println(cua3.getErrors.mkString("\n"))
      System.exit(-1)
    }

    val v2v3 = CompilationUnitChangeSet(cua1, cua2)
    val mods2 = ca.analyzeVersions(v2v3)

    val r = new ChangeAnalysisReportWriter(new StdoutLogger(), htmlDir)
    r.write("report", "ACCOUNTS-MODEL-V1", "ACCOUNTS-MODEL-V2", mods2)
  }
}
