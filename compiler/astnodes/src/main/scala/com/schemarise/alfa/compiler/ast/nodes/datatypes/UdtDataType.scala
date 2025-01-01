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
package com.schemarise.alfa.compiler.ast.nodes.datatypes

import java.util

import com.schemarise.alfa.compiler.ast.model.{IToken, IUdtBaseNode, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, IDataType, IUdtDataType}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.{ReferenceToAlfatheticDefinition, ResolutionMessage, TypeParameterCountMismatch}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.utils.{NoErrorsAssertor, TextUtils, TokenImpl}


case class UdtDataType(location: IToken = TokenImpl.empty,
                       namespaceNode: NamespaceNode = NamespaceNode.empty,
                       name: StringNode,
                       versionNode: Option[IntNode] = None,
                       typeArgumentsNode: Option[Seq[DataType]] = None,
                       synthUdtReference: Boolean = false,
                       nodeMeta: Option[NodeMeta] = None
                      ) extends DataType
  with ResolvableNode with IdentifiableNode with TraversableNode with IUdtDataType {

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[UdtDataType]) {
      val r = obj.asInstanceOf[UdtDataType]
      fullyQualifiedName.equals(r.fullyQualifiedName) &&
        versionNode.equals(r.versionNode) &&
        typeArgumentsNode.equals(r.typeArgumentsNode)
    } else {
      false
    }
  }

  def referencedFromField: Boolean = {
    if (isPreResolved()) {

      var p = parent
      while (p != null) {
        if (parent.isInstanceOf[UdtBaseNode] || parent.isInstanceOf[NamespaceNode]) {
          return false
        }
        else {
          return true
        }
      }
    }

    false
  }

  override def docs = if (nodeMeta.isDefined) nodeMeta.get.docs else Seq.empty

  override def annotations = if (nodeMeta.isDefined) nodeMeta.get.annotations else Seq.empty

  override def annotationsMap = if (nodeMeta.isDefined) nodeMeta.get.annotationsMap else Map.empty

  override def annotationValue(annotationName: String, annotationAttribute: String) =
    if (nodeMeta.isDefined) nodeMeta.get.annotationValue(annotationName, annotationAttribute) else None

  /**
   * Namespace declared UDT, if templated, the raw templated declaration
   */
  private var _resolvedType: Option[UdtBaseNode] = None

  /**
   * When a type ref that is templated, e.g. Pair<int, string>,
   * this will be in fully expanded form - Pair_int_string { L : int, R : string }
   */
  private var tmplInstantiatedResolvedType: Option[UdtBaseNode] = None

  override def fieldDataType() = AllFieldTypes.udt


  override def fullyQualifiedName = {
    if (_resolvedType.isDefined)
      _resolvedType.get.name.fullyQualifiedName
    else
      UdtName(namespaceNode, name).fullyQualifiedName
  }

  override def udt: IUdtBaseNode = resolvedType.get

  override def udtTemplateInit: IUdtBaseNode = {
    if (tmplInstantiatedResolvedType.isDefined)
      tmplInstantiatedResolvedType.get
    else
      udt
  }

  def version = if (versionNode.isDefined) Some(versionNode.get.number.get.intValue()) else None

  def typeArguments =
    if (typeArgumentsNode.isDefined)
      Some(typeArgumentsNode.get.map(e => e.unwrapTypedef))
    else None

  def typeParamsToArgs: Option[Map[String, IDataType]] = {
    if (typeArgumentsNode.isDefined) {
      val args = typeArgumentsNode.get.map(e => e.unwrapTypedef)
      val params = _resolvedType.get.name.typeParameters.keys.map(_.name.name)
      Some(params.zip(args).toMap)
    }
    else None
  }

  override def hasErrors = {
    if (super.hasErrors)
      true
    else if (_resolvedType.isDefined)
      _resolvedType.get.hasErrors
    else
      false
  }


  def resolvedType: Option[UdtBaseNode] =
    //    if ( tmplInstantiatedResolvedType.isDefined )
    //      tmplInstantiatedResolvedType
    //    else
    _resolvedType

  def tmplResolvedOrResolvedType: Option[UdtBaseNode] =
    if (tmplInstantiatedResolvedType.isDefined)
      tmplInstantiatedResolvedType
    else
      _resolvedType

  override def isUnmodifiedAssignableFrom(other: IAssignable) = {
    assertPreResolved(None)

    if (!hasErrors && _resolvedType.get.isNativeUdt && other.isInstanceOf[ScalarDataType] && other.asInstanceOf[ScalarDataType].isScalarString) {
      true
    }
    else {
      other.isInstanceOf[UdtDataType] &&
        _resolvedType.isDefined &&
        other.asInstanceOf[UdtDataType]._resolvedType.isDefined &&
        _resolvedType.get.isAssignableFrom(other.asInstanceOf[UdtDataType]._resolvedType.get)
    }
  }

  def referencedFromInclude(): Boolean =
    referencedFromInclude(this)

  private def referencedFromInclude(from: ResolvableNode): Boolean = {
    if (from.parent.isInstanceOf[Field])
      false
    else if (from.parent.isInstanceOf[UdtBaseNode])
      true
    else
      referencedFromInclude(from.parent.asInstanceOf[ResolvableNode])
  }

  override def resolvableInnerNodes() = {
    val tas = if (typeArgumentsNode.isDefined)
      typeArgumentsNode.get
    else
      Seq.empty

    val m = if (nodeMeta.isDefined)
      Seq(nodeMeta.get)
    else
      Seq.empty

    m ++ tas
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    val useImports = hasUdtParent

    if (useImports) {
      ctx.registry.pushImports(ctx, locateUdtParent().compUnitImports)
    }
    _resolvedType = ctx.registry.getUdt(Some(this), this, true)

    if (useImports) {
      ctx.registry.popImports()
    }

    if (_resolvedType.isDefined) {

      _resolvedType.get.startPreResolve(ctx, this)

      if (_resolvedType.get.isSynthetic && !synthUdtReference && locateExprParent().isEmpty) {
        ctx.addResolutionError(ResolutionMessage(location,
          ReferenceToAlfatheticDefinition)(None, List.empty, _resolvedType.get.nodeId.id))
      }

      // Instantiate if the target is templated
      if (typeArgumentsNode.isDefined) {
        val templateable = _resolvedType.get

        val typeArgs = typeArgumentsNode.get

        if (templateable.typeParamsNode.get.size != typeArgs.size) {
          ctx.addResolutionError(new ResolutionMessage(this.location, TypeParameterCountMismatch)(None, List.empty, this.name.text,
            templateable.typeParamsNode.get.size.toString, typeArgs.size.toString))
        }

        val tmplMap: Map[String, DataType] = templateable.typeParamsNode.get.map(_.nameNode.text).zip(typeArgs).toMap

        val newObj = templateable.getAsTemplatableUdtAndTemplateInstantiated(ctx, tmplMap)

        tmplInstantiatedResolvedType = Some(newObj)
        tmplInstantiatedResolvedType.get.startPreResolve(ctx, parent)
      }


      //      tmplInstantiatedResolvedType = Some( _resolvedType.get.templateInstantiated )
      //      tmplInstantiatedResolvedType.get.startPreResolve(ctx, this)
    }
  }

  override protected def resolve(ctx: Context): Unit = {
    super.resolve(ctx)
    if (_resolvedType.isDefined)
      _resolvedType.get.startResolve(ctx)
  }

  override protected def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)
    if (_resolvedType.isDefined)
      _resolvedType.get.startPostResolve(ctx)
  }


  override def toString: String = {
    val tmpl = if (typeArgumentsNode.isDefined) TextUtils.mkString(typeArgumentsNode.get) else ""
    val v = if (versionNode.isDefined) "@" + versionNode.get else ""

    val n = UdtDataType.substitute(fullyQualifiedName)
    n + v + tmpl
  }

  def asUnixPath(withExtension: String = ""): String = fullyQualifiedName.replace('.', '/') + withExtension

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType = {
    if (typeArguments.isDefined) {
      val newArgs = typeArgumentsNode.get.map(_.unwrapTypedef.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])
      new UdtDataType(location, namespaceNode, name, versionNode, Some(newArgs))
    }
    else
      this

    //    val tmplType : Option[DataType] = templateArgs.get( udtReference.name.text )
    //
    //    if ( tmplType.isDefined )
    //      tmplType.get
    //    else
    //      this
  }

  override def nodeId = new LocatableNodeIdentity(getClass.getSimpleName, toString)(location)

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
    }
    v.exit(this)
  }

  override def unwrapTypedef: DataType = this


  def getPossibleUdtVersionedNames(): Seq[UdtVersionedName] = {
    // WYSIWYG
    val a = UdtVersionedName(NamespaceNode.empty, name, None, typeArgumentsNode, versionNode, None)

    if (namespaceNode.isEmpty) {
      Seq(a)
    }
    else {
      Seq(a, UdtVersionedName(namespaceNode, name, None, typeArgumentsNode, versionNode, None))
    }
  }


}

