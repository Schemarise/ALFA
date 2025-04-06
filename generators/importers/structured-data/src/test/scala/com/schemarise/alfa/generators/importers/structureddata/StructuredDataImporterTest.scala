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

package com.schemarise.alfa.generators.importers.structureddata

import com.schemarise.alfa.compiler.utils.StdoutLogger
import com.schemarise.alfa.generators.common.AlfaImporterParams
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.Paths
import java.util

class StructuredDataImporterTest extends AnyFunSuite {
  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  val testDir = new File(targetDir, "../src/test/resources/").getCanonicalPath + "/"
  val genAlfa = new File(targetDir, "generated-test-sources/alfa").getCanonicalPath + "/"


  test("Trade Srv importers") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "alfabank.dataproducts.trade")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(),
      Paths.get(testDir + "json/Swap.json"),
      Paths.get(genAlfa, "trade-srv"),
      m) )

    sd.getDefinitions()
  }

  test("Structured Data Import tester") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "demo")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(),
      Paths.get(testDir + "json/simple.json"),
      Paths.get(genAlfa, "simple"),
      m) )

    val types = sd.getDefinitions()

    val r2 = sd.getDefinition("demo.Rec3").get
    assert(r2.allFields.size == 5)

    val fields = r2.allFields.keySet.toList

    assertResult(fields)(List("Age", "Name", "Salary", "LoyaltyPoints", "Accounts"))

    types.foreach(t => {
      val d = sd.getDefinition(t)
      println(d.get)
    })
  }

  test("YAML Structured Data Import tester") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "demo")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams( new StdoutLogger(),
      Paths.get(testDir + "yaml/mysql.yaml"), Paths.get(genAlfa, "yaml"), m) )

    val types = sd.getDefinitions()
    types.foreach(t => {
      val d = sd.getDefinition(t)
      //      println( d.get )
    })
  }

  test("CSV Schema Test 1") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(),
      Paths.get(testDir + "csv/test1.csv"),
      Paths.get(genAlfa, "csv-test1"),
      m) )

    sd.getDefinitions()
  }
}
