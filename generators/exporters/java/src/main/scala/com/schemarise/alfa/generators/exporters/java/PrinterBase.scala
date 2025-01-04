package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path

import com.google.googlejavaformat.java.{Formatter, JavaFormatterOptions}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.{IUdtDataType, _}
import com.schemarise.alfa.compiler.ast.nodes.Entity
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, ScalarDataType}
import com.schemarise.alfa.compiler.utils.{ILogger, LangKeywords}
import com.schemarise.alfa.generators.common.TextWriter

import scala.collection.immutable.ListMap

abstract class PrinterBase(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact) extends TextWriter(logger) {

  private val totalUdts = cua.getUdtVersionNames().size

  def outputDirectory: Path = outputDir

  def versionedPkg(udt: IUdtBaseNode) = udt.name.fullyQualifiedName + toJavaTypeNameVersion(udt)

  //  def concreteBaseClz(udt: IUdtBaseNode) = udt.name.namespace.name + "." + concreteBaseClzOnly(udt)

  def concreteClz(udt: IUdtBaseNode) = versionedPkg(udt) + "." + concreteClzOnly(udt)

  def builderImplClz(udt: IUdtBaseNode) = versionedPkg(udt) + "." + builderImplClzOnly(udt)

  def descClz(udt: IUdtBaseNode) = versionedPkg(udt) + "." + descClzOnly(udt)

  def concreteBaseClzOnly(udt: IUdtBaseNode) = "_" + udt.name.name + "__Base__"

  def concreteClzOnly(udt: IUdtBaseNode) = "_" + toJavaVersionedClassName(udt) + "Concrete"

  def builderImplClzOnly(udt: IUdtBaseNode) = "_" + toJavaVersionedClassName(udt) + "BuilderImpl"

  def builderIfcClzOnly(udt: IUdtBaseNode) = toJavaVersionedClassName(udt) + "Builder"

  def descClzOnly(udt: IUdtBaseNode) = toJavaVersionedClassName(udt) + "Descriptor"

  val formatter = new Formatter(JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build())


  def methodPrinter(methods: Set[IMethodDeclaration], expPrinter: ExpressionPrinter): String = {
    ""
  }

  override def formatSource(p: Path, source: String): String = {
    try {
      if (totalUdts > 100)
        return source;

      val formattedSource = formatter.formatSource(source)
      return formattedSource;
    } catch {
      case e: Exception => {
        logger.warn("Failed formatting Java file " + p + ". " + e.getMessage)
        return source
      }
    }
  }

  def bindClassAnnotation(udt: IUdtBaseNode) = {
    ""
  }

  def javaMethodSig(m: IMethodSignature): String = {
    val formals = m.formals.map(m => s"${javadoc(m._2)}${toJavaTypeName(m._2.dataType)} ${localFieldName(m._1)}")

    s"""    ${javadoc(m)}${toJavaTypeName(m.returnType)} ${m.fullyQualifiedName}( ${formals.mkString(", ")} )
       |""".stripMargin
  }

  def javadoc(f: IDocAndAnnotated): String = {
    val docs = f.docs.mkString("")
    if (docs.trim.length > 0)
      s"""/** $docs */"""
    else
      ""
  }

  def accessorMethod(clz: String, f: IField): String = {
    val n = f.name
    val dt = toJavaTypeName(f.dataType)

    s"""
       |    ${javadoc(f)}$dt get${pascalCase(n)}();
      """.stripMargin
  }

  def maskField(u: IUdtBaseNode, n: String) =
    s"${descClz(u)}.FIELD_ID_" + validLangIdentifier(n.toUpperCase(), LangKeywords.javaKeywords)

  def getNonExtendedFields(udt: IUdtBaseNode) = {
    val localFields = udt.localFieldNames.toSet ++ udt.includesClosureFieldNames.toSet
    udt.allFields.filter(f => localFields.contains(f._1))
  }

