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

import java.util

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser._
import com.schemarise.alfa.compiler.antlr.{AlfaBaseVisitor, AlfaParser}
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, Scalars}
import com.schemarise.alfa.compiler.ast.nodes.{MethodDeclaration, _}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, EnclosingDataType, ScalarDataType, UdtDataType}
import com.schemarise.alfa.compiler.err.{ParserError, ResolutionMessage, UnexpectedExpressionType}
import com.schemarise.alfa.compiler.utils.{TextUtils, TokenImpl}
import org.antlr.v4.runtime.tree.{ParseTree, TerminalNode}
import org.antlr.v4.runtime.{ParserRuleContext, Token}

import scala.collection.JavaConverters._

abstract class WithContextVisitor[T](resolveCtx: Context) extends AlfaBaseVisitor[T] {

  def hasChildParseException(ctx: ParserRuleContext): Boolean = {
    if (ctx != null && ctx.children != null) {
      val l = ctx.children.asScala.find(c => {
        if (c.isInstanceOf[ParserRuleContext]) {
          val prc = c.asInstanceOf[ParserRuleContext]
          if (prc.exception != null)
            true
          else
            hasChildParseException(prc)
        }
        else
          false
      })
      l.isDefined
    }
    else
      false
  }

  private def idOrQidToStringNode(ctx: AlfaParser.IdOrQidContext): StringNode = {
    val idOnly = ctx.idOnly
    val qid = ctx.QID
    val cqid = ctx.ID_COMPLETION()


    if (idOnly == null && qid == null && cqid == null) {
      val t = readToken(ctx)
      val n = t.getText
      val ren = if (n.length > 2) ". Rename to `" + n + "` to escape using `"
      else " '" + n + "'"
      val rm = new ResolutionMessage(t, ParserError)(None, List.empty, "Keyword in use on or before this token" + ren)
      resolveCtx.addResolutionError(rm)
      StringNode(t, n)
    }
    else if (idOnly != null) {
      StringNode(readToken(idOnly), idOnly.getText())
    }
    else if (cqid != null) {
      StringNode(readToken(cqid.getSymbol()), cqid.toString())
    }
    else {
      StringNode(readToken(qid.getSymbol()), qid.toString())
    }
  }

  private def idOnlyToStringNode(ctx: AlfaParser.IdOnlyContext): StringNode = {
    val t = readToken(ctx.id)
    StringNode(t, t.getText)
  }

  def readStringNode(ctx: IdOnlyContext): StringNode = idOnlyToStringNode(ctx)

  def readOptStringNode(n: IdOnlyContext): Option[StringNode] = {
    if (n == null)
      None
    else
      Some(readStringNode(n))
  }

  def readOptStringNode(n: Token): Option[StringNode] = {
    if (n == null)
      None
    else
      Some(readStringNode(n))
  }

  def readOptVersion(ctx: VersionMarkerContext): Option[IntNode] =
    if (ctx == null)
      None
    else {
      try {
        val ver = Integer.parseInt(ctx.versionNo.getText)
        Some(new IntNode(Scalars.int, Some(ver))(readToken(ctx)))
      } catch {
        case e: Exception => {
          val rm = new ResolutionMessage(readToken(ctx), ParserError)(None, List.empty, "Failed to parse number")
          resolveCtx.addResolutionError(rm)
          None
        }
      }
    }


  def readOptionalStringNode(t: Token) = if (t == null) None else Some(StringNode(readToken(t), TextUtils.removeQuotes(t.getText)))

  def readStringNode(t: Token): StringNode = StringNode(readToken(t), TextUtils.removeQuotes(t.getText))

  def readStringNode(ctx: IdOrQidContext): StringNode = idOrQidToStringNode(ctx)

  def readStringNode(terminal: TerminalNode): StringNode =
    StringNode(readToken(terminal), TextUtils.removeQuotes(terminal.getText))

