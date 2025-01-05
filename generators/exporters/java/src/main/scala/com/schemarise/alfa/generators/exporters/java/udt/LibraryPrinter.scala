package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.nodes.Library
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.{ExpressionPrinter, PrinterBase}

class LibraryPrinter(logger: ILogger, outputDir: Path,
                     cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {
  def print(udt: Library): Unit = {
    val clz = toJavaVersionedClassName(udt)

    val expPrinter = new ExpressionPrinter(logger, outputDir, cua, udt, compilerToRt)

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clz)

    val methodSigs = methodPrinter(udt.methodDecls.toSet, expPrinter)
    //    val methods = udt.methodDecls.toSet
    //    val methodSigs = methods.map( m => {
    //      val sig = javaMethodSig(m.signature)
    //      val body = expPrinter.printExpr(m.block)
    //      s"""
    //         |public $sig
    //         |$body
    //         |
    //     """.stripMargin
    //    } ).mkString

    enterFile(f)

    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |${javadoc(udt)}public class $clz implements com.schemarise.alfa.runtime.Library {
         |    private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;
         |
         |    public $clz( com.schemarise.alfa.runtime.IBuilderConfig bcfg ) {
         |        __builderConfig = bcfg;
         |    }
         |
         |    @Override
         |    public com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
         |        return __builderConfig;
         |    }
         |
         |$methodSigs
         |}
      """.stripMargin)

    exitFile()
  }
}
