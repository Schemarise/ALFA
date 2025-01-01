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

import scala.collection.mutable

case class AllSettings(val projectId: Option[ArtifactReference] = None,
                       val dependencies: Option[Set[ArtifactReference]] = None,
                       val compile: CompileSettings = new CompileSettings(),
                       val exports: GeneratorSettings = new GeneratorSettings(),
                       val imports: ImportSettings = new ImportSettings()
                      ) {
  override def toString: String = toYaml()

  def toYaml(): String = {
    val sb = new mutable.StringBuilder()
    sb.append("---\n")

    if (projectId.isDefined)
      sb.append("name: " + projectId.get.asUserNotation + "\n\n")

    if (dependencies.isDefined) {
      val d = dependencies.get
      sb.append("dependencies:\n")
      d.foreach(x => sb.append(s"- ${x.asUserNotation}\n"))
      sb.append("\n")
    }

    sb.append(compile.toString)
    sb.append(exports.toString)

    sb.toString()
  }
}

object AllSettings {
  def create(projectId: ArtifactReference, dependencies: Array[ArtifactReference]): AllSettings =
    new AllSettings(Some(projectId), Some(dependencies.toSet)
    )
}