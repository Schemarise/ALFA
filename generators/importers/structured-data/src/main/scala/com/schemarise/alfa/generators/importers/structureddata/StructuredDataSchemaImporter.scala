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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

class StructuredDataSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {
  private val structureImportSettings = new StructureImportSettings(param.importConfig)
  private val localPath = toLocalPath(param.rootPath)
  private val tBuilder = loadModel(localPath)

  runImport()
  private def runImport() = {

    val outputFile = if ( ! Files.isDirectory(localPath) )
                        localPath.getFileName.toString.split("\\.").head
                     else
                        importConfigStr("namespace")

    writeAlfaFile(tBuilder.cua, outputDirectory, outputFile )
  }

  private def loadModel(rootPath: Path): TypeBuilder = {
    if (Files.isDirectory(rootPath))
      throw new AlfaSettingsException("Expected file, got directory " + rootPath)

    val ctx = new Context()

    val pathstr = rootPath.toString

    if ( pathstr.endsWith(".csv") ) {
      val tb = new CsvTypeBuilder(param.logger, ctx, rootPath, structureImportSettings)
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

      val tb = new JsonBasedTypeBuilder(ctx, structureImportSettings, node)

      tb
    }
  }

  override def supportedConfig(): Array[String] = StructureImportSettings.all.toArray

  override def name: String = "StructuredDataImporter"

  override def getDefinitions(): Set[String] = tBuilder.udts.keySet.toSet

  override def getDefinition(name: String): Option[IUdtBaseNode] = tBuilder.udts.get(name)

  override def importSchema(): List[Path] = List.empty

  override def requiredConfig(): Array[String] = Array(StructureImportSettings.namespace)

  override def writeTopComment() = false
}
