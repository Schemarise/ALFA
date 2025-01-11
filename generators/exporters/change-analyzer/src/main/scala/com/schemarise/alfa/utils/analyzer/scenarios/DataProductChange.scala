package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.nodes.Dataproduct
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}

import java.util
import java.util.Optional
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import schemarise.alfa.runtime.model.diff._

object DataProductChange {

  def run(changeSet: CompilationUnitChangeSet, mods: util.ArrayList[IModification]): List[IModification] = {

    val results = ListBuffer[IModification]()

    val dpsv1 = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.dataproduct)
    val dpsv2 = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.dataproduct)

    val dpDiffs = new DiffResults[IUdtVersionName](dpsv1, dpsv2)

    dpDiffs.deleted.foreach(e => {
      results ++= ChangeAnalyzer.toUdtModifications(List(e), ChangeUdtType.Dataproduct, EditType.Removed, ChangeCategoryType.DataProductChange)
    })

    dpDiffs.added.foreach(e => {
      results ++= ChangeAnalyzer.toUdtModifications(List(e), ChangeUdtType.Dataproduct, EditType.Added, ChangeCategoryType.DataProductChange)
    })

    val globalModifiedTypes = mods.asScala.filter(e => e.isInstanceOf[IUdtModification]).
      map(_.asInstanceOf[IUdtModification]).
      filter(_.getEditType == EditType.Updated).
      map(_.getTargetUdt.getUdtName).toSet

    dataProductInternalChanges(changeSet, results, dpDiffs, globalModifiedTypes)

    val l = new ListBuffer[IModification]

    //    dps.foreach( e => {
    //      val dp = changeSet.v2.getUdt( e.fullyQualifiedName ).get.asInstanceOf[Dataproduct]
    //      val publishedMods = dp.publish.map( _.fullyQualifiedName ).filter( e => modifiedTypes.contains(e))
    //      val consumedMods = dp.consume.map( _._2.map( x => x.fullyQualifiedName) ).flatten.filter( e => modifiedTypes.contains(e))
    //
    //      if ( publishedMods.size > 0 || consumedMods.size > 0 ) {
    //        val d = DataproductModifications.builder().setDataproductName(e.fullyQualifiedName).
    //          setChangeCategory(ChangeCategoryType.IndirectBreakingDataStructureChange).
    //          addAllConsumeImpactPaths(consumedMods.toList.asJava).
    //          addAllPublishImpactPaths(publishedMods.toList.asJava).
    //          setEditType(EditType.Updated).build()
    //
    //        l.append(d)
    //      }
    //    })
    //    dpDeletions ++ dpAdditions ++ l.toList

    results.toList

  }

  private def dataProductInternalChanges(changeSet: CompilationUnitChangeSet,
                                         results: ListBuffer[IModification],
                                         dpDiffs: DiffResults[IUdtVersionName],
                                         globalModifiedTypes: Set[String]
                                        ): Unit = {
    dpDiffs.unchanged.foreach(dpName => {
      val dpv1 = changeSet.v1.getUdt(dpName.fullyQualifiedName).get.asInstanceOf[Dataproduct]
      val dpv2 = changeSet.v2.getUdt(dpName.fullyQualifiedName).get.asInstanceOf[Dataproduct]

      val publishDiff = new DiffResults[IUdtVersionName](dpv1.publish.map(_.versionedName).toSet, dpv2.publish.map(_.versionedName).toSet)
      val consumeDiff = new DiffResults[IUdtVersionName](dpv1.consume.map(_._2).flatten.toSet, dpv2.consume.map(_._2).flatten.toSet)

      if (!publishDiff.deleted.isEmpty) {
        results ++= ChangeAnalyzer.toUdtEntryModifications(
          udt = ChangeAnalyzer.toUdtReference(dpName),
          udtEntryNames = publishDiff.deleted.map(e => e.fullyQualifiedName).toList,
          uet = UdtEntryType.Publish,
          cct = ChangeCategoryType.DataProductChange,
          et = EditType.Removed,
          ct = ChangeUdtType.Dataproduct,
          message = Optional.of("Some published types removed"),
          before = Optional.empty(),
          after = Optional.empty())
      }

      if (!publishDiff.added.isEmpty) {
        results ++= ChangeAnalyzer.toUdtEntryModifications(
          udt = ChangeAnalyzer.toUdtReference(dpName),
          udtEntryNames = publishDiff.added.map(e => e.fullyQualifiedName).toList,
          uet = UdtEntryType.Publish,
          cct = ChangeCategoryType.DataProductChange,
          et = EditType.Added,
          ct = ChangeUdtType.Dataproduct,
          message = Optional.of("Additional types published"),
          before = Optional.empty(),
          after = Optional.empty())
      }

      if (!consumeDiff.deleted.isEmpty) {
        results ++= ChangeAnalyzer.toUdtEntryModifications(
          udt = ChangeAnalyzer.toUdtReference(dpName),
          udtEntryNames = consumeDiff.deleted.map(e => e.fullyQualifiedName).toList,
          uet = UdtEntryType.Consume,
          cct = ChangeCategoryType.DataProductChange,
          et = EditType.Removed,
          message = Optional.of("Some consumed types removed"),
          ct = ChangeUdtType.Dataproduct,
          before = Optional.empty(),
          after = Optional.empty())
      }

      if (!consumeDiff.added.isEmpty) {
        results ++= ChangeAnalyzer.toUdtEntryModifications(
          udt = ChangeAnalyzer.toUdtReference(dpName),
          udtEntryNames = consumeDiff.added.map(e => e.fullyQualifiedName).toList,
          uet = UdtEntryType.Consume,
          cct = ChangeCategoryType.DataProductChange,
          et = EditType.Added,
          message = Optional.of("Additional types consumed"),
          ct = ChangeUdtType.Dataproduct,
          before = Optional.empty(),
          after = Optional.empty())
      }

      val graph = changeSet.v2.graph

      val pubImpacted = publishDiff.unchanged.
        map(p => {
          val paths = globalModifiedTypes.map(g => {
            graph.shortestPath(changeSet.v2.getUdt(p.fullyQualifiedName).get, changeSet.v2.getUdt(g).get)
          }).flatten.toList
          paths
        }).flatten.toList

      val consumeImpacted = consumeDiff.unchanged.
        map(p => {
          val paths = globalModifiedTypes.map(g => {
            graph.shortestPath(changeSet.v2.getUdt(p.fullyQualifiedName).get, changeSet.v2.getUdt(g).get)
          }).flatten.toList
          paths
        }).flatten.toList

      //      val conImpacted = consumeDiff.unchanged.filter( e => globalModifiedTypes.contains(e.fullyQualifiedName ) ).map( _.fullyQualifiedName)

      if (!pubImpacted.isEmpty || !consumeImpacted.isEmpty) {
        val a = DataproductModifications.builder().setDataproductName(dpName.fullyQualifiedName).
          setChangeCategory(ChangeCategoryType.IndirectBreakingDataStructureChange).
          addAllConsumeImpactPaths(consumeImpacted.map(_.fullyQualifiedName).asJava).
          addAllPublishImpactPaths(pubImpacted.map(_.fullyQualifiedName).asJava).
          setEditType(EditType.Updated).build()

        results += a
      }
    })
  }
}
