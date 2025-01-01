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

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.expr.ISetExpression
import com.schemarise.alfa.compiler.ast.model.stmt.IAssignmentDeclarationStatement
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{ExpressionError, KeysShouldNotBeOptional, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.TokenImpl

class Key(ctx: Option[Context] = None, token: IToken = TokenImpl.empty,
          namespace: NamespaceNode = NamespaceNode.empty,
          nodeMeta: NodeMeta = NodeMeta.empty,
          modifiersNode: Seq[ModifierNode] = Seq.empty,
          nameNode: StringNode,
          typeParamsNode: Option[Seq[TypeParameter]] = None,
          typeArgumentsNode: Option[Map[String, DataType]] = None,
          extendedNode: Option[UdtDataType] = None,
          includesNode: Seq[UdtDataType] = Seq.empty,
          fieldsNode: Seq[FieldOrFieldRef] = Seq.empty,
          genFromEntity: Option[UdtName] = None,
          assertNode: Seq[AssertDeclaration] = Seq.empty,
          linkageNodes: Seq[LinkageDeclaration] = Seq.empty,
          synthetic: Boolean = false,
          imports: Seq[ImportDef] = Seq.empty
         )
  extends UdtBaseNode(
    ctx, token, namespace, nodeMeta, modifiersNode, nameNode, Option.empty, typeParamsNode,
    typeArgumentsNode, extendedNode, includesNode, fieldsNode, Seq.empty, assertNode, linkageNodes, imports) with IKey {

  override def nodeType: Nodes.NodeType = Nodes.Key

  override def udtType: UdtType = UdtType.key

  override def isSynthetic: Boolean = synthetic

  override def isAbstract = genFromEntity.isEmpty

  def entityName: Option[String] =
    if (genFromEntity.isEmpty) None
    else
      Some(genFromEntity.get.fullyQualifiedName)


  override def writeAsModuleDefinition = genFromEntity.isEmpty

  //  override def templateInstantiated : Key = {
  //    if ( ! typeArgumentsNode.isDefined ) {
  //      this
  //    } else {
  //      val resolveCtx = ctx.get
  //      val args = typeArgumentsNode.get
  //
  //      val params = filterParamsFromArgs
  //
  //      val t = new Key(
  //        ctx, token,
  //        namespace,
  //        nodeMeta,
  //        modifiersNode,
  //        nameNode.templatedName(params, args.values),
  //        params,
  //        None,
  //        templateInstantiate( extendedNode, resolveCtx, args ),
  //        includesNode.map(e => e.templateInstantiate(resolveCtx, args).asInstanceOf[UdtDataType] ),
  //        fieldsNode.map(e => e.templateInstantiate(resolveCtx, args)),
  //        methodDecls.map( e => e.templateInstantiate(resolveCtx, args))
  //      )
  //      t
  //    }
  //  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  //  override val isSynthetic = isSynthKey

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    //    val optFields = fieldsNode.filter( fr => fr.field.isDefined &&
    //           fr.field.get.dataType.unwrapTypedef.isInstanceOf[EnclosingDataType] &&
    //           fr.field.get.dataType.unwrapTypedef.asInstanceOf[EnclosingDataType].encType == Enclosed.opt )
    //
    //    optFields.foreach( f =>
    //      ctx.addResolutionError( new ResolutionMessage(f.location,
    //        KeysShouldNotBeOptional)( List.empty, nameNode.text, f.field.get.name ) ) )

    fieldsNode.filter(fr => fr.field.isDefined).map(fn => fn.field.get).foreach(fn => {

      val v = new NoOpNodeVisitor() {

        private def invalid(t: String): Mode = {
          ctx.addResolutionError(fn, ExpressionError, s"A $t field type is reachable via field ${fn.name}")
          NodeVisitMode.Break
        }

        override def enter(e: IMapDataType): Mode = {
          invalid("map")
        }

        override def enter(e: IScalarDataType): Mode = {
          if (e.scalarType == Scalars.double)
            invalid("double")

          NodeVisitMode.Continue
        }

        override def enter(e: IListDataType): Mode = {
          invalid("list")
        }

        override def enter(e: IMetaDataType): Mode = {
          invalid(e.metaType.toString)
          NodeVisitMode.Break
        }

        override def enter(e: IUdtDataType): Mode = {
          if (e.udt.isTrait)
            invalid("trait")
          else if (e.udt.isUnion)
            invalid("union")
          else
            e.udt.traverse(this)

          NodeVisitMode.Continue
        }

        override def enter(e: ISetExpression): Mode = {
          invalid("set")
        }

        override def enter(e: IEnclosingDataType): Mode = {
          if (e.encType == Enclosed.opt) {
            ctx.addResolutionError(fn.location, KeysShouldNotBeOptional, nameNode.text, fn.name)
          }

          NodeVisitMode.Break
        }
      }

      fn.dataType.unwrapTypedef.traverse(v)
    })
  }

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]],
                                                              typeArguments: Map[String, DataType]): UdtBaseNode = {
    assertTrue(typeParamsNode.isDefined && !typeArguments.isEmpty)

    val t = new Key(
      ctx, token,
      namespace,
      nodeMeta,
      modifiersNode,
      nameNode,
      params,
      Some(typeArguments),
      templateInstantiate(extendedNode, resolveCtx, typeArguments),
      includesNode.map(e => e.templateInstantiate(resolveCtx, typeArguments).asInstanceOf[UdtDataType]),
      fieldsNode.map(e => e.templateInstantiate(resolveCtx, typeArguments)), genFromEntity,
      assertNode, Seq.empty, synthetic, imports)

    t
  }
}
