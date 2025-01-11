package com.schemarise.alfa.generators.exporters.changeanalysis

import com.schemarise.alfa.compiler.utils.{StdoutLogger, VFS}
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams}
import com.schemarise.alfa.runtime.Alfa
import com.schemarise.alfa.utils.analyzer.{ChangeAnalysisReportWriter, ChangeAnalyzer, GitInteractions}
import org.apache.commons.io.FileUtils

import java.nio.file.{Files, Path, Paths}

class ChangeAnalysisExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  private val BaseDir = "baseDir"
  private val StartVersion = "startVersion"
  private val EndVersion = "endVersion"

  override def exportSchema(): List[Path] = {
    val startVersion = param.exportConfig.get(StartVersion).toString
    val baseDir = param.exportConfig.get(BaseDir).toString
    val endVersion = param.exportConfig.getOrDefault(EndVersion, GitInteractions.LOCAL_CHECKOUT).toString

    if ( !Files.exists(Paths.get(baseDir))) {
      throw new RuntimeException(s"baseDir directory $baseDir does not exist")
    }
    val gi = new GitInteractions(param.logger, baseDir)

    val mod = gi.compareRepoBranches(startVersion, endVersion)

    val ca = new ChangeAnalyzer()
    val analysis = ca.analyzeVersions(mod)

    val r = new ChangeAnalysisReportWriter(logger, param.outputDir)

    val endVer = if ( endVersion == GitInteractions.LOCAL_CHECKOUT) gi.getWorkingBranchName else endVersion

    r.write("report", startVersion, endVer, analysis)

    val json = Alfa.jsonCodec().toFormattedJson(analysis)
    VFS.write( outputDirectory.resolve("report.json"), json)

    List.empty
  }

  override def supportedConfig(): Array[String] = {
    Array(StartVersion, EndVersion, BaseDir)
  }

  override def requiredConfig(): Array[String] = {
    Array(StartVersion, BaseDir)
  }

  override def name: String = "changeanalysis"
}
