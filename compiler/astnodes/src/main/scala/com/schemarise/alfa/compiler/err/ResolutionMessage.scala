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

import java.nio.file.{Path, Paths}
import com.schemarise.alfa.compiler.ast.ResolvableNode
import com.schemarise.alfa.compiler.ast.model.{IResolutionMessage, IResolvableNode, IToken}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.DataType
import com.schemarise.alfa.compiler.utils.{StdoutLogger, TokenImpl}

import scala.collection.JavaConverters._

object ResolutionMessage {
  def apply(target: ResolvableNode, errorCode: ErrorCode, location: IToken = TokenImpl.empty)(completions: List[String], args: Any*): ResolutionMessage = {
    val loc = if (location == TokenImpl.empty) target.location else location
    val rm = new ResolutionMessage(loc, errorCode)(Some(target), completions, args: _*)
    target.addError(rm)
    rm
  }
}

// Use apply - better to be able to mark target node error
@deprecated
case class ResolutionMessage(location: IToken, errorCode: ErrorCode)(var targetNode: Option[ResolvableNode], completions: List[String], args: Any*) extends IResolutionMessage {
  override def toString: String = location + " " + formattedMessage

  private val ignored = 10

  def formattedMessage: String = errorCode.description.format(args: _*)

  def hasCompletions = !completions.isEmpty

  def getCompletionsAsJava = completions.asJava

  def formattedMessage(relativeTo: Path): String = {
    val rel = args.map(e => {
      if (e.isInstanceOf[TokenImpl]) {
        try {
          e.asInstanceOf[TokenImpl].toStringRelativeTo(relativeTo)
        } catch {
          case e: Exception =>
            Console.err.println("Ignoring " + e.getMessage)
            e.toString
        }
      }
      else
        e.toString
    }).toArray
    errorCode.description.format(rel: _*)
  }

  def code = errorCode.toString
}


