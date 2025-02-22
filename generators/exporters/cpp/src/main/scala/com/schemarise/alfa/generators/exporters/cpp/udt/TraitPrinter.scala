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
 
package com.schemarise.alfa.generators.exporters.cpp.udt

import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.Trait
import com.schemarise.alfa.generators.common.TextWriter
import com.schemarise.alfa.generators.exporters.cpp.PrinterBase

import java.nio.file.Path

class TraitPrinter(tw: TextWriter, outputDir: Path, udt: Trait) extends PrinterBase(tw.logger, outputDir) {

  def print(): Unit = {

    tw.enterFile(cppHeaderFileName(udt.name))

    val exts = Seq("public schemarise::alfa::AlfaObject") ++ udt.includes.map( i => s"public ${i.fullyQualifiedName.replace(".", "::")}" )

    tw.writeln(
      s"""${fileHeader(udt)}
         |    class ${cppTypeName(udt)} : ${exts.mkString(", ")} {
         |        public:
         |${getters(udt)}
         |    };
         |${fileFooter(udt)}
         |""".stripMargin)

    tw.exitFile()
  }

  private def getters(udt: IUdtBaseNode) = {
    val n = cppTypeName(udt)

    udt.allFields.
      filter(f => udt.localFieldNames.contains(f._1)).
      map(f => {
        val g = accessorMethod(f._2, "")
        s"""        virtual $g = 0;""".stripMargin
      }).mkString("\n")
  }
}
