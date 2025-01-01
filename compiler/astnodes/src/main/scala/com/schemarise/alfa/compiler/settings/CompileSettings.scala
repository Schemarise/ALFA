/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler.settings

import com.schemarise.alfa.compiler.err.CompilerSettingsException
import com.schemarise.alfa.compiler.settings.CompileSettingsFlags.FlagSettings
import com.schemarise.alfa.compiler.types.AllFieldTypes.FieldType
import com.schemarise.alfa.compiler.types.ManagedNodes.ManagedNodeType
import com.schemarise.alfa.compiler.types.{AllFieldTypes, ManagedNodes}

import scala.collection.mutable

class CompileSettings(
                       private val booleanFlags: Map[FlagSettings, Boolean] = Map.empty,
                       val DisallowedDeclarations: Option[Set[ManagedNodeType]] = None,
                       val DisallowedFieldTypes: Option[Set[FieldType]] = None,
                       val IncludesFilter: Option[Set[String]] = None,
                     ) {
  val EnforceTypeToFilenameMatch: Boolean = readFlag(CompileSettingsFlags.EnforceTypeToFilenameMatch)
  val DisallowInclude: Boolean = readFlag(CompileSettingsFlags.DisallowInclude)
  val DisallowCycles: Boolean = readFlag(CompileSettingsFlags.DisallowCycles)
  val DisallowNamespaceCycles: Boolean = readFlag(CompileSettingsFlags.AllowNamespaceCycles)
  val DisallowPolymorphicFunctions: Boolean = readFlag(CompileSettingsFlags.DisallowPolymorphicFunctions)
  val DisallowNonSelfCyclicDeclarations: Boolean = readFlag(CompileSettingsFlags.DisallowNonSelfCyclicDeclarations)
  val DisallowVersionedDeclarations: Boolean = readFlag(CompileSettingsFlags.DisallowVersionedDeclarations)
  val DisallowUnionDuplicateTypeField: Boolean = readFlag(CompileSettingsFlags.DisallowUnionDuplicateTypeField)
  val DisallowNonScalarKeyFields: Boolean = readFlag(CompileSettingsFlags.DisallowNonScalarKeyFields)
  val DisallowFloatingPointScalarKeyFields: Boolean = readFlag(CompileSettingsFlags.DisallowFloatingPointScalarKeyFields)
  val NamespaceRequired: Boolean = readFlag(CompileSettingsFlags.NamespaceRequired)
  val IgnoreWarnings: Boolean = readFlag(CompileSettingsFlags.IgnoreWarnings)
  // val AutoUnwrapOptionals: Boolean = readFlag(CompileSettingsFlags.AutoUnwrapOptionals)

  def readFlag(f: CompileSettingsFlags.FlagSettings): Boolean = if (booleanFlags.contains(f)) booleanFlags.get(f).get else false

  override def toString: String = {
    val sb = new mutable.StringBuilder()

    if (booleanFlags.size > 0)
      booleanFlags.foreach(b => {
        sb.append("  " + b._1.toString + ": " + b._2.toString)
      })

    if (DisallowedFieldTypes.isDefined) {
      sb.append("  " + CompileSettingsFlags.DisallowedFieldTypes.toString + ": " +
        DisallowedFieldTypes.get.mkString("", ",", ""))
    }

    if (IncludesFilter.isDefined) {
      sb.append("  " + CompileSettingsFlags.IncludesFilter.toString + ": " +
        IncludesFilter.get.mkString("", ",", ""))
    }

    if (DisallowedDeclarations.isDefined) {
      sb.append("  " + CompileSettingsFlags.DisallowedDeclarations.toString + ": " +
        DisallowedDeclarations.get.mkString("", ",", ""))
    }

    if (sb.length > 0)
      sb.insert(0, "compile:\n")

    sb.toString
  }
}

object CompileSettings {
  def fromConfig(settings: Map[String, Object]): CompileSettings = {

    settings.keySet.foreach(k => {
      if (!CompileSettingsFlags.withNameOpt(k).isDefined) {
        throw new CompilerSettingsException("Unknown compile setting specified '" + k + "'")
      }
    })

    val DisallowedDeclarations = settings.get(CompileSettingsFlags.DisallowedDeclarations.toString).flatMap(f => Some(f.toString.split(",").map(ManagedNodes.withEnumName(_)).toSet))
    val DisallowedFieldTypes = settings.get(CompileSettingsFlags.DisallowedFieldTypes.toString).flatMap(f => Some(f.toString.split(",").map(AllFieldTypes.withEnumName(_)).toSet))
    val IncludesFilter = settings.get(CompileSettingsFlags.IncludesFilter.toString).flatMap(f => Some(f.toString.split(",").toSet))

    val booleanFlags = new scala.collection.mutable.HashMap[CompileSettingsFlags.FlagSettings, Boolean]()

    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowInclude)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowCycles)
    readBool(booleanFlags, settings, CompileSettingsFlags.AllowNamespaceCycles)
    readBool(booleanFlags, settings, CompileSettingsFlags.EnforceTypeToFilenameMatch)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowVersionedDeclarations)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowPolymorphicFunctions)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowNonSelfCyclicDeclarations)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowUnionDuplicateTypeField)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowNonScalarKeyFields)
    readBool(booleanFlags, settings, CompileSettingsFlags.DisallowFloatingPointScalarKeyFields)
    readBool(booleanFlags, settings, CompileSettingsFlags.NamespaceRequired)
    readBool(booleanFlags, settings, CompileSettingsFlags.IgnoreWarnings)

    new CompileSettings(booleanFlags.toMap, DisallowedDeclarations, DisallowedFieldTypes, IncludesFilter)
  }


  def readBool(booleanFlags: mutable.HashMap[FlagSettings, Boolean], settings: Map[String, Object], f: CompileSettingsFlags.FlagSettings) = {

    try {
      val b = settings.get(f.toString).flatMap(f => Some(Boolean.unbox(f)))
      if (b.isDefined)
        booleanFlags.put(f, b.get)
    } catch {
      case e: Exception =>
        throw new CompilerSettingsException("Setting '" + f + "' should be set as true/false.")
    }
  }
}

object CompileSettingsFlags extends com.schemarise.alfa.compiler.SearchableEnumeration {
  type FlagSettings = Value

  val DisallowNonScalarKeyFields = Value("DisallowNonScalarKeyFields")
  val DisallowFloatingPointScalarKeyFields = Value("DisallowFloatingPointScalarKeyFields")
  val DisallowedDeclarations = Value("DisallowedDeclarations")
  val DisallowedFieldTypes = Value("DisallowedFieldTypes")

  val EnforceTypeToFilenameMatch = Value("EnforceTypeToFilenameMatch")

  val DisallowPolymorphicFunctions = Value("DisallowPolymorphicFunctions")
  val DisallowNonSelfCyclicDeclarations = Value("DisallowNonSelfCyclicDeclarations")
  val DisallowVersionedDeclarations = Value("DisallowVersionedDeclarations")


  val DisallowUnionDuplicateTypeField = Value("DisallowUnionDuplicateTypeField")

  val NamespaceRequired = Value("NamespaceRequired")

  val DisallowInclude = Value("DisallowInclude")
  val DisallowCycles = Value("DisallowCycles")
  val AllowNamespaceCycles = Value("AllowNamespaceCycles")

  val FormattedDocType = Value("FormattedDocType")

  val IncludesFilter = Value("IncludesFilter")
  val OutputPackagePath = Value("OutputPackagePath")

  val Dependencies = Value("Dependencies")

  val OutputPackage = Value("OutputPackage")

  val IgnoreWarnings = Value("IgnoreWarnings")

}