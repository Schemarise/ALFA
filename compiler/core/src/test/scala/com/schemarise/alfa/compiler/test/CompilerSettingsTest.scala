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
package com.schemarise.alfa.compiler.test

import com.schemarise.alfa.compiler.settings.ArtifactReference
import com.schemarise.alfa.compiler.tools.AllSettingsFactory
import org.scalatest.funsuite.AnyFunSuite

class CompilerSettingsTest extends AnyFunSuite {

  test("Artifact reference test") {
    val ar = ArtifactReference(None, "DataModels:MyProject")

    assert(ar.group.equals("DataModels"))
    assert(ar.name.equals("MyProject"))
  }

  test("Valid Compiler Settings as JSON") {
    val json =
      """
        |---
        |name: data.models:customers
        |dependencies:
        |- MyNamespace:MyProject
        |compile:
        |  DisallowInclude: true
        |  DisallowUnionDuplicateTypeField: false
      """.stripMargin

    val settings = AllSettingsFactory.fromJsonOrYamlString(None, json, "yaml")

    assert(settings.projectId.get.name.equals("customers"))
    assert(settings.projectId.get.group.equals("data.models"))
    assert(settings.dependencies.get.head.name.equals("MyProject"))
    assert(settings.compile.DisallowInclude.equals(true))
  }
}
