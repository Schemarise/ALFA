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

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact

import java.nio.file.Path
import scala.collection.JavaConverters._

object TestCompiler {
  private val compiler = new com.schemarise.alfa.compiler.AlfaCompiler()

  def compileScriptOnly(script: String, failIfErrors: Boolean = true): ICompilationUnitArtifact = {
    val d = compiler.compile(script.stripMargin)
    if (d.getErrorsAsList.size() > 0 && failIfErrors)
      throw new RuntimeException(d.getErrorsAsList.asScala.mkString("\n"))
    d
  }

  def compileScriptOnly(root: Path, script: String): ICompilationUnitArtifact = {
    compiler.compile(root, script.stripMargin)
  }

  def compileScriptOnly(root: Path): ICompilationUnitArtifact = {
    compiler.compile(root)
  }

  def compileValidScript(script: String): ICompilationUnitArtifact = {
    val cua = compiler.compile(script.stripMargin)

    if (cua.hasErrors) {
      throw new Exception(cua.getErrors.mkString("\n", "\n", "\n"))
    }

    if (cua.hasWarnings)
      println(cua.getWarnings.mkString("\n"))

    cua.traverse(new ResolvedAssertor)

    cua
  }

  private def normalize(s: String) = {
    s.trim().replaceAll("\r\n", "\n").replace(" ", "")
  }

  def compileInvalidScript(errMsg: String, script: String): ICompilationUnitArtifact = {
    val cua = compiler.compile(script.stripMargin)

    if (cua.hasErrors) {

      val matches = cua.getErrors.filter(f => normalize(f.toString).trim.equals(normalize(errMsg).trim))

      if (matches.size > 0)
        cua
      else {
        val errs = cua.getErrors.zipWithIndex.map(e => s"    ${e._2 + 1}. '${e._1.toString}'").mkString("\n")
        throw new Exception("Expected to fail with \n'" + errMsg + "'\nbut failed with:\n'" + errs + "'\n")

      }
    }
    else
      throw new Exception("Expected to fail with '" + errMsg + "', but did not")

  }
}
