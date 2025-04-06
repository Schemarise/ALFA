package com.schemarise.alfa.generators.exporters.markdown

import com.schemarise.alfa.compiler.ast.model

import java.nio.file.Path
import schemarise.alfa.runtime.model._
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.{CompilerToRuntimeTypes, TextWriter}
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.util
import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap

class UdtPrinter(logger: ILogger, outputDir: Path,
                 c2r: CompilerToRuntimeTypes, cua: ICompilationUnitArtifact, includeUml: Boolean, htmlLinks : Boolean) extends TextWriter(logger) {

  private val SPACE = "&nbsp;"
  val utils = new MarkdownUtils(cua, c2r)

  val allNss: AllNamespaces = c2r.getAllNamespaces
  val hierarchy: Hierarchy = c2r.getCompleteHierarchy()
  val udts: AllUdts = c2r.getAllUdts

  override def writeTopComment() = false

  def printType(t: IDataType, asHtml: Boolean): String = {

    t match {
      case s: OptionalDataType =>
        val str = s"${printType(s.getComponentType, asHtml)}"
        if (asHtml)
          s"<i>$str</i> <b>?</b>"
        else
          s"_${str}_ **?**"

      case s: ScalarDataType =>
        val deco =
          if (s.getMin.isPresent || s.getMax.isPresent) {
            val min = if (s.getMin.isPresent) s.getMin.get().caseValue().toString else "*";
            val max = if (s.getMax.isPresent) s.getMax.get().caseValue().toString else "*";

            val precision = if (s.getPrecision.isPresent) s.getPrecision.get().getPrecision + "" else "*"
            val scale = if (s.getPrecision.isPresent) s.getPrecision.get().getScale + "" else "*"

            val pre = if (s.getScalarType == ScalarType.decimalType) s"$scale, $precision, " else ""

            s"($pre$min, $max)"
          }
          else if (s.getStrPattern.isPresent)
            s"""("${s.getStrPattern.get()}")"""

          else if (s.getPrecision.isPresent) {
            val p = s.getPrecision.get()
            s"(${p.getScale}, ${p.getPrecision})"
          }
          else
            ""

        s.getScalarType.toString.replaceAll("Type", "") + deco

      case s: UdtDataType => utils.udtAsLink(s, true, false, asHtml, htmlLinks)
      case s: ListDataType => s"list< ${printType(s.getComponentType, asHtml)} >"
      case s: SetDataType => s"set< ${printType(s.getComponentType, asHtml)} >"
      case s: MapDataType => s"map< ${printType(s.getKeyType, asHtml)}, ${printType(s.getValueType, asHtml)} >"

      case s: EnumDataType =>
        val enf = s.getFields.asScala.map(e => e).mkString(", ")
        s"enum< $enf >"

      case s: TupleDataType =>
        val enf = s.getFields.asScala.map(f => s"${f._1} : ${printType(f._2.getDataType, asHtml)}").mkString(", ")
        s"tuple< $enf >"

      case s: TryDataType => s"try< ${printType(s.getComponentType, asHtml)} >"

      case s: EncryptedDataType => s"encrypted< ${printType(s.getComponentType, asHtml)} >"

      case s: CompressedDataType => s"compressed< ${printType(s.getComponentType, asHtml)} >"

      case s: TabularDataType => s"table< ${printType(s.getComponentType, asHtml)} >"

      case s: EitherDataType => s"either< ${printType(s.getLeftComponentType, asHtml)}, ${printType(s.getRightComponentType, asHtml)} >"

      case s: MetaDataType =>
        "$" + s.getMetaType.value().toLowerCase

      case _ => "Unhandled type " + t.getClass.getName
    }
  }

  def cnv(op: MathOperatorType) = {
    op match {
      case MathOperatorType.Add => "+"
      case MathOperatorType.Divide => "/"
      case MathOperatorType.Modulus => "%"
      case MathOperatorType.Multiply => "*"
      case MathOperatorType.Subtract => "-"
    }
  }

  def printExpr(e: IExpression): String = {
    e match {
      case a: schemarise.alfa.runtime.model.Expression.CaseLiteralExpr =>
        a.getLiteralExpr.getValue
      case a: schemarise.alfa.runtime.model.Expression.CaseIdentifierExpr =>
        a.caseValue().toString
      case a: schemarise.alfa.runtime.model.Expression.CaseMathExpr =>
        val me = a.getMathExpr
        printExpr(me.getLhs) + cnv(me.getOperator) + printExpr(me.getRhs)
      case a: schemarise.alfa.runtime.model.Expression.CaseMethodCallExpr =>
        val mce = a.getMethodCallExpr

        val args = mce.getArgs.asScala.map(arg => {
          printExpr(arg)
        }).mkString(", ")

        s"${mce.getName}($args)"

      case _ => "Unhandled Expr " + e.getClass.getName
    }
  }

  def printFields(udt: ModelBaseNode, fields: Map[String, IAttribute]) = {
    val fieldsValues = fields.values.toList
    printFieldLists(udt, fieldsValues)
  }

  def mdToHtml(mdStr: String) = {
    //    val mdStr2 = "Hello World\n* A\n* B\n* C\n"
    val parser = Parser.builder.build
    val document = parser.parse(mdStr)
    val renderer = HtmlRenderer.builder.escapeHtml(true).sanitizeUrls(true).build
    val html = renderer.render(document)
    html
  }

  def printFieldLists(udt: ModelBaseNode, fields: List[IAttribute]) = {
    val hasExprs = fields.filter(f => f.getDefaultValue.isPresent).size > 0
    val hasAsserts = udt.isInstanceOf[UdtBaseNode] && udt.asInstanceOf[UdtBaseNode].getAsserts.isPresent

    val defaultExpression = if (hasExprs) "\n      <th>Default</th>" else ""
    val assertHtml = if (hasAsserts) "\n      <th>Referenced Asserts</th>" else ""

    writeln(
      s"""
         |<table >
         |  <thead>
         |    <tr>
         |      <th>Name</th>
         |      <th>Datatype</th>$defaultExpression
         |      <th>Description</th>$assertHtml
         |    </tr>
         |  </thead>
         |  <tbody>""".stripMargin)

    fields.foreach(m => {
      val doc = mdToHtml(m.getDoc.orElse("").replace('\n', ' '))

      writeln(
        s"""    <tr>
           |        <td>${m.getName}</td>
           |        <td>${printType(m.getDataType, true)}</td>
           |        <td>$doc</td>
           |    </tr>""".stripMargin)
    })

    writeln(
      """
        |  </tbody>
        |</table>
      """.stripMargin)
  }

  def printEnumFields(fields: Map[String, Field]) = {
    writeln("\n")
    writeln(s"| Name        | Description |")
    writeln(s"| ----------- | ----------- |")

    fields.values.map(f => {
      writeln(s"| ${f.getName} | ${f.getDoc.orElse("").replace('\n', ' ')}  |")
    })
  }

  def printUdt(udt: UdtBaseNode) = {
    val localNames = udt.getLocalFieldNames.asScala.toSet

    val locals = ListMap(udt.getAllFields.asScala.toStream.filter(f => localNames.contains(f._1)): _*)
    val nonLocals = ListMap(udt.getAllFields.asScala.toStream.filter(f => !localNames.contains(f._1)): _*)

    if (udt.isInstanceOf[Entity]) {
      val e = udt.asInstanceOf[Entity]
      if (e.getKey.isPresent) {
        if (e.getKey.get().isKeyRef) {
          val k = printType(e.getKey.get().getKeyRef, false)
          writeln(s"\n**Key:** $SPACE $k")
        }
        else {
          val keyFields = e.getKey.get().getKeyFields
          printFieldLists(udt, keyFields.asScala.toList)
        }
      }
      else {
        writeln("Keyless entity")
      }
    }

    // Fields
    if (locals.size > 0) {
      writeln("\n## Local Fields")
      if (udt.isInstanceOf[Enum])
        printEnumFields(locals)
      else
        printFields(udt, locals)

    }
    else
      writeln("\nNo local fields declared\n")


    if (nonLocals.size > 0) {
      writeln("\n<br/>\n")

      writeln("\n## Inherited Fields")
      if (udt.isInstanceOf[Enum])
        printEnumFields(locals)
      else
        printFields(udt, nonLocals)
    }


    // Referenced from
    udt.getReferencedInFieldTypeFrom.ifPresent(f => {
      val types = f.asScala.
        filter( e => e.getIsSynthetic.isEmpty || e.getIsSynthetic.get() == false ).
        sortBy(n => n.getFullyQualifiedName).map(e => utils.udtAsLink(e, true, false, false))

      val typesStr = types.map(s => s"- $s").mkString("\n")

      if (f.size() <= 10) {
        writeln("\n<br/>\n")
        writeln("### Referenced from fields in:")
        writeln(typesStr)
      } else {
        writeln(
          s"""
             |<details>
             |  <summary>Referenced from fields in:</summary>
             |
             |  ${typesStr}
             |</details>
            """.stripMargin)
      }
    })
  }

  def printService(srv: Service) = {

    val ctor = srv.getConstructorFormals

    // Constructor
    if (ctor.size > 0) {
      writeln("\n## Constructor Arguments")
      printFields(srv, ctor.asScala.toMap)
    } else
      writeln("\nNo constructor arguments\n")

    val ms = srv.getMethods.asScala.map(e => e._1 -> e._2.getSignature)

    val sigs = cua.getUdt(srv.getName.getFullyQualifiedName).get.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Service].getMethodSignatures()

    if (sigs.size > 0) {
      writeln("## Methods")

      writeln(
        """
          |<table >
          |  <thead>
          |    <tr>
          |      <th>Name</th>
          |      <th>Arguments</th>
          |      <th>Return type</th>
          |      <th>Exceptions</th>
          |      <th>Description</th>
          |    </tr>
          |  </thead>
          |  <tbody>""".stripMargin)


      sigs.keySet.toList.sorted.map( k => sigs.get(k).get).foreach(m => {
        val argTableStart =
          """          <!-- start -->
            |          <table>
            |            <thead>
            |              <tr>
            |                <th>Name</th>
            |                <th>Type</th>
            |                <th>Description</th>
            |              </tr>
            |            </thead>
            |            <tbody>""".stripMargin

        val argTableEnd =
          """
            |            </tbody>
            |          </table>
            |          <!-- end -->
          """.stripMargin

        val fmls = m.formals.values.map(f => {

          val scope = if ( f.scope.isEmpty ) "" else " : " + f.scope.get

          s"""
             |              <tr>
             |                <td>${f.name}${scope}</td>
             |                <td>${f.dataType}</td>
             |                <td>${oneLineDoc(f.docs)}</td>
             |              </tr>""".stripMargin
        }).mkString(argTableStart, "", argTableEnd)

        writeln(
          s"""    <tr>
             |        <td>${m.name}</td>
             |        <td>
             |$fmls</td>
             |        <td>${m.returnType}</td>
             |        <td>${m.exceptionTypes.mkString(", ")}</td>
             |        <td>${oneLineDoc(m.docs)}</td>
             |    </tr>""".stripMargin)
      })

      writeln(
        """
          |  </tbody>
          |</table>
        """.stripMargin)
    }
  }

  private def oneLineDoc(docs: Seq[model.IDocumentation]) = {
    docs.mkString(" ").replace('\n', ' ')
  }


  def printTestcase(srv: Testcase) = {

    val ms = srv.getMethods()

    if (ms.size() > 0) {
      writeln("## Scenarios")

      writeln(
        """
          |<table >
          |  <thead>
          |    <tr>
          |      <th>Name</th>
          |      <th>Description</th>
          |    </tr>
          |  </thead>
          |  <tbody>""".stripMargin)


      ms.asScala.foreach(m => {
        val doc = m._2.getSignature.getDoc.orElse("")

        writeln(
          s"""    <tr>
             |        <td>${m._1}</td>
             |        <td>$doc</td>
             |    </tr>""".stripMargin)
      })

      writeln(
        """
          |  </tbody>
          |</table>
        """.stripMargin)
    }
  }

  def print(): Unit = {

    udts.getUdts.asScala.foreach(u => {
      val name = u.getFullyQualifiedName
      val uBase = c2r.getUdtDetails(name).getResult
      val deco = utils.decorator(u.getUdtType, true)

      enterFile(s"UDT-$name.md")

      writeln(s"<sub>&lt;$SPACE [Namespace](index.md)</sub>")
      writeln(s"# $deco $name")

      uBase.getDoc.ifPresent(d => {
        writeln(">" + d.replaceAll("\n", "\n>"))
      })

      if (includeUml) {
        writeln(utils.classDiagram(outputDir, cua.getUdt(name).get))
      }

      if (uBase.getIncludes.isPresent && uBase.getIncludes.get.size() > 0) {
        val incs = uBase.getIncludes.get.asScala.map(e => {
          utils.udtAsLink(e, true, false, false, htmlLinks)
        }).mkString(utils.spacing)

        writeln(s"\n**Includes:**$SPACE" + incs)
      }

      if ( uBase.getAnnotations.isPresent )
        writeAnnotations( uBase.getAnnotations.get() )

      uBase match {
        case udt: UdtBaseNode =>
          printUdt(udt)
        case _ => uBase match {
          case srv: Service =>
            printService(srv)
          case srv: Testcase =>
            printTestcase(srv)
          case _ =>
        }
      }

      exitFile()
    })
  }

  private def writeAnnotations(ann: util.Map[String, util.Map[String, IExpression]]) = {
    writeln(s"\n**Annotations:**")

    ann.asScala.foreach( e => {
      writeln( "  - @" + e._1 )
    })
  }

  override def outputDirectory: Path = outputDir
}