  def getLocalFields(udt: IUdtBaseNode) = {
    val localFields = udt.localFieldNames.toSet
    val locals = udt.allFields.filter(f => localFields.contains(f._1))
    locals
  }

  def udtEntityKeyType(udt: IUdtBaseNode) = {
    if (udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined)
      udt.asInstanceOf[IEntity].keyType
    else
      None
  }

  def hasUnboxedScalar(t: IDataType): Boolean = {
    if (t.isInstanceOf[IScalarDataType]) {
      t.whenScalar(_.scalarType match {
        case Scalars.boolean => true
        case Scalars.`int` => true
        case Scalars.long => true
        case Scalars.short => true
        case Scalars.double => true
        //        case Scalars.float => true
        //        case Scalars.byte => true
        //        case Scalars.char => true
        case _ => false
      }
      ).get
    }
    else
      false
  }

  //  private def udtEntityKeyFields(udt : IUdtBaseNode) = {
  //    if ( udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined )
  //      udt.asInstanceOf[IEntity].key.get.allFields
  //    else
  //      Map.empty
  //  }

  def typeAndNameAndLiteralDefault(expPr: ExpressionPrinter, f: IField): String = {
    val tn = typeAndName(f)

    val exp =
      //      if (f.expression.isDefined && f.expression.get.isLiteralValue)
      //        s" = ${expPr.printExpr(f.expression.get)}"
      //        else
      ""

    s"$tn$exp"
  }

  def typeAndName(f: IField): String = {
    val n = localFieldName(f.name)
    val dt = toJavaTypeName(f.dataType)

    s"$dt $n"
  }

