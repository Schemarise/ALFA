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
package com.schemarise.alfa.compiler.test.lang.feature

import com.schemarise.alfa.compiler.ast.UdtVersionedName
import com.schemarise.alfa.compiler.ast.nodes.StringNode
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class ConstTest extends AnyFunSuite {
  test("Extension simple") {
    val cua = TestCompiler.compileValidScript(
      """
        |
        |const ADJUSTABLE =    @Sample.AdjustableField()
        |const CLASS_CDE =     @Sample.FieldClassification( Sample.FieldClassificationType.CDE )
        |const CLASS_OTHER =   @Sample.FieldClassification( Sample.FieldClassificationType.Other )
        |
        |namespace Sample
        |
        |annotation AdjustableField( field ) {
        |}
        |
        |annotation FieldClassification( field ) {
        |    ftype : FieldClassificationType
        |}
        |
        |enum FieldClassificationType {
        |    CDE Other
        |}
        |
        |@alfa.meta.FieldAnnotations( {
        |    [              $CLASS_CDE,  ] : [ F1 ],
        |    [              $CLASS_OTHER ] : [ F2 ],
        |    [ $ADJUSTABLE, $CLASS_OTHER ] : [ F3 ]
        |})
        |record REC {
        |    F1 : int
        |    F2 : int
        |    F3 : int
        |}
      """)
  }

}

