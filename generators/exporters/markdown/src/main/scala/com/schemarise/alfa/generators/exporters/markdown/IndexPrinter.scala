package com.schemarise.alfa.generators.exporters.markdown

import java.nio.file.Path
import schemarise.alfa.runtime.model.{ModelBaseNode, UdtBaseNode, UdtMetaType, Namespace}
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.{CompilerToRuntimeTypes, TextWriter}

import scala.collection.JavaConverters._

class IndexPrinter(logger: ILogger, outputDir: Path,
                   c2r: CompilerToRuntimeTypes, cua: ICompilationUnitArtifact, includeUml: Boolean) extends TextWriter(logger) {

  val allNss = c2r.getAllNamespaces
  val hierarchy = c2r.getCompleteHierarchy()

  val utils = new MarkdownUtils(cua, c2r)

  override def writeTopComment() = false

  def print(): Unit = {
    printByName()
    printByType()
  }

  def printByName(): Unit = {
    enterFile("sorted-name-index.md")

    allNss.getNamespaces.asScala.foreach(e => {
      val ns = e._1
      val udts = hierarchy.getNsUdts.get(e._1)

      val nonNativeTotal = udts.asScala.filter(f => f.getName.getUdtType != UdtMetaType.nativeUdtType).size

      if (nonNativeTotal > 0) {
        writeln(s"<br/>\n\n## Namespace : $ns\n")

        e._2.getDoc.ifPresent(d => {
          writeln(">" + d.replaceAll("\n", "\n>"))
        })

        writeln("\n")
        addUmlDiagram(e._2)

        writeln(s"\n<sub>[Sort by type](index.md)</sub>")

        writeln("\n")
        writeln(s"| Name        | Summary |")
        writeln(s"| ----------- | ------- |")

        val sortedByName = udts.asScala.map(r => c2r.getUdtDetails(r.getName.getFullyQualifiedName).getResult).toSeq.sortBy(r => {
          (r.getName.getFullyQualifiedName, r.getName.getUdtType.toString)
        })

        sortedByName.foreach(u => {
          printUdtLine(u)
        })
      }
    })

    exitFile()
  }

  private def printUdtLine(u: ModelBaseNode) = {
    val name = u.getName.getFullyQualifiedName
    val udt = c2r.getUdtDetails(name).getResult

    val doc = udt.getDoc.orElse("").trim
    val newline = doc.indexOf("\n")

    val summary = if (newline > 0) doc.substring(0, newline) + " ... " else doc


    val isSynthetic = if (udt.isInstanceOf[UdtBaseNode])
      udt.asInstanceOf[UdtBaseNode].getIsSynthetic
    else
      false

    val isNative = u.getName.getUdtType == UdtMetaType.nativeUdtType

    if (!isNative && !isSynthetic) {
      val udtLink = utils.udtAsLink(u.getName, false, false, false)
      writeln(s"| $udtLink | $summary |")
    }
  }


  def printByType(): Unit = {
    enterFile("index.md")

    allNss.getNamespaces.asScala.foreach(e => {
      val ns = e._1
      val udts = hierarchy.getNsUdts.get(e._1)

      val nonNativeTotal = udts.asScala.filter(f => f.getName.getUdtType != UdtMetaType.nativeUdtType).size

      if (nonNativeTotal > 0) {

        writeln(s"<br/>\n\n## Namespace : $ns\n")

        e._2.getDoc.ifPresent(d => {
          writeln(">" + d.replaceAll("\n", "\n>"))
        })

        writeln("\n")

        addUmlDiagram(e._2)

        writeln(s"\n<sub>[Sort by name](sorted-name-index.md)</sub>")

        writeln("\n")
        writeln(s"| Name        | Summary |")
        writeln(s"| ----------- | ------- |")

        val sortedByName = udts.asScala.map(r => c2r.getUdtDetails(r.getName.getFullyQualifiedName).getResult).toSeq.sortBy(r => {
          (r.getName.getUdtType.toString, r.getName.getFullyQualifiedName)
        })

        sortedByName.foreach(u => {
          printUdtLine(u)
        })
      }
    })

    exitFile()
  }

  private def addUmlDiagram(n:Namespace) = {
    if (includeUml) {
      val defs = utils.generateIndexPageMermaid(logger, cua, n)

      val uml =
        s"""
           |${MarkdownUtils.plantumlHeader()}
           |
           |$defs
           |
           |@enduml
           |""".stripMargin

      val svgLink = MarkdownUtils.writeSvgAndCreateLink( outputDir, n.getQualifiedName, uml)
      writeln(svgLink)
    }
  }

  override def outputDirectory: Path = outputDir

}
