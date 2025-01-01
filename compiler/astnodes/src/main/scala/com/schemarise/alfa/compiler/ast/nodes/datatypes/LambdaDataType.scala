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
package com.schemarise.alfa.compiler.ast.nodes.datatypes

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, IAssignable, ILambdaDataType}
import com.schemarise.alfa.compiler.ast.TemplateableNode
import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode}
import com.schemarise.alfa.compiler.types.AllFieldTypes
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.utils.TokenImpl

class LambdaDataType(val location: IToken = TokenImpl.empty,
                     val argTypes: Seq[DataType],
                     val resultType: DataType
                    ) extends DataType with ILambdaDataType {

  override def resolvableInnerNodes() = argTypes ++ Seq(resultType)

  override def unwrapTypedef: DataType = this

  override def toString: String = {
    val args = if (argTypes.isEmpty) "()" else argTypes.mkString(",")
    s"func< $args, ${resultType.toString} >"


  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      argTypes.foreach(_.traverse(v))
      resultType.traverse(v)
    }
    v.exit(this)
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = {
    other match {
      case x: LambdaDataType =>
        if (x == this)
          true
        else
          x.argTypes.equals(this.argTypes) && x.resultType.equals(this.resultType)

      case _ => false
    }
  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode = ???

  override def fieldDataType(): FieldType = AllFieldTypes.lambda
}
