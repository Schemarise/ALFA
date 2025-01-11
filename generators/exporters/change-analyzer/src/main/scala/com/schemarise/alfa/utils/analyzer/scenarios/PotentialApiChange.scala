package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.nodes.Library
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet}
import schemarise.alfa.runtime.model.diff._

object PotentialApiChange {

  def run(changeSet: CompilationUnitChangeSet): List[IUdtModification] = {
    val v1LibrariesNames = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.library)
    val v2LibrariesNames = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.library)

    val v1commonNames = v1LibrariesNames.filter(e => v2LibrariesNames.contains(e))

    val v1Libraries = v1commonNames.map(e => changeSet.v1.getUdt(e.fullyQualifiedName).
      get.asInstanceOf[Library]).toList.sortBy(e => e.name.fullyQualifiedName)

    val v2Libraries = v1commonNames.map(e => changeSet.v2.getUdt(e.fullyQualifiedName).
      get.asInstanceOf[Library]).toList.sortBy(e => e.name.fullyQualifiedName)

    val zipped = v1Libraries.zip(v2Libraries)

    val updates: List[UdtEntryModification] =
      zipped.map(e => {
        val m1 = e._1.getMethodSignatures
        val m2 = e._2.getMethodSignatures

        val matchingMethods = m1.keySet.map(n => (m1.get(n), m2.get(n))).filter(e => e._2.isDefined).map(e => (e._1.get, e._2.get))

        val deletedMethods = m1.keySet.filter(k => !m2.keySet.contains(k))
        val addedMethods = m2.keySet.filter(k => !m1.keySet.contains(k))
        val changedMethods = matchingMethods.filter(e => e._1.toString != e._2.toString)

        val udtRef = UdtReference.builder().setUdtName(e._1.name.fullyQualifiedName).setUdtType(ChangeUdtType.Library).build()

        val delMods = deletedMethods.map(name => {
          val m = m1.get(name).get
          UdtEntryModification.builder().
            setEditType(EditType.Removed).
            setBeforeSnippet(ChangeAnalyzer.toSnippet(m)).
            setChangeCategory(ChangeCategoryType.PotentialApiChange).
            setEntryName(name).
            setEntryType(UdtEntryType.Method).
            setTargetUdt(udtRef).build()
        })

        val newMods = addedMethods.map(name => {
          val m = m2.get(name).get
          UdtEntryModification.builder().
            setEditType(EditType.Added).
            setAfterSnippet(ChangeAnalyzer.toSnippet(m)).
            setChangeCategory(ChangeCategoryType.ApiUpsert).
            setEntryName(name).
            setEntryType(UdtEntryType.Method).
            setTargetUdt(udtRef).build()
        })

        val updMods = changedMethods.map(pair => {
          val left = pair._1
          val right = pair._2

          UdtEntryModification.builder().
            setEditType(EditType.Updated).
            setBeforeSnippet(ChangeAnalyzer.toSnippet(left)).
            setAfterSnippet(ChangeAnalyzer.toSnippet(right)).
            setChangeCategory(ChangeCategoryType.PotentialApiChange).
            setEntryName(left.name.fullyQualifiedName).
            setEntryType(UdtEntryType.Method).
            setTargetUdt(udtRef).build()
        })

        delMods ++ newMods ++ updMods
      }).flatten

    updates
  }

}