  def readModifierNodes(ctx: ModifiersContext): Seq[ModifierNode] = {
    if (ctx == null) List.empty[ModifierNode]
    else {
      val v = new ModifiersVisitor(resolveCtx)
      j2sNoParseExcpStream(ctx.modifier()).map(v.visitModifier(_))
    }
  }

  def readStringNodes(contexts: util.List[IdOnlyContext]): Seq[StringNode] = {
    j2sNoParseExcpStream(contexts).map(f => readStringNode(f))
  }

  def readTypeParameters(resolveCtx: Context, ctx: TypeParametersContext, namespace: NamespaceNode): Option[Seq[TypeParameter]] = {
    if (ctx == null)
      None
    else {
      val v = new TypeParameterVisitor(resolveCtx, namespace)
      Some(j2sNoParseExcpStream(ctx.typeParam()).map(v.visitTypeParam(_)))
    }
  }

  def j2sNoParseExcpStream[T <: ParserRuleContext](in: java.util.List[T]): Seq[T] = {
    val l = if (in == null)
      Seq.empty[T]
    else
      in.asScala

    l.filter(_.exception == null)
  }

  def j2sStream[T](in: java.util.List[T]): Seq[T] = {
    if (in == null)
      Seq.empty[T]
    else
      in.asScala
  }

  def readTypeArguments(dataTypeVisitor: DataTypeVisitor, ctx: AlfaParser.TypeArgumentsContext): Option[Seq[DataType]] =
    if (ctx == null) None
    else
      Some(j2sNoParseExcpStream(ctx.fieldType()).map(dataTypeVisitor.visitFieldType(_)))

  def readFieldOrFieldRefs(resolveCtx: Context, namespace: NamespaceNode,
                           children: util.List[ParseTree]): Seq[FieldOrFieldRef] = {
    val v = new FieldOrFieldRefVisitor(resolveCtx, namespace)
    val s1 = j2sStream(children).
      filter(_.isInstanceOf[EnumFieldContext]).
      map(_.asInstanceOf[EnumFieldContext]).
      map(v.visitEnumField(_))

    val s2 = j2sStream(children).
      filter(_.isInstanceOf[FieldContext]).
      map(_.asInstanceOf[FieldContext]).
      map(v.visitField(_))

    s1 ++ s2
  }

  def readNodeMetaDocsOnly(resolveCtx: Context, docCtx: DocstringsContext) = {
    val docVisitor = new DocumentationVisitor(resolveCtx)
    val d = docVisitor.visitDocstrings(docCtx)

    new NodeMeta(topDocs = d)
  }

  def readNodeMeta(resolveCtx: Context, namespace: NamespaceNode,
                   docAnnCtx: DocAndAnnotationsContext, sameLineDocCtx: Sameline_docstringsContext): NodeMeta = {
    val annsVisitor = new AnnotationsVisitor(resolveCtx, namespace)
    val docVisitor = new DocumentationVisitor(resolveCtx)

    val anns = docAnnCtx.annotations()
    val docStrs = docAnnCtx.docstrings()

    new NodeMeta(annsVisitor.visitAnnotations(anns),
      docVisitor.visitDocstrings(docStrs),
      docVisitor.visitSameline_docstrings(sameLineDocCtx)
    )
  }

  def readFieldType(resolveCtx: Context, context: FieldTypeContext, namespace: NamespaceNode): DataType = {
    var v = new DataTypeVisitor(resolveCtx, namespace)
    v.visitFieldType(context)
  }

  def readExtends(ctx: OptExtendsContext, namespace: NamespaceNode): Option[UdtDataType] = {
    if (ctx != null) {
      val oe = ctx.extendOrIncludeDef()
      val nm: Option[NodeMeta] = if (oe.docAndAnnotations() != null)
        Some(readNodeMeta(resolveCtx, namespace, oe.docAndAnnotations(), null))
      else
        None
      var v: DataTypeVisitor = new DataTypeVisitor(resolveCtx, namespace)

      Some(v.visitUdt(oe.idOrQidWithOptTmplArgRefs(), nm))
    }
    else {
      None
    }
  }

