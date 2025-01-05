package com.schemarise.alfa.compiler.test

import java.io.File
import java.nio.file.Paths
import java.util.Collections

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.utils.{ResolvedAssertor, StdoutLogger}
import com.schemarise.alfa.generators.exporters.java.JavaExporter
import org.scalatest.funsuite.AnyFunSuite

class CompilerFileTest extends AnyFunSuite {
  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  val javaGen = Paths.get(targetDir + "generated-test-sources/java")

  val compiler = new AlfaCompiler()
  val logger = new StdoutLogger

  test("compile a file") {
    //    val cua = compiler.compile(Paths.get("/Users/sadia/IdeaProjects/alfa-core/demo/alfa-air/model"))
    ////    val cua = compiler.compile(Paths.get("/Users/sadia/IdeaProjects/smt/generators/exporters/testing/test-material/src/alfa/primary/udts/udts.Union.alfa"))
    //
    //    cua.traverse( new ResolvedAssertor )
    //
    //    if ( cua.hasErrors )
    //      println( cua.getErrors.mkString("\n") )
    //    else {
    //        val j = new Java8Exporter( new StdoutLogger(), javaGen, cua, Collections.emptyMap() )
    //        j.exportSchema()
    //        //    VFS.printFileSystemContents( javaGen )
    //      }
  }
}

