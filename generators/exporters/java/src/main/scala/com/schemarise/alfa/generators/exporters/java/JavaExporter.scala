package com.schemarise.alfa.generators.exporters.java

import com.schemarise.alfa.runtime.{Alfa, AlfaRuntimeException}
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode

import java.nio.file.Path
import com.schemarise.alfa.generators.common.{AlfaExporter, AlfaExporterParams, CompilerToRuntimeTypes}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.stmt.{IAssignmentDeclarationStatement, IVarDeclarationStatement}
import com.schemarise.alfa.compiler.ast.nodes.{Library, Transformer, UdtBaseNode}
import com.schemarise.alfa.compiler.tools.graph.IsTransformerPredicate
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, VFS}
import com.schemarise.alfa.generators.exporters.java.udt._

import java.util.concurrent.atomic.AtomicInteger

object JavaExporter {
  val MetaInfSchemasPath = "META-INF/alfa-schemas"
  val ResourcesPath = "resources-path"

}

class JavaExporter(param: AlfaExporterParams) extends AlfaExporter(param) {

  override def name = "Java8"

  private val outputDir = param.outputDir

  def exportSchema(): List[Path] = {

    val compilerToRt = CompilerToRuntimeTypes.create(logger, param.cua)
    val to = typesToGenerate()

    exportResourcesSchema(compilerToRt, to)

    var requiresMutable = false
    param.cua.traverse(new NoOpNodeVisitor() {
      override def enter(e: IVarDeclarationStatement): Mode = {
        requiresMutable = true
        super.enter(e)
      }

      override def enter(e: IAssignmentDeclarationStatement): Mode = {
        requiresMutable = true
        super.enter(e)
      }
    })

    val tp = new TraitPrinter(logger, outputDir, param.cua, compilerToRt, requiresMutable)
    val rp = new RecordPrinter(logger, outputDir, param.cua, compilerToRt, requiresMutable)
    val trp = new TransformPrinter(logger, outputDir, param.cua, compilerToRt)
    val kp = new KeyPrinter(logger, outputDir, param.cua, compilerToRt, requiresMutable)
    val up = new UnionPrinter(logger, outputDir, param.cua, compilerToRt, requiresMutable)
    val ep = new EntityPrinter(logger, outputDir, param.cua, compilerToRt, requiresMutable)
    val enp = new EnumPrinter(logger, outputDir, param.cua, compilerToRt)
    val sp = new ServicePrinter(logger, outputDir, param.cua, compilerToRt)
    val pp = new LibraryPrinter(logger, outputDir, param.cua, compilerToRt)


    to.filter(e => !skipCodegen(e)).foreach(e => {

      e match {
        case u: ITrait => tp.print(u)
        case u: IRecord =>
          rp.print(u)
          u.versions.foreach(v => rp.print(v.asInstanceOf[IRecord]))

        case u: IKey => kp.print(u)
        case u: IUnion => up.print(u)
        case u: IEntity => {
          ep.print(u)
          if (u.key.isDefined && u.key.get.isSynthetic)
            rp.print(u.key.get) // synthetic nodes are ignored by topo sort
        }
        case u: IEnum => enp.print(u)
        case u: IService =>
          sp.print(u)
          u.versions.foreach(v => sp.print(v.asInstanceOf[IService]))
        case u: Library => pp.print(u)
        case _ =>
      }
    })

    to.foreach(e => {
      val nodes = graphToGenerate.incomingEdgeNodes(e, IsTransformerPredicate)
      val transformers = nodes.map(tn => {
        val t = tn.asInstanceOf[Transformer]
        t
      })

      if (transformers.size > 0)
        trp.print(e.asInstanceOf[UdtBaseNode], transformers)
    })

    List.empty
  }

  def supportedConfig(): Array[String] = Array(JavaExporter.ResourcesPath)

  private def exportResourcesSchema(compilerToRt: CompilerToRuntimeTypes, to: Seq[IdentifiableNode]) = {

    val rp = param.exportConfig.get(JavaExporter.ResourcesPath)

    val resourcesRoot =
      if (rp == null)
        outputDir
      else if (!rp.isInstanceOf[Path]) {
        throw new AlfaRuntimeException("Expected resources-path to be a java Path instance, was " + rp.getClass.getName)
      }
      else {
        rp.asInstanceOf[Path]
      }

    val targets = to.filter(e => !skipCodegen(e))

    val ai = new AtomicInteger(0)

    targets.foreach(e => {
      if (e.isInstanceOf[IUdtBaseNode]) {
        val u = e.asInstanceOf[IUdtBaseNode]
        if (u.isFieldContainer) {
          val res = compilerToRt.getUdtDetails(u.name.fullyQualifiedName)
          if (res.isResult) {
            val j = res.getResult
            val json = Alfa.jsonCodec().toFormattedJson(j)
            VFS.write(resourcesRoot.resolve(JavaExporter.MetaInfSchemasPath).resolve(u.name.fullyQualifiedName + ".json"), json)
            ai.getAndIncrement()
          }
          else if (res.isFailure && BuiltinModelTypes.DoesNotInclude(u.name.fullyQualifiedName)) {
            throw new Exception("Failed to locate " + u.name.fullyQualifiedName + " to export to JSON")
          }
        }
      }
    })

    logger.info(s"Wrote ${ai.get()} JSON models to path " + resourcesRoot)

  }
}
