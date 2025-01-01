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
package com.schemarise.alfa.compiler.tools.tabular

import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.model.types.tabular._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType
import com.schemarise.alfa.compiler.tools.tabular.ColumnMappingType.ColumnMappingType

import java.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class Tablular(val udt: IUdtBaseNode) extends ITabular {
  private var longColumnNames = false

  def fullyExpandColumns(b: Boolean) = longColumnNames = b


  private val cols = new mutable.ListBuffer[IColumn]()
  private val names = new util.HashSet[String]()

  // FIXME State of cols being added after allColumns read
  lazy val allColumns = cols.toList

  override def toString: String = {
    val sb = new StringBuilder

    sb.append(s"tablular ${udt.name.fullyQualifiedName} {\n")
    cols.foreach(c => sb.append(s"  ${c.toString}\n"))
    sb.append(s"}\n")

    sb.toString()
  }

  def primaryKeyColumns(): List[IColumn] = {
    allColumns.filter(c => c.isKey)
  }

  def dataColumns(): List[IColumn] = {
    allColumns.filter(c => !c.isKey)
  }

  def createScalarColumn(path: List[IColumnPathEntry], dataType: ScalarDataType, isKey: Boolean, descr: String): Unit = {
    val n = makeName(path, "", longColumnNames)
    names.add(n)

    cols += new ScalarColumn(path, dataType, isKey, n, descr)
  }

  def createMetaColumn(path: List[IColumnPathEntry], mapping: ColumnMappingType, isKey: Boolean, descr: String): Unit = {
    val n = makeName(path, mapping.toString, longColumnNames)
    names.add(n)

    cols += new MetaColumn(path, mapping, isKey, n, descr)
  }

  private def makeName(path: List[IColumnPathEntry], suffix: String, longName: Boolean): String = {
    val l = path.
      map(_.name(longName)).
      foldRight(new ListBuffer[String]()) {
        (s, list) => {
          val accum = if (list.lastOption.isDefined) "...." + list.last else ""
          val joint = s + accum
          list += joint
        }
      }.
      map(e => {
        // val sep = if (e.length > 0 && suffix.length > 0) "..." else ""
        //        e + sep + suffix
        e + suffix
      })

    val ho = l.filter(e => !names.contains(e)).headOption

    val unique =
      if (ho.isDefined)
        ho.get
      else if (suffix.length > 0)
        suffix
      else {
        return "undefinedColumnName";
      }

    unique
  }
}