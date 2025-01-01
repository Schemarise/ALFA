package com.schemarise.alfa.generators.importers.jsonschema

import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.IAnnotation
import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, Scalars}
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.ast.nodes.{Annotation, FieldOrFieldRef, _}
import com.schemarise.alfa.compiler.utils._
import com.schemarise.alfa.compiler.{AlfaCompiler, AlfaInternalException, CompilationUnitArtifact, Context}
import com.schemarise.alfa.runtime.AlfaRuntimeException
import org.everit.json.schema._
import org.everit.json.schema.loader.SchemaLoader
import org.json.{JSONObject, JSONTokener}

import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object AlfaTypeBuilder {
  def typeNameFromLocation(s: String) = {
    val fn = new File(s);
    val fname = fn.getName
    val typeName = fname.split("\\.").head

    val validName = typeName.map(c => {
      if (Character.isJavaIdentifierPart(c))
        c
      else
        "_"
    }).mkString("")

    validName
  }
}

class AlfaTypeBuilder(logger: ILogger, ctx: Context) {

  val counter = new AtomicInteger()

  //  val asserts = new java.util.ArrayList[String]()

  val fragments = new java.util.HashMap[String, ListBuffer[String]]()

  //  private val localUdts = new ListBuffer[UdtBaseNode]()

  val ai = new AtomicInteger()
  val cu = ctx.readScript(None, AlfaCompiler.builtinAnnotations)
  new CompilationUnitArtifact(ctx, cu)

  def validateName(name: String) = {
    if (name == null)
      throw new RuntimeException

    name.replaceAll(" ", "_").replace(':', '_').replace('/', '_').replace('#', '_')
  }

  def intNode(i: Number, adjust: Number => Number = x => x): IntNode =
    if (i == null)
      IntNode(number = None)()
    else
      IntNode(number = Some(adjust(i).intValue()))()

  def doubleNode(i: Number, adjust: Number => Number = x => x): DoubleNode =
    if (i == null)
      DoubleNode(number = None)()
    else
      DoubleNode(number = Some(adjust(i).doubleValue()))()

  def schemaToUdtDataType(s: Schema) = {
    val typeName = AlfaTypeBuilder.typeNameFromLocation(s.getSchemaLocation)
    UdtDataType.fromName(typeName)
  }

  def loadJsonSchemaAndRegisterType(s: Schema) = {

  }

  def genAlfaModel(jsonSchema: Path, namespace: String) = {

    val schemaStr = VFS.read(jsonSchema)
    val rawSchema = new JSONObject(new JSONTokener(schemaStr))
    val schema: Schema = SchemaLoader.load(rawSchema, new AlfaJsonSchemaClient(jsonSchema.getParent))

    val rootTypeName = AlfaTypeBuilder.typeNameFromLocation(jsonSchema.toString)

    fragments.clear()

    convert(schema, true, rootTypeName)

    val f = fragments.get(rootTypeName)
    if (f != null)
      f.mkString("\n")
    else
      ""

    //    println("asserts: " + asserts.asScala.mkString("\n"))

    //
    //    val root = ctx.registry.getUdt(None, UdtDataType.fromName(rootTypeName), false)
    //
    //    val udts = if (root.isDefined)
    //      Seq(root.get)
    //    else {
    //      Seq.empty
    //    }
    //
    //    val nn = new NamespaceNode(collectedUdts = udts, nameNode = StringNode.create(namespace))
    //    val cu = new CompilationUnit(ctx = ctx, namespaces = Seq(nn))
    //    val cua = new CompilationUnitArtifact(ctx, cu)
    //
    ////    val locals = localUdts.mkString("\n")
    //
    //    if (root.isDefined) {
    //      s"""namespace $namespace
    //         |
    //         |${root.get.toString}
    //         |
    //         |$locals
    //         |${fragments.asScala.mkString("\n\n")}
    //       """.stripMargin
    //    }
    //    else {
    //      logger.warn("No types were found in " + jsonSchema.getFileName)
    //      ""
    //    }
  }

