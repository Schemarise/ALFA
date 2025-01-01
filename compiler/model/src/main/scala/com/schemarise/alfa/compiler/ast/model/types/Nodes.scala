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
package com.schemarise.alfa.compiler.ast.model.types

object Nodes extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type NodeType = Value

  val
  CompilationUnitArtifactNode,
  CompilationUnitNode,
  NumericNode,
  TemplateParameterType,
  NodeMeta,
  LiteralNode,
  ValueNode,
  StringNode,
  Namespace,
  FieldsNode,
  TypeDefsNode,

  Extension,
  ExtensionInstance,
  Annotation,
  Service,
  Library,
  Testcase,
  Trait,
  Enum,
  Dataproduct,
  Record,
  Entity,
  Union,
  Key,
  Transform,
  NativeUdt,
  Type,

  SyntheticRecordNode,
  SyntheticUnionNode,
  SyntheticEnumNode,

  Field,
  ExpressionNode,
  FieldOrRefNode,
  DataTypeNode,
  AnnotationInstance,
  Documentation,
  AnnotationEntryNode,

  FormalNode,
  Method,
  ModifierNode,

  AssertNode,
  MethodDeclarationNode,
  Block,

  Statement,

  ThisExpr,
  AssignmentExpr,
  MathExpr,
  EqualityExpr,
  RelativeExpr,
  LogicalExpr,
  MethodCallExpr,
  LambdaExpr

  = Value

}
