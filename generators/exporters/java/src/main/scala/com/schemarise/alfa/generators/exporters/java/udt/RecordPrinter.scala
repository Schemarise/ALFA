package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IUdtBaseNode}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.{BuilderInnerClassPrinter, ConcreteInnerClassPrinter, TypeDescriptorPrinter, PrinterBase}

class RecordPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes, reqMutable: Boolean) extends PrinterBase(logger, outputDir, cua) {

  val mandatoryInclude = "com.schemarise.alfa.runtime.Record"

  protected val iip = new ConcreteInnerClassPrinter(logger, outputDir, cua, compilerToRt)
  protected val bp = new BuilderInnerClassPrinter(logger, outputDir, cua, compilerToRt, reqMutable)
  protected val mp = new TypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)

  def print(udt: IUdtBaseNode): Unit = {

    val clz = toJavaVersionedClassName(udt)
    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clz)

    val accessorFields = udt.allFields // getNonExtendedFields( udt )

    val accessorMethods = accessorFields.map(f => accessorMethod(clz, f._2)).mkString("")

    val extendsOrImplements = includesAndExtends(udt, mandatoryInclude)
    val imp = if (extendsOrImplements.size > 0) " extends " + extendsOrImplements.mkString(", ") else ""

    enterFile(f)

    val tp = typeParams(udt)

    val udtFqn = udt.name.fullyQualifiedName
    val udtBuilder = builderImplClz(udt)

    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |${javadoc(udt)}public interface ${classNameWithTypeParams(udt)} $imp {
         |
         |$accessorMethods
         |
         |${auxiliaryCode(udt)}
         |}
         |
    """.stripMargin)

    exitFile()
  }

  protected def auxiliaryCode(udt: IUdtBaseNode) = {

    val isTemplated = false //  udt.name.typeParameters.size > 0

    val builder = if (isTemplated) "" else bp.print(udt)
    val ifcImpl = if (isTemplated) "" else iip.print(udt)
    val model = mp.print(udt)

    s"""
       |$builder
       |
       |    //<editor-fold defaultstate="collapsed" desc="Concrete class">
       |$ifcImpl
       |    //</editor-fold>
       |
       |    //<editor-fold defaultstate="collapsed" desc="TypeDescriptor class">
       |$model
       |    //</editor-fold>
       |""".stripMargin
  }
}
