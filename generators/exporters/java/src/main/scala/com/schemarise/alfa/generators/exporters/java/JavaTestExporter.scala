package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.nodes.{Library, Testcase}
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams, CompilerToRuntimeTypes}
import com.schemarise.alfa.generators.exporters.java.udt._


class JavaTestExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  override def name = "Java8Test"

  def exportSchema(): List[Path] = {

    val compilerToRt = CompilerToRuntimeTypes.create(logger, param.cua)

    val tcp = new TestcasePrinter(logger, param.outputDir, param.cua, compilerToRt)
    val to = typesToGenerate()

    to.foreach(e => {
      e match {
        case u: Testcase => tcp.print(u)
        case _ =>
      }
    })

    List.empty
  }

  def supportedConfig(): Array[String] = Array.empty


}
