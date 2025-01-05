package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path
import java.time._
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IEntity, IUdtBaseNode}
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, IUdtDataType, IVectorDataType, Scalars}
import com.schemarise.alfa.compiler.ast.nodes.Expression
import com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

import scala.collection.immutable.ListMap

class ConcreteInnerClassPrinter(logger : ILogger,outputDir : Path, cua: ICompilationUnitArtifact, compilerToRt : CompilerToRuntimeTypes)
  extends PrinterBase( logger, outputDir, cua ) {

  def print(udt : IUdtBaseNode) = {
    val accessorFields = udt.allFields // getNonExtendedFields( udt )

    val k = udtEntityKeyType( udt )

    val concreteConstructor = makeConcreteConstructor(accessorFields, udt, k )

    val immclz = toJavaVersionedClassName(udt)

    val args = if ( udt.name.typeParameters.size > 0 )
      udt.name.typeParameters.map( _._1.name.fullyQualifiedName ).mkString("< ", ", ", " >")
    else ""

    val base = printBaseClass(udt)

    s"""
       |    final class ${concreteClzOnly(udt)}$args extends ${concreteBaseClzOnly(udt)}$args implements $immclz {
       |$concreteConstructor
       |    }
       |
       |$base
       |""".stripMargin
  }

  def applyAsserts(udt: IUdtBaseNode) = {
    if (udt.allSingularAsserts.size > 0) {
      val sa = udt.allSingularAsserts.values.map(a =>
        s"""
           |if ( !__builderConfig.shouldSkipAssert("${a.name}") ) {
           |  __builderConfig.getAssertListener().enterAlfaObjectContext( (com.schemarise.alfa.runtime.AlfaObject) this );
           |  __assert${a.name}( __builderConfig, "${a.name}" );
           |  __builderConfig.getAssertListener().exitAlfaObjectContext( (com.schemarise.alfa.runtime.AlfaObject) this );
           |}
           |""".stripMargin
      ).mkString("")

      sa

//      s"""
//         |${sa}
//         |if ( __builderConfig.getAssertListener().getValidationReport().size() > 0 )
//         |  throw new com.schemarise.alfa.runtime.AlfaAssertException( __builderConfig.getAssertListener().getValidationReport() );
//     """.stripMargin
    }
    else ""
  }

  def validateField(f: IField): String = {
    val n = pascalCase(f.name)
    val fn = localFieldName(n)

    validateDataType( s"""new com.schemarise.alfa.runtime.utils.PathCreator("${f.name}")""", f.dataType, s"get$n()", "            ", 0, ";")
  }


  private def asJava(st: ScalarType, c: Option[_]): String = {
    if (c.isEmpty)
      "null"
    else {
      val n = c.get

      if (n.isInstanceOf[LocalDate]) {
        val a = n.asInstanceOf[LocalDate]
        s"""java.time.LocalDate.of( ${a.getYear}, ${a.getMonthValue}, ${a.getDayOfMonth} )"""
      } else if (n.isInstanceOf[LocalTime]) {
        val a = n.asInstanceOf[LocalTime]
        s"""java.time.LocalTime.of( ${a.getHour},${a.getMinute},${a.getSecond},${a.getNano} )"""
      } else if (n.isInstanceOf[LocalDateTime]) {
        s"""java.time.LocalDateTime.parse( "${n.toString}" )"""
      } else if (n.isInstanceOf[ZonedDateTime]) {
        s"""java.time.ZonedDateTime.parse( "${n.toString}" )"""
      } else if (n.isInstanceOf[ZonedDateTime]) {
        s"""java.time.ZonedDateTime.parse( "${n.toString}" )"""
      } else if (n.isInstanceOf[Duration]) {
        s"""java.time.Duration.parse( "${n.toString}" )"""
      } else if (n.isInstanceOf[NormalizedAstPeriod]) {
        s"""com.schemarise.alfa.runtime.NormalizedPeriod.of( "${n.toString}" )"""
      } else if (st == Scalars.decimal) {
        s"new java.math.BigDecimal(${n.toString})"
      } else if (st == Scalars.long) {
        s"${n.toString}L"
      } else {
        s"${n.toString}"
      }
    }
  }

  private def validateDataType(path: String, dt: IDataType, accessor: String, indent: String, level: Int, terminate: String): String = {

    val whenOpt: Option[String] = dt.whenEncOptional(t => {
      val inner = validateDataType(path, t.componentType, accessor + ".get()", indent, level, terminate)
      if (inner.size > 0)
        s"if ( $accessor.isPresent() ) { " + inner + "}"
      else
        ""
    })

    val whenScalar = dt.whenScalar(t => {
      var dec = if ( t.scale.isDefined && t.precision.isDefined ) {
        s"""${indent}com.schemarise.alfa.runtime.utils.Utils.validateDecimalScaleAndPrecision( __builderConfig.getAssertListener(), () -> $path.scalarElement( $accessor ), $accessor, ${t.precision.get}, ${t.scale.get} )${terminate}"""
      }
      else
        ""

      val range = if (t.min.isDefined || t.max.isDefined) {
        t.scalarType match {
          case Scalars.string =>
            s"""${indent}com.schemarise.alfa.runtime.utils.Utils.validateScalarRange( __builderConfig.getAssertListener(), () -> $path.scalarElement( $accessor ), ${accessor}.length(), ${asJava(t.scalarType, t.min)}, ${asJava(t.scalarType, t.max)} )${terminate}"""
          case _ =>
            s"""${indent}com.schemarise.alfa.runtime.utils.Utils.validateScalarRange( __builderConfig.getAssertListener(), () -> $path.scalarElement( $accessor ), $accessor, ${asJava(t.scalarType, t.min)}, ${asJava(t.scalarType, t.max)} )${terminate}"""
        }
      }
      else {
        val sdt = t.asInstanceOf[ScalarDataType]

        if (sdt.stringPattern.isDefined && sdt.isScalarString)
          s"""${indent}com.schemarise.alfa.runtime.utils.Utils.validateWithPattern( __builderConfig.getAssertListener(), () -> $path.scalarElement($accessor), $accessor, "${sdt.escapedFormat.get}" )${terminate}"""
        else
          ""
      }

      dec + range
    })

    val whenMap: Option[String] = dt.whenMap(t => {
      val validateKeys = validateDataType(path + s""".mapKeyElement( e${level} )""", t.keyType, s"e${level}", "", level + 1, ";")
      val k = if (validateKeys.size > 0)
        s"${indent}${accessor}.keySet().forEach( e${level} -> { $validateKeys } );\n";
      else ""

      val validateVals = validateDataType(path + s""".mapEntryElement( e${level} )""", t.valueType,
        s"e${level}.getValue()", "",
        level + 1, ";")

      val v = if (validateVals.size > 0)
        s"${indent}${accessor}.entrySet().stream().filter( e${level} -> e${level}.getValue() != null).forEach( e${level} -> { $validateVals } );\n";
      else ""

      k + v + validateVectorDataType(path, accessor, t, indent, terminate)
    })

    val whenSet: Option[String] = dt.whenSet(t => {
      val validateKeys = validateDataType(path + s""".setEntryElement( e${level} )""", t.componentType, s"e${level}", "", level + 1, ";")
      val el = if (validateKeys.size > 0)
        s"${indent}${accessor}.forEach( e${level} -> { $validateKeys } );\n";
      else ""

      el + validateVectorDataType(path, accessor, t, indent, terminate)
    })

    val whenSeq: Option[String] = dt.whenList(t => {
      val et = validateDataType(path + s""".listIdxElement( e${level} )""", t.componentType, accessor + s".get(e${level})", "", level + 1, ";")
      val el = if (et.size > 0)
        s"""${indent}java.util.stream.IntStream.range(0, ${accessor}.size()).forEach( e${level} -> { $indent$indent$et } );\n""".stripMargin
      else
        ""

      el + validateVectorDataType(path, accessor, t, indent, terminate)
    })

    val whenTuple : Option[String] = dt.whenTuple(t => {
      Some( accessor + ".validate(__builderConfig);" )
    }, None)

    val whenUdt: Option[String] = dt.whenUDT(t => {
      accessor + ".validate(__builderConfig);"
    })


    val custom = "" +
      (if (whenMap.isDefined) whenMap.get else "") +
      (if (whenOpt.isDefined) whenOpt.get else "") +
      (if (whenSet.isDefined) whenSet.get else "") +
      (if (whenScalar.isDefined) whenScalar.get else "") +
      (if (whenSeq.isDefined) whenSeq.get else "") +
      (if (whenUdt.isDefined) whenUdt.get else "") +
      (if (whenTuple.isDefined) whenTuple.get else "")

    if (custom.length > 0)
      custom
    else
      ""
  }


  private def validateVectorDataType(path: String, accessor: String, t: IVectorDataType, indent: String, terminate: String) = {
    if (t.min.isDefined || t.max.isDefined) {
      val fr = if (t.min.isEmpty) "null" else s"${t.min.get}"
      val to = if (t.max.isEmpty) "null" else s"${t.max.get}"

      s"""${indent}com.schemarise.alfa.runtime.utils.Utils.validateCollectionSize( __builderConfig.getAssertListener(), () -> $path, $accessor, ${fr}, ${to} )${terminate}"""
    } else
      ""
  }

  private def printBaseClass( udt : IUdtBaseNode ) : String = {
    val clz = concreteBaseClzOnly(udt)
    val baseClz = concreteBaseClzOnly(udt)

    val topoSortedFields = udt.topologicallySortedFields // getNonExtendedFields( udt )
    val allFields = udt.allFields

    val accessorMethodImpls = topoSortedFields.map(f => accessorMethodImpl( f._2 ) ).mkString("")

    val expPrinter = new ExpressionPrinter(logger, outputDir, cua, udt, compilerToRt )

    val localFieldDecl = topoSortedFields.map(f => "        public " + typeAndNameAndLiteralDefault( expPrinter, f._2 ) + ";" ).mkString("\n")

    val k = udtEntityKeyType( udt )
    val keyFldDecl = if ( udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined )
      s"        public ${toJavaTypeName(k.get)} _key;"
    else ""

    val keyMethod = if ( udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined )
      s"""
         |        public java.util.Optional< ${toJavaTypeName(k.get)} > get${Expression.DollarKey}() {
         |            return java.util.Optional.ofNullable( _key );
         |        }
                       """.stripMargin
    else
      s"""
        |        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get${Expression.DollarKey}() {
        |            return java.util.Optional.empty();
        |        }
      """.stripMargin


    val hash = makeHashCode( topoSortedFields, udt, k )
    val toStr = makeToString( topoSortedFields, udt, k )
    val baseConstructor = makeBaseConstructor(allFields, baseClz, udt, k )

    val eqls = makeEquals(topoSortedFields, udt, k )

    val args = if ( udt.name.typeParameters.size > 0 )
      udt.name.typeParameters.map( _._1.name.fullyQualifiedName ).mkString("< ", ", ", " >")
    else ""

    val genericGetterCases = topoSortedFields.map(f => {
      val dt = toJavaTypeName( f._2.dataType )
      s"""
         |                case "${f._1}" : return ${localFieldName(f._1)};""".stripMargin
    } ).mkString("")

    val fieldVals = topoSortedFields.values.map(f => {
      validateField(f)
    }).mkString("\n")


    val body =
      s"""|//</editor-fold>
          |
          |//<editor-fold defaultstate="collapsed" desc="Base class">
          |abstract class $baseClz$args {
          |$localFieldDecl
          |$keyFldDecl
          |
          |$baseConstructor
          |$accessorMethodImpls
          |$hash
          |$toStr
          |$eqls
          |$traversal
          |$keyMethod
          |
          |    public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
          |      return ${descClz(udt)}.INSTANCE;
          |    }
          |
          |    public Object get( java.lang.String fieldName) {
          |        switch ( fieldName ) {  $genericGetterCases
          |            default :
          |                throw new com.schemarise.alfa.runtime.AlfaRuntimeException( "Cannot get unknown field " + fieldName );
          |        }
          |    }
          |
          |        public void validate( com.schemarise.alfa.runtime.IBuilderConfig __builderConfig ) {
          |$fieldVals
          |${applyAsserts(udt)}
          |
          |          // TODO
          |          // _key if exists, will be mandatory
          |        }
          |${buildAsserts(udt)}
          |
          |}
      """.stripMargin

    body
  }

  private def traversal =
    s"""
       |        public void traverse( com.schemarise.alfa.runtime.Visitor v ) {
       |        }
     """.stripMargin

  private def accessorMethodImpl( f : IField ) : String = {
    val n = f.name
    val dt = toJavaTypeName( f.dataType )

//    public Immutable with$n( $dt v ) {
//      return null;
//    }

    s"""
       |        public $dt get${ pascalCase(n) }() {
       |          return ${localFieldName(n)};
       |        }
      """.stripMargin
  }

  def makeHashCode(accessorFields: ListMap[String, IField], udt: IUdtBaseNode, k : Option[IUdtDataType] ) = {
    val keyHashCodeArg = if ( k.isDefined ) List( "_key" ) else List.empty
    val hashCodeArgs = ( keyHashCodeArg ++ accessorFields.map( f => localFieldName( f._1 ) ) ).mkString(", ")

    s"""        public int hashCode() {
       |          return java.util.Objects.hash($hashCodeArgs);
       |        }
       """.stripMargin
  }

  def makeFieldEqual(f: IField) : String = {
    val n = localFieldName( f.name )
    if ( ! f.dataType.wasTemplateDerived && hasUnboxedScalar( f.dataType) )
      s"${n} == rhs.${n}"
    else if ( f.dataType.isBinary() )
      s"java.util.Arrays.equals(${n}, rhs.${n})"
    else if ( f.dataType.isStream() )
      s"true /* Stream ${f.name} */"
    else if ( f.dataType.isFuture() )
      s"true /* Future ${f.name} */"
    else
      s"java.util.Objects.equals(${n}, rhs.${n})"
  }

  def makeEquals(accessorFields: ListMap[String, IField], udt: IUdtBaseNode, k : Option[IUdtDataType] ) = {
    val equalsArgs = accessorFields.map( n => makeFieldEqual( n._2 ) )
    val equalsKeyArg = if ( k.isDefined ) List( "java.util.Objects.equals(_key, rhs._key)" ) else List.empty

    val body = ( equalsArgs ++ equalsKeyArg ).mkString(" &&\n              ")
    val body2 = if ( body.size == 0 ) "true" else body // if no fields

    val c = toJavaTypeName(udt)

    s"""        public boolean equals(Object o) {
       |          if (this == o) return true;
       |          if ( !(o instanceof ${concreteClz(udt)} ) ) return false;
       |          ${concreteClz(udt)} rhs = ( ${concreteClz(udt)} ) o;
       |          return $body2;
       |        }
       """.stripMargin
  }

  def makeConcreteConstructor(accessorFields: ListMap[String, IField], udt: IUdtBaseNode, k: Option[IUdtDataType]) = {
    val keyCtorArg = if ( k.isDefined ) List( s"${super.toJavaTypeName(k.get)} _key" ) else List.empty
    val ctorArgs = ( keyCtorArg ++ accessorFields.map( f => typeAndName( f._2 ) ) ).mkString(", ")

    val superKeyCtorArg = if ( k.isDefined ) List( "_key" ) else List.empty
    val superArgs = ( superKeyCtorArg ++ accessorFields.map( f => localFieldName( f._1 ) ) ).mkString(", ")

    val defaultCtor = if ( ctorArgs.trim().size == 0 ) "" else s"private ${concreteClzOnly(udt)}() { super(); }"

    s"""
       |        $defaultCtor
       |        private ${concreteClzOnly(udt)}( $ctorArgs ) {
       |            super( $superArgs );
       |        }
     """.stripMargin
  }

  def buildAsserts(udt: IUdtBaseNode) =
    new ExpressionPrinter(logger, outputDir, cua, udt, compilerToRt).buildAsserts()


  def makeBaseConstructor(accessorFields: ListMap[String, IField], baseClz : String, udt: IUdtBaseNode, k: Option[IUdtDataType]) = {
    val keyCtorAssign = if ( k.isDefined ) s"            this._key = _key;" else ""
    val keyCtorArg = if ( k.isDefined ) List( s"${super.toJavaTypeName(k.get)} _key" ) else List.empty

    val ctorArgs = ( keyCtorArg ++ accessorFields.map( f => typeAndName( f._2 ) ) ).mkString(", ")
    val ctorAssign = accessorFields.map( f => s"            this.${localFieldName(f._1)} = ${localFieldName(f._1)};" ).mkString("\n")

    val defaultCtor = if ( ctorArgs.trim().size == 0 ) "" else s"public $baseClz() {}"

    s"""
       |        $defaultCtor
       |        public $baseClz( $ctorArgs ) {
       |$ctorAssign
       |$keyCtorAssign
       |        }
     """.stripMargin
  }
}
