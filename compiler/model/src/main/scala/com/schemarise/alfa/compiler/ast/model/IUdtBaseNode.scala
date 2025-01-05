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
package com.schemarise.alfa.compiler.ast.model

import com.schemarise.alfa.compiler.ast.model.types.{EnumToString, IUdtDataType, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType

import scala.collection.immutable.{ListMap, ListSet}

trait IUdtBaseNode extends INode with IdentifiableNode with IDocAndAnnotated with IMethodsContainer {

  def name: IUdtVersionName

  def checksum(): String

  def modelId(): Option[String]

  def versions: List[IUdtBaseNode]

  def isLoadedFromRepository: Boolean
  /*
  trait T1 { t1 }
  trait T2 { t2 }
  record A inc T1 { a }
  record B ext A { b }
  record C ext B inc T2 { c }
  record D ext C { d }

      allFields           localFields     includesFields
  A   t1, a               a               t1
  B   t1, a, b            b
  C   t1, a, b, t2, c     c               t2
  D   t1, a, b, t2, c, d  d
   */

  def udtType: UdtType

  def isInternal: Boolean

  def isTrait = udtType == UdtType.`trait`

  def isTraitWithScope = isTrait && asInstanceOf[ITrait].hasScope

  def isUnion = udtType == UdtType.union

  def isUntaggedUnion = udtType == UdtType.untaggedUnion

  def isEntity = udtType == UdtType.entity

  def isEntityWithKey = udtType == UdtType.entity && this.asInstanceOf[IEntity].key.isDefined

  def isRecord = udtType == UdtType.record

  def isKey = udtType == UdtType.key

  def isAnnotation = udtType == UdtType.annotation

  def isEnum = udtType == UdtType.enum

  override def isService = udtType == UdtType.service

  override def isTestcase = udtType == UdtType.testcase

  override def isLibrary = udtType == UdtType.library

  override def isTransform = udtType == UdtType.transform

  def isExtension = udtType == UdtType.extension

  def isMethodSig = udtType == UdtType.methodSig

  def isNativeUdt = udtType == UdtType.nativeUdt

  def udtNodeTypeName =
    EnumToString.udtTypeToString(udtType)

  def isTemplated: Boolean

  def templateInstatiations: Seq[IUdtBaseNode]

  def allFields: ListMap[String, IField]

  def allAddressableFields: ListMap[String, IField]

  def topologicallySortedFields: ListMap[String, IField]

  def includes: Seq[IUdtDataType]

  def extendsDef: Option[IUdtDataType]

  def allAsserts: ListMap[String, IAssertDeclaration]

  def allLinkages: ListMap[String, ILinkageDeclaration]

  def allSingularAsserts: ListMap[String, IAssertDeclaration]

  def allVectorizedAsserts: ListMap[String, IAssertDeclaration]

  def localFieldNames: ListSet[String]

  def includesClosureFieldNames: ListSet[String] = {
    val l = allFields.filter(f => !localFieldNames.contains(f._1)).map(_._1)
    ListSet(l.toSeq: _*)
  }

  //  val repositoryEntry: Option[ArtifactEntry]
  //
  //  def hasModifier(m: ModifierType): Boolean
  //
  def isSynthetic: Boolean

  def isFragment: Boolean

  //  def isAssignableTo(other: Assignable): Boolean


  def asDataType: IUdtDataType

  def whenTrait(fn: (ITrait) => Unit): Boolean = {
    if (this.isInstanceOf[ITrait]) {
      fn(this.asInstanceOf[ITrait])
      return true
    }
    false
  }

  def whenService(fn: (IService) => Unit): Boolean = {
    if (this.isInstanceOf[IService]) {
      fn(this.asInstanceOf[IService])
      return true
    }
    false
  }

  def whenLibrary(fn: (ILibrary) => Unit): Boolean = {
    if (this.isInstanceOf[ILibrary]) {
      fn(this.asInstanceOf[ILibrary])
      return true
    }
    false
  }

  def whenTestcase(fn: (ITestcase) => Unit): Boolean = {
    if (this.isInstanceOf[ITestcase]) {
      fn(this.asInstanceOf[ITestcase])
      return true
    }
    false
  }

  def whenRecord(fn: (IRecord) => Unit): Boolean = {
    if (this.isInstanceOf[IRecord]) {
      fn(this.asInstanceOf[IRecord])
      return true
    }
    false
  }

  def whenNativeUdt(fn: (INativeUdt) => Unit): Boolean = {
    if (this.isInstanceOf[INativeUdt]) {
      fn(this.asInstanceOf[INativeUdt])
      return true
    }
    false
  }

  def whenUnion[T](fn: (IUnion) => T): Option[T] = {
    if (this.isInstanceOf[IUnion]) {
      return Some(fn(this.asInstanceOf[IUnion]))
    }
    None
  }

  def whenKey(fn: (IKey) => Unit): Boolean = {
    if (this.isInstanceOf[IKey]) {
      fn(this.asInstanceOf[IKey])
      return true
    }
    false
  }

  def whenEntity(fn: (IEntity) => Unit): Boolean = {
    if (this.isInstanceOf[IEntity]) {
      fn(this.asInstanceOf[IEntity])
      return true
    }
    false
  }

  def whenEntityOrElse[T](fn: (IEntity) => T, other: T): T = {
    if (this.isInstanceOf[IEntity])
      fn(this.asInstanceOf[IEntity])
    else
      other
  }

  def whenAnnotationDecl(fn: (IAnnotationDecl) => Unit): Boolean = {
    if (this.isInstanceOf[IAnnotationDecl]) {
      fn(this.asInstanceOf[IAnnotationDecl])
      return true
    }
    false
  }

  def whenEnum(fn: (IEnum) => Unit): Boolean = {
    if (this.isInstanceOf[IEnum]) {
      fn(this.asInstanceOf[IEnum])
      return true
    }
    false
  }

  def isFieldContainer = isTrait || isUnion || isRecord || isKey || isRecord || isAnnotation || isEntity || isEnum
}
