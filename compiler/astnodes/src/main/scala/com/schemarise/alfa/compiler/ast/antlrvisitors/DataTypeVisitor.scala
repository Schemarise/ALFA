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
package com.schemarise.alfa.compiler.ast.antlrvisitors

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser._
import com.schemarise.alfa.compiler.antlr.{AlfaBaseVisitor, AlfaParser}
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.model.types.Vectors.VectorType
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.err.{IncorrectParametersToType, NumberTooLargeForInt, ParserError, ResolutionMessage}
import com.schemarise.alfa.compiler.utils.TokenImpl
import org.antlr.v4.runtime.Token

import scala.collection.JavaConverters._

class DataTypeVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[DataType](resolveCtx) {

  override def visitExtendOrIncludeDef(ctx: ExtendOrIncludeDefContext): DataType = {
    super.visitExtendOrIncludeDef(ctx)
  }

  override def visitFieldType(ctx: FieldTypeContext): DataType = {
    if (ctx == null) {
      new ErrorableDataType(Left(ResolutionMessage(TokenImpl.empty, ParserError)(None, List.empty, "Failed to read datatype")))
    }
    else if (hasChildParseException(ctx)) {
      new ErrorableDataType(Right(readStringNode(ctx.start)))
    }
    else {
      val ft: DataType = _visit(ctx)
      if (ctx.opt != null) {
        val loc = readToken(ctx.opt)
        new EnclosingDataType(loc, Enclosed.opt, ft)
      } else
        ft
    }
  }

  def readOptInt(i: Token): Option[NumericNode] =
    if (i == null)
      None
    else if (i.getText.equals("*")) // wildcard
      None
    else
      Some(new NumericNode(Scalars.int, Integer.parseInt(i.getText))(readToken(i)))


  def readOptDouble(i: Token): Option[NumericNode] =
    if (i == null)
      None
    else {
      val t = i.getText
      if (t.equals("*"))
        None
      else
        Some(new NumericNode(Scalars.double, t.toDouble)(readToken(i)))
    }

  def readOptStringRangeNode(i: Token): Option[StringNode] =
    if (i == null)
      None
    else {
      val t = i.getText
      if (t.equals("*"))
        None
      else {
        val t = readToken(i)
        Some(StringNode(t, t.getText))
      }
    }

  def visitSet(vt: VectorTypeContext): SetDataType = {
    val uniqueFields: Seq[StringNode] = readUniqueFields(vt.setUniqueFields())

    val intRange = visitIntRangeParams(Scalars.int, vt.intRangeParams())

    new SetDataType(visitFieldType(vt.ft1))(readToken(vt), intRange, uniqueFields)
  }

  def visitMap(vt: VectorTypeContext): MapDataType = {
    val keyType: DataType = visitFieldType(vt.keyType.fieldType)
    val valueType: DataType = visitFieldType(vt.valType.fieldType)

    val intRange = visitIntRangeParams(Scalars.int, vt.intRangeParams())

    val keyFieldName: Option[StringNode] = readOptStringNode(vt.keyType.name)
    val valueFieldName: Option[StringNode] = readOptStringNode(vt.valType.name)

    new MapDataType(keyType, valueType)(readToken(vt), intRange, keyFieldName, valueFieldName)
  }

  def visitSeq(vt: VectorTypeContext): DataType = {
    val intRange = visitIntRangeParams(Scalars.int, vt.intRangeParams())

    val ft = visitFieldType(vt.ft1)
    ListDataType(ft)(readToken(vt), intRange)
  }

