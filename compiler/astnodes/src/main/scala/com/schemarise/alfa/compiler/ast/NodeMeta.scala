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
package com.schemarise.alfa.compiler.ast

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.model.{IAnnotation, IDocAndAnnotated, IDocumentation, NodeVisitor}
import com.schemarise.alfa.compiler.ast.nodes.{Annotation, Documentation, UdtBaseNode}
import com.schemarise.alfa.compiler.err.{ResolutionMessage, SingleLineDocCommentsOnSameLine}
import com.schemarise.alfa.compiler.utils.TokenImpl

case class NodeMeta( // val namespace: NamespaceNode = NamespaceNode.empty,
                     val annotations: Seq[Annotation] = Seq.empty,
                     topDocs: Seq[IDocumentation] = Seq.empty,
                     samelineDocs: Seq[Documentation] = Seq.empty
                   ) extends ResolvableNode with IDocAndAnnotated {

  def withAlternateAnnotations(newAnns: Seq[Annotation]) = {
    NodeMeta(newAnns, topDocs, samelineDocs)
  }

  override def resolvableInnerNodes() = annotations

  override def nodeType: Nodes.NodeType = Nodes.NodeMeta

  override val location = TokenImpl.empty

  def docs: Seq[IDocumentation] = topDocs ++ samelineDocs

  def topDocsToString(indent: String) = {
    val startDoc = if (topDocs.size > 0) topDocs.mkString(indent + "/# ", "", " #/\n") else ""
    startDoc
  }

  override def toString: String = {
    Seq(annotations.mkString("\n"), topDocs.mkString("\n"), samelineDocs.mkString("\n")).mkString("\n")
  }

  def annotationsString(indent: String) = {
    NodeMeta.annotationsString(annotations, indent)
  }

  def docs1stSentence() = {
    val alldocs = (topDocs.map(_.text) ++ samelineDocs.map(_.text).filter(_.size > 0)).headOption

    val firstStr = alldocs.getOrElse("")

    val line = firstStr.split(".\n").head
    line
  }

  def samelineDocsToString() = {
    val sameLnDoc = if (samelineDocs.size > 0) samelineDocs.mkString(" ## ", " ", "") else ""
    sameLnDoc
  }

  override protected def resolve(ctx: Context): Unit = {
    super.resolve(ctx)

    if (samelineDocs.length > 0) {
      val d = samelineDocs.head

      if (parent.location.getStartLine != d.location.getStartLine) {
        ctx.addResolutionError(new ResolutionMessage(d.location,
          SingleLineDocCommentsOnSameLine)(None, List.empty, d.text, parent.location.getStartLine.toString))
      }
    }

  }

  override def traverse(v: NodeVisitor): Unit = {

  }
}

object NodeMeta {
  def withDoc(fdescr: String, anns: Seq[Annotation] = Seq.empty) =
    new NodeMeta(topDocs = Seq(new Documentation(text = fdescr)), annotations = anns)

  val empty: NodeMeta = new NodeMeta()

  def annotationsString(annotations: Seq[IAnnotation], indent: String) = {
    val deli = "\n" + indent
    val s = indent + annotations.map(a => {
      if (a.objectExpression.isDefined) {
        val oe = a.objectExpression.get
        val e = oe.value.map(v => v._1 + "=" + v._2.toString).mkString(", ")
        s"@${a.versionedName.fullyQualifiedName}($e)"
      }
      else
        s"@${a.versionedName.fullyQualifiedName}"

    }).mkString(deli) + deli

    if (s.trim.length == 0) "" else s
  }
}