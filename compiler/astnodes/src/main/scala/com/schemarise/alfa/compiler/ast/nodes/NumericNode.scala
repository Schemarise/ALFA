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

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.BaseNode
import com.schemarise.alfa.compiler.ast.model.types.{Nodes, Scalars}
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType
import com.schemarise.alfa.compiler.utils.TokenImpl

case class NumericNode(val scalarType: ScalarType, val number: Number)(val location: IToken = TokenImpl.empty) extends BaseNode {
  override def nodeType: Nodes.NodeType = Nodes.NumericNode

  override def toString: String = {
    //    if ( ( scalarType == Scalars.double && ( number == Double.MaxValue || number == Double.MinValue ) ) ||
    //         ( scalarType == Scalars.int && ( number == Int.MaxValue || number == Int.MinValue ) ) ||
    //         ( scalarType == Scalars.long && ( number == Long.MaxValue || number == Long.MinValue ) ) ||
    //         ( scalarType == Scalars.short && ( number == Short.MaxValue || number == Short.MinValue ) )
    //    ) {
    //      "*"
    //    } else
    number.toString
  }

  def scalarDataType =
    scalarType match {
      case Scalars.short => ScalarDataType.shortType
      case Scalars.int => ScalarDataType.intType
      case Scalars.long => ScalarDataType.longType
      //      case Scalars.float => ScalarDataType.floatType
      case Scalars.double => ScalarDataType.doubleType
      case Scalars.decimal => ScalarDataType.decimalType
    }

}

case class IntNode(val scalarType: ScalarType = Scalars.int, val number: Option[Int])(val location: IToken = TokenImpl.empty) extends BaseNode {
  override def nodeType: Nodes.NodeType = Nodes.NumericNode

  override def toString: String = if (number.isEmpty) "*" else number.get.toString
}

case class DoubleNode(val scalarType: ScalarType = Scalars.double, val number: Option[Double])(val location: IToken = TokenImpl.empty) extends BaseNode {
  override def nodeType: Nodes.NodeType = Nodes.NumericNode

  override def toString: String = if (number.isEmpty) "*" else number.get.toString
}

case class LongNode(val scalarType: ScalarType, val number: Option[Long])(val location: IToken = TokenImpl.empty) extends BaseNode {
  override def nodeType: Nodes.NodeType = Nodes.NumericNode

  override def toString: String = if (number.isEmpty) "*" else number.get.toString
}

case class ShortNode(val scalarType: ScalarType, val number: Option[Short])(val location: IToken = TokenImpl.empty) extends BaseNode {
  override def nodeType: Nodes.NodeType = Nodes.NumericNode

  override def toString: String = if (number.isEmpty) "*" else number.get.toString
}


object NumericNode {
  def unwrap(version: Option[NumericNode]): Option[Number] = {
    if (version.isDefined)
      Some(version.get.number)
    else
      None
  }

}