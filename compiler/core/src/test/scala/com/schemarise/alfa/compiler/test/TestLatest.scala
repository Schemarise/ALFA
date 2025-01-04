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
package com.schemarise.alfa.compiler.test

import com.schemarise.alfa.compiler.utils.{AlfaTestPaths, StdoutLogger}
import org.apache.commons.io.FileUtils

import java.io.File
import scala.collection.JavaConverters._

object TestLatest extends AlfaTestPaths {

  @throws[Exception]
  def compileLatestCmlFile(): Unit = {

    val stdout = new StdoutLogger

    stdout.info("Looking for latest .s files from " + TestRootDir.toFile + " ... ")
    val cmlFiles: Iterable[File] = FileUtils.listFiles(new File("/Users/sadia/IdeaProjects/smt"), Array("smt"), true).asScala

    cmlFiles.foreach(f => {
      val o = f.getAbsolutePath
      val n = o.replace(".smt", ".alfa")
      println(s"git mv $o $n")
    })

    //    val latestFile : File = cmlFiles.max( new Ordering[File] {
    //      override def compare(x: File, y: File): Int = x.lastModified().compareTo(y.lastModified())
    //    } )

    //    stdout.info("Compiling latest file " + latestFile + " ...")
    //
    //    val compiler = new com.schemarise.alfa.compiler.SynCompiler(stdout)
    //
    //    val cua = compiler.compile(latestFile.toPath)
    //
    //    cua.getErrors.foreach( println(_) )
  }

  def main(args: Array[String]): Unit = {
    compileLatestCmlFile
  }
}