  private def convert(s: Schema, isRoot: Boolean = false, rootTypeName: String = "",
                      accessPrefix: String = "", schemaDescr: String = ""): (DataType, Option[IExpression]) = {

    val res: (DataType, Option[IExpression]) = s match {
      case x: ObjectSchema => {

        //        if (x.requiresObject()) {
        val fields = x.getPropertySchemas.asScala.map(z => {
          val name = z._1

          //            if ( name.equals("identity"))
          //              println(222)

          val descr = z._2.getDescription

          val suggestedName = rootTypeName + "_" + name
          val dt = convert(z._2, false, suggestedName, name + "?.", descr)

          val ft = if (x.getRequiredProperties.contains(name)) dt._1 else EnclosingDataType(encType = Enclosed.opt, declComponentType = dt._1)

          val fdescr = z._2.getDescription
          val nm = if (fdescr != null && fdescr.size > 0) NodeMeta.withDoc(fdescr) else NodeMeta.empty
          val f = new Field(rawNodeMeta = nm, nameNode = StringNode.create(validateName(name)), declDataType = ft, rawExpression = dt._2)
          f
        }).toSeq

        val allFields = if (fields.size == 0 && x.getRequiredProperties.size() > 0)
          x.getRequiredProperties.asScala.map(j => new Field(nameNode = StringNode.create(j), declDataType = ScalarDataType.voidType))
        else
          fields

        val anns = if (x.permitsAdditionalProperties()) {
          val an = new Annotation(nameNode = StringNode.create(IAnnotation.Annotation_Parse_SkipUnknownFields))
          Seq(an)
        }
        else
          Seq.empty

        // We wont create tuple as it displays poorly in docs
        //        if (isRoot) {
        val nm = if (x.getDescription != null)
          NodeMeta.withDoc(x.getDescription, anns)
        else
          NodeMeta(annotations = anns)

        val r = new Record(nodeMeta = nm, nameNode = StringNode.create(rootTypeName),
          fields = allFields.map(u => new FieldOrFieldRef(u)), imports = Seq.empty)
        ctx.registry.registerUdt(r)
        (r.asDataType, None)
        //        }
        //        else
        //          (TupleDataType.apply(allFields, NodeMeta( annotations = anns)), None)
      }

      case x: CombinedSchema => {

        val sl = x.getSubschemas.asScala.toList

        sl match {
          case List(a: ReferenceSchema, _*) =>
            if (sl.size == 1) {
              convert((a))
            }
            else {
              val dq = sl.map(z => convert(z)).map(_._1)
              val names = sl.map(z => convert(z)).map(e => StringNode.create(e._1.toString.replace('.', '_')))
              (new TupleDataType(declComponentTypes = dq, fieldNames = names), None)
              //
              //              val nm = if (x.getDescription != null)
              //                NodeMeta.withDoc(x.getDescription)
              //              else
              //                NodeMeta.empty
              //
              //              val tup = new TupleDataType(declComponentTypes = dq).syntheticRecord.fields
              //              val r = new Record(nodeMeta = nm, nameNode = StringNode.create(rootTypeName), fields = tup )
              //              ctx.registry.registerUdt(r)
              //              (r.asDataType, None)
            }

          case List(a: EnumSchema, _*) =>
            val t = schemaToUdtDataType(x)
            convert(a, true, rootTypeName, accessPrefix, x.getDescription)

          case List(_, a: EnumSchema) =>
            val t = schemaToUdtDataType(x)
            convert(a, true, rootTypeName, accessPrefix, x.getDescription)

          //          case List( cs : ConditionalSchema ) =>
          //            ???

          case _ =>
            handleCombinedSchema(x, isRoot, rootTypeName, accessPrefix)
        }
      }

      case a: StringSchema =>
        val t =
          if (a.getFormatValidator != null && a.getFormatValidator.formatName() != "unnamed-format") {
            val fm = a.getFormatValidator.formatName()

            if (fm == "date-time")
              new ScalarDataType(scalarType = Scalars.datetime)
            else if (fm == "time")
              new ScalarDataType(scalarType = Scalars.time)
            else if (fm == "date")
              new ScalarDataType(scalarType = Scalars.date)
            else if (fm == "duration")
              new ScalarDataType(scalarType = Scalars.duration)
            else if (fm == "uuid")
              new ScalarDataType(scalarType = Scalars.uuid)
            else if (fm == "uri")
              new ScalarDataType(scalarType = Scalars.string)
            else
              throw new AlfaInternalException("Unsupported string format " + fm)
          }
          else if (a.getMinLength == null && a.getMaxLength == null && a.getPattern == null)
            ScalarDataType.stringType

          else if (a.getPattern != null)
            new ScalarDataType(scalarType = Scalars.string, formatArg = Some(StringNode.create(a.getPattern.pattern())))

          else {
            val range = Some(IntRangeNode(minNode = intNode(a.getMinLength), maxNode = intNode(a.getMaxLength)))
            new ScalarDataType(scalarType = Scalars.string, sizeRange = range)
          }
        (t, None)

      case a: NumberSchema =>
        val t = if (a.requiresInteger()) {
          val min = if (a.getMinimum != null) intNode(a.getMinimum) else intNode(a.getExclusiveMinimumLimit, (x) => x.intValue() + 1)
          val max = if (a.getMaximum != null) intNode(a.getMaximum) else intNode(a.getExclusiveMaximumLimit, (x) => x.intValue() - 1)

          val range = if (min.number.isEmpty && max.number.isEmpty) None else Some(IntRangeNode(minNode = min, maxNode = max))

          if (a.requiresInteger())
            new ScalarDataType(TokenImpl.empty, Scalars.int, sizeRange = range)
          else
            new ScalarDataType(TokenImpl.empty, Scalars.long, sizeRange = range)
        }
        else {
          val min = if (a.getMinimum != null) doubleNode(a.getMinimum) else doubleNode(a.getExclusiveMinimumLimit, (x) => x.doubleValue() + Double.MinValue)
          val max = if (a.getMaximum != null) doubleNode(a.getMaximum) else doubleNode(a.getExclusiveMaximumLimit, (x) => x.doubleValue() - Double.MinValue)

          val range = if (min.number.isEmpty && max.number.isEmpty) None else Some(DoubleRangeNode(minNode = min, maxNode = max))

          new ScalarDataType(TokenImpl.empty, Scalars.double, sizeRange = range)
        }
        (t, None)


      case x: EnumSchema => {

        val fs = x.getPossibleValuesAsList.asScala.map(e => {
          val c = e.toString

          val cont = if (LangKeywords.isTargetLangKeyword(c))
            s"`$c`"
          else
            c

          val lex = if (LangKeywords.isTargetLangKeyword(c))
            Some(StringNode.create(c))
          else
            None

          new Field(nameNode = StringNode.create(cont),
            declDataType = ScalarDataType.voidType,
            enumLexicalNode = lex)
        }
        ).toSeq

        val t = createEnumAndGetDataType(rootTypeName, schemaDescr, fs)
        (t, None)
      }

      case x: BooleanSchema =>
        (ScalarDataType.booleanType, None)

      case x: NullSchema =>
        logger.warn("Use of null schema type is not recommended " + x.getLocation)
        (ScalarDataType.stringType, None)

      case x: TrueSchema =>
        (ScalarDataType.booleanType, None)

      case x: FalseSchema =>
        (ScalarDataType.booleanType, None)

      case x: ReferenceSchema => {
        val t = schemaToUdtDataType(x.getReferredSchema)

        val resultDataType: DataType =
          if (ctx.registry.getUdt(None, t, false).isDefined) {
            t
          }
          else {
            //            val location = x.getReferredSchema.getSchemaLocation

            val converted = convert(x.getReferredSchema, true, t.fullyQualifiedName, "", x.getDescription)._1

            val local =
              if (converted.isInstanceOf[TupleDataType]) {
                val tdt = converted.asInstanceOf[TupleDataType]

                val flds = tdt.getAsFields().map(d => new FieldOrFieldRef(d))
                val l = new Record(nameNode = StringNode.create(t.fullyQualifiedName),
                  fields = flds, imports = Seq.empty)
                ctx.registry.registerUdt(l)
                //                localUdts += l
                l.asDataType
              }
              else if (converted.isInstanceOf[EnumDataType]) {
                val tdt = converted.asInstanceOf[EnumDataType]
                createEnumAndGetDataType(t.fullyQualifiedName, schemaDescr, tdt.fields)
              }
              else {
                converted
              }

            local

            //            }
            //            else {
            //              // create stub record for externally referenced type
            //              val l = new Record(nameNode = StringNode.create(t.fullyQualifiedName))
            //              ctx.registry.registerUdt(l)
            //              l.asDataType
            //            }

          }

        val result = (resultDataType, None)

        result
      }

      case x: ArraySchema => {

        val elType = convert(x.getAllItemSchema, isRoot, rootTypeName, "")

        val min = intNode(x.getMinItems)
        val max = intNode(x.getMaxItems)
        val range = if (min.number.isEmpty && max.number.isEmpty) None else Some(IntRangeNode(minNode = min, maxNode = max))

        val dt =
          if (x.needsUniqueItems())
            SetDataType(declComponentType = elType._1)(sizeRange = range)
          else
            ListDataType(declComponentType = elType._1)(sizeRange = range)

        (dt, None)
      }

      case x: EmptySchema =>
        (ScalarDataType.voidType, None)

      case _ =>
        throw new AlfaRuntimeException("Unsupported schema " + s.getClass.getName + " " + s.getSchemaLocation + ". " +
          "\nPlease contact info@schemarise.com")
    }

    res
  }

