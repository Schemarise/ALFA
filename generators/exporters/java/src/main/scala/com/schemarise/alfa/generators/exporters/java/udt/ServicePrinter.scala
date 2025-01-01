package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.IService
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.PrinterBase

class ServicePrinter(logger: ILogger, outputDir: Path,
                     cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {

  //  val spring = new SpringRestServicePrinter( logger, outputDir, cua )

  def print(udt: IService): Unit = {
    //    spring.print(udt)

    val clzNameOnly = toJavaVersionedClassName(udt)
    val clzWithTmpl = classNameWithTypeParams(udt)

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + clzNameOnly)

    val methods = udt.getMethodSignatures.toSet
    val methodSigs = methods.map(m => javaMethodSig(m._2)).mkString("", ";", ";")

    val extendsOrImplements = includesAndExtends(udt, "com.schemarise.alfa.runtime.Service")

    enterFile(f)

    val ctorArgs = udt.constructorFormals.map(e => {
      javadoc(e._2) + super.toJavaTypeName(e._2.dataType) + " " + localFieldName(e._1)
    }).mkString(", ")

    // TODO write a Descriptor for the service
    writeln(
      s"""package ${toJavaPackageName(udt)};
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |${javadoc(udt)}public interface $clzWithTmpl extends ${extendsOrImplements.mkString(", ")} {
         |$methodSigs
         |
         |    public interface Factory${typeParams(udt)} extends com.schemarise.alfa.runtime.ServiceFactory {
         |        public $clzWithTmpl create( $ctorArgs );
         |    }
         |
         |    //<editor-fold defaultstate="collapsed" desc="TypeDescriptor class">
         |    //</editor-fold>
         |}
      """.stripMargin)

    exitFile()
  }
}
