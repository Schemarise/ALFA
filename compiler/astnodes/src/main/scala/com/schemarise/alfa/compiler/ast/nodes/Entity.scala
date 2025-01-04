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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.{IEntity, IToken, NodeVisitor, _}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{EntityKeyAndBodyFieldsDuplicate, ExtendedEntityAlreadyDefinesKey, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.{TextUtils, TokenImpl}

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

class Entity(ctx: Option[Context] = None, token: IToken = TokenImpl.empty,
             namespace: NamespaceNode = NamespaceNode.empty,
             nodeMeta: NodeMeta = NodeMeta.empty,
             modifiersNode: Seq[ModifierNode] = Seq.empty,
             name: StringNode,
             versionNo: Option[IntNode] = None,
             typeParams: Option[Seq[TypeParameter]] = None,
             typeArguments: Option[Map[String, DataType]] = None,
             extended: Option[UdtDataType] = None,
             includes: Seq[UdtDataType] = Seq.empty,
             fields: Seq[FieldOrFieldRef] = Seq.empty,
             declKeyDataType: Option[UdtDataType] = None,
             createdKey: Option[Key] = None,
             isSynthKey: Boolean = false,
             assertNode: Seq[AssertDeclaration] = Seq.empty,
             linkageNodes: Seq[LinkageDeclaration] = Seq.empty,
             imports: Seq[ImportDef] = Seq.empty,
             val isEntityUnion: Boolean = false
            ) extends UdtBaseNode(
  ctx, token, namespace, nodeMeta, modifiersNode, name, versionNo, typeParams, typeArguments,
  extended, includes, fields, Seq.empty, assertNode, linkageNodes, imports) with IEntity {

  private val dollarKey: Option[Field] =
    if (declKeyDataType.isDefined)
      Some(Field.of(Expression.DollarKey, declKeyDataType.get, this, true))
    else
      None

  override def keyType: Option[UdtDataType]

  = {
    val k = keyWithPath()
    k
  }

  override def udtType: UdtType

  = UdtType.entity

  override def key = createdKey

  //  override def key: Option[Key] = {
  //    val k = keyWithPath()
  //
  //    if (k.isDefined && k.get.resolvedType.isDefined)
  //      Some(k.get.resolvedType.get.asInstanceOf[Key])
  //    else
  //      None
  //  }

  private def keyWithPath(p: Option[ListBuffer[Entity]] = None): Option[UdtDataType]
  = {
    assertPreResolved(None)

    if (p.isDefined)
      p.get += this

    if (declKeyDataType.isDefined) {
      declKeyDataType
    }
    else
      ancestorKey(p)
  }

  private def ancestorKey(p: Option[ListBuffer[Entity]] = None): Option[UdtDataType]

  = {
    if (extended.isEmpty)
      None
    else if (extended.isDefined && !extended.get.hasErrors && extended.get.isUdtEntity) {
      val sup = extended.get.resolvedType.get.asInstanceOf[Entity]
      sup.keyWithPath(p)
    }
    else
      None
  }

  def isSingleton: Boolean = declKeyDataType.isEmpty && ancestorKey().isEmpty

  override def nodeType: Nodes.NodeType

  = Nodes.Entity

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    val dk = if (dollarKey.isDefined) asSeq(dollarKey.get) else Seq.empty

    val res: Seq[ResolvableNode] = Seq(declKeyDataType, createdKey).filter(e => e.isDefined).map(e => e.get)

    super.resolvableInnerNodes() ++ res ++ dk
  }


  override protected def resolve(ctx: Context): Unit

  = {
    super.resolve(ctx)

    val lb = new ListBuffer[Entity]()
    if (declKeyDataType.isDefined && ancestorKey(Some(lb)).isDefined) {
      ctx.addResolutionError(location, ExtendedEntityAlreadyDefinesKey, versionedName.toString,
        lb.map(e => e.nodeId.id).mkString("", " > ", ""), lb.last.location.toString)
    }

    if (declKeyDataType.isDefined && declKeyDataType.get.resolvedType.isDefined) {
      val keyFields = declKeyDataType.get.resolvedType.get.asInstanceOf[Key].allAccessibleFields()
      val bodyFields = allAccessibleFields()

      val keyAndBodyFieldDups = keyFields.filter(k => {
        val f = bodyFields.get(k._1)
        f.isDefined
      }).map(_._2.nameNode.text)


      if (keyAndBodyFieldDups.size > 0) {
        ctx.addResolutionError(location, EntityKeyAndBodyFieldsDuplicate, keyAndBodyFieldDups.mkString("", ", ", ""))
      }
    }
  }


  //  override def templateInstantiated : Entity = {
  //    if ( ! typeParamsNode.isDefined ) {
  //      this
  //    } else {
  //      val resolveCtx = ctx.get
  //      val tmplMap = typeArgumentsNode.get
  //
  //      val params = filterParamsFromArgs
  //
  //      new Entity(
  //        ctx,
  //        token,
  //        namespace,
  //        nodeMeta,
  //        modifiersNode,
  //        name.templatedName(params, tmplMap.values),
  //        versionNo,
  //        params,
  //        None,
  //        templateInstantiate(extended, resolveCtx, tmplMap),
  //        includes.map(e => e.templateInstantiate(resolveCtx, tmplMap).asInstanceOf[UdtDataType]),
  //        fields.map(e => e.templateInstantiate(resolveCtx, tmplMap)),
  //        methodDecls.map(e => e.templateInstantiate(resolveCtx, tmplMap)),
  //        TemplateableNode.templateInstantiate(resolveCtx, declaredKey, tmplMap),
  //        isSynthKey
  //      )
  //    }
  //  }

  override def traverse(v: NodeVisitor): Unit

  = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      if (declKeyDataType.isDefined)
        declKeyDataType.get.traverse(v)

      traverseBody(v)
    }
    v.exit(this)
  }

  override def toString: String = {
    val sb = new StringBuilder

    if ( annotationNodes.size > 0 )
      sb ++= annotationNodes.mkString("", "\n", "\n")

    if (docs.size > 0)
      sb ++= docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")

    val uni = if (isUnion) "union " else ""

    sb ++= "entity " + uni +
      versionedName.fullyQualifiedName + TextUtils.mkString(typeParams);

    if (declKeyDataType.isDefined) {
      sb ++= " key "

      if (isSynthKey) {
        if (declKeyDataType.isDefined && declKeyDataType.get.resolvedType.isDefined)
          sb ++= declKeyDataType.get.resolvedType.get.rawDeclaredFields.map(e => e.field.get).mkString("( ", ", ", " )")
      }
      else if (key.isDefined && !key.get.includes.isEmpty) { // can be key-less
        val topLevelKey = key.get.includes.head
        sb ++= topLevelKey.fullyQualifiedName
      }
      else if (declKeyDataType.isDefined) {
        sb ++= declKeyDataType.get.name.text
      }
    }

    sb ++= toStringIncludesAndBody

    sb.toString()
  }


  override def allAddressableFields

  = {
    val fs = allAccessibleFields()

    if (dollarKey.isDefined) {
      // WE dont add key fields as entity fields. Use $key instead
      //      key.get.allAccessibleFields() ++ fs
      val d = ListMap[String, Field](Expression.DollarKey -> dollarKey.get)
      d ++ fs
    }
    else
      fs

  }


  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode

  = ???
}
