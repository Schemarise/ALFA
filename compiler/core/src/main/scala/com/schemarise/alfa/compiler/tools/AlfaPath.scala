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
package com.schemarise.alfa.compiler.tools

import java.nio.file.{Files, Path}
import java.util.regex.Pattern

//import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException

import scala.collection.JavaConverters._

object AlfaPath {
  val SourceFileExtension = ".alfa"
  val GlobalDefsFile = ".alfa-global.alfa"


  val CompilerSettingsFileExtension = ".alfa-proj.yaml"

  val SettingsFileExtPattern = Pattern.compile("([^\\s]+(\\.(?i)(alfa-proj\\.yaml|.alfa-roj\\.json|alfa-proj\\.json))$)");

  val CompilerSettingsFile = "settings" + CompilerSettingsFileExtension
  val CompilerSettingsPath = ".alfa-meta/" + CompilerSettingsFile
  val ZipIndexFile = ".alfa-meta/zip-index.txt"

  val ZipFileExtension = ".alfa-proj.zip"

  def isAlfaSourceFile(p: Path): Boolean = {
    val e = p.toString.endsWith(SourceFileExtension)
    e
  }

  def locateProjectFileInPath(p: Path): Option[Path] = {
    if (Files.isDirectory(p)) {
      val projFiles = Files.newDirectoryStream(p).asScala.filter(e => isAlfaProjectFile(e))

      projFiles.size match {
        case 0 => None
        case 1 => Some(projFiles.head)
        case _ =>
          throw new RuntimeException("More than 1 possible match found for a project file - " + projFiles)
      }
    }
    else
      None
  }

  def isAlfaProjectFile(file: Path) = {
    AlfaPath.SettingsFileExtPattern.asPredicate().test(file.getFileName.toString)
  }

  def containsAlfaResources(p: Path): Boolean = {
    if (Files.isDirectory(p)) {
      if (Files.newDirectoryStream(p).asScala.filter(e => isAlfaProjectFile(e)).size > 0) {
        true
      } else {
        val first = Files.walk(p).filter(e => isAlfaSourceFile(e)).findFirst()
        first.isPresent
      }
    }
    else {
      isAlfaProjectFile(p) || isAlfaSourceFile(p)
    }
  }
}
