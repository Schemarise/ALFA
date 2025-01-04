package com.schemarise.alfa.generators.exporters.java

import org.scalatest.funsuite.AnyFunSuite

class TypeDescriptorClassPrinterTest extends AnyFunSuite {
  test("TypeDescriptor datatype creation") {
    //    val cua = TestCompiler.compileValidScript(
    //      """
    //        |record A {
    //        |   F : list< int >?
    //        |   /*
    //        |   F1 : list<string>
    //        |   F2 : int
    //        |   F3 : map< Cost : double, Total : int? >
    //        |   F4 : list< list< list< date > > >
    //        |   F41 : set< date >
    //        |   F5 : datetime?
    //        |   F6 : tuple< a : int, b : string >
    //        |   F7 : B
    //        |   F8 : enum< A, B, C >
    //        |   F10 : typeof< entity >
    //        |   F11 : tabular< B >
    //        |   F12 : stream< list< int > >
    //        |   F13 : future< set< string > >
    //        |   F14 : encrypted< list< byte > > */
    //        |}
    //        |
    //        |/*
    //        |entity B key ( id : uuid ) {
    //        |}
    //        |*/
    //        |
    //        |
    //      """)
    //
    //    val udt = cua.getUdt("A").get
    //    val f = udt.allFields.get("F").get
    //
    //    val p = new ModelClassPrinter(null)
    //
    //    println ( p.consumer(udt, f) )

    //    println( p.buildDataType(fields.get("F7").get.dataType) )
    //    println( p.buildDataType(fields.get("F8").get.dataType) )
    //    println( p.buildDataType(fields.get("F9").get.dataType) )
    //    println( p.buildDataType(fields.get("F10").get.dataType) )
    //    println( p.buildDataType(fields.get("F11").get.dataType) )
    //    println( p.buildDataType(fields.get("F12").get.dataType) )
    //    println( p.buildDataType(fields.get("F13").get.dataType) )
    //    println( p.buildDataType(fields.get("F14").get.dataType) )
  }
}


