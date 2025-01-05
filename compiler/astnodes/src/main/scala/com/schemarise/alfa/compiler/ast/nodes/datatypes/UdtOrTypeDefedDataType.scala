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

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.BaseNode
import com.schemarise.alfa.compiler.ast.model.types.IAssignable
import com.schemarise.alfa.compiler.err.{CyclicDeclaration, ResolutionMessage, UnknownType}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.utils.TokenImpl

// REVISIT ASANKA
class UdtOrTypeDefedDataType(val location: IToken = TokenImpl.empty,
                             namespace: NamespaceNode = NamespaceNode.empty,
                             val name: StringNode,
                             val version: Option[IntNode] = None,
                             val typeArguments: Option[Seq[DataType]] = None) extends DataType {
  //  private val udtDataType : UdtDataType = new UdtDataType( location, namespace, name, version, typeArguments )
  var target: Option[DataType] = None

  override def resolvableInnerNodes() = {
    asSeq(target) ++
      (if (typeArguments.isDefined)
        typeArguments.get
      else
        Seq.empty)
  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType = {
    val other = templateArgs.get(name.text)
    if (other.isDefined)
      other.get
    else
      this
  }

  override def preResolve(ctx: Context): Unit = {
    val searchUdt = new UdtDataType(location, namespace, name, version, typeArguments)

    _typeDefDeclared = Some(false)

    var resolvedUdt = ctx.registry.getUdt(Some(this), searchUdt, false)

    if (resolvedUdt.isEmpty && hasUdtParent) {
      ctx.registry.pushImports(ctx, locateUdtParent().compUnitImports)
      resolvedUdt = ctx.registry.getUdt(Some(this), searchUdt, false)
      ctx.registry.popImports()
    }

    if (resolvedUdt.isDefined) {
      val t = if (resolvedUdt.get.isInstanceOf[TypeParameter])
        new TypeParameterDataType(resolvedUdt.get.asInstanceOf[TypeParameter])
      else
        new UdtDataType(location, resolvedUdt.get.namespaceNode, StringNode.create(resolvedUdt.get.name.name), version, typeArguments)

      t.startPreResolve(ctx, parent)
      target = Some(t)
    }
    else {
      val typeDef = ctx.registry.getTypeDef(ctx, this, searchUdt)

      if (typeDef.isDefined) {
        val typedefInstantiated = typeDef.get.templateInstantiate(ctx, Map.empty).asInstanceOf[DataType]
        typedefInstantiated.typeDefedFrom(searchUdt)
        typedefInstantiated.startPreResolve(ctx, parent)

        if (!typedefInstantiated.isPreResolved()) {
          ctx.addResolutionError(new ResolutionMessage(location, CyclicDeclaration)(None, List.empty, searchUdt.toString, typedefInstantiated.toString))
        }

        _typeDefDeclared = Some(true)
        target = Some(typedefInstantiated)
      }
      else {
        val l = ctx.registry.matchingUdts(searchUdt, None)
        val m = new ResolutionMessage(location, UnknownType)(None, l, toString)
        ctx.addResolutionError(m)
        addError(m)
      }
    }

    super.startPreResolve(ctx, parent)
  }

  private var _typeDefDeclared: Option[Boolean] = None

  def isTypeDefDeclared = _typeDefDeclared.isDefined && _typeDefDeclared.get

  override def fieldDataType(): FieldType = AllFieldTypes.udtOrTypedef

  override def traverse(v: NodeVisitor): Unit = {
    if (target.isDefined)
      target.get.traverse(v)
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = {
    if (this == other) {
      return true
    }
    else if (other.isInstanceOf[UdtOrTypeDefedDataType]) {
      val rhs = other.asInstanceOf[UdtOrTypeDefedDataType]
      if (target.isDefined && rhs.target.isDefined) {
        return target.get.isAssignableFrom(rhs.target.get)
      }
    }

    false
  }

  override def toString: String =
    if (target.isDefined) target.get.toString
    else name.text

  def targetType() = target.get

  override def unwrapTypedef: DataType = {
    if (target.isDefined)
      target.get.unwrapTypedef
    else
      this
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[UdtOrTypeDefedDataType]

  override def equals(other: Any): Boolean = other match {
    case that: UdtOrTypeDefedDataType =>
      (that canEqual this) &&
        name == that.name &&
        version == that.version &&
        typeArguments == that.typeArguments
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, version, typeArguments)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
