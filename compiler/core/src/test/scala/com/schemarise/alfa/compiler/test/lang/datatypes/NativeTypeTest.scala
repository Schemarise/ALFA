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

import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class NativeTypeTest extends AnyFunSuite {
  test("NativeType test1") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |   storage = native com.acme.types.StorageUnit
        |}
        |
        |record PC {
        |    SDD : storage
        |    HDD : storage
        |}
        |
    """.stripMargin)

    val u = cua.getUdt("PC").get
    val dt = u.allFields.get("SDD").get.dataType
    assert(dt.isUdt)
    assert(dt.asInstanceOf[UdtDataType].udt.isNativeUdt)

  }

  test("NativeType equality") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |   storage = native com.acme.types.StorageUnit
        |}
        |
        |record PC {
        |    ST : storage
        |
        |    assert SType {
        |        let x = ST == "SDD"
        |        let xx : storage = "20"
        |        let s = toString(ST)
        |    }
        |}
        |
    """.stripMargin)

    val u = cua.getUdt("PC").get
  }


}
