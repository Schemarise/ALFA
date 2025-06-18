package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.utils.ILogger

import java.util.Properties
import scala.collection.JavaConverters._

abstract class GeneratorConfigBase(logger : ILogger) {
  def loadClasspathGenerators(genType: String): Map[String, String] = {
    val exporterUrls = getClass.getClassLoader.getResources(s"META-INF/schemarise/alfa/generators/$genType.properties").asScala.toSeq

    if ( exporterUrls.nonEmpty ) {
      logger.debug(s"Found $genType generators " + exporterUrls.mkString(", ") + " from dependencies" )
    }

    val fromClasspath : Map[String, String] =
      exporterUrls.flatMap(u => {
        val p = new Properties
        p.load(u.openStream())

        p.stringPropertyNames().asScala.toSeq.map(k => k -> p.getProperty(k))
      }).toMap

    // validate class exists
    fromClasspath.foreach( f => {
      logger.debug(s"Loading ${f._1} $genType from dependencies" )
      try {
        Class.forName(f._2, false, getClass.getClassLoader)
      }
      catch {
        case _ =>
          logger.error("Failed to load class " + f._2 + " for exportType " + f._1)
      }
    })

    fromClasspath
  }

}
