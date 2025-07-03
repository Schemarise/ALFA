package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.err.CompilerSettingsException
import com.schemarise.alfa.compiler.tools.AllSettingsFactory
import com.schemarise.alfa.compiler.tools.repo.{FileRepositoryMgr, RepositoryManagerFactory}
import com.schemarise.alfa.compiler.utils.{ILogger, StdoutLogger, VFS}
import com.schemarise.alfa.compiler.{AlfaCompiler, CompilationUnitArtifact}
import com.schemarise.alfa.generators.common.AlfaPath
import com.schemarise.alfa.runtime.Logger
import org.rogach.scallop._
import org.rogach.scallop.exceptions.ScallopException

import java.io.FileReader
import java.nio.file._
import java.util.Properties
import scala.collection.JavaConverters._

final class AlfaCli(compileFlag: Boolean = false,
                    installFlag: Boolean = false,
                    exporters: Option[String] = None,
                    importers: Option[String] = None,
                    path: Option[Path] = None,
                    modulePaths: Option[List[Path]] = None,
                    outputDir: Option[Path] = None,
                    filterTypes: Option[List[String]] = None,
                    verbose: Option[Boolean] = None,
                    settings: Map[String, String] = Map.empty,
                    exitOnError: Boolean = false,
                    val datafiles: Option[Path] = None,
                    testFlag: Boolean = false,
                    _logger : Option[ILogger] = None
                   ) {

  private val logger = if ( _logger.isDefined ) _logger.get else new StdoutLogger(verbose.isDefined && verbose.get)

  logger.debug(s"AlfaCli parameters - exporters:${exporters.getOrElse("")} importers:${importers.getOrElse("")} " +
    s"path:${path.getOrElse(Paths.get("empty")).toAbsolutePath} outputDir:${outputDir.getOrElse(Paths.get("empty"))} settings:$settings")

  def run() = {
    if (hasValidationError()) {
      cliEarlyExit()
    }

    if (importers.isDefined && exporters.isDefined) {
      logger.error("Both import schema and export cant be used in the same step. Run them as separate steps.")
      cliEarlyExit()
    }

    if ((importers.isDefined || exporters.isDefined) && outputDir.isEmpty) {
      logger.error("outputDir required if importer or exporter specified")
      cliEarlyExit()
    }

    if (importers.isDefined) {
      runImporters(importers.get, path.get, outputDir.get, settings)
      logger.info(s"Importer ${importers.get} completed. Output written to ${outputDir.get.toAbsolutePath}.")
    }
    else {

      val _cua = compile(path.get, modulePaths)

      val filtered = CompilationUnitArtifact.filtered(_cua, filterTypes)

      if (exporters.isDefined) {
        val exps = exporters.get.split(",")
        exps.foreach(e => {
          val op =
            if (exps.size == 1)
              outputDir.get
            else
              outputDir.get.resolve(e)

          runExporters(e, op, filtered, settings)
          logger.info(s"Exporter ${e} completed. Output written to ${outputDir.get.toAbsolutePath}.")
        })
      }

      // create runtime logger based on delegated settings
      Logger.getOrCreateDefault(logger.isDebugEnabled, logger.isTraceEnabled)

      if (installFlag) {
        try {
          val archive = filtered.asInstanceOf[CompilationUnitArtifact].writeAsZipModule(logger, modulePaths.get.head, true)
          logger.info("Wrote module " + archive)
        } catch {
          case cse: CompilerSettingsException =>
            logger.error(cse.getMessage)
            cliEarlyExit()
          case x: Throwable =>
            logger.error("Unknown error " + x.getMessage, x)
            cliEarlyExit()
        }
      }
    }
  }

  private def compile(p: Path, m: Option[List[Path]]): ICompilationUnitArtifact = {

    val rm = if (m.isEmpty) {
      val rmf = new RepositoryManagerFactory(logger, "file://" + p.toAbsolutePath.toString, logger)
      rmf.getRepoMgrAndSettings()
    }
    else {
      val rmf = new FileRepositoryMgr(logger, m)
      (rmf, AllSettingsFactory.empty)
    }

    val sc = new AlfaCompiler(logger, rm._1)
    val cua = sc.compile(p, rm._2)

    if (cua.hasErrors) {
      logger.formatAndLogMessages(p, cua.getErrors, false)
      cliEarlyExit
    }

    if (cua.hasWarnings) {
      logger.formatAndLogMessages(p, cua.getWarnings, true)
    }

    cua
  }

  def cliEarlyExit() = {
    if (exitOnError) {
      System.exit(1)
    }
    else
      throw new AlfaExitException("Exiting ALFA CLI")
  }

  private def runExporters(g: String, o: Path, cua: ICompilationUnitArtifact, settings: Map[String, String]): Unit = {

    //    val es = cua.asInstanceOf[CompilationUnitArtifact].settings.exports
    //
    //    val cfg = es.getGeneratorSettings(g)


    val instance = new Exporters(logger).exporterInstance(g, o, cua, settings)

    if (instance.isEmpty) {
      logger.error("Failed to find exporter for " + g)
      cliEarlyExit()
    }
    val exportedToPath = instance.get.exportSchema()
  }

  private def runImporters(g: String, i: Path, o: Path, settings: Map[String, String]): Unit = {

    val instance = new Importers(logger).importerInstance(logger, g, i, o, settings)

    if (instance.isEmpty) {
      logger.error("Failed to find importer for " + g)
      cliEarlyExit()
    }
    instance.get.importSchema()

    logger.info(s"Importer $g completed")
  }

  private def hasValidationError(): Boolean = {

    var failed = false

    if (!compileFlag && !installFlag && !testFlag && !exporters.isDefined && !importers.isDefined) {
      logger.error("One of compile, test, exporters, importers or deploy commands expected")
      failed = true
    }

    else if (!path.isDefined) {
      logger.error("path is mandatory")
      failed = true
    }

    else if (!path.get.toString.toLowerCase().startsWith("http") && Files.notExists(path.get)) {
      logger.error("path supplied does not exist - " + path.get)
      failed = true
    }

    else if (!failed && installFlag && !modulePaths.isDefined) {
      logger.error("modules parameter required for install command")
      failed = true
    }

    else if (!failed && exporters.isDefined && !outputDir.isDefined) {
      logger.error("output parameter required when generate specified")
      failed = true
    }

    failed
  }
}

