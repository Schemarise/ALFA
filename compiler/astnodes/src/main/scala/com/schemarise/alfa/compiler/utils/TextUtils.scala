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


import com.schemarise.alfa.compiler.AlfaInternalException
import org.apache.commons.lang3.StringUtils

object TextUtils {
  def pascalCase(s: String): String =
    s.head.toString.capitalize + s.tail

  def escapeJava(x: String) = {
    x.map(e => {
      if (Character.isJavaIdentifierPart(e))
        e
      else
        ""
    }).mkString

  }

  def toMultilineBlockText(s: String, max: Int = 97, separator: String = "\n") = {
    val x = if (s.length > max) s.substring(0, max) + "..." else s
    x.split("(?<=\\G.{25})").mkString(separator)
  }

  def plainTextSummary(str: String, i: Int, suffix: String, prefix: String) = {
    val s = str.filter(c => {
      Character.isLetterOrDigit(c) || c == ' '
    })

    if (s.length == 0)
      s
    else if (s.length > i)
      prefix + s.substring(0, i) + "..." + suffix
    else
      prefix + s + suffix
  }

  def removeQuotes(t: String): String = removeQuotes(t, "\"")

  def closestMatch(list: Seq[String], text: String) = {

    val d = list.map(n => {
      val distance = StringUtils.getLevenshteinDistance(n, text, 100)
      distance -> n
    }).sortBy(_._1)

    val best = d.filter(e => e._1 <= 2).map(_._2)
    val rest = d.map(_._2)

    if (!best.isEmpty)
      best.take(Math.min(3, best.size))
    else if (!rest.isEmpty)
      rest.take(Math.min(3, rest.size))
    else
      Seq(text)
  }

  def removeQuotes(t: String, quote: String): String = {
    if (t.startsWith(quote) && t.endsWith(quote))
      removeQuotes(t.substring(1, t.length - 1), quote)
    else
      t
  }

  def allButLast(s: String): String = {
    if (!isDotQualified(s))
      throw new AlfaInternalException("String does not contain '.'")
    else {
      s.substring(0, s.lastIndexOf('.'))
    }
  }

  def isDotQualified(str: String) = str.contains(".")

  def mkString[T](items: Seq[T]): String =
    mkString(items, "<", ",", ">")

  def mkString[T](items: Option[Seq[T]]): String =
    if (items.isDefined)
      mkString(items.get, "<", ",", ">")
    else
      ""

  def mkString[T](items: Seq[T], start: String, sep: String, end: String): String =
    if (items.size == 0)
      ""
    else
      items.mkString(start, sep, end)

  def equalsIgnoreWhitespace(l: String, r: String): Boolean = {
    val al = l.replaceAll("\\s+", "")
    val ar = r.replaceAll("\\s+", "")
    val matched = al.contentEquals(ar)

    if ( !matched ) {
      println("LEFT :" + al)
      println("RIGHT:" + ar)
    }
    matched
  }
  
  def validAlfaIdentifier(s: String) =
    LexerUtils.validAlfaIdentifier(s)

}
