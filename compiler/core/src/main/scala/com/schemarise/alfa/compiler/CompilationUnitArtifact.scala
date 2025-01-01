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

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.graph.{GraphReachabilityScopeType, ICompilationUnitArtifactGraph}
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.ast.{BaseNode, Namespace, TraversableNode}
import com.schemarise.alfa.compiler.err.{ExpressionError, UnsupportedNamespace}
import com.schemarise.alfa.compiler.tools.graph.CompilationUnitArtifactGraph
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, ILogger, TokenImpl}

import java.nio.file.Path

object CompilationUnitArtifact {

  def filtered(cua: ICompilationUnitArtifact, filterTypes: Option[List[String]]): ICompilationUnitArtifact = {
    // TODO implement
    cua
  }
}

class CompilationUnitArtifact(ctx: Context, var compilationUnit: CompilationUnit) extends BaseNode with TraversableNode with ICompilationUnitArtifact {

  try {
    compilationUnit.startPreResolve(ctx, this)
    compilationUnit.startResolve(ctx)
    compilationUnit.startPostResolve(ctx)
  } catch {
    case e: java.lang.Throwable =>
      val msg = "Fatal error resolving CompilationUnitArtifact : " + e.getMessage
      e.printStackTrace()
      ctx.logger.error(msg, e)
  }


  override def toString: String = compilationUnit.toString

  def getNamespace(str: String): Option[NamespaceNode] =
    compilationUnit.namespaces.filter(_.nodeId.id.equals(str)).headOption

  def settings = ctx.allSettings

  private[alfa] def context = ctx

  override def nodeType = Nodes.CompilationUnitArtifactNode

  override val location = compilationUnit.location

  override def hasErrors = ctx.getErrors().size != 0

  override def hasWarnings = ctx.getWarnings().size != 0

  override def getErrors = ctx.getErrors()

  override def getWarnings = ctx.getWarnings()

  private[alfa] def writeAsZipModule(logger: ILogger, modulesDir: Path, includeGroup: Boolean): Path =
    AlfaModuleWriter.writeAsZipModule(ctx, logger, graph, modulesDir, includeGroup)

  private[alfa] def writeAsZipModule(logger: ILogger, zipFile: Path): Unit =
    AlfaModuleWriter.writeAsZipModule(ctx, logger, graph, zipFile)

  private[alfa] def writeAsFileSystemModule(logger: ILogger, modulesDir: Path) =
    AlfaModuleWriter.writeAsFileSystemModule(ctx, logger, graph, modulesDir)

  def getNamespaceMeta(namespace: String) = {
    ctx.registry.getNamespaceMeta(Namespace(namespace))
  }

  def getUdtWithParams(qualifiedName: String, paramNames: Seq[String]): Option[IUdtBaseNode] = {
    ctx.registry.getUdt(None, UdtDataType.fromNameAndParam(qualifiedName, paramNames), false)
  }


  def getUdt(qualifiedName: String, allowNameOnlyMatch: Boolean = false) =
    ctx.registry.getUdt(
      requestor = None,
      ref = new UdtDataType(name = StringNode.create(qualifiedName)),
      logError = false,
      allowNameOnlyMatch = allowNameOnlyMatch)

  override lazy val graph: CompilationUnitArtifactGraph = _graph(false)
  override lazy val graphWithReversedIncludes: CompilationUnitArtifactGraph = _graph(true)

  private def _graph(reverseIncludes: Boolean) =
    new CompilationUnitArtifactGraph(ctx, this, compilationUnit, reverseIncludes, GraphReachabilityScopeType.localandreachable)

  override def traverse(v: NodeVisitor): Unit =
    compilationUnit.traverse(v)

  // TODO Should have better filter to exclude internal types

  override def getUdtVersionNames(): Set[IUdtVersionName] =
    ctx.registry.getUdtVersionNames().filter(e => BuiltinModelTypes.DoesNotInclude(e.fullyQualifiedName))

  override def getNamespaces(): Seq[INamespaceNode] =
    ctx.registry.getAllNamespaces().filter(e => BuiltinModelTypes.DoesNotInclude(e.name))


  def getCompilationUnitNamespaces() =
    compilationUnit.namespaces

  override def getTransformersTo(str: String): List[ITransform] = {
    val u = getUdt(str).get
    ctx.registry.getTransformersTo(u)
  }

  def getTransformers(): List[ITransform] = {
    ctx.registry.getTransformers()
  }

  override def getLatestModifiedFileTimestamp(): Long = ctx.latestModifiedFileTime()

  override def hashForScripts: String = ctx.getScriptsMD5

  override def scopedGraph(exportScopeType: GraphReachabilityScopeType, reversedIncludes: Boolean = false): ICompilationUnitArtifactGraph =
    new CompilationUnitArtifactGraph(ctx, this, compilationUnit, reversedIncludes, exportScopeType)

}

