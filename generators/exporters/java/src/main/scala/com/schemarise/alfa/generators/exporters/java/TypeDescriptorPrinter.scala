package com.schemarise.alfa.generators.exporters.java

import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IUdtBaseNode}
import com.schemarise.alfa.compiler.ast.model.types.IDataType
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, ILogger}
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

import java.nio.file.Path

class TypeDescriptorPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes)
  extends PrinterBase(logger, outputDir, cua) {

  private val alfaNs = new AlfaNamespaceTypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)
  private val userNs = new UserTypesTypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)

  def buildDeclAndDataType(t: IDataType): (String, String) = {
    t match {
      case x: IUdtBaseNode =>
        if (BuiltinModelTypes.Includes(x.name.fullyQualifiedName)) {
          return alfaNs.buildDeclAndDataType(t)
        }
      case _ =>
    }

    userNs.buildDeclAndDataType(t)
  }

  def print(udt: IUdtBaseNode): String = {
    if (BuiltinModelTypes.DoesNotInclude(udt.name.fullyQualifiedName))
      userNs.print(udt)
    else
      alfaNs.print(udt)
  }
}
