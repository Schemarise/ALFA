package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.AlfaInternalException
import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.ast.{DocumentableNode, EmptyDocumentableNode}
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}

import java.util.Optional
import schemarise.alfa.runtime.model.diff._

object QualityImprovements {

  def isTargetUdtType(t: UdtType): Boolean =
    t == UdtType.record || t == UdtType.union || t == UdtType.entity || t == UdtType.key ||
      t == UdtType.`trait` || t == UdtType.enum || t == UdtType.annotation || t == UdtType.service

  def run(changeSet: CompilationUnitChangeSet): List[IModification] = {

    val namespaces = new DiffResults[String](changeSet.v1.getNamespaces.map(_.name).toSet, changeSet.v2.getNamespaces.map(_.name).toSet)

    val nsDocs = namespaces.unchanged.filter(ns => {
      changeSet.v1.getNamespaceMeta(ns).get.docs.mkString != changeSet.v2.getNamespaceMeta(ns).get.docs.mkString
    }).map(ns => {
      List(ChangeAnalyzer.toNamespaceModifications(ns,
        docMods(new EmptyDocumentableNode(changeSet.v1.getNamespaceMeta(ns).get), new EmptyDocumentableNode(changeSet.v2.getNamespaceMeta(ns).get)),
        ChangeCategoryType.DocumentationChanges,
        ChangeAnalyzer.toSnippet(changeSet.v1.getNamespaceMeta(ns).get), ChangeAnalyzer.toSnippet(changeSet.v2.getNamespaceMeta(ns).get))
      )
    }).flatten.toList

    val v1UdtNames = changeSet.v1.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))
    val v2UdtNames = changeSet.v2.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))

    val d = new DiffResults[IUdtVersionName](v1UdtNames, v2UdtNames)
    val unchanged = d.unchanged

    val mods = unchanged.map(e => {
      val v1Udt = changeSet.v1.getUdt(e.fullyQualifiedName).get
      val v2Udt = changeSet.v2.getUdt(e.fullyQualifiedName).get

      val ut = ChangeAnalyzer.toChangeUdtType(e.udtType)
      val udtRef = UdtReference.builder().setUdtName(e.name).setUdtType(ut).build()

      val testcaseChanges = new DiffResults[IUdtVersionName](v1Udt.getTestcases.map(_.name).toSet, v2Udt.getTestcases.map(_.name).toSet)
      val assertChanges = new DiffResults[String](v1Udt.allAsserts.map(_._1).toSet, v2Udt.allAsserts.map(_._1).toSet)

      val m0: List[IUdtModification] = docUpdates(ut, udtRef, v1Udt, v2Udt, ut)

      val asrtMods = assertChanges.added ++ assertChanges.deleted

      val m1: List[IUdtModification] = ChangeAnalyzer.toUdtModifications(testcaseChanges.added.toList, ChangeUdtType.Testcase, EditType.Added, ChangeCategoryType.QualityChanges)
      val m2: List[IUdtModification] = ChangeAnalyzer.toUdtModifications(testcaseChanges.deleted.toList, ChangeUdtType.Testcase, EditType.Removed, ChangeCategoryType.QualityChanges)

      val m3: List[IUdtModification] = assertChanges.added.map(a => {
        ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a), UdtEntryType.Assert, ut, EditType.Added, ChangeCategoryType.QualityChanges, Optional.empty(), Optional.empty())
      }).flatten.toList

      val m4: List[IUdtModification] = assertChanges.deleted.map(a => {
        ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a), UdtEntryType.Assert, ut, EditType.Removed, ChangeCategoryType.QualityChanges, Optional.empty(), Optional.empty())
      }).flatten.toList

      m0 ++ m1 ++ m2 ++ m3 ++ m4
    }).flatten.toList

    nsDocs ++ mods
  }

  def docUpdates(ct: ChangeUdtType, udtRef: UdtReference, v1Udt: UdtBaseNode, v2Udt: UdtBaseNode, ut: ChangeUdtType): List[IUdtModification] = {
    val m0: List[IUdtModification] =
      if (v1Udt.docs.mkString != v2Udt.docs.mkString) {
        ChangeAnalyzer.toUdtModifications(v1Udt.name, ut,
          docMods(v1Udt, v2Udt),
          ChangeCategoryType.DocumentationChanges, ChangeAnalyzer.toSnippet(v1Udt.docs), ChangeAnalyzer.toSnippet(v2Udt.docs))

      } else
        List.empty

    val fields = new DiffResults[String](v1Udt.allFields.keySet, v2Udt.allFields.keySet)
    val asserts = new DiffResults[String](v1Udt.allAsserts.keySet, v2Udt.allAsserts.keySet)
    val methods = new DiffResults[String](v1Udt.getMethodSignatures.keySet, v2Udt.getMethodSignatures.keySet)

    val m1: List[IUdtModification] = fields.unchanged.filter(e => {
      v1Udt.allFields.get(e).get.docs.mkString("") != v2Udt.allFields.get(e).get.docs.mkString("")
    }).map(e => {
      ChangeAnalyzer.toUdtEntryModifications(udtRef, List(e), UdtEntryType.Field, ct,
        docMods(v1Udt.allFields.get(e).get.asInstanceOf[DocumentableNode], v2Udt.allFields.get(e).get.asInstanceOf[DocumentableNode]),
        ChangeCategoryType.DocumentationChanges, ChangeAnalyzer.toSnippet(v1Udt.allFields.get(e).get.docs), ChangeAnalyzer.toSnippet(v2Udt.allFields.get(e).get.docs))
    }).flatten.toList

    val m2: List[IUdtModification] = asserts.unchanged.filter(e => {
      v1Udt.allAsserts.get(e).get.docs.toString() != v2Udt.allAsserts.get(e).get.docs.toString()
    }).map(e => {
      ChangeAnalyzer.toUdtEntryModifications(udtRef, List(e), UdtEntryType.Assert, ct,
        docMods(v1Udt.allAsserts.get(e).get.asInstanceOf[DocumentableNode], v2Udt.allAsserts.get(e).get.asInstanceOf[DocumentableNode]),
        ChangeCategoryType.DocumentationChanges
        , ChangeAnalyzer.toSnippet(v1Udt.allAsserts.get(e).get.docs), ChangeAnalyzer.toSnippet(v2Udt.allAsserts.get(e).get.docs)
      )
    }).flatten.toList

    val m3: List[IUdtModification] = methods.unchanged.filter(e => {
      v1Udt.getMethodSignatures().get(e).get.docs.toString() != v2Udt.getMethodSignatures.get(e).get.docs.toString()
    }).map(e => {
      ChangeAnalyzer.toUdtEntryModifications(udtRef, List(e), UdtEntryType.Method, ct,
        docMods(v1Udt.getMethodSignatures.get(e).get.asInstanceOf[DocumentableNode], v2Udt.getMethodSignatures.get(e).get.asInstanceOf[DocumentableNode]),
        ChangeCategoryType.DocumentationChanges
        , ChangeAnalyzer.toSnippet(v1Udt.getMethodSignatures.get(e).get.docs), ChangeAnalyzer.toSnippet(v2Udt.getMethodSignatures.get(e).get.docs)
      )
    }).flatten.toList


    m0 ++ m1 ++ m2 ++ m3
  }


  private def docMods(l: DocumentableNode, r: DocumentableNode): EditType = {
    if (l.docNodes.mkString == r.docNodes.mkString)
      throw new AlfaInternalException("Cannot match")
    else if (l.docNodes.size == 0)
      EditType.Added
    else if (r.docNodes.size == 0)
      EditType.Removed
    else
      EditType.Updated
  }
}
