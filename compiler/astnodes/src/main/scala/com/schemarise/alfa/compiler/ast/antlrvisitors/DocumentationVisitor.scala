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
package com.schemarise.alfa.compiler.ast.antlrvisitors

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes.Documentation
import com.schemarise.alfa.compiler.err.DocTagFormatError
import com.schemarise.alfa.compiler.utils.TokenImpl

class DocumentationVisitor(resolveCtx: Context) extends WithContextVisitor[Seq[Documentation]](resolveCtx) {
  override def visitSameline_docstrings(ctx: AlfaParser.Sameline_docstringsContext): Seq[Documentation] = {
    if (ctx == null || ctx.SAMELINE_DOCSTRING() == null)
      Seq.empty
    else {
      val t = readToken(ctx.SAMELINE_DOCSTRING())
      Seq(new Documentation(t, trimDocTags(t, t.text)))
    }
  }

  override def visitDocstrings(ctx: AlfaParser.DocstringsContext): Seq[Documentation] = {
    if (ctx == null)
      Seq.empty
    else {
      val docs = j2sStream(ctx.DOCSTRING()).map(f => {
        val t = readToken(f.getSymbol)
        val docStr = trimDocTags(t, f.getText)
        new Documentation(t, docStr)

      })

      docs
    }
  }

  private def ltrim(s: String) = s.replaceAll("^\\s+", "")

  private def rtrim(s: String) = s.replaceAll("\\s+$", "")

  private def trimDocTags(t: IToken, v: String): String = {

    val str = if (v.startsWith("##"))
      v.substring(2).trim
    else if (v.startsWith("#")) {
      val a = v.substring(1)
      val offset = if (a.size > 1 && a.charAt(0) == ' ')
        a.substring(1)
      else
        a

      rtrim(offset)

    }
    else {
      val s = "  " + v.substring(2).dropRight(2)
      val splitPre = s.split("\n")

      val split = if (splitPre.size > 1 && splitPre.head.trim.size != 0)
        //        resolveCtx.addResolutionWarning(t, DocTagFormatError, s"After /# the documentation should start on a new line for documentation generation")
        Array((" " * t.getStartColumn) + splitPre.head) ++ splitPre.tail
      else
        splitPre

      val firstLineStringOffsetOpt = split.filter(_.trim.size > 0).map(s => s.size - ltrim(s).size).headOption

      val mdText =
        if (firstLineStringOffsetOpt.isDefined) {
          val firstLineStringOffset = firstLineStringOffsetOpt.get
          split.map(e => {
            if (e.size < firstLineStringOffset && e.trim.size != 0) {
              resolveCtx.addResolutionWarning(t, DocTagFormatError, s"Documentation indentation (line '$e') is not consistent")
              e
            }
            else if (e.size > firstLineStringOffset && e.substring(0, firstLineStringOffset).trim.size != 0)
              resolveCtx.addResolutionWarning(t, DocTagFormatError, s"Documentation indentation cannot start before the offset of the 1st row (line '$e')")

            else if (e.trim.size == 0) {
              e
            }
            else
              e.substring(firstLineStringOffset)
          }).mkString("\n")
        } else {
          split.mkString("\n")
        }

      mdText

    }

    str
  }
}
