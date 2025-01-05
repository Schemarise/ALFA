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

import com.schemarise.alfa.compiler.ast.model.{IField, IStatement, IToken, NodeVisitMode, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.ast.model.types.Enclosed.EnclosedType
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, IAssignable, IEnclosingDataType}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err._
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.TokenImpl

object EnclosingDataType {
  def optional(location: IToken = TokenImpl.empty, compType: DataType) = {
    EnclosingDataType(location, Enclosed.opt, compType)
  }

  def stream(compType: DataType, location: IToken = TokenImpl.empty) = {
    EnclosingDataType(location, Enclosed.stream, compType)
  }
}

case class EnclosingDataType(location: IToken = TokenImpl.empty, encType: EnclosedType, val declComponentType: DataType) extends DataType with IEnclosingDataType {

  //  if ( declComponentType.isInstanceOf[EnclosingDataType] )
  //    println(111)

  override def componentType = _componentType

  private var _componentType = declComponentType

  override def resolvableInnerNodes() = Seq(componentType)

  private var resolvedKey: Option[Key] = None

  override def isOptional = encType == Enclosed.opt

  //  override def isKey = encType == Enclosed.key
  override def isTabular = encType == Enclosed.table

  override def isStream = encType == Enclosed.stream

  override def isFuture = encType == Enclosed.future

  override def isEncrypt = encType == Enclosed.encrypt

  override def isCompress = encType == Enclosed.compress

  override def isTry = encType == Enclosed.try_

  override def isEither = encType == Enclosed.either

  override def isPair = encType == Enclosed.pair

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType =
    EnclosingDataType(location, encType, componentType.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType])

  override def fieldDataType() = AllFieldTypes.withEnumName(encType.toString)


  override def toString: String = {
    val s = new StringBuilder

    if (encType != Enclosed.opt)
      s.append(s"${encType.toString.toLowerCase}< ")

    s.append(componentType.toString)

    if (encType != Enclosed.opt)
      s.append(" >")
    else
      s.append(" ?")

    s.toString()
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)

    if (encType == Enclosed.key &&
      !componentType.hasErrors) {
      if (!componentType.isInstanceOf[UdtDataType] ||
        !componentType.asInstanceOf[UdtDataType].resolvedType.get.isInstanceOf[Entity])
        ctx.addResolutionError(componentType.location, InvalidKeyUsage, componentType + " not an entity")
      else {
        val entity = componentType.asInstanceOf[UdtDataType].resolvedType.get.asInstanceOf[Entity]
        if (entity isSingleton) {
          ctx.addResolutionError(componentType.location, EntityIsSingleton, entity.nodeId.id)
        }
        else {
          resolvedKey = Some(entity.keyType.get.resolvedType.get.asInstanceOf[Key])
        }
      }
    }

    if (encType == Enclosed.stream && componentType.isStream()) {
      ctx.addResolutionError(this, InvalidEnclosedType,"Stream", "stream")
    }

    if ( !hasErrors && encType == Enclosed.stream && locateMethodSignatureParent().isEmpty  ) {
      ctx.addResolutionError(this, InvalidEnclosedType, "Use", "stream outside of function parameter or result")
    }
  }

  //  override def resolve(ctx: Context): Unit = {
  //    super.resolve(ctx)
  //
  //    // to avoid cycle key refs, locate resolvedKey in resolve stage
  //    if ( encType == Enclosed.key && !componentType.hasErrors) {
  //      val entity = componentType.asInstanceOf[UdtDataType].resolvedType.get.asInstanceOf[Entity]
  //
  //      if ( ! entity.isSingleton ) {
  //        resolvedKey = Some(entity.keyType.get.resolvedType.get.asInstanceOf[Key])
  //      }
  //    }
  //  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) = {
    if (other.isInstanceOf[EnclosingDataType] &&
      encType.equals(other.asInstanceOf[EnclosingDataType].encType) &&
      componentType.isAssignableFrom(other.asInstanceOf[EnclosingDataType].componentType))
      true
    //    else if ( (encType == Enclosed.opt ) && componentType.isAssignableFrom(other) )
    //      true
    else
      false
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {

      //      if ( encType == Enclosed.key ) {
      //        if ( resolvedKey.isDefined ) {
      //          resolvedKey.get.traverse(v)
      //        }
      //      }
      //      else
      {
        componentType.traverse(v)
      }

      v.exit(this)
    }
  }

  // TODO Not good.. refactor mutability
  override def unwrapTypedef: DataType = {
    _componentType = componentType.unwrapTypedef

    if (encType == Enclosed.key && resolvedKey.isDefined) {
      resolvedKey.get.asDataType
    }
    else {
      this
    }
  }
}
