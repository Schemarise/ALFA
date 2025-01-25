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

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import com.schemarise.alfa.compiler.ast.model.{IStatement, IToken, NodeVisitor}
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err._
import com.schemarise.alfa.compiler.types._
import com.schemarise.alfa.compiler.utils.{TokenImpl}

case class ScalarDataType(location: IToken = TokenImpl.empty,
                          val scalarType: ScalarType,
                          precisionAndScaleArgs: Seq[NumericNode] = Seq.empty,
                          formatArg: Option[StringNode] = Option.empty,
                          val sizeRange: Option[IDataTypeSizeRange[_]] = None,
                          override val wasTemplateDerived: Boolean = false
                         ) extends DataType with IScalarDataType {
  override def resolvableInnerNodes() = Seq.empty

  override def hashCode(): Int = {
    scalarType.hashCode()
  }

  def escapedFormat : Option[String] = {
    if ( formatArg.isEmpty )
      None
    else {
      val f = formatArg.get.text
      val a = f.replace( "\\", "\\\\" )
      val b = a.replace("\"", "\\\"")
      Some( b )
    }
  }

  override def equals(obj: Any): Boolean = {
    if ( obj.isInstanceOf[ScalarDataType] ) {
      obj.asInstanceOf[ScalarDataType].scalarType.equals(scalarType)
    }
    else
      false
  }

  override def toString: String = {
    val sp = precisionAndScaleArgs.map( e => Some(e.toString) ).toList


    val fmt =
      if ( formatArg.isDefined ) {
        val pat = formatArg.get.text
        if ( pat.contains("\"") || pat.contains("\\") )
          Some(s"""${"\""*3}${formatArg.get.text}${"\""*3}""")
        else
          Some(s""""${formatArg.get.text}"""")
      }
      else
        None

    val sz = if (sizeRange.isDefined) Some(sizeRange.get.toString) else None

    val args = ( sp ++ Seq( sz, fmt ) ).flatten

    val argsStr = if ( args.length > 0 )
      args.mkString("( ", ", ", " )")
    else
      ""

    scalarType.toString + argsStr
  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): DataType = this

  override def asTemplateDerived: DataType = {
    scalarType match {
      case Scalars.int => ScalarDataType.intTypeTemplated
      case Scalars.short => ScalarDataType.shortTypeTemplated
      case Scalars.long => ScalarDataType.longTypeTemplated
      case Scalars.double => ScalarDataType.doubleTypeTemplated
      case Scalars.boolean => ScalarDataType.booleanTypeTemplated
      case _ => this
    }
  }

  private def isNumeric(s: ScalarDataType): Boolean = {
    val st = s.scalarType
    st == Scalars.short || st == Scalars.int || st == Scalars.long || st == Scalars.double || st == Scalars.decimal
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable) = {
    if (other.isInstanceOf[ScalarDataType]) {
      val n = other.asInstanceOf[ScalarDataType]

      if (scalarType.equals(n.scalarType)) {
        // compare based on type, without the constraints parts
        true
      } else {
        val rhs = n.scalarType
        scalarType.id >= rhs.id && isNumeric(this) && isNumeric(n)
      }
    }
    else
      false
  }

  override def traverse(v: NodeVisitor): Unit = {
    v.enter(this)
    v.exit(this)
  }

  override def fieldDataType() = AllFieldTypes.withEnumName(scalarType.toString)

  def validateScalar(ctx: Context): Unit = {

    if ( sizeRange.isDefined ) {
      // this will parse and validate the figures
      sizeRange.get.min
      sizeRange.get.max
    }

    scalarType match {
      case Scalars.decimal =>
        if (precisionAndScaleArgs.size != 0 && precisionAndScaleArgs.size != 2)
          ctx.addResolutionError(precisionAndScaleArgs.head.location, InvalidScalarArgs, "Both scale and precision must be specified")

        if ( precisionAndScaleArgs.size == 2 ) {
          val precision = precisionAndScaleArgs.head
          val scale = precisionAndScaleArgs.last

          if ( scale.number.intValue() > precision.number.intValue() )
            ctx.addResolutionError(precisionAndScaleArgs.head.location, InvalidScalarArgs, "Scale has to be less than precision")
        }

      case Scalars.date =>
        if (!formatArg.isEmpty) {
          val df = formatArg.head
          try {
            new   SimpleDateFormat(df.text)
          } catch {
            case e: Exception => {
              ctx.addResolutionError(formatArg.head.location, InvalidPattern, formatArg.head, e.getMessage)
            }
          }
        }

      case Scalars.datetime =>
        if (!formatArg.isEmpty) {
          val df = formatArg.head
          try {
            new SimpleDateFormat(df.text)
          } catch {
            case e: Exception => {
              ctx.addResolutionError(formatArg.head.location, InvalidPattern, formatArg.head, e.getMessage)
            }
          }
        }

      case Scalars.datetimetz =>
        if (!formatArg.isEmpty) {
          val df = formatArg.head
          try {
            new SimpleDateFormat(df.text)
          } catch {
            case e: Exception => {
              ctx.addResolutionError(formatArg.head.location, InvalidPattern, formatArg.head, e.getMessage)
            }
          }
        }

      case Scalars.time =>
        if (!formatArg.isEmpty) {
          val df = formatArg.head
          try {
            val fmt = new SimpleDateFormat(df.text)
          } catch {
            case e: Exception => {
              ctx.addResolutionError(formatArg.head.location, InvalidPattern, formatArg.head, e.getMessage)
            }
          }
        }

      case Scalars.string => {
        if (formatArg.size == 1) {
          val pat = formatArg.head.text
          // https://spacetelescope.github.io/understanding-json-schema/reference/regular_expressions.html
          try {
            Pattern.compile(pat)
          } catch {
            case e: Exception => {
              ctx.addResolutionError(formatArg.head.location, InvalidPattern, formatArg.head, e.getMessage)
            }
          }
        }
      }

      case Scalars.void => {
        var err = false
        if (parent.isInstanceOf[Field]) {
          val udt = parent.asInstanceOf[Field].locateUdtParent
          if (!udt.isUnion && !udt.isEnum) err = true
        }
        else if (parent.isInstanceOf[MethodSignature] ||
          parent.isInstanceOf[IStatement] ||
          parent.isInstanceOf[IExpression] ||
          parent.isInstanceOf[LambdaDataType ]) {
          // all good
        }
        else {
          err = true
        }

        if (err)
          ctx.addResolutionError(new ResolutionMessage(location, InvalidUsageOfVoid)(None, List.empty))
      }

      case _ => {
        if (sizeRange.isDefined)
          sizeRange.get.validate()
      }

    }
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)
    validateScalar(ctx)
  }

  override def unwrapTypedef: DataType = this

  def scale: Option[Int] =
    if ( precisionAndScaleArgs.size > 0 )
      Some( precisionAndScaleArgs(1).number.intValue() )
    else
      None

  def precision: Option[Int] =
    if ( precisionAndScaleArgs.size > 1 )
      Some( precisionAndScaleArgs(0).number.intValue() )
    else
      None

  def dateFormat: Option[String] =
    if (formatArg.isEmpty)
      None
    else
      Some(formatArg.head.text)

  def stringPattern: Option[String] =
    if (formatArg.isEmpty)
      None
    else
      Some(formatArg.head.text)

  override def min: Option[_] = if (sizeRange.isDefined) sizeRange.get.min else None

  override def max: Option[_] = if (sizeRange.isDefined) sizeRange.get.max else None
}