/**
 * Models the
 * Foo.Bar of F1 : Foo.Bar
 * Foo.Bar@2 of F2 : Foo.Bar@2
 * Foo.Bar@3< int > of F3 : Foo.Bar@2< int >
 * Foo.Bar< int, Baz< string > > of F4 : Foo.Bar@< int,Baz< string > >
 */

object UdtDataType {

  private val subs = new util.HashMap[String, String]()

  def toStringSubstitution(from: String, to: String): Unit = {
    subs.put(from, to)
  }

  def substitute(from: String): String = {
    subs.getOrDefault(from, from)
  }

  def fromNameAndParam(qualifiedName: String, params: Seq[String]): UdtDataType = {
    new UdtDataType(name = StringNode.create(qualifiedName),
      typeArgumentsNode = Some(params.map(p => TypeParameterDataType(new TypeParameter(nameNode = StringNode.create(p))))))
  }

  def fromName(qualifiedName: String): UdtDataType = {
    new UdtDataType(name = StringNode.create(qualifiedName))
  }

  def fromUdtDataType(resolved: UdtBaseNode): UdtDataType = {
    val nw = TokenImpl.empty
    val udt = new UdtDataType(resolved.declaredRawName.location, NamespaceNode.empty,
      StringNode(nw, resolved.versionedName.fullyQualifiedName), resolved.versionNo, Option.empty)

    udt._resolvedType = Some(resolved)
    udt.safeUpdateAllResolved
    udt
  }
}