package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IUdtBaseNode}
import com.schemarise.alfa.compiler.ast.nodes.Expression
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

class EntityPrinter(logger: ILogger, outputDir: Path,
                    cua: ICompilationUnitArtifact,
                    compilerToRt: CompilerToRuntimeTypes, reqMutable: Boolean) extends RecordPrinter(logger, outputDir, cua, compilerToRt, reqMutable) {

  override val mandatoryInclude = "com.schemarise.alfa.runtime.Entity"

  override protected def auxiliaryCode(udt: IUdtBaseNode): String = {
    val k = udtEntityKeyType(udt)
    val kstr = if (k.isDefined) s"public java.util.Optional< ${toJavaTypeName(k.get)} > get${Expression.DollarKey}();" else ""

    return kstr + super.auxiliaryCode(udt)
  }
}