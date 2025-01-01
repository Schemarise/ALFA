package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.graph.{Edges, ICompilationUnitArtifactGraph, IGraphEdge}
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.utils.VFS

import java.nio.file.Path
import java.util.function.Predicate

abstract class AlfaExporter(param: AlfaExporterParams) extends TextWriter(param.logger) with SupportedGenerator {

  if (param.cua.hasErrors)
    throw new GeneratorException("Cannot exportObj due to compilation errors." + param.cua.getErrors.mkString("\n", "-", ""))

  VFS.mkdir(outputDirectory.getFileSystem, outputDirectory.toString)

  def skipCodegen(e: IdentifiableNode) = {
    e match {
      case x: UdtBaseNode => x.skipCodeGen
      case _ => false
    }
  }

  def exportConfig = param.exportConfig

  val graphToGenerate: ICompilationUnitArtifactGraph = param.cua.scopedGraph(param.exportScopeType)

  def typesToGenerate(): Seq[IdentifiableNode] = {
    val g = graphToGenerate.topologicalOrPermittedOrdered
    if (g.isFailure)
      throw new RuntimeException("Failed to get graph " + g)
    else
      g.get
  }


  def reversedIncsTypesToGenerate(): Seq[IdentifiableNode] = {
    val g = param.cua.scopedGraph(param.exportScopeType, true).topologicalOrPermittedOrdered
    if (g.isFailure)
      throw new RuntimeException("Failed to get graph " + g)
    else
      g.get
  }


  override def outputDirectory: Path = param.outputDir

  @throws[GeneratorException]
  def exportSchema(): List[Path]

  def supportedConfig(): Array[String]

  def requiredConfig(): Array[String] = Array()

  def name: String

  def makeModel(e: IdentifiableNode) = Unit

  private object KeyPredicate extends Predicate[IGraphEdge] {
    override def test(t: IGraphEdge): Boolean = t.getType == Edges.EntityToUDTKey
  }

  private object IsIncludesOnlyPredicate extends Predicate[IGraphEdge] {
    override def test(t: IGraphEdge): Boolean = t.getType == Edges.Includes
  }

}
