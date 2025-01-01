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
package com.schemarise.alfa.compiler.tools.graph

import com.schemarise.alfa.compiler.ast.model.graph.{Edges, IGraphEdge}

import java.util.function.Predicate

object IsDataTypeOrIncOrExtendsOrKeyEdgePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.UdtToFieldDataType ||
      t.getType == Edges.Includes ||
      t.getType == Edges.Extends ||
      //      t.getType == Edges.EntityToUDTKey ||
      t.getType == Edges.EntityToDirectKey
}

object IsFieldDataTypePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.UdtToFieldDataType
}


object IsKeyEdgePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.EntityToUDTKey
}

object IsEntityKeyEdgePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.EntityToDirectKey
}

class IsImmediateDataTypeOrIncOrExtendsOrKeyEdgePredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.UdtToFieldDataType ||
      t.getType == Edges.Includes ||
      t.getType == Edges.Extends ||
      //      t.getType == Edges.EntityToUDTKey
      t.getType == Edges.EntityToDirectKey

    if (d)
      t.getTgt.equals(tgt.node) || t.getSrc.equals(tgt.node)
    else
      d
  }
}


class IsFieldDataTypeOrKeyEdgePredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.UdtToFieldDataType ||
      t.getType == Edges.EntityToDirectKey

    if (d)
      t.getTgt.equals(tgt.node) || t.getSrc.equals(tgt.node)
    else
      d
  }
}

class IsFieldAnnotationPredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.FieldToAnnotation

    if (d)
      t.getTgt.equals(tgt.node) || t.getSrc.equals(tgt.node)
    else
      d
  }
}

class IsMethodAnnotationPredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.MethodToAnnotation

    if (d)
      t.getTgt.equals(tgt.node) || t.getSrc.equals(tgt.node)
    else
      d
  }
}

class IsUdtAnnotationPredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.UdtToAnnotation

    if (d)
      t.getTgt.equals(tgt.node)
    else
      d
  }
}


class IsExtendsPredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.Extends

    if (d)
      t.getTgt.equals(tgt.node)
    else
      d
  }
}

class IsIncludesPredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.Includes

    if (d)
      t.getTgt.equals(tgt.node)
    else
      d
  }
}

object IsDataTypeOrKeyEdgePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.UdtToFieldDataType ||
      // t.getType == Edges.EntityToUDTKey ||
      t.getType == Edges.EntityToDirectKey

}

class IsFieldDataTypePredicate(tgt: Vertex) extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = {
    val d = t.getType == Edges.UdtToFieldDataType

    if (d)
      t.getSrc.equals(tgt.node)
    else
      d
  }
}

object IsNamespaceToNamespacePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = t.getType == Edges.NamespaceToNamespace
}

object IsNamespaceUdtPredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = t.getType == Edges.NamespaceToUdt
}


object IsExtendsOnlyPredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean =
    t.getType == Edges.Extends
}

object IsIncludesOnlyPredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = t.getType == Edges.Includes
}

object IsTestcasePredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = t.getType == Edges.TestTarget
}

object IsTransformerPredicate extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = t.getType == Edges.TransformerOutput
}

object AllEdges extends Predicate[IGraphEdge] {
  override def test(t: IGraphEdge): Boolean = true
}
