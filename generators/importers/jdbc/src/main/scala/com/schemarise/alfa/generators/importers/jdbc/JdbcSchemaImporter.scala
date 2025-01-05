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

package com.schemarise.alfa.generators.importers.jdbc

import java.nio.file.Path
import java.sql.{Connection, DriverManager}
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.{NamespaceNode, StringNode}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, GeneratorException}

import scala.collection.mutable.ListBuffer

class JdbcSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {

  private val reqdKeys = Seq("catalog", "schema", "namespace", "url", "user", "password")

  private var errored = false
  reqdKeys.foreach(k => {
    if (!param.importConfig.containsKey(k)) {
      logger.error(s"Missing setting for setting '$k'")
      errored = true
    }
  })

  if (errored)
    throw new GeneratorException("Missing settings. See log messages.")

  val connection = connect()

  private val tBuilder: TypeBuilder = loadModel()

  writeAlfaFile(tBuilder.cua, outputDirectory, importConfigStr("namespace"))


  def loadModel(): TypeBuilder = {

    val md = connection.getMetaData

    val tables = md.getTables(importConfigStr("catalog"), importConfigStr("schema"), null, Array("TABLE"))

    val names = new ListBuffer[String]()
    while (tables.next()) {
      names += tables.getString("TABLE_NAME")
    }

    val ns = NamespaceNode(nameNode = StringNode.create(importConfigStr("namespace")))

    new TypeBuilder(connection, ns, names)
  }


  override def supportedConfig(): Array[String] = reqdKeys.toArray

  override def name: String = "JdbcImporter"

  override def getDefinitions(): Set[String] = tBuilder.cua.getUdtVersionNames().map(_.fullyQualifiedName)

  override def getDefinition(name: String): Option[IUdtBaseNode] = tBuilder.cua.getUdt(name)

  override def importSchema(): List[Path] = List.empty

  def connect(): Connection = {
    val url = importConfigStr("url")
    val username = importConfigStr("user")
    val password = importConfigStr("password")
    val conn = DriverManager.getConnection(url, username, password)
    conn
  }

  override def requiredConfig(): Array[String] = Array.empty
}
