package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams}
import com.schemarise.alfa.generators.exporters.java.udt._


class JavaRestEndpointExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  override def name = "Java8Rest"


  def exportSchema(): List[Path] = {

    val tcp = new SpringRestServicePrinter(logger, param.outputDir, param.cua)
    val to = typesToGenerate()

    to.foreach(e => {
      e match {
        case u: IService =>
          tcp.print(u)
          u.versions.foreach(v => tcp.print(v.asInstanceOf[IService]))

        case _ =>
      }
    })

    List.empty
  }

  def supportedConfig(): Array[String] = Array.empty
}
