/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler

import com.schemarise.alfa.compiler.ast.model.IAnnotation.{AnnotationGenSkip, Annotation_Deprecated, Annotation_IgnoreServiceWarnings}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.err.{InternalErrorMsg, MultipleProjectConfigurationsFound, ResolutionMessage}
import com.schemarise.alfa.compiler.settings.AllSettings
import com.schemarise.alfa.compiler.tools.repo.{FileRepositoryMgr, IRepositoryManager}
import com.schemarise.alfa.compiler.tools.{AlfaPath, AllSettingsFactory}
import com.schemarise.alfa.compiler.utils._

import java.io.{PrintWriter, StringWriter}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.JavaConverters._

final object AlfaCompiler {

  private val dollar = "$"

  val builtinAnnotations =
    s"""
       |@${AnnotationGenSkip}
       |annotation ${Annotation_Deprecated} ( field, record, trait, entity, union, trait, key, method, library, service ) { }
       |
       |# types of db persistence
       |enum alfa.db.StorageMode
       |{
       |    Relational ## only fully flattened
       |    Composite  ## modern relations with struct columns, arrays etc
       |    JSON       ## store as json
       |    JSONB      ## json binary store
       |}
       |
       |# Metadata for persisting entities
       |annotation ${IAnnotation.Meta_DB_Table}( entity, record, namespace ) {
       |    # Underlying database table name override
       |    Name : string = ""
       |
       |    # Underlying database schema name
       |    Schema : string = ""
       |    PayloadColumnName : string = "__Payload"
       |
       |    # Schema layout approach
       |    StorageMode : alfa.db.StorageMode = alfa.db.StorageMode.Composite
       |
       |    # Hint which fields are queryable, and possibly stored outside the JSON(B)
       |    Queryable : set< ${dollar}fieldName > = { }
       |
       |    # Indexes by set of fields
       |    Indexes : map< name: string, cols: set< ${dollar}fieldName > > = {}
       |
       |    PartitionFields : set< ${dollar}fieldName > = {}
       |    ClusterFields : set< ${dollar}fieldName > = {}
       |    PartitionExpression : string = ""
       |    Options : set< string > = {}
       |}
       |
       |annotation ${IAnnotation.Meta_DB_Column}( field, keyfield ) {
       |    # Underlying database column name override
       |    Name : string = ""
       |}
       |
       |annotation ${IAnnotation.Meta_Field_Annotations} ( record, trait, entity, union, trait, key ) {
       |    Tags : map< list< ${dollar}annotation >, list< ${dollar}fieldName > >
       |}
       |
       |annotation ${AnnotationGenSkip}( service, annotation ) { }
       |
       |annotation ${Annotation_IgnoreServiceWarnings}( service ) { }
       |
       |annotation ${IAnnotation.Annotation_Parse_SkipUnknownFields}( record, entity, trait, union, key, tuple ) { }
       |
       |annotation ${IAnnotation.Annotation_Http_Get}( method ) { }
       |annotation ${IAnnotation.Annotation_Http_Put}( method ) { }
       |annotation ${IAnnotation.Annotation_Http_Post}( method ) { }
       |annotation ${IAnnotation.Annotation_Http_Delete}( method ) { }
       |
      """.stripMargin

  val testService =
    """
      |// This service is passed into methods that are part of a 'test' user-defined-type. Number of method are available to
      |// formula data required for a test, and execute functionality and assert expected results.
      |@alfa.gen.Skip
      |@alfa.lang.IgnoreServiceWarnings
      |service schemarise.alfa.test.Scenario() {
      |    // The specified entity object is available within the test to be accessed via the 'query' or 'lookup' functions.
      |    given( description : string, data : $entity ) : void
      |
      |    // Load JSON files from the path specified (file, or directory), and those entity objects are available within the
      |    // test to be accessed via the 'query' or 'lookup' functions.
      |    // givenAll( description : string, path : string ) : void
      |
      |    // The specified service is available in the context of this Scenario as a mocked service.
      |    // When invoked, returns a random value from the results list.
      |    withServiceResults( description : string, srv : $service, results : map< FunctionName : string, ReturnValues : list< string > > ) : void
      |
      |    // When the testBody is executed, no error is expected and completes normally
      |    succeeds( description : string, testBody : func<(), void > ) : boolean
      |
      |    // When the testBody is executed, an error is expected.
      |    // This method should be used when the exact error message expected can be implementation specific (E.g.Java, Python etc)
      |    fails( description : string, testBody : func<(), void > ) : boolean
      |
      |    // When the testBody is executed, an error is expected on the field specified by the 'errorFieldPath' field
      |    failsOn( description : string, testBody : func<(), void >, expectedErrorFieldPath : string ) : boolean
      |
      |    // When the testBody is executed, an error is expected with the message being reported
      |    failsWith( description : string, testBody : func<(), void >, expectedErrorMessage : string ) : boolean
      |
      |    // Assert the lambda returns true
      |    assertTrue( description : string, testBody : func<(), boolean > ) : void
      |
      |    // Create a random object of the given type
      |    random( typeName : $udtName ) : $udt
      |
      |    loadObjectFromCsv( typeName : $udtName, pathOrUrl : string ) : $udt
      |    loadObjectsFromCsv( typeName : $udtName, pathOrUrl : string, headerLineNo : int, delimiter : string ) : list< $udt >
      |
      |    loadObjectFromJSON( typeName : $udtName, pathOrUrl : string ) : $udt
      |
      |    listFiles( pathOrUrl : string, filePattern : string ) : list< string >
      |
      |    // Create a random object of the given type with give values
      |    randomWith( builderObject : $udt ) : $udt
      |
      |    // Create copy with overriding from 2nd arg
      |    copyWith( toCopy : $udt, toOverride : $udt ) : $udt
      |
      |}
    """.stripMargin

