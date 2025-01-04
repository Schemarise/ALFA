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
package com.schemarise.alfa.compiler

import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.{IScalarDataType, IUdtDataType}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ScalarDataType, UdtDataType}
import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

class NodeVisitorTest extends AnyFunSuite {

  val cua = TestCompiler.compileValidScript(
    """
      |namespace A
      |trait X {
      | F1 : set<string>?
      |}
      |
      |namespace B.C
      |record Y {
      | F3 : A.X
      |}
      |
      |namespace A.B
      |union Z {
      | F2 : map< string, string >
      |}
      |
      |service Publisher( port : int )  {
      | publish( data : Z ) : void
      |}
      |
    """)


  test("Udt visit") {
    val v = new NoOpNodeVisitor() with AssertVisit {
      private var test = new ListBuffer[String]

      override def enter(e: ITrait): Mode = {
        test += e.asInstanceOf[Trait].nodeId.id
        super.enter(e)
      }

      override def enter(e: IUnion): Mode = {
        test += e.asInstanceOf[Union].nodeId.id
        super.enter(e)
      }

      override def enter(e: IRecord): Mode = {
        test += e.asInstanceOf[Record].nodeId.id
        super.enter(e)
      }

      override def assert(): Boolean = test == List("A.X", "A.B.Z", "B.C.Y")
    }

    cua.graph.traverse(v)
    assert(v.assert())
  }

  test("Fields visit") {
    val v = new NoOpNodeVisitor() with AssertVisit {
      private var test = new ListBuffer[String]

      override def enter(e: IField): Mode = {
        test += e.asInstanceOf[Field].nodeId.id
        super.enter(e)
      }

      override def assert(): Boolean = test == List("F1", "F2", "F3")
    }

    cua.graph.traverse(v)
    assert(v.assert())
  }

  test("Nested datatype visit") {
    val v = new NoOpNodeVisitor() with AssertVisit {
      private var test = new ListBuffer[String]

      override def enter(e: IScalarDataType): Mode = {
        test += e.asInstanceOf[ScalarDataType].scalarType.toString
        super.enter(e)
      }

      override def assert(): Boolean = test == List("string", "string", "string", "int", "void")
    }

    cua.graph.traverse(v)
    assert(v.assert())
  }

  test("UDT visit") {
    val v = new NoOpNodeVisitor() with AssertVisit {
      private var test = new ListBuffer[String]

      override def enter(e: IUdtDataType): Mode = {
        test += e.asInstanceOf[UdtDataType].resolvedType.get.nodeId.id
        super.enter(e)
      }

      override def assert(): Boolean = test == List("A.B.Z", "A.X")
    }

    cua.graph.traverse(v)
    assert(v.assert())
  }


  test("Block downstream visit") {
    val v = new NoOpNodeVisitor() with AssertVisit {
      private var test = new ListBuffer[String]

      override def enter(e: ITrait): Mode = NodeVisitMode.Break

      override def enter(e: IUnion): Mode = NodeVisitMode.Break

      override def enter(e: IRecord): Mode = NodeVisitMode.Break

      override def enter(e: IService): Mode = NodeVisitMode.Break

      override def enter(e: IScalarDataType): Mode = {
        test += e.asInstanceOf[ScalarDataType].scalarType.toString
        super.enter(e)
      }

      override def assert(): Boolean = test == List()
    }

    cua.graph.traverse(v)
    assert(v.assert())
  }

  trait AssertVisit {
    def assert(): Boolean
  }

}
