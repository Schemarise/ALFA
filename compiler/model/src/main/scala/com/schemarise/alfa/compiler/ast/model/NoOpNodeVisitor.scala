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
package com.schemarise.alfa.compiler.ast.model

import java.util
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.expr._
import com.schemarise.alfa.compiler.ast.model.stmt.{IAssignmentDeclarationStatement, ILetDeclarationStatement, IVarDeclarationStatement}
import com.schemarise.alfa.compiler.ast.model.types._
import scala.collection.mutable.ListBuffer

class NoOpNodeVisitor extends NodeVisitor {
  private val path = new util.Stack[INode]()

  def pathEntries(): List[INode] = {
    var l = new ListBuffer[INode]()
    path.forEach(e => l.append(e))
    return l.toList
  }

  def peekPath(): INode = {
    path.peek();
  }

  private def popPath(): Unit = {
    if (!path.empty()) {
      path.pop()
    }
  }

  private def pushPath(n: INode) = {
    path.push(n)
    NodeVisitMode.Continue
  }

  override def enter(e: INamespaceNode): Mode = {
    pushPath(e)
  }

  override def enter(e: IAnnotationDecl): Mode = {
    pushPath(e)
  }

  override def enter(e: IRecord): Mode = {
    pushPath(e)
  }

  override def enter(e: ITrait): Mode = {
    pushPath(e)
  }

  override def enter(e: IEnum): Mode = {
    pushPath(e)

  }

  override def enter(e: IUnion): Mode = {
    pushPath(e)
  }

  override def enter(e: IEntity): Mode = {
    pushPath(e)
  }

  override def enter(e: IKey): Mode = {
    pushPath(e)
  }

  override def enter(e: IService): Mode = {
    pushPath(e)
  }

  override def enter(e: ILibrary): Mode = {
    pushPath(e)
  }

  override def enter(e: ITestcase): Mode = {
    pushPath(e)

  }

  override def enter(e: IField): Mode = {
    pushPath(e)

  }

  override def enter(e: IFormal): Mode = {
    pushPath(e)
  }

  override def enter(e: IMethodDeclaration): Mode = {
    pushPath(e)
  }

  override def enter(e: IMethodSignature): Mode = {
    pushPath(e)
  }

  def enterDataType(dt: IDataType) = {
  }

  override def enter(e: IAnyDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IScalarDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IMapDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IListDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IMetaDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: ISetDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: ITupleDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IUnionDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IEnumDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IEnclosingDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IUdtDataType): Mode = {
    enterDataType(e)
    pushPath(e)
  }

  override def enter(e: IAnnotation): Mode = {
    pushPath(e)
  }

  override def enter(e: IBooleanLiteral): Mode = {
    pushPath(e)
  }

  override def exit(e: IBooleanLiteral): Unit = {
    popPath()
  }

  override def enter(e: IStringLiteral): Mode = {
    pushPath(e)
  }

  override def exit(e: IStringLiteral): Unit = {
    popPath()
  }

  override def enter(e: INumberLiteral): Mode = {
    pushPath(e)
  }

  override def exit(e: IField): Unit = {
    popPath()
  }

  override def exit(e: INumberLiteral): Unit = {
    popPath()
  }

  override def enter(e: IMapExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IMapExpression): Unit = {
    popPath()
  }

  override def enter(e: ISetExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: ISetExpression): Unit = {
    popPath()
  }

  override def enter(e: ISeqExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: ISeqExpression): Unit = {
    popPath()
  }

  override def enter(e: IAssertDeclaration): Mode = {
    pushPath(e)
  }

  override def exit(e: IAssertDeclaration): Unit = {
    popPath()
  }

  override def enter(e: ILinkageDeclaration): Mode = {
    pushPath(e)
  }

  override def exit(e: ILinkageDeclaration): Unit = {
    popPath()
  }

  override def enter(e: IObjectExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IObjectExpression): Unit = {
    popPath()
  }

  override def enter(e: ICharLiteral): Mode = {
    pushPath(e)

  }

  override def exit(e: ICharLiteral): Unit = {
    popPath()
  }

  override def enter(e: ITupleExpression): Mode = {
    pushPath(e)

  }

  override def exit(e: ITupleExpression): Unit = {
    popPath()
  }

  override def enter(e: IQualifiedStringLiteral): Mode = {
    path.push(e)
    if (e.resolvedEnum.isDefined) {
      val en = e.resolvedEnum.get._1
      enter(en)
      exit(en)
    }
    NodeVisitMode.Continue
  }

  override def exit(e: IQualifiedStringLiteral): Unit = {
    popPath()
  }

  override def enter(e: IBlockExpression): Mode = {
    pushPath(e)
  }

  override def enter(e: IStatement): Mode = {
    pushPath(e)
  }

  override def enter(e: IAssignmentExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IAssignmentExpression): Unit = {
    popPath()
  }

  override def enter(e: IIfElseExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IIfElseExpression): Unit = {
    popPath()
  }

  override def enter(e: ILetDeclarationStatement): Mode = {
    pushPath(e)
  }

  override def exit(e: ILetDeclarationStatement): Unit = {
    popPath()
  }


