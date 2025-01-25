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
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams}

import scala.collection.mutable.ListBuffer

class JdbcSchemaImporter(param: AlfaImporterParams) extends AlfaImporter(param) {

  private val Catalog = "catalog"
  private val Schema = "schema"
  private val Namespace = "namespace"
  private val Url = "url"
  private val User = "user"
  private val Password = "password"

  private val connection = connect()

  private val tBuilder: TypeBuilder = loadModel()

  writeAlfaFile(tBuilder.cua, outputDirectory, importConfigStr(Namespace))


  def loadModel(): TypeBuilder = {

    val md = connection.getMetaData

    val tables = md.getTables(importConfigStr(Catalog), importConfigStr(Schema), null, Array("TABLE"))

    val names = new ListBuffer[String]()
    while (tables.next()) {
      names += tables.getString("TABLE_NAME")
    }

    val ns = NamespaceNode(nameNode = StringNode.create(importConfigStr(Namespace)))

    new TypeBuilder(connection, ns, names)
  }


  override def supportedConfig(): Array[String] = requiredConfig()

  override def name: String = "JdbcImporter"

  override def getDefinitions(): Set[String] = tBuilder.cua.getUdtVersionNames().map(_.fullyQualifiedName)

  override def getDefinition(name: String): Option[IUdtBaseNode] = tBuilder.cua.getUdt(name)

  override def importSchema(): List[Path] = List.empty

  def connect(): Connection = {
    val url = importConfigStr(Url)
    val username = importConfigStr(User)
    val password = importConfigStr(Password)
    val conn = DriverManager.getConnection(url, username, password)
    conn
  }

  override def requiredConfig(): Array[String] = Array(Catalog, Schema, Namespace, Url, User, Password)
}
