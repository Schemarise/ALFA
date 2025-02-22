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
 
package com.schemarise.alfa.generators.exporters.cpp

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.nodes.{Entity, EnumDecl, Key, Record, Service, Trait, Union}
import com.schemarise.alfa.compiler.utils.VFS
import com.schemarise.alfa.generators.common._
import com.schemarise.alfa.generators.exporters.cpp.udt._

import java.nio.file.Path
import scala.collection.mutable.{MultiMap, _}


class CppExporter(param: AlfaExporterParams)
  extends AlfaExporter(param) {

  private val compilerToRt = CompilerToRuntimeTypes.create(logger, param.cua)
  private val outputDir = param.outputDir

  param.logger.warn("C++ Exporter is under development and some ALFA features are not yet supported")

  override def name = "cpp"

  VFS.mkdir(outputDir.getFileSystem, outputDir.toString)

  def exportSchema(): List[Path] = {

    val enp = new EnumPrinter(this, outputDir)

    val topOrdered = typesToGenerate()

    var nspaceUdts = new HashMap[INamespaceNode, Set[IUdtBaseNode]] with MultiMap[INamespaceNode, IUdtBaseNode]

    topOrdered.foreach(e => {
      if (e.isInstanceOf[IUdtBaseNode]) {
        val u = e.asInstanceOf[IUdtBaseNode]
        val up = u.name.namespace
        nspaceUdts.addBinding(up, u)
      }
    })


    nspaceUdts.foreach(nsDef => {
      val to = nsDef._2
      val ns = nsDef._1

      to.foreach(e => {
        e match {
          case u: Record => new RecordPrinter(this, outputDir, compilerToRt, u).print()
          case u: EnumDecl => enp.print(u)
          case u: Key => new KeyPrinter(this, outputDir, compilerToRt, u).print()

          case u:Trait => new TraitPrinter(this, outputDir, u ).print()
          case u:Service => new ServicePrinter(this, outputDir, u).print()

          case u: Entity =>
            new EntityPrinter(this, outputDir, compilerToRt, u ).print()
            if (u.key.isDefined && u.key.get.isSynthetic) {
              // synthetic nodes are ignored by topo sort
              new KeyPrinter(this, outputDir, compilerToRt, u.key.get.asInstanceOf[Key]).print()
            }
          case u:Union => new UnionPrinter(this, outputDir, u).print()
          case _ =>
            logger.warn(s"Skipping C++ generation for ${e.name.udtType} ${e.name.fullyQualifiedName}")

        }
      })
    })

    List.empty
  }

  def supportedConfig(): Array[String] = Array.empty
}
