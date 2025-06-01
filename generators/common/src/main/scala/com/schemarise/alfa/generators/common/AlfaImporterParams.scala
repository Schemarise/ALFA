package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.utils.ILogger

import java.nio.file.Path

case class AlfaImporterParams(logger: ILogger,
                              rootPath: Path,
                              outputDirectory: Path,
                              importConfig: java.util.Map[String, Object]
                             ) {

}
