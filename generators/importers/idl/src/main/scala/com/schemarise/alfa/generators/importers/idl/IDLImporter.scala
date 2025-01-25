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

package com.schemarise.alfa.generators.importers.idl

import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.utils.VFS
import com.schemarise.alfa.generators.common.{AlfaImporter, AlfaImporterParams}

import java.nio.file.{Files, Path}

class IDLImporter(param : AlfaImporterParams) extends AlfaImporter(param) {

  override def importSchema(): List[Path] = {

    val idlFiles =
      if ( Files.isDirectory(param.rootPath) )
        VFS.listFileForExtension(param.rootPath, "*.idl")
      else
        List(param.rootPath)

    idlFiles.foreach( p => {
      val r = new IDLReader(param.logger, param.outputDirectory)
      r.read(p)
    } )

    List.empty
  }

  override def supportedConfig(): Array[String] = ???

  override def requiredConfig(): Array[String] = ???

  override def name: String = "idl"

  override def getDefinitions(): Set[String] = ???

  override def getDefinition(name: String): Option[IUdtBaseNode] = ???
}
