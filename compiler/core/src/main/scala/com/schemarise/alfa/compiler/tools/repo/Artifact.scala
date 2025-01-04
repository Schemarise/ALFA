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
package com.schemarise.alfa.compiler.tools.repo

import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}
import com.schemarise.alfa.compiler.tools.{AlfaPath, AllSettingsFactory}

import java.nio.file.{FileSystem, Files, Path}
import scala.collection.mutable

case class Artifact(val artifactReference: ArtifactReference)(val fs: FileSystem, val pathInRepository: Path)
  extends IArtifact {

  val allSettings: AllSettings = AllSettingsFactory.fromJsonOrYamlFilePath(
    fs.getPath(AlfaPath.CompilerSettingsPath))

  val fileIndex: Set[String] = new String(
    Files.readAllBytes(
      fs.getPath(AlfaPath.ZipIndexFile))).split("\n").toSet

  val globalDefs = if (Files.exists(fs.getPath(AlfaPath.GlobalDefsFile)))
    Some(new String(Files.readAllBytes(fs.getPath(AlfaPath.GlobalDefsFile))))
  else None

  override def toString: String = {
    val sb = new mutable.StringBuilder()
    sb.append("Artifact:" + artifactReference.asUserNotation + "  Total files:" + fileIndex.size)
    sb.toString()
  }

  def getEntryPath(s: String) = fs.getPath(s)
}
