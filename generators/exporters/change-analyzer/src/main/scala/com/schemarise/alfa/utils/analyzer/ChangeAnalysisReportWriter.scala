package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.TextWriter

import schemarise.alfa.runtime.model.diff._

import java.nio.file.Path
import java.util
import java.util.stream.Collectors
import java.util.{Collections, Date}
import scala.collection.JavaConverters._

class ChangeAnalysisReportWriter(logger: ILogger, outputDir: Path) extends TextWriter(logger) {

  private val categoryIndex = new util.LinkedHashMap[ChangeCategoryType, ReportLine]()

  categoryIndex.put(ChangeCategoryType.BreakingApiChange, ReportLine("Breaking API Changes", "Red"))
  categoryIndex.put(ChangeCategoryType.PotentialApiChange, ReportLine("Potential API Breaking Changes", "Purple"))
  categoryIndex.put(ChangeCategoryType.ApiUpsert, ReportLine("Non-breaking API Changes", "Teal"))
  categoryIndex.put(ChangeCategoryType.BreakingDataStructureChange, ReportLine("Breaking Data Structure Changes", "OrangeRed"))
  categoryIndex.put(ChangeCategoryType.DataStructureUpsert, ReportLine("Non-breaking Data Structure Changes", "MediumSeaGreen"))
  categoryIndex.put(ChangeCategoryType.ModelMetadataChange, ReportLine("Metadata Changes", "MediumPurple"))
  categoryIndex.put(ChangeCategoryType.ImplementationLogicChange, ReportLine("Implementation Logic Changes", "RoyalBlue"))
  categoryIndex.put(ChangeCategoryType.QualityChanges, ReportLine("Data Quality Changes", "Goldenrod"))
  categoryIndex.put(ChangeCategoryType.IndirectBreakingDataStructureChange, ReportLine("Indirect impact from other changes", "Coral"))
  categoryIndex.put(ChangeCategoryType.DocumentationChanges, ReportLine("Documentation Changes", "CadetBlue"))


  def write(filename: String, v1: String, v2: String, mods: Modifications): Unit = {
    enterFile(filename + ".html", false)
    writeln(writeHeader())

    writeln(
      s"""
         |<body>
         |<h1>Impact Analysis Report</h1>
         |<p>Created: <span class="text-monospace">${new Date()}</span></p>
         |<h5>Between branch/tag version <span class="text-monospace text-secondary">$v1</span> and <span class="text-monospace text-secondary">$v2</span> </h5>
         |<br/>
      """.stripMargin)

    writeln(report(mods))

    writeln(
      """
        |</div>
        |</body>
        |</html>
      """.stripMargin)
    exitFile()

    logger.info(s"Wrote $filename.html")
  }

  case class ReportLine(descr: String, colour: String) {}

