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

import com.schemarise.alfa.compiler.ast.model.types.tabular.IColumnPathEntry
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ListDataType, MapDataType, SetDataType}

/**
 * namespace Flattened
 *
 * Scalar
 * ------
 * record A1 {
 * Scalar : int
 * }
 *
 * ScalarOpt SclarOpt$none
 * --------- -------------
 * record A2 {
 * ScalarOpt : int?
 * }
 *
 * SeqOfScalar SeqOfScalar$index
 * ----------- -----------------
 * record A3 {
 * SeqOfScalar : list< int >
 * }
 *
 * SeqOfUdt$index SeqOfScalar
 * -------------- -----------
 * record A4 {
 * SeqOfUdt : list< B >
 * }
 *
 * SeqOfOptInts$index SeqOfOptInts.Entry SeqOfOptInts.Entry$Present
 * ------------------ ------------------ --------------------------
 * record A4 {
 * SeqOfOptInts : list< int? >
 * }
 *
 * SeqOfOptInts$index SeqOfOptInts.Entry SeqOfOptInts$Present
 * ------------------ ------------------ --------------------
 * record A4 {
 * OptSeqOfInts : list< int >?
 * }
 *
 * SeqOfScalar$index SeqOfScalar
 * ----------------- -----------
 * record A5 {
 * SetOfString : set< string >
 * }
 *
 * record A6 {
 * MapOfScalarToScalar : map< string, long >
 * }
 *
 * record A7 {
 * MapOfScalarToUdt : map< string, B >
 * }
 *
 * record A8 {
 * MapOfUdtToUdt : map< C, B >
 * }
 *
 * record A9 {
 * TupleFld : tuple< F1 : int, F2 : C >
 * }
 *
 * record A10 {
 * UnionFld : union< F1 : int, F2 : C >
 * }
 *
 * record A11 {
 * EnumFld : enum< X, Y, Z >
 * }
 *
 * record A12 {
 * TraitFld : Base
 * }
 *
 * record B includes Base {
 * Scalar : int
 * CRec : C
 * }
 *
 * record C includes Base {
 * F1 : int
 * F2 : string
 * }
 *
 * trait Base {}
 *
 *
 */

case class FieldColumnPathEntry(field: Field) extends IColumnPathEntry {
  override def toString: String = field.nameNode.text

  override def name(longName: Boolean): String = {
    if (longName) {
      val p = field.locateUdtParent()
      p.versionedName.fullyQualifiedName + ".." + toString
    } else
      toString
  }
}

case class ListColumnPathEntry(dt: ListDataType) extends IColumnPathEntry {
  override def toString: String = "entry"

  override def name(longName: Boolean): String = toString
}

case class SetColumnPathEntry(dt: SetDataType) extends IColumnPathEntry {
  override def toString: String = "entry"

  override def name(longName: Boolean): String = toString
}

case class MapKeyColumnPathEntry(dt: MapDataType) extends IColumnPathEntry {
  override def toString: String = if (dt.keyNameNode.isDefined) dt.keyNameNode.get.text
  else "key"

  override def name(longName: Boolean): String = toString
}

case class MapValueColumnPathEntry(dt: MapDataType) extends IColumnPathEntry {
  override def toString: String = if (dt.valueNameNode.isDefined) dt.valueNameNode.get.text
  else "value"

  override def name(longName: Boolean): String = toString
}

