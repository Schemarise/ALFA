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

import Enclosed.EnclosedType

trait IEnclosingDataType extends IDataType with IContainerDataType {

  def encType: EnclosedType

  def isOptional: Boolean

  //  def isKey: Boolean

  def isTabular: Boolean

  def isStream: Boolean

  def isFuture: Boolean

  def isEncrypt: Boolean

  def isCompress: Boolean

  def isTry: Boolean

  def isEither: Boolean

  def isPair: Boolean

}
