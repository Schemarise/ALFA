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

import com.jgraph.layout.JGraphFacade
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.graph.Edges.EdgeType
import com.schemarise.alfa.compiler.ast.model.graph.{Edges, GraphReachabilityScopeType, IGraphEdge}
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import org.jgraph.JGraph
import org.jgrapht.GraphPath
import org.jgrapht.alg.CycleDetector
import org.jgrapht.alg.shortestpath.{AllDirectedPaths, KShortestPaths}
import org.jgrapht.ext.JGraphModelAdapter
import org.jgrapht.traverse.{BreadthFirstIterator, TopologicalOrderIterator}

import java.util
import javax.swing.{JFrame, JScrollPane}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DirectedGraphBuilder(ctx: Context, cu: CompilationUnit, reverseIncludesEdge: Boolean,
                           exportScopeType: GraphReachabilityScopeType) {


  private[graph] val graph = new AlfaDirectedGraph[Vertex, Edge](classOf[Edge])
  private[graph] val namespaceGraph = new AlfaDirectedGraph[Vertex, Edge](classOf[Edge])
  private val gb = new GraphBuilder

  // This is GraphReachabilityScopeType and reachable
  cu.traverse(gb)


  // Add all if reqd
  if (exportScopeType == GraphReachabilityScopeType.all) {

    ctx.registry.getUdtVersionNames().foreach(u => {

      val udt = ctx.registry.getUdtByVersionedName(u)

      val externals = ListBuffer[UdtBaseNode]()

      if (udt.isDefined && udt.get.isLoadedFromRepository && !graph.containsVertex(new Vertex(udt.get))) {
        externals.append(udt.get)
      }

      val grouped = externals.groupBy(e => e.namespaceNode)
      grouped.foreach(e => {
        gb.enter(e._1)
        ctx.logger.debug("ExportScope: All. Adding (repo) types " + e._2.map(v => v.name).mkString(", "))

        e._2.foreach(x => x.traverse(gb))
        gb.exit(e._1)
      })
    })
  }

  def shortestPath(start: Vertex, end: Vertex) = {
    val pathInspector = new KShortestPaths[Vertex, Edge](graph, Integer.MAX_VALUE, Integer.MAX_VALUE)
    val paths = pathInspector.getPaths(start, end)
    val l = paths.asScala.toList
    val vs = l.map(e => e.getVertexList.asScala).flatten
    vs
  }

  private[graph] def topologicallyOrdered(g: AlfaDirectedGraph[Vertex, Edge]) =
    new TopologicalOrderIterator[Vertex, Edge](g).asScala.toList

  private[graph] def breadthFirstIteratorOrdered(g: AlfaDirectedGraph[Vertex, Edge]) =
    new BreadthFirstIterator[Vertex, Edge](g).asScala.toList

  private val cycles = new CycleDetector[Vertex, Edge](graph).findCycles

  val hasCycles = cycles.size() > 0

  def namespaceCycleNamesWithPaths = {
    _cycleNamesWithPaths(namespaceCycles.asScala.toSeq, namespaceGraph)
  }

  def cycleNamesWithPaths = {
    _cycleNamesWithPaths(cycles.asScala.toSeq, graph)
  }

  private def _cycleNamesWithPaths(seqCycle: Seq[Vertex], gr: AlfaDirectedGraph[Vertex, Edge]) = {

    val tup = (seqCycle.last, seqCycle.head)
    val pairs = toPairs[Vertex](seqCycle) :+ tup

    pairs.map(p => {
      val dd = new AllDirectedPaths[Vertex, Edge](gr)
      val paths: util.List[GraphPath[Vertex, Edge]] = dd.getAllPaths(p._1, p._2, true, 100)

      paths.asScala.map(p => p.getStartVertex.node.nodeId.id + " > " + p.getEndVertex.node.nodeId.id + " " +
        p.getEdgeList.asScala.map(e => e.getType.toString).mkString("[", ", ", "]")).mkString(", ")
    }).mkString(" | ")
  }

  private def toPairs[A](xs: Seq[A]): Seq[(A, A)] = xs.zip(xs.tail)


  private val namespaceCycles = new CycleDetector[Vertex, Edge](namespaceGraph).findCycles
  val namespaceHasCycles = namespaceCycles.size() > 0

  private[graph] def outgoingEdgeNodes(g: AlfaDirectedGraph[Vertex, Edge],
                                       from: IdentifiableNode,
                                       filter: java.util.function.Predicate[IGraphEdge]): Seq[IdentifiableNode] = {
    val visited = new mutable.ListBuffer[IdentifiableNode]()
    outgoingEdgeNodes(g, from, filter, visited)
    visited
  }

  private[graph] def outgoingEdgeNodes(g: AlfaDirectedGraph[Vertex, Edge],
                                       from: IdentifiableNode,
                                       filter: java.util.function.Predicate[IGraphEdge],
                                       visited: mutable.ListBuffer[IdentifiableNode]): Unit = {
    val v = new Vertex(from)

    if (g.containsVertex(v)) {
      val vv = g.outgoingEdgesOf(v)

      vv.stream().filter(filter).iterator().asScala.map(_.getTgt).foreach(e => {
        if (!visited.contains(e)) {
          visited += e
          outgoingEdgeNodes(g, e, filter, visited)
        }
      })
    }
    else {
      //      println("No outgoing vertices from " + from)
    }
  }

  private[graph] def incomingEdgeNodes(g: AlfaDirectedGraph[Vertex, Edge],
                                       from: IdentifiableNode,
                                       filter: java.util.function.Predicate[IGraphEdge]): Seq[IdentifiableNode] = {
    val visited = new mutable.ListBuffer[IdentifiableNode]()
    incomingEdgeNodes(g, from, filter, visited)
    visited
  }

  private def incomingEdgeNodes(g: AlfaDirectedGraph[Vertex, Edge],
                                from: IdentifiableNode,
                                filter: java.util.function.Predicate[IGraphEdge], visited: mutable.ListBuffer[IdentifiableNode]): Unit = {

    if (graph.containsVertex(new Vertex(from)) && !from.isInstanceOf[INativeUdt]) {
      val vv = graph.incomingEdgesOf(new Vertex(from))

      vv.stream().filter(filter).iterator().asScala.map(_.getSrc).foreach(e => {
        if (!visited.contains(e)) {
          visited += e
          incomingEdgeNodes(g, e, filter, visited)
        }
      })
    }
  }

  class GraphBuilder extends NoOpUdtVisitor {
    private val edgeContext = new util.Stack[EdgeType]()
    private val parentContext = new util.Stack[Vertex]()
    private var currentNamespaceDef: Vertex = null

    override def exit(e: INamespaceNode): Unit = {
      parentContext.pop()
      edgeContext.pop()
    }

    override def enter(arg: INamespaceNode): Mode = {
      val e = arg.asInstanceOf[NamespaceNode]

      val v = new Vertex(e)
      graph.addVertex(v)
      parentContext.push(v)
      edgeContext.push(Edges.NamespaceToUdt)

      currentNamespaceDef = v

      // update namespace graph - this is repeated in enterUdt, but some
      // namspace may be declared with no immediate UDTs
      if (e.nodeId.id.length > 0) {
        val srcNs = new Vertex(Namespace(e.nodeId.id))
        namespaceGraph.addVertex(srcNs)
      }

      NodeVisitMode.Continue
    }

    override def enterUdt(u: IUdtBaseNode): Mode = {

      // if local only do not traverse repo types
      if (exportScopeType == GraphReachabilityScopeType.localonly && u.isLoadedFromRepository) {
        ctx.logger.debug("ExportScope: LocalOnly. Excluding (repo) type " + u.name)
        return NodeVisitMode.Break
      }


      if (!u.isFragment) {
        val e = u.asInstanceOf[UdtBaseNode]
        val v = new Vertex(e)
        graph.addVertex(v)

        // update namespace graph
        val srcNs = new Vertex(e.versionedName.namespace)
        namespaceGraph.addVertex(srcNs)
        namespaceGraph.addVertex(v)

        namespaceGraph.addEdge(srcNs, v, namespaceGraph.getEdgeFactory.createEdge(srcNs, v).setData(srcNs, v, Edges.NamespaceToUdt))

        val src = parentContext.peek()
        createEdge(src, v)

        parentContext.push(v)

        u.annotations.map(a => {

          val annTgt = new Vertex(a.asInstanceOf[Annotation].resolvedAnnotationDeclType.get)
          graph.addVertex(annTgt)
          graph.addEdge(v, annTgt, graph.getEdgeFactory.createEdge(v, annTgt).setData(v, annTgt, Edges.UdtToAnnotation))
        })

        e.allAccessibleFields().values.filter(f => u.localFieldNames.contains(f.name)).map(f => {
          f.annotations.map(a => {
            val annTgt = new Vertex(a.resolvedAnnotationDeclType.get)
            val fv = new Vertex(f)
            graph.addVertex(fv)
            graph.addVertex(annTgt)
            val ed = graph.getEdgeFactory.createEdge(fv, annTgt).setData(fv, annTgt, Edges.FieldToAnnotation)
            graph.addEdge(fv, annTgt, ed)
          })
        })

        edgeContext.push(Edges.Includes)
        e.includes.foreach(_.traverse(this))
        edgeContext.pop

        edgeContext.push(Edges.Scope)
        if (e.isTrait) {
          e.asInstanceOf[Trait].scope.foreach(_.traverse(this))
        }

        edgeContext.pop


        edgeContext.push(Edges.Extends)
        e.extendsDef.map(x =>
          x.traverse(this))
        edgeContext.pop


        edgeContext.push(Edges.UdtToFieldDataType)
        e.allAccessibleFields().values.foreach(_.traverse(this))
        edgeContext.pop

        if (e.isInstanceOf[Service]) {
          val service = e.asInstanceOf[Service]

          edgeContext.push(Edges.UdtToFieldDataType)
          service.constructorFormals.values.foreach(f => f.traverse(this))

          service.getMethodSignatures.foreach(f => {
            f._2.traverse(this)

            f._2.annotations.map(a => {
              val annTgt = new Vertex(a.asInstanceOf[Annotation].resolvedAnnotationDeclType.get)
              val fv = new Vertex(f._2.asInstanceOf[MethodSignature])
              graph.addVertex(fv)
              graph.addVertex(annTgt)
              val ed = graph.getEdgeFactory.createEdge(fv, annTgt).setData(fv, annTgt, Edges.MethodToAnnotation)
              graph.addEdge(fv, annTgt, ed)
            })
          })
          edgeContext.pop
        }

        if (e.isInstanceOf[Library]) {
          val lib = e.asInstanceOf[Library]

          edgeContext.push(Edges.UdtToFieldDataType)
          lib.methodDecls.foreach(f => f.traverse(this))
          edgeContext.pop
        }

        if (e.isInstanceOf[Testcase]) {
          val tc = e.asInstanceOf[Testcase]

          edgeContext.push(Edges.UdtToFieldDataType)
          tc.methodDecls.foreach(f => f.traverse(this))
          edgeContext.pop

          if (tc.targetUdt.isDefined) {
            edgeContext.push(Edges.TestTarget)
            val kv = new Vertex(tc.targetUdt.get)
            createEdge(v, kv)
            edgeContext.pop
          }
        }

        if (e.isInstanceOf[Entity]) {
          val entity = e.asInstanceOf[Entity]
          if (!entity.isSingleton) {
            val key = entity.keyType.get.resolvedType.get.asInstanceOf[Key]

            val kv = new Vertex(key)
            graph.addVertex(kv)
            edgeContext.push(Edges.EntityToDirectKey)
            createEdge(v, kv)
            edgeContext.pop

            // the top level key needs to be included into a UDT's one
            if (key.includes.size > 0) {
              val incKeyUdt = key.includes.head.udt
              val kv = new Vertex(incKeyUdt)
              graph.addVertex(kv)
              edgeContext.push(Edges.EntityToUDTKey)
              createEdge(v, kv)
              edgeContext.pop
            }
          }
        }

        if (e.isInstanceOf[UdtBaseNode]) {
          val udtOut = e.asInstanceOf[UdtBaseNode]
          val trans = ctx.registry.getTransformersTo(udtOut)
          trans.foreach(t => {
            val tv = new Vertex(t)
            graph.addVertex(tv)
            createEdge(tv, v, Edges.TransformerOutput)
          })
        }

        parentContext.pop()

        val udtv = new Vertex(u.asInstanceOf[UdtBaseNode])
        if (graph.containsVertex(udtv) && !u.isSynthetic) {
          val udtedges = graph.outgoingEdgesOf(udtv).asScala.filter(e => e.getType == Edges.UdtToFieldDataType)

          // add links to UDTs via synthetic links
          udtedges.
            filter(_.getTgt.isInstanceOf[UdtBaseNode]).
            map(_.getTgt.asInstanceOf[UdtBaseNode]).
            filter(_.isSynthetic).
            foreach(e => addUdtEdgeToAnonymousPaths(udtv, new Vertex(e))
            )
        }
      }

      NodeVisitMode.Break
    }

    override def enter(arg: IEnclosingDataType): Mode = {
      val e = arg.asInstanceOf[EnclosingDataType]

      //      if ( e.encType == Enclosed.key ) {
      //
      //        val entity = e.componentType.asInstanceOf[UdtDataType].resolvedType.get.asInstanceOf[Entity]
      //        if ( ! entity.isSingleton ) {
      //          val v = new Vertex(entity.keyType.get.resolvedType.get)
      //          graph.addVertex(v)
      //          val src = parentContext.peek()
      //          createEdge( src, v )
      //        }
      //
      //        NodeVisitMode.Break
      //      }
      //      else
      {
        NodeVisitMode.Continue
      }
    }

    override def enter(arg: ITupleDataType): Mode = {
      val e = arg.asInstanceOf[TupleDataType]
      parentContext.push(currentNamespaceDef)
      edgeContext.push(Edges.NamespaceToUdt)

      enterUdt(e.syntheticRecord)

      parentContext.pop()
      edgeContext.pop()

      NodeVisitMode.Continue
    }

    override def enter(arg: IUnionDataType): Mode = {
      val e = arg.asInstanceOf[UnionDataType]
      parentContext.push(currentNamespaceDef)
      edgeContext.push(Edges.NamespaceToUdt)

      enterUdt(e.syntheticUnion)

      parentContext.pop()
      edgeContext.pop()

      NodeVisitMode.Continue
    }

    override def enter(arg: IEnumDataType): Mode = {
      val e = arg.asInstanceOf[EnumDataType]
      parentContext.push(currentNamespaceDef)
      edgeContext.push(Edges.NamespaceToUdt)

      enterUdt(e.syntheticEnum())

      parentContext.pop()
      edgeContext.pop()

      NodeVisitMode.Continue
    }

    override def enter(arg: IUdtDataType): Mode = {

      val e = arg.asInstanceOf[UdtDataType]
      val u = e.resolvedType.get

      if (exportScopeType == GraphReachabilityScopeType.localonly && u.isLoadedFromRepository) {
        ctx.logger.debug("ExportScope: LocalOnly. Excluding (repo) type " + u.name)
        return NodeVisitMode.Break
      }

      val v = new Vertex(u)
      val added = graph.addVertex(v)

      val src = parentContext.peek()
      createEdge(src, v)
      NodeVisitMode.Break
    }

    private def createEdge(src: Vertex, tgt: Vertex): Unit = {
      createEdge(src, tgt, edgeContext.peek())
    }

    private def createEdge(src: Vertex, tgt: Vertex, edgeType: EdgeType): Boolean = {

      // println( src.node.Id + " -- " + edgeType + " --> " + tgt.node.Id )

      val added = if (reverseIncludesEdge && edgeType == Edges.Includes) {
        val e = graph.getEdgeFactory.createEdge(tgt, src).setData(tgt, src, edgeType)
        GraphUtils.safeAddEdge[Vertex, Edge](graph, tgt, src, e)
      }
      else {
        val e = graph.getEdgeFactory.createEdge(src, tgt).setData(src, tgt, edgeType)
        GraphUtils.safeAddEdge[Vertex, Edge](graph, src, tgt, e)
      }

      // build NS graph
      if (edgeType == Edges.UdtToFieldDataType || edgeType == Edges.Includes || edgeType == Edges.Extends || edgeType == Edges.Scope) {
        val srcUdt = src.node.asInstanceOf[UdtBaseNode]
        val tgtUdt = tgt.node.asInstanceOf[UdtBaseNode]

        val tgtNs = new Vertex(tgtUdt.versionedName.namespace)
        val srcNs = new Vertex(srcUdt.versionedName.namespace)

        namespaceGraph.addVertex(tgtNs)
        namespaceGraph.addVertex(tgt)

        if (!tgtNs.equals(srcNs)) {
          namespaceGraph.addEdge(srcNs, tgtNs, namespaceGraph.getEdgeFactory.createEdge(srcNs, tgtNs).setData(srcNs, tgtNs, Edges.NamespaceToNamespace))
        }
        namespaceGraph.addEdge(tgtNs, tgt, namespaceGraph.getEdgeFactory.createEdge(tgtNs, tgt).setData(tgtNs, tgt, Edges.NamespaceToUdt))
      }

      added
    }

    override def exitUdt(e: IUdtBaseNode): Unit = {}

    private def addUdtEdgeToAnonymousPaths(root: Vertex, current: Vertex): Unit = {

      if (current.node.isInstanceOf[UdtBaseNode] &&
        !current.node.asInstanceOf[UdtBaseNode].isSynthetic &&
        graph.containsVertex(current)
      ) {
        createEdge(root, current, Edges.UdtToFieldDataType)
      }
      else {
        val udtedges = graph.outgoingEdgesOf(current).asScala.filter(e => e.getType == Edges.UdtToFieldDataType)
        udtedges.foreach(e =>
          addUdtEdgeToAnonymousPaths(root, new Vertex(e.getTgt.asInstanceOf[UdtBaseNode]))
        )
      }
    }
  }

  @throws[Exception]
  def debugUI(): Unit = {
    // The visualisation modifies the graph! display a clone
    val copy = graph.clone.asInstanceOf[AlfaDirectedGraph[Vertex, Edge]]
    val g = new JGraph(new JGraphModelAdapter[Vertex, Edge](copy))
    val layout = new JGraphHierarchicalLayout
    val facade = new JGraphFacade(g)

    layout.run(facade)
    val nested = facade.createNestedMap(false, false)
    g.getGraphLayoutCache.edit(nested)
    val sp = new JScrollPane(g)
    val f = new JFrame("JGraph")
    //    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.getContentPane.add(sp)
    f.setVisible(true)
    f.setLocation(100, 100)
    f.setSize(800, 600)

    Thread.sleep(60 * 1000 * 10)
  }
}

