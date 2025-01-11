package com.schemarise.alfa.generators.importers.jsonschema

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, GeneratorException}

import java.nio.file.{Files, Path}

class JsonSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {

  private var errored = false
  val ctx = new Context()

  private val rootPath = param.rootPath

  if (errored)
    throw new GeneratorException("Missing settings. See log messages.")

  processInputFiles()

  override def writeTopComment() = false

  private def processInputFiles() = {
    logger.info("Reading JSON Schema files from " + rootPath)
    if (Files.isDirectory(rootPath)) {
      Files.list(rootPath).filter(p => p.getFileName.toString.endsWith(".json")).forEach(f => {
        processInputFile(f)
      })
    } else {
      processInputFile(rootPath)
    }

    writeAlfaFiles(ctx, alfaNamespace)
  }

  private def alfaNamespace = {
    var ns = importConfigStr("namespace")

    if (ns == null)
      ns = "defaultnamespace"

    ns
  }

  private def processInputFile(p: Path) = {

    if (!p.getFileName.toString.endsWith(".json"))
      throw new GeneratorException("Expected input file to have .json extension")

    var ns = importConfigStr("namespace")

    if (ns == null) {
      logger.info("Using defaultnamespace. Set a 'namespace' configuration to override this.")
      ns = "defaultnamespace"
    }

    val tBuilder = new AlfaTypeBuilder(logger, ctx)
    val fragments = tBuilder.genAlfaModel(p, ns)

    if (fragments.length > 0) {
      val fn = p.getFileName.toString.replace(".json", "-fragments.alfa")

      enterFile(fn)
      write(
        s"""
           |namespace $alfaNamespace
           |""".stripMargin)
      write(fragments)
      exitFile()
    }
  }

  override def supportedConfig(): Array[String] = Array("namespace")

  override def requiredConfig(): Array[String] = Array("namespace")

  override def name: String = "json-schema"

  override def getDefinitions(): Set[String] = Set.empty

  override def getDefinition(name: String): Option[IUdtBaseNode] = None

  override def importSchema(): List[Path] = List.empty

}
