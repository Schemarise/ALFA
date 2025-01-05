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
package com.schemarise.alfa.compiler.test.lang.datatypes

import com.schemarise.alfa.compiler.ast.nodes.datatypes.TablularDataType
import com.schemarise.alfa.compiler.tools.tabular.UdtFlattener
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class BuiltinGenericsTest extends AnyFunSuite {
  test("Built-in generic types") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record Data {
        | Name : string
        | Age : int
        | Aliases : list< string >
        |}
        |
        |record Request {
        |   sheet : table< Data >
        |   response : future< int >
        |}
        |
      """)
    val udt = cua.getUdt("com.acme.Request").get
    val field = udt.allFields.get("sheet").get
    val tdt = field.dataType.asInstanceOf[TablularDataType]
    val t = new UdtFlattener(cua, tdt.targetUdt.udt).table
    val cols = t.allColumns

    assert(cols.map(c => c.proposedName).equals(List("Name", "Age", "Aliases$Idx", "Aliases")))
  }

  test("table type") {
    val cua = TestCompiler.compileInvalidScript("@5:11 Table datatype can only be parameterized with user-defined-types",
      """
        |namespace com.acme
        |
        |record Request {
        |   sheet : table< list< string > >
        |}
        |
      """)
  }

  // TODO have union X {} and tabular< X >. Should Columns have the concept of optionality - null/not null.
}