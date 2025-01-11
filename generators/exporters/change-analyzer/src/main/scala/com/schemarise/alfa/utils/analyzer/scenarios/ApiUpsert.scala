package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}
import schemarise.alfa.runtime.model.diff._

object ApiUpsert {

  def run(changeSet: CompilationUnitChangeSet): List[IUdtModification] = {
    val v1LibNames = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.library)
    val v2LibNames = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.library)
    val libsDiff = new DiffResults[IUdtVersionName](v1LibNames, v2LibNames)

    val v1SrvNames = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.service)
    val v2SrvNames = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.service)
    val srvsDiff = new DiffResults[IUdtVersionName](v1SrvNames, v2SrvNames)

    val m1 = ChangeAnalyzer.toUdtModifications(libsDiff.added.toList, ChangeUdtType.Library, EditType.Added, ChangeCategoryType.ApiUpsert)
    val m2 = ChangeAnalyzer.toUdtModifications(srvsDiff.added.toList, ChangeUdtType.Service, EditType.Added, ChangeCategoryType.ApiUpsert)

    // Captured as Potential API change - otherwise adding new Library method doubles the mod count

    //    val m3 = libsDiff.unchanged.map( e => {
    //      val v1Lib = changeSet.v1.getUdt(e.fullyQualifiedName).get.asInstanceOf[Library]
    //      val v2Lib = changeSet.v2.getUdt(e.fullyQualifiedName).get.asInstanceOf[Library]
    //
    //      val libMethodsV1 = v1Lib.getMethodDecls().keySet
    //      val libMethodsV2 = v2Lib.getMethodDecls().keySet
    //      val dif = new DiffResults[String](libMethodsV1, libMethodsV2)
    //
    //      val udtRef = UdtReference.builder().setUdtName(e.fullyQualifiedName).setUdtType(ChangeUdtType.Library).build()
    //
    //      val m1 = ChangeAnalyzer.toUdtEntryModifications( udtRef, dif.added.toList, UdtEntryType.Method,
    //        ChangeUdtType.Library, EditType.Added, ChangeCategoryType.ApiUpsert, Optional.empty(), Optional.empty())
    //
    //      m1
    //    }).flatten

    m1 ++ m2
  }


}
