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
package com.schemarise.alfa.compiler.utils

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.utils.antlr.CustomAntlrInputStream
import org.antlr.v4.runtime.Token

case class TokenImpl(sourceFile: Option[Path],
                     start: Int,
                     end: Int,
                     text: String,
                     startLine: Int,
                     var startColumn: Int,
                     var endLine: Int = -1,
                     var endColumn: Int = -1
                    ) extends IToken {

  if (startLine == endLine && endColumn != -1 && startColumn > endColumn) {
    // Antlr gives this for syntax errors
    val t = startColumn
    startColumn = endColumn
    endColumn = t
  }

  def this(ct: Token) {
    this(ct.getInputStream.asInstanceOf[CustomAntlrInputStream].srcPath,
      ct.getStartIndex,
      ct.getStopIndex,
      ct.getText,
      ct.getLine,
      ct.getCharPositionInLine
    )
  }

  def this(from: Token, to: Token) {
    this(from.getInputStream.asInstanceOf[CustomAntlrInputStream].srcPath,
      from.getStartIndex,
      to.getStopIndex,
      from.getText,
      from.getLine,
      from.getCharPositionInLine,
      to.getLine,
      TokenImpl.adjustEndColumn(from, to)
    )
  }

  override def appendAndCreate(right: IToken): IToken =
    TokenImpl(right.getSourcePath,
      getStartInStream,
      right.getEndInStream,
      getText + right.getText,
      getStartLine,
      getStartColumn,
      right.getEndLine,
      right.getEndColumn
    )

  override def narrowColumnsAndCreate(narrowText: String, startCol: Int, endCol: Int): IToken =
    TokenImpl(getSourcePath,
      getStartInStream,
      getEndInStream,
      narrowText,
      getStartLine,
      startCol,
      getEndLine,
      endCol
    )

  def toStringRelativeTo(relativeTo: Path): String = {
    val sb = new StringBuilder
    if (sourceFile.isDefined) {
      // both need to be absolute otherwise IllegalArgumentException("'other' is different type of Path");
      sb.append(relativeTo.toAbsolutePath.relativize(sourceFile.get.toAbsolutePath).toString)
    }

    buildLoc(sb)
  }

  private def buildLoc(sb: StringBuilder) = {
    sb.append("@" + startLine)
    sb.append(":" + startColumn)
    sb.toString()
  }

  override def toString: String = {
    val sb = new StringBuilder
    if (sourceFile.isDefined)
      sb.append(sourceFile.get.toString)

    buildLoc(sb)
  }


  def toExtendedString: String = {
    val sb = new StringBuilder
    if (sourceFile.isDefined)
      sb.append(sourceFile.get.toString)

    sb.append("@" + startLine)
    sb.append(":" + startColumn)
    sb.append("-")
    sb.append("@" + getEndLine)
    sb.append(":" + getEndColumn)
    sb.toString()
  }

  override def getEndColumn: Int = {
    if (endColumn == -1) {
      endColumn = if (getStartLine == getEndLine)
        startColumn + text.size
      else
        text.size - text.lastIndexOf('\n')
    }

    endColumn
  }

  override def getEndLine: Int = {
    if (endLine == -1) {
      val numberOfNewlines = text.count(c => c == '\n')
      endLine = startLine + numberOfNewlines

      if (text.endsWith("\n"))
        endLine = endLine - 1

    }
    endLine
  }

  override def getText = text

  override def getEndInStream = end

  override def getStartInStream = start

  override def getStartLine = startLine

  override def getSourcePath = sourceFile

  override def getStartColumn = startColumn
}

object TokenImpl {
  val empty = new TokenImpl(None, -1, -1, "", -1, -1)


  private def adjustEndColumn(from: Token, to: Token): Int = {
    if (from == to)
      from.getCharPositionInLine + from.getText.length
    else
      to.getCharPositionInLine
  }
}