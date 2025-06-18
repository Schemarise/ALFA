/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.schemarise.alfa.generators.importers.structureddata

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.{AlfaSettingsException, Context}
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, MissingParameter}
import com.schemarise.alfa.compiler.ast.nodes.Record

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable

class StructuredDataSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {
  private val loaded = new mutable.HashMap[String, Record]

  runImport()
  private def runImport() = {

    val localPath = toLocalPath(param.rootPath)

    if ( Files.isDirectory(localPath)) {

      Files.list(localPath).
        filter( p => p.toString.endsWith(".csv") ||  p.toString.endsWith(".json") ||  
        p.toString.endsWith(".xml") ||  p.toString.endsWith(".yaml") ).
        forEach( p => processFile(p) )
    }
    else {
      processFile(localPath)
    }
  }

  private def processFile(p: Path): Unit = {

    val outputFile = if (!Files.isDirectory(p))
      p.getFileName.toString.split("\\.").head
    else
      importConfigStr("namespace")

    val settings = new StructureImportSettings(param.importConfig, outputFile)

    val tBuilder = loadModel(p, settings)
    loaded ++= tBuilder.udts


    writeAlfaFile(tBuilder.cua, outputDirectory, outputFile)
  }

  private def loadModel(rootPath: Path, settings : StructureImportSettings ): TypeBuilder = {
    if (Files.isDirectory(rootPath)) {
      throw new AlfaSettingsException("Expected file, got directory " + rootPath)
    }

    val ctx = new Context()

    val pathstr = rootPath.toString

    logger.info("Loading " + pathstr)

    if ( pathstr.endsWith(".csv") ) {
      val tb = new CsvTypeBuilder(param.logger, ctx, rootPath, settings)
      tb
    }
    else {
      val jf: JsonFactory =
        if (pathstr.endsWith(".json"))
          null
        else if (pathstr.endsWith(".yaml"))
          new YAMLFactory()
        else if (pathstr.endsWith(".xml"))
          new XmlFactory()
        else
          throw new AlfaSettingsException("Unsupported file extension " + pathstr)

      val mapper = new ObjectMapper(jf)
      val contents = new String(Files.readAllBytes(rootPath), StandardCharsets.UTF_8)
      val node = mapper.readTree(contents)

      val tb = new JsonBasedTypeBuilder(ctx, settings, node)

      tb
    }
  }

  override def supportedConfig(): Array[String] = StructureImportSettings.all.toArray

  override def name: String = "StructuredDataImporter"

  override def getDefinitions(): Set[String] = loaded.keySet.toSet

  override def getDefinition(name: String): Option[IUdtBaseNode] = loaded.get(name)

  override def importSchema(): List[Path] = List.empty

  override def requiredConfig(): Array[String] = Array(StructureImportSettings.namespace)

  override def writeTopComment() = false
}
