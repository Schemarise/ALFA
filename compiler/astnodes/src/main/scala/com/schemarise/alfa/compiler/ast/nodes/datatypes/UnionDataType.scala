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

import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.AstUnionType.UnionTypeEnum
import com.schemarise.alfa.compiler.err.{AnonymousUnionTypesMustBeUnique, ResolutionMessage}
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.utils.{LexerUtils, TokenImpl}


case class UnionDataType(location: IToken = TokenImpl.empty,
                         rawfieldsNode: Seq[(Option[StringNode], Option[DataType])],
                         untaggedUnion: Boolean
                        ) extends VectorDataType(None) with IUnionDataType {

  private var _syntheticUnion: Option[SyntheticUnion] = None

  override val vectorType: VectorType = Vectors.union

  override def resolvableInnerNodes() = Seq(_syntheticUnion.get)

  override def fieldDataType() = AllFieldTypes.union

  protected def rawfields = rawfieldsNode

  override def unionType: UnionTypeEnum = if (untaggedUnion) AstUnionType.Untagged else AstUnionType.Tagged

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    UnionDataType(location, rawfields.map(e => {
      val dt = e._2.get.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType]
      (e._1, Some(dt))
    }), untaggedUnion)

  override def toString: String = {
    val s = new StringBuilder
    s.append("union")

    if (untaggedUnion) {
      val body = rawfields.map(f => {
        val t = f._2.get.toString
        t
      }).mkString("< ", " | ", " >")
      s.append(body)

    }
    else {
      val body = rawfields.map(f => {
        val n = if (f._1.isDefined) {
          val nn = f._1.get.text
          if (LexerUtils.keywords.contains(nn)) s"`$nn`" else nn
        } else ""
        val t = if (f._2.isDefined) " : " + f._2.get.toString else ""

        n + t
      }).mkString("< ", ", ", " >")
      s.append(body)
    }

    s.toString
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) =
    if (!other.isInstanceOf[UnionDataType])
      false
    else {
      val othert = other.asInstanceOf[UnionDataType]

      syntheticUnion.isAssignableFrom(othert.syntheticUnion)
    }

  override def unwrapTypedef: DataType = this

  override def syntheticUnion = {
    assertPreResolved(None)
    _syntheticUnion.get
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      if (_syntheticUnion.isDefined)
        _syntheticUnion.get.traverse(v)
      v.exit(this)
    }
  }

  override def preResolve(ctx: Context): Unit = {
    val ai = new AtomicInteger(0)
    val fieldRefs = rawfields.map(e => {
      val n = if (e._1.isDefined) e._1.get else StringNode(TokenImpl.empty, "_" + ai.incrementAndGet())
      val t = if (e._2.isDefined)
        e._2.get
      else {
        val f = ctx.registry.getField(n)
        if (f.isDefined)
          f.get.dataType
        else
          new ErrorableDataType(Right(this))
      }

      val f = new Field(location = n.location, nameNode = n, declDataType = t.unwrapTypedef)
      new FieldOrFieldRef(f)
    })

    val unnamedFields = rawfields.filter(f => f._1.isEmpty).map(f => f._2.get)

    if (unnamedFields.length != unnamedFields.toSet.size)
      ctx.addResolutionError(ResolutionMessage(location, AnonymousUnionTypesMustBeUnique)(None, List.empty))

    val parentUdt: UdtBaseNode = locateUdtParent()
    val parentField: (String, Field) = locateFieldParent(false).get

    val r = new Random()

    val su = new SyntheticUnion(
      location, parentUdt.namespaceNode, NodeMeta.empty, Seq.empty,
      StringNode(parentField._2.location,
        SynthNames.Union + parentUdt.versionedName.name + SyntheticTypeFieldSeparator + parentField._2.name + parentField._1),
      None, None, None, Seq.empty, fieldRefs, unnamedFields.length > 0, untaggedUnion, Seq.empty)

    _syntheticUnion = Some(su)

    su.startPreResolve(ctx, this)

    ctx.registry.registerUdt(_syntheticUnion.get)

    super.preResolve(ctx)
  }

  def fieldNames = _syntheticUnion.get.allAccessibleFields().map(_._2.nameNode).filter(_.location != TokenImpl.empty).toSeq

  def componentTypes = _syntheticUnion.get.allAccessibleFields().map(_._2.dataType).toSeq


  override def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)
    if (_syntheticUnion.isDefined)
      _syntheticUnion.get.startPostResolve(ctx)
  }

  override def taggedFields: Seq[(Option[String], IDataType)] = {
    if (_syntheticUnion.isEmpty)
      Seq()
    else
      _syntheticUnion.get.allFields.map(f => (Some(f._1), f._2.dataType)).toSeq
  }

  override def untaggedTypes: Seq[IDataType] = {
    if (_syntheticUnion.isEmpty)
      Seq()
    else
      _syntheticUnion.get.allFields.values.map(_.dataType).toSeq
  }
}
