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

import com.schemarise.alfa.compiler.ast.model.types.ISizeConstraits
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.IDataTypeSizeRange
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType

abstract class VectorDataType(
                               val sizeRange: Option[IDataTypeSizeRange[Int]]
                             ) extends DataType with ISizeConstraits {
  val vectorType: VectorType

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)
  }


  override def max: Option[_] = {
    if (sizeRange.isDefined)
      sizeRange.get.max
    else
      None
  }

  override def min: Option[_] = {
    if (sizeRange.isDefined)
      sizeRange.get.min
    else
      None
  }

}
