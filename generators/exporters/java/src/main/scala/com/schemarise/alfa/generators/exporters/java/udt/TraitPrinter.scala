package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.ITrait
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.{BuilderInnerClassPrinter, ConcreteInnerClassPrinter, TypeDescriptorPrinter, PrinterBase}

class TraitPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes, reqMutable: Boolean) extends PrinterBase(logger, outputDir, cua) {
  protected val mp = new TypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)
  protected val iip = new ConcreteInnerClassPrinter(logger, outputDir, cua, compilerToRt)
  protected val bp = new BuilderInnerClassPrinter(logger, outputDir, cua, compilerToRt, reqMutable)

  def print(udt: ITrait): Unit = {
    val clz = toJavaVersionedClassName(udt)

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clz)

    val localFields = udt.localFieldNames
    val accessorFields = udt.allFields.filter(f => localFields.contains(f._1))
    val accessorMethods = accessorFields.map(f => accessorMethod(clz, f._2)).mkString("")

    val extendsOrImplements = includesAndExtends(udt, "com.schemarise.alfa.runtime.Trait")

    val model = mp.print(udt)

    enterFile(f)

    val args = if (udt.name.typeParameters.size > 0)
      udt.name.typeParameters.map(_._1.name.fullyQualifiedName).mkString("< ", ", ", " >")
    else ""

    val parentMutators = includesAndExtends(udt, "", true, ".Mutator")

    val isTemplated = false //  udt.name.typeParameters.size > 0
    val builder = if (isTemplated) "" else bp.print(udt)
    val ifcImpl = if (isTemplated) "" else iip.print(udt)

    val bindAnn = bindClassAnnotation(udt)
    val udtFqn = udt.name.fullyQualifiedName

    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |${javadoc(udt)}${bindAnn}public interface ${classNameWithTypeParams(udt)} extends ${extendsOrImplements.mkString(", ")} {
         |
         |    public static com.schemarise.alfa.runtime.TypeDescriptor descriptor = new ${descClz(udt)}();
         |$accessorMethods
         |
         |$builder
         |
         |    //<editor-fold defaultstate="collapsed" desc="Default concrete class">
         |$ifcImpl
         |    //</editor-fold>
         |
         |    //<editor-fold defaultstate="collapsed" desc="TypeDescriptor class">
         |$model
         |    //</editor-fold>
         |}
      """.stripMargin)

    exitFile()
  }

}
