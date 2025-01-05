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
package com.schemarise.alfa.compiler.ast.nodes

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.ast.{NodeMeta, TraversableNode, UdtName, UdtVersionedName}
import com.schemarise.alfa.compiler.err.{DuplicateEntry, ExpressionError, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.TokenImpl

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MethodSignature(token: IToken = TokenImpl.empty,
                      nodeMeta: NodeMeta = NodeMeta.empty,
                      namespace: NamespaceNode = NamespaceNode.empty,
                      val nameNode: StringNode,
                      typeParamsNode: Option[Seq[TypeParameter]] = None,
                      typeArgumentsNode: Option[Map[String, DataType]] = None,
                      rawFormals: Seq[FieldOrFieldRef],
                      val result: DataType,
                      imports: Seq[ImportDef] = Seq.empty)
  extends UdtBaseNode(
    None, token, namespace, nodeMeta, Seq.empty, nameNode, None, typeParamsNode, typeArgumentsNode,
    None, List.empty, List.empty, List.empty, Seq.empty, Seq.empty, imports)
    with TraversableNode
    with IMethodSignature {

  private var formals_ : ListMap[String, Formal] = ListMap.empty

  override def resolvableInnerNodes() = {
    super.resolvableInnerNodes() ++ Seq(result) ++ formals_.values.toSeq
  }

  def returnType = result.unwrapTypedef

  override def udtType: UdtType = UdtType.methodSig

  def methodName = versionedName.name

  override def nodeType: Nodes.NodeType = Nodes.Method

  //  override def templateInstantiated : MethodSignature = ???

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
      formals.foreach(_._2.traverse(v))
      returnType.traverse(v)
    }
    v.exit(this)
  }

  def formals = {
    // can cause stack overflow
    //    assertPreResolved(None)
    formals_
  }

  def asSyntheticRecord(name: String) = {
    new SyntheticRecord(
      location, this.namespaceNode, NodeMeta.empty, Seq.empty,
      StringNode.create(name),
      None, None, None, None, Seq.empty, formals.values.map(_.asFieldRef).toSeq, None, imports)
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    ctx.registry.pushTypeParameters(typeParamsNode)

    val collect = new mutable.LinkedHashMap[String, Formal]()

    rawFormals.
      flatMap(e => e.field).
      map(f => Formal.from(f)).
      foreach(f => {
        if (collect.contains(f.nameNode.text) &&
          !collect.get(f.nameNode.text).get.dataType.isAssignableFrom(f.dataType)) {
          ctx.addResolutionError(f.location, DuplicateEntry, "field", f.nameNode.text)
        }
        else {
          collect.put(f.nameNode.text, f)
        }
      })

    formals_ = ListMap(collect.toSeq: _*)

    formals_.foreach(_._2.startPreResolve(ctx, this))

    ctx.registry.popTypeParameters(typeParamsNode)


    val ann = new ListBuffer[String]()

    if (hasAnnotation(IAnnotation.Annotation_Http_Delete))
      ann += "Delete"

    if (hasAnnotation(IAnnotation.Annotation_Http_Get))
      ann += "Get"

    if (hasAnnotation(IAnnotation.Annotation_Http_Post))
      ann += "Post"

    if (hasAnnotation(IAnnotation.Annotation_Http_Put))
      ann += "Put"

    if (ann.size > 1)
      ctx.addResolutionError(this, ExpressionError, s"Only 1 annotation out of ${ann.mkString(", ")} can be specified")
  }

  override def toString: String = {
    val sb = new StringBuilder

    if (nodeMeta.docs.size > 0)
      sb ++= nodeMeta.docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")

    sb.append(name.toString)
    sb.append(rawFormals.map(_.toString.trim).mkString("( ", ", ", " ) : ").replace("\n", ""))
    sb.append(result.toString)

    sb.toString()
  }


  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = ???

  def comparisonTypeNames = {
    val t = rawFormals.filter(_.field.isDefined).map(_.field.get.dataType.toString) ++ Seq(returnType.toString)
    t.mkString
  }

  override def equals(other: Any): Boolean = other match {
    case that: MethodSignature =>
      comparisonTypeNames.mkString == that.comparisonTypeNames.mkString
    case _ => false
  }

  override def hashCode(): Int = {
    comparisonTypeNames.hashCode()
  }

  // Method can be builtin - so get the UDT where its used
}
