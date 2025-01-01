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
import com.schemarise.alfa.compiler.tools.graph.d3.D3SupportModel
import org.jgraph.JGraph
import org.jgrapht.ext.JGraphModelAdapter
import org.jgrapht.graph.{AbstractBaseGraph, ClassBasedEdgeFactory}
import org.jgrapht.{DirectedGraph, EdgeFactory}

import javax.swing.{JFrame, JScrollPane}
import scala.collection.JavaConverters._

class AlfaDirectedGraph[V, E](ef: EdgeFactory[V, E])
  extends AbstractBaseGraph[V, E](ef, true, true)
    with DirectedGraph[V, E] {

  def d3SupportModelFrom(v: V): D3SupportModel = {
    val out = this.outgoingEdgesOf(v)

    val p = D3SupportModel(Seq(v.toString))(0)

    val children = out.asScala.map(e => this.getEdgeTarget(e))

    children.foreach(c => {
      p.addChildD3Model(d3SupportModelFrom(c))
    })

    p
  }

  def this(edgeClass: Class[_ <: E]) {
    this(new ClassBasedEdgeFactory[V, E](edgeClass).asInstanceOf[EdgeFactory[V, E]])
  }

  @throws[Exception]
  def debugUI(): Unit = {
    // The visualisation modifies the graph! display a clone
    val copy = this.clone.asInstanceOf[AlfaDirectedGraph[Vertex, Edge]]
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
