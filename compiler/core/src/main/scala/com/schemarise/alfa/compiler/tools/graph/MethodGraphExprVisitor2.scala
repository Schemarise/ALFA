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

import com.schemarise.alfa.compiler.AlfaInternalException
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr.{IObjectExpression, IQualifiedStringLiteral}
import com.schemarise.alfa.compiler.ast.model.graph.{Edges, NodeIdentity}
import com.schemarise.alfa.compiler.ast.model.stmt.{ILetDeclarationStatement, IReturnStatement}
import com.schemarise.alfa.compiler.ast.model.types.IDataType
import com.schemarise.alfa.compiler.tools.graph.MethodBuildVertexType.VariableType

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class MethodGraphExprVisitor2(graph: AlfaDirectedGraph[MethodGraphVertex, Edge]) extends NoOpNodeVisitor {

  private val constructorParam = new util.LinkedList[String]()
  private val accessPrefixStack = new util.Stack[String]()
  private val lastVisitedLiterals = new ListBuffer[MethodGraphVertex]()

  case class IdNode(s: String) extends IdentifiableNode {
    override def nodeId: NodeIdentity = new NodeIdentity("id", s)

    override def traverse(v: NodeVisitor): Unit = {}

    override def toString: String = s

    override val location: IToken = null
  }

  def createOrFindVertex(n: String, vt: VariableType, doc: Seq[IDocumentation] = Seq.empty, dataType: Option[IDataType] = None) = {
    val mgv = new MethodGraphVertex(IdNode(n), None, vt, doc, dataType)

    val existing = graph.vertexSet().asScala.find(e => e.equals(mgv))

    if (existing.isDefined)
      existing.get
    else {
      if (dataType.isEmpty)
        throw new AlfaInternalException("Datatype is manadatory when creating vertex")

      graph.addVertex(mgv)
      mgv
    }
  }

  private def buildFieldsOfVar(f: INode) = {
    val (vertexType, name, dataType, docs) = f match {
      case x: IFormal =>
        (MethodBuildVertexType.Formal, x.name, x.dataType, x.docs)

      case x: ILetDeclarationStatement =>
        (MethodBuildVertexType.Variable, x.variableName, x.dataType.get, x.rhs.docs)
    }

    val v = createOrFindVertex(name, vertexType, docs, Some(dataType))
    //      addVertex(v)

    //    fieldHierarchy(v, List(t._2), t._3)
  }

  override def enter(e: IFormal): Mode = {
    buildFieldsOfVar(e)
    super.enter(e)
  }

  //  override def enter(e: IIfElseExpression): Mode = {
  //    super.enter(e)
  //  }

  override def enter(v: ILetDeclarationStatement): Mode = {
    accessPrefixStack.push(v.variableName)
    buildFieldsOfVar(v)
    super.enter(v)
  }

  override def exit(v: ILetDeclarationStatement): Unit = {
    val from = createOrFindVertex(v.variableName, MethodBuildVertexType.Variable)

    lastVisitedLiterals.map(l => {
      addEdge(l, from)
    })

    lastVisitedLiterals.clear()
    accessPrefixStack.pop()
    super.exit(v)
  }

  override def enter(e: IStatement) = {
    e match {
      case r: IReturnStatement =>
        accessPrefixStack.push("return")
        val retv = createOrFindVertex("return", MethodBuildVertexType.Return, r.rhs.docs, r.dataType)

      case _ =>
    }

    super.enter(e)
  }

  override def exit(e: IStatement) = {

    super.exit(e)

    e match {
      case r: IReturnStatement =>
        accessPrefixStack.pop()
        val retv = createOrFindVertex("return", MethodBuildVertexType.Return)

        lastVisitedLiterals.map(l => {
          addEdge(l, retv)
        })

      case _ =>
    }

    lastVisitedLiterals.clear()
  }

  private def addEdge(i: MethodGraphVertex, o: MethodGraphVertex) = {
    val label = if (constructorParam.size() == 0)
      None
    else
      Some(constructorParam.removeFirst())

    val e = graph.getEdgeFactory.createEdge(i, o).setData(i, o, Edges.ExpressionInput, label)

    val added = graph.addEdge(i, o, e)
    if (!added)
      debug("addEdge NOT added " + i + " --> " + o)
    else
      debug("addEdge added " + i + " --> " + o)

  }

  private def debug(s: String) = {
    // println("DEBUG: " + s)
  }

  override def enter(e: IObjectExpression): Mode = {
    super.enter(e)
  }

  override def exit(e: IObjectExpression): Unit = {
    e.value.map(e => {
      val paramName = e._1
      constructorParam.add(paramName)
    })
  }

}
