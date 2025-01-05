package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

class KeyPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact,
                 compilerToRt: CompilerToRuntimeTypes, reqMutable: Boolean) extends RecordPrinter(logger, outputDir, cua, compilerToRt, reqMutable) {
  override val mandatoryInclude = "com.schemarise.alfa.runtime.Key"

}
