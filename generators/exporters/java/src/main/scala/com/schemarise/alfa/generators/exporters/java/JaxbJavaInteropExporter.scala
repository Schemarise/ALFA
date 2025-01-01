package com.schemarise.alfa.generators.exporters.java

import java.nio.file.{Path, Paths}

import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, UdtType}
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams}


class JaxbJavaInteropExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  val cua = param.cua

  private val writer = this

  private def processJaxbToAlfa(udt: UdtBaseNode): Unit = {
    val name = udt.name.name

    val ns = udt.name.namespace.name
    val outputClz = ns + "." + name
    val inputClz = "jaxb_" + outputClz.replace("_", ".")

    val mappings = udt.allFields.map(f => {

      val dataType = f._2.dataType
      val NAME = pascalCase(f._1)

      if (dataType.isEncOptional()) {
        val enc = dataType.asInstanceOf[EnclosingDataType]

        if (enc.componentType.isList()) {
          val listType = enc.componentType.asInstanceOf[ListDataType].componentType
          val tgt = transformTargetClass(listType)
          s"        if ( in.get$NAME() != null ) out.set$NAME( java.util.Optional.of( in.get$NAME().stream().map( e -> ${tgt}transform(e) ).filter( e -> e != null ).collect(java.util.stream.Collectors.toList() ) ) );"
        }
        else {
          val ct = enc.componentType
          val access = if (ct.isScalarBoolean) "is" else "get"
          val tgt = transformTargetClass(ct)

          val nullcheck = if (ct.isScalarBoolean || ct.isScalarNumeric) "" else s"if ( in.$access$NAME() != null )"

          s"        ${nullcheck}out.set$NAME( java.util.Optional.of( ${tgt}transform( in.$access$NAME() ) ) );"
        }
      }
      else {
        val access = if (dataType.isScalarBoolean) "is" else "get"

        if (dataType.isList()) {
          val listType = dataType.asInstanceOf[ListDataType].componentType
          val tgt = transformTargetClass(listType)
          s"        out.addAll$NAME( in.get$NAME().stream().map( e -> ${tgt}transform(e) ).filter( e -> e != null ).collect(java.util.stream.Collectors.toList() ) );"
        }
        else {
          val tgt = transformTargetClass(dataType)
          s"        out.set$NAME( ${tgt}transform( in.$access$NAME() ) );"
        }
      }
    }).mkString("\n")

    val body =
      if (udt.udtType == UdtType.enum) {
        val cases = udt.allFields.map(e => s"""            case ${e._1} : return ${outputClz}.${e._1}; """).mkString("\n")
        s"""        switch ( in ) {
           |$cases
           |        }
           |        throw new RuntimeException("Unhandled value " + in);""".stripMargin
      } else {
        s"""        $outputClz.${name}Builder out = $outputClz.${name}Descriptor.INSTANCE.builder();
           |$mappings
           |        return out.build();""".stripMargin
      }

    writer.writeln(
      s"""    public static $outputClz transform($inputClz in ) {
         |$body
         |    }
         |""".stripMargin)
  }

  private def transformTargetClass(t: IDataType) = {
    if (t.isInstanceOf[UdtDataType]) {
      t.asInstanceOf[UdtDataType].name.text + "."
    }
    else if (t.isInstanceOf[MetaDataType]) {
      "GenericTransformer."
    }
    else
      ""
  }

  private def processAlfaToJaxb(udt: UdtBaseNode): Unit = {
    val name = udt.name.name

    val ns = udt.name.namespace.name
    val inputClz = ns + "." + name
    val outputClz = "jaxb_" + inputClz.replace("_", ".")

    val mappings = udt.allFields.map(f => {

      val dataType = f._2.dataType
      val NAME = pascalCase(f._1)

      if (dataType.isEncOptional()) {
        val enc = dataType.asInstanceOf[EnclosingDataType]

        if (enc.componentType.isList()) {
          val listType = enc.componentType.asInstanceOf[ListDataType].componentType
          val tgt = transformTargetClass(listType)
          s"        if ( in.get$NAME().isPresent() ) out.get$NAME().addAll( in.get$NAME().get().stream().map( e -> ${tgt}transform(e) ).filter( e -> e != null ).collect(java.util.stream.Collectors.toList() ) );"
        } else {
          val tgt = transformTargetClass(enc.componentType)
          s"        if ( in.get$NAME().isPresent() ) out.set$NAME( ${tgt}transform( in.get$NAME().get() ) );"
        }
      }
      else {
        if (dataType.isList()) {
          val listType = dataType.asInstanceOf[ListDataType].componentType
          val tgt = transformTargetClass(listType)
          s"        out.get$NAME().addAll( in.get$NAME().stream().map( e -> ${tgt}transform(e) ).filter( e -> e != null ).collect(java.util.stream.Collectors.toList() ) );"
        } else {
          val tgt = transformTargetClass(dataType)
          s"        out.set$NAME( ${tgt}transform( in.get$NAME() ) );"
        }
      }
    }).mkString("\n")

    val body =
      if (udt.udtType == UdtType.enum) {
        val cases = udt.allFields.map(e => s"""            case ${e._1} : return ${outputClz}.${e._1}; """).mkString("\n")
        s"""        switch ( in ) {
           |$cases
           |        }
           |        throw new RuntimeException("Unhandled value " + in);""".stripMargin
      } else {
        s"""        $outputClz out = new $outputClz();
           |$mappings
           |        return out;""".stripMargin
      }

    writer.writeln(
      s"""    public static $outputClz transform($inputClz in ) {
         |$body
         |    }
         |""".stripMargin)
  }

  def process(): Unit = {
    val nsGrps = cua.getUdtVersionNames().groupBy(n => n.namespace.name)

    nsGrps.foreach(g => {
      val ns = g._1
      val udts = g._2

      val path1 = "../java/" + ns.replace(".", "/") + "/alfa2jaxb/TransformAlfaToJaxb.java"
      logger.info("Writing " + Paths.get(path1).normalize())
      writer.enterFile(path1)
      writer.writeln(
        s"""package $ns.alfa2jaxb;
           |
           |public class TransformAlfaToJaxb {
      """.stripMargin)

      cua.getUdtVersionNames().foreach(e => {
        val path1 = "../java/" + ns.replace(".", "/") + s"/alfa2jaxb/${e.name}.java"
        writer.enterFile(path1)

        writer.writeln(
          s"""package $ns.alfa2jaxb;
             |
             |public final class ${e.name} extends TransformAlfaToJaxb {
           """.stripMargin)

        processAlfaToJaxb(cua.getUdt(e.fullyQualifiedName).get.asInstanceOf[UdtBaseNode])
        writeln("\n}")
        writer.exitFile()
      })

      writer.writeln(defaultTransforms)
      writeln("\n}")
      writer.exitFile()

      writer.writeln(alfaToJaxbTransform(udts))
      val alfa2jaxbGenericTransformer = "../java/" + ns.replace(".", "/") + s"/alfa2jaxb/GenericTransformer.java"
      writer.enterFile(alfa2jaxbGenericTransformer)
      writer.writeln(jaxbToAlfaTransform(s"$ns.alfa2jaxb", udts))
      writer.exitFile()

      val path2 = "../java/" + ns.replace(".", "/") + "/jaxb2alfa/TransformJaxbToAlfa.java"
      logger.info("Writing " + Paths.get(path2).normalize())

      writer.enterFile(path2)

      writer.writeln(
        s"""package $ns.jaxb2alfa;
           |
           |public class TransformJaxbToAlfa {
      """.stripMargin)

      cua.getUdtVersionNames().foreach(e => {
        val path1 = "../java/" + ns.replace(".", "/") + s"/jaxb2alfa/${e.name}.java"
        writer.enterFile(path1)

        writer.writeln(
          s"""package $ns.jaxb2alfa;
             |
             |public final class ${e.name} extends TransformJaxbToAlfa {
           """.stripMargin)

        processJaxbToAlfa(cua.getUdt(e.fullyQualifiedName).get.asInstanceOf[UdtBaseNode])

        writeln("\n}")
        writer.exitFile()
      })

      writer.writeln(defaultTransforms)
      writeln("\n}")

      writer.exitFile()

      val jaxb2alfaGenericTransformer = "../java/" + ns.replace(".", "/") + s"/jaxb2alfa/GenericTransformer.java"
      writer.enterFile(jaxb2alfaGenericTransformer)
      writer.writeln(jaxbToAlfaTransform(s"$ns.jaxb2alfa", udts))
      writer.exitFile()
    })
  }

  private def defaultTransforms() = {
    s"""
       |    public static java.time.LocalDate transform( java.time.LocalDate s ) { return s; }
       |    public static java.time.LocalTime transform( java.time.LocalTime s ) { return s; }
       |    public static java.time.ZonedDateTime transform( java.time.ZonedDateTime c ) { return c; }
       |
       |    public static String transform( String s ) { return s; }
       |    public static int transform( int s ) { return s; }
       |    public static long transform( long s ) { return s; }
       |    public static long transform( Long s ) { return s; }
       |
       |    public static short transform( short s ) { return s; }
       |    public static java.math.BigDecimal transform( java.math.BigDecimal s ) { return s; }
       |    public static long transform( java.math.BigInteger s ) { return s.longValue(); }
       |    public static double transform( double s ) { return s; }
       |
       |    public static Boolean transform( Boolean s ) { return s; }
       |
        """.stripMargin
  }


  private def alfaToJaxbTransform(udts: Set[IUdtVersionName]) = {

    val anycases = udts.map(u => {
      val n = u.fullyQualifiedName
      s"""            case "$n" : return (T) transform( ( $n ) o);"""
    }).mkString("\n")

    s"""/*
       |    public static <T> T transform(Object o) {
       |
       |        switch (o.getClass().getName() ) {
       |$anycases
       |        }
       |
       |        throw new RuntimeException( "Unknown class to transform " + o.getClass().getName() );
       |    }
       |*/
       |}
        """.stripMargin
  }

  private def jaxbToAlfaTransform(pkg: String, udts: Set[IUdtVersionName]) = {

    val anycases = udts.map(u => {
      val n = u.fullyQualifiedName.replace("_", ".")
      s"""            case "jaxb_$n" : return transform( ( jaxb_$n ) o);"""
    }).mkString("\n")

    s"""package $pkg;
       |
       |public class GenericTransformer {
       |    public static com.schemarise.alfa.runtime.AlfaObject transform(Object o) {
       |        switch (o.getClass().getName() ) {
       |$anycases
       |        }
       |
       |        if ( o instanceof org.w3c.dom.Element ) return null;
       |
       |        throw new RuntimeException( "Unknown class to transform " + o.getClass().getName() );
       |    }
       |}
        """.stripMargin
  }

  override def exportSchema(): List[Path] = {
    process()
    Nil
  }

  override def supportedConfig(): Array[String] = Array("namespace")

  override def requiredConfig() = supportedConfig()

  override def name: String = "javajaxb"
}
