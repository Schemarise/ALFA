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

import com.schemarise.alfa.compiler.ast.model.types.Scalars
import com.schemarise.alfa.compiler.ast.nodes.Record
import com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class TypeDefsTest extends AnyFunSuite {

  test("Typedefs nested resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |  money = ccy
        |  ccy = string
        |}
        |
        |record Trans {
        |  F1 : money
        |}
        |
        |"""
    )
    val dt = cua.getUdt("Trans").get.allFields.get("F1").get.dataType
    assert(dt.asInstanceOf[ScalarDataType].scalarType.equals(Scalars.string))
  }


  test("Typedefs recursive resolution") {
    val cua = TestCompiler.compileInvalidScript(
      "@4:7 Cyclic declaration between T1 and T2 - if intentional, nice try ;)",
      """
        |typedefs {
        |  T1 = T2
        |  T2 = T1
        |}
        |
        |record Trade {
        |}
        |
        |"""
    )
    //    val dt = cua.getUdt("Trade").get.allFields.get("F1").get.dataType
    //    assert( dt.asInstanceOf[ScalarDataType].scalarType.equals( Scalars.string ) )
  }

  test("Typedefs resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |  ccy = string
        |  ccys = list<ccy>
        |}
        |
        |record Example.Price {
        |  F1 : list<ccy>
        |  F2 : set<ccy>
        |  F3 : map<ccy, ccy?>
        |  F4 : tuple< Ccy : ccy, Nominal : double>
        |  Ccys : ccys
        |}
        |
      """)
    val o = cua.getUdt("Example.Price").get
    val i = o.asInstanceOf[Record]

    assert(i.allFields.contains("Ccys"))
    assert(i.allFields.get("Ccys").get.dataType.toString.equals("list< string >"))
  }

  test("UDT Typedefs ") {
    val cua = TestCompiler.compileValidScript(
      """
        |typedefs {
        |  DateSeries<T> = Series<date,T>
        |}
        |
        |record Series< X, Y > {
        |    x : X
        |    y : Y
        |}
        |
        |record Example.Price {
        |  F1 : DateSeries< int >
        |}
        |
      """)
    val o = cua.getUdt("Example.Price").get
    val i = o.asInstanceOf[Record]
    assert(i.allFields.get("F1").get.dataType.toString.equals("Series<date,int>"));
  }

  // TODO Test all other templating cases
}
