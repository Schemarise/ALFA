package com.schemarise.alfa.runtime.utils

import com.schemarise.alfa.runtime.{Alfa, JsonCodec}
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig
import demoskipunknown.SkipUnknownField
import org.scalatest.funsuite.AnyFunSuite

class SkipUnwantedFieldsTest extends AnyFunSuite {
  test("skipFieldsTest") {

    val json =
      """
        |{
        |  "payload" : {
        |      "n" : "test",
        |      "age" : 23,
        |      "ignorable1" : "sdf"
        |  },
        |  "ignorable2" : {
        |      "a" : "23",
        |      "b" : 23
        |  }
        |}
        |
      """.stripMargin

    //    println( json )
    val d = decode(json)
    println(Alfa.jsonCodec.toFormattedJson(d))
  }

  def decode(s: String) = {
    val jc = JsonCodecConfig.builder().setAssignableToClass(classOf[SkipUnknownField]).build()
    val decoded: SkipUnknownField = Alfa.jsonCodec.fromJsonString(jc, s)

    println(Alfa.jsonCodec.toFormattedJson(jc.getAssertListener.getValidationReport.build()))

    decoded

  }
}
