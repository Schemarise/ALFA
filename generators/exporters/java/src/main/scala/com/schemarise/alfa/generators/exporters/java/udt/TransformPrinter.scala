package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.nodes.{Transformer, UdtBaseNode}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.{ExpressionPrinter, PrinterBase}

object TransformPrinter {
  val TransformSuffix = "__Transformer"
}

class TransformPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {
  def print(udt: UdtBaseNode, transformers: Seq[Transformer]) = {
    val clz = toJavaVersionedClassName(udt) + TransformPrinter.TransformSuffix
    val pkg = toJavaPackageName(udt)
    val f = toJavaFileName(pkg + "/" + clz)

    val expPrinter = new ExpressionPrinter(logger, outputDir, cua, udt, compilerToRt)

    val methods = transformers.map(t => t.methodDecls).flatten
    val methodStrs = methodPrinter(methods.toSet, expPrinter)

    enterFile(f)

    writeln(
      s"""
         |package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |public class $clz implements com.schemarise.alfa.runtime.Transformer {
         |    private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;
         |
         |    public $clz() {
         |        this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
         |    }
         |
         |    public $clz(com.schemarise.alfa.runtime.IBuilderConfig cc) {
         |        __builderConfig = cc;
         |    }
         |
         |    private com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
         |        return __builderConfig;
         |    }
         |
         |$methodStrs
         |}
      """.stripMargin)
    exitFile()

  }
}
