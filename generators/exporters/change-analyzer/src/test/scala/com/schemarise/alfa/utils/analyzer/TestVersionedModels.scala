package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.utils.StdoutLogger
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class TestVersionedModels extends AnyFunSuite {
  val htmlDir = getClass.getProtectionDomain.getCodeSource.getLocation.getFile + "../generated-sources/html"
  val loc = getClass.getProtectionDomain.getCodeSource.getLocation.getFile + "../../src/test/alfa/"

  test("v1-v2") {

    val ca = new ChangeAnalyzer()

    val c = new AlfaCompiler()
    val cua1 = c.compile(Paths.get(loc + "testv1.alfa"))
    val cua2 = c.compile(Paths.get(loc + "testv2.alfa"))
    val cua3 = c.compile(Paths.get(loc + "testv3.alfa"))

    if (cua3.hasErrors) {
      println(cua3.getErrors.mkString("\n"))
      System.exit(-1)
    }

    val v2v3 = CompilationUnitChangeSet(cua1, cua2)
    val mods2 = ca.analyzeVersions(v2v3)

    val r = new ChangeAnalysisReportWriter(new StdoutLogger(), Paths.get(htmlDir))
    r.write("report", "ACCOUNTS-MODEL-V1", "ACCOUNTS-MODEL-V2", mods2)
  }
}
