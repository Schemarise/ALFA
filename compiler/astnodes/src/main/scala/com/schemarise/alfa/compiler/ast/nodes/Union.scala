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
import com.schemarise.alfa.compiler.ast.model.types.{AstUnionType, IDataType, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.AstUnionType.UnionTypeEnum
import com.schemarise.alfa.compiler.ast.model.{IToken, IUnion, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{DuplicateUnionType, ResolutionMessage, UnionCannotHaveOptionalFields}
import com.schemarise.alfa.compiler.utils.{TextUtils, TokenImpl}

import scala.collection.mutable.ListBuffer

class Union(ctx: Option[Context] = None,
            token: IToken = TokenImpl.empty,
            namespace: NamespaceNode = NamespaceNode.empty,
            nodeMeta: NodeMeta = NodeMeta.empty,
            modifiersNode: Seq[ModifierNode] = Seq.empty,
            nameNode: StringNode,
            versionNo: Option[IntNode] = None,
            typeParamsNode: Option[Seq[TypeParameter]] = None,
            typeArgumentsNode: Option[Map[String, DataType]] = None,
            extendedNode: Option[UdtDataType] = None,
            includesNode: Seq[UdtDataType] = Seq.empty,
            fieldsNode: Seq[FieldOrFieldRef] = Seq.empty,
            assertNode: Seq[AssertDeclaration] = Seq.empty,
            untaggedUnion: Boolean = false,
            imports: Seq[ImportDef] = Seq.empty
           )
  extends UdtBaseNode(
    ctx, token, namespace, nodeMeta, modifiersNode, nameNode, versionNo, typeParamsNode, typeArgumentsNode,
    extendedNode, includesNode, fieldsNode, Seq.empty, assertNode, Seq.empty, imports) with IUnion {

  override def nodeType: Nodes.NodeType = Nodes.Union

  override def udtType: UdtType = if (untaggedUnion) UdtType.untaggedUnion else UdtType.union

  override def resolvableInnerNodes() = super.resolvableInnerNodes()

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)

    var l = new ListBuffer[DataType]

    if (untaggedUnion) {
      val typesOnly = allFields.values.map(_.dataType)
      typesOnly.foreach(t => {
        val fnd = l.filter(d => d.isAssignableFrom(t) && t.isAssignableFrom(d))
        if (fnd.size > 0) {
          ctx.addResolutionError(t, DuplicateUnionType, t.toString, fnd.head.toString)
        }
        l += t
      })
    }

    allAccessibleFields().foreach(e => {
      val td = e._2.dataType.unwrapTypedef
      td.whenEncOptional(e => {
        val f = e.asInstanceOf[DataType] locateFieldParent (true)
        val fn = if (f.isDefined) f.get._2.name else ""
        val loc = if (f.isDefined) f.get._2.location else this.location

        ctx.addResolutionError(ResolutionMessage(loc, UnionCannotHaveOptionalFields)(None, List.empty, fn))
      })
    })
  }

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = ???

  override def unionType: UnionTypeEnum = if (untaggedUnion) AstUnionType.Untagged else AstUnionType.Tagged

  override def untaggedTypes: Seq[IDataType] = allFields.values.map(_.dataType).toSeq

  override def toString: String = {
    if (untaggedUnion) {
      val sb = new StringBuilder


      sb ++= annotationNodes.mkString("", "\n", "")

      if (docs.size > 0)
        sb ++= docs.map("  " + _.toString).mkString("\n/#\n ", "\n", "\n #/")

      var mods = modifiersNode.map(m => m.modifier.toString.toLowerCase()).mkString("", " ", " ")
      if (mods.trim.isBlank)
        mods = ""

      sb ++= modifiersNode.map(m => m.modifier.toString.toLowerCase()).mkString("", " ", "")

      sb ++= "\n" + mods + name.udtType.toString.toLowerCase + " " +
        versionedName.fullyQualifiedName + TextUtils.mkString(typeParamsNode);

      sb ++= " {\n"
      sb ++= "    " + untaggedTypes.map(_.toString).mkString(" | ")
      sb ++= "\n}"

      sb.toString()
    }
    else {
      super.toString
    }
  }
}
