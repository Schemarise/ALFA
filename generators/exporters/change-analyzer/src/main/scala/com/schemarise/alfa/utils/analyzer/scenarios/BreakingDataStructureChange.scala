package com.schemarise.alfa.utils.analyzer.scenarios

import com.schemarise.alfa.compiler.ast.UdtVersionedName
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types.{IUdtDataType, UdtType}
import com.schemarise.alfa.compiler.ast.model.{ITrait, IUdtVersionName}
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.tools.graph.Vertex
import com.schemarise.alfa.utils.analyzer.{ChangeAnalyzer, CompilationUnitChangeSet, DiffResults}

import java.util.Optional
import scala.collection.mutable.ListBuffer
import schemarise.alfa.runtime.model.diff._

object BreakingDataStructureChange {

  def isTargetUdtType(t: UdtType): Boolean =
    t == UdtType.record || t == UdtType.union || t == UdtType.entity || t == UdtType.key || t == UdtType.`trait` || t == UdtType.enum || t == UdtType.annotation

  def run(changeSet: CompilationUnitChangeSet): List[IUdtModification] = {
    val v1UdtNames = changeSet.v1.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))
    val v2UdtNames = changeSet.v2.getUdtVersionNames().filter(e => isTargetUdtType(e.udtType))

    val diffs = new DiffResults[IUdtVersionName](v1UdtNames, v2UdtNames)
    val unchanged = diffs.unchanged

    val udtDeletions = diffs.deleted.flatMap(e => {
      val ut = ChangeAnalyzer.toChangeUdtType(e.udtType)
      ChangeAnalyzer.toUdtModifications(List(e), ut, EditType.Removed, ChangeCategoryType.BreakingDataStructureChange)
    }).toList

    val fieldMods =
      unchanged.flatMap(e => {
        val v1Udt = changeSet.v1.getUdt(e.fullyQualifiedName).get
        val v2Udt = changeSet.v2.getUdt(e.fullyQualifiedName).get

        val ut = ChangeAnalyzer.toChangeUdtType(e.udtType)
        val udtRef = UdtReference.builder().setUdtName(e.fullyQualifiedName).setUdtType(ut).build()

        val v1Fields = v1Udt.allFields.keySet
        val v2Fields = v2Udt.allFields.keySet

        val scopeChanges: List[UdtEntryModification] =
          if (v1Udt.isTrait && v2Udt.isTrait) {
            val v1t = v1Udt.asInstanceOf[ITrait]
            val v2t = v2Udt.asInstanceOf[ITrait]
            val dr = new DiffResults[IUdtDataType](v1t.scope.toSet, v2t.scope.toSet)

            val beforeSnippet = Optional.of(Snippet.builder().setCode(v1t.scope.mkString(",")).build())
            val afterSnippet = Optional.of(Snippet.builder().setCode(v2t.scope.mkString(",")).build())

            val scopeAdditions = dr.added.flatMap(a => {
              ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a.fullyQualifiedName), UdtEntryType.Scope,
                ut, EditType.Added, ChangeCategoryType.BreakingDataStructureChange, beforeSnippet, afterSnippet)
            }).toList

            val scopeRemovals = dr.deleted.flatMap(a => {
              ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a.fullyQualifiedName), UdtEntryType.Scope,
                ut, EditType.Removed, ChangeCategoryType.BreakingDataStructureChange, beforeSnippet, afterSnippet)
            }).toList

            scopeRemovals ++ scopeAdditions
          }
          else {
            List.empty
          }

        val fieldsDiff = new DiffResults[String](v1Fields, v2Fields)

        val additions = fieldsDiff.added.map(a => {
          val addedField = v2Udt.allFields.get(a).get
          val snippet = Optional.of(Snippet.builder().setCode(addedField.toString).build())

          if (addedField.dataType.isEncOptional() || e.udtType == UdtType.enum)
            ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a), UdtEntryType.Field,
              ut, EditType.Added, ChangeCategoryType.DataStructureUpsert, Optional.empty(), snippet)
          else
            ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a), UdtEntryType.Field, ut, EditType.Added,
              ChangeCategoryType.BreakingDataStructureChange, Optional.empty(), snippet)
        })

        val deletions = fieldsDiff.deleted.map(a => {
          val deletedField = v1Udt.allFields.get(a).get
          val snippet = Optional.of(Snippet.builder().setCode(deletedField.toString).build())

          ChangeAnalyzer.toUdtEntryModifications(udtRef, List(a), UdtEntryType.Field, ut, EditType.Removed,
            ChangeCategoryType.BreakingDataStructureChange, snippet, Optional.empty())
        })

        val typeChanged = fieldsDiff.unchanged.filter(f => {
          val a = v1Udt.allFields.get(f).get
          val b = v2Udt.allFields.get(f).get
          a.dataType.toString != b.dataType.toString
        }).map(f => {
          val a = v1Udt.allFields.get(f).get
          val b = v2Udt.allFields.get(f).get

          val snippet1 = Optional.of(Snippet.builder().setCode(a.toString).build())
          val snippet2 = Optional.of(Snippet.builder().setCode(b.toString).build())

          // to check if constraints have changed
          val aPlain = a.toString.replaceAll("\\((.*?)\\)", "")
          val bPlain = b.toString.replaceAll("\\((.*?)\\)", "")

          val cat = if (aPlain.equals(bPlain)) ChangeCategoryType.DataStructureUpsert else ChangeCategoryType.BreakingDataStructureChange

          ChangeAnalyzer.toUdtEntryModifications(udtRef, List(f), UdtEntryType.Field, ut, EditType.Updated, cat, snippet1, snippet2)
        })

        additions.flatten ++ deletions.flatten ++ typeChanged.flatten ++ scopeChanges
      }).toList

    val modifiedTypes = fieldMods.map(e => UdtVersionedName(name = com.schemarise.alfa.compiler.ast.nodes.StringNode.create(e.getTargetUdt.getUdtName))).toSet
    val unmodifiedTypes = v2UdtNames.filter(e => !modifiedTypes.contains(e.asInstanceOf[UdtVersionedName]))

    val indirectMods = new ListBuffer[UdtModification]
    unmodifiedTypes.foreach(start => {
      modifiedTypes.foreach(end => {
        if (!start.equals(end)) {
          val path = changeSet.v2.graph.shortestPath(
            new Vertex(changeSet.v2.getUdt(start.fullyQualifiedName).get),
            new Vertex(changeSet.v2.getUdt(end.fullyQualifiedName).get))

          if (path.size > 0) {
            val m = ChangeAnalyzer.toUdtModifications(start, ChangeUdtType.Record, EditType.Updated,
              ChangeCategoryType.IndirectBreakingDataStructureChange, None, None,
              Optional.of(path.map(x => x.node.asInstanceOf[UdtBaseNode].name.fullyQualifiedName).mkString(" > "))
            ).head

            indirectMods.append(m)
          }
        }
      })
    })


    val res = udtDeletions ++ fieldMods ++ indirectMods

    val breakingTypes = res.map(e => e.getTargetUdt.getUdtName)
    val apiMods = apiBreaks(changeSet, breakingTypes)

    res ++ apiMods
  }

  private def apiBreaks(changeSet: CompilationUnitChangeSet, breakingTypes: List[String]): List[IUdtModification] = {

    val v1UdtNames = changeSet.v1.getUdtVersionNames().filter(e => e.udtType == UdtType.service || e.udtType == UdtType.library)
    val v2UdtNames = changeSet.v2.getUdtVersionNames().filter(e => e.udtType == UdtType.service || e.udtType == UdtType.library)

    val d = new DiffResults[IUdtVersionName](v1UdtNames, v2UdtNames)

    val udts = d.unchanged.
      filter(f => f.udtType == UdtType.service || f.udtType == UdtType.library).
      map(e => changeSet.v2.getUdt(e.fullyQualifiedName).get)

    udts.map(u => {
      val ut = ChangeAnalyzer.toChangeUdtType(u.udtType)
      val udtRef = UdtReference.builder().setUdtName(u.name.fullyQualifiedName).setUdtType(ut).build()

      val mods: List[IUdtModification] = u.getMethodSignatures().
        map(m => {
          val breakingMethod = m._2.formals.find(f => f._2.dataType.isUdt && breakingTypes.contains(f._2.dataType.asInstanceOf[UdtDataType].fullyQualifiedName))

          if (breakingMethod.isDefined) {
            val fml = breakingMethod.get
            val fname = fml._1
            val fieldType = fml._2.dataType.asInstanceOf[UdtDataType].fullyQualifiedName

            val a = ChangeAnalyzer.toUdtEntryModifications(udtRef, List(m._1), UdtEntryType.Method, ut,
              EditType.Updated, ChangeCategoryType.BreakingApiChange, Optional.empty(),
              Optional.empty(), java.util.Optional.of(s"Structure of type '${fieldType}' for parameter '${fname}' has been modified"))
            a
          }
          else {
            List.empty[IUdtModification]
          }
        }).flatten.toList

      mods
    }).flatten.toList
  }

}
