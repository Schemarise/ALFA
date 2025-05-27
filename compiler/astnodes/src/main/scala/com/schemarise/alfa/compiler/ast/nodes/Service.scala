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

import com.schemarise.alfa.compiler.ast.model.{IService, IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.{IMetaDataType, IUdtDataType, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{DuplicateEntry, ResolutionMessage, ServiceReferencesMetaTypes, ServiceReferencesTraits}
import com.schemarise.alfa.compiler.utils.TokenImpl

import java.util.function.Supplier
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Service(ctx: Option[Context],
              token: IToken = TokenImpl.empty,
              namespace: NamespaceNode = NamespaceNode.empty,
              nodeMeta: NodeMeta = NodeMeta.empty,
              nameNode: StringNode,
              versionNo: Option[IntNode] = None,
              typeParams: Option[Seq[TypeParameter]] = None,
              typeArguments: Option[Map[String, DataType]] = None,
              val rawConstructorArgs: Seq[FieldOrFieldRef] = Seq.empty,
              val methodSigNodes: Seq[MethodSignature] = Seq.empty,
              modifiers: Seq[ModifierNode] = Seq.empty,
              imports: Seq[ImportDef] = Seq.empty
             )
  extends UdtBaseNode(
    ctx, token, namespace, nodeMeta, modifiers, nameNode, versionNo, typeParams, typeArguments, None, Seq.empty,
    Seq.empty, List.empty, List.empty, List.empty, imports)
    with TraversableNode with IService {

  private var ctorFormals_ : ListMap[String, Formal] = ListMap.empty

  override def nodeType: Nodes.NodeType = Nodes.Service

  override def udtType: UdtType = UdtType.service

  //  override def templateInstantiated : ServiceDecl = ???

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    super.resolvableInnerNodes() ++ methodSigNodes ++ constructorFormals.values.toSeq.asInstanceOf[Seq[ResolvableNode]]
  }

  override def getMethodSignatures() = {
    methodSigNodes.map(e => e.nameNode.text -> e).toMap
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
      constructorFormals.values.foreach(_.traverse(v))
      methodSigNodes.foreach(_.traverse(v))
    }
    v.exit(this)
  }

  override def resolve(ctx: Context): Unit = {
    super.resolve(ctx)

    if (!hasErrors) {
      if (!isInternal && getAnnotation(IAnnotation.Annotation_IgnoreServiceWarnings).isEmpty) {
        reportMetaTypeReferences(ctx)
        reportTraitReferences(ctx)
      }
    }
  }

  private def reportTraitReferences(ctx: Context) = {
    val visiting = new ListBuffer[String]

    this.traverse(new NoOpNodeVisitor() {
      override def enter(e: ITrait): Mode = {
        if (e.scope.isEmpty) {
          ctx.addResolutionError(nameNode.location, ServiceReferencesTraits, name.name, e.name.fullyQualifiedName)
        }
        super.enter(e)
      }

      override def enter(e: IUdtDataType): Mode = {
        val p = peekPath()
        val parentIsUdt = p.isInstanceOf[IRecord] ||
          p.isInstanceOf[ITrait] ||
          p.isInstanceOf[IEnum] ||
          p.isInstanceOf[IEntity] ||
          p.isInstanceOf[IUnion] ||
          p.isInstanceOf[IKey]

        val u = e.asInstanceOf[UdtDataType]
        if (!parentIsUdt && // not from an include, as service references to includes are ok
          u.resolvedType.isDefined &&
          !visiting.contains(u.resolvedType.get.name.fullyQualifiedName)) {
          visiting += u.resolvedType.get.name.fullyQualifiedName
          u.resolvedType.get.traverse(this)
        }

        super.enter(e)
      }
    })
  }

  private def reportMetaTypeReferences(ctx: Context) = {

    val visiting = new ListBuffer[String]

    this.traverse(new NoOpNodeVisitor() {
      override def enter(e: IMetaDataType): Mode = {
        ctx.addResolutionError(nameNode.location, ServiceReferencesMetaTypes, name.name)
        super.enter(e)
      }

      override def enter(e: IUdtDataType): Mode = {
        val u = e.asInstanceOf[UdtDataType]
        if (u.resolvedType.isDefined && !visiting.contains(u.resolvedType.get.name.fullyQualifiedName)) {
          visiting += u.resolvedType.get.name.fullyQualifiedName
          u.resolvedType.get.traverse(this)
        }
        super.enter(e)
      }
    })
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    validateUnique(ctx, methodSigNodes.map(_.nameNode), "function")

    val collect = new mutable.LinkedHashMap[String, Formal]()

    rawConstructorArgs.
      flatMap(e => e.field).
      map(f => Formal.from(f)).
      foreach(f => {
        if (collect.contains(f.nameNode.text) &&
          !collect.get(f.nameNode.text).get.dataType.isAssignableFrom(f.dataType))
          ctx.addResolutionError(f, DuplicateEntry, "field", f.nameNode.text)
        else {
          collect.put(f.nameNode.text, f)
        }
      })

    ctorFormals_ = ListMap(collect.toSeq: _*)

    ctorFormals_.foreach(_._2.startPreResolve(ctx, this))

  }

  override def toStringIncludesAndBody(
                                        _includes:Supplier[Seq[IUdtDataType]] = () => includes,
                                        _fields:Supplier[Seq[FieldOrFieldRef]] = () => fields
                                      ): String = {
    val sigs = methodSigNodes.map(ms => {
      ms.toString
    }).mkString("\n")

    s"""
       |{
       |${indent(sigs, "    ")}
       |}
    """.stripMargin
  }

  override def constructorFormals: ListMap[String, IFormal] = ctorFormals_

  override def allFields: ListMap[String, Field] = ctorFormals_

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode = ???
}