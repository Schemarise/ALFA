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

import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.expr._
import com.schemarise.alfa.compiler.ast.model.stmt.{IAssignmentDeclarationStatement, ILetDeclarationStatement, IVarDeclarationStatement}
import com.schemarise.alfa.compiler.ast.model.types._

trait NodeVisitor {
  def enter(e: INamespaceNode): Mode

  def exit(e: INamespaceNode): Unit = {}

  def enter(e: IAnnotationDecl): Mode

  def exit(e: IAnnotationDecl): Unit = {}

  def enter(e: IRecord): Mode

  def exit(e: IRecord): Unit = {}

  def enter(e: IDataproduct): Mode

  def exit(e: IDataproduct): Unit = {}

  def enter(e: ITrait): Mode

  def exit(e: ITrait): Unit = {}

  def enter(e: IEnum): Mode

  def exit(e: IEnum): Unit = {}

  def enter(e: IUnion): Mode

  def exit(e: IUnion): Unit = {}

  def enter(e: IEntity): Mode

  def exit(e: IEntity): Unit = {}

  def enter(e: ITransform): Mode

  def exit(e: ITransform): Unit = {}

  def enter(e: IKey): Mode

  def exit(e: IKey): Unit = {}

  def enter(e: IService): Mode

  def exit(e: IService): Unit = {}

  def enter(e: ILibrary): Mode

  def exit(e: ILibrary): Unit = {}

  def enter(e: ITestcase): Mode

  def exit(e: ITestcase): Unit = {}

  def enter(e: IField): Mode

  def exit(e: IField): Unit = {}

  def enter(e: IFormal): Mode

  def exit(e: IFormal): Unit = {}

  def enter(e: IAnnotation): Mode

  def exit(e: IAnnotation): Unit = {}

  def enter(e: IMethodDeclaration): Mode

  def exit(e: IMethodDeclaration): Unit = {}

  def enter(e: INewExpression): Mode

  def exit(e: INewExpression): Unit = {}

  def enter(e: IRaiseExpression): Mode

  def exit(e: IRaiseExpression): Unit = {}

  def enter(e: IBlockExpression): Mode

  def exit(e: IBlockExpression): Unit = {}

  def enter(e: IStatement): Mode

  def exit(e: IStatement): Unit = {}

  def enter(e: IMethodSignature): Mode

  def exit(e: IMethodSignature): Unit = {}

  // data types
  def enter(e: IScalarDataType): Mode

  def exit(e: IScalarDataType): Unit = {}

  def enter(e: IMapDataType): Mode

  def exit(e: IMapDataType): Unit = {}

  def enter(e: IListDataType): Mode

  def exit(e: IListDataType): Unit = {}

  def enter(e: IMetaDataType): Mode

  def exit(e: IMetaDataType): Unit = {}

  def enter(e: ISetDataType): Mode

  def exit(e: ISetDataType): Unit = {}

  def enter(e: ITupleDataType): Mode

  def exit(e: ITupleDataType): Unit = {}

  def enter(e: IUnionDataType): Mode

  def exit(e: IUnionDataType): Unit = {}

  def enter(e: IEnumDataType): Mode

  def exit(e: IEnumDataType): Unit = {}

  def enter(e: IDecisionTableExpression): Mode

  def exit(e: IDecisionTableExpression): Unit = {}

  def enter(e: IEnclosingDataType): Mode

  def exit(e: IEnclosingDataType): Unit = {}

  def enter(e: IAnyDataType): Mode

  def exit(e: IAnyDataType): Unit = {}

  def enter(e: IDecisionInputExpression): Mode

  def exit(e: IDecisionInputExpression): Unit = {}

  def enter(e: IUdtDataType): Mode

  def exit(e: IUdtDataType): Unit = {}

  def enter(e: ILambdaDataType): Mode

  def exit(e: ILambdaDataType): Unit = {}

  // expressions
  def enter(e: IBooleanLiteral): Mode

  def exit(e: IBooleanLiteral): Unit

  def enter(e: IStringLiteral): Mode

  def exit(e: IStringLiteral): Unit

  def enter(e: ICharLiteral): Mode

  def exit(e: ICharLiteral): Unit

  def enter(e: IQualifiedStringLiteral): Mode

  def exit(e: IQualifiedStringLiteral): Unit

  def enter(e: INumberLiteral): Mode

  def exit(e: INumberLiteral): Unit

  def enter(e: IMapExpression): Mode

  def exit(e: IMapExpression): Unit

  def enter(e: ISetExpression): Mode

  def exit(e: ISetExpression): Unit

  def enter(e: ISeqExpression): Mode

  def exit(e: ISeqExpression): Unit

  def enter(e: IParenthesisExpression): Mode

  def exit(e: IParenthesisExpression): Unit

  def enter(e: IMathExpression): Mode

  def exit(e: IMathExpression): Unit

  def enter(e: IObjectExpression): Mode

  def exit(e: IObjectExpression): Unit

  def enter(e: IRelativeExpression): Mode

  def exit(e: IRelativeExpression): Unit

  def enter(e: ITupleExpression): Mode

  def exit(e: ITupleExpression): Unit

  def enter(e: IAssertDeclaration): Mode

  def exit(e: IAssertDeclaration): Unit

  def enter(e: ILinkageDeclaration): Mode

  def exit(e: ILinkageDeclaration): Unit

  def enter(e: IAssignmentExpression): Mode

  def exit(e: IAssignmentExpression): Unit

  def enter(e: IIfElseExpression): Mode

  def exit(e: IIfElseExpression): Unit

  def enter(e: INotExpression): Mode

  def exit(e: INotExpression): Unit

  def enter(e: ILogicalExpression): Mode

  def exit(e: ILogicalExpression): Unit

  def enter(e: IInExpression): Mode

  def exit(e: IInExpression): Unit

  def enter(e: ILambdaExpression): Mode

  def exit(e: ILambdaExpression): Unit

  def enter(e: IMethodCallExpression): Mode

  def exit(e: IMethodCallExpression): Unit

  def enter(e: ILetDeclarationStatement): Mode

  def exit(e: ILetDeclarationStatement): Unit

  def enter(e: IVarDeclarationStatement): Mode

  def exit(e: IVarDeclarationStatement): Unit

  def enter(e: IAssignmentDeclarationStatement): Mode

  def exit(e: IAssignmentDeclarationStatement): Unit


  def enter(e: IRangeExpression): Mode

  def exit(e: IRangeExpression): Unit


  def enter(e: IPartialRelativeExpression): Mode

  def exit(e: IPartialRelativeExpression): Unit
}
