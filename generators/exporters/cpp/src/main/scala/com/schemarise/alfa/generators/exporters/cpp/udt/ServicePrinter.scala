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

import com.schemarise.alfa.compiler.ast.model.{IMethodSignature, IService}
import com.schemarise.alfa.compiler.ast.nodes.Service
import com.schemarise.alfa.generators.common.TextWriter
import com.schemarise.alfa.generators.exporters.cpp.PrinterBase

import java.nio.file.Path

class ServicePrinter(tw: TextWriter, outputDir: Path, udt: Service) extends PrinterBase(tw.logger, outputDir) {

  def cppMethodSig(m: IMethodSignature): String = {
    val formals = m.formals.map(m =>
      s"${toCppTypeName(m._2.dataType)} ${localFieldName(m._1)}"
    )

    s"""    virtual ${toCppTypeName(m.returnType)} ${m.name.fullyQualifiedName}(${formals.mkString(", ")});
       |""".stripMargin
  }

  def print(): Unit = {
    val clz = cppTypeName(udt)

    val methods = udt.getMethodSignatures.toSet
    val methogSigs = methods.map(m => cppMethodSig(m._2)).mkString("")

    val ctorArgs = udt.constructorFormals.map(e => {
      toCppTypeName(e._2.dataType)  + " " + localFieldName(e._1)
    }).mkString(", ")

    tw.enterFile(cppHeaderFileName(udt.name))

    tw.writeln(
      s"""${fileHeader(udt)}
         |
         |class $clz {
         |$methogSigs};
         |
         |class ${clz}Factory {
         |   virtual $clz create($ctorArgs) = 0;
         |};
         |
         |${fileFooter(udt)}
      """.stripMargin)

    tw.exitFile()
  }
}
