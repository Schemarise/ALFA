package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.CompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}

import java.net.{URI, URL}
import java.nio.file.{Path, Paths}

abstract class AlfaImporter(param: AlfaImporterParams) extends TextWriter(param.logger) with SupportedGenerator {


  def toLocalPath(u: Path): Path = {
    toLocalPath(u.toUri)
  }

  def importConfigStr( k : String ) = {
    val v = param.importConfig.get(k)
    if ( v != null )
      v.toString
    else
      null
  }

  def toLocalPath(uri: URI): Path = {
    val scheme = uri.getScheme.toLowerCase

    logger.debug("Checking file scheme " + scheme)

    if (scheme.startsWith("http")) {
      val url = new URL(uri.toString)
      val fs = VFS.create()
      val path = fs.getPath("/")

      val d = HttpDownloadUtil.downloadFile(uri.toString, path)
      logger.info("Downloaded " + d.getFileName + " from " + uri.toString)
      d
    }
    else {
      Paths.get(uri)
    }
  }

  @throws[GeneratorException]
  def importSchema(): List[Path]

  def supportedConfig(): Array[String]

  def requiredConfig(): Array[String]

  def name: String

  def getDefinitions(): Set[String]

  def getDefinition(name: String): Option[IUdtBaseNode]

  private val start = System.currentTimeMillis()

  def writeAlfaFile(cua: CompilationUnitArtifact, outputDir: Path, fileName: String): Unit = {

    val e: String = cua.getErrors.size match {
      case 0 => ""
      case 1 => s" ${cua.getErrors.size} error"
      case _ => s" ${cua.getErrors.size} errors"
    }

    val w: String = cua.getWarnings.size match {
      case 0 => ""
      case 1 => s" ${cua.getWarnings.size} warning"
      case _ => s" ${cua.getWarnings.size} warnings"
    }

    val m = if (e.size > 0 || w.size > 0) "( found" + e + w + " )" else ""

    cua.getErrors.foreach(e => println(e))

    val elapsed = System.currentTimeMillis - start
    logger.vitalInfo("Import to Alfa completed in " + elapsed + "ms " + m)

    val filename = outputDir.resolve(fileName + ".alfa").toString

    enterFile(filename)

    val ns = cua.getCompilationUnitNamespaces().headOption
    if (ns.isDefined)
      writeln(s"namespace ${ns.get.name}\n")

    cua.getUdtVersionNames().foreach(n => {
      val ud = cua.getUdt(n.fullyQualifiedName).get

      if (!ud.isSynthetic && ud.writeAsModuleDefinition) {
        writeln(ud.toString + "\n")
      }
    })
    exitFile()
  }

  override def outputDirectory: Path = param.outputDirectory

}
