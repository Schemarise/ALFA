package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.nodes.Testcase
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.{ExpressionPrinter, PrinterBase}

class TestcasePrinter(logger: ILogger, outputDir: Path,
                      cua: ICompilationUnitArtifact, c2r: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {
  def print(udt: Testcase): Unit = {
    val clz = toJavaVersionedClassName(udt)

    val expPrinter = new ExpressionPrinter(logger, outputDir, cua, udt, c2r)

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clz)

    val methods = udt.methodDecls.toSet
    val methodSigs = methods.map(m => {
      val sig = javaMethodSig(m.signature)
      val body = expPrinter.printExpr(m.block)

      val name = m.signature.methodName

      s"""
         |@org.junit.Test
         |public void test${name}() {
         |    $name( new com.schemarise.alfa.runtime.utils.TestScenarioImpl() );
         |}
         |
         |public $sig
         |$body
         |
     """.stripMargin
    }).mkString

    enterFile(f)

    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |${javadoc(udt)}public class $clz implements com.schemarise.alfa.runtime.AlfaTestCase {
         |
         |    private com.schemarise.alfa.runtime.IBuilderConfig __builderConfig = com.schemarise.alfa.runtime.BuilderConfig.builder().build();
         |
         |    @Override
         |    public com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
         |        return __builderConfig;
         |    }
         |$methodSigs
         |}
      """.stripMargin)

    exitFile()
  }
}
