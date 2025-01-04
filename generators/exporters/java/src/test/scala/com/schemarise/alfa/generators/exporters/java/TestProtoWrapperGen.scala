//package com.schemarise.alfa.generators.exporters.java
//
//import java.io.File
//import java.nio.file.Paths
//import java.util.Collections
//
//import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
//import com.schemarise.alfa.generators.common.AlfaExporterParams
//import io.alfa.utils.testing.AlfaFunSuite
//
//class TestProtoWrapperGen extends AlfaFunSuite {
//
//  def genPbWrapperJava(script: String): Any = {
//    val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
//    //val targetDir = VFS.create().getPath("target")
//
//    val javaGen = Paths.get(targetDir + "generated-test-sources/java")
//
//    VFS.mkdir(javaGen)
//
//    val cua = TestCompiler.compileValidScript(script)
//
//    val j = new Java8ProtobufWrapperExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
//    j.exportSchema()
//  }
//
//  test("test 1" ) {
//    genPbWrapperJava(
//      """
//        |namespace features.mixed
//        |
//        |record MainRec {
//        |    OtherRec : AnotherRec
//        |    Direction : SomeEnumType
//        |    Price : double
//        |    Total : long
//        |    Version : int
//        |    EventDate : date
//        |    ListOfStrs : list< string >
//        |    ListOfRecs : list< AnotherRec >
//        |    MapOfRecs : map< date, AnotherRec >
//        |
//        |}
//        |
//        |record AnotherRec {
//        |	Name : string
//        |}
//        |
//        |
//        |enum SomeEnumType {
//        |	N S W E
//        |}
//        |
//        |
//      """)
//  }
//
//}
