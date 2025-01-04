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
package com.schemarise.alfa.compiler.test.perf

import com.schemarise.alfa.compiler.ast.model.types.Scalars
import com.schemarise.alfa.compiler.utils.{AlfaTestPaths, StdoutLogger}
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class PerfTest extends AnyFunSuite with BeforeAndAfter with AlfaTestPaths {

  val genDir = GeneratedTestResourcesAlfaDir
  val bigTestFile = Paths.get(genDir.toString, "BigFile.alfa")
  private val TotalRecords = 2000
  private val FieldsPerRecord = 20
  private val ExpectedTime = 15

  before {
    Files.createDirectories(genDir)

    var sb = new StringBuffer

    val allScalars: List[String] = Scalars.values.map(_.toString).toList

    for (i <- 1 to TotalRecords) {
      sb.append("record Record" + i + "\n{\n")

      for (j <- 1 to FieldsPerRecord) {
        val idx = new Random().nextInt(allScalars.size)

        val randomType = allScalars(new Random().nextInt(allScalars.size))
        val t = if (randomType.eq("void") ||
          randomType.eq("pattern")) "string" else randomType

        sb.append("\tPrimF" + j + " : " + t + "\n")
      }
      sb.append("\tF" + FieldsPerRecord + " : Record" + ThreadLocalRandom.current.nextInt(1, TotalRecords) + "\n")
      sb.append("}\n\n")
    }

    FileUtils.write(bigTestFile.toFile, sb.toString, Charset.defaultCharset, false)
  }

  test("Compile large file with " + TotalRecords + " in under " + ExpectedTime + " seconds ") {
    val traceEnabled = StdoutLogger.trace

    val compiler = new com.schemarise.alfa.compiler.AlfaCompiler()
    StdoutLogger.trace(false)
    compiler.compile(bigTestFile)

    // time the 2nd run
    val start = System.currentTimeMillis
    compiler.compile(bigTestFile)
    val end = System.currentTimeMillis

    assert(end - start <= ExpectedTime * 1000)
    StdoutLogger.trace(traceEnabled)
  }
}