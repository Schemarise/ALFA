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
package com.schemarise.alfa.compiler.ast.model.types

import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.ast.model.{IDocumentation, _}

trait IDataType extends INode with IAssignable with IDocAndAnnotated {

  val SyntheticTypeFieldSeparator = "__"

  def isUdtBaseNode: Boolean = this.isInstanceOf[IUdtBaseNode]

  override def docs: Seq[IDocumentation] = Seq.empty

  override def annotations: Seq[IAnnotation] = Seq.empty

  override def annotationsMap: Map[IUdtVersionName, IAnnotation] = Map.empty

  override def annotationValue(annotationName: String, annotationAttribute: String): Option[IExpression] = None

  def wasTemplateDerived: Boolean

  def isNullable: Boolean

  def unwrapTypedef: IDataType

  def unwrapIfOptional: IDataType = {
    if (isEncOptional()) {
      asInstanceOf[IContainerDataType].componentType
    }
    else
      this
  }

  def isErrorDataType: Boolean = this.isInstanceOf[IErrorDataType]

  def isExprDelegateType: Boolean = this.isInstanceOf[IExprDelegateType]

  def isScalar: Boolean = {
    val b = whenScalar(s => {
      true
    })
    b.isDefined && b.get
  }

  def isScalarString: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.string)
    b.isDefined && b.get
  }

  def isScalarDouble: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.double)
    b.isDefined && b.get
  }

  def isScalarBigDecimal: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.decimal)
    b.isDefined && b.get
  }

  def isMetatype: Boolean = {
    val b = whenMetatype(_ => true)
    b.isDefined && b.get
  }

  def isScalarBoolean: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.boolean)
    b.isDefined && b.get
  }

  def isScalarVoid: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.void)
    b.isDefined && b.get
  }

  def isScalarDate: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.date)
    b.isDefined && b.get
  }

  def isScalarDuration: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.duration)
    b.isDefined && b.get
  }

  def isScalarDatetime: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.datetime)
    b.isDefined && b.get
  }

  def isScalarDatetimetz: Boolean = {
    val b = whenScalar(s => s.scalarType == Scalars.datetimetz)
    b.isDefined && b.get
  }

  //  def isScalarUri : Boolean = {
  //    val b = whenScalar( s => s.scalarType == Scalars.uri  )
  //    b.isDefined && b.get
  //  }
  //
  //  def isScalarPattern : Boolean = {
  //    val b = whenScalar( s => s.scalarType == Scalars.pattern  )
  //    b.isDefined && b.get
  //  }

  def isScalarNumeric: Boolean = {
    val b = whenScalar(s => {
      (s.scalarType == Scalars.short ||
        s.scalarType == Scalars.int ||
        s.scalarType == Scalars.long ||
        s.scalarType == Scalars.double ||
        //        s.scalarType == Scalars.float  ||
        s.scalarType == Scalars.decimal)
    })
    b.isDefined && b.get
  }


  def isScalarTemporal: Boolean = {
    val b = whenScalar(s => {
      (s.scalarType == Scalars.datetime ||
        s.scalarType == Scalars.datetimetz ||
        s.scalarType == Scalars.date ||
        s.scalarType == Scalars.time ||
        s.scalarType == Scalars.period ||
        s.scalarType == Scalars.duration)
    })
    b.isDefined && b.get
  }


  def isUdt: Boolean = {
    val b = whenUDT(s => {
      true
    })
    b.isDefined && b.get
  }

  def isUdtEnum: Boolean = {
    val b = whenUDT(s => {
      s.udt.isEnum
    })
    b.isDefined && b.get
  }

  def isUdtRecord: Boolean = {
    val b = whenUDT(s => {
      s.udt.isRecord
    })
    b.isDefined && b.get
  }

  def isUdtTrait: Boolean = {
    val b = whenUDT(s => {
      s.udt.isTrait
    })
    b.isDefined && b.get
  }

  def isUdtKey: Boolean = {
    val b = whenUDTKey(s => true)
    b.isDefined && b.get
  }

  def isUdtUnion: Boolean = {
    val b = whenUDTUnion(s => true)
    b.isDefined && b.get
  }

  def isUdtEntity: Boolean = {
    val b = whenUDTEntity(s => true)
    b.isDefined && b.get
  }

  def isBinary(): Boolean = {
    val b = whenScalar(s => {
      s.scalarType == Scalars.binary
    })
    b.isDefined && b.get
  }

  def isVoid(): Boolean = {
    val b = whenScalar(s => {
      s.scalarType == Scalars.void
    })
    b.isDefined && b.get
  }

  def isStream(): Boolean = {
    val b = whenEncStream(e => {
      true
    })
    b.isDefined && b.get
  }

  def isFuture(): Boolean = {
    val b = whenEncFuture(e => {
      true
    })
    b.isDefined && b.get
  }

  def isSet(): Boolean = {
    val b = whenSet(e => {
      true
    })
    b.isDefined && b.get
  }

  def isVector(): Boolean = {
    isMap() || isSet() || isList()
  }

  def isMap(): Boolean = {
    val b = whenMap(e => {
      true
    })
    b.isDefined && b.get
  }

  def isTuple(): Boolean = {
    whenTuple(e => {
      true
    }, false)
  }

  def isEnumDatatype(): Boolean = {
    whenEnumDatatype(e => {
      true
    }, false)
  }

  def isUnion(): Boolean = {
    val b = whenUnion(e => {
      true
    })
    b.isDefined && b.get
  }

  def isLambda(): Boolean = {
    val b = whenLambda(e => {
      true
    })
    b.isDefined && b.get
  }

  def isList(): Boolean = {
    whenListOrElse(e => {
      true
    }, false)
  }

  def sizeConstraints: Option[ISizeConstraits] = {
    if (isInstanceOf[ISizeConstraits])
      Some(asInstanceOf[ISizeConstraits])
    else
      None
  }

  def isEncOptional(): Boolean = {
    val b = whenEncOptional(e => {
      true
    })
    b.isDefined && b.get
  }

  def isEncOptionalString(): Boolean = {
    val b = whenEncOptional(e => e.isScalarString)
    b.isDefined && b.get
  }

  def isEncKey(): Boolean = {
    val b = whenEncKey(e => {
      true
    })
    b.isDefined
  }

  //  def isEncKeyOf(): Boolean = {
  //    val b = whenEncKey(e => {  true } )
  //    b.isDefined && b.get
  //  }

  def isEncTry(): Boolean = {
    val b = whenEncTry(e => {
      true
    })
    b.isDefined && b.get
  }

  def whenEncOptional[T](fn: (IEnclosingDataType) => T, other: T): T = {
    val v = whenEncOptional(fn)
    if (v.isDefined) v.get else other
  }

  def whenEncKey[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.encType == Enclosed.key) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncOptional[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isOptional) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  //  def whenEncKey[T]( fn : ( IEnclosingDataType ) => T ) : Option[T] = {
  //    if ( this.isInstanceOf[IEnclosingDataType]) {
  //      val enc = this.asInstanceOf[IEnclosingDataType]
  //      if (enc.isKeyOf) {
  //        return Some( fn(this.asInstanceOf[IEnclosingDataType]) )
  //      }
  //    }
  //    None
  //  }

  //  def whenEncCheck[T]( fn : ( IEnclosingDataType ) => T ) : Option[T] = {
  //    if ( this.isInstanceOf[IEnclosingDataType]) {
  //      val enc = this.asInstanceOf[IEnclosingDataType]
  //      if (enc.isTry) {
  //        return Some( fn(this.asInstanceOf[IEnclosingDataType]) )
  //      }
  //    }
  //    None
  //  }


  //  def whenEncKey[T]( fn : ( IEnclosingDataType ) => T ) : Option[T] = {
  //    if ( this.isInstanceOf[IEnclosingDataType]) {
  //      val enc = this.asInstanceOf[IEnclosingDataType]
  //      if (enc.isKey) {
  //        return Some( fn(this.asInstanceOf[IEnclosingDataType]) )
  //      }
  //    }
  //    None
  //  }

  def whenEncTry[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isTry) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncEither[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isEither) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncStream[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isStream) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncFuture[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isFuture) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncEncrypt[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isEncrypt) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenEncCompress[T](fn: (IEnclosingDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnclosingDataType]) {
      val enc = this.asInstanceOf[IEnclosingDataType]
      if (enc.isCompress) {
        return Some(fn(this.asInstanceOf[IEnclosingDataType]))
      }
    }
    None
  }

  def whenScalar[T](fn: (IScalarDataType) => T): Option[T] = {
    if (this.isInstanceOf[IScalarDataType]) {
      return Some(fn(this.asInstanceOf[IScalarDataType]))
    }
    else
      None
  }

  def whenScalarNumeric[T](fn: (IScalarDataType) => T): Option[T] = {
    if (isScalarNumeric) {
      return Some(fn(this.asInstanceOf[IScalarDataType]))
    }
    else
      None
  }


  def whenMetatype[T](fn: (IMetaDataType) => T): Option[T] = {
    if (this.isInstanceOf[IMetaDataType]) {
      return Some(fn(this.asInstanceOf[IMetaDataType]))
    }
    else
      None
  }

  def whenMap[T](fn: (IMapDataType) => T): Option[T] = {
    if (this.isInstanceOf[IMapDataType]) {
      return Some(fn(this.asInstanceOf[IMapDataType]))
    }
    else
      None
  }

  def whenUnion[T](fn: (IUnionDataType) => T): Option[T] = {
    if (this.isInstanceOf[IUnionDataType]) {
      return Some(fn(this.asInstanceOf[IUnionDataType]))
    }
    else
      None
  }

  def whenLambda[T](fn: (ILambdaDataType) => T): Option[T] = {
    if (this.isInstanceOf[ILambdaDataType]) {
      return Some(fn(this.asInstanceOf[ILambdaDataType]))
    }
    else
      None
  }

  def whenListOrElse[T](fn: (IListDataType) => T, elze: T): T = {
    val r = whenList(fn)
    r.getOrElse(elze)
  }

  def whenList[T](fn: (IListDataType) => T): Option[T] = {
    if (this.isInstanceOf[IListDataType]) {
      return Some(fn(this.asInstanceOf[IListDataType]))
    }
    else
      None
  }

  def whenSet[T](fn: (ISetDataType) => T): Option[T] = {
    if (this.isInstanceOf[ISetDataType]) {
      return Some(fn(this.asInstanceOf[ISetDataType]))
    }
    else
      None
  }

  def whenUDT[T](fn: (IUdtDataType) => T): Option[T] = {
    if (this.isInstanceOf[IUdtDataType]) {
      return Some(fn(this.asInstanceOf[IUdtDataType]))
    }
    else
      None
  }

  def whenUDTKey[T](fn: (IUdtDataType) => T): Option[T] = {
    if (this.isInstanceOf[IUdtDataType] &&
      this.asInstanceOf[IUdtDataType].udt.isKey) {
      return Some(fn(this.asInstanceOf[IUdtDataType]))
    }
    else
      None
  }

  def whenUDTUnion[T](fn: (IUdtDataType) => T): Option[T] = {
    if (this.isInstanceOf[IUdtDataType] &&
      this.asInstanceOf[IUdtDataType].udt.isUnion) {
      return Some(fn(this.asInstanceOf[IUdtDataType]))
    }
    else
      None
  }

  def whenUDTEntity[T](fn: (IUdtDataType) => T): Option[T] = {
    if (this.isInstanceOf[IUdtDataType] &&
      this.asInstanceOf[IUdtDataType].udt.isEntity) {
      return Some(fn(this.asInstanceOf[IUdtDataType]))
    }
    else
      None
  }

  def whenEnum[T](fn: (IEnumDataType) => T): Option[T] = {
    if (this.isInstanceOf[IEnumDataType]) {
      return Some(fn(this.asInstanceOf[IEnumDataType]))
    }
    else
      None
  }

  //  def whenUnion[T]( fn : ( IUnionDataType ) => T ) : Option[T] = {
  //    if ( this.isInstanceOf[IUnionDataType]) {
  //      return Some( fn ( this.asInstanceOf[IUnionDataType] ) )
  //    }
  //    else
  //      None
  //  }

  def whenTable[T](fn: (ITabularDataType) => T): Option[T] = {
    if (this.isInstanceOf[ITabularDataType]) {
      return Some(fn(this.asInstanceOf[ITabularDataType]))
    }
    else
      None
  }

  def whenTuple[T](fn: (ITupleDataType) => T, elze: T): T = {
    if (this.isInstanceOf[ITupleDataType]) {
      return fn(this.asInstanceOf[ITupleDataType])
    }
    else
      elze
  }

  def whenEnumDatatype[T](fn: (IEnumDataType) => T, elze: T): T = {
    if (this.isInstanceOf[IEnumDataType]) {
      return fn(this.asInstanceOf[IEnumDataType])
    }
    else
      elze
  }
}
