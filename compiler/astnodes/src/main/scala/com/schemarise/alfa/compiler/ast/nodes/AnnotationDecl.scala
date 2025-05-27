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

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IAnnotationDecl, IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{ExpressionError, NoAnnotationTargets}
import com.schemarise.alfa.compiler.types.AnnotationTargetType.TargetType
import com.schemarise.alfa.compiler.utils.TokenImpl

class AnnotationDecl(ctx: Option[Context] = None,
                     token: IToken = TokenImpl.empty,
                     namespace: NamespaceNode = NamespaceNode.empty,
                     nodeMeta: NodeMeta = NodeMeta.empty,
                     val nameNode: StringNode,
                     fields: Seq[FieldOrFieldRef] = Seq.empty,
                     val annotationTargets: Seq[TargetType] = Seq.empty,
                     includes: Seq[UdtDataType] = Seq.empty,
                     imports: Seq[ImportDef] = Seq.empty
                    )
  extends UdtBaseNode(ctx, token, namespace, nodeMeta, List.empty, nameNode, rawFieldRefs = fields,
    rawIncludesNode = includes, assertNodes = Seq.empty, imports = imports) with IAnnotationDecl {

  override def nodeType: Nodes.NodeType = Nodes.Annotation

  // override def resolvableInnerNodes() = fields :+ nodeMeta

  //  override def templateInstantiated : AnnotationDecl = ???

  def supportedTarget(tgt: TargetType): Boolean =
    annotationTargets.filter(a => a.equals(tgt)).size > 0

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    if (annotationTargets.isEmpty) {
      ctx.addResolutionWarning(nameNode.location, NoAnnotationTargets, nameNode.text)
    }
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb ++= annotationNodes.mkString("", "\n", "")

    if (docs.size > 0)
      sb ++= docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")


    sb ++= "annotation " + versionedName.fullyQualifiedName + annotationTargets.mkString("( ", ", ", " )")

    sb ++= toStringIncludesAndBody()

    sb.toString()
  }

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = ???

  override def udtType: UdtType = UdtType.annotation
}
