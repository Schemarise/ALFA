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

package com.schemarise.alfa.generators.importers.java


import com.schemarise.alfa.compiler.utils._
import com.schemarise.alfa.generators.common.AlfaImporterParams
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.Paths
import java.util

class JavaClassImporterTest extends AnyFunSuite with AlfaTestPaths {
//   com.opengamma.strata.product.Product
  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  val testDir = new File(targetDir, "../src/test/resources/").getCanonicalPath + "/"
  val testSources = new File(targetDir, "generated-test-sources/alfa").getCanonicalPath + "/"


  test("import opengamma") {
    val m = new util.HashMap[String, Object]()

    m.put(JavaClassImporter.ImportPackageFilter, "com.opengamma.strata")
    m.put(JavaClassImporter.ImportClassBaseType, "org.joda.beans.ImmutableBean")

    val sd = new JavaClassImporter(  new
        AlfaImporterParams( new StdoutLogger(false), Paths.get(testSources), Paths.get(testSources), m) )

    sd.importSchema()
  }



}
