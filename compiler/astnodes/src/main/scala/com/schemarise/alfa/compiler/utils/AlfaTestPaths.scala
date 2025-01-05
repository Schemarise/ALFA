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

import java.nio.file.{Path, Paths}

trait AlfaTestPaths {
  def TestRootDir: Path = Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "/../../../..").normalize()

  val logger = new StdoutLogger()

  def GeneratedTestResourcesAlfaDir: Path = {
    Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "../generated-test-resources/alfa")
  }

  def TargetTestClassesDir: Path = {
    Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "../test-classes/")
  }

  def SourceTestDir: Path = {
    Paths.get(PathUtils.ResourceDirAsUnixPath(getClass()) + "../../src/test/")
  }
}
