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

import com.schemarise.alfa.compiler.ast.BaseNode
import com.schemarise.alfa.compiler.ast.model.IdentifiableNode
import com.schemarise.alfa.compiler.ast.model.graph.Edges.EdgeType
import com.schemarise.alfa.compiler.ast.model.graph.IGraphEdge
import com.schemarise.alfa.compiler.ast.model.types.EnumToString

class Edge extends IGraphEdge {
  private var edgeType: Option[EdgeType] = None
  private var srcVtx: Option[Vertex] = None
  private var tgtVtx: Option[Vertex] = None
  private var label: Option[String] = None

  def setData(src: Vertex, tgt: Vertex, et: EdgeType, lbl: Option[String] = None): Edge = {
    edgeType = Some(et)
    srcVtx = Some(src)
    tgtVtx = Some(tgt)
    label = lbl
    this
  }

  def getLabel() = {
    label
  }

  override def toString: String = {
    val s = srcVtx.get.node
    val t = tgtVtx.get.node

    val snode = if (s.isInstanceOf[BaseNode]) s"(${EnumToString.nodeTypeToString(s.asInstanceOf[BaseNode].nodeType)})" else ""
    val tnode = if (t.isInstanceOf[BaseNode]) s"(${EnumToString.nodeTypeToString(t.asInstanceOf[BaseNode].nodeType)})" else ""

    //    s"${edgeType.get.toString} : ${s.nodeId.toString}${snode} -> ${t.nodeId.toString}${tnode} "
    label.getOrElse("")
  }

  override def getType: EdgeType = edgeType.get

  override def getSrc: IdentifiableNode = srcVtx.get.node

  override def getTgt: IdentifiableNode = tgtVtx.get.node

  def getUnderSrc: Vertex = srcVtx.get

  def getUnderTgt: Vertex = tgtVtx.get


  def canEqual(other: Any): Boolean = other.isInstanceOf[Edge]

  override def equals(other: Any): Boolean = other match {
    case that: Edge =>
      (that canEqual this) &&
        edgeType == that.edgeType &&
        srcVtx == that.srcVtx &&
        tgtVtx == that.tgtVtx
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(edgeType, srcVtx, tgtVtx)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
