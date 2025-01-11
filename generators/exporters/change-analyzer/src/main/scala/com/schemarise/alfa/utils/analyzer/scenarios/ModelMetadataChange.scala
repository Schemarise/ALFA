package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.{IMethodSignature, IUdtVersionName}
import com.schemarise.alfa.compiler.ast.nodes.{Annotation, Field, UdtBaseNode}
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}

import java.util.Optional
import schemarise.alfa.runtime.model.diff._

object ModelMetadataChange {

  def isTargetUdtType(t: UdtType): Boolean =
    t == UdtType.record || t == UdtType.union || t == UdtType.entity || t == UdtType.key ||
      t == UdtType.`trait` || t == UdtType.enum || t == UdtType.annotation || t == UdtType.service

  def run(changeSet: CompilationUnitChangeSet): List[IModification] = {

    val namespaces = new DiffResults[String](changeSet.v1.getNamespaces.map(_.name).toSet, changeSet.v2.getNamespaces.map(_.name).toSet)

    val nsMetaUpd = namespaces.unchanged.filter(ns => {
      changeSet.v1.getNamespaceMeta(ns).get.annotations.mkString != changeSet.v2.getNamespaceMeta(ns).get.annotations.mkString
    }).map(e => {
      List(ChangeAnalyzer.toNamespaceModifications(e, EditType.Updated, ChangeCategoryType.ModelMetadataChange,
        makeSnippet(changeSet.v1.getNamespaceMeta(e).get.annotations.toList),
        makeSnippet(changeSet.v2.getNamespaceMeta(e).get.annotations.toList),
      ))
    }).flatten.toList


    val v1UdtNames = changeSet.v1.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))
    val v2UdtNames = changeSet.v2.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))

    val d = new DiffResults[IUdtVersionName](v1UdtNames, v2UdtNames)
    val unchanged = d.unchanged

    val mods = unchanged.map(e => {
      val v1Udt = changeSet.v1.getUdt(e.fullyQualifiedName).get
      val v2Udt = changeSet.v2.getUdt(e.fullyQualifiedName).get

      val v1a = v1Udt.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)
      val v2a = v2Udt.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)

      val udtAnns = if (v1a.toString() != v2a.toString()) {
        makeUdtAnnonDiffs(v1Udt, v1a, v2a)
      }
      else {
        List.empty[IUdtModification]
      }

      val md = new DiffResults[String](v1Udt.getMethodSignatures().keySet, v2Udt.getMethodSignatures.keySet)
      val methodMods: List[IUdtModification] = methodAnnMods(md, v1Udt, v2Udt, v1a, v2a)


      val d = new DiffResults[String](v1Udt.allFields.keySet, v2Udt.allFields.keySet)
      val fieldMods: List[IUdtModification] = fieldAnnMods(d, v1Udt, v2Udt, v1a, v2a)

      udtAnns ++ fieldMods ++ methodMods
    }).flatten.toList

    mods ++ nsMetaUpd
  }

  def methodAnnMods(d: DiffResults[String], v1Udt: UdtBaseNode, v2Udt: UdtBaseNode, v1a: List[Annotation], v2a: List[Annotation]): List[IUdtModification] = {
    val fieldMods: List[IUdtModification] =
      d.unchanged.map(fp => {
        val f1 = v1Udt.getMethodSignatures.get(fp).get
        val f2 = v2Udt.getMethodSignatures.get(fp).get

        val v1fa = f1.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)
        val v2fa = f2.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)

        val udtAnns = if (v1fa.toString() != v2fa.toString()) {
          makeUdtMethodAnnonDiffs(v1Udt, f1, v1a, v2a)
        }
        else {
          List.empty[IUdtModification]
        }

        udtAnns
      }).flatten.toList

    fieldMods
  }

  def fieldAnnMods(d: DiffResults[String], v1Udt: UdtBaseNode, v2Udt: UdtBaseNode, v1a: List[Annotation], v2a: List[Annotation]): List[IUdtModification] = {
    val fieldMods: List[IUdtModification] =
      d.unchanged.map(fp => {
        val f1 = v1Udt.allFields.get(fp).get
        val f2 = v2Udt.allFields.get(fp).get

        val v1fa = f1.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)
        val v2fa = f2.annotations.toList.sortBy(a => a.versionedName.fullyQualifiedName)

        val udtAnns = if (v1fa.toString() != v2fa.toString()) {
          makeUdtFieldAnnonDiffs(v1Udt, f1, v1a, v2a)
        }
        else {
          List.empty[IUdtModification]
        }

        udtAnns
      }).flatten.toList

    fieldMods
  }


  def makeUdtMethodAnnonDiffs(v1Udt: UdtBaseNode, fld: IMethodSignature, v1a: List[Annotation],
                              v2a: List[Annotation]): List[IUdtModification] = {

    val ut = ChangeAnalyzer.toChangeUdtType(v1Udt.udtType)
    val udtRef = UdtReference.builder().setUdtName(v1Udt.name.fullyQualifiedName).setUdtType(ut).build()

    val l = UdtEntryModification.builder()
      .setChangeCategory(ChangeCategoryType.ModelMetadataChange)
      .setEditType(EditType.Updated)
      .setTargetUdt(udtRef)
      .setEntryName(fld.name.fullyQualifiedName)
      .setEntryType(UdtEntryType.Method)
      .setBeforeSnippet(makeSnippet(v1a))
      .setAfterSnippet(makeSnippet(v2a))
      .build()

    List(l)
  }

  def makeUdtFieldAnnonDiffs(v1Udt: UdtBaseNode, fld: Field, v1a: List[Annotation],
                             v2a: List[Annotation]): List[IUdtModification] = {

    val ut = ChangeAnalyzer.toChangeUdtType(v1Udt.udtType)
    val udtRef = UdtReference.builder().setUdtName(v1Udt.name.fullyQualifiedName).setUdtType(ut).build()

    val l = UdtEntryModification.builder()
      .setChangeCategory(ChangeCategoryType.ModelMetadataChange)
      .setEditType(EditType.Updated)
      .setTargetUdt(udtRef)
      .setEntryName(fld.name)
      .setEntryType(UdtEntryType.Field)
      .setBeforeSnippet(makeSnippet(v1a))
      .setAfterSnippet(makeSnippet(v2a))
      .build()

    List(l)
  }

  def makeUdtAnnonDiffs(v1Udt: UdtBaseNode, v1a: List[Annotation],
                        v2a: List[Annotation]): List[IUdtModification] = {

    val ut = ChangeAnalyzer.toChangeUdtType(v1Udt.udtType)
    val udtRef = UdtReference.builder().setUdtName(v1Udt.name.fullyQualifiedName).setUdtType(ut).build()

    val l = UdtModification.builder()
      .setChangeCategory(ChangeCategoryType.ModelMetadataChange)
      .setEditType(EditType.Updated)
      .setTargetUdt(udtRef)
      .setBeforeSnippet(makeSnippet(v1a))
      .setAfterSnippet(makeSnippet(v2a))
      .build()

    List(l)
  }

  def makeSnippet(l: List[Annotation]): Optional[Snippet] = {
    if (l.size > 0) {
      Optional.of(Snippet.builder().setCode(l.mkString("\n")).build())
    }
    else {
      Optional.empty()
    }
  }
}
