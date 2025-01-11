package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.CompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact

case class CompilationUnitChangeSet(v1: CompilationUnitArtifact, v2: CompilationUnitArtifact) {
}

object CompilationUnitChangeSet {
  def apply(v1: ICompilationUnitArtifact, v2: ICompilationUnitArtifact): CompilationUnitChangeSet =
    new CompilationUnitChangeSet(v1.asInstanceOf[CompilationUnitArtifact], v2.asInstanceOf[CompilationUnitArtifact])
}