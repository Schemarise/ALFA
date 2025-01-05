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

import java.nio.file.Path

import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}

trait IArtifact {
  val pathInRepository: Path
  val artifactReference: ArtifactReference
  val globalDefs: Option[String]
  val allSettings: AllSettings
  val fileIndex: Set[String]

  def getEntryPath(s: String): Path
}
