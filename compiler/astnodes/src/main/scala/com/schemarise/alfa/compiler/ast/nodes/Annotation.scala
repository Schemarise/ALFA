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

import com.schemarise.alfa.compiler.ast.model.{IAnnotation, IToken, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.{AlfaInternalException, Context}
import com.schemarise.alfa.compiler.antlr.AlfaParser
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, EnumToString, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, EnclosingDataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{AnnotationNotPermitted}
import com.schemarise.alfa.compiler.types.AnnotationTargetType
import com.schemarise.alfa.compiler.utils.{ILogger, TokenImpl}

object Annotation {
  val Meta_Field_Annotations_Vn = UdtVersionedName(name = StringNode.create(IAnnotation.Meta_Field_Annotations))
}

class Annotation(val location: IToken = TokenImpl.empty, namespace: NamespaceNode = NamespaceNode.empty,
                 val nameNode: StringNode,
                 val valueCtx: Option[AlfaParser.NamedExpressionSequenceContext] = None,
                 rawObjExpr: Option[Expression] = None,
                )
  extends BaseNode with ResolvableNode
    with TraversableNode with IAnnotation {

  var objectExpression: Option[Expression] = rawObjExpr

  var resolvedAnnotationDeclType: Option[UdtBaseNode] = None

  private var annotatedDefinition: Option[AnnotatableNode] = None

  def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                           typeArguments: Map[String, DataType]): Annotation = {
    new Annotation(location, namespace, nameNode, valueCtx, rawObjExpr)
  }

  def targetUdt: Option[UdtBaseNode] = {
    if (annotatedDefinition.isDefined && annotatedDefinition.get.isInstanceOf[UdtBaseNode])
      Some(annotatedDefinition.get.asInstanceOf[UdtBaseNode])
    else
      None
  }

  def asDataType = UdtDataType(name = nameNode)

  override def versionedName: UdtVersionedName = {
    if (resolvedAnnotationDeclType.isDefined)
      resolvedAnnotationDeclType.get.name
    else
      // dummy
      UdtVersionedName(UdtName(namespace, nameNode, None, None), None)(Some(UdtType.annotation))
  }


  override def nodeType: Nodes.NodeType = Nodes.AnnotationInstance

  override def resolvableInnerNodes() = {
    val b = (if (resolvedAnnotationDeclType.isDefined) Seq(resolvedAnnotationDeclType.get) else Seq.empty)
    b
  }

  private def locateAnnotatableParent(): BaseNode =
    locateAnnotatableParent(this)

  private def locateAnnotatableParent(from: ResolvableNode): BaseNode = {
    if (from.isInstanceOf[AnnotatedUdtVersionedName]) {
      from
    }
    else if (from.parent.isInstanceOf[UdtBaseNode]) {
      from.parent.asInstanceOf[UdtBaseNode]
    }
    else if (from.isInstanceOf[NamespaceNode]) {
      from
    }
    else if (from.isInstanceOf[Field]) {
      from
    }
    else if (from.parent.isInstanceOf[UdtDataType]) {
      from.parent
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateAnnotatableParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      throw new com.schemarise.alfa.compiler.AlfaInternalException("Unhandled node type " + from)
    }
  }

  private def locateAnnotatedDefn(n: BaseNode): AnnotatableNode = {
    if (n == null)
      throw new AlfaInternalException("Failed to locateAnnotatedDefn1")
    else if (n.isInstanceOf[AnnotatableNode])
      n.asInstanceOf[AnnotatableNode]
    else if (n.isInstanceOf[ResolvableNode])
      locateAnnotatedDefn(n.asInstanceOf[ResolvableNode].parent)
    else {
      throw new AlfaInternalException("Failed to locateAnnotatedDefn2")
    }
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    val udtDataType = new UdtDataType(location = nameNode.location, namespaceNode = namespace, name = nameNode)
    resolvedAnnotationDeclType = ctx.registry.getAnnotation(Some(this), udtDataType)

    if (resolvedAnnotationDeclType.isDefined)
      resolvedAnnotationDeclType.get.startPreResolve(ctx, this)

    annotatedDefinition = Some(locateAnnotatedDefn(parent))
  }

  override def resolve(ctx: Context): Unit = {
    super.resolve(ctx)


    if (resolvedAnnotationDeclType.isDefined) {

      val udtDataType = resolvedAnnotationDeclType.get.asDataType

      val annotationDecl = resolvedAnnotationDeclType.get.asInstanceOf[AnnotationDecl]
      val node = locateAnnotatableParent()
      val nodeType = EnumToString.nodeTypeToString(node.nodeType)

      // if against a Udt, it must be an include annotation ... hack'ish though
      val udtType =
        if (nodeType == Nodes.DataTypeNode.toString)
          AnnotationTargetType.Includes.toString
        else if (nodeType == Nodes.SyntheticRecordNode.toString)
          AnnotationTargetType.Record.toString
        else if (nodeType == Nodes.Type.toString)
          AnnotationTargetType.Type.toString
        else nodeType

      val isSupported = annotationDecl.supportedTarget(AnnotationTargetType.withEnumName(udtType))
      if (!isSupported) {
        ctx.addResolutionError(location, AnnotationNotPermitted, annotationDecl.nameNode.toString,
          udtType, annotationDecl.annotationTargets.mkString("", ", ", ""))
      }

      val reqdFields =
        if (annotationDecl.allAccessibleFields().size > 0) {
          annotationDecl.allAccessibleFields().filter({ f =>
            val t = f._2.dataType

            if (f._2.hasExpression)
              false
            else if (t.isInstanceOf[EnclosingDataType])
              t.asInstanceOf[EnclosingDataType].encType != Enclosed.opt
            else
              true
          })
        }
        else
          Map.empty
    }
  }

  override def toString: String = {
    val e =
      if (objectExpression.isDefined) objectExpression.get.toString
      else if (rawObjExpr.isDefined) rawObjExpr.get.toString
      else ""

    s"@${nameNode}$e"
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      if (objectExpression.isDefined) {
        objectExpression.get.traverse(v)
      }
    }
    v.exit(this)
  }
}
