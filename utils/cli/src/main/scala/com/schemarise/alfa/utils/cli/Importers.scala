package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.{AlfaImporter, GeneratorException, SupportedGenerator}
import com.schemarise.alfa.generators.importers.jsonschema.JsonSchemaImporter

import java.nio.file.Path
import scala.collection.JavaConverters.mapAsJavaMap
import scala.io.Source

class Importers(logger : ILogger) extends GeneratorConfigBase(logger) {

  def getImportClassName(t: String): Option[String] = {
    val c = baseImporters.get(t)

    c match {
      case Some(_) => Some(c.get)
      case None =>
        val l = getClass.getResource(s"/META-INF/alfa/importer-${t}.ini")
        if (l == null)
          None
        else {
          val className = Source.fromURL(l).mkString.trim
          Some(className)
        }
    }
  }

  def importers = baseImporters.keySet

  private def baseImporters = {
    val fixed = Map(
      "jsonschema" -> classOf[JsonSchemaImporter].getName,
    )

    val fromClasspath = loadClasspathGenerators("importer")
    fixed ++ fromClasspath
  }

  def importerInstance(logger: ILogger,
                       name: String,
                       i: Path,
                       o: Path,
                       cfg: Map[String, String]): Option[AlfaImporter] = {
    val impClass = getImportClassName(name.toLowerCase)

    if (impClass.isEmpty)
      None
    else {
      val ctor = Class.forName(impClass.get).getConstructor(
        classOf[ILogger],
        classOf[Path],
        classOf[Path],
        classOf[java.util.Map[String, String]])

      try {
        val impInstance = ctor.newInstance(logger, i, o, mapAsJavaMap(cfg)).asInstanceOf[AlfaImporter]
        Some(impInstance)
      } catch {
        case g: GeneratorException =>
          logger.error(g.getMessage)
          None
      }
    }
  }
}