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
package com.schemarise.alfa.compiler.ast.nodes.datatypes

import com.schemarise.alfa.compiler.ast.model.{IToken, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, IAssignable, IDataType, IEitherDataType}
import com.schemarise.alfa.compiler.utils.TokenImpl

class EitherDataType(location: IToken = TokenImpl.empty, l: DataType, r: DataType)
  extends EnclosingDataType(location, Enclosed.either, l) with IEitherDataType {
  private var _left = l
  private var _right = r

  override def parameterizedTypes = Seq(left, right)

  override def resolvableInnerNodes() = Seq(l, r)

  override def toString: String = {
    val s = new StringBuilder

    s.append(s"${encType.toString.toLowerCase}< ")

    s.append(left.toString)
    s.append(", ")
    s.append(right.toString)
    s.append(" >")

    s.toString()
  }

  override def unwrapTypedef: DataType = {
    _left = l.unwrapTypedef
    _right = r.unwrapTypedef
    this
  }

  override def componentType = throw new com.schemarise.alfa.compiler.AlfaInternalException("Component type not accessible for either<L,R>")

  override def left: IDataType = _left

  override def right: IDataType = _right

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      _left.traverse(v)
      _right.traverse(v)

      v.exit(this)
    }
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) = {
    if (other.isInstanceOf[EitherDataType] &&
      l.isAssignableFrom(other.asInstanceOf[EitherDataType].left) &&
      r.isAssignableFrom(other.asInstanceOf[EitherDataType].right)
    )
      true
    //    else if ( (encType == Enclosed.opt ) && componentType.isAssignableFrom(other) )
    //      true
    else
      false
  }
}
