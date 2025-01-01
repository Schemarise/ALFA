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

import com.schemarise.alfa.compiler.TraverseUtils
import com.schemarise.alfa.compiler.ast.nodes.Record
import com.schemarise.alfa.compiler.ast.nodes.datatypes.TupleDataType
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class VectorTupleTest extends AnyFunSuite {
  test("Labelled tuple") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record Data {
        | TupleField : tuple< F1 : int, F2 : string >
        | TupleField2 : tuple< F1 : int
        |                      F2 : string >
        |}
        |
      """)

    val rec = cua.getUdt("com.acme.Data").get.asInstanceOf[Record]

    val synthRec = rec.allFields.get("TupleField").get.dataType.asInstanceOf[TupleDataType].syntheticRecord

    assert(synthRec.versionedName.fullyQualifiedName == "com.acme.Data__TupleField")
    assert(synthRec.allFields.size == 2)
    assert(synthRec.allFields.contains("F1"))
    assert(synthRec.allFields.contains("F2"))
  }

  test("Non-labelled tuple") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record Data {
        | TupleField : tuple< IVal :int, SVal :string >
        |}
        |
      """)

    val rec = cua.getUdt("com.acme.Data").get.asInstanceOf[Record]

    val synthRec = rec.allFields.get("TupleField").get.dataType.asInstanceOf[TupleDataType].syntheticRecord

    assert(synthRec.versionedName.fullyQualifiedName == "com.acme.Data__TupleField")
    assert(synthRec.allFields.size == 2)
    assert(synthRec.allFields.contains("IVal"))
    assert(synthRec.allFields.contains("SVal"))

    val udts: Seq[String] = TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered().get).map(_.nodeId.id)
    assert(udts == Seq("com.acme.Data", "com.acme.Data__TupleField"))

  }

  //  test( "Mixed labelled tuple" ) {
  //    val cua = TestCompiler.compileInvalidScript( "@5:21 Either all tuple entries should be named or none - 'Age'",
  //      """
  //        |namespace com.acme
  //        |
  //        |record Data {
  //        | TupleField : tuple< Age : int, Name : string >
  //        |}
  //        |
  //      """)
  //  }
}
