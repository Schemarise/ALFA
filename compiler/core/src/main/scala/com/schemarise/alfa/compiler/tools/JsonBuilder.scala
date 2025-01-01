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
package com.schemarise.alfa.compiler.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, BaseJsonNode, ObjectNode}
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr._
import com.schemarise.alfa.compiler.ast.model.types._

import java.util

// TODO Complete implementation

class JsonBuilder(cua: ICompilationUnitArtifact) extends NoOpNodeVisitor {
  private val mapper = new ObjectMapper()
  private val stack = new util.Stack[BaseJsonNode]()

  def asFormattedJson(on: ObjectNode) = mapper.writerWithDefaultPrettyPrinter.writeValueAsString(on)

  def build(): ObjectNode = {
    val on = mapper.createObjectNode()
    // TODO write fields and typedefs

    val an = on.putArray("namespaces")

    stack.push(an)
    cua.graph.traverse(this)
    stack.pop()
    on
  }

  override def enter(e: INamespaceNode): Mode = {
    val an = node.asInstanceOf[ArrayNode]
    val on = an.addObject()
    on.put("name", e.name);
    val defsn = on.putArray("definitions")
    stack.push(defsn)
    NodeVisitMode.Continue
  }

  override def exit(e: INamespaceNode): Unit = {
    stack.pop // definitions
  }

  private def node: BaseJsonNode = {
    stack.peek
  }

  private def exitUdt(e: IUdtBaseNode): Unit = {
    stack.pop() // fields
  }

  private def enterUdt(e: IUdtBaseNode) = {
    val an = node.asInstanceOf[ArrayNode]
    val on = an.addObject()
    on.put("type", e.getClass.getSimpleName.toLowerCase())
    on.put("name", e.name.fullyQualifiedName)

    val fn = if (e.isInstanceOf[IService]) "formals" else "allFields"

    val fieldsNode = on.putArray(fn)
    stack.push(fieldsNode)

    NodeVisitMode.Continue
  }

  override def enter(e: ILibrary): Mode = super.enter(e)

  override def enter(e: IService): Mode = {
    enterUdt(e)
    NodeVisitMode.Break // FIXME Services are not being printed
  }

  override def enter(e: IRecord): Mode = enterUdt(e)

  override def enter(e: IUnion): Mode = enterUdt(e)

  override def enter(e: IEnum): Mode = enterUdt(e)

  override def enter(e: IAnnotationDecl): Mode = enterUdt(e)

  override def enter(e: IEntity): Mode = enterUdt(e)

  override def enter(e: ITrait): Mode = enterUdt(e)

  override def enter(e: IKey): Mode = enterUdt(e)

  override def exit(e: IService): Unit = exitUdt(e)

  override def exit(e: IRecord): Unit = exitUdt(e)

  override def exit(e: IDataproduct): Unit = exitUdt(e)

  override def exit(e: IUnion): Unit = exitUdt(e)

  override def exit(e: IEnum): Unit = exitUdt(e)

  override def exit(e: IAnnotationDecl): Unit = exitUdt(e)

  override def exit(e: IEntity): Unit = exitUdt(e)

  override def exit(e: ITrait): Unit = exitUdt(e)

  override def exit(e: IKey): Unit = exitUdt(e)


  override def enter(e: IField): Mode = {
    val an = node.asInstanceOf[ArrayNode]
    val on = an.addObject()
    on.put("name", e.name)

    val typeNode = on.putObject("type")
    stack.push(typeNode)
    e.dataType.traverse(this)
    stack.pop()

    if (e.expression.isDefined) {
      val expr = on.putObject("default")
      stack.push(expr)
      e.expression.get.traverse(this)
      stack.pop()
    }

    NodeVisitMode.Break
  }

  override def enter(e: IFormal): Mode = {
    val an = node.asInstanceOf[ArrayNode]
    val on = an.addObject()
    on.put("name", e.name)

    val typeNode = on.putObject("type")
    stack.push(typeNode)
    e.dataType.traverse(this)
    stack.pop()

    if (e.expression.isDefined) {
      val expr = on.putObject("default")
      stack.push(expr)
      e.expression.get.traverse(this)
      stack.pop()
    }

    NodeVisitMode.Break
  }

  override def enter(e: IEnclosingDataType): Mode = super.enter(e)

  override def enter(e: IScalarDataType): Mode = {
    val n = node
    val on = n.asInstanceOf[ObjectNode]
    on.put("type", "scalar")
    on.put("scalarType", e.scalarType.toString)
    super.enter(e)
  }

  override def enter(e: IMapDataType): Mode = {
    val mapNode = node.asInstanceOf[ObjectNode]

    mapNode.put("type", "map")
    val kd = mapNode.putObject("keyType")
    stack.push(kd)
    e.keyType.traverse(this)
    stack.pop()

    val vd = mapNode.putObject("valueType")
    stack.push(vd)
    e.valueType.traverse(this)
    stack.pop()

    NodeVisitMode.Break
  }


  override def enter(e: IListDataType): Mode = {
    val mapNode = node.asInstanceOf[ObjectNode]

    mapNode.put("type", "seq")
    val kd = mapNode.putObject("componentType")
    stack.push(kd)
    e.componentType.traverse(this)
    stack.pop()
    NodeVisitMode.Break
  }

  override def enter(e: ISetDataType): Mode = {
    val mapNode = node.asInstanceOf[ObjectNode]

    mapNode.put("type", "set")
    val kd = mapNode.putObject("componentType")
    stack.push(kd)
    e.componentType.traverse(this)
    stack.pop()
    NodeVisitMode.Break
  }

  override def enter(e: IUdtDataType): Mode = {
    val objNode = node.asInstanceOf[ObjectNode]

    objNode.put("type", "udt")
    objNode.put("name", e.fullyQualifiedName)
    NodeVisitMode.Break
  }

  override def enter(e: IMethodSignature): Mode = {
    val an = node.asInstanceOf[ArrayNode]
    val on = an.addObject()
    on.put("type", e.getClass.getSimpleName.toLowerCase())
    on.put("name", e.name.fullyQualifiedName)

    val fn = if (e.isInstanceOf[IService]) "formals" else "allFields"

    val fieldsNode = on.putArray(fn)
    stack.push(fieldsNode)

    NodeVisitMode.Continue
  }

  override def enter(e: ITupleDataType): Mode = ???

  override def enter(e: IUnionDataType): Mode = ???

  override def enter(e: IBooleanLiteral): Mode = {
    val on = node.asInstanceOf[ObjectNode]
    on.put("type", "boolean")
    on.put("value", "" + e.value)
    super.enter(e)
  }


  override def enter(e: IStringLiteral): Mode = {
    val on = node.asInstanceOf[ObjectNode]
    on.put("type", "string")
    on.put("value", "" + e.resolvedValue)
    super.enter(e)
  }


  override def enter(e: ICharLiteral): Mode = {
    val on = node.asInstanceOf[ObjectNode]
    on.put("type", "char")
    on.put("value", "" + e.value)
    super.enter(e)
  }


  override def enter(e: INumberLiteral): Mode = {
    val on = node.asInstanceOf[ObjectNode]
    val t = e.dataType.toString
    on.put("type", t)
    on.put("value", "" + e.value)
    super.enter(e)
  }

  override def enter(e: IMapExpression): Mode = ???

  override def enter(e: ISetExpression): Mode = ???

  override def enter(e: ISeqExpression): Mode = ???

  override def enter(e: IObjectExpression): Mode = ???

  override def enter(e: ITupleExpression): Mode = ???

  override def enter(e: IQualifiedStringLiteral): Mode = ???
}

