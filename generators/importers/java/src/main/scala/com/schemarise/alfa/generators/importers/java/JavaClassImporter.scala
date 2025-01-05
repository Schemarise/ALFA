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

package com.schemarise.alfa.generators.exporters.java

import java.io.File
import java.net.{URL, URLClassLoader}
import java.nio.file.{Files, Path}
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, NamespaceNode, StringNode}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, Context}
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, GeneratorException}
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._

class JavaClassImporter(param: AlfaImporterParams) extends AlfaImporter(param) {

  private var errored = false
  val ctx = new Context()

  if (errored)
    throw new GeneratorException("Missing settings. See log messages.")

  processInputClasses()

  override def writeTopComment() = false

  private def processInputClasses() = {

    val classLoader = new URLClassLoader(
      Array(param.rootPath.toUri.toURL),
      this.getClass().getClassLoader()
    )

    val rootCanonical = param.rootPath.toFile.getCanonicalPath

    val classFiles = FileUtils.listFiles(param.rootPath.toFile, Array("class"), true)

    val classNames = classFiles.asScala.map(m => {
      val subPath = m.getCanonicalPath.substring(rootCanonical.length + 1)
      subPath.replace(File.separator, ".").dropRight(".class".length)
    }).toSeq.filter(e => {
      val f = importConfigStr("filter")

      if (f != null && f.trim.length > 0) {
        val nspaces = f.split(",")
        nspaces.filter(ns => e.startsWith(ns)).length > 0
      }
      else
        true
    })


    val tBuilder = new AlfaTypeBuilder(logger, ctx, classLoader, classNames)
    tBuilder.genAlfaModel()

    writeAlfaFiles()
  }

  private def alfaNamespace = {
    var ns = importConfigStr("namespace")

    if (ns == null)
      ns = "defaultnamespace"

    ns
  }

  def writeAlfaFiles() = {

    val types = ctx.registry.allUserDeclarations

    val udts = types.map(t => ctx.registry.getUdt(None, UdtDataType.fromName(t), false).get).toList


    val nn = new NamespaceNode(collectedUdts = udts, nameNode = StringNode.create(alfaNamespace))
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    val cua = new CompilationUnitArtifact(ctx, cu)

    udts.foreach(u => {
      val tn = u.name.fullyQualifiedName

      enterFile(tn + ".alfa")
      writeln(
        s"""
           |${u.toString}
         """.stripMargin)
      exitFile()

      logger.debug("Generated " + tn)
    })
  }

  override def supportedConfig(): Array[String] = Array("namespace")

  override def requiredConfig(): Array[String] = Array("namespace")

  override def name: String = "java"

  override def getDefinitions(): Set[String] = Set.empty

  override def getDefinition(name: String): Option[IUdtBaseNode] = None

  override def importSchema(): List[Path] = List.empty

}
