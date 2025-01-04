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
package com.schemarise.alfa.compiler.test.lang.feature.generics

import com.schemarise.alfa.compiler.ast.model.types.{ITypeParameterDataType, Scalars}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ScalarDataType, TypeParameterDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

/**
 * Different forms of generic use:
 *
 * 1. from includes/extends
 * trait A< T > {
 * F1 : T
 * }
 *
 * #
 * record B includes A< int > {
 * }
 *
 *
 * 2. from field declarations
 *
 * record E {
 * F1 : A< string >
 * }
 *
 * 3. from multi-level declarations ( extension of 1. )
 *
 * trait G< T > {
 * F1 : T
 * }
 *
 * trait H< T1, T2 > includes G< T1 > {
 * F2 : T2
 * }
 *
 * trait I includes H< int, string > {
 * F3 : long
 * }
 */

class GenericsTest extends AnyFunSuite {

  test("Template arg required") {
    val cua = TestCompiler.compileInvalidScript(
      "@4:25 Unknown type 'Base'",
      """
        |trait Base<T> { }
        |
        |record Extended includes Base {}
      """)

    assert(cua.getWarnings.head.toString.equals("@4:25 Matching type name 'Base' found, but type arguments do not match"))
  }

  test("Template Argument Resolution") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Base<T> { F1 : T }
        |
        |record Extended includes Base<int>{}
      """)

    val recExtended = cua.getUdt("Extended").get

    val dtExtended = recExtended.allFields.get("F1").get.dataType
    assert(dtExtended.isInstanceOf[ScalarDataType] && dtExtended.asInstanceOf[ScalarDataType].scalarType == Scalars.int)

    val recBase = cua.getUdtWithParams("Base", Seq("T")).get
    val dtBase = recBase.allFields.get("F1").get.dataType
    assert(dtBase.isInstanceOf[TypeParameterDataType] && dtBase.asInstanceOf[TypeParameterDataType].tp.nameNode.text.equals("T"))
  }

  test("Diamond shape templated types") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Level0<T> {
        |   L0 : T
        |}
        |
        |trait Level1A includes Level0<int> {
        |   L1A : date
        |}
        |
        |trait Level1B includes Level0<int> {
        |   L1B : string
        |}
        |
        |trait Level2 includes Level1A, Level1B {
        |   L2 : long
        |}
      """)
  }


  test("Type parameter passed as param") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Value< T > { F : T }
        |
        |trait Calc< R > includes Value< R > {}
      """)

    val c = cua.getUdtWithParams("Calc", Seq("R")).get
    val f = c.allFields.get("F").get.dataType

    assert(f.isInstanceOf[ITypeParameterDataType])
    assert(f.asInstanceOf[ITypeParameterDataType].parameterName.equals("R"))
  }

  test("Type parameter passed as param 2") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait TraitOneParam< T > {
        |    OneParamField : T
        |}
        |
        |trait AbstractRecordTwoParam< T1, T2 > includes TraitOneParam< T1 > {
        |//    RecParamField : T2
        |}
        |
        |//record ConcreteRecordTwoParam1 includes TraitOneParam< long > { }
        |record ConcreteRecordTwoParam2 includes AbstractRecordTwoParam< long, short > { }
        |
        |//record ConcreteRecordTwoParam3 {
        |//    F: AbstractRecordTwoParam< string, short >
        |//}
        |
      """)

    //    val c = cua.getUdt("ConcreteRecordTwoParam3").get
    //    val t = c.allFields.get("F").get.dataType.asInstanceOf[IUdtDataType].udtTemplateInit
    //
    //    val f = t.allFields.get("OneParamField").get.dataType
    //    assert( f.asInstanceOf[IScalarDataType].scalarType.toString.equals("string") )
    //
    //    println( f )
  }

  test("Templated Name Conflict 1") {
    val cua = TestCompiler.compileInvalidScript("@3:6 The declaration of 'Result.Data<R>' in @3:6 was previously declared in @2:0",
      """
        |trait Result.Data<T> {}
        |trait Result.Data<R> {}
      """
    )
  }

  test("Templated Name Conflict 2") {
    val cua = TestCompiler.compileInvalidScript(
      "@3:18 Template parameter 'T' conflicts with a user defined type with the same name at @2:0",
      """
        |trait T {}
        |trait Result.Data<T> {
        | F : T
        |}
      """
    )
  }


}
