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

package com.schemarise.alfa.generators.importers.jdbc

import java.io.File
import java.nio.file.Paths
import java.util
import com.schemarise.alfa.compiler.utils.StdoutLogger
import com.schemarise.alfa.generators.common.AlfaImporterParams
import org.scalatest.funsuite.AnyFunSuite

class JdbcImporterTest extends AnyFunSuite {

  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  val testDir = new File(targetDir, "../src/test/resources/").getCanonicalPath + "/"
  val testSources = new File(targetDir, "generated-test-sources/alfa").getCanonicalPath + "/"

  test("JDBC Schema Import tester") {

    val m = new util.HashMap[String, Object]()
    m.put("url", "jdbc:h2:mem:testdb;;INIT=RUNSCRIPT FROM 'classpath:ddl.sql'")
    m.put("user", "sa")
    m.put("password", "password")
    m.put("namespace", "warehouse.model")
    m.put("catalog", null)
    m.put("schema", null)

    val sd = new JdbcSchemaImporter(  new AlfaImporterParams( new StdoutLogger(false), Paths.get(testSources), Paths.get(testSources), m) )

    val t = sd.getDefinition("warehouse.model.Item").get
    assert(t.allFields.size == 4)
  }
}
