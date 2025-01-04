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
import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}
import com.schemarise.alfa.compiler.utils.ILogger

import java.nio.file._
import java.util
import scala.collection.mutable

class FileRepositoryMgr(logger: ILogger, repositoryPaths: Option[List[Path]]) extends IRepositoryManager {
  private val loadedArtifacts = new mutable.HashMap[ArtifactReference, IArtifact]()

  private def repoPaths = if (repositoryPaths.isDefined) repositoryPaths.get else List.empty

  override def toString: String = "FileRepository[ paths " + repositoryPaths.mkString(",") + "]"

  def getArtifact(ref: ArtifactReference): Option[IArtifact] = {
    val p = loadedArtifacts.get(ref)

    if (p.isDefined)
      p
    else {
      val matches = repoPaths.map(mp => findMatchingArtifact(ref, mp)).filter(e => e.isDefined)

      matches.size match {
        case 0 =>
          None
        case 1 =>
          loadedArtifacts.put(ref, matches.head.get)
          matches.head
        case _ =>
          logger.warn("Artifact " + ref + " found in multiple location, using first - " + matches)
          loadedArtifacts.put(ref, matches.head.get)
          matches.head
      }
    }
  }

  private def findMatchingArtifact(ref: ArtifactReference, mp: Path): Option[IArtifact] = {

    val p = mp.resolve(ref.asGroupAndNameAndVersionZipFileName(true))

    logger.trace("Searching for " + ref + " in " + p)

    if (Files.exists(p)) {
      val fs = FileSystems.newFileSystem(p, getClass.getClassLoader)
      val newRep = Artifact(ref)(fs, p)
      Some(newRep)
    } else
      None
  }

  def createDependencyManager(settings: AllSettings, ctx: Context): IDependencyManager = {
    new DependencyMgr(this, settings, ctx)
  }

  override def getArtifacts(): util.List[IArtifact] = {
    val a = new util.ArrayList[IArtifact]()
    loadedArtifacts.values.foreach(e => a.add(e))
    a
  }
}


