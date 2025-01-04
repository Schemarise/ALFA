package com.schemarise.alfa.runtime.utils

import com.schemarise.alfa.runtime.JsonCodec
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig
import enumtest.Foo
import hierachytest.Address
import org.scalatest.funsuite.AnyFunSuite

class HierarchyTest extends AnyFunSuite {
  test("HierarchyTest 1") {
    //    val ar = new AlfaRandomizer()
    //    val o : Address = ar.random(  hierachytest.Address.TYPE_NAME)

    val json =
      """
        |{
        |  "Address" : "1 A Street",
        |  "Country" : "UK",
        |  "PostCode" : "W1"
        |}
        |
      """.stripMargin

    // not persuing this for now .. not a usecase that needed to be fixed with json schema etc

    //    val jc = JsonCodecConfig.builder().setAssignableToClass(classOf[Address]).build()
    //    val decoded: Foo = JsonCodec.fromJsonString(jc, json)
    //
    //    println(decoded)
  }
}
