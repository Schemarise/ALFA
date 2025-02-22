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

package com.schemarise.alfa.generators.importers.java

import com.schemarise.alfa.compiler.ast.NodeMeta

import java.nio.file.{Files, Path}
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, NamespaceNode, StringNode}
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, Context}
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams, GeneratorException}
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import java.net.URLClassLoader
import scala.collection.JavaConverters._

object JavaClassImporter {
  val ImportPackageFilter = "importPackageFilter"
  val ImportClassBaseType = "importClassBaseType"
}
class JavaClassImporter(param: AlfaImporterParams) extends AlfaImporter(param) {

  private val ctx = new Context()
  override def writeTopComment() = false

  private def processInputClasses(): Unit = {

    val pkgs = param.importConfig.get(JavaClassImporter.ImportPackageFilter).toString.split(",")
    val urls = pkgs.map( s => ClasspathHelper.forPackage(s).asScala.head )

    val reflections = new Reflections(new ConfigurationBuilder().setUrls(urls:_*))

    val baseClassName = param.importConfig.get(JavaClassImporter.ImportClassBaseType)
    val baseClass = if ( baseClassName != null ) Some( Class.forName(baseClassName.toString) ) else None

    val targetClasses = reflections.getAll(Scanners.SubTypes).asScala
      .map( n => Class.forName(n))
      .filter(e => !e.isInterface && !java.lang.reflect.Modifier.isAbstract(e.getModifiers))
      .filter( e => baseClass.isEmpty || baseClass.get.isAssignableFrom(e) )

    val classLoader = new URLClassLoader(
      Array(param.rootPath.toUri.toURL),
      this.getClass().getClassLoader()
    )

    val tBuilder = new AlfaTypeBuilder(logger, ctx, classLoader, targetClasses.map( _.getName).toList )
    tBuilder.genAlfaModel()

    writeAlfaFiles()
  }

  def writeAlfaFiles() = {

    val types = ctx.registry.allUserDeclarations

    val udts = types.map(t => ctx.registry.getUdt(None, UdtDataType.fromName(t), false).get).toList


    val nn = new NamespaceNode(collectedUdts = udts)
    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    val cua = new CompilationUnitArtifact(ctx, cu)

    ctx.registry.getAllNamespaces().foreach( nsz => {

      val nns = ctx.registry.getNamespaceDecls(nsz)
      val nsWithDoc = nns.filter(z => ! z.docs.isEmpty).headOption

      if ( nsWithDoc.isDefined ) {
        enterFile( nsz.name + ".alfa")
        writeln(nsWithDoc.get.toString)
        exitFile()
      }
    })

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

  override def supportedConfig(): Array[String] = Array(JavaClassImporter.ImportPackageFilter, JavaClassImporter.ImportClassBaseType)

  override def requiredConfig(): Array[String] = Array(JavaClassImporter.ImportPackageFilter)

  override def name: String = "java"

  override def getDefinitions(): Set[String] = Set.empty

  override def getDefinition(name: String): Option[IUdtBaseNode] = None

  override def importSchema(): List[Path] = {
    processInputClasses()

    List.empty
  }

}
