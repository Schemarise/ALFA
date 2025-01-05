package com.schemarise.alfa.generators.exporters.java

import java.io.File
import java.nio.file.Paths
import java.util.Collections

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
import com.schemarise.alfa.generators.common.AlfaExporterParams
import org.scalatest.funsuite.AnyFunSuite

class TestOneSmt2Java extends AnyFunSuite {
  //  test( "Compile external script" ) {
  //    val p = Paths.get("/Users/sadia/IdeaProjects/positions/source/alfa")
  //    val cua : ICompilationUnitArtifact = TestCompiler.compileScriptOnly(p)
  //    if ( cua.hasErrors )
  //      cua.getErrors.foreach( e => println(e))
  //    else
  //      genJava(cua)
  //  }

  //  test("Quick test ") {
  //    val p = Paths.get("/Users/sadia/IdeaProjects/smt/libs/runtime/ast/src/test/alfa/Mock.alfa")
  //    val cua : ICompilationUnitArtifact = TestCompiler.compileScriptOnly(p)
  //    val s = cua.getUdt("Mock.EitherStringOrSampleNested")
  //    val ss = s.get.allFields("val")
  //    genJava(cua)
  //  }

  private def genJava(cua: ICompilationUnitArtifact) = {
    val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
    val javaGen = Paths.get(targetDir + "generated-test-sources/java/")
    VFS.mkdir(javaGen)

    val j = new JavaExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
    j.exportSchema()
  }
}
