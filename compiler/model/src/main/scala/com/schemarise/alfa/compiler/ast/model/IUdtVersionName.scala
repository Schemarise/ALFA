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

import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, IUdtDataType}

trait IUdtVersionName {
  // for identification
  val fullyQualifiedName: String

  val fullyQualifiedNameAndVersion: String

  val name: String

  def version: Option[Int]

  def udtType: UdtType

  /**
   * The < L, R > returned as L -> 0, R -> 1 > from -
   * record Pair< L, R > {
   * Left : L
   * Right : R
   * }
   */
  def typeParameters: Map[ITypeParameter, Int]

  /**
   * The < double, double > returned as 0 -> double, 1 -> double from -
   * record Data {
   * Point : Pair< double, double >
   * }
   */
  def typeArguments: Map[Int, IDataType]

  // utility REMOVE?? TODO
  def asUdtDataType: IUdtDataType

  def namespace: INamespaceNode
}
