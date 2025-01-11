package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.CompilationUnitArtifact
import com.schemarise.alfa.compiler.ast.UdtVersionedName
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.{IUdtDataType, UdtType}
import com.schemarise.alfa.compiler.ast.model.{IField, IUdtVersionName, NoOpNodeVisitor}
import com.schemarise.alfa.compiler.ast.nodes.Service
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet}

import scala.collection.mutable.ListBuffer
import schemarise.alfa.runtime.model.diff._

object BreakingApiChanges {

  def run(changeSet: CompilationUnitChangeSet, udtChanges: List[IUdtModification]): List[IUdtModification] = {
    val v1ServicesNames = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.service)
    val v2ServicesNames = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.service)

    val dels = deletedLibOrSrv(changeSet, v1ServicesNames, v2ServicesNames)
    val changes = modificationBaseChanges(changeSet, v1ServicesNames, v2ServicesNames)

    val udtImpact = servicesAffectedByReachableUdtChange(v2ServicesNames, changeSet.v2, udtChanges).toSet

    // deletions of types completely and changes to method signatures break APIs
    dels ++ changes ++ udtImpact
  }

  private def servicesAffectedByReachableUdtChange(svcs: Set[IUdtVersionName],
                                                   v2: CompilationUnitArtifact,
                                                   udtChanges: List[IUdtModification]): List[IUdtModification] = {

    val modifiedUdtNames = udtChanges.map(e => e.getTargetUdt.getUdtName).toSet


    val lb = new ListBuffer[IUdtModification]()
    val visited = ListBuffer[String]()

    svcs.foreach(s => {
      val svc = v2.getUdt(s.fullyQualifiedName).get

      svc.traverse(new NoOpNodeVisitor() {

        override def enter(e: IUdtDataType): Mode = {
          val res = super.enter(e)

          val n = e.fullyQualifiedName
          checkUsage(n)

          if (!visited.contains(n)) {
            visited.append(n)
            e.udt.traverse(this)
          }

          res
        }

        private def checkUsage(e: String): Unit = {

          if (modifiedUdtNames.contains(e)) {
            val path = pathEntries()
            val firstType = path.find(e => e.isInstanceOf[IUdtDataType])

            val pathStr = List(firstType.get.asInstanceOf[IUdtDataType].fullyQualifiedName) ++
              path.filter(e => e.isInstanceOf[IField]).map(e => e.asInstanceOf[IField].name)

            val d =
              UdtEntryModification.builder().
                setEditType(EditType.Updated).
                setChangeCategory(ChangeCategoryType.BreakingApiChange).
                setEntryName(e).
                setEntryType(UdtEntryType.ReachableType).
                setMessage("Path: " + pathStr.mkString(", ")).
                setTargetUdt(toUdtReference(s)).build()
            lb.append(d)
          }
        }
      })
    })

    lb.toList
  }

  private def deletedLibOrSrv(changeSet: CompilationUnitChangeSet, v1ServicesNames: Set[IUdtVersionName], v2ServicesNames: Set[IUdtVersionName]): List[IUdtModification] = {
    val deletedSrv = v1ServicesNames.filter(e => !v2ServicesNames.contains(e))

    val sdel = deletedSrv.map(d => {
      val udtRef = toUdtReference(d)

      UdtModification.builder().
        setEditType(EditType.Removed).
        setTargetUdt(udtRef).
        setBeforeSnippet(ChangeAnalyzer.toSnippet(d.asInstanceOf[UdtVersionedName])).
        setChangeCategory(ChangeCategoryType.BreakingApiChange).build()
    }).toList

    val v1Libs = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.library)
    val v2Libs = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.library)

    val deletedLibs = v1Libs.filter(e => !v2Libs.contains(e))

    val ldel = deletedLibs.map(d => {
      val udtRef = UdtReference.builder().setUdtName(d.fullyQualifiedName).setUdtType(ChangeUdtType.Library).build()

      UdtModification.builder().
        setEditType(EditType.Removed).
        setTargetUdt(udtRef).
        setBeforeSnippet(ChangeAnalyzer.toSnippet(d.asInstanceOf[UdtVersionedName])).
        setChangeCategory(ChangeCategoryType.BreakingApiChange).build()
    }).toList

    sdel ++ ldel
  }

  private def toUdtReference(d: IUdtVersionName) = {
    val t = d.udtType match {
      case UdtType.service => ChangeUdtType.Service
      case UdtType.library => ChangeUdtType.Library
    }
    UdtReference.builder().setUdtName(d.fullyQualifiedName).setUdtType(t).build()
  }

  private def modificationBaseChanges(changeSet: CompilationUnitChangeSet,
                                      v1ServicesNames: Set[IUdtVersionName],
                                      v2ServicesNames: Set[IUdtVersionName]): List[IUdtModification] = {
    val v1CommonNames = v1ServicesNames.filter(e => v2ServicesNames.contains(e))

    val v1CommonServices = v1CommonNames.map(e => changeSet.v1.getUdt(e.fullyQualifiedName).
      get.asInstanceOf[Service]).toList.sortBy(e => e.name.fullyQualifiedName)

    val v2CommonServices = v1CommonNames.map(e => changeSet.v2.getUdt(e.fullyQualifiedName).
      get.asInstanceOf[Service]).toList.sortBy(e => e.name.fullyQualifiedName)

    val zipped = v1CommonServices.zip(v2CommonServices)
    val updates = methodChangeDrivenResults(zipped)

    updates
  }

  private def methodChangeDrivenResults(zipped: List[(Service, Service)]): List[IUdtModification] = {
    val updates: List[UdtEntryModification] =
      zipped.map(e => {
        val m1 = e._1.getMethodSignatures()
        val m2 = e._2.getMethodSignatures()

        val matchingMethods = m1.keySet.map(n => (m1.get(n), m2.get(n))).filter(e => e._2.isDefined).map(e => (e._1.get, e._2.get))

        val deletedMethods = m1.keySet.filter(k => !m2.keySet.contains(k))
        val addedMethods = m2.keySet.filter(k => !m1.keySet.contains(k))
        val changedMethods = matchingMethods.filter(e => e._1.toString != e._2.toString)

        val udtRef = UdtReference.builder().setUdtName(e._1.name.fullyQualifiedName).setUdtType(ChangeUdtType.Service).build()

        val delMods = deletedMethods.map(name => {
          val m = m1.get(name).get
          UdtEntryModification.builder().
            setEditType(EditType.Removed).
            setBeforeSnippet(ChangeAnalyzer.toSnippet(m)).
            setChangeCategory(ChangeCategoryType.BreakingApiChange).
            setEntryName(name).
            setEntryType(UdtEntryType.Method).
            setTargetUdt(udtRef).build()
        })

        val newMods = addedMethods.map(name => {
          val m = m2.get(name).get
          UdtEntryModification.builder().
            setEditType(EditType.Added).
            setAfterSnippet(ChangeAnalyzer.toSnippet(m)).
            setChangeCategory(ChangeCategoryType.BreakingApiChange).
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
            setChangeCategory(ChangeCategoryType.BreakingApiChange).
            setEntryName(left.name.fullyQualifiedName).
            setEntryType(UdtEntryType.Method).
            setTargetUdt(udtRef).build()
        })

        delMods ++ newMods ++ updMods
      }).flatten

    updates
  }
}
