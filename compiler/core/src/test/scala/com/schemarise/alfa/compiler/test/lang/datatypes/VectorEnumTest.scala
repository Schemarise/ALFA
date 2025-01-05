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
package com.schemarise.alfa.compiler.test.lang.datatypes

import com.schemarise.alfa.compiler.ast.nodes.Record
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{EnumDataType, TupleDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class VectorEnumTest extends AnyFunSuite {

  test("Enum test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |enum T {
        |   R G B
        |}
        |
        |record Data {
        |   PrimaryColour : enum< Red, Green, Blue >
        |}
      """)
  }

  test("Enum-in-place") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record Data {
        |   PrimaryColour : enum< Red, Green, Blue >
        |   Windspeed : tuple< Speed : int, Direction : enum< North, South, East, West > >
        |}
              """)

    val rec = cua.getUdt("com.acme.Data").get.asInstanceOf[Record]

    val synthEnum = rec.allFields.get("PrimaryColour").get.dataType.asInstanceOf[EnumDataType].syntheticEnum()

    assert(synthEnum.versionedName.fullyQualifiedName == "com.acme.Data__PrimaryColour")
    assert(synthEnum.allFields.size == 3)
    assert(synthEnum.allFields.contains("Red"))
    assert(synthEnum.allFields.contains("Green"))
    assert(synthEnum.allFields.contains("Blue"))


    val synthRec = rec.allFields.get("Windspeed").get.dataType.asInstanceOf[TupleDataType].syntheticRecord
    val directionType = synthRec.allFields.get("Direction").get.dataType.asInstanceOf[EnumDataType].syntheticEnum()
    assert(directionType.allFields.size == 4)
    assert(directionType.allFields.contains("North"))
    assert(directionType.allFields.contains("South"))
    assert(directionType.allFields.contains("East"))
    assert(directionType.allFields.contains("West"))
  }


  test("Enum-in-place reference error") {
    TestCompiler.compileInvalidScript("@9:10 Cannot reference synthetic declaration com.acme.Room__PrimaryColour",
      """
        |namespace com.acme
        |
        |record Room {
        | PrimaryColour : enum< Red, Green, Blue >
        |}
        |
        |record Wall {
        | Colour : Room__PrimaryColour
        |}
        |
      """)
  }

}

