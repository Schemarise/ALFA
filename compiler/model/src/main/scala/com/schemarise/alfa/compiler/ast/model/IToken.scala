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

import com.schemarise.alfa.compiler.ast.model.expr.IBlockExpression

import java.nio.file.Path

trait IToken {

  def getText: String

  def getEndInStream: Int

  def getStartInStream: Int

  def getStartColumn: Int

  def getEndColumn: Int

  def getEndLine: Int

  def getStartLine: Int

  def getSourcePath: Option[Path]

  def appendAndCreate(t: IToken): IToken

  def narrowColumnsAndCreate(narrowText: String, start: Int, end: Int): IToken

  def enclosedWithin(e: IToken): Boolean = {
    if (getSourcePath.equals(e.getSourcePath)) {
      val within = this.getStartInStream >= e.getStartInStream && this.getEndInStream <= e.getEndInStream
      within
    }
    else {
      false
    }
  }
}
