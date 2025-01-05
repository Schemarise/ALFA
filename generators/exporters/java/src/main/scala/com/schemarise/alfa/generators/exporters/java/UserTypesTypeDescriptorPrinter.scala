package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path
import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.nodes.datatypes.TypeParameterDataType
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes.{Expression, Key, UdtBaseNode}
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, ILogger, LangKeywords}
import com.schemarise.alfa.generators.common.{CompilerToRuntimeTypes, NonConvertibleToBuilderVisitor, ReferencesTraitVisitor}
import com.schemarise.alfa.runtime.{Alfa, AlfaRuntimeException}
import schemarise.alfa.runtime.model.ModifierType

import scala.collection.JavaConverters._


class UserTypesTypeDescriptorPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes)
  extends PrinterBase(logger, outputDir, cua) {

  private val jsonStringEncoder = new JsonStringEncoder()
  private var isBootstrapType = false

  private def scalarTypeValue(s: ScalarType) = {
    val n = s.toString
    val namePart = if (n.contains('.')) n.substring(n.lastIndexOf('.')) else n
    namePart.toLowerCase()
  }

  private def supplier(t: IUdtBaseNode, f: IField): String = {
    val parentJavaType = toJavaTypeName(t)
    val n = localFieldName(f.name)
    val fieldDataType = f.dataType
    val fn = f.name
    val fieldTypeVar = s"""MODEL.getAllFields().get(${fieldName(f)}).getDataType()"""

    val code = fieldDataType match {
      case d: ITypeParameterDataType =>
        ""
      case _ =>
        s"""private final ${nestedSupplier(fieldDataType, fn, 1, "", fieldTypeVar + "")}
           |        private final java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier = ( p, consumer ) -> {
           |            ${n}SupplierInner1.accept( p.get${pascalCase(fn)}(), consumer );
           |        };""".stripMargin
    }

    s"""
       |        /* -- Supplier ${fieldDataType} -- */
       |$code""".stripMargin
  }

  private def nestedSupplier(fieldDataType: IDataType, fn: String, level: Int, varSuffix: String, typeAccessCallChain: String): String = {
    val parentJavaType = toJavaTypeName(fieldDataType, true, false)
    val n = localFieldName(fn)
    val l = if (level == 0) "" else "Inner" + level.toString
    val nextl = "Inner" + (level + 1).toString

    val jDeclType = buildDeclAndDataType(fieldDataType)._1
    val castedType = s"( ($jDeclType) $typeAccessCallChain )"

    val code = fieldDataType match {
      case t: ISetDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.componentType, fn, level + 1, "", castedType + ".getComponentType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier$nextl  );
           |    };""".stripMargin

      case t: IListDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.componentType, fn, level + 1, "", castedType + ".getComponentType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier$nextl  );
           |    };""".stripMargin


      case t: IPairDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.left, fn, level + 1, "Left", castedType + ".getLeftComponentType()")}
           |${nestedSupplier(t.right, fn, level + 1, "Right", castedType + ".getRightComponentType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier${nextl}Left, ${n}Supplier${nextl}Right );
           |    };""".stripMargin

      case t: IEitherDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.left, fn, level + 1, "Left", castedType + ".getLeftComponentType()")}
           |${nestedSupplier(t.right, fn, level + 1, "Right", castedType + ".getRightComponentType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier${nextl}Left, ${n}Supplier${nextl}Right );
           |    };""".stripMargin

      case t: IEnclosingDataType =>
        //        if ( t.isKey ) // do on expand to the key<T> component T, process key<T>
        //          s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l )-> {
        //             |        consumer$l.consume( $castedType, p$l );
        //             |    };""".stripMargin
        //        else
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.componentType, fn, level + 1, "", castedType + ".getComponentType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier$nextl  );
           |    };""".stripMargin

      case t: IMapDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |${nestedSupplier(t.keyType, fn, level + 1, "Key", castedType + ".getKeyType()")}
           |${nestedSupplier(t.valueType, fn, level + 1, "Val", castedType + ".getValueType()")}
           |        consumer$l.consume( $castedType, p$l, ${n}Supplier${nextl}Key, ${n}Supplier${nextl}Val  );
           |    };""".stripMargin

      case t: ITypeParameterDataType =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |        throw new UnsupportedOperationException("Should be overridden");
           |    };""".stripMargin

      case iudt: IUdtDataType =>
        val tmplArgs = if (iudt.typeArguments.isDefined) {
          val params2Args = iudt.typeParamsToArgs.get
          val argVisitors = params2Args.zipWithIndex.map(e => {
            val entry = e._1

            val argReader = nestedSupplier(entry._2, fn, level + 1, s"Tmpl${e._2}", castedType + s""".getTypeArguments().get().get("${entry._1}")""")
            argReader + "\n" +
              s"""        tmplReaders.put( "${entry._1}", ${n}Supplier$nextl${varSuffix}Tmpl${e._2});\n"""
          }).mkString

          "java.util.Map< String, java.util.function.BiConsumer > tmplReaders = new java.util.LinkedHashMap();\n" +
            argVisitors
        } else ""

        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |        ${tmplArgs}
           |        consumer$l.consume( $castedType, p$l );
           |    };""".stripMargin

      case _ =>
        s"""    java.util.function.BiConsumer< $parentJavaType, com.schemarise.alfa.runtime.DataConsumer > ${n}Supplier$l$varSuffix = ( p$l, consumer$l ) -> {
           |        consumer$l.consume( $castedType, p$l );
           |    };""".stripMargin
    }

    val pad = ("    " * level)

    val indented = code.linesIterator.toList.mkString(pad, "\n" + pad, pad)
    indented
  }

  private def consumer(udt: IUdtBaseNode, f: IField): String = {
    val rawName = f.name
    val jFieldName = localFieldName(f.name)
    val fieldDataType = f.dataType
    val fieldTypeVar = s"""MODEL.getAllFields().get(${fieldName(f)}).getDataType()"""

    val method = (
      f.dataType match {
        case m: IMapDataType => "putAll"
        case m: ISetDataType => "addAll"
        case m: IListDataType => "addAll"
        case _ => "set"
      }) + pascalCase(f.name)

    val code = fieldDataType match {
      case t: TypeParameterDataType =>
        ""
      case _ =>
        s"""private final ${nestedConsumer(fieldDataType, rawName, 1, "", fieldTypeVar + "")}
           |        private final java.util.function.BiConsumer< ${builderImplClz(udt)}, com.schemarise.alfa.runtime.DataSupplier > ${jFieldName}Consumer = ( builder, supplier ) -> {
           |            builder.$method( ${jFieldName}ConsumerInner1.apply(supplier) );
           |        };""".stripMargin
    }

    s"""
       |        /* -- Consumer ${fieldDataType} -- */
       |$code""".stripMargin
  }

  private def nestedConsumer(fieldDataType: IDataType, rawFieldName: String, level: Int, varSuffix: String, typeAccessCallChain: String): String = {
    val resultJavaType = toJavaTypeName(fieldDataType, true, false)
    val jDeclType = buildDeclAndDataType(fieldDataType)._1
    val jFieldName = localFieldName(rawFieldName)

    val l = if (level == 0) "" else "Inner" + level.toString
    val nextl = "Inner" + (level + 1).toString

    val castedType = s"( ($jDeclType) $typeAccessCallChain )"

    val code = fieldDataType match {
      case t: ISetDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.componentType, rawFieldName, level + 1, "", castedType + ".getComponentType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl} );
           |    };""".stripMargin

      case t: IListDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.componentType, rawFieldName, level + 1, "", castedType + ".getComponentType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl} );
           |    };""".stripMargin

      case t: IEitherDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.left, rawFieldName, level + 1, "Left", castedType + ".getLeftComponentType()")}
           |${nestedConsumer(t.right, rawFieldName, level + 1, "Right", castedType + ".getRightComponentType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl}Left, ${jFieldName}Consumer${nextl}Right  );
           |    };""".stripMargin

      case t: IPairDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.left, rawFieldName, level + 1, "Left", castedType + ".getLeftComponentType()")}
           |${nestedConsumer(t.right, rawFieldName, level + 1, "Right", castedType + ".getRightComponentType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl}Left, ${jFieldName}Consumer${nextl}Right  );
           |    };""".stripMargin


      case t: IEnclosingDataType =>
        //        if ( t.isKey ) {
        //          s"""   java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
        //             |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}($fieldTypeVar );
        //             |   };""".stripMargin
        //        }
        //        else
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.componentType, rawFieldName, level + 1, "", castedType + ".getComponentType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl} );
           |    };""".stripMargin

      case t: IMapDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |${nestedConsumer(t.keyType, rawFieldName, level + 1, "Key", castedType + ".getKeyType()")}
           |${nestedConsumer(t.valueType, rawFieldName, level + 1, "Val", castedType + ".getValueType()")}
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}( $castedType, ${jFieldName}Consumer${nextl}Key, ${jFieldName}Consumer${nextl}Val  );
           |    };""".stripMargin

      case t: IUnionDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}($castedType);
           |    };""".stripMargin

      case t: IUdtDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}($castedType);
           |    };""".stripMargin

      case t: ITupleDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |        return supplier$l.${javaDataSupplierMethod(fieldDataType)}($castedType);
           |    };""".stripMargin

      case t: IEnumDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |       return supplier$l.${javaDataSupplierMethod(fieldDataType)}($castedType);
           |    };""".stripMargin

      case t: TypeParameterDataType =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |       throw new UnsupportedOperationException("Should be overridden");
           |    };""".stripMargin

      case _ =>
        s"""    java.util.function.Function< com.schemarise.alfa.runtime.DataSupplier, $resultJavaType > ${jFieldName}Consumer$l$varSuffix = ( supplier$l ) -> {
           |       return supplier$l.${javaDataSupplierMethod(fieldDataType)}($castedType);
           |    };""".stripMargin
    }

    val pad = ("    " * level)

    val indented = code.linesIterator.toList.mkString(pad, "\n" + pad, pad)
    indented
  }

  private def buildDataType(t: IDataType): String = {
    buildDeclAndDataType(t)._2
  }

  private def toJava(e: Expression): String = {
    "null"
  }

  private def docAnnotationToCode(d: IDocAndAnnotated) = {
    if (d.annotations.size > 0) {
      val ann = d.annotationsMap.filter(e => e._1.fullyQualifiedName != IAnnotation.Meta_Field_Annotations).map(e => {
        val annField = e._2
        annField.versionedName

        val annFieldVal = if (annField.objectExpression.isDefined)
          toJava(annField.objectExpression.get.asInstanceOf[Expression])
        else
          null

        s"""put("${annField.versionedName}", $annFieldVal);
           |""".stripMargin

      }).mkString("")

      s"""
         |java.util.Optional.of( new java.util.HashMap() {
         |      {
         |        $ann
         |      }
         |    } )
              """.stripMargin
    }
    else {
      "java.util.Optional.empty()"
    }
  }

  def buildDeclAndDataType(t: IDataType): (String, String) = {
    val rtType = compilerToRt.convert(t)
    val json = Alfa.jsonCodec().toJsonString(rtType)
    val typeRestore = "com.schemarise.alfa.runtime.Alfa.jsonCodec().uncheckedFromJson(\"" + jsonStringEncoder.quoteAsString(json).mkString("") + "\")"

    t match {
      case d: IScalarDataType =>

        cua.getNamespaces()

        val decl = if (isBootstrapType)
          s"schemarise.alfa.runtime.model.ScalarDataType.builder().setScalarType( schemarise.alfa.runtime.model.ScalarType.${d.scalarType.toString}Type ).build()"
        else typeRestore

        ("schemarise.alfa.runtime.model.ScalarDataType", decl)

      case d: IMapDataType =>

        val min = if (d.min.isDefined) s"setSizeMin(java.util.Optional.of(${d.min.get}))." else ""
        val max = if (d.max.isDefined) s"setSizeMax(java.util.Optional.of(${d.max.get}))." else ""

        val kn = if (d.keyName.isDefined) s"""setKeyName( java.util.Optional.of( "${d.keyName.get}" ) ).""" else ""
        val vn = if (d.valueName.isDefined) s"""setValueName( java.util.Optional.of( "${d.valueName.get}" ) ).""" else ""

        ("schemarise.alfa.runtime.model.MapDataType",
          s"""schemarise.alfa.runtime.model.MapDataType.builder().
             |                    setKeyType( ${buildDataType(d.keyType)} ).$kn
             |                    setValueType( ${buildDataType(d.valueType)} ).$vn
             |                    ${min}${max}build()""".stripMargin)

      case d: IListDataType =>
        val min = if (d.min.isDefined) s"setSizeMin(java.util.Optional.of(${d.min.get}))." else ""
        val max = if (d.max.isDefined) s"setSizeMax(java.util.Optional.of(${d.max.get}))." else ""

        ("schemarise.alfa.runtime.model.ListDataType",
          s"""schemarise.alfa.runtime.model.ListDataType.builder().setComponentType(
             |                    ${buildDataType(d.componentType)} ).${min}${max}build()""".stripMargin)

      case d: ISetDataType =>
        val min = if (d.min.isDefined) s"setSizeMin(java.util.Optional.of(${d.min.get}))." else ""
        val max = if (d.max.isDefined) s"setSizeMax(java.util.Optional.of(${d.max.get}))." else ""

        ("schemarise.alfa.runtime.model.SetDataType",
          s"""schemarise.alfa.runtime.model.SetDataType.builder().setComponentType(
             |                    ${buildDataType(d.componentType)} ).${min}${max}build()""".stripMargin)

      case d: IUdtDataType =>
        val decl = if (d.typeArguments.isDefined)
          typeRestore
        else
          s"""schemarise.alfa.runtime.model.UdtDataType.builder().setFullyQualifiedName( "${d.fullyQualifiedName}" ).setUdtType(schemarise.alfa.runtime.model.UdtMetaType.${d.udt.udtNodeTypeName}Type).build()""".stripMargin

        ("schemarise.alfa.runtime.model.UdtDataType",
          decl)

      case d: IEitherDataType =>
        val lcomp = s"setLeftComponentType( ${buildDataType(d.left)} )"
        val rcomp = s"setRightComponentType( ${buildDataType(d.right)} )"
        val inner = "schemarise.alfa.runtime.model.EitherDataType"
        (inner, s"""$inner.builder().${lcomp}.${rcomp}.build()""".stripMargin)

      case d: IPairDataType =>
        val lcomp = s"setLeftComponentType( ${buildDataType(d.left)} )"
        val rcomp = s"setRightComponentType( ${buildDataType(d.right)} )"
        val inner = "schemarise.alfa.runtime.model.PairDataType"
        (inner, s"""$inner.builder().${lcomp}.${rcomp}.build()""".stripMargin)

      case d: IEnclosingDataType =>
        val comp = s"setComponentType( ${buildDataType(d.componentType)} )"

        val inner = if (d.isOptional)
          "schemarise.alfa.runtime.model.OptionalDataType"

        //                    else if ( d.isKey )
        //                      "schemarise.alfa.runtime.model.KeyDataType"

        else if (d.isTabular)
          "schemarise.alfa.runtime.model.TabularDataType"

        else if (d.isStream)
          "schemarise.alfa.runtime.model.StreamDataType"

        else if (d.isFuture)
          "schemarise.alfa.runtime.model.FutureDataType"

        else if (d.isEncrypt)
          "schemarise.alfa.runtime.model.EncryptedDataType"

        else if (d.isCompress)
          "schemarise.alfa.runtime.model.CompressedDataType"

        else if (d.isTry)
          "schemarise.alfa.runtime.model.TryDataType"

        else
          throw new AlfaRuntimeException("Unsupported in java codegen enclosing type " + d)

        (inner, s"""$inner.builder().${comp}.build()""".stripMargin)

      case d: IEnumDataType =>
        val fqn = d.syntheticEnum.name.fullyQualifiedName
        ("schemarise.alfa.runtime.model.EnumDataType",
          s"""schemarise.alfa.runtime.model.EnumDataType.builder().setSynthFullyQualifiedName("$fqn").addAllFields(
             |            new java.util.ArrayList<String>( ${descClz(d.syntheticEnum)}.INSTANCE.getAllFieldsMeta().keySet()) ).build()""".stripMargin)

      case d: ITupleDataType =>
        val fqn = d.syntheticRecord.name.fullyQualifiedName
        val synthNames = d.syntheticRecord.localFieldNames.filter(nm => nm.startsWith("_")).size > 0

        val ann = docAnnotationToCode(d)

        ("schemarise.alfa.runtime.model.TupleDataType",
          s"""schemarise.alfa.runtime.model.TupleDataType.builder().setSynthFullyQualifiedName("$fqn").
             |            setSyntheticFieldNames(${synthNames.toString.toLowerCase}).
             |            setAnnotations($ann).
             |            putAllFields( ${descClz(d.syntheticRecord)}.INSTANCE.getAllFieldsMeta().
             |            entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey(),
             |            e -> schemarise.alfa.runtime.model.Field.builder().setName(e.getKey()).setDataType(e.getValue().getDataType()).build())) ).build()""".stripMargin)

      case d: IUnionDataType =>
        val fqn = d.syntheticUnion.name.fullyQualifiedName
        val synthNames = d.syntheticUnion.localFieldNames.filter(nm => nm.startsWith("_")).size > 0

        val utype = if (d.isTagged) "Tagged" else "Untagged"

        ("schemarise.alfa.runtime.model.UnionDataType",
          s"""schemarise.alfa.runtime.model.UnionDataType.builder().
             |            setSynthFullyQualifiedName("$fqn").
             |            setUnionType(schemarise.alfa.runtime.model.UnionType.$utype).
             |            // setSyntheticFieldNames(${synthNames.toString.toLowerCase}).
             |            putAllFields( ${descClz(d.syntheticUnion)}.INSTANCE.getAllFieldsMeta().
             |            entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey(),
             |            e ->  schemarise.alfa.runtime.model.Field.builder().setName(e.getKey()).setDataType(e.getValue().getDataType()).build())) ).build()""".stripMargin)

      case d: ITypeParameterDataType =>
        ("schemarise.alfa.runtime.model.TypeParameterDataType",
          s""" schemarise.alfa.runtime.model.TypeParameterDataType.builder().setParamName( "${d.parameterName}" ).build()""")

      case d: IMetaDataType =>
        val m = d.metaType.toString
        val meta = m.substring(1, 2).toUpperCase + m.substring(2)
        ("schemarise.alfa.runtime.model.MetaDataType",
          s""" schemarise.alfa.runtime.model.MetaDataType.builder().setMetaType( schemarise.alfa.runtime.model.MetaType.${meta} ).build()""")

      case _ =>
        throw new AlfaRuntimeException("Unsupported in java codegen " + t + " " + t.getClass.getName);
    }
  }

  private def fieldId(f: IField) =
    s"""FIELD_ID_${validLangIdentifier(f.name.toUpperCase, LangKeywords.javaKeywords)}"""

  private def fieldName(f: IField) =
    s"""FIELD_${validLangIdentifier(f.name.toUpperCase, LangKeywords.javaKeywords)}"""


  def print(udt: IUdtBaseNode): String = {

    val rtUdt = compilerToRt.convert(udt)

    isBootstrapType = BuiltinModelTypes.Includes(udt.name.fullyQualifiedName)

    val accessorFields = udt.allFields

    val noConsumerSupplier = udt.isInstanceOf[IEnum] // || udt.name.typeParameters.size > 0

    val consumers = if (noConsumerSupplier) "" else accessorFields.map(f => consumer(udt, f._2)).mkString("")
    val suppliers = if (noConsumerSupplier) "" else accessorFields.map(f => supplier(udt, f._2)).mkString("")

    val fieldIdConsts = accessorFields.values.zipWithIndex.map(f => {
      s"""        public final static short ${fieldId(f._1)} = ${f._2};
         |        public final static java.lang.String ${fieldName(f._1)} = "${f._1.name}";
         |""".stripMargin
    }).mkString("\n")


    val maskToName = accessorFields.values.zipWithIndex.map(f => {
      s"""
         |                case ${fieldId(f._1)} : return ${fieldName(f._1)};""".stripMargin
    }).mkString("")

    //    val fieldDefns = accessorFields.map(f => {
    //      val sdataType = buildDeclAndDataType(f._2.dataType)
    //      val typeField = s"${localFieldName(f._1)}Type"
    //      s"""
    //         |        private ${sdataType._1} $typeField = ${sdataType._2};
    //         |        """.stripMargin
    //    }).mkString("")

    val fieldTypeMapBuild = accessorFields.map(f => {
      val n = s"${validLangIdentifier(camelCased(f._1), LangKeywords.javaKeywords)}Meta"
      s"""
         |                put(${fieldName(f._2)}, $n );""".stripMargin
    }).mkString("")


    val assertsMapBuild = udt.allAsserts.map(f => {
      val assertModel = compilerToRt.convertAssert(f._2)
      val json = Alfa.jsonCodec().toJsonString(assertModel)

      val jsonSplit = json.split("(?<=\\G.{10000})")

      val multiStr = jsonSplit.map(x => "\"" + jsonStringEncoder.quoteAsString(x).mkString("") + "\"").mkString(", ")
      val typeRestore = "com.schemarise.alfa.runtime.Alfa.jsonCodec().uncheckedFromJson(" + multiStr + ")"

      s"""
         |                put(\"${f._1}\", $typeRestore );""".stripMargin
    }).mkString("")

    val fieldMetas = accessorFields.map(f => {
      val n = localFieldName(f._1)
      val fSupp = s"${n}Supplier"
      val fCons = s"${n}Consumer"
      val fType = s"${n}Type"
      val metaField = s"${validLangIdentifier(camelCased(f._1), LangKeywords.javaKeywords)}Meta"

      val fieldAnnotation = docAnnotationToCode(f._2)
      if (noConsumerSupplier || f._2.dataType.isInstanceOf[TypeParameterDataType])
        s"""        public com.schemarise.alfa.runtime.FieldMeta $metaField = new com.schemarise.alfa.runtime.FieldMeta( java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), MODEL.getAllFields().get(${fieldName(f._2)}) );"""
      else
        s"""        public com.schemarise.alfa.runtime.FieldMeta $metaField = new com.schemarise.alfa.runtime.FieldMeta( java.util.Optional.of( $fSupp ), java.util.Optional.of( $fCons ), java.util.Optional.of( ${fSupp}Inner1 ), java.util.Optional.of( ${fCons}Inner1), MODEL.getAllFields().get(${fieldName(f._2)}) );"""

    }).mkString("\n")

    val fc = new NonConvertibleToBuilderVisitor();
    udt.traverse(fc)
    val convToBuilder = fc.convertableToBuilder.toString

    val udtClz = toJavaVersionedClassName(udt)

    val consumerSuppliers = if (noConsumerSupplier)
      s"""
         |        public java.util.Optional< java.util.function.Function< $udtClz, java.util.function.Supplier > > getFieldSupplier( java.lang.String fieldName ) { return java.util.Optional.empty(); }
         |
         |        public java.util.Optional< java.util.function.BiConsumer<com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.DataSupplier> > getFieldConsumer( java.lang.String fieldName ) { return java.util.Optional.empty(); }
        """.stripMargin
    else
      s"""
         |        public java.util.Optional< java.util.function.BiConsumer< $udtClz, com.schemarise.alfa.runtime.DataConsumer > > getFieldSupplier( java.lang.String fieldName ) {
         |            return _fieldsMeta_.get( fieldName ).getSupplier();
         |        }
         |
         |        public java.util.Optional< java.util.function.BiConsumer<com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.DataSupplier> > getFieldConsumer( java.lang.String fieldName ) {
         |            return _fieldsMeta_.get( fieldName ).getConsumer();
         |        }
        """.stripMargin

    val builderIfc = builderIfcClzOnly(udt)

    val builderMethods = if (noConsumerSupplier)
      s"""
         |        @Override
         |        public boolean hasBuilder() { return false; }
         |
         |        @Override
         |        public boolean convertableToBuilder() { return false; }
         |
         |        @Override
         |        public com.schemarise.alfa.runtime.Builder builder()  { throw new UnsupportedOperationException(); }
         |
         |        @Override
         |        public com.schemarise.alfa.runtime.Builder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) { throw new UnsupportedOperationException(); }
      """.stripMargin
    else
      s"""
         |        @Override
         |        public boolean hasBuilder() { return true; }
         |
         |        @Override
         |        public boolean convertableToBuilder() { return $convToBuilder; }
         |
         |        @Override
         |        public $builderIfc builder()  { return new ${builderImplClz(udt)}(); }
         |
         |        @Override
         |        public $builderIfc builder(com.schemarise.alfa.runtime.IBuilderConfig cc) { return new ${builderImplClz(udt)}(cc); }
      """.stripMargin

    val udtType = udt match {
      case t: IEnum => "enum"
      case t: IEntity => "entity"
      case t: IRecord => "record"
      case t: ITrait => "trait"
      case t: IKey => "key"
      case t: IUnion => if (t.isTagged) "union" else "untaggedUnion"
    }

    //    val keyAccess = if (udt.isInstanceOf[IEntity]) {
    //      val access = if (udt.asInstanceOf[IEntity].key.isDefined) {
    //        val k = udt.asInstanceOf[IEntity].key.get
    //        s"""return java.util.Optional.of( schemarise.alfa.runtime.model.UdtDataType.builder().setFullyQualifiedName("${k.name.fullyQualifiedName}").build() );"""
    //      }
    //      else
    //        "return java.util.Optional.empty();"
    //
    //      s"""
    //         |        public java.util.Optional< schemarise.alfa.runtime.model.UdtDataType > getKey() {
    //         |            $access
    //         |        }
    //                         """.stripMargin
    //    }
    //    else ""


    val hasKeyModel = udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined
    val kModel = if (hasKeyModel) {
      val k = udt.asInstanceOf[IEntity].key.get
      "java.util.Optional.of( " + k.name.fullyQualifiedName + s".${descClzOnly(k)}.INSTANCE )"
    } else if (udt.isInstanceOf[IKey] && udt.asInstanceOf[Key].entityName.isDefined) {
      val ent = udt.asInstanceOf[Key].entityName.get
      "java.util.Optional.of( " + ent + s".${descClzOnly(cua.getUdt(ent).get)}.INSTANCE )"
    }
    else
      "java.util.Optional.empty()"

    val kModelDoc = if (udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined) {
      "Key descriptor for this entity"
    } else if (udt.isInstanceOf[IKey] && udt.asInstanceOf[Key].entityName.isDefined) {
      "Entity descriptor for this key"
    }
    else {
      "Not applicable - this is not an entity or a key directly linked to an entity"
    }

    val depsOpt = rtUdt.getIncludedFrom

    val annotations = if (udt.annotations.size > 0) {
      s"""
         |        public java.util.Optional<java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Expression>> getAnnotations() {
         |        return ${docAnnotationToCode(udt)};
         |        }
         |""".stripMargin
    }
    else ""

    val modifiers = if (rtUdt.getModifiers.contains(ModifierType.Internal)) {
      s"""
         |        public java.util.Set <schemarise.alfa.runtime.model.ModifierType> getModifiers() {
         |            return new java.util.HashSet <schemarise.alfa.runtime.model.ModifierType>() {
         |               {
         |                    add(schemarise.alfa.runtime.model.ModifierType.Internal);
         |               }
         |            };
         |        }
         |""".stripMargin
    } else
      ""

    val immDescendents = if (depsOpt.isPresent) {
      val deps = "new java.util.HashSet( java.util.Arrays.asList(" + depsOpt.get().asScala.map(w => "\"" + w.getFullyQualifiedName + "\"").mkString(", ") + ") )"
      s"""
         |        public java.util.Set< java.lang.String > getImmediateDescendants() {
         |            return $deps;
         |        }
         |""".stripMargin
    }
    else {
      ""
    }

    val absRef = if (ReferencesTraitVisitor.hasTraitRef(udt)) "true" else "false"

    val absTypesInFieldClosures = if (ReferencesTraitVisitor.hasTraitRef(udt)) {
      s"""
         |        public boolean hasAbstractTypeFieldsInClosure() {
         |            return true;
         |        }
         |""".stripMargin
    }
    else
      ""

    val keyModel = if (hasKeyModel) {
      s"""
         |        @Override
         |        /**
         |         * $kModelDoc
         |         */
         |        public java.util.Optional< com.schemarise.alfa.runtime.TypeDescriptor > getEntityKeyModel() {
         |            return $kModel;
         |        }
         |""".stripMargin
    }
    else
      ""

    val cu = udt.asInstanceOf[UdtBaseNode].locateCompUnitParent()
    val modelVersion = if (cu.modelVersion.isDefined) {
      s"""
         |        public java.util.Optional<java.lang.String> getModelId() {
         |            return java.util.Optional.of("${cu.modelVersion.get.text}");
         |        }
         |""".stripMargin
    }
    else
      ""

    val udtName = compilerToRt.getUdtDetails(udt.name.fullyQualifiedName)

    s"""
       |    public static final class ${descClzOnly(udt)} ${typeParams(udt)} extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
       |        public static java.lang.String TYPE_NAME = "${udt.name.fullyQualifiedName}";
       |        public static ${descClz(udt)} INSTANCE  = new ${descClz(udt)}();
       |
       |        private schemarise.alfa.runtime.model.UdtDataType userType = schemarise.alfa.runtime.model.UdtDataType.builder().setUdtType(schemarise.alfa.runtime.model.UdtMetaType.${udtType}Type).setFullyQualifiedName(TYPE_NAME).build();
       |
       |        private ${rtUdt.descriptor().getUdtDataType.getFullyQualifiedName} MODEL = loadModel(this.getClass(), userType);
       |
       |$fieldIdConsts
       |$consumers
       |$suppliers
       |$consumerSuppliers
       |$fieldMetas
       |$builderMethods
       |
       |        public ${descClzOnly(udt)}() {
       |            super.init();
       |        }
       |
       |        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert > _asserts_ = java.util.Collections.unmodifiableMap( new java.util.LinkedHashMap() {
       |            {$assertsMapBuild
       |            }
       |        } );
       |
       |        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<$udtClz> > _fieldsMeta_ = java.util.Collections.unmodifiableMap( new java.util.LinkedHashMap() {
       |            {$fieldTypeMapBuild
       |            }
       |        } );
       |
       |        @Override
       |        public schemarise.alfa.runtime.model.UdtDataType getUdtDataType() {
       |            return userType;
       |        }
       |
       |        @Override
       |        public java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert > getAsserts() {
       |             return _asserts_;
       |        }
       |
       |        @Override
       |        public java.util.Map< java.lang.String, com.schemarise.alfa.runtime.FieldMeta<$udtClz> > getAllFieldsMeta() {
       |            return _fieldsMeta_;
       |        }
       |
       |$keyModel
       |        @Override
       |        public java.lang.String fieldIdName( int id ) {
       |            switch ( id ) {
       |$maskToName
       |                default: throw new com.schemarise.alfa.runtime.AlfaRuntimeException( "Unknown field id " + id );
       |            }
       |        }
       |
       |$absTypesInFieldClosures
       |$immDescendents
       |$annotations
       |$modifiers
       |
       |        public java.lang.String getChecksum() {
       |            /*
       |            ${udt.asInstanceOf[UdtBaseNode].checksumCalcString(true, false)}
       |            ${udt.asInstanceOf[UdtBaseNode].checksumCalcString(true, true)}
       |            */
       |            return "${udt.checksum()}";
       |        }
       |$modelVersion
       |    }""".stripMargin
  }
}
