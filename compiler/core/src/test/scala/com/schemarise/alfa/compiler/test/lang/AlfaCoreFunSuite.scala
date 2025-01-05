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
package com.schemarise.alfa.compiler.test.lang

import org.scalatest.funsuite.AnyFunSuite

class AlfaCoreFunSuite extends AnyFunSuite {

  def assertEqualsIgnoringWhitespace(l: String, r: String): Unit = {
    assert(equalsIgnoringWhitespace(l, r))
  }

  def equalsIgnoringWhitespace(l: String, r: String): Boolean = {
    val nl = normalized(l)
    val nr = normalized(r)
    val res = nl.equals(nr)
    if (!res) {
      println("LHS: " + nl)
      println("RHS: " + nr)
    }
    res
  }

  def normalized(s: String): String = s.replaceAll("(?s)\\s+", " ").replaceAll(" ", "").trim
}
