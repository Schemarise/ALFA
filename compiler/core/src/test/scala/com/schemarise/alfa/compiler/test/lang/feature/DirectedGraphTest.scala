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

import com.schemarise.alfa.compiler.ast.Namespace
import com.schemarise.alfa.compiler.ast.model.graph.{Edges, IGraphEdge}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.tools.graph._
import com.schemarise.alfa.compiler.utils.TestCompiler
import com.schemarise.alfa.compiler.{CompilationUnitArtifact, TraverseUtils}
import org.scalatest.funsuite.AnyFunSuite

import java.util.function.Predicate

class DirectedGraphTest extends AnyFunSuite {

  test("Graph with tuple dependent") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace sample
        |
        |record A {
        |   F : tuple< a : int
        |              b : tuple< x : B >
        |            >
        |}
        |
        |record B {
        |   F : int
        |}
        |
      """)


    cua.graph.topologicalOrPermittedOrdered()
    val a = cua.getUdt("sample.A").get
    val b = cua.getUdt("sample.B").get

    val out = cua.graph.outgoingEdgeNodes(a, x => true)
    assert(out.contains(b))
  }

  test("Model directed graph") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace alfa.model
        |
        |trait IDataType { }
        |
        |trait UdtBaseNode {
        |    Name : UdtVersionedName
        |    AllFields : map< string, Field >
        |    LocalFieldNames : list< string >
        |    IsSynthetic : boolean
        |}
        |
        |
        |record UdtVersionedName {
        |   // TypeParameters : list< TypeParameter >?
        |   FullyQualifiedName: string
        |   Version : int?
        |}
        |
        |record TypeParameter includes UdtBaseNode {
        |    DerivedFrom : IDataType?
        |}
        |
        |record Record includes UdtBaseNode {}
        |
        |record Field {
        |    Name : string
        |    DataType : IDataType
        |}
        |
      """)

    cua.graph.topologicalOrPermittedOrdered.get.foreach(e => {
      e match {
        case u: ITrait => println(u.name)
        case u: IRecord => println(u.name)
        case u: IKey => println(u.name)
        case u: IUnion => println(u.name)
        case u: IEntity => println(u.name)
        case u: IEnum => println(u.name)
        case u: IService => println(u.name)
        case u: ILibrary =>
        case ns: INamespaceNode =>
      }
    })
    //
    val to = cua.graph.topologicalOrPermittedOrdered().get.foreach(e => println(e.getClass.getName))

    //     println( to )
  }

  test("Simple directed graph") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait X { }
        |
        |namespace B
        |trait Y includes A.X { }
        |
        |key Id {}
        |
        |entity Order key Id {
        | F1 : map< string, list< Y >? >
        | F2 : A.X
        |}
      """)


    val orderedNames: List[String] = TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered().get).map(
      _.nodeId.asStringNode.toString).toList.sorted

    val expected = List("A.X", "B.Id", "B.Order", "B.OrderKey", "B.Y")

    assert(orderedNames.equals(expected))
    assert(!cua.graph.hasUserDefinedTypeCycles)
    assert(!cua.graph.namespacesHasCycles)
  }

  test("Has namespace cycles") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait AY includes B.BX { }
        |trait AZ { }
        |
        |namespace B
        |trait BX { }
        |trait BY includes A.AY { }
        |
      """)

    assert(cua.graph.namespacesHasCycles)
  }

  test("No namespace cycles") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait A.AX { }
        |
        |namespace A
        |trait AY includes B.BX { }
        |
        |trait AA.Trait { }
        |
        |namespace B
        |trait BX { }
        |
        |namespace C
        |trait CX { }
        |
      """)

    assert(!cua.graph.namespacesHasCycles)

    val expected = List("A", "A.AA", "C", "B")
    val orderedNames = cua.graph.namespacesTopologicallyOrPermittedOrdered().get.map(_.name)

    assert(orderedNames.equals(expected))

    val expectedUdts = List("A.AX", "A.AY")
    val udtsInA = cua.graph.namespaceOutgoingEdgeNodes(Namespace("A"), IsNamespaceUdtPredicate).toList.map(_.nodeId.id)

    assert(udtsInA.equals(expectedUdts))
  }

  test("Diamond-shaped UDT dependency order") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait Bottom includes Left, Right2 {}
        |trait Root {}
        |trait Left includes Root{}
        |trait Right1 includes Root{}
        |trait Right2 includes Left, Right1{}
        |
      """)

    val orderedNames = TraverseUtils.onlyUdts(cua.graph.topologicalOrPermittedOrdered().get).map(_.nodeId.id)
    val expected = List("Bottom", "Right2", "Left", "Right1", "Root")
    assert(orderedNames.equals(expected))
    assert(!cua.graph.hasUserDefinedTypeCycles)
  }

  test("Cycle detect") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait X includes Y { }
        |
        |trait Y {
        | F1 : set< Z >
        |}
        |
        |trait Z {
        |  F1 : map< string, X? >
        |}
      """)

    assert(cua.graph.hasUserDefinedTypeCycles)
  }

  test("Namespace cycle detect") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A
        |trait X includes Y { }
        |
        |trait Y {
        | F1 : set< B.Z >
        |}
        |
        |namespace B
        |trait Z {
        |  F1 : map< string, A.X? >
        |}
      """)

    assert(cua.graph.hasUserDefinedTypeCycles)
  }

  test("DirectedGraph Includes Dependency Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |trait A includes B1, B2 { }
        |
        |trait B1 { }
        |trait B2 includes C { }
        |
        |trait C includes D { }
        |
        |trait D { }
        |
        |record E includes D { }
        |record F includes D { }
        |
         """)

    val out = cua.graph.outgoingEdgeNodes(cua.getUdt("A").get, IsIncludesOnlyPredicate).toList.map(_.nodeId.id)
    List("B1", "B1", "C", "D").foreach(e => assert(out.contains(e)))

    val in = cua.graph.incomingEdgeNodes(cua.getUdt("D").get, IsIncludesOnlyPredicate).toList.map(_.nodeId.id)
    List("C", "B2", "A", "E", "F").foreach(e => assert(in.contains(e)))


    val includedIn = cua.graph
  }

  test("DirectedGraph Key Dependency Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |key Id { Id : uuid }
        |
        |entity Order key Id {}
        |entity Supplier key Id {}
        |entity Customer key Id {}
        |
         """)

    val in = cua.graph.incomingEdgeNodes(cua.getUdt("Id").get, new KeyPredicate).toList.map(_.nodeId.id)
    List("Order", "Supplier", "Customer").foreach(e => assert(in.contains(e)))

    val entityKeyPairs = cua.graph.topologicalOrPermittedOrdered().get.
      filter(e => e.isInstanceOf[IEntity]).
      map(e => (e.asInstanceOf[IEntity], cua.graph.outgoingEdgeNodes(e, new KeyPredicate).headOption)).
      filter(e => e._2.isDefined).
      map(e => (e._1.name, e._2.get.asInstanceOf[IKey].name)).toSeq

    val b = cua.graph.outgoingEdgeNodes(cua.getUdt("Order").get, new KeyPredicate).toList.map(_.nodeId.id)
  }

  class KeyPredicate extends Predicate[IGraphEdge] {
    override def test(t: IGraphEdge): Boolean = t.getType == Edges.EntityToUDTKey
  }

  test("DirectedGraph DataType Dependency Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |record A {
        | F1 : B
        | F2 : map< string, set< list< C >? >? >? // C buried several layers in
        |}
        |
        |record B { }
        |record C { }
        |
         """)

    val in = cua.graph.outgoingEdgeNodes(cua.getUdt("A").get, new IsDataTypePredicate).toList.map(_.nodeId.id)
    List("B", "C").foreach(e => assert(in.contains(e)))
  }

  class IsDataTypePredicate extends Predicate[IGraphEdge] {
    override def test(t: IGraphEdge): Boolean = t.getType == Edges.UdtToFieldDataType
  }

  test("DirectedGraph UDT Dependency Test") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |trait A {}
        |record B {}
        |entity C key (Id : uuid ) {}
        |
         """)

    val out = cua.graph.outgoingEdgeNodes(cua.asInstanceOf[CompilationUnitArtifact].getNamespace("com.acme").get, IsNamespaceUdtPredicate).toList.map(_.nodeId.id)
    List("com.acme.A", "com.acme.B", "com.acme.C").foreach(e => assert(out.contains(e)))
  }


  test("Namespace root to sub test 1") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace A.B
        |
        |record Rec1 {}
        |
         """)

    val out = cua.graph.namespacesTopologicallyOrPermittedOrdered()
    println(out)
  }
}
