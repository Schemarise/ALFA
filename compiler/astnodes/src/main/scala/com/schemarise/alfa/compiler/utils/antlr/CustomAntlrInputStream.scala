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
package com.schemarise.alfa.compiler.utils.antlr

import java.nio.file.Path

import org.antlr.v4.runtime.ANTLRInputStream

class CustomAntlrInputStream(val srcPath: Option[Path], script: String) extends ANTLRInputStream(script) {
  override def getSourceName: String = {
    if (srcPath.isDefined)
      srcPath.get.toString
    else
      super.getSourceName
  }
}