  def getVersion = {
    val loc = classOf[AlfaCompiler].getProtectionDomain.getCodeSource.getLocation.toString
    if (loc.endsWith(".jar"))
      " v" + loc.split("/").last.replace(".jar", "").split("-").last
    else
      ""
  }
}

class AlfaCompiler(val logger: ILogger, repoMgr: IRepositoryManager) {

  def this(logger: ILogger, modulesPath: List[Path]) {
    this(logger, new FileRepositoryMgr(logger, Some(modulesPath)))
  }

  def this(logger: ILogger = new StdoutLogger) {
    this(logger, List.empty)
  }

  private[alfa] val repoManager = repoMgr


  def compile(script: String): ICompilationUnitArtifact =
    _compile(None, None, script, AllSettingsFactory.empty)

  def compile(path: Path, script: String): ICompilationUnitArtifact =
    _compile(Some(path.getFileSystem), Some(path), script, AllSettingsFactory.empty)

  def compile(fs: FileSystem, script: String): ICompilationUnitArtifact =
    _compile(Some(fs), None, script, AllSettingsFactory.empty)

  def compile(script: String, settings: AllSettings): ICompilationUnitArtifact =
    _compile(None, None, script, settings)

  def compile(path: Path, script: String, settings: AllSettings): ICompilationUnitArtifact =
    _compile(None, Some(path), script, settings)

  def compile(scriptOrDirFile: Path): ICompilationUnitArtifact =
    compile(scriptOrDirFile, AllSettingsFactory.empty)

