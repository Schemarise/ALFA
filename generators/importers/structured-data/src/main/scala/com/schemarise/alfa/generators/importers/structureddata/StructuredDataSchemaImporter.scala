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
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.compiler.{AlfaSettingsException, Context}
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, MissingParameter}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.time.format.DateTimeFormatter

class StructuredDataSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {
  private val reqdKeys = Seq("namespace")

  private var errored = false

  reqdKeys.foreach(k => {
    if (!param.importConfig.containsKey(k)) {
      logger.error(s"Missing setting for setting '$k'")
      errored = true
    }
  })

  if (errored)
    throw new MissingParameter("Missing settings. See log messages.")

  private val tBuilder = loadModel(toLocalPath(param.rootPath))

  writeAlfaFile(tBuilder.cua, outputDirectory, importConfigStr("namespace"))

  def loadModel(rootPath: Path): TypeBuilder = {
    if (Files.isDirectory(rootPath))
      throw new AlfaSettingsException("Expected file, got directory " + rootPath)

    val ctx = new Context()

    val pathstr = rootPath.toString

    if ( pathstr.endsWith(".csv") ) {
      val tb = new CsvTypeBuilder(ctx, rootPath,
        importConfigStr("namespace"),
        importConfigStr("typename", "CsvImported" ),
        importConfigStr("dateformat", "yyyy-MM-dd" ),
        importConfigStr("datetimeformat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
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

      val tb = new JsonBasedTypeBuilder(ctx, importConfigStr("namespace"), node)

      tb
    }
  }


  override def supportedConfig(): Array[String] = Array("dateformat")

  override def name: String = "StructuredDataImporter"

  //  override def baseDir: Path = rootPath

  override def getDefinitions(): Set[String] = tBuilder.udts.keySet.toSet

  override def getDefinition(name: String): Option[IUdtBaseNode] = tBuilder.udts.get(name)

  override def importSchema(): List[Path] = List.empty

  override def requiredConfig(): Array[String] = Array.empty

  override def writeTopComment() = false
}
