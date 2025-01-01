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
package com.schemarise.alfa.compiler.tools.graph.d3

import scala.collection.mutable.ListBuffer

case class D3SupportModel(rootPath: Seq[String], isNamespace: Boolean = true)(val totalTypes: Int) {

  private val childNss = new ListBuffer[D3SupportModel]()

  def children = childNss.toList.sortBy(e => e.nameOnly)

  def depth = rootPath.size

  def isChildOf(rhs: D3SupportModel): Boolean = {
    val parentNs = rootPath.dropRight(1).mkString(".")
    val rhsNs = rhs.rootPath.mkString(".")
    parentNs == rhsNs
  }

  def addChildD3Model(d: D3SupportModel) = {
    childNss.append(d)
  }

  def nameOnly = rootPath.last


  def fqn(del: String = ".") = rootPath.mkString(del)

  def id = fqn("_")
}
