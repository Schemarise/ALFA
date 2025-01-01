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

import java.time.chrono.ChronoLocalDate
import java.time._

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.BaseNode
import com.schemarise.alfa.compiler.ast.model.NormalizedAstPeriod
import com.schemarise.alfa.compiler.ast.model.types.IDataTypeSizeRange
import com.schemarise.alfa.compiler.err._

abstract class ValidateRange[T](rctx: Option[Context], minNode: BaseNode, maxNode: BaseNode) extends IDataTypeSizeRange[T] {
  def validate(): Unit = {
    val mi = min
    val mx = max

    // FIXME validate mi < mx

    if (mi.isDefined && mx.isDefined) {
      val mit = mi.get
      val mxt = mx.get

      if (rctx.isDefined && mi.get.asInstanceOf[Comparable[T]].compareTo(mxt) > 0)
        rctx.get.addResolutionError(new ResolutionMessage(minNode.location, NumberRangeError)(None, List.empty, mit, mxt))
    }
  }

  def parse[T](n: StringNode, ptype: String, format: String, callback: String => T): Option[T] = {
    try {
      val v = callback(n.text)
      Some(v)
    } catch {
      case e: Throwable => {
        rctx.get.addResolutionError(new ResolutionMessage(n.location, ValueParseError)(None, List.empty, ptype, format, e.getLocalizedMessage))
        None
      }
    }
  }

  def str(sn: BaseNode) = {
    if (sn == StringNode.empty) "*" else sn.toString

  }

  override def toString: String = {
    str(minNode) + ", " + str(maxNode)
  }
}


case class IntRangeNode(ctx: Option[Context] = None, minNode: IntNode, maxNode: IntNode) extends ValidateRange[Int](ctx, minNode, maxNode) {
  if (minNode.number.isDefined && maxNode.number.isDefined && minNode.number.get > maxNode.number.get) {
    val rm = ResolutionMessage(minNode.location, NumberRangeError)(None, List.empty, minNode.number.get, maxNode.number.get)
    if (ctx.isDefined)
      ctx.get.addResolutionError(rm)
    else
      throw new com.schemarise.alfa.compiler.AlfaInternalException(rm.toString)
  }

  //  def validate(ctx: Context, min : Int, max : Int ) = {
  //    if ( minNode.number < min || maxNode.number > max  )
  //      ctx.addResolutionError(new ResolutionMessage(minNode.location, NumberWithinRangeError )( min, max ))
  //  }

  def min: Option[Int] = if (minNode.number.isDefined) Some(minNode.number.get) else None

  def max: Option[Int] = if (maxNode.number.isDefined) Some(maxNode.number.get) else None

  //  override def toString: String = {
  //    "( " + minNode.toString + ", " + maxNode.toString + " )"
  //  }
}

case class LongRangeNode(ctx: Option[Context] = None, minNode: LongNode, maxNode: LongNode) extends ValidateRange[Long](ctx, minNode, maxNode) {
  if (minNode.number.isDefined && maxNode.number.isDefined && minNode.number.get.compareTo(maxNode.number.get) > 0) {
    val rm = new ResolutionMessage(minNode.location, NumberRangeError)(None, List.empty, minNode.number.get, maxNode.number.get)
    if (ctx.isDefined)
      ctx.get.addResolutionError(rm)
    else
      throw new com.schemarise.alfa.compiler.AlfaInternalException(rm.toString)
  }

  def validate(ctx: Context, min: Long, max: Long) = {
    if (minNode.number.isDefined && minNode.number.get < min)
      ctx.addResolutionError(new ResolutionMessage(minNode.location, NumberWithinRangeError)(None, List.empty, min, max))

    if (maxNode.number.isDefined && maxNode.number.get > max)
      ctx.addResolutionError(new ResolutionMessage(minNode.location, NumberWithinRangeError)(None, List.empty, min, max))
  }

  def min: Option[Long] = if (minNode.number.isDefined) Some(minNode.number.get) else None

  def max: Option[Long] = if (maxNode.number.isDefined) Some(maxNode.number.get) else None

  //  override def toString: String = {
  //    "( " + minNode.toString + ", " + maxNode.toString + " )"
  //  }
}

case class ShortRangeNode(ctx: Option[Context] = None, minNode: ShortNode, maxNode: ShortNode) extends ValidateRange[Short](ctx, minNode, maxNode) {
  if (minNode.number.isDefined && maxNode.number.isDefined && minNode.number.get.compareTo(maxNode.number.get) > 0) {
    val rm = new ResolutionMessage(minNode.location, NumberRangeError)(None, List.empty, minNode.number.get, maxNode.number.get)
    if (ctx.isDefined)
      ctx.get.addResolutionError(rm)
    else
      throw new com.schemarise.alfa.compiler.AlfaInternalException(rm.toString)
  }

  def validate(ctx: Context, min: Short, max: Short) = {
    if (minNode.number.isDefined && minNode.number.get < min)
      ctx.addResolutionError(new ResolutionMessage(minNode.location, NumberWithinRangeError)(None, List.empty, min, max))

    if (maxNode.number.isDefined && maxNode.number.get > max)
      ctx.addResolutionError(new ResolutionMessage(minNode.location, NumberWithinRangeError)(None, List.empty, min, max))
  }

  def min: Option[Short] = if (minNode.number.isDefined) Some(minNode.number.get) else None

  def max: Option[Short] = if (maxNode.number.isDefined) Some(maxNode.number.get) else None

  //  override def toString: String = {
  //    "( " + minNode.toString + ", " + maxNode.toString + " )"
  //  }
}

