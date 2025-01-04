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
package com.schemarise.alfa.compiler

import com.schemarise.alfa.compiler.ast.model.graph.ICompilationUnitArtifactGraph
import com.schemarise.alfa.compiler.ast.nodes.datatypes.TypeDefedDataType
import com.schemarise.alfa.compiler.ast.nodes.{Field, UdtBaseNode}
import com.schemarise.alfa.compiler.err.CompilerSettingsException
import com.schemarise.alfa.compiler.tools.AlfaPath
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, ILogger, VFS}

import java.io.FileOutputStream
import java.nio.file._
import java.util.zip.ZipOutputStream
import scala.collection.mutable.ListBuffer

object AlfaModuleWriter {

  private[alfa] def writeAsFileSystemModule(ctx: Context, logger: ILogger, graph: ICompilationUnitArtifactGraph, modulesDir: Path) = {
    VFS.mkdir(modulesDir)
    writeModule(logger, modulesDir, ctx, graph)
  }

  private[alfa] def writeAsZipModule(ctx: Context, logger: ILogger, graph: ICompilationUnitArtifactGraph,
                                     tgtArchive: Path) = {
    _writeAsZipModule(ctx, logger, graph, tgtArchive)
  }

  private[alfa] def writeAsZipModule(ctx: Context, logger: ILogger, graph: ICompilationUnitArtifactGraph,
                                     modulesDir: Path, includeGroupInName: Boolean): Path = {
    if (!ctx.allSettings.projectId.isDefined)
      throw new CompilerSettingsException("Cannot write zip module, an Alfa project file required with a ProjectId specified")

    VFS.mkdir(modulesDir)

    val tgtArchive: Path = modulesDir.resolve(ctx.allSettings.projectId.get.asGroupAndNameAndVersionZipFileName(includeGroupInName))
    _writeAsZipModule(ctx, logger, graph, tgtArchive)
    tgtArchive
  }

  private def _writeAsZipModule(ctx: Context, logger: ILogger, graph: ICompilationUnitArtifactGraph, tgtArchive: Path) = {

    val tmpZip = Files.createTempFile("AlfaModule", ".zip")
    new ZipOutputStream(new FileOutputStream(tmpZip.toFile)).close()

    val fs = FileSystems.newFileSystem(tmpZip, null.asInstanceOf[ClassLoader])
    writeModule(logger, fs.getPath("/"), ctx, graph)

    fs.close

    if (tgtArchive.getParent != null)
      VFS.mkdir(tgtArchive.getParent)

    if (Files.exists(tgtArchive))
      Files.delete(tgtArchive)

    Files.copy(tmpZip, tgtArchive)

    Files.delete(tmpZip)
  }

  private[alfa] def writeModule(logger: ILogger, pathRoot: Path, ctx: Context, graph: ICompilationUnitArtifactGraph) = {
    val entryNames = new ListBuffer[String]()

    graph.topologicalOrPermittedOrdered.get.
      filter(_.isInstanceOf[UdtBaseNode]).
      map(e => e.asInstanceOf[UdtBaseNode]).
      filter(e => BuiltinModelTypes.DoesNotInclude(e.name.fullyQualifiedName)).
      foreach(e => writeUDT(logger, e, entryNames, pathRoot))

    ctx.registry.getDataproducts().values.foreach(dp => {
      writeUDT(logger, dp, entryNames, pathRoot)
    })

    val sb = new StringBuilder()

    val fields: List[Field] = ctx.registry.fields()
    val typeDefs: List[TypeDefedDataType] = ctx.registry.typedefs()

    // TODO this printing should be done by code aware of grammar
    if (fields.size > 0) {
      sb.append("fields {\n")
      fields.foreach(f => sb.append("  " + f.toString + "\n"))
      sb.append("}\n")
    }

    if (typeDefs.size > 0) {
      sb.append("\ntypedefs {\n")
      typeDefs.foreach(f => sb.append("  " + f.toString + "\n"))
      sb.append("}\n")
    }


    if (sb.size > 0)
      writeEntry(pathRoot, AlfaPath.GlobalDefsFile, sb.toString)

    val csm = ctx.allSettings.toYaml()
    writeEntry(pathRoot, AlfaPath.CompilerSettingsPath, csm)
    writeEntry(pathRoot, AlfaPath.ZipIndexFile, entryNames.mkString("", "\n", ""))
  }

  private def writeUDT(logger: ILogger, e: UdtBaseNode, entryNames: ListBuffer[String], pathRoot: Path): Unit = {
    // do not write if loaded from repo
    if (!e.isSynthetic && e.writeAsModuleDefinition && e.repositoryEntry.isEmpty) {
      entryNames += e.versionedName.fullyQualifiedName
      val path = e.versionedName.asUnixPath(".alfa")
      logger.debug(s"Writing: ${e.name.udtType}")
      writeEntry(pathRoot, path, e.toString)
    } else
      logger.debug(s"Skipped writing: ${e.name.udtType}")
  }

  private def writeEntry(pathRoot: Path, filePath: String, contents: String): Unit = {
    val p = pathRoot.resolve(filePath)

    if (p.getParent != null && !Files.exists(p.getParent))
      VFS.mkdir(p.getParent)


    Files.write(p, contents.getBytes, StandardOpenOption.CREATE)
  }
}
