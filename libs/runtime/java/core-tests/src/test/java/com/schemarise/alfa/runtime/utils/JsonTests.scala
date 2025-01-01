package com.schemarise.alfa.runtime.utils

import RefData.LEI._
import com.schemarise.alfa.runtime.{Alfa, AlfaRuntimeException}
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig
import com.schemarise.alfa.runtime_int.RingBufferJsonParserWrapper
import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import com.schemarise.alfa.runtime.utils.AlfaRandomizer
import com.schemarise.alfa.utils.testing.AlfaFunSuite

import java.io.{BufferedInputStream, File, FileInputStream}

class JsonTests extends AlfaFunSuite {

  test("JSON RingBuffer Test") {
    val j =
      """
        |{
        | "name" : "Bob",
        | "age" : 21,
        | "salary" : 30231.21,
        | "list" : [1,2,3,4],
        | "map" : { "x":1, "y":32, "z":239 },
        | "homeowner" : true
        |}
        |""".stripMargin


    //    var fis = new FileInputStream( new File("/Users/sadia/Downloads/20240922-1600-gleif-goldencopy-lei2-golden-copy.json") );
    //    val jp2 = new JsonFactory().createParser(new BufferedInputStream(fis))
    //    val rb = new RingBufferJsonParserWrapper(jp2);
    //
    //    var counter = 0
    //    while ( rb.nextToken() != null ) {
    //      var t = rb.currentToken()
    //      counter = counter + 1
    //
    //      if ( counter % 1000 == 0 )
    //        println(".")
    //    }
    //
    //    var s = System.currentTimeMillis()
    //
    //    var line = 0
    //    while ( jp2.nextToken() != null ) {
    //      var t = new RingBufferJsonParserWrapper.TokenWrapper(jp2)
    //      line = t.getLocation.getLineNr
    //    }
    //
    //    var e = System.currentTimeMillis()
    //
    //    System.out.println( (e - s)/1000 + "s")
  }

  test("JSON encode decode A") {
    val r = new AlfaRandomizer()

    val a: Header = r.random(Header.HeaderDescriptor.TYPE_NAME)

    val cc = JsonCodecConfig.builder.setWriteChecksum(true).setIgnoreDateFormat(true).build
    val json = Alfa.jsonCodec.toFormattedJson(cc, a)
    System.out.println(json)

    val obj: Header = Alfa.jsonCodec.fromJsonString(cc, json)

    val mock =
      """
        |{
        |  "$type" : "RefData.LEI.Header",
        |  "$csum" : "f4548csa:",
        |  "ContentDate" : "1548-06-11T11:51:58",
        |  "Originator" : "jtfha",
        |  "FileContent" : "ywbjk",
        |  "RecordCount" : "48963906631338742"
        |}
      """.stripMargin

    assertThrows[AlfaRuntimeException](
      // checksum mismatches
      Alfa.jsonCodec.fromJsonString(cc, mock)
    )
  }

  test("JSON encode decode B") {
    val r = new AlfaRandomizer()

    val a: Record = r.random(Record.RecordDescriptor.TYPE_NAME)

    val cc = JsonCodecConfig.builder.setWriteChecksum(true).setIgnoreDateFormat(true).build
    val json = Alfa.jsonCodec.toFormattedJson(cc, a)
    System.out.println(json)

    val obj: Record = Alfa.jsonCodec.fromJsonString(cc, json)

    assert(obj.equals(a))
  }


  test("JSON encode decode C") {
    var a = OtherEntityName.builder().setType("AAA").setValue("BBB").build();
    val cc = JsonCodecConfig.builder.setWriteChecksum(true).setIgnoreDateFormat(true).build
    val json = Alfa.jsonCodec.toFormattedJson(cc, a)

    assert(a.descriptor().getChecksum() == "3ea048cd:a1823582")

    println(json)

    val modAllCheckSum =
      """
        |{
        |  "$type" : "RefData.LEI.OtherEntityName",
        |  "$csum" : "3ea048cxx:a1823582",
        |  "type" : "AAA",
        |  "Value" : "BBB"
        |}
      """.stripMargin
    val obj: OtherEntityName = Alfa.jsonCodec.fromJsonString(cc, modAllCheckSum)


    val modAllAndMandCheckSum =
      """
        |{
        |  "$type" : "RefData.LEI.OtherEntityName",
        |  "$csum" : "3ea048cxx:a18235xx",
        |  "type" : "AAA",
        |  "Value" : "BBB"
        |}
      """.stripMargin
    assertThrows[AlfaRuntimeException](
      // Incompatible checksums in JSON 3ea048cxx:a18235xx and locally 3ea048cd:a1823582
      Alfa.jsonCodec.fromJsonString(cc, modAllAndMandCheckSum)
    )
  }
}