object ScalarDataType {
  val voidType = new ScalarDataType(TokenImpl.empty, Scalars.void)
  val shortType = new ScalarDataType(TokenImpl.empty, Scalars.short)
  val doubleType = new ScalarDataType(TokenImpl.empty, Scalars.double)
  val decimalType = new ScalarDataType(TokenImpl.empty, Scalars.decimal)
//  val floatType = new ScalarDataType(TokenImpl.empty, Scalars.float)
  val intType = new ScalarDataType(TokenImpl.empty, Scalars.int)
  val dateType = new ScalarDataType(TokenImpl.empty, Scalars.date)
  val longType = new ScalarDataType(TokenImpl.empty, Scalars.long)
  val stringType = new ScalarDataType(TokenImpl.empty, Scalars.string)
//  val charType = new ScalarDataType(TokenImpl.empty, Scalars.char)
  val booleanType = new ScalarDataType(TokenImpl.empty, Scalars.boolean)
  val binaryType = new ScalarDataType(TokenImpl.empty, Scalars.binary)
  val timeType = new ScalarDataType(TokenImpl.empty, Scalars.time)
  val datetimeType = new ScalarDataType(TokenImpl.empty, Scalars.datetime)
  val datetimetzType = new ScalarDataType(TokenImpl.empty, Scalars.datetimetz)
//  val uriType = new ScalarDataType(TokenImpl.empty, Scalars.uri)
//  val patternType = new ScalarDataType(TokenImpl.empty, Scalars.pattern)
  val uuidType = new ScalarDataType(TokenImpl.empty, Scalars.uuid)
  val durationType = new ScalarDataType(TokenImpl.empty, Scalars.duration)
  val periodType = new ScalarDataType(TokenImpl.empty, Scalars.period)

//  val floatTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.float, Seq.empty, Seq.empty, Option.empty, true)
  val doubleTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.double, Seq.empty, Option.empty, Option.empty, true)
  val voidTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.void, Seq.empty, Option.empty, Option.empty, true)
  val shortTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.short, Seq.empty, Option.empty, Option.empty, true)
  val intTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.int, Seq.empty, Option.empty, Option.empty, true)
  val longTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.long, Seq.empty, Option.empty, Option.empty, true)
  val stringTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.string, Seq.empty, Option.empty, Option.empty, true)
  val booleanTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.boolean, Seq.empty, Option.empty, Option.empty, true)
  val binaryTypeTemplated = new ScalarDataType(TokenImpl.empty, Scalars.binary, Seq.empty, Option.empty, Option.empty, true)
}