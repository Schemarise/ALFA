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
package com.schemarise.alfa.compiler

import com.schemarise.alfa.compiler.ast.model.IdentifiableNode
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode

object TraverseUtils {

  def onlyUdts(l: Seq[IdentifiableNode]) = l.filter(_.isInstanceOf[UdtBaseNode]).map(_.asInstanceOf[UdtBaseNode])

  //  def dependencyOrderedUdts(): List[UdtBaseNode] = {
  //    directedGraph.udtDependencyOrdered
  //  }

  //  def topologicallyOrderedUdts(): Seq[UdtBaseNode] = {
  //    directedGraph.topologicallyOrdered.filter(_.node.isInstanceOf[UdtBaseNode]).map(_.node.asInstanceOf[UdtBaseNode])
  //  }


}