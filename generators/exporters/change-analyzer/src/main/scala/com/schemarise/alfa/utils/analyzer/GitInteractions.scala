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
 
package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.tools.repo.RepositoryManagerFactory
import com.schemarise.alfa.compiler.utils.ILogger
import org.apache.commons.io.FileUtils

import java.io.{ByteArrayOutputStream, File, FileNotFoundException, PrintWriter}
import java.nio.file.{Path, Paths}
import scala.sys.process._
import sys.process._

object GitInteractions {
  val LOCAL_CHECKOUT = s"[local]"
}

class GitInteractions(logger: ILogger, existingCheckoutPath: String) {

  private val validatedCheckoutPath = new File(existingCheckoutPath)

  if (!validatedCheckoutPath.exists())
    throw new FileNotFoundException("Invalid Repository path " + existingCheckoutPath)

  private val tmpCheckoutPath = init()

  private def init() = {

    val url: String = getRemoteUrl(validatedCheckoutPath.getCanonicalFile.getAbsolutePath)
    val p = Paths.get(System.getProperty("java.io.tmpdir"), "alfa-tmp-git").toFile
    if (p.exists())
      FileUtils.deleteDirectory(p)

    FileUtils.forceMkdir(p)

    logger.info("Working directory : " + p.getAbsolutePath)

    gitexec(s"clone $url", p.toString)

    val f = p.listFiles().toSeq
    if (f.size == 0)
      throw new RuntimeException("Checkout failed")

    f.head.getAbsoluteFile
  }

  def getAllBranches(): List[String] = {
    val names = getRemoteBranchNames(tmpCheckoutPath.toString) //  analyzerGit.branchList().setListMode(ListMode.all).call().asScala.map(e => e.getName).toList
    names
  }

  def compareLocalToRepoVersion(): CompilationUnitChangeSet = {
    compareRepoBranches(GitInteractions.LOCAL_CHECKOUT, getWorkingBranchName)
  }

  def compareRepoBranches(branch1Name: String, branch2Name: String): CompilationUnitChangeSet = {
    val cu1 = checkoutAndGetCompilationUnit(branch1Name, tmpCheckoutPath.toPath)
    val cu2 = checkoutAndGetCompilationUnit(branch2Name, tmpCheckoutPath.toPath)
    CompilationUnitChangeSet(cu1, cu2)
  }

  private def checkout(n: String) = {

    val branchOnly = if (n.indexOf("/") > 0) n.split("/").last else n

    if (branchOnly == getBranchName(tmpCheckoutPath.toString)) {
      logger.info(s"Branch $n already checked-out")
    }
    else {
      if (n.indexOf("/") > 0 && branchOnly != "main") {
        gitexec(s"checkout -b $branchOnly $n")
      }
      else {
        gitexec(s"checkout $n")
      }
    }
  }

  private def checkoutAndGetCompilationUnit(bname: String, path: Path) = {
    if (bname == GitInteractions.LOCAL_CHECKOUT) {
      getCompilationUnit(validatedCheckoutPath.toPath)
    }
    else {
      checkout(bname)
      getCompilationUnit(path)
    }
  }

  private def getCompilationUnit(path: Path) = {
    logger.info("Compiling ALFA source in path " + path)
    val rms = new RepositoryManagerFactory(logger, path.toUri.toString, logger).getRepoMgrAndSettings()
    val rm = rms._1
    val allsettings = rms._2
    val compiler = new AlfaCompiler(logger, rm)
    val cu = compiler.compile(path, allsettings)

    if (cu.hasErrors)
      throw new RuntimeException("Has compilation errors:" + cu.getErrors.mkString("\n", "\n  - ", ""))

    cu
  }

  private def gitexec(cmd: String, inDir: String = tmpCheckoutPath.toString) = {
    val gitcmd = s"git -C $inDir $cmd"
    exec(gitcmd)
  }

  private def exec(cmd: String): (Int, String) = {
    val os = new ByteArrayOutputStream
    val writer = new PrintWriter(os)
    val status = cmd.!(ProcessLogger(writer.println, writer.println))
    writer.close()

    val output = os.toString("UTF-8")

    val cmdBuffer = new StringBuilder()
    cmdBuffer.append("alfa-shell> " + cmd + "\n")
    if (status != 0) {
      cmdBuffer.append(s"[return status $status]\n")
    }
    cmdBuffer.append(s"$output\n")

    if (status != 0) {
      logger.error(cmdBuffer.toString())
      throw new RuntimeException(s"Error status $status reported for command '$cmd'")
    }
    else {
      logger.debug(cmdBuffer.toString())
    }

    (status, output)
  }

  private def getBranchName(loc: String) = {
    val s = s"git -C $loc branch --show-current"
    exec(s)._2.trim
  }

  def getWorkingBranchName = {
    val s = s"git -C $existingCheckoutPath branch --show-current"
    exec(s)._2.trim
  }

  private def getRemoteBranchNames(loc: String) = {
    val s = s"git -C $loc branch -r"
    val result = exec(s)

    // remove symbolic link lines
    result._2.split("\n").filter(e => !e.contains("->")).map(_.trim).toList
  }

  private def getRemoteUrl(loc: String) = {
    val s = s"git -C $loc config --get remote.origin.url "
    val result = exec(s)
    result._2.trim
  }
}