  override def enter(e: IVarDeclarationStatement): Mode = {
    pushPath(e)
  }

  override def exit(e: IVarDeclarationStatement): Unit = {
    popPath()
  }


  override def enter(e: IAssignmentDeclarationStatement): Mode = {
    pushPath(e)
  }

  override def exit(e: IAssignmentDeclarationStatement): Unit = {
    popPath()
  }


  override def enter(e: ILambdaExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: ILambdaExpression): Unit = {
    popPath()
  }

  override def enter(e: IMethodCallExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IMethodCallExpression): Unit = {
    popPath()
  }

  override def enter(e: ILambdaDataType): Mode = {
    pushPath(e)

  }

  override def exit(e: ILambdaDataType): Unit = {
    popPath()
  }

  override def enter(e: IParenthesisExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IParenthesisExpression): Unit = {
    popPath()
  }

  override def enter(e: IMathExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IMathExpression): Unit = {
    popPath()
  }

  override def enter(e: IRelativeExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IRelativeExpression): Unit = {
    popPath()
  }

  override def enter(e: ILogicalExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: ILogicalExpression): Unit = {
    popPath()
  }

  override def enter(e: INewExpression): Mode = {
    pushPath(e)
  }

  override def enter(e: IRaiseExpression): Mode = {
    pushPath(e)
  }

  override def enter(e: IInExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IInExpression): Unit = {
    popPath()
  }

  override def enter(e: IDecisionTableExpression): Mode = {
    pushPath(e)
  }

  override def enter(e: IRangeExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IRangeExpression): Unit = {
    popPath()
  }

  override def enter(e: IPartialRelativeExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: IPartialRelativeExpression): Unit = {
    popPath()
  }

  override def enter(e: IDecisionInputExpression): Mode = {
    pushPath(e)
  }

  override def enter(e: INotExpression): Mode = {
    pushPath(e)
  }

  override def exit(e: INotExpression): Unit = {
    popPath()
  }

  override def enter(e: ITransform): Mode = {
    pushPath(e)
  }

  override def exit(e: ITransform): Unit = {
    popPath()
  }

  override def enter(e: IDataproduct): Mode = {
    pushPath(e)
  }

  override def exit(e: IDataproduct): Unit = {
    popPath()
  }

  override def hashCode(): Int = super.hashCode()

  override def exit(e: INamespaceNode): Unit = popPath()

  override def exit(e: IAnnotationDecl): Unit = popPath()

  override def exit(e: IRecord): Unit = popPath()

  override def exit(e: ITrait): Unit = popPath()

  override def exit(e: IEnum): Unit = popPath()

  override def exit(e: IUnion): Unit = popPath()

  override def exit(e: IEntity): Unit = popPath()

  override def exit(e: IKey): Unit = popPath()

  override def exit(e: IService): Unit = popPath()

  override def exit(e: ILibrary): Unit = popPath()

  override def exit(e: ITestcase): Unit = popPath()

  override def exit(e: IFormal): Unit = popPath()

  override def exit(e: IAnnotation): Unit = popPath()

  override def exit(e: IMethodDeclaration): Unit = popPath()

  override def exit(e: INewExpression): Unit = popPath()

  override def exit(e: IRaiseExpression): Unit = popPath()

  override def exit(e: IBlockExpression): Unit = popPath()

  override def exit(e: IStatement): Unit = popPath()

  override def exit(e: IMethodSignature): Unit = popPath()

  override def exit(e: IScalarDataType): Unit = popPath()

  override def exit(e: IMapDataType): Unit = popPath()

  override def exit(e: IListDataType): Unit = popPath()

  override def exit(e: IMetaDataType): Unit = popPath()

  override def exit(e: ISetDataType): Unit = popPath()

  override def exit(e: ITupleDataType): Unit = popPath()

  override def exit(e: IUnionDataType): Unit = popPath()

  override def exit(e: IEnumDataType): Unit = popPath()

  override def exit(e: IDecisionTableExpression): Unit = popPath()

  override def exit(e: IEnclosingDataType): Unit = popPath()

  override def exit(e: IAnyDataType): Unit = popPath()

  override def exit(e: IDecisionInputExpression): Unit = popPath()

  override def exit(e: IUdtDataType): Unit = popPath()
}

abstract class NoOpUdtVisitor extends NoOpNodeVisitor {

  def enterUdt(e: IUdtBaseNode): Mode

  def exitUdt(e: IUdtBaseNode): Unit

  override def enter(e: IAnnotationDecl): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IRecord): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: ITrait): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IEnum): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IUnion): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IEntity): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IKey): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: IService): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: ILibrary): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def enter(e: ITestcase): Mode = {
    super.enter(e)
    enterUdt(e)
  }

  override def exit(e: IAnnotationDecl): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IRecord): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IDataproduct): Unit = {
    super.exit(e)
  }

  override def exit(e: ITrait): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IEnum): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IUnion): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IEntity): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IKey): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: IService): Unit = {
    super.exit(e)
    exitUdt(e)
  }

  override def exit(e: ILibrary): Unit = {
    super.exit(e)
    exitUdt(e)
  }


}