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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.schemarise.alfa.compiler.AlfaSettingsException
import com.schemarise.alfa.compiler.err.CompilerSettingsException
import com.schemarise.alfa.compiler.settings._

import java.nio.file.{Files, Path, StandardOpenOption}
import scala.io.Source


object AllSettingsFactory {

  val empty: AllSettings = new AllSettings()

  def isAlfaProjectFile(file: Path) =
    AlfaPath.SettingsFileExtPattern.asPredicate().test(file.getFileName.toString)

  def fromJsonOrYamlFilePath(file: Path): AllSettings = {

    if (!isAlfaProjectFile(file))
      throw new CompilerSettingsException(
        "Project settings file is expected to have .alfa-proj.yaml or .alfa-proj.json extension. E.g." +
          AlfaPath.CompilerSettingsFile)

    val s = Source.fromInputStream(Files.newInputStream(file, StandardOpenOption.READ))
    val ext = file.getFileName.toString.takeRight(4)
    fromJsonOrYamlString(Some(file), s.mkString, ext)
  }

  def fromJsonOrYamlString(sourcepath: Option[Path], yamlStr: String, extension: String): AllSettings = {

    val mapper = if (extension.equals("yaml"))
      new ObjectMapper(new YAMLFactory()) with ClassTagExtensions
    else if (extension.equals("json"))
      new ObjectMapper() with ClassTagExtensions
    else
      throw new AlfaSettingsException("Unknown project file extension " + extension)

    mapper.registerModule(DefaultScalaModule)
    val parsedYaml = mapper.readValue[Map[String, Object]](yamlStr)

    val topLevel = List("name", "dependencies", "compile") // , "build", "export" )

    val unknown = parsedYaml.keySet.filter(k => !topLevel.contains(k))

    if (unknown.size > 0)
      throw new CompilerSettingsException(sourcepath.getOrElse("") + " Unsupported configuration(s) found '" +
        unknown.mkString("", ", ", "") + "'. Supported settings are '" + topLevel.mkString("", ", ", "") + "'")

    val buf = new scala.collection.mutable.HashMap[String, String]()
    val name = parsedYaml.get("name")
    val OutputPackage: Option[ArtifactReference] = name.flatMap(e => Some(ArtifactReference(None, e.toString)))

    val deps = parsedYaml.get("dependencies")
    val Dependencies = if (deps.isEmpty) None
    else if (deps.get == null) None
    else deps.flatMap(f => Some(f.asInstanceOf[List[String]].map(e => ArtifactReference(None, e)).toSet))

    val compileNode = parsedYaml.get("compile")
    val compile: Map[String, Object] = if (compileNode.isDefined)
      compileNode.get.asInstanceOf[Map[String, Object]]
    else Map.empty


    val exportNode = parsedYaml.get("export")
    val export: Map[String, Object] = if (exportNode.isDefined)
      exportNode.get.asInstanceOf[Map[String, Object]]
    else Map.empty

    new AllSettings(
      projectId = OutputPackage,
      dependencies = Dependencies,
      compile = CompileSettings.fromConfig(compile),
      //      build = PackageSettings.fromConfig(build),
      //      deploy = DeploySettings.fromConfig(deploy),
      exports = GeneratorSettings.fromConfig(export)
    )
  }
}
