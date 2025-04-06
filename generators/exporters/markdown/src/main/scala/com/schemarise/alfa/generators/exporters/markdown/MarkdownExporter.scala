package com.schemarise.alfa.generators.exporters.markdown

import java.nio.file.Path
import com.schemarise.alfa.compiler.utils.VFS
import com.schemarise.alfa.generators.common._
import schemarise.alfa.runtime.model.UdtMetaType

class MarkdownExporter(param: AlfaExporterParams) extends AlfaExporter(param) with SupportedGenerator {

  override def writeTopComment() = false

  val htmlLinks = "htmlLinks"

  override def exportSchema(): List[Path] = {
    val includeUml = param.cua.getUdtVersionNames().size < 2000

    val c2r = CompilerToRuntimeTypes.create(logger, param.cua)

    val ip = new IndexPrinter(logger, param.outputDir, c2r, param.cua, includeUml)
    ip.print()

    val htmlLinks =  "true" == param.exportConfig.getOrDefault("htmlLinks", "false")

    val up = new UdtPrinter(logger, param.outputDir, c2r, param.cua, includeUml, htmlLinks)
    up.print()

    writeIcons(param.outputDir)

    Nil
  }

  private def writeIcons(p : Path) = {

    val supportedTypes = UdtMetaType.values().map( e => {
      e match {
        case UdtMetaType.annotationType => ("A", "90CCDE", e)
        case UdtMetaType.entityType => ("E", "A09BCC", e)
        case UdtMetaType.enumType => ("Em", "86B0BE", e)
        case UdtMetaType.keyType => ("K", "67931A", e)
        case UdtMetaType.libraryType => ("L", "88A02C", e)
        case UdtMetaType.recordType => ("R", "D9B01C", e)
        case UdtMetaType.serviceType => ("S", "754F71", e)
        case UdtMetaType.traitType => ("T", "995579", e)
        case UdtMetaType.unionType => ("U", "418798", e)
        case UdtMetaType.nativeUdtType => ("N", "985241", e)
        case UdtMetaType.testcaseType => ("Tc", "985241", e)
        case _ => ("", "", e)
      }
    }).filter( x => x._1 != "")

    supportedTypes.foreach( s => {
      val letter = s._1
      val colour = s._2
      val udttype = s._3.value()

      val icon =
        s"""<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20">
           |  <circle cx="10" cy="10" r="10" fill="#$colour" />
           |  <text x="50%" y="50%" text-anchor="middle" fill="white" font-size="10px" font-family="Arial" dy=".3em">$letter</text>
           |</svg>
           |
           |""".stripMargin

      val large =
        s"""
           |<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40">
           |  <circle cx="20" cy="20" r="20" fill="#$colour" />
           |  <text x="50%" y="50%" text-anchor="middle" fill="white" font-size="20px" font-family="Arial" dy=".3em">$letter</text>
           |</svg>
           |
           |""".stripMargin

      VFS.write(p.resolve(s"images/$udttype.svg"), icon)
      VFS.write(p.resolve(s"images/$udttype-lg.svg"), large)
    })
  }

  override def supportedConfig(): Array[String] = Array(htmlLinks)

  override def name: String = "markdown"

}
