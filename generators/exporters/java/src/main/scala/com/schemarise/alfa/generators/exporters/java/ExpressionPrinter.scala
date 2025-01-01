package com.schemarise.alfa.generators.exporters.java

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr._
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

import java.nio.file.Path


class ExpressionPrinter(logger: ILogger, outputDir: Path,
                        cua: ICompilationUnitArtifact, udt: IUdtBaseNode,
                        compilerToRt: CompilerToRuntimeTypes)
  extends PrinterBase(logger, outputDir, cua) {

  def buildAsserts(): String = {

    val singles = udt.allSingularAsserts.values.map(a => {
      s"""
         |        private void __assert${a.name}(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig, String __assertName) {
         |        }
         |
       """.stripMargin
    }).mkString("")

    val vectors =
      udt.allVectorizedAsserts.values.map(a => {
        val arg = localFieldName(a.argName.get)
        val udtName = toJavaVersionedClassName(udt)

        s"""
           |        Void _assert${a.name}(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig, java.util.stream.Stream<$udtName> $arg) {
           |            return null;
           |        }
           |
           |        public void assert${a.name}(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig, java.util.stream.Stream<$udtName> $arg, String __assertName) {
           |        }
           |
       """.stripMargin
      }).mkString("")

    singles ++ vectors
  }
  def printExpr(ex: IExpression): String = ""

}
