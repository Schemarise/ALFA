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

import com.schemarise.alfa.compiler.ast.nodes.MethodDeclaration
import com.schemarise.alfa.compiler.tools.graph.d3.D3SupportModel
import com.schemarise.alfa.compiler.utils.TextUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MethodGraphBuilder {

  private def backtrace(argNames: Set[String], buf: ListBuffer[String], s: Vertex): Unit = {
    val inputArg = argNames.filter(a => s.node.nodeId.id.startsWith(a + "."))
    if (inputArg.size > 0) {
      val l = s.node.nodeId.id.split("\\.").last
      buf.append(l)
    }
    else {
      val edges = graph.incomingEdgesOf(s.asInstanceOf[MethodGraphVertex]).asScala

      edges.foreach(e => {
        backtrace(argNames, buf, e.getUnderSrc)
      })
    }
  }

  def pathToVertex(argNames: Set[String], typeName: String, fieldName: String) = {
    val ds = graph.edgeSet().asScala.filter(e => {
      val dt = e.getUnderTgt.asInstanceOf[MethodGraphVertex].dataType

      dt.isDefined && dt.get.toString.equals(typeName) &&
        e.getLabel().getOrElse("").equals(fieldName)
    }
    ).toList

    val b = new ListBuffer[String]()

    if (ds.size > 0) {
      backtrace(argNames, b, ds.head.getUnderSrc)
    }

    b.toList
  }

  private val graph = new AlfaDirectedGraph[MethodGraphVertex, Edge](classOf[Edge])

  private val gb = new MethodGraphExprVisitor2(graph)

  def graphToD3Model: D3SupportModel = {
    val formals = graph.vertexSet().asScala.filter(_.vtype == MethodBuildVertexType.Formal)

    val g =
      if (formals.size > 1) {
        val root = D3SupportModel(Seq("Transformer"))(0)
        formals.map(f => {
          val c = graph.d3SupportModelFrom(f)
          root.addChildD3Model(c)
        })
        root
      }
      else {
        val root = D3SupportModel(Seq("Transformer"))(0)
        val c = graph.d3SupportModelFrom(formals.head)
        root.addChildD3Model(c)
        root
      }

    g
  }

  def graphToPlantUml = {
    val formals = graph.vertexSet().asScala.filter(_.vtype == MethodBuildVertexType.Formal)

    val sb = new mutable.ListMap[String, String]
    val formalNames = formals.map(_.name)

    //    graph.debugUI()
    val visited = new mutable.HashSet[Int]()

    val start = formals.map(f => {
      buildNodePath(formalNames, sb, f, visited)
      s"""[*] --> ${f.name} """
    }).mkString("\n")

    val notes = graph.vertexSet().asScala.filter(_.doc.size > 0).map(s => {
      val full = s.doc.mkString(" ")
      val n = TextUtils.toMultilineBlockText(full, 97, "\\n")
      s"""note bottom of ${s.name} : $n  """
    }).mkString("\n")

    val body = sb.keys.mkString("\n") // .replaceAll("return", "result")
    val d =
      s"""
         |@startuml
         |
         |left to right direction
         |
         |skinparam Shadowing false
         |skinparam backgroundColor #white
         |'skinparam ActivityBorderColor darkblue
         |'skinparam ActivityStartColor #white
         |skinparam ArrowColor darkblue
         |hide empty description
         |skinparam defaultFontSize 12
         |skinparam nodesep 20
         |skinparam stateArrowFontColor blue
         |
         |
         |$start
         |
         |$body
         |
         |$notes
         |@enduml
     """.stripMargin

    d
  }

  def build(md: MethodDeclaration) = {
    md.traverse(gb)
    graph
  }

  private def buildNodePath(formalNames: mutable.Set[String], sb: mutable.ListMap[String, String], n: MethodGraphVertex, visited: mutable.Set[Int]): Unit = {
    val outEdges = graph.outgoingEdgesOf(n)

    val ihc = System.identityHashCode(n)

    if (!visited.contains(ihc)) {
      visited += ihc

      outEdges.asScala.foreach(e => {
        val src = graph.getEdgeSource(e)
        val tgt = graph.getEdgeTarget(e)

        val srcCol =
          if (src.name.startsWith("result")) "#lightblue"
          else ""

        val tgtCol =
          if (formalNames.contains(src.name)) "#lightgreen"
          else ""

        val lbl = if (e.getLabel().isDefined) s" : ${e.getLabel().get}" else ""

        val k = s"""${src.name} $srcCol --> ${tgt.name} $tgtCol $lbl\n"""
        sb.put(k, null)
        //      println(formalNames + "   " + sb.toString() + tgt.toString)
        buildNodePath(formalNames, sb, tgt, visited)
      })
    }
  }

}