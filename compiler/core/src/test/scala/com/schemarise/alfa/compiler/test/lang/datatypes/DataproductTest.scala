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

import com.schemarise.alfa.compiler.utils.TestCompiler
import org.scalatest.funsuite.AnyFunSuite

class DataproductTest extends AnyFunSuite {
  test("data product 1") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |annotation Ann ( type ) {
        |}
        |
        |record Foo {
        |}
        |
        |dataproduct A {
        |    publish {
        |        @Ann
        |        # This is important
        |        Foo
        |    }
        |}
      """)

    assert(cua.getErrors.isEmpty)

    TestCompiler.compileInvalidScript(
      "@9:8 Unknown type 'com.acme.Food'",
      """
        |namespace com.acme
        |
        |record Foo {
        |}
        |
        |dataproduct A {
        |    publish {
        |        Food
        |    }
        |}
      """)
  }

  test("data product 2") {
    val cua = TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |annotation Ifc ( type ) {}
        |
        |record Foo {
        |}
        |
        |dataproduct A {
        |    publish {
        |        @Ifc
        |        # This is a comment
        |        Foo
        |    }
        |}
        |
        |dataproduct B {
        |    consume {
        |      A {
        |        @Ifc
        |        # This is a comment
        |        Foo
        |      }
        |    }
        |}
      """)

    println(cua.getUdt("com.acme.A").get)
    println(cua.getUdt("com.acme.B").get)

    assert(cua.getErrors.isEmpty)
  }

  test("data product 3") {
    TestCompiler.compileValidScript(
      """
        |namespace com.acme
        |
        |record Foo {
        |}
        |
        |dataproduct X {
        |    publish {
        |        Foo
        |    }
        |}
        |
        |dataproduct Y {
        |    consume {
        |      X {
        |        Foo
        |      }
        |    }
        |}
      """)

    TestCompiler.compileInvalidScript(
      "@18:6 Failed in expression. Dataproduct com.acme.X does not publish com.acme.Bar",
      """
        |namespace com.acme
        |
        |record Foo {
        |}
        |
        |record Bar {
        |}
        |
        |dataproduct X {
        |    publish {
        |        Foo
        |    }
        |}
        |
        |dataproduct Y {
        |    consume {
        |      X {
        |        Bar
        |      }
        |    }
        |}
      """)
  }

  test("data product internal 1") {
    val cua = TestCompiler.compileInvalidScript(
      "@9:8 Data products cannot use internal types - 'com.acme.Foo'",
      """
        |namespace com.acme
        |
        |internal record Foo {
        |}
        |
        |dataproduct A {
        |    publish {
        |        Foo
        |    }
        |}
      """)
  }

  test("data product internal 2") {
    val cua = TestCompiler.compileInvalidScript(
      "@10:8 Data products cannot use internal types - 'com.acme.Foo'",
      """
        |namespace com.acme
        |
        |internal record Foo {
        |}
        |
        |dataproduct B {
        |    consume {
        |      A {
        |        Foo
        |      }
        |    }
        |}
      """)
  }
}
