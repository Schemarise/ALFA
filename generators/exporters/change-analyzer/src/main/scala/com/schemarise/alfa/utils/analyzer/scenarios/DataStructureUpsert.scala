package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}

import java.util.Optional
import schemarise.alfa.runtime.model.diff._

object DataStructureUpsert {

  def isTargetUdtType(t: UdtType): Boolean =
    t == UdtType.record || t == UdtType.union || t == UdtType.entity || t == UdtType.key || t == UdtType.`trait` || t == UdtType.annotation

  def run(changeSet: CompilationUnitChangeSet): List[IModification] = {

    val namespaces = new DiffResults[String](changeSet.v1.getNamespaces.map(_.name).toSet, changeSet.v2.getNamespaces.map(_.name).toSet)

    val es: Optional[Snippet] = Optional.empty()

    val nsAdded = namespaces.added.map(e => {
      List(ChangeAnalyzer.toNamespaceModifications(e, EditType.Added, ChangeCategoryType.DataStructureUpsert, es, es
      ))
    }).flatten.toList

    val nsDel = namespaces.deleted.map(e => {
      List(ChangeAnalyzer.toNamespaceModifications(e, EditType.Removed, ChangeCategoryType.DataStructureUpsert, es, es
      ))
    }).flatten.toList


    val v1UdtNames = changeSet.v1.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))
    val v2UdtNames = changeSet.v2.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))

    val added = new DiffResults[IUdtVersionName](v1UdtNames, v2UdtNames).added

    val additions = added.map(e => {
      val ut = ChangeAnalyzer.toChangeUdtType(e.udtType)
      ChangeAnalyzer.toUdtModifications(List(e), ut, EditType.Added, ChangeCategoryType.DataStructureUpsert)
    }).flatten.toList

    additions ++ nsAdded ++ nsDel
  }

}
