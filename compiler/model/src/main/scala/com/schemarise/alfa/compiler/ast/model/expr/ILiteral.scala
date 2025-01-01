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
package com.schemarise.alfa.compiler.ast.model.expr

import com.schemarise.alfa.compiler.ast.model.types.IDataType
import com.schemarise.alfa.compiler.ast.model.{IEnum, IField, NormalizedAstPeriod}

import java.net.URI
import java.time._
import java.util.UUID

trait ILiteral extends IExpression {
}

trait IBooleanLiteral extends ILiteral {
  val value: Boolean
}

trait ICharLiteral extends ILiteral {
  val value: Char
}

trait INumberLiteral extends ILiteral {
  def value: Number
}

trait IStringLiteral extends ILiteral {
  def resolvedValue: Any

  def text: String
}

trait IDateLiteral extends ILiteral {
  def value: LocalDate
}

trait IDatetimeLiteral extends ILiteral {
  def value: LocalDateTime
}

trait IDatetimetzLiteral extends ILiteral {
  def value: ZonedDateTime
}

trait ITimeLiteral extends ILiteral {
  def value: LocalTime
}

trait IUriLiteral extends ILiteral {
  def value: URI
}


trait IPatternLiteral extends ILiteral {
  def value: String
}

trait IBinaryLiteral extends ILiteral {
  def value: String
}

trait IUuidLiteral extends ILiteral {
  def value: UUID
}

trait IDurationLiteral extends ILiteral {
  def value: Duration
}

trait IPeriodLiteral extends ILiteral {
  def value: NormalizedAstPeriod
}

trait IQualifiedStringLiteral extends ILiteral {
  def isOptionalChaining: Boolean

  def resolvedEnum: Option[(IEnum, String)]

  def resolvedField: Option[IField]

  def fieldPathTypes(): Seq[(String, IDataType)]
}