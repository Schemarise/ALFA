package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.graph.GraphReachabilityScopeType
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams, SupportedGenerator}
import com.schemarise.alfa.generators.exporters.java._

import java.io.File
import java.nio.file.Path
import java.util.Properties
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

      "javajaxb" -> classOf[JaxbJavaInteropExporter].getName,
      "protobufjavawrapper" -> classOf[JavaProtobufWrapperExporter].getName,
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
    val expClass = getExportClassName(name.toLowerCase)

    if (expClass.isEmpty)
      None
    else {
      val ctor = Class.forName(expClass.get).getConstructor(classOf[AlfaExporterParams])

        val expInstance = ctor.newInstance(new AlfaExporterParams(logger, o, cua,
          mapAsJavaMap(cfg), GraphReachabilityScopeType.all)).asInstanceOf[AlfaExporter]

      Some(expInstance)
    }
  }
}