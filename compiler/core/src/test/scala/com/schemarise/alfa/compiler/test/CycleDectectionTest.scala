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

import com.schemarise.alfa.compiler.ast.model.graph.Edges.EdgeType
import com.schemarise.alfa.compiler.ast.model.graph.{Edges, IGraphEdge}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

import java.util.function.Predicate
import scala.collection.mutable.ListBuffer

class CycleDectectionTest extends AnyFunSuite {
  test("test trait cycle") {
    val cua = TestCompiler.compileInvalidScript(
      "@6:6 Cycle detected in Trait included from C   Feature.Docs.B > Feature.Docs.A > Feature.Docs.C > Feature.Docs.B",
      """
        |namespace Feature.Docs
        |
        |trait A includes C {}
        |trait B includes A {}
        |trait C includes B {}
        |
      """.stripMargin)
  }

  test("test record cycle") {
    val cua = TestCompiler.compileInvalidScript(
      "@4:0 Cycle detected in extends from Feature.Docs.A Feature.Docs.B",
      """
        |namespace Feature.Docs
        |
        |record A extends C {}
        |record B extends A {}
        |record C extends B {}
        |
      """.stripMargin)
  }


  test("test extends DAG ") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.Docs
        |
        |record A {}
        |record B extends A {}
      """.stripMargin)

    val s = cua.graph.topologicalOrPermittedOrdered.get
    val g = cua.graph

    val t = cua.getUdt("Feature.Docs.B").get

    val etypes = ListBuffer[EdgeType]()

    val edges = g.outgoingEdgeNodes(t, new Predicate[IGraphEdge]() {
      override def test(t: IGraphEdge): Boolean = {
        etypes += t.getType
        true
      }
    })

    assert(etypes.length == 1)
    assert(etypes.contains(Edges.Extends))
  }

  test("test trait ") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace Feature.Docs
        |
        |record Sample1 includes TraitA
        |{
        |    F4 : map< int,  TraitA>
        |}
        |
        |# This is Trait A
        |trait TraitA {
        |    TA1 : string
        |}
        |
      """.stripMargin)

    val s = cua.graph.topologicalOrPermittedOrdered.get
    val g = cua.graph

    val t = cua.getUdt("Feature.Docs.Sample1").get


    val etypes = ListBuffer[EdgeType]()

    val edges = g.outgoingEdgeNodes(t, new Predicate[IGraphEdge]() {
      override def test(t: IGraphEdge): Boolean = {
        etypes += t.getType
        true
      }
    })

    assert(etypes.length == 2)
    assert(etypes.contains(Edges.Includes))
    assert(etypes.contains(Edges.UdtToFieldDataType))
  }
}