  def visitTuple(vt: VectorTypeContext): TupleDataType = {
    val types = vt.embeddedField().asScala.filter(_.fieldType() != null).map(e => visitFieldType(e.fieldType))
    val names = vt.embeddedField().asScala.map(e => readOptStringNode(e.name)).flatten
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, vt.docAndAnnotations(), null)
    new TupleDataType(readToken(vt), types, names, Seq.empty, meta)
  }

  def readEmbeddedFields(fs: Seq[AlfaParser.EmbeddedFieldContext]) = {
    fs.map(e => {
      val name = readOptStringNode(e.name)
      val dtype = if (e.fieldType() != null) Some(visitFieldType(e.fieldType)) else None
      (name, dtype)
    })
  }

  def visitUnion(vt: VectorTypeContext): UnionDataType = {

    val isUntagged = vt.untaggedTypes() != null


    val fields: Seq[(Option[StringNode], Option[DataType])] =
      if (isUntagged) {
        val types = j2sNoParseExcpStream(vt.untaggedTypes.fieldType()).map(ft => {
          visitFieldType(ft)
        })
        val s = UnionVisitor.unionTypeFields(types)
        s.map(e => {
          val f = e.field.get
          (Some(f.nameNode), Some(f.dataType))
        })
      }
      else {
        readEmbeddedFields(vt.embeddedField().asScala)
      }

    new UnionDataType(readToken(vt), fields, isUntagged)
  }

  def visitEnum(vt: VectorTypeContext): EnumDataType = {
    val fields = vt.idWithDoc().asScala.map(e => {
      val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, e.docAndAnnotations(), e.sameline_docstrings())
      val sn = readStringNode(e.idOnly())
      val literal = if (sn.wasEscaped) Some(sn) else None
      new Field(sn.location, meta, false, sn, ScalarDataType.voidType, literal)
    })
    new EnumDataType(readToken(vt), fields.toSeq)
  }

  def visitUdt(udt: IdOrQidWithOptTmplArgRefsContext, nm: Option[NodeMeta] = None): UdtDataType = {
    val name: StringNode = readStringNode(udt.versionedIdOrQid().idOrQid())
    val version: Option[IntNode] = readOptVersion(udt.versionedIdOrQid().versionMarker())
    val typeArguments: Option[Seq[DataType]] = readTypeArguments(this, udt.typeArguments())
    new UdtDataType(readToken(udt), namespace, name, version, typeArguments, false, nm)
  }

  def visitUdtOrTypedefed(udt: IdOrQidWithOptTmplArgRefsContext): DataType = {
    val name: StringNode = readStringNode(udt.versionedIdOrQid().idOrQid())
    val typeArguments: Option[Seq[DataType]] = readTypeArguments(this, udt.typeArguments())

    name.text match {
      case "map" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 2) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "map<K,V>", "2 type parameters expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val keyType: DataType = typeArguments.get.head
          val valueType: DataType = typeArguments.get.last

          new MapDataType(keyType, valueType)(readToken(udt), None, None, None)
        }

      //      case "key" =>
      //        if ( typeArguments.isEmpty || typeArguments.get.size != 1 ) {
      //          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)( "key<E>", "1 type parameter expected" )
      //          resolveCtx.addResolutionError(rm )
      //          new ErrorableDataType(Some(rm))
      //        }
      //        else {
      //          val keyType: DataType = typeArguments.get.head
      //          new EnclosingDataType(readToken(udt), Enclosed.key, keyType)
      //        }

      case "tuple" =>
        if (typeArguments.isEmpty) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "tuple<T1,T2,..>", "type parameters expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          new TupleDataType(readToken(udt), typeArguments.get)
        }

      case "enum" =>
        if (typeArguments.isEmpty) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "enum<C1, C2, ..>", "Constants parameters expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val fields = typeArguments.get.map(tx => {
            val sn = tx.asInstanceOf[UdtOrTypeDefedDataType].name

            val lexValue = if (sn.wasEscaped) Some(sn) else None
            new Field(sn.location, NodeMeta.empty, false, sn, ScalarDataType.voidType, lexValue)
          })

          new EnumDataType(readToken(udt), fields)
        }

      case "func" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 2) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "func<A,R>", "2 type parameters expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val arg: DataType = typeArguments.get.head
          val res: DataType = typeArguments.get.last

          new LambdaDataType(readToken(udt), Seq(arg), res)
        }

      case "stream" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 1) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "stream<T>", "1 type parameter expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val t: DataType = typeArguments.get.head
          new EnclosingDataType(readToken(udt), Enclosed.stream, t)
        }

      //      case "key" =>
      //        if (typeArguments.isEmpty || typeArguments.get.size != 1) {
      //          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(List.empty, "key<T>", "1 type parameter expected")
      //          resolveCtx.addResolutionError(rm)
      //          new ErrorableDataType(Left(rm))
      //        }
      //        else {
      //          val t: DataType = typeArguments.get.head
      //          new EnclosingDataType(readToken(udt), Enclosed.keyOf, t)
      //        }

      case "future" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 1) {
          new UdtOrTypeDefedDataType(readToken(udt), namespace, name, None, typeArguments)
          //          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(List.empty, "future<T>", "1 type parameter expected")
          //          resolveCtx.addResolutionError(rm)
          //          new ErrorableDataType(Left(rm))
        }
        else {
          val t: DataType = typeArguments.get.head
          new EnclosingDataType(readToken(udt), Enclosed.future, t)
        }


      case "set" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 1) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "set<T>", "1 type parameter expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val t: DataType = typeArguments.get.head
          new SetDataType(t)(readToken(udt), None, Nil)
        }

      case "list" =>
        if (typeArguments.isEmpty || typeArguments.get.size != 1) {
          val rm = new ResolutionMessage(readToken(udt), IncorrectParametersToType)(None, List.empty, "list<T>", "1 type parameter expected")
          resolveCtx.addResolutionError(rm)
          new ErrorableDataType(Left(rm))
        }
        else {
          val t: DataType = typeArguments.get.head
          ListDataType(t)(readToken(udt), None)
        }

      case _ =>
        val version: Option[IntNode] = readOptVersion(udt.versionedIdOrQid().versionMarker())
        new UdtOrTypeDefedDataType(readToken(udt), namespace, name, version, typeArguments)
    }

  }

  private def _visit(ctx: FieldTypeContext): DataType = {
    val udt = ctx.idOrQidWithOptTmplArgRefs
    val st = ctx.scalarType
    val vt = ctx.vectorType
    val et = ctx.enclosedType
    val lm = ctx.lambdaType
    val mt = ctx.metaType
    val an = ctx.anyType

    if (st != null) {
      visitScalar(st)
    }
    else if (vt != null) {
      visitVector(vt)
    }
    else if (et != null) {
      visitEnclosed(et)
    }
    else if (mt != null) {
      MetaDataType(readToken(mt), MetaType.withEnumName(mt.mType.getText))
    }
    else if (udt != null) {
      visitUdtOrTypedefed(udt)
    }
    else if (lm != null) {
      visitLambda(lm)
    }
    else if (an != null) {
      new AnyDataType(readToken(an))
    }
    else {
      ErrorableDataType(Right(readStringNode(ctx.start)))
    }
  }

  private def visitVector(vt: VectorTypeContext): DataType = {
    val vecType: VectorType = Vectors.withEnumName(vt.vtName.getText)

    vecType match {
      case Vectors.set => visitSet(vt)
      case Vectors.`list` => visitSeq(vt)
      case Vectors.map => visitMap(vt)
      case Vectors.tuple => visitTuple(vt)
      case Vectors.union => visitUnion(vt)
      case Vectors.enum => visitEnum(vt)
    }
  }

  private def readOptInt(p: SizeIntParamContext): Option[Int] = {
    if (p == null || p.INT() == null)
      None
    else {

      Some(p.INT().getText.toInt)
    }
  }

  private def readOptFloat(p: SizeDoubleParamContext): Option[Double] = {

    if (p == null) None
    else if (p.FLOAT() != null) {
      Some(p.FLOAT().getText.toDouble)
    }
    else if (p.INT() != null) {
      Some(p.INT().getText.toDouble)
    }
    else {
      None
    }
  }

  private def readOptLong(p: SizeIntParamContext): Option[Long] = {

    if (p == null || p.INT() == null) None
    else {
      Some(p.INT().getText.toLong)
    }
  }

  private def visitIntRangeParams(scalarType: ScalarType, ctx: AlfaParser.IntRangeParamsContext): Option[IntRangeNode] = {
    if (ctx == null)
      None
    else {
      val f: Option[Int] = readOptInt(ctx.from) // sv.intLiteral(ctx.from.INT().getSymbol) //  sv.visitSignedInt(ctx.from.signedInt())
      val t: Option[Int] = readOptInt(ctx.to) // sv.visitSignedInt(ctx.to.signedInt())

      Some(IntRangeNode(Some(resolveCtx), new IntNode(scalarType, f)(readToken(ctx.from)), new IntNode(scalarType, t)(readToken(ctx.to))))
    }
  }

  private def visitIntOrLongRangeParams(scalarType: ScalarType, ctx: AlfaParser.IntRangeParamsContext): Option[IDataTypeSizeRange[_]] = {
    if (ctx == null)
      None
    else {
      val fl: Option[Long] = readOptLong(ctx.from) // sv.visitSignedLong(ctx.from.signedInt())
      val tl: Option[Long] = readOptLong(ctx.to) // sv.visitSignedLong(ctx.to.signedInt())


      var errored = false

      if (scalarType == Scalars.int && fl.isDefined && fl.get > Int.MaxValue) {
        resolveCtx.addResolutionError(readToken(ctx.from), NumberTooLargeForInt)
        errored = true
      }

      if (scalarType == Scalars.int && tl.isDefined && tl.get > Int.MaxValue) {
        resolveCtx.addResolutionError(readToken(ctx.to), NumberTooLargeForInt)
        errored = true
      }

      if (!errored && scalarType == Scalars.int) {
        val f: Option[Int] = readOptInt(ctx.from) // sv.visitSignedInt(ctx.from.signedInt())
        val t: Option[Int] = readOptInt(ctx.to) // sv.visitSignedInt(ctx.to.signedInt())

        Some(IntRangeNode(Some(resolveCtx), new IntNode(scalarType, f)(readToken(ctx.from)), new IntNode(scalarType, t)(readToken(ctx.to))))
      }
      else {
        Some(LongRangeNode(Some(resolveCtx), new LongNode(scalarType, fl)(readToken(ctx.from)), new LongNode(scalarType, tl)(readToken(ctx.to))))
      }
    }
  }

  private def visitDoubleRangeParams(scalarType: ScalarType, ctx: AlfaParser.DoubleRangeParamsContext): Option[DoubleRangeNode] = {
    if (ctx == null)
      None
    else {
      val f: Option[Double] = readOptFloat(ctx.from)
      val t: Option[Double] = readOptFloat(ctx.to)

      Some(DoubleRangeNode(Some(resolveCtx), new DoubleNode(scalarType, f)(readToken(ctx.from)), new DoubleNode(scalarType, t)(readToken(ctx.to))))
    }
  }

  private def visitStringRangeParams(scalarType: ScalarType, ctx: AlfaParser.StringRangeParamsContext): Option[IDataTypeSizeRange[_]] = {
    if (ctx == null)
      None
    else {
      val fr = if (ctx.from.STAR() != null) StringNode.empty else new QuotedStringNode(readStringNode(ctx.from.STRING()))
      val to = if (ctx.to.STAR() != null) StringNode.empty else new QuotedStringNode(readStringNode(ctx.to.STRING()))

      scalarType match {
        case Scalars.date => Some(DateRangeNode(resolveCtx, fr, to))
        case Scalars.datetime => Some(DatetimeRangeNode(resolveCtx, fr, to))
        case Scalars.datetimetz => Some(DatetimeRangeNode(resolveCtx, fr, to))
        case Scalars.duration => Some(DurationRangeNode(resolveCtx, fr, to))
        case Scalars.period => Some(PeriodRangeNode(resolveCtx, fr, to))
        case Scalars.time => Some(TimeRangeNode(resolveCtx, fr, to))
        case _ =>
          throw new com.schemarise.alfa.compiler.AlfaInternalException("Unhandled range type " + scalarType)
      }
    }
  }

  private def visitLambda(lm: AlfaParser.LambdaTypeContext): LambdaDataType = {

    val argTypes = if (lm.lambdaArgs.fieldType() == null) Nil
    else j2sNoParseExcpStream(lm.lambdaArgs().fieldType()).map(e => visitFieldType(e))

    val resultType = visitFieldType(lm.resultType)

    new LambdaDataType(readToken(lm), argTypes, resultType)
  }

  private def visitScalar(st: ScalarTypeContext): ScalarDataType = {
    val scale = readOptInt(st.scale)
    val precision = readOptInt(st.precision)

    val scalarType = Scalars.withEnumName(st.name.getText)

    val sizeRange: Option[IDataTypeSizeRange[_]] =
      if (st.intRangeParams() != null)
        visitIntOrLongRangeParams(scalarType, st.intRangeParams())

      else if (st.doubleRangeParams() != null)
        visitDoubleRangeParams(scalarType, st.doubleRangeParams())

      else if (st.stringRangeParams() != null)
        visitStringRangeParams(scalarType, st.stringRangeParams())

      else None

    val precisionAndScaleArgs: Seq[NumericNode] = Seq(precision, scale).flatten
    val formatArg = if (st.format != null)
      readOptStringNode(st.format)
    else
      None

    new ScalarDataType(readToken(st), Scalars.withEnumName(st.name.getText), precisionAndScaleArgs, formatArg, sizeRange)
  }

  private def readUniqueFields(context: SetUniqueFieldsContext): Seq[StringNode] = {
    if (context == null)
      List.empty[StringNode]
    else
      readStringNodes(context.idOnly)
  }

  private def visitEnclosed(et: AlfaParser.EnclosedTypeContext): DataType = {
    val encType = Enclosed.withEnumName(et.encType.getText)
    val t = readToken(et)

    if (encType == Enclosed.table)
      new TablularDataType(t, visitFieldType(et.ft))

    else if (encType == Enclosed.either)
      new EitherDataType(t, visitFieldType(et.ft), visitFieldType(et.right))

    else if (encType == Enclosed.pair)
      new PairDataType(t, visitFieldType(et.ft), visitFieldType(et.right))

    else
      new EnclosingDataType(t, encType, visitFieldType(et.ft))
  }
}
