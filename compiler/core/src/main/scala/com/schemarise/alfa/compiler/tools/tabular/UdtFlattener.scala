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

import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.expr.IBlockExpression
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.model.types.tabular.IColumnPathEntry
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.tools.graph.IsIncludesOnlyPredicate

import scala.collection.mutable

// TODO Review collection flatten and naming
class UdtFlattener(cua: ICompilationUnitArtifact, udt: IUdtBaseNode) {

  val table = new Tablular(udt)

  val v = new FlattenVisitor()

  udt.traverse(v)

  class FlattenVisitor extends NoOpNodeVisitor {
    private val path = new mutable.ListBuffer[IColumnPathEntry]()
    private var inKey = false
    // TODO do we need a description?
    private var currentFieldDescr = ""

    override def enter(e: IBlockExpression): Mode = NodeVisitMode.Break

    override def enter(e: IRecord): Mode = super.enter(e)

    def enterWhenFieldType(e: Trait): Mode = {

      table.createMetaColumn(path.toList, ColumnMappingType.traitImpl, inKey, "Implementation of " + e.name.fullyQualifiedName)

      val exts =
        if (e.scope.isEmpty)
          cua.graph.incomingEdgeNodes(e, IsIncludesOnlyPredicate)
        else
          e.scope

      table.fullyExpandColumns(true)
      exts.map(_.asInstanceOf[UdtBaseNode]).filter(_.instantiatable).foreach(_.traverse(this))
      table.fullyExpandColumns(false)

      NodeVisitMode.Break
    }

    override def enter(e: ITrait): Mode = super.enter(e)

    override def enter(e: IEnum): Mode = {
      table.createScalarColumn(path.toList, ScalarDataType.stringType, inKey, currentFieldDescr)
      NodeVisitMode.Break
    }

    override def enter(e: IAnnotation): Mode = {
      NodeVisitMode.Break
    }

    override def enter(e: IUnion): Mode = {
      table.createMetaColumn(path.toList, ColumnMappingType.unionCase, inKey, "Union case of " + e.name.fullyQualifiedName)
      super.enter(e)
    }

    override def enter(e: IEntity): Mode = {
      super.enter(e)
    }

    override def enter(e: IKey): Mode = {
      inKey = true
      super.enter(e)
    }

    override def exit(e: IKey): Unit = {
      super.exit(e)
      inKey = false
    }

    override def enter(e: IField): Mode = {
      path += FieldColumnPathEntry(e.asInstanceOf[Field])
      super.enter(e)
    }

    override def enter(e: IScalarDataType): Mode = {
      table.createScalarColumn(path.toList, e.asInstanceOf[ScalarDataType], inKey, currentFieldDescr)
      super.enter(e)
    }

    override def enter(e: IMapDataType): Mode = {
      val m = e.asInstanceOf[MapDataType]
      path += MapKeyColumnPathEntry(m)
      m.keyType.traverse(this)
      popPath()

      path += MapValueColumnPathEntry(m)
      m.valueType.traverse(this)
      popPath()

      NodeVisitMode.Break
    }

    override def enter(e: IListDataType): Mode = {
      table.createMetaColumn(path.toList, ColumnMappingType.seqIndex, inKey, "")

      if (!e.componentType.isInstanceOf[ScalarDataType])
        path += ListColumnPathEntry(e.asInstanceOf[ListDataType])

      super.enter(e)
    }

    override def enter(e: ISetDataType): Mode = {
      table.createMetaColumn(path.toList, ColumnMappingType.setIndex, inKey, "")

      if (!e.componentType.isInstanceOf[ScalarDataType])
        path += SetColumnPathEntry(e.asInstanceOf[SetDataType])

      super.enter(e)
    }

    override def enter(e: ITupleDataType): Mode = {
      e.asInstanceOf[TupleDataType].syntheticRecord.traverse(this)
      NodeVisitMode.Break
    }

    override def enter(e: IUnionDataType): Mode = {
      e.syntheticUnion.traverse(this)
      NodeVisitMode.Break
    }

    override def enter(e: IEnumDataType): Mode = {
      enter(e.syntheticEnum) // we only want to enter Enum not traverse its fields
      NodeVisitMode.Break
    }

    def enterDataType(t: DataType): Unit = {
      if (t.isInstanceOf[ScalarDataType])
        enter(t.asInstanceOf[ScalarDataType])
      else if (t.isInstanceOf[SetDataType])
        enter(t.asInstanceOf[SetDataType])
      else if (t.isInstanceOf[ListDataType])
        enter(t.asInstanceOf[ListDataType])
      else if (t.isInstanceOf[MapDataType])
        enter(t.asInstanceOf[MapDataType])
      else if (t.isInstanceOf[EnclosingDataType])
        enter(t.asInstanceOf[EnclosingDataType])
      else if (t.isInstanceOf[UdtDataType])
        enter(t.asInstanceOf[UdtDataType])
      else
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Unhandled " + t)
    }

    override def enter(e: IEnclosingDataType): Mode = {
      val i = e.asInstanceOf[EnclosingDataType]
      if (i.encType == Enclosed.opt) {
        table.createMetaColumn(path.toList, ColumnMappingType.optional, inKey, "")
      }
      super.enter(e)
    }

    override def enter(e: IMetaDataType): Mode = {
      if (e.metaType.toString.endsWith("Name")) {
        table.createScalarColumn(path.toList, ScalarDataType.stringType, inKey, currentFieldDescr)
      }
      else {
        // TODO what to create for $record $entity etc.
        table.createScalarColumn(path.toList, ScalarDataType.stringType, inKey, currentFieldDescr)
      }
      super.enter(e)
    }

    override def enter(e: IUdtDataType): Mode = {
      val u = e.asInstanceOf[UdtDataType]
      val rt = u.resolvedType.get

      if (rt.isInstanceOf[Entity])
        rt.asInstanceOf[Entity].traverse(this)
      else if (rt.isInstanceOf[Key])
        rt.asInstanceOf[Key].traverse(this)
      else if (rt.isInstanceOf[Trait]) {
        if (!u.referencedFromInclude)
          enterWhenFieldType(rt.asInstanceOf[Trait])
      }
      else if (rt.isInstanceOf[Union])
        rt.asInstanceOf[Union].traverse(this)
      else if (rt.isInstanceOf[EnumDecl])
        rt.asInstanceOf[EnumDecl].traverse(this)
      else if (rt.isInstanceOf[Record])
        rt.asInstanceOf[Record].traverse(this)
      else
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Unhandled " + rt)

      super.enter(e)
    }

    override def exit(e: IField): Unit = {
      popPath()
      super.exit(e)
    }

    private def popPath(): Unit = {
      path.remove(path.size - 1)

    }
  }
}
