package com.schemarise.alfa.generators.importers.structureddata

import com.schemarise.alfa.compiler.CompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.nodes.Record

import scala.collection.mutable

trait TypeBuilder {
  val udts : mutable.HashMap[String, Record]
  val cua : CompilationUnitArtifact
}
