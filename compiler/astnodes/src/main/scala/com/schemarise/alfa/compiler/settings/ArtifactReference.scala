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
package com.schemarise.alfa.compiler.settings

import java.nio.file.Path

import com.schemarise.alfa.compiler.err.CompilerSettingsException

case class ArtifactReference(group: String, name: String, version: Option[String] = None)(val artifactPath: Option[Path] = None) {
  override def toString: String =
    group + ":" + name + (if (version.isDefined) ":" + version.get else "")

  def asUserNotation = toString

  def asGroupAndNameAndVersionZipFileName(incGroup: Boolean) = {
    asGroupAndNameAndVersion(incGroup) + ".zip"
  }

  def asGroupAndNameAndVersion(incGroup: Boolean) = {
    val n = name + (if (version.isDefined) "-" + version.get else "")

    if (incGroup)
      group + "-" + n
    else
      n
  }
}

object ArtifactReference {
  def apply(sourcepath: Option[Path], txt: String): ArtifactReference = {
    val nameAndGroupAndVersion = txt.split(":")

    if (nameAndGroupAndVersion.length != 2 && nameAndGroupAndVersion.length != 3)
      throw new CompilerSettingsException("Invalid ArtifactReference " + txt + " " +
        sourcepath.getOrElse("") +
        ", Needs to be separated by at least 1 ':' to extract a group/scope and name and optionally version. E.g. com.acme:DataModel")

    val g = nameAndGroupAndVersion(0)
    val n = nameAndGroupAndVersion(1)

    val v = if (nameAndGroupAndVersion.size == 3)
      Some(nameAndGroupAndVersion(2))
    else
      None

    val p: Option[Path] = None
    new ArtifactReference(g, n, v)(p)
  }
}