  private def generateIncludeScript(dir: Path): String = {
    val sb = new StringBuilder

    Files.walkFileTree(dir, new SimpleFileVisitor[Path]() {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val fn = dir.getFileName

        if ((fn != null && fn.toString.equals(".")) || (fn != null && !fn.toString.startsWith(".")) || (fn != null && fn.toString.equals("..")))
          super.preVisitDirectory(dir, attrs)
        else {
          logger.debug("Skipping hidden directory from files to compile " + dir)
          FileVisitResult.SKIP_SUBTREE
        }
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.getFileName.toString.endsWith(".alfa")) {
          val scr = file.normalize.toAbsolutePath.toString
          // account for windows returning c:\... which will fail parse when \ is considered escape
          val path = if (file.getFileSystem.getSeparator.equals("\\"))
            scr.replace('\\', '/')
          else
            scr
          sb.append(s"""include "${path}"\n""")
        }

        FileVisitResult.CONTINUE
      }
    })

    if (sb.size == 0)
      logger.warn("No Alfa files found in " + dir)
    else
      logger.debug("Compiling generated file:\n" + sb.toString)

    sb.toString
  }

  def compile(scriptOrDirPath: Path, _settings: AllSettings, withContext: Option[Context] = None): ICompilationUnitArtifact = {
    if (Files.isDirectory(scriptOrDirPath)) {
      val settingsFiles = Files.list(scriptOrDirPath).
        filter(f => !Files.isDirectory(f) && AlfaPath.isAlfaProjectFile(f.getFileName)).
        iterator().asScala.toList

      var err: Option[ResolutionMessage] = None

      val settings = if (_settings == AllSettingsFactory.empty) {
        settingsFiles.size match {
          case 0 => _settings
          case 1 => AllSettingsFactory.fromJsonOrYamlFilePath(settingsFiles.headOption.get)
          case _ => {
            err = Some(ResolutionMessage(TokenImpl.empty, MultipleProjectConfigurationsFound)(None, List.empty, settingsFiles.mkString("", ",", "")))
            AllSettingsFactory.empty
          }
        }
      }
      else _settings

      val genScipt = generateIncludeScript(scriptOrDirPath)
      val cua = _compile(Some(scriptOrDirPath.getFileSystem), None, genScipt, settings, withContext)

      if (cua.isInstanceOf[CompilationUnitArtifact])
        cua.asInstanceOf[CompilationUnitArtifact].context.addResolutionError(err)

      cua
    }

    else if (AlfaPath.isAlfaProjectFile(scriptOrDirPath.getFileName)) {
      val settings = AllSettingsFactory.fromJsonOrYamlFilePath(scriptOrDirPath)
      compile(scriptOrDirPath.toAbsolutePath.getParent, settings)
    }

    else if (scriptOrDirPath.getFileName.toString.endsWith(AlfaPath.ZipFileExtension)) {
      val fs = VFS.createAndCopyFrom(scriptOrDirPath)
      compile(fs.getPath("/"))
    }

    else if (scriptOrDirPath.getFileName.toString.endsWith(".zip")) {
      val fs = VFS.createFromZIP(scriptOrDirPath)
      compile(fs.getPath("/"))
    }

    else if (scriptOrDirPath.getFileName.toString.endsWith(AlfaPath.SourceFileExtension))
      _compile(Some(scriptOrDirPath.getFileSystem), Some(scriptOrDirPath), new String(Files.readAllBytes(scriptOrDirPath)), _settings)

    else if (!Files.exists(scriptOrDirPath))
      throw new AlfaSettingsException("Path does not exist - " + scriptOrDirPath.toAbsolutePath)

    else
      throw new AlfaSettingsException("File with unrecognized extension supplied - " + scriptOrDirPath.toAbsolutePath)
  }

  class ErrCompilationUnitArtifact(e: Throwable, errType: String, ctx: Context) extends DefaultCompilationUnitArtifact {
    val msg = errType + " : " + e.getMessage
    logger.error(msg)

    val stacktrace = logger.stacktraceToString(e)
    logger.trace(stacktrace)

    private val errs = ctx.getErrors() ++ Seq(new ResolutionMessage(TokenImpl.empty, InternalErrorMsg)(None, List.empty, msg))

    override def hasErrors: Boolean = true

    override def getErrors: Seq[IResolutionMessage] = errs
  }

  private def _compile(fileSystem: Option[FileSystem], scriptPath: Option[Path],
                       script: String, settings: AllSettings,
                       withContext: Option[Context] = None): ICompilationUnitArtifact = {


    val ctx = if (withContext.isEmpty) {
      new Context(repoManager, fileSystem, settings, logger)
    } else {
      withContext.get
    }

    try {
      val cua = __compile(fileSystem, scriptPath, script, settings, ctx)
      cua
    } catch {
      case e: AlfaSettingsException =>
        new ErrCompilationUnitArtifact(e, "Error in settings", ctx)

      case e: AlfaInternalException =>
        new ErrCompilationUnitArtifact(e, "Internal error", ctx)

      case e: Throwable =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))

        new ErrCompilationUnitArtifact(e, "Fatal internal error.\n" + sw.toString, ctx)
    }

  }

  private def __compile(fileSystem: Option[FileSystem], scriptPath: Option[Path],
                        script: String, settings: AllSettings, ctx: Context): ICompilationUnitArtifact = {

    val st = System.currentTimeMillis
    loadBootstrapScripts(ctx)

    val cu = ctx.readScript(scriptPath, script)

    // resolve/compile all dependencies
    repoManager.getArtifacts.forEach(a => {
      a.fileIndex.foreach(fi => {
        val udt = ctx.registry.getUdt(None, UdtDataType.fromName(fi), false)
        if (udt.isDefined) {
          udt.get.startPreResolve(ctx, cu)
          udt.get.startResolve(ctx)
          udt.get.startPostResolve(ctx)
        }
      })
    })

    val elapsed = System.currentTimeMillis - st
    val cua = new CompilationUnitArtifact(ctx, cu)

    val e: String = cua.getErrors.size match {
      case 0 => ""
      case 1 => s" ${cua.getErrors.size} error"
      case _ => s" ${cua.getErrors.size} errors"
    }

    val w: String = cua.getWarnings.size match {
      case 0 => ""
      case 1 => s" ${cua.getWarnings.size} warning"
      case _ => s" ${cua.getWarnings.size} warnings"
    }

    val m = if (e.size > 0 || w.size > 0) "( found" + e + w + " )" else ""

    logger.vitalInfo(s"ALFA Compiler${AlfaCompiler.getVersion} completed in " + elapsed + "ms " + m)
    cua
  }

  private def loadBootstrapScripts(ctx: Context) = {
    val cu = ctx.readScript(None, AlfaCompiler.builtinAnnotations + AlfaCompiler.testService)

    new CompilationUnitArtifact(ctx, cu)
    if (cu.hasErrors)
      throw new com.schemarise.alfa.compiler.AlfaInternalException("Internal Error " + cu.getErrors)
  }

}

