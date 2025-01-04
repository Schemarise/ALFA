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

@deprecated
class Logger(val isTraceEnabled: Boolean, val isDebugEnabled: Boolean) extends ILogger {
  private val logger = new ThreadLocal[ILogger]

  def debug(s: String): Unit = logger.get.debug(s)

  def info(s: String): Unit = logger.get.info(s)

  def trace(s: String): Unit = logger.get.trace(s)

  def error(s: String): Unit = logger.get.error(s)

  def error(s: String, e: Throwable): Unit = logger.get.error(s, e)

  def warn(s: String): Unit = logger.get.warn(s)

  def current: ILogger = logger.get

  override def vitalInfo(s: String): Unit = logger.get.vitalInfo(s)
}

