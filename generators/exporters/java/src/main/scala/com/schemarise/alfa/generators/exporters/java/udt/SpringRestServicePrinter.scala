package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.{IAnnotation, ICompilationUnitArtifact, IMethodSignature, IService}
import com.schemarise.alfa.compiler.ast.nodes.MethodSignature
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.exporters.java.PrinterBase

import scala.collection.mutable.ListBuffer

class SpringRestServicePrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact) extends PrinterBase(logger, outputDir, cua) {

  override def writeTopComment(): Boolean = false

  def javaMethodImpl(udt: IService, m: MethodSignature): String = {
    val formals = m.formals.map(m =>
      s"""@org.springframework.web.bind.annotation.RequestParam(name="${m._1}") ${javadoc(m._2)}${toJavaTypeName(m._2.dataType)} ${localFieldName(m._1)}""")

    val args = m.formals.map(m => s"${localFieldName(m._1)}")

    val ret = if (m.returnType.isScalarVoid) "" else "return "

    val ann = new ListBuffer[String]()

    if (m.hasAnnotation(IAnnotation.Annotation_Http_Delete))
      ann += "org.springframework.web.bind.annotation.DeleteMapping"

    if (m.hasAnnotation(IAnnotation.Annotation_Http_Get))
      ann += "org.springframework.web.bind.annotation.GetMapping"

    if (m.hasAnnotation(IAnnotation.Annotation_Http_Post))
      ann += "org.springframework.web.bind.annotation.PostMapping"

    if (m.hasAnnotation(IAnnotation.Annotation_Http_Put))
      ann += "org.springframework.web.bind.annotation.PutMapping"

    if (ann.isEmpty)
      ann += "org.springframework.web.bind.annotation.PostMapping /* default */"

    val version = "v" + (udt.versions.size + 1)

    s"""    @${ann.head}("/api/${udt.name.namespace.name}/${udt.name.name}/$version/${m.fullyQualifiedName}")
       |    ${javadoc(m)} public ${toJavaTypeName(m.returnType)} ${m.fullyQualifiedName}( ${formals.mkString(", ")} ) {
       |        ${ret}this.delegate.${m.fullyQualifiedName}( ${args.mkString(", ")} );
       |    }
       |""".stripMargin
  }


  def nonLambdaMethod(ms: IMethodSignature): Boolean = {
    ms.formals.values.filter(f => f.dataType.isLambda()).isEmpty
  }

  def print(udt: IService): Unit = {
    val clz = toJavaVersionedClassName(udt)
    val springClz = s"${clz}SpringRest"

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + springClz)

    val methods = udt.getMethodSignatures.toSet
    val methodSigs = methods. // filter(m => nonLambdaMethod(m._2)).
      map(m => javaMethodImpl(udt, m._2.asInstanceOf[MethodSignature])).mkString("")

    enterFile(f)

    val ctorArgs = udt.constructorFormals.map(e => {
      javadoc(e._2) + super.toJavaTypeName(e._2.dataType) + " " + localFieldName(e._1)
    }).mkString(", ")

    val javaClass = toJavaTypeName(udt)

    // TODO write a Descriptor for the service
    writeln(
      s"""
         |package ${toJavaPackageName(udt)};
         |
         |@org.springframework.web.bind.annotation.RestController(value = "$clz")
         |${javadoc(udt)}public class $springClz${typeParams(udt)} implements $javaClass${typeParams(udt)} {
         |    private final $javaClass delegate;
         |    private static $javaClass staticDelegate;
         |
         |    public static void setDelegateSingleton($javaClass del) {
         |        staticDelegate = del;
         |    }
         |
         |    public $springClz() {
         |        this( staticDelegate );
         |        if ( staticDelegate == null )
         |            throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Cannot create RestController. $springClz.setDelegateSingleton(...) should be called to assign a delegate before starting SpringApplication");
         |    }
         |
         |    public $springClz( $javaClass delegate ) {
         |        this.delegate = delegate;
         |    }
         |
         |$methodSigs
         |}
      """.stripMargin)

    exitFile()
  }
}
