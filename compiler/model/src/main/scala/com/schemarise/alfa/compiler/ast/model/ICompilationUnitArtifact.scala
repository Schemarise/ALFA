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
package com.schemarise.alfa.compiler.ast.model

import com.schemarise.alfa.compiler.ast.model.graph.{DefaultCompilationUnitArtifactGraph, GraphReachabilityScopeType, ICompilationUnitArtifactGraph}
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.types.Nodes.NodeType

import scala.collection.JavaConversions

trait ICompilationUnitArtifact extends INode {
  def scopedGraph(exportScopeType: GraphReachabilityScopeType, reversedIncludes: Boolean = false): ICompilationUnitArtifactGraph

  def getTransformersTo(str: String): List[ITransform]

  def getTransformers(): List[ITransform]

  def hasErrors: Boolean

  def hasWarnings: Boolean

  def hashForScripts: String

  def getErrors: Seq[IResolutionMessage]

  def getErrorsAsList = JavaConversions.asJavaCollection(getErrors)

  def getWarnings: Seq[IResolutionMessage]

  def graph: ICompilationUnitArtifactGraph

  def graphWithReversedIncludes: ICompilationUnitArtifactGraph

  def getUdt(qualifiedName: String, allowNameOnlyMatch: Boolean = false): Option[IUdtBaseNode]

  def getUdtWithParams(qualifiedName: String, paramNames: Seq[String]): Option[IUdtBaseNode]

  def getUdtVersionNames(): Set[IUdtVersionName]

  def getNamespaces(): Seq[INamespaceNode]

  def getNamespaceMeta(namespace: String): Option[IDocAndAnnotated]

  def getLatestModifiedFileTimestamp(): Long
}

class DefaultCompilationUnitArtifact extends ICompilationUnitArtifact {
  override def hasErrors: Boolean = false

  override def hasWarnings: Boolean = false

  override def getErrors: Seq[IResolutionMessage] = Nil

  override def getWarnings: Seq[IResolutionMessage] = Nil

  override def graph: ICompilationUnitArtifactGraph = new DefaultCompilationUnitArtifactGraph()

  override def graphWithReversedIncludes: ICompilationUnitArtifactGraph = new DefaultCompilationUnitArtifactGraph()

  override def getUdt(qualifiedName: String, allowNameOnlyMatch: Boolean = false): Option[IUdtBaseNode] = None

  override def getUdtWithParams(qualifiedName: String, paramNames: Seq[String]) = None

  override def getUdtVersionNames(): Set[IUdtVersionName] = Set()

  override def getNamespaces(): Seq[INamespaceNode] = Nil

  override def getNamespaceMeta(namespace: String): Option[IDocAndAnnotated] = None

  override def traverse(v: NodeVisitor): Unit = {}

  override def getTransformersTo(str: String): List[ITransform] = List.empty

  override def getTransformers(): List[ITransform] = List.empty

  override def getLatestModifiedFileTimestamp(): Long = 0

  override def hashForScripts: String = "" + System.currentTimeMillis()

  override def scopedGraph(exportScopeType: GraphReachabilityScopeType, reversedIncs: Boolean): ICompilationUnitArtifactGraph = new DefaultCompilationUnitArtifactGraph()

  override val location: IToken = null
}