  def report(mods: Modifications) = {
    val totalChanges = if (mods.getResults.size() == 0) 0 else mods.getResults.values().asScala.map(es => es.size()).reduce((acc, e) => acc + e)

    val progressDivs = categoryIndex.entrySet().stream().map[String](e => {
      val res = mods.getResults.get(e.getKey)

      val bar = if (res != null) {
        val subTotal = res.size()
        val colour = e.getValue.colour
        val percent = Math.round((100 * subTotal.asInstanceOf[Double]) / totalChanges.asInstanceOf[Double])
        s"""    <div class="progress-bar" role="progressbar" style="width:${percent}%; background-color:${colour};" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100">${percent}%</div>
         """.stripMargin
      }
      else
        ""

      val sep =
        s"""    <div class="progress-bar" role="progressbar" style="width:1px; background-color:White;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
         """.stripMargin


      if (bar.nonEmpty)
        sep + bar
      else
        ""
    }).collect(Collectors.toList[String]).asScala.mkString("\n")

    val progressTable = categoryIndex.entrySet().stream().map[String](e => {
      var res = mods.getResults.get(e.getKey)
      if (res == null) res = Collections.emptyList()
      val subTotal = res.size()

      val percent = if (totalChanges == 0) 0 else Math.round((100 * subTotal.asInstanceOf[Double]) / totalChanges.asInstanceOf[Double])
      val bkg = if (subTotal > 0) e.getValue.colour else "White"

      val clz = if (subTotal == 0) """ class="text-secondary" """ else ""

      val descr =
        if (subTotal == 0)
          s"""<td $clz>${e.getValue.descr}</a></td> """
        else
          s"""<td $clz><a href="#${e.getKey}">${e.getValue.descr}</a></td> """

      s"""
         |    <tr>
         |      $descr
         |      <td $clz>${subTotal}</td>
         |      <td $clz>${percent}%</td>
         |      <td style="width:12px; background-color:${bkg};"></td>
         |    </tr>
       """.stripMargin
    }).collect(Collectors.toList[String]).asScala.mkString("\n")

    val details = changeDetails(mods)

    s"""
       |<h4>Change Analysis Summary</h4>
       |
       |  <div class="progress" style="height: 20px;">
       |$progressDivs
       |  </div>
       |
       |<br/>
       |
       |<table class="table table-hover table-responsive table-sm">
       |  <thead>
       |    <tr>
       |      <th scope="col">Change Type</th>
       |      <th scope="col">Total Changes</th>
       |      <th scope="col">Percentage</th>
       |      <th scope="col"></th>
       |    </tr>
       |  </thead>
       |  <tbody>
       |$progressTable
       |    <tr>
       |      <td></td>
       |      <td><b>${totalChanges}</b></td>
       |      <td></td>
       |      <td></td>
       |    </tr>
       |  </tbody>
       |</table>
       |
       |<hr/>
       |
       |<h4>Change Analysis Details</h4>
       |$details
     """.stripMargin
  }

  private def writeHeader() = {
    val bs = bootstampIncludes()

    s"""<!doctype html>
       |<html lang="en">
       |<head>
       |  <title>Impact Analysis Report</title>
       |$bs
       |</head>
       |<div class="container-fluid" style="font-size:90%">
    """.stripMargin
  }

  private def bootstampIncludes() = {
    """
      |  <meta charset="utf-8">
      |  <meta name="viewport" content="width=device-width, initial-scale=1">
      |
      |  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
      |  <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
      |  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz" crossorigin="anonymous"></script>
      |  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
      |  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
      |
      |  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.18/css/bootstrap-select.css" />
      |  <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.18/js/bootstrap-select.js"></script>
  """.stripMargin
  }
  def buildCode(m: IModification) = {
    val codeChanges =
      if (m.getBeforeSnippet.isPresent && m.getAfterSnippet.isPresent) {

        val l = m.getBeforeSnippet.get().getCode.trim
        val r = m.getAfterSnippet.get().getCode.trim

        if (l.size > 5000 || r.size > 5000) {
          "[Cannot preview - change larger than 5000 characters]"
        }
        else {
          val dr = new HtmlDiffReporter()
          val diffHtml = dr.process(l, r, diffHtmlTemplate)
          diffHtml
        }
      }
      else if (m.getBeforeSnippet.isPresent) {
        s"""
           |Deleted snippet:<br/>
           |<pre>${m.getBeforeSnippet.get().getCode}</pre><br>
         """.stripMargin
      }
      else if (m.getAfterSnippet.isPresent) {
        s"""
           |    <div class="card" style="background-color:HoneyDew;">
           |      <div class="card-body" style="padding:2px">
           |        <b>Added snippet:</b>
           |        <br/>
           |        <span class="text-monospace"><pre>${m.getAfterSnippet.get().getCode}</pre></span>
           |      </div>
           |    </div>
         """.stripMargin
      }
      else ""

    if (codeChanges.nonEmpty)
      s"""<div class="ml-3"><font style="font-size:90%">$codeChanges</font></div>"""
    else
      ""
  }

