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
package com.schemarise.alfa.compiler.test.lang.feature

import com.schemarise.alfa.compiler.utils.{AlfaTestPaths, VFS}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class MultiProjectTest extends AnyFunSuite with AlfaTestPaths {

  private val compiler = new com.schemarise.alfa.compiler.AlfaCompiler()

  test("inter-dependent projects") {

    val path = "/compiler/core/src/test/alfa/multi-project/projects-with-deps/payments"

    var fs = VFS.createAndCopyFrom(Paths.get(TestRootDir.toString, path))

    val cua = compiler.compile(fs.getPath("/"))

    if (cua.hasErrors) {
      cua.getErrors.foreach(println(_))
    }
  }
}
