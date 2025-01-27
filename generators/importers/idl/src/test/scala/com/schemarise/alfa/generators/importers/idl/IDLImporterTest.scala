package com.schemarise.alfa.generators.importers.idl

import com.schemarise.alfa.compiler.utils.{ILogger, TestCompiler, VFS}
import com.schemarise.alfa.generators.common.AlfaImporterParams
import com.schemarise.alfa.utils.testing.AlfaFunSuite

import java.nio.file.{Path, Paths}

class IDLImporterTest extends AlfaFunSuite {
  test("idl basic test") {
    val inputDir = Paths.get(testResourcesPath("/idl/example.idl"))

    val outputDir = Paths.get(targetGeneratedTestSources("alfa"))

    val params = new AlfaImporterParams( ILogger.stdoutLogger, inputDir, outputDir, java.util.Collections.emptyMap() )

    val i = new IDLImporter(params)
    i.importSchema()
  }

  test("forward decl with docs") {
    val s = s"""
       |
       |/* This
       |   is
       |   a long comment
       | */
       |module MM {
       |//foo bar1
       |struct A;
       |
       |//foo bar2
       |struct Data {
       |  // field A
       |  A data;
       |};
       |
       |//foo bar3
       |struct A {
       |   // field a
       |   int a;
       |};
       |
       |// Some interface
       |interface BasicTester
       |{
       |    // gest
       |    void ping();
       |
       |};
       |
       |/* My enum
       |   doc */
       |enum E {
       |  // const a
       |  A,
       |  // const b
       |  B,
       |  C
       |};
       |
       |};
       |""".stripMargin

    val alfa = validateIdl(s)

    assertEqualsIgnoringWhitespace(alfa,
      """
        |/# This
        |   is
        |   a long comment
        |  #/
        |namespace MM
        |
        |
        |# foo bar2
        |record Data {
        |    # field A
        |    data : MM.A
        |}
        |
        |# foo bar3
        |record A {
        |    # field a
        |    a : int
        |}
        |
        |@alfa.lang.IgnoreServiceWarnings
        |# Some interface
        |service BasicTester
        |{
        |    # gest 
        |    ping(  ) : void
        |}
        |
        |/# My enum
        |   doc  #/
        |enum E {
        |   # const a 
        |   A,
        |   # const b 
        |   B,
        |   C
        |}
        |""".stripMargin)
  }

  test("forward decl in module") {
    val s = s"""
               |module MM {
               |  struct A;
               |
               |  struct Data {
               |    A data;
               |  };
               |
               |  struct A {
               |     int a;
               |  };
               |};
               |""".stripMargin

    val alfa = validateIdl(s)
  }



  test( "test other namespace typedefs" ) {
    val s =
      """
        |module MA
        |{
        |    typedef struct SA
        |    {
        |    } mySA;
        |
        |    interface IA
        |    {
        |        SA MethodMAIA(out mySA objSA);
        |    };
        |};
        |
        |""".stripMargin

    val alfa = validateIdl(s)

    assertEqualsIgnoringWhitespace(
      s"""
         |typedefs {
         |  mySA = MA.SA
         |}
         |namespace MA
         |
         |record SA {
         |
         |}
         |
         |@alfa.lang.IgnoreServiceWarnings
         |service IA
         |{
         |  MethodMAIA( out objSA : mySA ) : MA.SA
         |}
         |""".stripMargin, alfa)
  }

  private def validateIdl(s: String) = {
    val fs = VFS.create()
    val input = fs.getPath("sample.idl")
    VFS.write(input, s)

    val outputDir = Paths.get(targetGeneratedTestSources("alfa"))

    val params = new AlfaImporterParams(ILogger.stdoutLogger, input, outputDir, java.util.Collections.emptyMap())

    val i = new IDLImporter(params)
    i.importSchema()
    VFS.read(outputDir.resolve("sample.alfa"))
  }

  test( "test nested typedefs" ) {
    val s =
      """
        |    typedef struct HazelNut114{
        |      struct CrabApple114{
        |        struct Lentil114{
        |          sequence<sequence<sequence<sequence<char,18> > ,18> >  GoldenDelicous114[13];
        |        } Fig114;
        |      } RedOnion114;
        |    } Cinnamon114;
        |""".stripMargin

    val alfa = validateIdl(s)
  }

  test("test suite") {
    val tp = testResourcesPath("/idl/testsuite/")
    val idlFiles = VFS.listFileForExtension(Paths.get(tp), ".idl")

    idlFiles.foreach( inputFile => {
      println("Processing " + inputFile + " ... ")
      val fs = VFS.create()
//      val outputDir = Paths.get(targetGeneratedTestSources("alfa"))
       val outputDir = fs.getPath("/alfa")

      val params = new AlfaImporterParams( ILogger.stdoutLogger, inputFile, outputDir, java.util.Collections.emptyMap() )

      val i = new IDLImporter(params)
      i.importSchema()

      val cua = TestCompiler.compileScriptOnly(outputDir)
      if ( cua.hasErrors ) {
        ILogger.stdoutLogger.formatAndLogMessages(outputDir, cua.getErrors, false )
        VFS.printFileSystemContents(outputDir)
        throw new RuntimeException()
      }

    })

  }
}
