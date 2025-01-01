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

import java.nio.file.{FileSystem, Files, Path}
import java.security.MessageDigest
import java.util
import java.util.Base64

import com.schemarise.alfa.compiler.antlr.AlfaParser.CompilationUnitContext
import com.schemarise.alfa.compiler.antlr.{AlfaLexer, AlfaParser}
import com.schemarise.alfa.compiler.ast.ResolvableNode
import com.schemarise.alfa.compiler.ast.antlrvisitors.CompilationUnitVisitor
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.ast.nodes.{CompilationUnit, StringNode, UdtBaseNode}
import com.schemarise.alfa.compiler.err.{CompilerSettingError, ErrorCode, FileIncludeError, ResolutionMessage}
import com.schemarise.alfa.compiler.settings.AllSettings
import com.schemarise.alfa.compiler.tools.repo.{ArtifactEntry, IRepositoryManager, NoOpRepositoryManager}
import com.schemarise.alfa.compiler.utils._
import com.schemarise.alfa.compiler.utils.antlr.{CustomAntlrErrorListener, CustomAntlrInputStream}
import org.antlr.v4.runtime._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Context(repoMan: IRepositoryManager,
              fileSystem: Option[FileSystem],
              val allSettings: AllSettings, val logger: ILogger) {

  private val scriptMd5s = new ListBuffer[Array[Byte]]()

  private val errors: mutable.Set[ResolutionMessage] = mutable.Set.empty
  private val warnings: mutable.Set[ResolutionMessage] = mutable.Set.empty

  // Which repo is being accessed currently
  private val repositoryContextStack = new util.Stack[ArtifactEntry]()
  private val dependencyMgr = repoMan.createDependencyManager(allSettings, this)

  private val processedIncludes: mutable.Set[Path] = mutable.Set.empty

  private var newestFileTime: Long = 0

  loadDeclarationsFromDependencies()

  def this() {
    this(new NoOpRepositoryManager(), None, new AllSettings(), new StdoutLogger())
  }

  def this(logger: ILogger) {
    this(new NoOpRepositoryManager(), None, new AllSettings(), logger)
  }

  def latestModifiedFileTime(): Long = newestFileTime


  def readIncludeScript(parentScriptPath: Option[Path], includeStmt: StringNode): Option[CompilationUnit] = {
    if (allSettings.compile.DisallowInclude) {
      addResolutionError(new ResolutionMessage(includeStmt.location,
        CompilerSettingError)(None, List.empty, includeStmt.toString +
        " used, but include is disabled in Compiler Settings"))
      None
    }
    else if (fileSystem.isEmpty) {
      addResolutionError(new ResolutionMessage(includeStmt.location,
        CompilerSettingError)(None, List.empty, includeStmt.toString +
        " used, no file system to load from"))
      None
    }
    else {
      val d = fileSystem.get.getPath(includeStmt.text)

      val scriptPath = if (d.isAbsolute) {
        d
      }
      else if (parentScriptPath.isDefined) {
        val parentDir = parentScriptPath.get.getParent
        parentDir.resolve(includeStmt.text)
      }
      else {
        logger.warn("Attempting include from filesystem as parent script path is not defined " + includeStmt.text)
        d
      }

      if (!Files.exists(scriptPath)) {
        addResolutionError(new ResolutionMessage(includeStmt.location,
          FileIncludeError)(None, List.empty, includeStmt.text, scriptPath.toAbsolutePath.toString))
        None
      }
      else if (processedIncludes.contains(scriptPath)) {
        logger.debug("Ignoring already included file " + scriptPath)
        None
      }
      else {
        processedIncludes += scriptPath
        Some(readScript(Some(scriptPath), new String(Files.readAllBytes(scriptPath))))
      }
    }
  }

  def getErrors(): Seq[ResolutionMessage] = errors.toSeq

  def getWarnings(): Seq[ResolutionMessage] = warnings.toSeq

  lazy val registry: TypeRegistry = new TypeRegistry(this)

  def addResolutionError(target: ResolvableNode, errorCode: ErrorCode, args: Any*): Unit = {
    addResolutionError(ResolutionMessage(target, errorCode)(List.empty, args: _*))
  }

  def addResolutionWarning(target: ResolvableNode, errorCode: ErrorCode, args: Any*): Unit = {
    addResolutionWarning(ResolutionMessage(target, errorCode)(List.empty, args: _*))
  }

  def addResolutionError(target: ResolvableNode, errorCode: ErrorCode, completions: List[String], args: Any*): Unit = {
    addResolutionError(ResolutionMessage(target, errorCode)(completions, args: _*))
  }

  //  def addResolutionWarning(target: ResolvableNode, errorCode: ErrorCode, args: Any*) : Unit = {
  //    addResolutionWarning(ResolutionMessage(target, errorCode)(args:_*))
  //  }

  def addResolutionError(target: ResolvableNode, errorCode: ErrorCode, location: IToken, args: Any*): Unit = {
    addResolutionError(ResolutionMessage(target, errorCode, location)(List.empty, args: _*))
  }

  def addResolutionError(loc: IToken, errorCode: ErrorCode, args: Any*): Unit = {
    addResolutionError(ResolutionMessage(loc, errorCode)(None, List.empty, args: _*))
  }

  def addResolutionWarning(loc: IToken, errorCode: ErrorCode, args: Any*): Unit = {
    if (!shouldIgnoreWarnings)
      addResolutionWarning(ResolutionMessage(loc, errorCode)(None, List.empty, args: _*))
  }

  def addResolutionError(message: Option[ResolutionMessage]): Unit = {
    if (message.isDefined)
      addResolutionError(message.get)
  }

  def addResolutionError(message: ResolutionMessage): Unit = {
    if (!errors.contains(message)) {
      errors.+=(message)
    }
  }

  def shouldIgnoreWarnings = allSettings.compile.IgnoreWarnings

  def addResolutionWarning(message: ResolutionMessage): Unit = {
    if (!warnings.contains(message) && !shouldIgnoreWarnings) {
      warnings.+=(message)
    }
  }

  def getScriptsMD5() = {
    val digest = MessageDigest.getInstance("md5");
    digest.update(scriptMd5s.flatten.toArray)
    val combined = digest.digest()
    val str = Base64.getEncoder.encodeToString(combined)

    val safe = str.map(ch => if (Character.isJavaIdentifierPart(ch)) ch else '_')
    safe
  }


  def readScript(fromPath: Option[Path], script: String): CompilationUnit = {
    val digest = MessageDigest.getInstance("md5");
    digest.update(script.getBytes("UTF-8"));
    val x = digest.digest()
    scriptMd5s += x

    if (fromPath.isDefined) {
      val fileMilli = Files.getLastModifiedTime(fromPath.get).toMillis

      if (fileMilli > newestFileTime)
        newestFileTime = fileMilli
    }

    val is = new CustomAntlrInputStream(fromPath, script)
    val lexer = new AlfaLexer(is)
    val tokens = new CommonTokenStream(lexer)

    val parser = new AlfaParser(tokens)

    parser.removeErrorListeners()
    lexer.removeErrorListeners()

    val errLis = new CustomAntlrErrorListener(this, is)

    lexer.addErrorListener(errLis)
    parser.addErrorListener(errLis)

    // DEBUGGING
    //    parser.addErrorListener(new DiagnosticErrorListener() {
    //      override def reportAttemptingFullContext(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int,
    //                                               conflictingAlts: util.BitSet, configs: ATNConfigSet): Unit = {
    //        val format = "reportAttemptingFullContext d=%s, input='%s'";
    //        val decision = this.getDecisionDescription(recognizer, dfa);
    //        val ds = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
    //        val message1 = java.lang.String.format(format, decision, ds);
    //
    //        val alts = this.getConflictingAlts(conflictingAlts, configs)
    //        val text = recognizer.getTokenStream.getText(Interval.of(startIndex, stopIndex))
    //        val message2 = String.format(format, decision, alts, text)
    //
    //        super.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs)
    //      }
    //    })

    val pcu: CompilationUnitContext = parser.compilationUnit

    val hasParserError = errors.size > 0

    val listener = new CompilationUnitVisitor(this, hasParserError)
    listener.visitCompilationUnit(pcu)
  }

  def currentRepositoryEntry: Option[ArtifactEntry] =
    if (repositoryContextStack.isEmpty)
      None
    else
      Some(repositoryContextStack.peek())

  private def lookupUdtInDependencies(requestor: Option[ResolvableNode],
                                      ref: UdtDataType, logError: Boolean): Option[UdtBaseNode] = {
    val defn: Option[(ArtifactEntry, String)] = dependencyMgr.getExternalDeclAsString(this, ref)

    if (defn.isDefined) {
      repositoryContextStack.push(defn.get._1)
      val cu = readScript(Some(defn.get._1.pathInArtifact), defn.get._2)
      repositoryContextStack.pop

      if (!cu.hasErrors)
        if (cu.namespaces.headOption.isDefined)
          cu.namespaces.head.udts.headOption
        else
          None
      else
        None
    }
    else
      None
  }

  private def loadDeclarationsFromDependencies(): Unit = {
    val allDecls = dependencyMgr.allDeclarations

    if (allDecls.size > 0) {
      logger.debug("Start reading definitions from dependencies")

      allDecls.foreach(d => {
        val r = new UdtDataType(name = StringNode.create(d))
        lookupUdtInDependencies(None, r, false)
      })

      logger.debug("Completed reading definitions from dependencies")
    }
  }
}
