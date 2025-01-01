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
package com.schemarise.alfa.compiler.tools.repo

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.err.{DependencyZipNotFound, MultipleDeclsFromDependencies, ResolutionMessage}
import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}
import com.schemarise.alfa.compiler.utils.TokenImpl

import java.nio.file._
import scala.collection.mutable

class DependencyMgr(repoMgr: IRepositoryManager, cs: AllSettings, ctx: Context) extends IDependencyManager {

  private val entries = new mutable.HashMap[ArtifactReference, IArtifact]()
  private var fullIndex: Set[String] = Set.empty

  readDependencies(cs.dependencies)
  validate()
  loadGlobalDefs()

  logRepo()

  resolveRepoObjs()

  override def allDeclarations: Set[String] = fullIndex

  def validate(): Unit = {
    // TODO validate merged compiler settings
    // TODO validate indexed files will not conflict
  }

  def loadGlobalDefs(): Unit = {
    entries.values.foreach(e => {
      if (e.globalDefs.isDefined)
        ctx.readScript(Some(e.pathInRepository), e.globalDefs.get)
    })
  }

  private def readDependencies(curr: Option[Set[ArtifactReference]]): Unit = {
    if (curr.isDefined) {
      val c = curr.get.foreach(e => {
        if (!entries.contains(e)) {

          val art = repoMgr.getArtifact(e)

          if (art.isEmpty) {
            ctx.logger.error("Did not find " + curr + " dependency " + e + ". Available artifacts " + repoMgr.getArtifacts())
            ctx.addResolutionError(ResolutionMessage(TokenImpl.empty, DependencyZipNotFound)(None, List.empty, e))
          }
          else {
            entries += (e -> art.get)
            readDependencies(art.get.allSettings.dependencies)
          }
        }
      })
    }

    fullIndex = entries.values.flatMap(e => e.fileIndex).toSet
  }


  override def toString: String = {
    val sb = new mutable.StringBuilder()
    sb.append("Repository\n")
    entries.values.foreach(e => sb.append(e.toString))
    sb.toString()
  }


  private def resolveRepoObjs() = {
    entries.values.foreach(e => {
      ctx.logger.trace("Dependency location: " + e.pathInRepository)
      ctx.logger.trace("  Types: " + e.fileIndex.mkString("\n     ", "\n     ", ""))
    })
  }

  private def logRepo() = {
    if (ctx.logger.isTraceEnabled) {
      entries.values.foreach(e => {
        ctx.logger.trace("Dependency location: " + e.pathInRepository)
        ctx.logger.trace("  Types: " + e.fileIndex.mkString("\n     ", "\n     ", ""))
      })
    }
  }

  override def getExternalDeclAsString(ctx: Context, ref: UdtDataType): Option[(ArtifactEntry, String)] = {
    val refFullyQual = ref.toString
    val matches = entries.values.filter(e => e.fileIndex.contains(refFullyQual))
    if (matches.seq.size > 1) {
      ctx.addResolutionError(ResolutionMessage(ref.location, MultipleDeclsFromDependencies)(None, List.empty, ref.toString, matches.toString))
    }

    val m: Option[IArtifact] = matches.headOption

    if (m.isDefined) {
      val p = m.get.getEntryPath(ref.asUnixPath(".alfa"))
      val script = new String(Files.readAllBytes(p))

      // ctx.logger.trace("Resolved " + ref + " from " + m.get.pathInRepository + "!/" + p )

      val re = new ArtifactEntry(m.get, p)

      Some((re, script))
    }
    else
      None
  }
}


