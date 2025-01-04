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
package com.schemarise.alfa.compiler.ast.nodes

import org.jgrapht.DirectedGraph

object GraphUtils {
  def safeAddEdge[V, E](g: DirectedGraph[V, E], f: V, t: V) = {
    if (!g.containsVertex(f))
      g.addVertex(f)

    if (!g.containsVertex(t))
      g.addVertex(t)

    g.addEdge(f, t)
  }

  def safeAddEdge[V, E](g: DirectedGraph[V, E], f: V, t: V, e: E) = {
    if (!g.containsVertex(f))
      g.addVertex(f)

    if (!g.containsVertex(t))
      g.addVertex(t)

    g.addEdge(f, t, e)
  }

}
