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
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{IAssignable, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.err.{DataProductUsingInternalType, ExpressionError}
import com.schemarise.alfa.compiler.utils.TokenImpl

import scala.collection.mutable.{HashMap, ListBuffer, MultiMap, Set}

class Dataproduct(ctx: Option[Context] = None,
                  location: IToken = TokenImpl.empty,
                  ns: NamespaceNode = NamespaceNode.empty,
                  nodeMeta: NodeMeta = NodeMeta.empty,
                  nameNode: StringNode,
                  val publish: List[AnnotatedUdtVersionedName],
                  val rawConsume: List[(UdtVersionedName, AnnotatedUdtVersionedName)],
                  versionNo: Option[IntNode] = None,
                  imports: Seq[ImportDef]
                 )
  extends UdtBaseNode(ctx, location, ns, nodeMeta, Seq.empty, nameNode,
    versionNo, None, None, None, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, imports) with IDataproduct {

  override def consume = _consume.toMap

  private val _consume = new HashMap[IUdtVersionName, Set[IAnnotatedUdtVersionName]] with MultiMap[IUdtVersionName, IAnnotatedUdtVersionName]
  private val _consumeProducts = new ListBuffer[Dataproduct]()

  val publishMap = publish.map(e => e.versionedName -> e).toMap

  override def nodeType: Nodes.NodeType = Nodes.Dataproduct


  override protected def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)

    publish.filter(p => {
      !p.hasErrors &&
        p.asUdtDataType.udt.isInternal
    }).foreach(p => {
      ctx.addResolutionError(p.versionedName.location, DataProductUsingInternalType, p.versionedName.fullyQualifiedName)
    })

    rawConsume.foreach(nc => {
      val consumedType = nc._2
      if (consumedType.asUdtDataType.udt.isInternal) {
        ctx.addResolutionError(consumedType.location, DataProductUsingInternalType, consumedType.versionedName.fullyQualifiedName)
      }
    })

  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    rawConsume.foreach(nc => {
      val dpName = nc._1
      val consumedType = nc._2

      nc._2.startPreResolve(ctx, this)

      val p = ctx.registry.getDataproduct(dpName)

      if (p.isDefined) {
        _consumeProducts.append(p.get)

        if (!p.get.isPreResolved())
          p.get.startPreResolve(ctx, p.get.namespaceNode)

        val dn = p.get.publishMap.get(consumedType.versionedName)

        if (dn.isEmpty)
          ctx.addResolutionError(nc._1.location, ExpressionError, s"Dataproduct ${dpName.fullyQualifiedName} does not publish ${nc._2.fullyQualifiedName}")
        else
          _consume.addBinding(dpName, consumedType)
      }

    })
  }

  override def traverse(v: NodeVisitor): Unit = {
    v.enter(this)
    v.exit(this)
  }

  override def resolvableInnerNodes(): Seq[ResolvableNode] = {
    Seq(nodeMeta) ++ publish ++ rawConsume.map(e => e._2) ++ _consumeProducts
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = ???

  override def udtType: UdtType = UdtType.dataproduct

  override def toString: String = {
    val sb = new StringBuilder
    sb ++= annotationNodes.mkString("", "\n", "")

    if (docs.size > 0)
      sb ++= docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")

    val con = consume.map(e => {
      s"""
         |        ${e._1.fullyQualifiedName} {
         |           ${e._2.mkString("\n           ")}
         |        }
       """.stripMargin
    }).mkString("")

    sb ++= "dataproduct " + versionedName.fullyQualifiedName
    sb ++=
      s"""
         |{
         |    publish {
         |        ${publish.mkString("\n        ")}
         |    }
         |
         |    consume {$con
         |    }
         |}
      """.stripMargin

    sb.toString()
  }

  private def rawname = {
    namespaceNode.nameNode.text + nameNode.text
  }

  override def hashCode(): Int = rawname.hashCode

  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[Dataproduct] && obj.asInstanceOf[Dataproduct].rawname == rawname
  }

  /**
   * Includes and Extends are template instantiated, fields should be left-as-is - i.e. templates if they are.
   */
  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context,
                                                              params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = ???
}