case class DoubleRangeNode(ctx: Option[Context] = None, minNode: DoubleNode, maxNode: DoubleNode) extends ValidateRange[Double](ctx, minNode, maxNode) {
  if (minNode.number.isDefined && maxNode.number.isDefined && minNode.number.get.compareTo(maxNode.number.get) > 0) {
    val rm = new ResolutionMessage(minNode.location, NumberRangeError)(None, List.empty, minNode.number.get, maxNode.number.get)
    if (ctx.isDefined)
      ctx.get.addResolutionError(rm)
    else
      throw new com.schemarise.alfa.compiler.AlfaInternalException(rm.toString)
  }

  def validate(ctx: Context, min: Double, max: Double) = {
    if (minNode.number.isDefined && minNode.number.get < min)
      ctx.addResolutionError(minNode.location, NumberWithinRangeError, min, max)

    if (maxNode.number.isDefined && maxNode.number.get > max)
      ctx.addResolutionError(minNode.location, NumberWithinRangeError, min, max)
  }

  def min: Option[Double] = if (minNode.number.isDefined) Some(minNode.number.get) else None

  def max: Option[Double] = if (maxNode.number.isDefined) Some(maxNode.number.get) else None

  //  override def toString: String = {
  //    "( " + minNode.toString + ", " + maxNode.toString + " )"
  //  }
}

case class TimeRangeNode(ctx: Context, minNode: StringNode, maxNode: StringNode) extends ValidateRange[LocalTime](Some(ctx), minNode, maxNode) {

  def min: Option[LocalTime] = if (minNode == StringNode.empty) None else parse[LocalTime](minNode, "time", "HH:MM or HH:MM:SS", LocalTime.parse(_))

  def max: Option[LocalTime] = if (maxNode == StringNode.empty) None else parse[LocalTime](maxNode, "time", "HH:MM or HH:MM:SS", LocalTime.parse(_))
}

case class DateRangeNode(ctx: Context, minNode: StringNode, maxNode: StringNode) extends ValidateRange[ChronoLocalDate](Some(ctx), minNode, maxNode) {
  def min: Option[ChronoLocalDate] = if (minNode == StringNode.empty) None else parse[LocalDate](minNode, "date", "YYYY-MM-DD", LocalDate.parse(_))

  def max: Option[ChronoLocalDate] = if (maxNode == StringNode.empty) None else parse[LocalDate](maxNode, "date", "YYYY-MM-DD", LocalDate.parse(_))
}

case class DatetimeRangeNode(ctx: Context, minNode: StringNode, maxNode: StringNode) extends ValidateRange[LocalDateTime](Some(ctx), minNode, maxNode) {
  def min: Option[LocalDateTime] = if (minNode == StringNode.empty) None else parse[LocalDateTime](minNode, "datetime", "YYYY-MM-DD'T'HH:MM:SS", LocalDateTime.parse(_))

  def max: Option[LocalDateTime] = if (maxNode == StringNode.empty) None else parse[LocalDateTime](maxNode, "datetime", "YYYY-MM-DD'T'HH:MM:SS", LocalDateTime.parse(_))
}

case class DurationRangeNode(ctx: Context, minNode: StringNode, maxNode: StringNode) extends ValidateRange[Duration](Some(ctx), minNode, maxNode) {
  def min: Option[Duration] = if (minNode == StringNode.empty) None else parse[Duration](minNode, "duration", "PnYnMnDTnHnMnS", Duration.parse(_))

  def max: Option[Duration] = if (maxNode == StringNode.empty) None else parse[Duration](maxNode, "duration", "PnYnMnDTnHnMnS", Duration.parse(_))
}

case class PeriodRangeNode(ctx: Context, minNode: StringNode, maxNode: StringNode) extends ValidateRange[NormalizedAstPeriod](Some(ctx), minNode, maxNode) {
  def min: Option[NormalizedAstPeriod] = if (minNode == StringNode.empty) None else parse[NormalizedAstPeriod](minNode, "period", "PnYnMnDTnHnMnS", e => new NormalizedAstPeriod(Period.parse(e)))

  def max: Option[NormalizedAstPeriod] = if (maxNode == StringNode.empty) None else parse[NormalizedAstPeriod](maxNode, "period", "PnYnMnDTnHnMnS", e => new NormalizedAstPeriod(Period.parse(e)))
}


