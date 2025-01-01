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
import com.schemarise.alfa.compiler.antlr.AlfaParser.ExpressionUnitContext
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.err.{DuplicateEntry}
import com.schemarise.alfa.compiler.utils.{LexerUtils, TokenImpl}

import scala.collection.mutable.ListBuffer

class CompilationUnit(
                       ctx: Context,
                       val location: IToken = TokenImpl.empty,
                       val includedCompUnits: Seq[CompilationUnit] = Seq.empty,
                       val fields: Seq[Fields] = Seq.empty,
                       val typeDefs: Seq[TypeDefs] = Seq.empty,
                       val namespaces: Seq[NamespaceNode] = Seq.empty,
                       val extensions: Seq[ExtensionDecl] = Seq.empty,
                       val imports: Seq[ImportDef] = Seq.empty,
                       val rawconstexprsx : Seq[( StringNode, ExpressionUnitContext )] = Seq.empty,
                       val languageVersion : Option[Int] = None,
                       val modelVersion : Option[StringNode] = None
                     )
  extends BaseNode
    with ResolvableNode
    with TraversableNode {

  private val constexprs = rawconstexprsx.map( e => { e._1 -> e._2 } ).toMap

  override def preResolve(ctx: Context): Unit = {
    includedCompUnits.foreach(e => {
      e.startPreResolve(ctx, this)
    })

    val dups = rawconstexprsx.groupBy( e => e._1 ).filter( e => e._2.size > 1 )
    dups.foreach( e => {
      ctx.addResolutionError(e._1.location, DuplicateEntry, "const", e._1.text  )
    })

//    if ( imports.size > 0 )
//      println()
    ctx.registry.pushImports(ctx, imports)
    ctx.registry.pushConsts(constexprs)

    super.preResolve(ctx)
    ctx.registry.popImports()
    ctx.registry.popConsts()
  }

  protected override def resolve(ctx: Context): Unit = {
    includedCompUnits.foreach(e => {
      e.startResolve(ctx)
    })
    ctx.registry.pushImports(ctx, imports)
    ctx.registry.pushConsts(constexprs)

    super.resolve(ctx)

    ctx.registry.popImports()
    ctx.registry.popConsts()
  }

  protected override def postResolve(ctx: Context): Unit = {
    includedCompUnits.foreach(e => {
      e.startPostResolve(ctx)
    })
    ctx.registry.pushImports(ctx, imports)
    ctx.registry.pushConsts(constexprs)

    super.postResolve(ctx)

    ctx.registry.popImports()
    ctx.registry.popConsts()
  }

  override def nodeType: Nodes.NodeType = Nodes.CompilationUnitNode

  override def resolvableInnerNodes() = fields ++ typeDefs ++ extensions ++ namespaces

  override def toString: String = {
    val sb = new StringBuilder

    if ( languageVersion.isDefined ) {
      sb.append(s"""language-version ${languageVersion.get}\n\n""")
    }

    if ( modelVersion.isDefined ) {
      sb.append(s"""model-id "${modelVersion.get.text}"\n\n""")
    }

    imports.distinct.foreach( i => {
      val escapedNs = i.name.origString.split("\\.").map( e => LexerUtils.validAlfaIdentifier(e) ).mkString(".")

      val wildcard =
        if ( i.hasWildcard )
          ".*"
        else
          ""

      sb.append(s"import ${escapedNs}$wildcard\n")
    })

    if ( !imports.isEmpty ) sb.append("\n")

    resolvableInnerNodes.foreach(sb ++= _.toString)

    sb.toString
  }

  private def collectAllNamespaces(visitedCompUnits: ListBuffer[CompilationUnit], allNamespaces: ListBuffer[NamespaceNode], cu: CompilationUnit): Unit = {
    if (!visitedCompUnits.contains(cu)) {
      allNamespaces ++= cu.namespaces
      visitedCompUnits += cu
      cu.includedCompUnits.foreach(icu => {
        collectAllNamespaces(visitedCompUnits, allNamespaces, icu)
      })
    }
  }

  def makeNamespaceNode(e: String, grouped: Map[String, Seq[NamespaceNode]]) : NamespaceNode = {
    val udts = grouped.get(e).get.map(_.udts).flatten

    val latestOnly = udts.filter( e => e.name.versionNode.isEmpty )

    new NamespaceNode(
      meta = new NodeMeta(
        grouped.get(e).get.map(f => f.meta.annotations).flatten,
        grouped.get(e).get.map(g => g.meta.docs).flatten),
      nameNode = StringNode(TokenImpl.empty, e),
      isSynthetic = true,
      collectedUdts = latestOnly)
  }

  override def traverse(v: NodeVisitor): Unit = {
    val allNamespaces = new ListBuffer[NamespaceNode]()
    val visitedCompUnits = new ListBuffer[CompilationUnit]()
    collectAllNamespaces(visitedCompUnits, allNamespaces, this)


    val groupedNss: Map[String, Seq[NamespaceNode]] = allNamespaces.groupBy(_.nameNode.text)

    val aggregatedNss: Set[NamespaceNode] = groupedNss.keySet.map(e => makeNamespaceNode(e, groupedNss) )

    aggregatedNss.toSeq.sortWith(_.nameNode.text < _.nameNode.text).foreach( e => {
      e.traverse(v)
    })
  }
}
