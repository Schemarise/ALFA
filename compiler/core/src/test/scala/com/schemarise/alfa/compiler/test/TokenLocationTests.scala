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

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.utils.TokenImpl
import org.scalatest.funsuite.AnyFunSuite

class TokenLocationTests extends AnyFunSuite {

  val compiler = new AlfaCompiler()

  test("Valiate token location") {
    val cua = compiler.compile(
      """
        |record Data {
        |    F1 : int
        |}
      """.stripMargin)

    val udt = cua.getUdt("Data").get

    println(udt.asInstanceOf[UdtBaseNode].location)
  }

  test("Validate missing datatype") {
    val cua = compiler.compile(
      """
        |record Data {
        |    F1 : set<
        |}
      """.stripMargin)

    val udt = cua.getUdt("Data").get

    println(udt.asInstanceOf[UdtBaseNode].location.asInstanceOf[TokenImpl].toExtendedString)

    println(cua.getErrors)

  }

  test("Auto completion trick") {
    val cua = compiler.compile(
      """
        |record Data {
        |    F1 : set< int >
        |}
        |
        |r
      """.stripMargin)

    val udt = cua.getUdt("Data").get
    println(cua.getErrors)

  }

}