class AlfaExitException(msg : String) extends Exception(msg) {}

/**
 * Alfa CLI
 *
 * < path > can be
 * a) A path to a file with .alfa-proj.yaml or .alfa-proj.json extension
 * b) A path to a directory or sub-directories containing *.s files.
 * If a single *.alfa-proj.yaml or *.alfa-proj.json file exists in the path, it will be used as the settings.
 * c) A *.alfa-proj.zip file containing a) or b)
 *
 *
 * alfac [ -m:odule modulesPath ] -c:compile  < -p:path path >
 * alfac < -m:odule modulesPath > -i:install  < -p:path path >  < -t:types types >
 * alfac [ -m:odule modulesPath ] -g:generate [avro|protobuf|cassandra|docs]  < -t:types types > < -p:path path > < -o:utputDir dir >
 */
object AlfaCli {
  val logger = new StdoutLogger

  def main(args: Array[String]): Unit = {
    System.setProperty("java.awt.headless", "true")

    ILogger.setupAnsiConsole

    if (System.getSecurityManager == null)
      logger.trace("No security manager used")

    if (args.filter(a => a.equals("-v") || a.equals("--verbose")).length > 0)
      println("Arguments - " + args.mkString("[", "|", "]"))

    parseAndRun(args, None)
  }

  def parseAndRun(args: Array[String], loggerParam : ILogger): Unit = {
    parseAndRun(args, Some(loggerParam))
  }

  def parseAndRun(args: Array[String], loggerParam : Option[ILogger]): Unit = {
    val opts = new ScallopConf(args) {
      banner(
        s"""
ALFA Command Line Interface ${AlfaCompiler.getVersion}

For usage see below:
    """)

      val compile = opt[Boolean]("compile", descr = "Compiles files in the given arg path")
      val test = opt[Boolean]("test", descr = "Runs tests defined in the models")
      val deploy = opt[Boolean]("deploy", descr = "Compiles files in the given arg path, and deploy")

      val modules = opt[List[String]]("modules", descr = "A path to load dependencies from or to install projects as a packages")

      val types = opt[List[String]]("types", descr = "Type for DQ checks or restrict the output to this list of types and their derived/dependant/reachable definitions")

      val verbose = opt[Boolean]("verbose", descr = "Display verbose messages from compiler")

      private val verboseSet = args.contains("-v") || args.contains("--verbose")
      val exporters = opt[String]("exporters",
        descr = s"""Optional code generator to use. Multiple comma delimited generators can be specified ${if (verboseSet) new Exporters(logger).exporters.toSeq.sorted.mkString("[", ", ", "]") else ""} """)

      val importers = opt[String]("importers",
        descr = s"""Importer to use for generating ALFA model ${if (verboseSet) new Importers(logger).importers.toSeq.sorted.mkString("[", ", ", "]") else ""} """)

      val settings = opt[String]("settings", descr = "Settings specific to the generator (e.g. skip-assert-all=true;skip-unknown-fields=true;exclude-asserts=ExtValidate )")
      val settingsFile = opt[Path]("settingsFile", descr = "Path to settings configuration file")

      val datafiles = opt[Path]("datafiles", descr = "Path to data file for Data Quality checks")
      val output = opt[Path]("output", descr = "Path to write generated output, optional unless generator specified")

      val path = trailArg[String]()

      override def onError(e: Throwable) = e match {
        case ScallopException(message) =>
          logger.error(message)
          printHelp
          System.exit(1)
        case ex => super.onError(ex)
      }
    }

    opts.verify()

    val c = opts.compile.toOption
    val v = opts.verbose.toOption
    val d = opts.deploy.toOption
    val i = opts.importers.toOption
    val e = opts.exporters.toOption
    val tst = opts.test.toOption.isDefined && opts.test.toOption.get

    val p = opts.path.toOption
    val _m = opts.modules.toOption
    val s = opts.settings.toOption
    val sf = opts.settingsFile.toOption

    val datafiles = opts.datafiles.toOption

    val settings: Map[String, String] = if (s.isDefined) {
        s.get.split(";").map(a => {
          val x = a.split("=")
          x.head -> x.last
        }
        ).toMap
      }
      else if (sf.isDefined) {
          val p = new Properties()
          try {
            p.load( new FileReader(sf.get.toFile) )
          }
          catch {
            case excp:Exception =>
              throw new AlfaExitException("Failed to load settingsFile. Expected format is a line per setting of key=value. " + excp.getMessage)
          }
          p.asScala.toMap
      }
      else {
        Map.empty
      }

    val m = if (_m.isDefined)
      Some(_m.get.map(e => Paths.get(e)))
    else
      None

    val t = opts.types.toOption

    val o = opts.output.toOption


    val ppath = if (p.isDefined) Some(AlfaPath.get(p.get)) else None

    val smt = new AlfaCli(c.getOrElse(false), d.getOrElse(false), e, i, ppath, m, o, t, v, settings, true, datafiles, tst, loggerParam)

    if (smt.hasValidationError()) {
      opts.printHelp()
      smt.cliEarlyExit
    }
    else
      smt.run
  }
}



