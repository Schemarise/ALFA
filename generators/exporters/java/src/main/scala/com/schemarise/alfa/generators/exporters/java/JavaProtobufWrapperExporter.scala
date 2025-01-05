package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams, CompilerToRuntimeTypes}
import com.schemarise.alfa.generators.exporters.java.udt._


class JavaProtobufWrapperExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  override def name = "Java8Protobuf3WrapperExporter"

  override def outputDirectory: Path = {
    val p = param.outputDir.getParent.resolve("java")
    p
  }

  def exportSchema(): List[Path] = {

    val compilerToRt = CompilerToRuntimeTypes.create(logger, param.cua)

    val tcp = new ProtobufWrapperPrinter(logger, outputDirectory, param.cua, compilerToRt)
    val to = typesToGenerate()

    to.foreach(e => {
      e match {
        case u: Testcase =>
        case u: Library =>
        case u: Service =>
        case u: Testcase =>
        case u: Testcase =>
        case u: NamespaceNode =>
        case u: EnumDecl =>
          tcp.enumPrint(e.asInstanceOf[UdtBaseNode])
        case _ =>
          tcp.udtPrint(e.asInstanceOf[UdtBaseNode])
      }
    })

    List.empty
  }

  def supportedConfig(): Array[String] = Array.empty


}
