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

import com.schemarise.alfa.compiler.settings.{AllSettings, ArtifactReference}
import com.schemarise.alfa.compiler.tools.AllSettingsFactory
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}
import org.w3c.dom.Element

import java.io.File
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util
import javax.xml.parsers.DocumentBuilderFactory
import scala.collection.JavaConverters._

class RepositoryManagerFactory(log: ILogger, rootUri: String, clientLogger: ILogger) {
  def getRepoMgrAndSettings(): (IRepositoryManager, AllSettings) = {

    val uri = new URI(rootUri.replace("\\", "/"))
    log.debug("Discovering pom.xml paths based on " + uri)
    val rootDir = Paths.get(uri)

    val pom = rootDir.resolve("pom.xml")
    val buildpom = rootDir.resolve("build" + File.separator + "pom.xml")

    if (Files.exists(pom) && Files.exists(buildpom))
      log.info(s"Both ${pom} and ${buildpom} exists. Will use ${pom}")

    if (Files.exists(pom))
      processPom(pom)
    else if (Files.exists(buildpom))
      processPom(buildpom)
    else {
      log.debug(s"No pom files found")
      (new NoOpRepositoryManager(), AllSettingsFactory.empty)
    }
  }

  private def readBuildOutputDir(pom: Path): String = {

    val pomFile = pom.toFile

    val dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = dBuilder.parse(pomFile)
    doc.getDocumentElement().normalize()
    val dependencyNodes = doc.getDocumentElement.getElementsByTagName("build")

    if (dependencyNodes != null && dependencyNodes.getLength > 0) {
      for (a <- 0 until dependencyNodes.getLength) {
        val n = dependencyNodes.item(a).asInstanceOf[Element]
        val dir = n.getElementsByTagName("directory")

        if (dir != null && dir.getLength > 0) {
          val d = dir.item(0).asInstanceOf[Element]
          return d.getTextContent;
        }
      }
    }

    "target"
  }

  private def processPom(pom: Path): (IRepositoryManager, AllSettings) = {

    val cmd = "mvn -Dmdep.useRepositoryLayout=true clean dependency:copy-dependencies"

    log.debug("Using pom.xml - " + pom)

    val pomDir = pom.getParent

    val targetDir = pomDir.resolve(readBuildOutputDir(pom))

    val depsDir = targetDir.resolve("dependency")

    log.debug("Looking for dependency directory " + depsDir)

    if (!Files.exists(depsDir)) {
      log.info(s"If project depends on other ALFA projects run '$cmd' in '$pomDir' directory")
      (new NoOpRepositoryManager(), AllSettingsFactory.empty)
    }
    else {
      val zips = VFS.listFileForExtension(depsDir, "-alfa.zip")

      log.info("Found ALFA zips " + zips.mkString(" | "))

      val fs = VFS.create()
      val vfs = fs.getPath("/")

      VFS.mkdir(vfs)

      zips.foreach(f => {
        val t = vfs.resolve(f.getName(f.getNameCount - 1).toString)
        Files.copy(f, t)
      })

      val copiedZips = VFS.listFileForExtension(vfs, "zip")
      log.info("Copied ALFA zips " + copiedZips.mkString(" | "))

      clientLogger.info("Loaded dependencies:\n" + copiedZips.mkString("\n"))

      val artRefs = zips.map(z => {
        val repoPath = depsDir.relativize(pomDir.resolve(z))

        // e.g com/acme/models/Data/1.0-SNAPSHOT/Data-1.0-SNAPSHOT.zip
        val zipFullPath = repoPath.toString.split("/")

        val pkgAndArtifactAndVersion = zipFullPath.dropRight(1)
        val version = pkgAndArtifactAndVersion.last
        val name = pkgAndArtifactAndVersion.dropRight(1).last
        val pkg = pkgAndArtifactAndVersion.dropRight(2).mkString(".")

        ArtifactReference(pkg, name, Some(version))(Some(z))
      })

      val cpp = new CompilerParamProvider() {
        override def getOutputRootDir: Path = ???

        override def getSourcePath: Path = pomDir

        override def getProjectArtifactDef: ArtifactReference = ???

        override def getDependencies: util.List[ArtifactReference] = artRefs.asJava
      }

      val settings = AllSettings(dependencies = Some(artRefs.toSet))
      val rm = new MavenBasedRepositoryMgr(log, cpp)
      (rm, settings)
    }
  }

}
