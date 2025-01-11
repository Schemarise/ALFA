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
package com.schemarise.alfa.compiler

import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IField, IUdtBaseNode}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class NormaliseTest extends AnyFunSuite {
  test("Normalize schema") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Normalize
        |
        |record Foo {
        | X : int
        | Y : int
        |}
        |
        |record Bar {
        | X : int
        | Y : int
        |}
        |
        |record Baz {
        | X : int
        | Y : int
        | Z : Bar
        |}
        |
      """)

    deduplicate(cua)
  }

  def deduplicate(cua: ICompilationUnitArtifact): ICompilationUnitArtifact = {

    val dedup = new util.HashMap[List[IField], IUdtBaseNode]()
    val dedupUdts = new util.HashMap[IUdtBaseNode, ListBuffer[IUdtBaseNode]]()

    cua.getUdtVersionNames().map(e => {
      val udt = cua.getUdt(e.fullyQualifiedName).get
      if (udt.includes.isEmpty) {
        val fieldsKey = udt.allFields.map(e => {
          e._2
        }).toList.sortBy(e => e.name)

        val lookup = dedup.get(fieldsKey)
        if (lookup != null) {
          dedupUdts.get(lookup).append(udt)
        }
        else {
          dedup.put(fieldsKey, udt)
          dedupUdts.put(udt, new ListBuffer[IUdtBaseNode]())
        }
      }
    })

    val excludedUdts = new ListBuffer[String]()
    dedupUdts.entrySet().asScala.foreach(e => {
      e.getValue.foreach(v => {
        excludedUdts.append(v.name.fullyQualifiedName)
        UdtDataType.toStringSubstitution(v.name.fullyQualifiedName, e.getKey.name.fullyQualifiedName)
      })
    })

    val all = cua.getUdtVersionNames().
      filter(e => !excludedUdts.contains(e.fullyQualifiedName)).
      map(e => cua.getUdt(e.fullyQualifiedName).get.toString).mkString("\n")

    println(all)

    TestCompiler.compileValidScript(all)
  }
}