  private def createEnumAndGetDataType(fullyQualifiedName: String, descr: String, fields: Seq[Field]) = {
    val meta = if (descr != null && descr.trim.length > 0)
      NodeMeta.withDoc(descr.trim)
    else
      NodeMeta.empty

    val l = new EnumDecl(
      nameNode = StringNode.create(fullyQualifiedName),
      fieldsNode = fields.map(d => new FieldOrFieldRef(d)),
      enNodeMeta = meta, imports = Seq.empty
    )

    ctx.registry.registerUdt(l)
    l.asDataType
  }

  private def handleCombinedSchema(cs: CombinedSchema, isRoot: Boolean,
                                   rootTypeName: String, accessPrefix: String = ""): (DataType, Option[IExpression]) = {

    // Any ReferenceSchemas should be converted into alfa
    val dereferenced = cs.getSubschemas.asScala.filter(_.isInstanceOf[ReferenceSchema]).map(_.asInstanceOf[ReferenceSchema].getReferredSchema)
    dereferenced.foreach(d => convert(d))

    val cm = new CombinedModel(cs, accessPrefix)
    val res = convert(cm.outputSchema, isRoot, rootTypeName)

    val c = cm.outputCriteria

    val flds = res._1 match {
      case t: TupleDataType =>
        t.itemsAsFields()
      case r: UdtDataType =>
        r.resolvedType.get.rawDeclaredFields.map(_.field.get)
    }

    val mandFields = flds.filter(f => !f.dataType.isEncOptional()).map(_.name)

    val at = c.toAssertText(mandFields)

    if (at.size > 0) {
      val asert =
        s"""
           |fragment record $rootTypeName {
           |    assert Validate$rootTypeName${counter.getAndIncrement()} {
           |$at
           |    }
           |}
       """.stripMargin

      var f = fragments.get(rootTypeName)
      if (f == null) {
        f = new ListBuffer[String]()
        fragments.put(rootTypeName, f)
      }

      f.append(asert)
    }

    res
  }
}