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
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{LambdaDataType, ScalarDataType}
import com.schemarise.alfa.compiler.ast.nodes.{Record, Service}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class LambdaTypeTest extends AnyFunSuite {

  test("lambda declarations") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace etl
        |
        |service Converters {
        |	  listToSet<T>( l : list<T>,     lambda : func< T, T > ) : set<T>
        |	  mapToSet<K,V,T>( m : map<K,V>, lambda : func< (K, V), T > ) : set<T>
        |
        |	  mapToList<K,V,T>( m : map<K,V>, lambda : func< (K, V), T > ) : list<T>
        |	  setToList<T>( s : set<T>,       lambda : func< T, T > ) : list<T>
        |
        |	  setToMap<T,K>( s : set<T>,   keyMakerLambda : func< T, K > ) : map<K,T>
        |   listToMap<T,K>( l : list<T>, keyMakerLambda : func< T, K > ) : map<K,T>
        |}
        |
      """)

    val rec = cua.getUdt("etl.Converters").get.asInstanceOf[Service]

  }
}
