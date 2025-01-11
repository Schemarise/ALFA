package com.schemarise.alfa.utils.analyzer

import com.schemarise.alfa.compiler.ast.model.IUdtVersionName
import com.schemarise.alfa.compiler.ast.model.types.UdtType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.utils.analyzer.scenarios._
import schemarise.alfa.runtime.model.diff._

import java.util
import java.util.{Collections, Optional}
import scala.collection.JavaConverters._

object ChangeAnalyzer {
  def toChangeUdtType(udtType: UdtType): ChangeUdtType = {
    udtType match {
      case UdtType.`trait` => ChangeUdtType.Trait
      case UdtType.record => ChangeUdtType.Record
      case UdtType.union => ChangeUdtType.Union
      case UdtType.entity => ChangeUdtType.Entity
      case UdtType.key => ChangeUdtType.Key
      case UdtType.library => ChangeUdtType.Library
      case UdtType.testcase => ChangeUdtType.Testcase
      case UdtType.transform => ChangeUdtType.Transform
      case UdtType.service => ChangeUdtType.Service
      case UdtType.annotation => ChangeUdtType.Annotation
      case UdtType.enum => ChangeUdtType.Enum
      case UdtType.nativeUdt => ChangeUdtType.NativeType
      case UdtType.dataproduct => ChangeUdtType.Dataproduct
      case _ => throw new RuntimeException("Unhandled ChangeAnalyzer Type " + udtType)
    }
  }

  def toSnippet(m2: Any): Optional[Snippet] = {
    if (m2.isInstanceOf[Locatable]) {
      val code = m2.toString
      Optional.of(Snippet.builder().setCode(code).
        build())
    }
    else if (m2.isInstanceOf[java.util.List[_]]) {
      val m = m2.asInstanceOf[java.util.List[_]].asScala
      if (m.size == 0)
        Optional.empty()
      else
        Optional.of(Snippet.builder().setCode(m.mkString(";")).build())
    }
    else if (m2.isInstanceOf[Iterable[_]]) {
      val m = m2.asInstanceOf[Iterable[_]]

      if (m.size == 0)
        Optional.empty()
      else
        Optional.of(Snippet.builder().setCode(m.mkString(";")).build())
    }
    else if (m2 == null) {
      Optional.empty()
    }
    else {
      throw new RuntimeException()
    }
  }

  def toUdtEntryModifications(udt: UdtReference, udtEntryNames: List[String], uet: UdtEntryType,
                              ct: ChangeUdtType, et: EditType, cct: ChangeCategoryType,
                              before: java.util.Optional[Snippet], // = Optional.empty(),
                              after: java.util.Optional[Snippet], // = Optional.empty(),
                              message: java.util.Optional[String] = Optional.empty()
                             ) = {
    udtEntryNames.map(e => {
      UdtEntryModification.builder().setEditType(et).setChangeCategory(cct).
        setTargetUdt(udt).setEntryName(e).setEntryType(uet).
        setBeforeSnippet(before).
        setAfterSnippet(after).
        setMessage(message).
        build()
    })
  }

  def toNamespaceModifications(nsName: String, et: EditType, cct: ChangeCategoryType,
                               be: Optional[Snippet],
                               af: Optional[Snippet],
                              ) = {
    NamespaceModifications.builder().
      setEditType(et).
      setNamespaceName(nsName).
      setBeforeSnippet(be).
      setAfterSnippet(af).
      setChangeCategory(cct).build()
  }


  def toUdtModifications(e: IUdtVersionName, ct: ChangeUdtType, et: EditType, cct: ChangeCategoryType,
                         be: Optional[Snippet], af: Optional[Snippet]
                        ) = {

    val udtRef = UdtReference.builder().setUdtName(e.fullyQualifiedName).setUdtType(ct).build()
    val um = UdtModification.builder().
      setEditType(et).
      setBeforeSnippet(be).
      setAfterSnippet(af).
      setTargetUdt(udtRef).
      setChangeCategory(cct).build()

    List(um)
  }


  def toUdtReference(e: IUdtVersionName) = {
    UdtReference.builder().setUdtName(e.fullyQualifiedName).setUdtType(ChangeAnalyzer.toChangeUdtType(e.udtType)).build()
  }

  def toUdtModifications(e: IUdtVersionName, ct: ChangeUdtType, et: EditType, cct: ChangeCategoryType,
                         before: Option[String] = None, after: Option[String] = None,
                         msg: Optional[String] = Optional.empty()
                        ) = {

    val be: Optional[Snippet] = if (before.isDefined) Optional.of(Snippet.builder().setCode(before.get).build()) else Optional.empty()
    val af: Optional[Snippet] = if (after.isDefined) Optional.of(Snippet.builder().setCode(after.get).build()) else Optional.empty()

    val udtRef = toUdtReference(e)
    val um = UdtModification.builder().
      setEditType(et).
      setBeforeSnippet(be).
      setAfterSnippet(af).
      setTargetUdt(udtRef).
      setMessage(msg).
      setChangeCategory(cct).build()

    List(um)
  }

  def toUdtModifications(n: List[IUdtVersionName], ct: ChangeUdtType, et: EditType, cct: ChangeCategoryType) = {
    n.map(e => {
      val udtRef = UdtReference.builder().setUdtName(e.fullyQualifiedName).setUdtType(ct).build()

      UdtModification.builder().
        setEditType(et).
        setTargetUdt(udtRef).
        setChangeCategory(cct).build()
    })
  }
}

class ChangeAnalyzer {
  def analyzeVersions(changeSet: CompilationUnitChangeSet) = {
    val mods = new util.ArrayList[IModification]()

    mods.addAll(ApiUpsert.run(changeSet).asJava)

    val breakingDataStructs = BreakingDataStructureChange.run(changeSet)
    mods.addAll(breakingDataStructs.asJava)

    mods.addAll(BreakingApiChanges.run(changeSet, breakingDataStructs).asJava)

    mods.addAll(DataStructureUpsert.run(changeSet).asJava)
    mods.addAll(ModelMetadataChange.run(changeSet).asJava)
    mods.addAll(PotentialApiChange.run(changeSet).asJava)
    mods.addAll(QualityImprovements.run(changeSet).asJava)

    mods.addAll(DataProductChange.run(changeSet, mods).asJava)

    val b = Modifications.builder()

    b.putAllResults(Collections.emptyMap())
    mods.asScala.groupBy(e => e.getChangeCategory).map(g => {
      b.putResults(g._1, g._2.toList.asJava)
    })

    b.build()
  }
}
