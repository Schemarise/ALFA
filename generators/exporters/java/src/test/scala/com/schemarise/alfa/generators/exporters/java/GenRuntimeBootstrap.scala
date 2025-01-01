package com.schemarise.alfa.generators.exporters.java

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler}
import com.schemarise.alfa.generators.common.AlfaExporterParams
import com.schemarise.alfa.utils.testing.{AlfaFunSuite, TestUtils}

import java.nio.file.Paths
import java.util.Collections

class GenRuntimeBootstrap extends AlfaFunSuite {
//    test("Java runtime update") {
//      val input = "/Users/sadia/IdeaProjects/alfa-core/libs/runtime/ast/src/main/alfa"
//      val javaGen = "/Users/sadia/IdeaProjects/alfa-core/libs/runtime/java/core/src/gen/java"
//      codegen(input, javaGen)
//    }
//
//  test("OpenAPI runtime update") {
//    val input = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/openapi/src/main/alfa/openapi.alfa"
//    val javaGen = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/openapi/src/main/gen/java/"
//
//    codegen(input, javaGen)
//  }
//
//  test("Spark model runtime update") {
//    val input = "/Users/sadia/IdeaProjects/alfa-core/pro/libs/runtime/java/alfa-lib-spark/src/main/alfa/spark-commands.alfa"
//    val javaGen = "/Users/sadia/IdeaProjects/alfa-core/pro/libs/runtime/java/alfa-lib-spark/src/main/java"
//
//    codegen(input, javaGen)
//  }
//
//  test("BigQuery runtime update") {
//    val input = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/gcp/big-query/src/main/alfa/bigquery-model.alfa"
//    val javaGen = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/gcp/big-query/src/main/gen/java"
//
//    codegen(input, javaGen)
//  }
//
//  test("Avro runtime update") {
//    val input = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/avro/src/main/alfa"
//    val javaGen = "/Users/sadia/IdeaProjects/alfa-core/generators/exporters/avro/src/main/gen/java"
//
//    codegen(input, javaGen)
//  }
//
//  // only used during code move§
//  def codegen(sinput: String, sjavaGen: String): Unit = {
//    val input = Paths.get(sinput)
//    val javaGen = Paths.get(sjavaGen)
//
//    val cua = TestCompiler.compileScriptOnly(input)
//
//    val j = new JavaExporter(AlfaExporterParams(new StdoutLogger(), javaGen, cua, Collections.emptyMap()))
//    j.exportSchema()
//  }
}
