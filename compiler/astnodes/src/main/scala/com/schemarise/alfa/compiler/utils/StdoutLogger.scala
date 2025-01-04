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

import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date

import com.schemarise.alfa.compiler.ast.model.IResolutionMessage
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._

class StdoutLogger(verbose: Boolean, trace: Boolean = false) extends ILogger {
  def this() {
    this(false)
  }

  override def debug(s: String): Unit =
    if (verbose)
      log(ansi().fg(BLUE).a("[DEBUG] " + s))

  override def info(s: String): Unit = log(ansi().fg(GREEN).a(" [INFO] " + s))

  override def vitalInfo(s: String): Unit = log(ansi().fg(GREEN).bold().a(" [INFO] " + s))

  override def trace(s: String): Unit = if (trace || StdoutLogger.traceEnabled) log(ansi().a("[TRACE] " + s))

  override def warn(s: String): Unit = log(ansi().fg(MAGENTA).a(" [WARN] " + s))

  override def error(s: String, e: Throwable): Unit = {
    error(s)
    error(stacktraceToString(e))
  }

  private val df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")

  private def log(m: Ansi) {
    flush
    Console.out.print(df.format(new Date()) + " ")
    Console.out.println(m.reset())
    flush
  }

  override def error(s: String) {
    flush
    Console.out.print(df.format(new Date()) + " ")
    Console.out.println(ansi().fg(RED).a("[ERROR] " + s).reset())
    flush
  }

  private def flush() = {
    Console.out.flush
  }

  def error(relativeTo: Path, errs: Seq[IResolutionMessage]): Unit = {
    flush
    formatAndLogMessages(relativeTo, errs, false)
    flush
  }

  override def isTraceEnabled: Boolean = verbose

  override def isDebugEnabled: Boolean = verbose
}

object StdoutLogger {
  protected var traceEnabled = false

  def trace(setting: Boolean) = traceEnabled = setting

  def trace = traceEnabled
}
