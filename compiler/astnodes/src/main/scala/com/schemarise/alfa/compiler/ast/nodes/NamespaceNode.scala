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

import com.schemarise.alfa.compiler.ast.model.{INamespaceNode, IToken, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.err.{ExpressionError, NameComponentPotentialConflict, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.{LangKeywords, LexerUtils, TokenImpl}

case class NamespaceNode(val nameNode: StringNode = StringNode.empty,
                         val isSynthetic: Boolean = false,
                         val location: IToken = TokenImpl.empty,
                         val meta: NodeMeta = NodeMeta.empty,
                         collectedUdts: Seq[UdtBaseNode] = Seq.empty) extends BaseNode
  with ResolvableNode
  with TraversableNode
  with AnnotatableNode
  with DocumentableNode
  with IdentifiableNode with INamespaceNode {

  private var allUdts: Seq[UdtBaseNode] = collectedUdts

  override def name = nameNode.text

  private[compiler] def addAll(all: Seq[UdtBaseNode]): Unit = {
    if (!allUdts.isEmpty)
      throw new com.schemarise.alfa.compiler.AlfaInternalException("Internal error - unexpected reassignment")

    allUdts = all
  }

  def first = {
    if (nameNode == StringNode.empty)
      ""
    else {
      val s = nameNode.text.split('.')
      s.head
    }
  }

  override def isEmpty: Boolean = nameNode.text.length == 0

  private[compiler] def udts: Seq[UdtBaseNode] = allUdts


  override def nodeType = Nodes.Namespace

  override def resolvableInnerNodes() = {
    // ONLY RESOLVE UDTS IF CU BEING RESOLVED. OTHERWISE CAUSES CYCLES NS1 -> NS1.B -> NS2.A -> NS1.C -> NS1 -> NS1.B
    // THERE CAN BE FAKE NAME NODES SHOULD NOT TRIGGER ON THOSE
    val p = this.parent
    val rest =
      if (p.isInstanceOf[CompilationUnit])
        udts
      else
        Seq.empty

    Seq(meta) ++ rest
  }

  override def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)

    val withName = annotationsMap.filter(ann => {
      ann._1.fullyQualifiedName.equals(IAnnotation.Meta_DB_Table) &&
        ann._2.objectExpression.isDefined && ann._2.objectExpression.get.value.contains("Name")
    })

    if (!withName.isEmpty) {
      val n = withName.head._2.versionedName.asInstanceOf[UdtVersionedName]
      ctx.addResolutionError(location, ExpressionError, IAnnotation.Meta_DB_Table + " Name cannot be specified at namespace level")
    }

  }

  //  override def preResolve(ctx: Context): Unit = {
  //      super.preResolve(ctx)

  //    if ( ! this.parent.isInstanceOf[CompilationUnit] )
  //      println(2222)

  //    if ( name.size > 0 ) {
  //      nameNode.origString.split("\\.").toSeq.foreach( pkg => {
  //        // We should escape all ALFA keywords and allow all names.
  //        // Lang code generators should handle conflicts
  ////        if ( LexerUtils.keywords.contains(pkg) ) {
  ////          ctx.addResolutionError( ResolutionMessage(location, NameComponentPotentialConflict)( List.empty, pkg, LangKeywords.matchingLanguages(pkg).mkString(",")))
  ////        }
  //      } )
  //    }

  //    val withName = annotationsMap.filter( ann => {
  //      ann._1.fullyQualifiedName.equals(IAnnotation.Meta_DB_Table) &&
  //        ann._2.objectExpression.isDefined && ann._2.objectExpression.get.value.contains("Name")
  //    } )
  //
  //    if (! withName.isEmpty  ) {
  //      val n = withName.head._2.versionedName.asInstanceOf[UdtVersionedName]
  //      ctx.addResolutionError( ResolutionMessage(n.location, ExpressionError)( List.empty,
  //        IAnnotation.Meta_DB_Table + " Name cannot be specified at namespace level"))
  //    }
  //  }

  override def toString: String = {
    val sb = new StringBuilder

    val alldocs = docNodes
    if (alldocs.size > 0)
      sb ++= alldocs.map(_.toString).mkString("\n/#\n ", "\n", "\n #/\n")

    sb ++= annotationNodes.mkString("", "\n", "")
    if (!annotationNodes.isEmpty) {
      sb.append("\n")
    }
    if (nameNode != StringNode.empty)
      sb ++= "namespace " + nameNode.origString + "\n"

    resolvableInnerNodes.filter(e => !e.isInstanceOf[NodeMeta]).foreach(
      z =>
        sb.append(z.toString)
    )

    sb.toString
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      udts.filter(_.isInstanceOf[TraversableNode]).map(_.asInstanceOf[TraversableNode]).foreach(_.traverse(v))
    }
    v.exit(this)
  }

  override def nodeId = LocatableNodeIdentity(getClass.getSimpleName, nameNode)

  override def docs = meta.docs

  override def annotations = meta.annotations

  override def parentNamespaces: Seq[INamespaceNode] =
    Namespace(name).parentNamespaces

  override def docNodes: Seq[IDocumentation] = meta.docs

  override def annotationNodes: Seq[Annotation] = meta.annotations
}

object NamespaceNode {
  val empty: NamespaceNode = NamespaceNode(isSynthetic = true)
}