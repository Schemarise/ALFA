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
package com.schemarise.alfa.compiler.utils

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path}
import com.schemarise.alfa.compiler.ast.model.{IResolutionMessage, IToken}
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.AnsiConsole

import java.util
import java.util.Comparator
import scala.collection.JavaConverters._

object ILogger {
  val IsWindows = System.getProperty("os.name").toLowerCase().contains("win")

  def stdoutLogger = new StdoutLogger()

  def setupAnsiConsole = {
    if (IsWindows)
      AnsiConsole.systemInstall()
  }

  def stacktraceToString(e: Throwable): String = {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    sw.toString
  }

}

trait ILogger {
  def debug(s: String): Unit

  def trace(s: String): Unit

  def info(s: String): Unit

  def vitalInfo(s: String): Unit

  def warn(s: String): Unit

  def error(s: String): Unit

  def error(s: String, e: Throwable): Unit

  def isTraceEnabled: Boolean

  def isDebugEnabled: Boolean

  def stacktraceToString(e: Throwable): String = {
    ILogger.stacktraceToString(e)
  }

  private def toStringFileAndLine(relativeTo: Path, t: IToken, showUrl: Boolean): String = {
    val sb = new StringBuilder
    if (t.getSourcePath.isDefined) {

      val relFS = relativeTo.toAbsolutePath.getFileSystem
      val tokFS = t.getSourcePath.get.toAbsolutePath.getFileSystem

      val relPath = if (showUrl)
        "file://" + t.getSourcePath.get.toAbsolutePath
      else if (relFS.equals(tokFS))
        relativeTo.toAbsolutePath.relativize(t.getSourcePath.get.toAbsolutePath).toString
      else
        ""


      if (relPath.length == 0 && t.getSourcePath.isDefined)
        sb.append(t.getSourcePath.get)
      else
        sb.append(relPath)

      sb.append(ansi().a(":").bold().a(t.getStartLine).reset())
    }
    else {
      sb.append("line:" + t.getStartLine)
    }
    sb.toString()
  }

  def formatAndLogMessages(relativeTo: Path, errs: Seq[IResolutionMessage], isWarning: Boolean, showUrl: Boolean = false): Unit = {

    val javaErrs = new util.ArrayList(errs.toList.asJava)

    javaErrs.sort((s1: IResolutionMessage, s2: IResolutionMessage) => {
      val lineComp = s1.location.getStartLine.compareTo(s2.location.getStartLine)

      if (s1.location.getSourcePath.isDefined && s2.location.getSourcePath.isDefined) {
        val l = s1.location.getSourcePath.get
        val r = s2.location.getSourcePath.get

        val nameCompare = l.getFileName.toString.compareTo(r.getFileName.toString)

        if (nameCompare == 0)
          lineComp
        else
          nameCompare
      }
      else
        lineComp
    })

    var sorted = javaErrs.asScala

    sorted.foreach(e => {
      if (e.location.getSourcePath.isDefined && e.location.getStartLine == e.location.getEndLine) {
        val p = e.location.getSourcePath.get

        val contents = new String(Files.readAllBytes(p))

        val split = contents.split("\n").toList
        if (split.length > e.location.getStartLine) {
          val srcLine = split(e.location.getStartLine - 1)

          val leftSrc = srcLine.substring(0, Math.min(e.location.getStartColumn, srcLine.length))
          val errSrc = srcLine.substring(e.location.getStartColumn, Math.min(e.location.getEndColumn, srcLine.length))
          val rightSrc = srcLine.substring(Math.min(e.location.getEndColumn, srcLine.length))


          val preSrc = ansi().fg(GREEN).a("        [" + toStringFileAndLine(relativeTo, e.location, showUrl)).
            fg(GREEN).a("] ").reset().a(leftSrc)

          val envSrc = if (ILogger.IsWindows) preSrc.bold() else preSrc.a(Attribute.UNDERLINE)
          val src = envSrc.fg(RED).a(errSrc).reset().a(rightSrc)

          val msg = ansi().fg(MAGENTA).a(e.formattedMessage(relativeTo) + "\n" + src).toString

          if (isWarning)
            warn(msg)
          else
            error(msg)
        }
        else {
          val preMsg = ansi().fg(GREEN).a("[" + toStringFileAndLine(relativeTo, e.location, showUrl)).
            fg(GREEN).a("] ").reset()

          val msg = preMsg + ansi().fg(MAGENTA).a(e.formattedMessage).toString

          if (isWarning)
            warn(msg)
          else
            error(msg)
        }
      }
      else {
        val preMsg = ansi().fg(GREEN).a("[" + toStringFileAndLine(relativeTo, e.location, showUrl)).
          fg(GREEN).a("] ").reset()

        val msg = preMsg + ansi().fg(MAGENTA).a(e.formattedMessage(relativeTo)).toString
        //        val msg = e.formattedMessage(relativeTo)


        if (isWarning)
          warn(msg)
        else
          error(msg)
      }
    })
  }

}
