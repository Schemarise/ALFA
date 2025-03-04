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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType

trait TemplateableNode {
  def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): TemplateableNode
}

object TemplateableNode {
  def templateInstantiate[T <: TemplateableNode](resolveCtx: Context, e: Option[T], templateArgs: Map[String, DataType]): Option[T] = {
    if (e.isDefined) {
      Some(e.get.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[T])
    } else
      None
  }
}