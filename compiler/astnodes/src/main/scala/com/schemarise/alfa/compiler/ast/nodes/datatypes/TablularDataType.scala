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

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, ITabularDataType, IUdtDataType}
import com.schemarise.alfa.compiler.err.{TabularDataTypeOnlyUDTs}
import com.schemarise.alfa.compiler.utils.TokenImpl

class TablularDataType(location: IToken = TokenImpl.empty, declComponentType: DataType)
  extends EnclosingDataType(location, Enclosed.table, declComponentType) with ITabularDataType {

  private var _targetUdt: Option[IUdtDataType] = None

  override def resolve(ctx: Context): Unit = {
    super.resolve(ctx)

    val t = declComponentType.unwrapTypedef

    if (!t.isUdtEntity && !t.isUdtRecord && !t.isUdtKey && !t.isUdtUnion && !t.isUdtTrait) {
      ctx.addResolutionError(this, TabularDataTypeOnlyUDTs)
    }
    else {
      _targetUdt = Some(t.asInstanceOf[IUdtDataType])
    }
  }

  override def targetUdt: IUdtDataType = _targetUdt.get
}
