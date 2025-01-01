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

import com.schemarise.alfa.compiler.ast.TableStorageType.StorageType
import com.schemarise.alfa.compiler.ast.nodes.Expression

import scala.collection.immutable.ListMap

class DBTableAnnotation(a: Option[com.schemarise.alfa.compiler.ast.nodes.Annotation]) {
  val obj: ListMap[String, Expression] = ListMap.empty
//
//  def name(default: String): String = {
//    getFieldValue("Name", default)
//  }
//
//  def payloadColumnName(): String = {
//    getFieldValue("PayloadColumnName", "__Payload")
//  }
//
//  def schema(default: String): String = {
//    getFieldValue("Schema", default)
//  }
//
//  private def getFieldValue[T](n: String, default: T) = {
//    if (obj.get(n).isDefined) {
//      obj.get(n).get.asInstanceOf[StringLiteral].rawValue.text.asInstanceOf[T]
//    }
//    else
//      default
//  }
//
//  def storageMode(): StorageType = {
//    val d =
//      if (obj.get("StorageMode").isDefined) {
//        obj.get("StorageMode").get.asInstanceOf[QualifiedIdentifierLiteral].resolvedField.get.name
//      }
//      else
//        "Composite"
//
//    TableStorageType.withEnumName(d)
//  }
//
//  private def fieldNames(n: String) = {
//    if (obj.get(n).isDefined) {
//      val set = obj.get(n).get.asInstanceOf[SetExpression].value
//      set.map(e => {
//        if (e.isInstanceOf[QualifiedIdentifierLiteral])
//          e.asInstanceOf[QualifiedIdentifierLiteral].rawValue.text
//        else
//          e.asInstanceOf[StringLiteral].rawValue.text
//
//      })
//    }
//    else
//      Seq.empty
//  }
//
//  def queryable(): Seq[String] = {
//    fieldNames("Queryable")
//  }
//
//  def indexes() = {
//    ???
//  }
//
//  def partitionFieldsDefined() = {
//    !partitionFields().isEmpty
//  }
//
//  def partitionFields() = {
//    fieldNames("PartitionFields")
//  }
//
//  def partitionExpressionDefined() = {
//    !partitionExpression().isEmpty
//  }
//
//  def partitionExpression() = {
//    getFieldValue("PartitionExpression", "")
//  }
//
//  def clusterFieldsDefined() = {
//    !clusterFields().isEmpty
//  }
//
//  def clusterFields() = {
//    fieldNames("ClusterFields")
//  }
//
//  def optionsDefined() = {
//    !options().isEmpty
//  }
//
//
//  def options() = {
//    fieldNames("Options")
//  }
}