  private def changeInfo(getKey: ChangeCategoryType, title: String, mods: List[IModification]) = {

    val nsMods = mods.filter(_.isInstanceOf[NamespaceModifications]).map(_.asInstanceOf[NamespaceModifications])
    val udtMods = mods.filter(_.isInstanceOf[UdtModification]).map(_.asInstanceOf[UdtModification]).sortBy( e => e.getTargetUdt.getUdtName )
    val udtEntryMods = mods.filter(_.isInstanceOf[UdtEntryModification]).map(_.asInstanceOf[UdtEntryModification]).sortBy( e => e.getTargetUdt.getUdtName )

    val nsHtml = nsMods.map(m => {
      val msg = if (m.getMessage.isPresent) "<br/>" + msgInCard(m.getMessage.get()) else ""

      s"""
         |Namespace <code>${m.getNamespaceName}</code> ${m.getEditType.toString.toLowerCase}
         |$msg
         |${buildCode(m)}
       """.stripMargin
    })

    val udtHtml = udtMods.map(m => {
      val msg = if (m.getMessage.isPresent) "<br/>" + msgInCard(m.getMessage.get()) else ""

      s"""
         |${m.getTargetUdt.getUdtType.toString} <code>${m.getTargetUdt.getUdtName}</code> ${m.getEditType.toString.toLowerCase}
         |$msg
         |${buildCode(m)}
       """.stripMargin
    })

    val udtEntryHtml = udtEntryMods.map(m => {
      val msg = if (m.getMessage.isPresent) "<br/>" + msgInCard(m.getMessage.get()) else ""
      s"""
         |${m.getTargetUdt.getUdtType.toString} <code>${m.getTargetUdt.getUdtName}</code>
         |${m.getEntryType} <code>${m.getEntryName}</code> ${m.getEditType.toString.toLowerCase}
         |$msg
         |${buildCode(m)}
       """.stripMargin
    })

    val allChanges = nsHtml ++ udtHtml ++ udtEntryHtml
    val allHtml = allChanges.zipWithIndex.map(e => {
        s"""
           |<li class="list-group-item">
           |<b>${e._2 + 1}.</b> ${e._1}
         </li>""".stripMargin
      })
      .mkString(s"""<ul class="list-group">\n""", "<br/>\n", "\n</ul>")

    s"""
       |<button class="btn btn-light position-relative"  style="background-color:#f2f2f2">
       |  <a name="${getKey.value()}"><b>$title</b></a>
       |  <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-warning" >
       |    ${mods.size}
       |  </span>
       |</button>
       |<div class="card" style="background-color:#f2f2f2; border:0px; width: 90%;">
       |  <div class="card-body">
       |
       |$allHtml
       |
       |  </div>
       |</div>
       |
    """.stripMargin
  }

  def changeDetails(mods: Modifications): String = {
    categoryIndex.entrySet().stream().map[String](e => {
      if (mods.getResults.containsKey(e.getKey)) {
        s"""
           |${changeInfo(e.getKey, e.getValue.descr, mods.getResults.get(e.getKey).asScala.toList)}
           |<br/>
           |<br/>
      """.stripMargin
      }
      else
        ""
    }).collect(Collectors.toList[String]).asScala.mkString("\n")
  }

  private def msgInCard(m: String) = {
    s"""
       |    <div class="card ml-3" style="background-color:#ffffe6;">
       |      <div class="card-body"  style="padding:2px">
       |        $m
       |    </div>
       |  </div>
     """.stripMargin
  }

  override def outputDirectory: Path = outputDir


  val diffHtmlTemplate =
    """
      |<div class="row">
      |  <div class="col-sm-6">
      |    <div class="card" style="background-color:#ffe6e6;">
      |      <div class="card-body" style="padding:2px">
      |        <b>Before</b>
      |        <br/>
      |        <span class="text-monospace"><pre>${left}</pre></span>
      |      </div>
      |    </div>
      |  </div>
      |
      |  <div class="col-sm-6">
      |    <div class="card" style="background-color:HoneyDew;">
      |      <div class="card-body"  style="padding:2px">
      |        <b>After</b>
      |        <br/>
      |        <span class="text-monospace"><pre>${right}</pre></span>
      |      </div>
      |    </div>
      |  </div>
      |</div>
    """.stripMargin
}
