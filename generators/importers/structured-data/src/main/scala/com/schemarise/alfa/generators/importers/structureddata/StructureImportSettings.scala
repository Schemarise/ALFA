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

package com.schemarise.alfa.generators.importers.structureddata

import com.schemarise.alfa.compiler.AlfaInternalException
import com.schemarise.alfa.compiler.utils.TextUtils
import com.schemarise.alfa.runtime.AlfaRuntimeException

import java.time.format.DateTimeFormatter

object StructureImportSettings {
  val encoding = "encoding"
  val dateformat = "dateFormat"
  val timeformat = "timeFormat"
  val datetimeFormat = "datetimeFormat"
  val namespace = "namespace"
  val typename = "typename"
  val typenameField = "typenameField"
  val enumUniqueValueLimit = "enumUniqueValueLimit"
  val csvDelimiter = "csvDelimiter"
  val csvMaxColumns = "csvMaxColumns"
  val csvMaxRows = "csvMaxRows"
  val csvMaxCharsPerColumn = "csvMaxCharsPerColumn"
  val csvLineSeparator = "csvLineSeparator"

  val all = Set( encoding, dateformat, timeformat, datetimeFormat, namespace, typename, typenameField, enumUniqueValueLimit,
    csvDelimiter, csvMaxColumns, csvMaxCharsPerColumn, csvMaxRows, csvLineSeparator)
}
class StructureImportSettings(config: java.util.Map[String, Object], outputFilename : String) {

  config.keySet().forEach( k => {
    if ( !StructureImportSettings.all.contains(k) ) {
      throw new AlfaRuntimeException(s"Unknown setting ${k}")
    }
  })

  def csvDelimiter : String = {
    val v = read(StructureImportSettings.csvDelimiter, ",")
    v
  }

  def lineSeparator : String = {
    val v = read(StructureImportSettings.csvLineSeparator, "\n")
    v
  }

  def encoding : String = {
    val v = read(StructureImportSettings.encoding, "UTF-8")
    v
  }

  def csvMaxRows : Int = {
    val v = read(StructureImportSettings.csvMaxRows, "100000")
    Integer.parseInt(v)
  }

  def csvMaxColumns : Int = {
    val v = read(StructureImportSettings.csvMaxColumns, "1024")
    Integer.parseInt(v)
  }

  def csvMaxCharsPerColumn : Int = {
    val v = read(StructureImportSettings.csvMaxCharsPerColumn, "512")
    Integer.parseInt(v)
  }
  def dateFormat = {
    val v = read(StructureImportSettings.dateformat)
    if ( v == null )
      DateTimeFormatter.ISO_DATE
    else
      DateTimeFormatter.ofPattern(v)
  }

  def timeFormat = {
    val v = read(StructureImportSettings.timeformat)
    if ( v == null )
      DateTimeFormatter.ISO_TIME
    else
      DateTimeFormatter.ofPattern(v)
  }

  def datetimeFormat = {
    val v = read(StructureImportSettings.datetimeFormat)
    if ( v == null )
      DateTimeFormatter.ISO_DATE_TIME
    else
      DateTimeFormatter.ofPattern(v)
  }

  def namespace = {
    read(StructureImportSettings.namespace, "default_namespace")
  }

  def typename = {
    val tn = TextUtils.escapeJava(outputFilename)
    read(StructureImportSettings.typename, tn)
  }

  def typenameField = {
    val v = read(StructureImportSettings.typenameField)
    if ( v == null)
      None
    else
      Some(v)
  }

  def enumUniqueValueLimit = {
    Integer.parseInt( read(StructureImportSettings.enumUniqueValueLimit, "20") )
  }

  private def read( k : String, dft : String = null ) = {
    val v = config.getOrDefault(k, dft)
    if ( v != null )
      v.toString
    else
      dft
  }

}
