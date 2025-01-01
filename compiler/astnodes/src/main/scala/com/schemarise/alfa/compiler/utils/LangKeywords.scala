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

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object LangKeywords {

  // TODO Should be moved to code generators

  // https://github.com/python/cpython/blob/master/Lib/keyword.py
  val pythonKeywords = Set(
    "False",
    "None",
    "True",
    "and",
    "as",
    "assert",
    "async",
    "await",
    "break",
    "class",
    "continue",
    "def",
    "del",
    "elif",
    "else",
    "except",
    "finally",
    "for",
    "from",
    "global",
    "if",
    "import",
    "in",
    "is",
    "lambda",
    "nonlocal",
    "not",
    "or",
    "pass",
    "raise",
    "return",
    "try",
    "while",
    "with",
    "yield",
    "type"
  )

  // https://docs.oracle.com/javase/specs/jls/se11/html/jls-3.html#jls-3.9
  val javaKeywords = Set(
    "abstract",
    "continue",
    "for",
    "new",
    "switch",
    "assert",
    "default",
    "if",
    "package",
    "synchronized",
    "boolean",
    "do",
    "goto",
    "private",
    "this",
    "break",
    "double",
    "implements",
    "protected",
    "throw",
    "byte",
    "else",
    "import",
    "public",
    "throws",
    "case",
    "enum",
    "instanceof",
    "return",
    "transient",
    "catch",
    "extends",
    "int",
    "short",
    "try",
    "char",
    "final",
    "interface",
    "static",
    "void",
    "class",
    "finally",
    "long",
    "strictfp",
    "volatile",
    "const",
    "float",
    "native",
    "super",
    "while"
  )

  // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#Keywords
  val javascriptKeywords = List(
    "break",
    "case",
    "catch",
    "class",
    "const",
    "continue",
    "debugger",
    "default",
    "delete",
    "do",
    "else",
    "export",
    "extends",
    "finally",
    "for",
    "function",
    "if",
    "import",
    "in",
    "instanceof",
    "new",
    "return",
    "super",
    "switch",
    "this",
    "throw",
    "try",
    "typeof",
    "var",
    "void",
    "while",
    "with",
    "yield",
    "enum",
    "implements",
    "interface",
    "let",
    "package",
    "private",
    "protected",
    "public",
    "static",
    "await",
    "abstract",
    "boolean",
    "byte",
    "char",
    "double",
    "final",
    "float",
    "goto",
    "int",
    "long",
    "native",
    "short",
    "synchronized",
    "throws",
    "transient",
    "volatile",
    "null",
    "true",
    "false"
  )


  val cppKeywords = Set(
    "auto"
  )

  private val rustKeywords = List(
  )

  private val goKeywords = List(
  )

  // https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/
  val csharpKeywords = Set(
    "abstract",
    "as",
    "base",
    "bool",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "checked",
    "class",
    "const",
    "continue",
    "decimal",
    "default",
    "delegate",
    "do",
    "double",
    "else",
    "enum",
    "event",
    "explicit",
    "extern",
    "false",
    "finally",
    "fixed",
    "float",
    "for",
    "foreach",
    "goto",
    "if",
    "implicit",
    "in",
    "int",
    "interface",
    "internal",
    "is",
    "lock",
    "long",
    "namespace",
    "new",
    "null",
    "object",
    "operator",
    "out",
    "override",
    "params",
    "private",
    "protected",
    "public",
    "readonly",
    "ref",
    "return",
    "sbyte",
    "sealed",
    "short",
    "sizeof",
    "stackalloc",
    "static",
    "string",
    "struct",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "uint",
    "ulong",
    "unchecked",
    "unsafe",
    "ushort",
    "using",
    "using",
    "static",
    "virtual",
    "void",
    "volatile",
    "while",
    "add",
    "alias",
    "ascending",
    "async",
    "await",
    "by",
    "descending",
    "dynamic",
    "equals",
    "from",
    "get",
    "global",
    "group",
    "into",
    "join",
    "let",
    "nameof",
    "on",
    "orderby",
    "partial",
    "remove",
    "select",
    "set",
    "value",
    "var",
    "when",
    "where",
    "yield"
  )

  // https://docs.microsoft.com/en-us/dotnet/fsharp/language-reference/keyword-reference
  private val fsharpKeywords = List(
    "abstract",
    "and",
    "as",
    "assert",
    "base",
    "begin",
    "class",
    "default",
    "delegate",
    "do",
    "done",
    "downcast",
    "downto",
    "elif",
    "else",
    "end",
    "exception",
    "extern",
    "FALSE",
    "finally",
    "fixed",
    "for",
    "fun",
    "function",
    "global",
    "if",
    "in",
    "inherit",
    "inline",
    "interface",
    "internal",
    "lazy",
    "let",
    "let!",
    "match",
    "match!",
    "member",
    "module",
    "mutable",
    "namespace",
    "new",
    "not",
    "null",
    "of",
    "open",
    "or",
    "override",
    "private",
    "public",
    "rec",
    "return",
    "return!",
    "select",
    "static",
    "struct",
    "then",
    "to",
    "TRUE",
    "try",
    "type",
    "upcast",
    "use",
    "use!",
    "val",
    "void",
    "when",
    "while",
    "with",
    "yield",
    "asr",
    "land",
    "lor",
    "lsl",
    "lsr",
    "lxor",
    "mod",
    "sig",
    "atomic",
    "break",
    "checked",
    "component",
    "const",
    "constraint",
    "constructor",
    "continue",
    "eager",
    "event",
    "external",
    "functor",
    "include",
    "method",
    "mixin",
    "object",
    "parallel",
    "process",
    "protected",
    "pure",
    "sealed",
    "tailcall",
    "trait",
    "virtual",
    "volatile"
  )

  private val scalaKeywords = List(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "FALSE",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "TRUE",
    "try",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )

  val allKeywords = (
    javaKeywords ++
      pythonKeywords ++
      javascriptKeywords ++
      rustKeywords ++
      goKeywords ++
      scalaKeywords ++
      csharpKeywords ++
      fsharpKeywords ++
      cppKeywords
    ).toSet

  def isTargetLangKeyword(w: String) =
    allKeywords.contains(w)

  def matchingLanguages(w: String): List[String] = {
    val l = new ListBuffer[String]()

    if (javascriptKeywords.contains(w))
      l += ("JavaScript")

    if (javaKeywords.contains(w))
      l += ("Java")

    if (pythonKeywords.contains(w))
      l += ("Python")

    if (rustKeywords.contains(w))
      l += ("Rust")

    if (goKeywords.contains(w))
      l += ("Go")

    if (scalaKeywords.contains(w))
      l += ("Scala")

    if (fsharpKeywords.contains(w))
      l += ("FSharp")

    if (csharpKeywords.contains(w))
      l += ("CSharp")

    l.toList
  }
}
