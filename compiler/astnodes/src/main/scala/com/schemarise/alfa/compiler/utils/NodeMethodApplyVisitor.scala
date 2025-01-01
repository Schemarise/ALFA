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
package com.schemarise.alfa.compiler.utils

import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr._
import com.schemarise.alfa.compiler.ast.model.stmt.{IAssignmentDeclarationStatement, ILetDeclarationStatement, IVarDeclarationStatement}
import com.schemarise.alfa.compiler.ast.model.types._

abstract class NodeMethodApplyVisitor extends NodeVisitor {

  def apply(u: Any): Mode

  override def enter(e: INamespaceNode): Mode = {
    NodeVisitMode.Continue
  }

  override def enter(e: IAnnotationDecl): Mode = {
    apply(e)
  }

  override def enter(e: IDataproduct): Mode = {
    apply(e)
  }


  override def enter(e: ITransform): Mode = {
    apply(e)
  }

  override def enter(e: IAnyDataType): Mode = {
    apply(e)
  }

  override def enter(e: IRecord): Mode = {
    apply(e)
  }

  override def enter(e: ITrait): Mode = {
    apply(e)
  }

  override def enter(e: IEnum): Mode = {
    apply(e)
  }

  override def enter(e: IUnion): Mode = {
    apply(e)
  }

  override def enter(e: IEntity): Mode = {
    apply(e)
  }

  override def enter(e: IKey): Mode = {
    apply(e)
  }

  override def enter(e: IService): Mode = {
    apply(e)
  }

  override def enter(e: ILibrary): Mode = {
    apply(e)
  }

  override def enter(e: ITestcase): Mode = {
    apply(e)
  }

  override def enter(e: IField): Mode = {
    apply(e)
  }

  override def enter(e: IFormal): Mode = {
    apply(e)
  }

  override def enter(e: IAnnotation): Mode = {
    apply(e)
  }

  override def enter(e: IMethodDeclaration): Mode = {
    apply(e)
  }

  override def enter(e: INewExpression): Mode = {
    apply(e)
  }

  override def enter(e: IRaiseExpression): Mode = {
    apply(e)
  }

  override def enter(e: IBlockExpression): Mode = {
    apply(e)
  }

  override def enter(e: IStatement): Mode = {
    apply(e)
  }

  override def enter(e: IMethodSignature): Mode = {
    apply(e)
  }

  override def enter(e: IScalarDataType): Mode = {
    apply(e)
  }

  override def enter(e: IMapDataType): Mode = {
    apply(e)
  }

  override def enter(e: IListDataType): Mode = {
    apply(e)
  }

  override def enter(e: IMetaDataType): Mode = {
    apply(e)
  }

  override def enter(e: ISetDataType): Mode = {
    apply(e)
  }

  override def enter(e: ITupleDataType): Mode = {
    apply(e)
  }

  override def enter(e: IUnionDataType): Mode = {
    apply(e)
  }

  override def enter(e: IEnumDataType): Mode = {
    apply(e)
  }

  override def enter(e: IEnclosingDataType): Mode = {
    apply(e)
  }

  override def enter(e: IUdtDataType): Mode = {
    apply(e)
  }

  override def enter(e: ILambdaDataType): Mode = {
    apply(e)
  }

  override def enter(e: IBooleanLiteral): Mode = {
    apply(e)
  }

  override def exit(e: IBooleanLiteral): Unit = {
  }

  override def enter(e: IStringLiteral): Mode = {
    apply(e)
  }

  override def exit(e: IStringLiteral): Unit = {
  }

  override def enter(e: ICharLiteral): Mode = {
    apply(e)
  }

  override def exit(e: ICharLiteral): Unit = {
  }

  override def enter(e: IQualifiedStringLiteral): Mode = {
    apply(e)
  }

  override def exit(e: IQualifiedStringLiteral): Unit = {
  }

  override def enter(e: INumberLiteral): Mode = {
    apply(e)
  }

  override def exit(e: INumberLiteral): Unit = {
  }

  override def enter(e: IMapExpression): Mode = {
    apply(e)
  }

  override def exit(e: IMapExpression): Unit = {
  }

  override def enter(e: ISetExpression): Mode = {
    apply(e)
  }

  override def exit(e: ISetExpression): Unit = {
  }

  override def enter(e: ISeqExpression): Mode = {
    apply(e)
  }

  override def exit(e: ISeqExpression): Unit = {
  }

  override def enter(e: IParenthesisExpression): Mode = {
    apply(e)
  }

  override def exit(e: IParenthesisExpression): Unit = {
  }

  override def enter(e: IMathExpression): Mode = {
    apply(e)
  }

  override def exit(e: IMathExpression): Unit = {
  }

  override def enter(e: IObjectExpression): Mode = {
    apply(e)
  }

  override def exit(e: IObjectExpression): Unit = {
  }

  override def enter(e: IRelativeExpression): Mode = {
    apply(e)
  }

  override def exit(e: IRelativeExpression): Unit = {
  }

  override def enter(e: ITupleExpression): Mode = {
    apply(e)
  }

  override def exit(e: ITupleExpression): Unit = {
  }

  override def enter(e: IAssertDeclaration): Mode = {
    apply(e)
  }

  override def exit(e: IAssertDeclaration): Unit = {
  }

  override def enter(e: ILinkageDeclaration): Mode = {
    apply(e)
  }

  override def exit(e: ILinkageDeclaration): Unit = {
  }

  override def enter(e: IAssignmentExpression): Mode = {
    apply(e)
  }

  override def exit(e: IAssignmentExpression): Unit = {
  }

  override def enter(e: IIfElseExpression): Mode = {
    apply(e)
  }

  override def exit(e: IIfElseExpression): Unit = {
  }

  override def enter(e: ILogicalExpression): Mode = {
    apply(e)
  }

  override def exit(e: ILogicalExpression): Unit = {
  }

  override def enter(e: ILambdaExpression): Mode = {
    apply(e)
  }

  override def exit(e: ILambdaExpression): Unit = {
  }

  override def enter(e: IMethodCallExpression): Mode = {
    apply(e)
  }

  override def exit(e: IMethodCallExpression): Unit = {
  }

  override def enter(e: ILetDeclarationStatement): Mode = {
    apply(e)
  }

  override def exit(e: ILetDeclarationStatement): Unit = {
  }

  override def enter(e: IVarDeclarationStatement): Mode = {
    apply(e)
  }

  override def exit(e: IVarDeclarationStatement): Unit = {
  }

  override def enter(e: IAssignmentDeclarationStatement): Mode = {
    apply(e)
  }

  override def exit(e: IAssignmentDeclarationStatement): Unit = {
  }

  override def enter(e: IInExpression): Mode = {
    apply(e)
  }

  override def exit(e: IInExpression): Unit = {
  }

  override def enter(e: IDecisionTableExpression): Mode = {
    apply(e)
  }

  override def enter(e: IRangeExpression): Mode = {
    apply(e)
  }

  override def exit(e: IRangeExpression): Unit = {
  }

  override def enter(e: IPartialRelativeExpression): Mode = {
    apply(e)
  }

  override def exit(e: IPartialRelativeExpression): Unit = {
  }

  override def enter(e: IDecisionInputExpression): Mode = {
    apply(e)
  }

  override def enter(e: INotExpression): Mode = {
    apply(e)
  }

  override def exit(e: INotExpression): Unit = {
  }
}
