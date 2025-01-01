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
package com.schemarise.alfa.compiler.tools.graph

import com.schemarise.alfa.compiler.ast.model.graph.{GraphReachabilityScopeType, ICompilationUnitArtifactGraph, IGraphEdge}
import com.schemarise.alfa.compiler.ast.model.{IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, UdtBaseNode}
import com.schemarise.alfa.compiler.ast.{Namespace, UdtVersionedName}
import com.schemarise.alfa.compiler.err.{ResolutionMessage, TopologicalTraversalCycles}
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, Context}

import java.util.function.Predicate
import scala.collection.JavaConverters.asScalaSetConverter
import scala.util.{Failure, Success, Try}

class CompilationUnitArtifactGraph(
                                    ctx: Context,
                                    cua: CompilationUnitArtifact,
                                    compilationUnit: CompilationUnit,
                                    reverseIncludesEdge: Boolean,
                                    exportScopeType: GraphReachabilityScopeType
                                  ) extends ICompilationUnitArtifactGraph {
  private val directedGraph = new DirectedGraphBuilder(ctx, compilationUnit, reverseIncludesEdge, exportScopeType)

  def traverse(v: NodeVisitor): Unit = compilationUnit.traverse(v)

  def shortestPath(start: UdtBaseNode, end: UdtBaseNode): List[UdtVersionedName] =
    shortestPath(new Vertex(start), new Vertex(end)).map(_.node.asInstanceOf[UdtBaseNode].name)

  def shortestPath(start: Vertex, end: Vertex) = {
    if (start.equals(end)) {
      List(start)
    }
    else {
      directedGraph.shortestPath(start, end)
    }
  }

  override def topologicalOrPermittedOrdered(): Try[Seq[IdentifiableNode]] = {
    if (ctx.allSettings.compile.DisallowCycles && directedGraph.hasCycles) {
      ctx.addResolutionError(new ResolutionMessage(cua.location, TopologicalTraversalCycles)(None, List.empty, directedGraph.cycleNamesWithPaths))
      new Failure(new IllegalStateException)
    }
    else {
      val g = if (directedGraph.hasCycles)
        directedGraph.breadthFirstIteratorOrdered(directedGraph.graph)
      else
        directedGraph.topologicallyOrdered(directedGraph.graph)

      Success(g.map(_.node))
    }
  }

  override def hasUserDefinedTypeCycles = directedGraph.hasCycles

  override def outgoingEdgeNodes(from: IdentifiableNode, filter: Predicate[IGraphEdge]): Seq[IdentifiableNode] = {
    directedGraph.outgoingEdgeNodes(directedGraph.graph, from.asInstanceOf[IdentifiableNode], filter)
  }

  override def incomingEdgeNodes(to: IdentifiableNode, filter: Predicate[IGraphEdge]): Seq[IdentifiableNode] = {
    directedGraph.incomingEdgeNodes(directedGraph.graph, to.asInstanceOf[IdentifiableNode], filter)
  }

  override def namespacesHasCycles = directedGraph.namespaceHasCycles

  override def namespacesTopologicallyOrPermittedOrdered(): Try[Seq[Namespace]] = {
    if (ctx.allSettings.compile.DisallowNamespaceCycles && directedGraph.namespaceHasCycles) {
      ctx.addResolutionError(new ResolutionMessage(cua.location, TopologicalTraversalCycles)(None, List.empty, directedGraph.namespaceCycleNamesWithPaths))
      new Failure(new IllegalStateException)
    }
    else {
      val g = if (directedGraph.namespaceHasCycles)
        directedGraph.breadthFirstIteratorOrdered(directedGraph.namespaceGraph)
      else
        directedGraph.topologicallyOrdered(directedGraph.namespaceGraph)

      val s = g.
        filter(_.node.isInstanceOf[Namespace]).
        map(_.node.asInstanceOf[Namespace])
      Success(s)
    }
  }

  override def namespaceOutgoingEdgeNodes(from: IdentifiableNode, filter: Predicate[IGraphEdge]): Seq[IdentifiableNode] = {
    directedGraph.outgoingEdgeNodes(directedGraph.namespaceGraph, from.asInstanceOf[IdentifiableNode], filter)
  }

  def filterEdges(filter: Predicate[IGraphEdge]): Seq[IGraphEdge] = {
    directedGraph.graph.edgeSet.asScala.filter(e => filter.test(e)).toSeq
  }

  def debugGui() = {
    directedGraph.debugUI()
  }
}
