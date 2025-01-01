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
package com.schemarise.alfa.compiler.utils.antlr

import java.io.File
import java.util

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaLexer
import com.schemarise.alfa.compiler.err.{ErroredToken, ParseResolutionMessage, ParserError, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.{LexerUtils, TextUtils, TokenImpl}
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.misc.Interval


class CustomAntlrErrorListener(ctx: Context, stream: CustomAntlrInputStream) extends ANTLRErrorListener {
  private val NO_VIABLE_ALTERNATIVE_AT_INPUT = "no viable alternative at input"
  private val EXTRANEOUS_INPUT = "extraneous input "
  private val TOKEN_RECOGNITION_ERROR = "token recognition error at:"

  override def reportContextSensitivity(parser: Parser, dfa: DFA, i: Int, i1: Int, i2: Int, atnConfigSet: ATNConfigSet): Unit = {

  }

  override def syntaxError(recognizer: Recognizer[_, _],
                           offendingSymbol: Any,
                           line: Int,
                           charPositionInLine: Int,
                           msg: String,
                           e: RecognitionException): Unit = {

    val expecting = " expecting {"
    val expectedWords: Set[String] = if (msg.contains(expecting)) {
      val expected = msg.substring(msg.indexOf(expecting) + expecting.length, msg.lastIndexOf("}"))
      expected.split(", ").
        map(e => TextUtils.removeQuotes(e, "'")).
        toSet.filter(e => !(e.equals("ID") || e.equals("QID") || e.equals("<EOF>")))
    } else
      Set.empty

    val antlrToken: Token = if (offendingSymbol.isInstanceOf[Token])
      offendingSymbol.asInstanceOf[Token]
    else if (e != null && e.getOffendingToken() != null)
      e.getOffendingToken()
    else {
      val slex = recognizer.asInstanceOf[AlfaLexer]
      val text = slex._input.getText(Interval.of(slex._tokenStartCharIndex, slex._input.index))

      new ErroredToken(None, line, charPositionInLine, stream,
        slex._tokenStartCharIndex, slex._input.index(), text)
    }

    val altMsg: String =
      if (msg.startsWith(NO_VIABLE_ALTERNATIVE_AT_INPUT))
        "Syntax error in" + msg.substring(NO_VIABLE_ALTERNATIVE_AT_INPUT.length()) + " at token '" + antlrToken + "'"
      else if (msg.startsWith(TOKEN_RECOGNITION_ERROR))
        "Syntax error in '" + antlrToken.getText + "'"
      else if (msg.startsWith(EXTRANEOUS_INPUT) && msg.indexOf(" expecting ") > 0) {
        val word = msg.substring(EXTRANEOUS_INPUT.length(), msg.indexOf(" expecting ")).trim()
        val v = AlfaLexer.VOCABULARY

        var isKeyword = LexerUtils.keywords.contains(word)

        val tmpMsg: String = "Syntax error : " + msg.substring(0, msg.indexOf(" expecting "))

        if (isKeyword)
          tmpMsg + ". Note " + word + " is a keyword. You may escape it as `" + TextUtils.removeQuotes(word, "'") + "`"
        else
          tmpMsg
      }
      else
        msg

    val tk = new TokenImpl(antlrToken)

    ctx.addResolutionError(new ParseResolutionMessage(tk, ParserError)(expectedWords, altMsg))
  }

  override def reportAttemptingFullContext(parser: Parser, dfa: DFA, i: Int, i1: Int, bitSet: util.BitSet, atnConfigSet: ATNConfigSet): Unit = {

  }

  override def reportAmbiguity(parser: Parser, dfa: DFA, i: Int, i1: Int, exact: Boolean, bitSet: util.BitSet, atnConfigSet: ATNConfigSet): Unit = {
    if (exact) {
      val t = parser.getCurrentToken
      val tk = new TokenImpl(t)
      ctx.addResolutionError(new ResolutionMessage(tk, ParserError)(None, List.empty, "Ambiguous syntax encountered near '" + t.getText + "'"))
    }
  }
}
