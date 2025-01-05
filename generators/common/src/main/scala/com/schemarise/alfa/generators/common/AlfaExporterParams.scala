package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.ast.model.ICompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.model.graph.GraphReachabilityScopeType
import com.schemarise.alfa.compiler.utils.ILogger

import java.nio.file.Path

case class AlfaExporterParams(logger: ILogger,
                              outputDir: Path,
                              cua: ICompilationUnitArtifact,
                              exportConfig: java.util.Map[String, Object],
                              exportScopeType: GraphReachabilityScopeType = GraphReachabilityScopeType.localandreachable
                             ) {

}
