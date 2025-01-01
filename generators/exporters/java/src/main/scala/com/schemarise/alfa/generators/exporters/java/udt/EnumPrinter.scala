package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IEnum}
import com.schemarise.alfa.generators.exporters.java.{PrinterBase, TypeDescriptorPrinter}
import com.schemarise.alfa.compiler.utils.{ILogger, LangKeywords}
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

class EnumPrinter(logger: ILogger, outputDir: Path,
                  cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {
  private val mp = new TypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)

  def print(udt: IEnum): Unit = {
    val clz = toJavaVersionedClassName(udt)

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clz)

    val consts = udt.allFields.map(f => {
      val lex =
        if (f._2.enumLexical.isDefined)
          s"""java.util.Optional.of("${f._2.enumLexical.get}")"""
        else
          s"java.util.Optional.empty()"

      s"""${javadoc(f._2)}${validLangIdentifier(f._1, LangKeywords.javaKeywords)}("${f._1}", $lex) """
    }).mkString(", ")

    val model = mp.print(udt)

    enterFile(f)

    val bindAnn = bindClassAnnotation(udt)

    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |$bindAnn
         |${javadoc(udt)}public enum $clz implements com.schemarise.alfa.runtime.Enum {
         |    $consts;
         |
         |    private final java.lang.String value;
         |    private final java.util.Optional< java.lang.String > lexical;
         |
         |    private static java.util.Map< java.lang.String, $clz > mappings;
         |
         |    $clz(java.lang.String v, java.util.Optional< java.lang.String > lex) {
         |        value = v;
         |        lexical = lex;
         |    }
         |
         |    public static $clz fromValue(java.lang.String v) {
         |        if ( mappings == null ) {
         |            java.util.Map< java.lang.String, $clz > m = new java.util.HashMap<>();
         |            for ($clz c: $clz.values()) {
         |                m.put( c.value, c );
         |            }
         |            mappings = m;
         |        }
         |
         |        return mappings.get( v );
         |    }
         |
         |    public java.util.Optional< String > getLexicalValue() {
         |        return lexical;
         |    }
         |
         |    public java.lang.String value() {
         |        return value;
         |    }
         |
         |    public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
         |        return ${descClz(udt)}.INSTANCE;
         |    }
         |
         |    //<editor-fold defaultstate="collapsed" desc="TypeDescriptor class">
         |$model
         |    //</editor-fold>
         |}
      """.stripMargin)

    exitFile()

  }
}
