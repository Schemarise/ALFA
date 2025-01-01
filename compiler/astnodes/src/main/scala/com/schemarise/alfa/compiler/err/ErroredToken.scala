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
package com.schemarise.alfa.compiler.err

import java.io.File

import com.schemarise.alfa.compiler.utils.antlr.CustomAntlrInputStream
import org.antlr.v4.runtime.Token

class ErroredToken(srcFile: Option[File], line: Int, column: Int,
                   stream: CustomAntlrInputStream,
                   startIndex: Int, endIndex: Int, text: String) extends Token {
  override def getStopIndex = endIndex

  override def getStartIndex = startIndex

  override def getText = text

  override def getCharPositionInLine = column

  override def getLine = line

  override def getInputStream = stream

  override def getChannel = 0

  override def getType = 0

  override def getTokenIndex = 0

  override def getTokenSource = throw new UnsupportedOperationException

  override def toString: String = {
    val sb = new StringBuilder
    if (stream.srcPath.isDefined)
      sb.append(stream.srcPath.get.toString)

    sb.append("@" + line)
    sb.append(":" + column)
    sb.toString()
  }
}
