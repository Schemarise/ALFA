package com.schemarise.alfa.runtime.utils

import com.schemarise.alfa.runtime.utils.AlfaRandomizer
import com.schemarise.alfa.runtime.{Alfa, JsonCodec}
import enumtest.Foo
import org.scalatest.funsuite.AnyFunSuite

class EnumEscapeTest extends AnyFunSuite {
  test("enumTest") {
    val ar = new AlfaRandomizer()
    val o: Foo = ar.random(enumtest.Foo.FooDescriptor.TYPE_NAME)

    //    val json = JsonCodec.toFormattedJson(o)

    val json =
      """
        |{
        |  "$type" : "enumtest.Foo",
        |  "Embedded" : "N/A",
        |  "EnumRef" : "N/A"
        |}
        |
      """.stripMargin

    //    println( json )
    val d = decode(json)
    println(d)

    val start = System.currentTimeMillis()
    for (a <- 1 to 100000) {
      val decoded1 = decode(json)
    }
    val end = System.currentTimeMillis()

    println(end - start)

    assert(end - start < 1000)
  }

  def decode(s: String) = {
    try {
      val decoded: Foo = Alfa.jsonCodec.fromJsonString(s)
      decoded
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
  }
}
