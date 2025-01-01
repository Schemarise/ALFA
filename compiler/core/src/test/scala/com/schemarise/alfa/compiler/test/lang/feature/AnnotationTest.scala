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

import com.schemarise.alfa.compiler.test.lang.AlfaCoreFunSuite
import com.schemarise.alfa.compiler.utils.TestCompiler

class AnnotationTest extends AlfaCoreFunSuite {

  test("Deprecated annotations") {
    val s =
      """
        |import alfa.lang.Deprecated
        |@Deprecated
        |trait Example.Plain {
        |}
      """.stripMargin

    val cua = TestCompiler.compileValidScript(s)

    assertEqualsIgnoringWhitespace(cua.toString, s)
  }


  test("FieldAnnotations simples") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |annotation AnnX( field ) {}
        |annotation AnnY( field ) {}
        |annotation AnnZ( field ) {}
        |
        |@alfa.meta.FieldAnnotations(
        |  {
        |     [ @AnnX(), @AnnY() ] : [ A, B ]
        |  }
        |)
        |trait Plain {
        |  A : int
        |  B : int
        |  C : int
        |  D : int
        |  E : int
        |  F : int
        |
        |}
      """)
  }

  test("toString tests") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |namespace alfabank.models.compliance
        |
        |annotation PII (entity, record, trait, field ) {
        |    Sensitivity : DataSensitivity = DataSensitivity.Medium
        |}
        |
        |enum DataSensitivity {
        |    High Medium Low
        |}
        |
      """)

    val d1 = cua.getUdt("alfabank.models.compliance.PII").get.toString
    println(d1)

    val d2 = cua.getUdt("alfabank.models.compliance.DataSensitivity").get.toString
    println(d2)

  }

  test("Annotation simple") {
    TestCompiler.compileValidScript("")

    val cua = TestCompiler.compileValidScript(
      """
        |# Metadata for data lineage. This can be a field in the model or maintained
        |# separately as tagged data against a target record
        |annotation Marker ( trait, union ) {
        |    F : int?
        |}
        |
        |@Marker
        |trait Plain {
        |}
      """)

    val d = cua.getUdt("Marker").get.toString
    println(d)
  }

  test("Annotation error test") {
    val cua = TestCompiler.compileInvalidScript(
      "@2:20 Unknown annotation target 'foo'. Supported targets are annotation, dataproduct, entity, enum, field, includes, key, keyfield, library, method, namespace, record, service, trait, tuple, type, union.",
      """
        |annotation Marker ( foo) {
        |}
        |
      """)
  }

  test("Annotation in namespace") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace CMS
        |
        |annotation Marker ( trait, union ) {
        |}
        |
        |@Marker
        |trait Plain { }
        |
        |namespace Accounts
        |
        |@CMS.Marker
        |trait BaseAccount { }
        |
      """)
  }

  test("Annotation wrong target") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:0 Annotation @Marker cannot be set on a record. It can be set on trait, union",
      """
        |annotation Marker ( trait, union ) {
        |}
        |
        |@Marker
        |record Plain {
        |}
      """)
  }


  test("Annotation unknown") {
    val cua = TestCompiler.compileInvalidScript(
      "@5:1 Unknown annotation 'Merker'",
      """
        |annotation Marker ( record ) {
        |}
        |
        |@Merker
        |record Plain {
        |}
      """)
  }

  test("Test annotation fields ") {
    TestCompiler.compileValidScript(
      """
        |# Sample model defined in ALFA
        |namespace acme.model
        |
        |# Type of customer based on usage
        |enum CustomerType {
        |    Standard ## Less than $1000 spend annually
        |    Gold ## Between $1000 to $10,000 spend annually
        |    Platinum ## Over $10,000 annual spend
        |}
        |
        |# Base Person type with generic fields
        |@PII( Classification = DataClassification.Confidential,
        |      Sensitivity = DataSensitivity.High )
        |trait Person {
        |    FirstName : string ## Legal first and middle name
        |    LastName : string ## Legal last name
        |    DateOfBirth : date ## Date of birth
        |    // Address : list<string> ## Residential address
        |
        |    # Ensure derrived type instance is an adult
        |    assert IsAdult {
        |        let age = dateDiff( today(), DateOfBirth )/365
        |        if (age<18) raise error("Not an adult")
        |    }
        |}
        |
        |annotation PII (entity, record, trait, field ) {
        |    Sensitivity : DataSensitivity = DataSensitivity.Low
        |    Classification : DataClassification = DataClassification.InternalOnly
        |}
        |
        |enum DataSensitivity {
        |    High Medium Low
        |}
        |
        |enum DataClassification {
        |    Public InternalOnly Confidential Restricted
        |}
        |
        |# Library for commission calculations
        |library CommissionLib {
        |    # Calculate commission based on given salary
        |    calcComm(Salary : double) : double {
        |        return if (Salary<50000) 0.03 else 0.05
        |    }
        |}
        |
        |
      """.stripMargin)
  }


  test("Annotated inheritance over traits") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Test
        |
        |annotation Foo (trait) {
        |}
        |
        |@Foo
        |trait B { }
        |
        |record A includes B {
        |}
        |
    """.stripMargin)

    val d = cua.getUdt("Test.A").get

    assert(d.localAndInheritedAnnotations.size == 1)
  }

  //  test( "Annotated inheritance over traits error" ) {
  //    val cua = TestCompiler.compileInvalidScript(
  //      "@13:0 Annotation Test.Foo already assigned against an ancestor trait",
  //      """
  //        |namespace Test
  //        |
  //        |annotation Foo (trait, record) {
  //        |}
  //        |
  //        |@Foo
  //        |trait X {
  //        |}
  //        |
  //        |trait B includes X { }
  //        |
  //        |@Foo
  //        |record A includes B {
  //        |}
  //        |
  //    """.stripMargin)
  //  }

  test("Namespace inheritance over traits error") {
    val cua = TestCompiler.compileValidScript(
      """
        |@Test.Foo
        |@Test.Bar
        |namespace Test
        |
        |annotation Foo (namespace, trait, record) {
        |}
        |
        |annotation Bar (namespace) {
        |}
        |
        |trait X {
        |}
        |
        |record A {
        |}
        |
    """.stripMargin)

    val d = cua.getUdt("Test.A").get

    assert(d.localAndInheritedAnnotations.size == 1)

    println(d)
  }


}

