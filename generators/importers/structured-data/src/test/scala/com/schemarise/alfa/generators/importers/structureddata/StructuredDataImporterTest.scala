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

import com.schemarise.alfa.compiler.utils.{StdoutLogger, VFS}
import com.schemarise.alfa.generators.common.AlfaImporterParams
import com.schemarise.alfa.utils.testing.AlfaFunSuite

import java.io.File
import java.nio.file.Paths
import java.util

class StructuredDataImporterTest extends AlfaFunSuite {
  private val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  private val testDir = new File(targetDir, "../src/test/resources/").getCanonicalPath + "/"
  private val genAlfa = new File(targetDir, "generated-test-sources/alfa").getCanonicalPath + "/"


  test("Trade Srv importers") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "alfabank.dataproducts.trade")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(),
      Paths.get(testDir + "json/Swap.json"),
      Paths.get(genAlfa, "trade-srv"),
      m) )

    val defs = sd.getDefinitions()
    println(defs)
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

  test("Structured Data Import from Array") {
    val m = new util.HashMap[String, Object]()
    m.put(StructureImportSettings.namespace, "demo")
    m.put(StructureImportSettings.typenameField, "@type")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(),
      Paths.get(testDir + "json/simple-array.json"),
      Paths.get(genAlfa, "simple-array"),
      m) )

    val types = sd.getDefinitions()

    val r2 = sd.getDefinition("demo.Rec1").get
    assert(r2.allFields.size == 4)
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

    val p = VFS.create().getPath("/")
    val csvFile = p.resolve("data.csv")
    VFS.write(csvFile,
      """Name,Age,AddressLine1,AddressLine2,Salary,Dob,Id,Enabled
        |Rob,27,Street 1,,391823.12,1990-01-01,e58ed763-928c-4155-bee9-fdbaaadc15f3,True
        |Paul,28,Street 1,London,239083,1990-06-01,8c8d17c8-e78c-4050-86b0-7456c1f68753,false
        |Liam,30,Street 1,,908901.54,1990-05-01,4c6661ce-cdae-4e68-aaa2-60f5670c406a,true
        |John,30,Street 1,,239089.32,1990-03-01,2b18bb29-27a9-4b2b-8f4c-fbd27930046c,False
        |""".stripMargin)

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    sd.getDefinitions()

    val generated = VFS.read(p.resolve("data.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record data {
        |  Name : string
        |  Age : int
        |  AddressLine1 : string
        |  AddressLine2 : string ?
        |  Salary : double
        |  Dob : date
        |  Id : uuid
        |  Enabled : boolean
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV Schema Test 2") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")

    val p = VFS.create().getPath("/")
    val csvFile = p.resolve("data.csv")

    VFS.write(csvFile,
      """NumberA,NumberB,NumberC,AlmostDate
        |10,20,10.5,1990-01-01
        |1000000000000,3,1000000000000,1990-10-01
        |10.10,1000000000000,20.1,ABC
        |""".stripMargin)

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    sd.getDefinitions()

    val generated = VFS.read(p.resolve("data.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record data {
        |  NumberA : double
        |  NumberB : long
        |  NumberC : double
        |  AlmostDate : string
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV Schema Test 6") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")
    m.put("dateFormat", "dd/MM/yyyy")

    val p = VFS.create().getPath("/")
    val csvFile = p.resolve("all-data.csv")

    VFS.write(csvFile,
      """Name,Dob
        |Bob,20/10/2001
        |""".stripMargin)

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    sd.getDefinitions()

    val generated = VFS.read(p.resolve("all-data.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record alldata {
        |  Name : string
        |  Dob : date
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV Schema Test temporal formats") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")
    m.put("dateFormat", "dd.MM.yyyy")
    m.put("timeFormat", "HH:mm")
    m.put("datetimeFormat", "HH:mm:ss.SSSXXX")

    val p = VFS.create().getPath("/")
    val csvFile = p.resolve("data-with-dates.csv")

    VFS.write(csvFile,
      """DateA,DatetimeB
        |02.01.1990,11:30:00.000-05:00
        |""".stripMargin)

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    sd.getDefinitions()

    val generated = VFS.read(p.resolve("data-with-dates.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record datawithdates {
        |  DateA : date
        |  DatetimeB : datetime
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV Schema Test Infer Enum") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")
    m.put("treatAsEnumField", "Availability")

    val csvFile = Paths.get(testDir, "csv", "products-1000.csv")

    val p = VFS.create().getPath("/")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    val defs = sd.getDefinitions()

    val generated = VFS.read(p.resolve("products-1000.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record products1000 {
        |  Index : int
        |  Name : string
        |  Description : string
        |  Brand : string
        |  Category : string
        |  Price : int
        |  Currency : string
        |  Stock : int
        |  EAN : long
        |  Color : string
        |  Availability : enum< backorder, discontinued, pre_order, limited_stock, in_stock, out_of_stock >
        |  `Internal ID` : int
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV Schema Test Infer GLEIF") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")
    m.put("typename", "GLEIFRecord")

    val csvFile = Paths.get(testDir + "csv", "gleif10k.csv")

    val p = VFS.create().getPath("/")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    val defs = sd.getDefinitions()

    val generated = VFS.read(p.resolve("gleif10k.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record GLEIFRecord {
        |  `Relationship.StartNode.NodeID` : string
        |  `Relationship.StartNode.NodeIDType` : enum< LEI >
        |  `Relationship.EndNode.NodeID` : string
        |  `Relationship.EndNode.NodeIDType` : enum< LEI >
        |  `Relationship.RelationshipType` : enum< IS_FEEDER_TO, IS_ULTIMATELY_CONSOLIDATED_BY, IS_DIRECTLY_CONSOLIDATED_BY, IS_INTERNATIONAL_BRANCH_OF, IS_SUBFUND_OF, `IS_FUND-MANAGED_BY` >
        |  `Relationship.RelationshipStatus` : enum< ACTIVE, INACTIVE >
        |  `Relationship.Period.1.startDate` : datetime ?
        |  `Relationship.Period.1.endDate` : datetime ?
        |  `Relationship.Period.1.periodType` : enum< RELATIONSHIP_PERIOD, DOCUMENT_FILING_PERIOD, ACCOUNTING_PERIOD > ?
        |  `Relationship.Period.2.startDate` : datetime ?
        |  `Relationship.Period.2.endDate` : datetime ?
        |  `Relationship.Period.2.periodType` : enum< RELATIONSHIP_PERIOD, DOCUMENT_FILING_PERIOD, ACCOUNTING_PERIOD > ?
        |  `Relationship.Period.3.startDate` : datetime ?
        |  `Relationship.Period.3.endDate` : datetime ?
        |  `Relationship.Period.3.periodType` : enum< RELATIONSHIP_PERIOD, DOCUMENT_FILING_PERIOD, ACCOUNTING_PERIOD > ?
        |  `Relationship.Period.4.startDate` : string ?
        |  `Relationship.Period.4.endDate` : string ?
        |  `Relationship.Period.4.periodType` : string ?
        |  `Relationship.Period.5.startDate` : string ?
        |  `Relationship.Period.5.endDate` : string ?
        |  `Relationship.Period.5.periodType` : string ?
        |  `Relationship.Qualifiers.1.QualifierDimension` : enum< ACCOUNTING_STANDARD > ?
        |  `Relationship.Qualifiers.1.QualifierCategory` : enum< IFRS, US_GAAP, OTHER_ACCOUNTING_STANDARD > ?
        |  `Relationship.Qualifiers.2.QualifierDimension` : string ?
        |  `Relationship.Qualifiers.2.QualifierCategory` : string ?
        |  `Relationship.Qualifiers.3.QualifierDimension` : string ?
        |  `Relationship.Qualifiers.3.QualifierCategory` : string ?
        |  `Relationship.Qualifiers.4.QualifierDimension` : string ?
        |  `Relationship.Qualifiers.4.QualifierCategory` : string ?
        |  `Relationship.Qualifiers.5.QualifierDimension` : string ?
        |  `Relationship.Qualifiers.5.QualifierCategory` : string ?
        |  `Relationship.Quantifiers.1.MeasurementMethod` : enum< ACCOUNTING_CONSOLIDATION > ?
        |  `Relationship.Quantifiers.1.QuantifierAmount` : double ?
        |  `Relationship.Quantifiers.1.QuantifierUnits` : enum< PERCENTAGE > ?
        |  `Relationship.Quantifiers.2.MeasurementMethod` : string ?
        |  `Relationship.Quantifiers.2.QuantifierAmount` : string ?
        |  `Relationship.Quantifiers.2.QuantifierUnits` : string ?
        |  `Relationship.Quantifiers.3.MeasurementMethod` : string ?
        |  `Relationship.Quantifiers.3.QuantifierAmount` : string ?
        |  `Relationship.Quantifiers.3.QuantifierUnits` : string ?
        |  `Relationship.Quantifiers.4.MeasurementMethod` : string ?
        |  `Relationship.Quantifiers.4.QuantifierAmount` : string ?
        |  `Relationship.Quantifiers.4.QuantifierUnits` : string ?
        |  `Relationship.Quantifiers.5.MeasurementMethod` : string ?
        |  `Relationship.Quantifiers.5.QuantifierAmount` : string ?
        |  `Relationship.Quantifiers.5.QuantifierUnits` : string ?
        |  `Registration.InitialRegistrationDate` : datetime
        |  `Registration.LastUpdateDate` : datetime
        |  `Registration.RegistrationStatus` : enum< PUBLISHED, LAPSED >
        |  `Registration.NextRenewalDate` : datetime
        |  `Registration.ManagingLOU` : string
        |  `Registration.ValidationSources` : enum< ENTITY_SUPPLIED_ONLY, PARTIALLY_CORROBORATED, FULLY_CORROBORATED >
        |  `Registration.ValidationDocuments` : enum< REGULATORY_FILING, SUPPORTING_DOCUMENTS, ACCOUNTS_FILING, OTHER_OFFICIAL_DOCUMENTS, CONTRACTS >
        |  `Registration.ValidationReference` : string ?
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }

  test("CSV file with BOM UTF8") {
    testDifferenceCharset("fileWithBOM")
  }

  test("CSV file with UTF8") {
    testDifferenceCharset("utf8file")
  }

  test("CSV file with cp1252") {
    testDifferenceCharset("cp1252file")
  }

  test("CSV file with newlineOnly") {
    testDifferenceCharset("newlineOnly")
  }


  test("CSV file with embeddedJson") {
    testDifferenceCharset("embeddedJson")
  }


  private def testDifferenceCharset(f : String): Unit = {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")
    m.put("csvMaxCharsPerColumn", "5000")

    val csvFile = Paths.get(testDir, "csv", f + ".csv")

    val p = VFS.create().getPath("/")

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m))
    sd.getDefinitions()

    val generated = VFS.read(p.resolve(f + ".alfa"))

    val expected =
      s"""namespace imported.csvmodel
        |
        |record $f {
        |  Name : string
        |  Age : int
        |  Country : string
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }


  test("CSV Schema Test Optionals") {
    val m = new util.HashMap[String, Object]()
    m.put("namespace", "imported.csvmodel")

    val p = VFS.create().getPath("/")
    val csvFile = p.resolve("data-opt.csv")
    VFS.write(csvFile,
      """ValInt,ValStr,ValDbl
        |10,,
        |,ABC,190
        |593,,
        |,DEF,901.54
        |""".stripMargin)

    val sd = new StructuredDataSchemaImporter(new AlfaImporterParams(new StdoutLogger(), csvFile, p, m) )
    sd.getDefinitions()

    val generated = VFS.read(p.resolve("data-opt.alfa"))

    val expected =
      """namespace imported.csvmodel
        |
        |record dataopt {
        |  ValInt : int?
        |  ValStr : string?
        |  ValDbl : double?
        |}
        |""".stripMargin

    assertEqualsIgnoringWhitespace(expected, generated)
  }
}
