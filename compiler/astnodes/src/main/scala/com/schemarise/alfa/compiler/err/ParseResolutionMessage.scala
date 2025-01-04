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

import com.schemarise.alfa.compiler.ast.model.IToken

class ParseResolutionMessage(override val location: IToken,
                             override val errorCode: ErrorCode)(
                              val possibleTokens: Set[String], altMsg: String) extends
  ResolutionMessage(location, errorCode)(None, List.empty, altMsg) {
  override def toString: String = {
    //    if (possibleTokens.size > 0) // this error can become too long and meaningless
    //    location + " Syntax error. Expected " + possibleTokens.mkString("", ", ", "")
    //  else

    val i = altMsg.indexOf("expecting")

    val subMsg =
      if (i > 0) {
        // avoid listing all the tokens
        altMsg.substring(0, i)
      } else
        altMsg

    location + " " + subMsg
  }

  def possibleTokensAsArray() = possibleTokens.toArray

}