  def makeToString(accessorFields: ListMap[String, IField], udt: IUdtBaseNode, k: Option[IUdtDataType]) = {
    val fieldNames = accessorFields.map(n => s""""${n._1}"""")
    val objs = accessorFields.map(n => s"${localFieldName(n._1)}")

    if (udt.isEntityWithKey) {
      val keyaccessors = udt.asInstanceOf[Entity].key.get.allAccessibleFields()
      val kfieldNames = keyaccessors.map(n => s""""${n._1}"""").map(e => e)
      val kobjs = keyaccessors.map(e => "_key.get" + pascalCase(e._1) + "()")

      s"""        public java.lang.String toString() {
         |          return
         |             com.schemarise.alfa.runtime.utils.Utils.udtToString(
         |             "${udt.name.fullyQualifiedName}",
         |             new java.lang.String[] { ${(kfieldNames ++ fieldNames).mkString(",")} },
         |             new java.lang.Object[] { ${(kobjs ++ objs).mkString(",")} } );
         |        }
      """.stripMargin
    }
    else {
      s"""        public java.lang.String toString() {
         |          return
         |             com.schemarise.alfa.runtime.utils.Utils.udtToString(
         |             "${udt.name.fullyQualifiedName}",
         |             new java.lang.String[] { ${fieldNames.mkString(",")} },
         |             new java.lang.Object[] { ${objs.mkString(",")} } );
         |        }
      """.stripMargin
    }
  }


  def toJavaTypeNameVersion(u: IUdtDataType): String = {
    if (u.version.isDefined)
      s"_V${u.version.get}"
    else
      ""
  }

  def toJavaTypeNameVersion(u: IUdtBaseNode): String = {
    if (u.name.version.isDefined)
      s"_V${u.name.version.get}"
    else
      ""
  }


  def toJavaTypeName(u: IUdtBaseNode): String = {
    u.name.fullyQualifiedName + toJavaTypeNameVersion(u)
  }

  def toJavaTypeName(_t: IDataType): String = {
    toJavaTypeName(_t, false, false)
  }

  def javaDataSupplierMethod(_t: IDataType): String = {
    _t match {
      case t: IEnclosingDataType =>
        if (t.isTry) "tryValue"
        else if (t.isCompress) "compressedValue"
        else if (t.isEncrypt) "encryptedValue"
        else if (t.isFuture) "futureValue"
        //        else if ( t.isKey ) "keyValue"
        else if (t.isOptional) "optionalValue"
        else if (t.isStream) "streamValue"
        else if (t.isEither) "eitherValue"
        else if (t.isPair) "pairValue"
        else if (t.isTabular) "tableValue"
        else
          throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Unhandled enclosed type " + t.encType)

      case t: IEnumDataType => "enumValue"

      case t: IMapDataType => "mapValue"

      case t: IMetaDataType => "metaValue"

      case t: IScalarDataType =>
        t.scalarType match {
          case Scalars.binary => "binaryValue"
          case Scalars.boolean => "booleanValue"
          //          case Scalars.byte => "byteValue"
          //          case Scalars.char => "charValue"
          case Scalars.date => "dateValue"
          case Scalars.datetime => "datetimeValue"
          case Scalars.datetimetz => "datetimetzValue"
          case Scalars.decimal => "decimalValue"
          case Scalars.double => "doubleValue"
          case Scalars.duration => "durationValue"
          case Scalars.period => "periodValue"
          //          case Scalars.float => "floatValue"
          case Scalars.`int` => "intValue"
          case Scalars.long => "longValue"
          //          case Scalars.pattern => "patternValue"
          case Scalars.short => "shortValue"
          case Scalars.string => "stringValue"
          case Scalars.time => "timeValue"
          //          case Scalars.uri => "uriValue"
          case Scalars.uuid => "uuidValue"
          case Scalars.void => "voidValue"
        }

      case t: IListDataType => "listValue"

      case t: ISetDataType => "setValue"

      case t: ITabularDataType => "tableValue"

      case t: ITupleDataType => "tupleValue"

      case t: IUdtDataType => "objectValue"

      case t: IUnionDataType => "unionValue"

      case t: ITypeParameterDataType => "paramTypeValue"
    }
  }

  def toJavaTypeName(_t: IDataType, box: Boolean, mutableObjs: Boolean): String = {

    _t.unwrapTypedef match {
      case t: IEnclosingDataType =>
        if (t.isTry) "schemarise.alfa.runtime.model.Try< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        else if (t.isCompress) "com.schemarise.alfa.runtime.Compressed< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        else if (t.isEncrypt) "com.schemarise.alfa.runtime.Encrypted< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        else if (t.isFuture) "java.util.concurrent.Future< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        //          else if ( t.isKey ) {
        //            val ent = t.componentType.asInstanceOf[UdtDataType].resolvedType.get.asInstanceOf[IEntity]
        //            qualifiedClassNameWithTypeArgs(ent.keyType.get)
        //          }
        else if (t.isOptional) "java.util.Optional< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        else if (t.isStream)
          "java.util.List< " + toJavaTypeName(t.componentType, true, mutableObjs) + " >"
        //          else if ( t.isTabular ) "com.schemarise.alfa.runtime.Tabular< " + toJavaTypeName( t.componentType ) + " >"
        else if (t.isTabular) "com.schemarise.alfa.runtime.ITable" // + toJavaTypeName( t.componentType ) + " >"
        else if (t.isEither) {
          val e = t.asInstanceOf[IEitherDataType]
          "schemarise.alfa.runtime.model.Either< " + toJavaTypeName(e.left, true, mutableObjs) + "," + toJavaTypeName(e.right, true, mutableObjs) + " >"
        }
        else if (t.isPair) {
          val e = t.asInstanceOf[IPairDataType]
          "schemarise.alfa.runtime.model.Pair< " + toJavaTypeName(e.left, true, mutableObjs) + "," + toJavaTypeName(e.right, true, mutableObjs) + " >"
        }
        else
          throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Unhandled enclosed type " + t.encType)

      case t: IEnumDataType =>
        t.syntheticEnum().name.fullyQualifiedName

      case t: IMapDataType =>
        s"java.util.Map< ${toJavaTypeName(t.keyType, true, mutableObjs)}, ${toJavaTypeName(t.valueType, true, mutableObjs)} >"

      case t: IScalarDataType =>
        val _box = box || t.wasTemplateDerived
        t.scalarType match {
          case Scalars.binary => "byte[]"
          case Scalars.boolean => if (_box) "java.lang.Boolean" else "boolean"
          //          case Scalars.byte => if (_box) "java.lang.Byte" else "byte"
          //          case Scalars.char => if (_box) "java.lang.Character" else "char"
          case Scalars.date => "java.time.LocalDate"
          case Scalars.datetime => "java.time.LocalDateTime"
          case Scalars.datetimetz => "java.time.ZonedDateTime"
          case Scalars.decimal => "java.math.BigDecimal"
          case Scalars.double => if (_box) "java.lang.Double" else "double"
          case Scalars.duration => "java.time.Duration"
          case Scalars.period => "com.schemarise.alfa.runtime.NormalizedPeriod"
          //          case Scalars.float => if (_box) "java.lang.Float" else "float"
          case Scalars.`int` => if (_box) "java.lang.Integer" else "int"
          case Scalars.long => if (_box) "java.lang.Long" else "long"
          //          case Scalars.pattern => "java.lang.String"
          case Scalars.short => if (_box) "java.lang.Short" else "short"
          case Scalars.string => "java.lang.String"
          case Scalars.time => "java.time.LocalTime"
          //          case Scalars.uri => "java.net.URI"
          case Scalars.uuid => "java.util.UUID"
          case Scalars.void =>
            val parentUdt = _t.asInstanceOf[DataType].locateUdtParent()
            if (parentUdt.isUnion)
              "com.schemarise.alfa.runtime.UnionUntypedCase"
            else if (_box)
              "Void"
            else
              "void"
        }

      case t: IListDataType =>
        s"java.util.List< ${toJavaTypeName(t.componentType, true, mutableObjs)} >"

      case t: ISetDataType =>
        s"java.util.Set< ${toJavaTypeName(t.componentType, true, mutableObjs)} >"

      case t: ITabularDataType =>
        s"com.schemarise.alfa.runtime.ITable" // < ${t.table.get.udt.name.fullyQualifiedName} >"

      case t: ITupleDataType =>
        t.syntheticRecord.name.fullyQualifiedName

      case t: IUdtDataType =>
        val tmpl =
          if (t.typeArguments.isDefined) {
            t.typeArguments.get.map(e => toJavaTypeName(e, true, mutableObjs)).mkString("< ", ", ", " >")
          }
          else ""

        val m = if (mutableObjs) ".Mutable" else ""
        toFQJavaVersionedClassName(t) + m + tmpl

      case t: IUnionDataType =>
        t.syntheticUnion.name.fullyQualifiedName

      case t: ITypeParameterDataType =>
        t.parameterName

      case t: ILambdaDataType =>
        val bi = if (t.argTypes.size == 2) "Bi" else ""

        val rt = if (t.resultType == ScalarDataType.voidType) "java.lang.Object" else toJavaTypeName(t.resultType, true, mutableObjs)

        val d = t.argTypes.map(s => toJavaTypeName(s, true, mutableObjs)).mkString(",")

        if (t.argTypes.size == 0) {
          bi + s"java.util.function.Supplier<${rt}>"
        }
        else if (t.resultType.isScalarBoolean) {
          bi + "java.util.function.Predicate"
        }
        else {
          bi + s"java.util.function.Function<$d,$rt>"
        }

      case t: IMetaDataType =>
        t.metaType match {
          case MetaType.Entity => "com.schemarise.alfa.runtime.Entity"
          case MetaType.Trait => "com.schemarise.alfa.runtime.Trait"
          case MetaType.Enum => "com.schemarise.alfa.runtime.Enum"
          case MetaType.Key => "com.schemarise.alfa.runtime.Key"
          case MetaType.Record => "com.schemarise.alfa.runtime.Record"
          case MetaType.Udt => "com.schemarise.alfa.runtime.AlfaObject"
          case MetaType.Union => "com.schemarise.alfa.runtime.Union"
          case MetaType.Service => "com.schemarise.alfa.runtime.Service"
          case MetaType.Annotation => "com.schemarise.alfa.runtime.Annotation"
          case MetaType.EntityName => "java.lang.String"
          case MetaType.TraitName => "java.lang.String"
          case MetaType.EnumName => "java.lang.String"
          case MetaType.KeyName => "java.lang.String"
          case MetaType.RecordName => "java.lang.String"
          case MetaType.UdtName => "java.lang.String"
          case MetaType.UnionName => "java.lang.String"
          case MetaType.ServiceName => "java.lang.String"
        }

      case t: IAnyDataType =>
        "java.lang.Object"

      case _ =>
        throw new RuntimeException("Java Exporter Unhandled type " + _t.getClass + "  " + _t)
    }
  }

  def extendsOnly(udt: IUdtBaseNode, withExtendsTypeArgs: Boolean = true, nestedClassSuffix: String = "") = {
    if (udt.extendsDef.isDefined) {
      if (withExtendsTypeArgs)
        Some(qualifiedClassNameWithTypeArgs(udt.extendsDef.get, nestedClassSuffix))
      else
        Some(udt.extendsDef.get.fullyQualifiedName + nestedClassSuffix)
    }
    else
      None
  }

  def includesAndExtends(udt: IUdtBaseNode, mandatoryInclude: String, withExtendsTypeArgs: Boolean = true, nestedClassSuffix: String = ""): List[String] = {
    val mandatoryIncs = if (mandatoryInclude.size > 0)
      List(mandatoryInclude)
    else
      List.empty

    val incs = mandatoryIncs ++ udt.includes.map(i => qualifiedClassNameWithTypeArgs(i, nestedClassSuffix))

    val ext = extendsOnly(udt, withExtendsTypeArgs, nestedClassSuffix)

    if (ext.isDefined)
      List(ext.get) ++ incs
    else
      incs
  }

  def toJavaFileName(n: String): String = {
    val f = n.replace('.', '/') + ".java"
    f
  }

  def toJavaVersionedClassName(u: IUdtBaseNode): String = {
    val vn = u.name
    val n = vn.fullyQualifiedName
    n.substring(n.lastIndexOf('.') + 1) + toJavaTypeNameVersion(u)
  }

  def toJavaVersionedClassName(u: IUdtDataType): String = {
    val n = u.fullyQualifiedName
    n.substring(n.lastIndexOf('.') + 1) + toJavaTypeNameVersion(u.udt)
  }


  def toFQJavaVersionedClassName(u: IUdtDataType): String = {

    val fqn = u.fullyQualifiedName
    if (fqn.indexOf(".") == -1)
      return toJavaVersionedClassName(u)

    val ns = fqn.substring(0, fqn.lastIndexOf('.'))

    val cn = ns + "." + toJavaVersionedClassName(u)
    cn
  }

  def qualifiedClassNameWithTypeArgs(u: IUdtDataType, nestedClassSuffix: String = ""): String = {
    val args = if (u.typeArguments.isDefined)
      u.typeArguments.get.map(ta => {

        toJavaTypeName(ta, true, false)
      }).mkString("< ", ", ", " >")
    else
      ""

    qualifiedClassNameOnly(u) + nestedClassSuffix + args
  }

  def qualifiedClassNameOnly(u: IUdtDataType): String = {
    toJavaPackageName(u) + "." + toJavaVersionedClassName(u)
  }

  def classNameWithTypeParams(u: IUdtBaseNode): String = {
    val args = typeParams(u)
    toJavaVersionedClassName(u) + args
  }

  def typeParams(u: IUdtBaseNode): String = {
    if (u.name.typeParameters.size > 0)
      u.name.typeParameters.map(_._1.name.fullyQualifiedName).mkString("< ", ", ", " >")
    else ""
  }

  def typeArgs(u: IUdtDataType): String = {
    if (u.typeArguments.isDefined)
      u.typeArguments.get.map(e => toJavaTypeName(e, true, false)).mkString("< ", ", ", " >")
    else ""
  }

  def toJavaPackageName(udt: IUdtBaseNode): String = {
    toJavaPackageName(udt.asDataType)
  }

  def toJavaPackageName(udt: IUdtDataType): String = {
    val n = udt.fullyQualifiedName

    if (n.indexOf('.') == -1)
      return "default_namespace"

    n.substring(0, n.lastIndexOf('.'))
  }

  def localFieldName(s: String): String =
    validLangIdentifier("_" + s.head.toString.toLowerCase + s.tail, LangKeywords.javaKeywords)

  def camelCased(s: String): String =
    s.head.toString.toLowerCase + s.tail

  def localFieldName(f: IField): String = {
    val n = pascalCase(f.name)
    val fn = localFieldName(n)
    fn
  }

  def setterDecl(f: IField, retType: String, term: String = ";"): String = {
    val n = pascalCase(f.name)
    val fn = localFieldName(f)
    val dt = toJavaTypeName(f.dataType)

    val whenMap: Option[String] = f.dataType.whenMap(t => {
      val kdt = toJavaTypeName(t.keyType)
      val vdt = toJavaTypeName(t.valueType)

      s"""        $retType put$n( $kdt k, $vdt v );
         |        $retType putAll$n( $dt all );
         |        """.stripMargin
    })

    val whenSet: Option[String] = f.dataType.whenSet(t => {
      val cdt = toJavaTypeName(t.componentType)

      s"""        $retType add$n( $cdt e );
         |        $retType addAll$n( $dt all );
         |        """.stripMargin
    })

    val whenSeq: Option[String] = f.dataType.whenList(t => {
      val cdt = toJavaTypeName(t.componentType)

      s"""        $retType add$n( $cdt e );
         |        $retType addAll$n( $dt all );
         |        """.stripMargin
    })

    val whenCompressed: Option[String] = f.dataType.whenEncCompress(t => {
      val compType = toJavaTypeName(t.componentType)
      val setter = s"set${pascalCase(n)}"

      s"""        $retType $setter( $dt v );
         |        $retType $setter( $compType v );""".stripMargin
    }
    )

    val whenEncrypted: Option[String] = f.dataType.whenEncEncrypt(t => {
      val compType = toJavaTypeName(t.componentType)
      val setter = s"set${pascalCase(n)}"

      s"""        $retType $setter( $dt v );
         |        $retType $setter( $compType v );""".stripMargin
    }
    )


    val whenOptional: Option[String] = f.dataType.whenEncOptional(t => {
      val setter = s"set${pascalCase(n)}"
      val compType = toJavaTypeName(t.componentType)

      val additional = s"$retType $setter( $compType v );"

      s"""        $retType $setter( $dt v );
         |$additional""".stripMargin
    })

    val custom = "" +
      (if (whenMap.isDefined) whenMap.get else "") +
      (if (whenSet.isDefined) whenSet.get else "") +
      (if (whenSeq.isDefined) whenSeq.get else "") +
      (if (whenCompressed.isDefined) whenCompressed.get else "") +
      (if (whenOptional.isDefined) whenOptional.get else "") +
      (if (whenEncrypted.isDefined) whenEncrypted.get else "")


    if (custom.length > 0)
      custom
    else {
      s"""        $retType set${pascalCase(n)}( $dt v )$term
         |""".stripMargin
    }
  }
}
