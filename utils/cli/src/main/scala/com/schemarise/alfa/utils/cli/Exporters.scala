package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.graph.GraphReachabilityScopeType
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams, SupportedGenerator}
import com.schemarise.alfa.generators.exporters.changeanalysis.ChangeAnalysisExporter
import com.schemarise.alfa.generators.exporters.cpp.CppExporter
import com.schemarise.alfa.generators.exporters.markdown.MarkdownExporter
import com.schemarise.alfa.generators.exporters.java._
import com.schemarise.alfa.generators.exporters.refactor.RefactorExporter
import com.schemarise.alfa.runtime.AlfaRuntimeException

import java.io.File
import java.nio.file.Path
import scala.collection.JavaConverters.mapAsJavaMap
import scala.io.Source
import scala.collection.JavaConverters._

class Exporters(logger : ILogger) extends GeneratorConfigBase(logger) {

  def getExportClassName(t: String): Option[String] = {
    val c = baseExporters.get(t)

    c match {
      case Some(_) => Some(c.get)
      case None =>
        val l = getClass.getResource(s"/META-INF/alfa/exporter-${t}.ini")
        if (l == null)
          return None
        else {
          val className = Source.fromURL(l).mkString.trim
          Some(className)
        }

    }
  }

  val ExpGcpBq = "gcp-bq"
  val ExpAvsc = "avsc"
  val ExpSampleData = "sampledata"

  private def baseExporters = {
    val fixed = Map(
      "java" -> classOf[JavaExporter].getName,
      "javatest" -> classOf[JavaTestExporter].getName,
      "javarest" -> classOf[JavaRestEndpointExporter].getName,
      "markdown" -> classOf[MarkdownExporter].getName,
      "changeanalyzer" -> classOf[ChangeAnalysisExporter].getName,

      "javajaxb" -> classOf[JaxbJavaInteropExporter].getName,
      "protobufjavawrapper" -> classOf[JavaProtobufWrapperExporter].getName,
      "refactor" -> classOf[RefactorExporter].getName,

      "cpp" -> classOf[CppExporter].getName,
    )

    val fromClasspath = loadClasspathGenerators("exporter")
    fixed ++ fromClasspath
  }

  private def exportDir = Map(
    "javarest" -> Array("..", "generated-sources", "java"),
    "javatest" -> Array("..", "generated-test-sources", "java"),
  )

  def exportSubDirectory(t: String) = {
    val path = exportDir.get(t)
    path.getOrElse(Array("..", "generated-sources", t)).mkString(File.separator)
  }

  def exporters = baseExporters.keySet

  def exporterInstance(name: String, o: Path,
                       cua: ICompilationUnitArtifact,
                       cfg: Map[String, String]): Option[AlfaExporter] = {

    logger.debug(s"Exporter $name with config $cfg")

    val expClass = getExportClassName(name.toLowerCase)

    if (expClass.isEmpty)
      None
    else {
      val ctor = Class.forName(expClass.get).getConstructor(classOf[AlfaExporterParams])

        val expInstance = ctor.newInstance(new AlfaExporterParams(logger, o, cua,
          mapAsJavaMap(cfg), GraphReachabilityScopeType.all)).asInstanceOf[AlfaExporter]

      val supported = expInstance.supportedConfig()
      cfg.keySet.foreach( s => {
        if (!supported.contains(s)) {
          throw new AlfaRuntimeException("The exportClass for '" + name + "' does not support <" + s +
            "> as a exportSetting config entry. Use one of " + supported.mkString(", "));
        }
      } )

      val reqd = expInstance.requiredConfig()
      reqd.foreach( s => {
        if (! cfg.contains(s)) {
          throw new AlfaRuntimeException("The export type " + name + "' requires <config><" + s +
            ">... as an exportSetting config entry");
        }
      } )

      Some(expInstance)
    }
  }
}