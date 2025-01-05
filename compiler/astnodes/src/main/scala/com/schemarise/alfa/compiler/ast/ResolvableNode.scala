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
package com.schemarise.alfa.compiler.ast

import com.schemarise.alfa.compiler.{AlfaInternalException, Context}
import com.schemarise.alfa.compiler.ast.ResolutionState.ResolutionState
import com.schemarise.alfa.compiler.ast.ResolveType.ResolveType
import com.schemarise.alfa.compiler.ast.model.{INode, IResolvableNode}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err.{ExpressionError, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.NoErrorsAssertor

import scala.collection.mutable

trait ResolvableNode extends IResolvableNode with BaseNode {
  self =>
  private var preResolveState = ResolutionState.NotStarted
  private var resolveState = ResolutionState.NotStarted
  private var postResolveState = ResolutionState.NotStarted

  private val resolutionErrors = mutable.HashSet[ResolutionMessage]()

  protected[ast] var parent: BaseNode = null

  def addError(err: ResolutionMessage): Unit = resolutionErrors += err

  def hasLocalNodeErrors = {
    resolutionErrors.size > 0
  }

  def safeUpdateAllResolved = {
    preResolveState = ResolutionState.Completed
    resolveState = ResolutionState.Completed
    postResolveState = ResolutionState.Completed
  }

  def hasErrors = {
    if (resolutionErrors.size > 0)
      true
    else if (this.isInstanceOf[INode]) {
      val in = this.asInstanceOf[INode]

      val nea = new NoErrorsAssertor()
      in.traverse(nea)
      nea.foundErrors
    }
    else
      false
  }

  def getErrors = resolutionErrors

  def shouldPreResolve(): Boolean = preResolveState == ResolutionState.NotStarted

  def isPreResolved(): Boolean = preResolveState == ResolutionState.Completed

  def isBeingPreResolved(): Boolean = preResolveState == ResolutionState.Started

  def isPostResolved(): Boolean = postResolveState == ResolutionState.Completed

  def isResolved(): Boolean = resolveState == ResolutionState.Completed


  protected def assertPreResolved(ctx: Option[Context]): Unit = {
    val errorable = !isPreResolved() && !hasErrors

    if (errorable) {
      if (ctx.isDefined && ctx.get.getErrors().size == 0)
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Not preresolved! " + this)
      else if (ctx.isEmpty)
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Not preresolved! " + this)
    }
  }

  protected def assertResolved(ctx: Option[Context]): Unit = {
    val errorable = !isResolved() && !hasErrors

    if (errorable) {
      if (ctx.isDefined && ctx.get.getErrors().size == 0)
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Not resolved! " + this)
      else if (ctx.isEmpty)
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Not resolved! " + this)
    }
  }

  def resolvableInnerNodes(): Seq[ResolvableNode] = Seq.empty

  def preResolve(ctx: Context): Unit = startAnyResolve(ResolveType.PreResolve, ctx, self, resolvableInnerNodes())

  protected def resolve(ctx: Context): Unit = {
    assertPreResolved(Some(ctx))
    startAnyResolve(ResolveType.Resolve, ctx, self, resolvableInnerNodes())
  }

  protected def postResolve(ctx: Context): Unit = {
    assertResolved(Some(ctx))
    startAnyResolve(ResolveType.PostResolve, ctx, self, resolvableInnerNodes())
  }

  private def startSpecificResolve(resType: ResolveType, resState: ResolutionState, ctx: Context, parent: BaseNode,
                                   startedOp: Function0[Unit], completedOp: Function0[Unit]): Unit = {

    if (resState == ResolutionState.NotStarted) {
      startedOp.apply()
      try {
        resType match {
          case ResolveType.PreResolve => {
            this.parent = parent
            preResolve(ctx)
          }
          case ResolveType.Resolve =>
            resolve(ctx)

          case ResolveType.PostResolve =>
            postResolve(ctx)
        }
      } catch {
        case e: Exception =>
          val name = e.getClass.getName
          if (name.startsWith("java.lang") || name.startsWith("java.util") || name.equals(classOf[AlfaInternalException].getName)) {
            ctx.addResolutionError(this, ExpressionError, "Internal error. " + e.getMessage)
            if (ctx.logger.isDebugEnabled) {
              e.printStackTrace()
            }
          }
      }
      completedOp.apply()
    }
    else if (resType == ResolutionState.Completed) {
      throw new IllegalStateException("Already " + resType.toString)
    }
  }

  def startPreResolve(ctx: Context, parent: BaseNode): Unit = {
    if (this == parent)
      throw new AlfaInternalException("Parent cannot be the same as child")

    startSpecificResolve(ResolveType.PreResolve, preResolveState, ctx, parent,
      () => {
        preResolveState = ResolutionState.Started
      },
      () => {
        preResolveState = ResolutionState.Completed
      })
  }

  def startResolve(ctx: Context): Unit = {
    startSpecificResolve(ResolveType.Resolve, resolveState, ctx, parent,
      () => {
        resolveState = ResolutionState.Started
      },
      () => {
        resolveState = ResolutionState.Completed
      })
  }

  def startPostResolve(ctx: Context): Unit = {
    startSpecificResolve(ResolveType.PostResolve, postResolveState, ctx, parent,
      () => {
        postResolveState = ResolutionState.Started
      },
      () => {
        postResolveState = ResolutionState.Completed
      })
  }

  protected def asSeq(items: ResolvableNode*): Seq[ResolvableNode] = items

  protected def asSeq(item: Option[ResolvableNode]): Seq[ResolvableNode] =
    if (item.isDefined) Seq(item.get) else Seq.empty

  //  def locateCompilationUnitParent() : CompilationUnit =
  //    locateCompilationUnitParent( this )
  //
  //  private def locateCompilationUnitParent( from : ResolvableNode ) : CompilationUnit = {
  //    if ( from.parent.isInstanceOf[CompilationUnit])
  //    {
  //      from.parent.asInstanceOf[CompilationUnit]
  //    }
  //    else if ( from.parent.isInstanceOf[ResolvableNode]) {
  //      locateCompilationUnitParent(from.parent.asInstanceOf[ResolvableNode])
  //    }
  //    else {
  //      throw new com.schemarise.alfa.compiler.AlfaInternalException("???")
  //    }
  //  }
  //

  //  def locateStatementParent() : Option[Statement] =
  //    locateStatementParent(this)
  //
  //  private def locateStatementParent(from: ResolvableNode): Option[Statement] = {
  //    if (from.parent.isInstanceOf[Statement]) {
  //      Some(from.parent.asInstanceOf[Statement])
  //    }
  //    else if (from.parent.isInstanceOf[ResolvableNode]) {
  //      locateStatementParent(from.parent.asInstanceOf[ResolvableNode])
  //    }
  //    else
  //      None
  //  }

  def immediateParent(): BaseNode = {
    parent
  }

  def hasUdtParent: Boolean = {
    hasUdtParent(this, this)
  }

  private def hasUdtParent(root: ResolvableNode, from: ResolvableNode): Boolean = {

    if (from.parent.isInstanceOf[UdtBaseNode]) {
      true
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      val next = from.parent.asInstanceOf[ResolvableNode]
      if (next.equals(root))
        throw new AlfaInternalException(next.toString + " has circular parent");

      hasUdtParent(root, next)
    }
    else
      false
  }

  def locateCompUnitParent(): CompilationUnit =
    locateCompUnitParent(this)

  private def locateCompUnitParent(from: ResolvableNode): CompilationUnit = {

    if (from.parent.isInstanceOf[CompilationUnit]) {
      from.parent.asInstanceOf[CompilationUnit]
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateCompUnitParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else if (from.parent.isInstanceOf[NamespaceNode]) {
      locateCompUnitParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      val e = new Exception
      e.printStackTrace()
      throw new com.schemarise.alfa.compiler.AlfaInternalException("locateCompUnitParent - unhandled node from " + from.getClass)
    }
  }


  def locateUdtParent(): UdtBaseNode =
    locateUdt(this).get


  def locateParentNamespaceNode(from: ResolvableNode = this): Option[NamespaceNode] = {

    if (from.parent.isInstanceOf[NamespaceNode]) {
      Some(from.parent.asInstanceOf[NamespaceNode])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateParentNamespaceNode(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }

  def locateMethodSignatureParent(from: ResolvableNode = this): Option[MethodSignature] = {

    if (from.parent.isInstanceOf[MethodSignature]) {
      Some(from.parent.asInstanceOf[MethodSignature])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateMethodSignatureParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }

  def locateMethodDeclParent(from: ResolvableNode = this): Option[MethodDeclaration] = {

    if (from.parent.isInstanceOf[MethodDeclaration]) {
      Some(from.parent.asInstanceOf[MethodDeclaration])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateMethodDeclParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }

  def locateUdt(): Option[UdtBaseNode] = {
    locateUdt(this)
  }

  def locateUdt(from: ResolvableNode): Option[UdtBaseNode] = {

    if (from.parent.isInstanceOf[UdtBaseNode]) {
      Some(from.parent.asInstanceOf[UdtBaseNode])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateUdt(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }

  def locateAssertParent(): Option[AssertDeclaration] =
    locateAssertParent(this)

  private def locateAssertParent(from: ResolvableNode): Option[AssertDeclaration] = {

    if (from.parent.isInstanceOf[AssertDeclaration])
      Some(from.parent.asInstanceOf[AssertDeclaration])
    else if (from.parent.isInstanceOf[ResolvableNode])
      locateAssertParent(from.parent.asInstanceOf[ResolvableNode])
    else
      None
  }


  def locateExprParent(): Option[Expression] =
    locateExprParent(this)

  private def locateExprParent(from: ResolvableNode): Option[Expression] = {
    //    if ( from == parent )
    //      throw new AlfaInternalException("Child == Parent")

    if (from.parent.isInstanceOf[Expression]) {
      Some(from.parent.asInstanceOf[Expression])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateExprParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }


  def locateAnnotationParent(): Option[Annotation] =
    locateAnnotationParent(this)

  private def locateAnnotationParent(from: ResolvableNode): Option[Annotation] = {
    if (from.parent.isInstanceOf[Annotation]) {
      Some(from.parent.asInstanceOf[Annotation])
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateAnnotationParent(from.parent.asInstanceOf[ResolvableNode])
    }
    else {
      None
    }
  }


  def locateFieldParent(strictFieldOnly: Boolean): Option[(String, Field)] = {
    val d = locateFieldParent(this, 0, strictFieldOnly)

    if (d.isEmpty)
      None
    else {
      val id = if (d.get._1 == 0) "" else "" + d.get._1
      Some(id, d.get._2)
    }
  }

  private def locateFieldParent(from: ResolvableNode, level: Int, strictFieldOnly: Boolean): Option[(Int, Field)] = {
    if ((from.parent.isInstanceOf[Field] && !from.parent.isInstanceOf[Formal]) ||
      (from.parent.isInstanceOf[Formal] && !strictFieldOnly)) {
      Some((level, from.parent.asInstanceOf[Field]))
    }
    else if (from.parent.isInstanceOf[ResolvableNode]) {
      locateFieldParent(from.parent.asInstanceOf[ResolvableNode], level + 1, strictFieldOnly)
    }
    else {
      None
    }
  }

  private def startAnyResolve(resType: ResolveType,
                              ctx: Context,
                              parent: BaseNode,
                              items: Seq[ResolvableNode]*): Unit = {
    items.foreach(_.foreach(i => {
      resType match {
        case ResolveType.PreResolve => i.startPreResolve(ctx, parent)
        case ResolveType.Resolve => i.startResolve(ctx)
        case ResolveType.PostResolve => i.startPostResolve(ctx)
      }
    }))
  }
}

object ResolveType extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type ResolveType = Value
  val PreResolve, Resolve, PostResolve = Value
}

object ResolutionState extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type ResolutionState = Value
  val NotStarted, Started, Completed = Value
}