  def readScopes(resolveCtx: Context, ctx: OptIncludedByListContext, namespace: NamespaceNode): Option[Seq[UdtDataType]] = {
    if (ctx.scopedef == null)
      None
    else {
      var v: DataTypeVisitor = new DataTypeVisitor(resolveCtx, namespace)
      val s = j2sNoParseExcpStream(ctx.extendOrIncludeDef()).map(i => {
        val nm: Option[NodeMeta] = if (i.docAndAnnotations() != null)
          Some(readNodeMeta(resolveCtx, namespace, i.docAndAnnotations(), null))
        else
          None

        v.visitUdt(i.idOrQidWithOptTmplArgRefs(), nm)
      })
      Some(s)
    }
  }

  def readIncludes(resolveCtx: Context, ctx: OptIncludesListContext, namespace: NamespaceNode): Seq[UdtDataType] = {
    if (ctx == null)
      List.empty
    else {
      var v: DataTypeVisitor = new DataTypeVisitor(resolveCtx, namespace)
      j2sNoParseExcpStream(ctx.extendOrIncludeDef()).map(i => {
        val nm: Option[NodeMeta] = if (i.docAndAnnotations() != null)
          Some(readNodeMeta(resolveCtx, namespace, i.docAndAnnotations(), null))
        else
          None

        v.visitUdt(i.idOrQidWithOptTmplArgRefs(), nm)
      })
    }
  }







  def readMethodSigs(namespace: NamespaceNode, ctx: MethodSignatureContext, imports: Seq[ImportDef]): MethodSignature = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val name: StringNode = readStringNode(ctx.fname)
    val typeParams = readTypeParameters(resolveCtx, ctx.typeParameters(), namespace)

    val v = new FieldOrFieldRefVisitor(resolveCtx, namespace)
    val fields = if (ctx.functionParams() == null) Seq.empty
    else j2sNoParseExcpStream(ctx.functionParams().field()).map(v.visitField(_))


    var ret = new DataTypeVisitor(resolveCtx, namespace)
    var retType = ret.visitFieldType(ctx.returnType)

    new MethodSignature(token, meta, namespace, name, typeParams, None, fields, retType, imports)
  }

  def readToken(ctx: ParserRuleContext): IToken = new TokenImpl(ctx.start, ctx.stop)

  def readToken(token: Token): IToken = new TokenImpl(token)

  def readToken(node: TerminalNode) = new TokenImpl(node.getSymbol)

  def readToken(from: Token, to: Token): IToken = new TokenImpl(from, to)

  def readOptionUdtDataType(ctx: IdOrQidWithOptTmplArgRefsContext, namespace: NamespaceNode): Option[UdtDataType] = {
    if (ctx == null)
      None
    else {
      val v = new DataTypeVisitor(resolveCtx, namespace)
      val s = Some(v.visitUdt(ctx))
      s
    }
  }


  def getAsContext[T](resolveCtx: Context, clz: Class[T], exp: ParserRuleContext): Option[T] = {
    if (clz.isAssignableFrom(exp.getClass))
      Some(exp.asInstanceOf[T])
    else {
      resolveCtx.addResolutionError(ResolutionMessage(readToken(exp),
        UnexpectedExpressionType)(None, List.empty, clz.getSimpleName, exp.getClass.getSimpleName))
      None
    }
  }

  def getAsExpression[T](resolveCtx: Context, clz: Class[T], exp: Expression): Option[T] = {
    if (clz.isAssignableFrom(exp.getClass))
      Some(exp.asInstanceOf[T])
    else {
      resolveCtx.addResolutionError(ResolutionMessage(exp.location,
        UnexpectedExpressionType)(None, List.empty, clz.getSimpleName, exp.getClass.getSimpleName))
      None
    }

  }
}
