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
package com.schemarise.alfa.compiler.ast.antlrvisitors

import com.schemarise.alfa.compiler.{AlfaInternalException, Context}
import com.schemarise.alfa.compiler.antlr.AlfaParser.{CompilationUnitContext, ExpressionUnitContext}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.UnsupportedVersion
import com.schemarise.alfa.compiler.utils.TextUtils

class CompilationUnitVisitor(resolveCtx: Context, hasParseError: Boolean)
  extends WithContextVisitor[CompilationUnit](resolveCtx) {

  override def visitCompilationUnit(ctx: CompilationUnitContext): CompilationUnit = {

    val parentScriptPath = readToken(ctx.start).getSourcePath

    val includedCompUnits: Seq[CompilationUnit] =
      j2sNoParseExcpStream(ctx.include()).flatten(f => resolveCtx.readIncludeScript(parentScriptPath,
        StringNode(readToken(f.includePath), TextUtils.removeQuotes(f.includePath.getText))))

    val rawconstexprsx: Seq[(StringNode, ExpressionUnitContext)] = j2sNoParseExcpStream(ctx.exprconst()).map(
      l => {
        val sn = readStringNode(l.name)
        (sn, l.expr)
      })

    // TOD currently just set as a constant
    val MAX_VERISON = 3

    val langVersion: Option[Int] =
      if (ctx.alfaVersion != null) {
        val ver = Integer.parseInt(ctx.alfaVersion.getText)
        if (ver > MAX_VERISON) {
          resolveCtx.addResolutionError(readToken(ctx.alfaVersion), UnsupportedVersion, ver, MAX_VERISON)
          None
        }
        else
          Some(ver)
      }
      else
        None

    val modelVersion =
      if (ctx.modelVersion != null) {
        Some(readStringNode(ctx.modelVersion))
      }
      else {
        None
      }

    val constexprs = rawconstexprsx.map(e => {
      e._1 -> e._2
    }).toMap

    val imports: Seq[ImportDef] = j2sNoParseExcpStream(ctx.localize()).map(
      l => {
        var name = readStringNode(l.imp)
        if (name.text.endsWith("."))
          name = StringNode.create(name.location, name.text.dropRight(1))

        val wildcard = l.wildcard != null

        new ImportDef(name, wildcard)
      }
    )

    resolveCtx.registry.pushConsts(constexprs)

    val fields = allFields(ctx)
    val typeDefs = allTypeDefs(ctx)
    val extensions = allExtensions(ctx)
    val namespaceGroups = allNamespaceGroups(ctx, imports)

    val udts: Seq[UdtBaseNode] = if (ctx.udt().isEmpty) {
      Seq.empty
    }
    else {
      val uv = new UdtVisitor(resolveCtx, NamespaceNode.empty, imports)

      j2sNoParseExcpStream(ctx.udt())
        .map(uv.visitUdt(_))
        .filter(_.isDefined)
        .map(_.get)
    }


    val noNamespace = if (udts.size == 0) Seq.empty
    else
      Seq(new NamespaceNode(collectedUdts = udts))

    val namespaces = namespaceGroups ++ noNamespace

    resolveCtx.registry.popConsts()

    new CompilationUnit(resolveCtx, readToken(ctx),
      includedCompUnits,
      fields,
      typeDefs,
      namespaces,
      extensions,
      imports,
      rawconstexprsx,
      langVersion,
      modelVersion,
    )
  }

  def allFields(ctx: CompilationUnitContext): Seq[Fields] = {
    if (ctx.fields().isEmpty) {
      Seq.empty
    }
    else {
      val v = new FieldsVisitor(resolveCtx, NamespaceNode.empty)
      j2sNoParseExcpStream(ctx.fields()).map(v.visitFields(_))
    }
  }

  def allExtensions(ctx: CompilationUnitContext): Seq[ExtensionDecl] = {
    val extdv = new ExtensionDeclVisitor(resolveCtx)
    j2sNoParseExcpStream(ctx.extensionDecl()).map(extdv.visitExtensionDecl(_))
  }

  def allTypeDefs(ctx: CompilationUnitContext): Seq[TypeDefs] = {
    if (ctx.typeDefs().isEmpty) {
      Seq.empty
    }
    else {
      val v = new TypeDefsVisitor(resolveCtx, NamespaceNode.empty)
      j2sNoParseExcpStream(ctx.typeDefs()).map(v.visitTypeDefs(_))
    }
  }

  def allNamespaceGroups(ctx: CompilationUnitContext, imports: Seq[ImportDef]): Seq[NamespaceNode] = {
    val v = new NamespaceGroupVisitor(resolveCtx, imports)
    j2sNoParseExcpStream(ctx.namespaceGroup()).map(v.visitNamespaceGroup(_))
  }
}
