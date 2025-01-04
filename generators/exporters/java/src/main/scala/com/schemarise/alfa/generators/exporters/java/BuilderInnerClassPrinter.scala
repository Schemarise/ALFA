package com.schemarise.alfa.generators.exporters.java

import java.nio.file.Path
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes.{Entity, Expression}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

class BuilderInnerClassPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes, requiresMutable: Boolean)
  extends PrinterBase(logger, outputDir, cua) {

  private val modelClassPrinter = new TypeDescriptorPrinter(logger, outputDir, cua, compilerToRt)

  def enforceNoFieldSet(udt: IUdtBaseNode, mandatoryFieldCount: Int) = {
    udt.whenUnion(f => {
      s"com.schemarise.alfa.runtime.utils.Utils.enforceNoFieldSet( __missingFields, ${descClz(udt)}.INSTANCE, $mandatoryFieldCount, flag );"
    }).getOrElse("")
  }

  def print(udt: IUdtBaseNode) = {

    val udtFqn = udt.name.fullyQualifiedName

    val accessorFields = udt.allFields
    val expPrinter = new ExpressionPrinter(logger, outputDir, cua, udt, compilerToRt)

    val localFieldDecl = accessorFields.map(f => "        private " + typeAndName(f._2) + ";").mkString("\n")

    val nonExtendedFields = getNonExtendedFields(udt)

    val clz = toJavaVersionedClassName(udt)

    val builderMutableIfcMethods = nonExtendedFields.filter(f => !f._2.hasNonLiteralExpression).
      map(f => {
        val getter =
          if (udt.isUnion || udt.isUntaggedUnion)
            ""
          else
            accessorMethod(clz, f._2)

        setterDecl(f._2, builderIfcClzOnly(udt)) + "\n" + getter

      }).mkString("")

    val entityUdtKey = udtEntityKeyType(udt)
    val builderIfc = builderIfcClzOnly(udt)

    val builderKeySetter = if (entityUdtKey.isDefined)
      s"""
         |        public $builderIfc set${Expression.DollarKey}( ${super.toJavaTypeName(entityUdtKey.get)} k );
       """.stripMargin
    else ""

    val setterImpls = accessorFields.map(f => setterImpl(udt, f._2)).mkString("")

    val genericSetterCases = accessorFields.map(f => {
      val dt = toJavaTypeName(f._2.dataType)
      s"""
         |                case "${f._1}" : set${pascalCase(f._1)}( ( $dt ) val ); break;""".stripMargin
    }).mkString("")

    val typeSetterCases = accessorFields.map(f => {
      val dt = toJavaTypeName(f._2.dataType, true, false)

      if (dt.indexOf("<") == -1)
        s"""
           |                else if ( val instanceof $dt ) set${pascalCase(f._1)}( ( $dt ) val );""".stripMargin
      else
        ""
    }).mkString("")

    val untaggedUnion =
      if (udt.isUntaggedUnion)
        s"""
           |        public void setByType( java.lang.Object val ) {
           |            if ( false ) return;
           |$typeSetterCases
           |            else
           |                throw new com.schemarise.alfa.runtime.AlfaRuntimeException( schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField, "Cannot set value by type " + val.getClass() );
           |        }
      """.stripMargin
      else ""

    val genericGetterCases = accessorFields.map(f => {
      val dt = toJavaTypeName(f._2.dataType)
      s"""
         |                case "${f._1}" : return ${localFieldName(f._1)};""".stripMargin
    }).mkString("")

    val ImmCtorKeyArg = if (entityUdtKey.isDefined) List(" _key") else List.empty

    val ctorArgs = (ImmCtorKeyArg ++ accessorFields.map(f => {
      val lf = localFieldName(f._1)

      if (udt.isTemplated || udt.isTrait) {
        lf
      }
      else {
        val args = s"( builderConfig(), ${descClz(udt)}.INSTANCE.${lf}SupplierInner1, $lf )"
        f._2.dataType match {
          case se: IListDataType => s"com.schemarise.alfa.runtime.utils.VectorCloner.immutableList$args"
          case se: ISetDataType => s"com.schemarise.alfa.runtime.utils.VectorCloner.immutableSet$args"
          case se: IMapDataType => s"com.schemarise.alfa.runtime.utils.VectorCloner.immutableMap$args"
          case _ => lf
        }
      }
    })).mkString(",\n                                  ")

    val localKeyDecl = if (entityUdtKey.isDefined) s"        private ${super.toJavaTypeName(entityUdtKey.get)} _key;" else ""
    val keySetter = if (entityUdtKey.isDefined)
      s"""
         |        public $builderIfc set${Expression.DollarKey}( ${super.toJavaTypeName(entityUdtKey.get)} k ) {
         |            this._key = k;
         |            return this;
         |        }
         |
         |        public ${super.toJavaTypeName(entityUdtKey.get)} assignedKey() {
         |            return this._key;
         |        }
         |
       """.stripMargin
    else ""

    val keyBuilderIfc = if (entityUdtKey.isDefined) s", com.schemarise.alfa.runtime.EntityBuilder<${super.toJavaTypeName(entityUdtKey.get)}> " else ""

    val fieldCount = accessorFields.size


    val keyCheck = if (udt.isEntity && udt.asInstanceOf[Entity].key.isDefined)
      s"""if ( _key == null )
         |  builderConfig().getAssertListener().addFailure(schemarise.alfa.runtime.model.asserts.ValidationAlert.builder().setMessage("Entity key not set").setTypeName(java.util.Optional.of("${udtFqn}")).setViolatedConstraint(java.util.Optional.of(schemarise.alfa.runtime.model.asserts.ConstraintType.MandatoryFieldNotSet)));""".stripMargin
    else ""

    val createArgsSeperator = if (ctorArgs.trim.isEmpty) "" else ", "

    val buildBody = if (udt.isInstanceOf[IUnion]) {
      val cases = accessorFields.map(f => {

        val rawlf = localFieldName(f._1)

        val lf = f._2.dataType match {
          case _: IListDataType => s"java.util.Collections.unmodifiableList( $rawlf )"
          case _: ISetDataType => s"java.util.Collections.unmodifiableSet( $rawlf )"
          case _: IMapDataType => s"java.util.Collections.unmodifiableMap( $rawlf )"
          case _ => rawlf
        }

        s"""
           |        if ( ! __missingFields.get( ${maskField(udt, f._1)} ) )
           |            return new Case${f._1}( __builderConfig, $lf );
         """.stripMargin
      }).mkString("")

      s"""
         |$cases
         |        throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Union case not assigned or more than 1 case assigned");
       """.stripMargin
    } else {
      s"""
         |            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet( builderConfig(), __missingFields, $fieldCount, ${descClz(udt)}.INSTANCE );
         |$keyCheck
         |            ${toJavaVersionedClassName(udt)} obj;
         |
         |            if ( builderConfig().getCustomBuilderFactory().isPresent() )
         |                obj = ( ${toJavaVersionedClassName(udt)} ) builderConfig().getCustomBuilderFactory().get().create( builderConfig(), descriptor() $createArgsSeperator $ctorArgs );
         |            else
         |                obj = new ${concreteClz(udt)}( $ctorArgs );
         |
         |            if ( builderConfig().shouldValidateOnBuild() )
         |                obj.validate( builderConfig() );
         |
         |            return obj;
       """.stripMargin
    }

    val toStr = makeToString(accessorFields, udt, entityUdtKey)

    //    val extParams = if ( udt.extend.isDefined ) typeArgs(udt.`extend`.get ) else ""

    //    val extendsDef = extendsOnly(udt, false )

    val setOptionals = accessorFields.map(f => {
      f._2.dataType match {
        case o: IEnclosingDataType =>
          if (o.isOptional)
            s"set${pascalCase(f._1)}( java.util.Optional.empty() );"
          else
            ""

        case _ => ""
      }
    }).filter(_.length > 0).mkString("\n            ")

    val setScalarLitFields = accessorFields
      .filter(f => f._2.hasLiteralValue)
      .map(f => s"set${pascalCase(f._1)}(${expPrinter.printExpr(f._2.expression.get)});")
      .filter(_.length > 0).mkString("\n            ")

    //           clearMissingFlag( ${maskField(udt,n)} );
    //    val masks = accessorFields.map( f => s"""${descClz(udt)}.FIELD_ID_${f._1.toUpperCase}""" ).mkString(", ")

    val args =
      if (udt.name.typeParameters.size > 0)
        udt.name.typeParameters.map(_._1.name.fullyQualifiedName).mkString("< ", ", ", " >")
      else
        ""

    val parentBuilderIfcs = udt.includes.map(i => qualifiedClassNameWithTypeArgs(i, "." + i.udt.name.name + "Builder"))
    val parentBuilderIfcStrs = if (parentBuilderIfcs.size > 0) parentBuilderIfcs.mkString(", ", ", ", "") else ""

    val tp = typeParams(udt)

    //    val extendz = udt.whenUnion( f => "").getOrElse(s" extends ${concreteClz(udt)} ")
    val extendz = udt.whenUnion(f => "").getOrElse(s" extends ${concreteBaseClzOnly(udt)}$tp ")

    val localVars = udt.whenUnion(f =>
      s"""
         |$localFieldDecl
         |$localKeyDecl
         |
         |        public Object get( java.lang.String fieldName) {
         |            switch ( fieldName ) {  $genericGetterCases
         |                default :
         |                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException( schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField, "Request for unknown field " + fieldName );
         |            }
         |        }
       """.stripMargin).getOrElse("")

    val udtBuilderImpl = builderImplClz(udt)

    val allAsserts = udt.allVectorizedAsserts.keySet.map(a => {
      s"""
         |        if ( ! excludeAsserts.contains("$a") )
         |            ms.addProcessor( "$a", s -> checker._assert$a( __builderConfig, ( java.util.stream.Stream<$udtFqn>) s) );""".stripMargin
    }).mkString("\n")

    val streamingAsserts = if (udt.allVectorizedAsserts.size > 0)
      s"""
         |        public <T extends com.schemarise.alfa.runtime.AlfaObject> void applyStreamingAsserts( java.util.stream.Stream<T> records, java.util.Set<java.lang.String> excludeAsserts ) {
         |            com.schemarise.alfa.runtime.utils.stream.IMultiStream<T, Void> ms = com.schemarise.alfa.runtime.utils.stream.MultistreamFactory.create("${udt.name.name}", records);
         |            $udtBuilderImpl checker = new $udtBuilderImpl(__builderConfig);
         |$allAsserts
         |            ms.executeAll( com.schemarise.alfa.runtime.utils.AlfaUtils.buildAssertAllResult() );
         |        }
       """.stripMargin
    else
      ""

    val newMutableMethod =
      if (requiresMutable) {
        s"""public static $tp Mutable$tp newMutable( ) { return new _MutableImpl(); }"""
      } else {
        ""
      }

    val mutableClass = newMutableClass(udt, args)

    val untaggedIfc = if (udt.isUntaggedUnion) "com.schemarise.alfa.runtime.UntaggedUnionBuilder, " else ""
    s"""
       |
       |    //<editor-fold defaultstate="collapsed" desc="Builder support">
       |    public static $tp $builderIfc$tp builder( ) { return new $udtBuilderImpl(); }
       |
       |    public static $tp $builderIfc$tp builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
       |        if ( bc.getCustomBuilderFactory().isPresent())
       |            return bc.getCustomBuilderFactory().get().builder( bc, ${descClz(udt)}.INSTANCE );
       |        else
       |            return new $udtBuilderImpl(bc);
       |    }
       |
       |$newMutableMethod
       |
       |    default public < B extends com.schemarise.alfa.runtime.Builder > B toBuilder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
       |        return com.schemarise.alfa.runtime.utils.AlfaUtils.toBuilder(bc, this);
       |    }
       |
       |    default public < B extends com.schemarise.alfa.runtime.Builder > B toBuilder() {
       |        return toBuilder(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
       |    }
       |    //</editor-fold>
       |
       |    //<editor-fold defaultstate="collapsed" desc="Builder Interface">
       |    public interface $builderIfc$tp extends ${untaggedIfc} com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject $parentBuilderIfcStrs $keyBuilderIfc {
       |$builderMutableIfcMethods
       |$builderKeySetter
       |        ${classNameWithTypeParams(udt)} build();
       |    }
       |    //</editor-fold>
       |
       |$mutableClass
       |
       |    //<editor-fold defaultstate="collapsed" desc="Builder Impl class">
       |    final class ${builderImplClzOnly(udt)} $args $extendz implements $builderIfc$tp {
       |        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;
       |
       |$localVars
       |
       |        private java.util.BitSet __missingFields;
       |
       |        private ${builderImplClzOnly(udt)}() {
       |            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
       |        }
       |
       |        private ${builderImplClzOnly(udt)}( com.schemarise.alfa.runtime.IBuilderConfig cc ) {
       |            __builderConfig = cc;
       |            __missingFields = new java.util.BitSet( $fieldCount );
       |            __missingFields.set(0, $fieldCount );
       |$setOptionals
       |$setScalarLitFields
       |        }
       |
       |        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
       |            return __builderConfig;
       |        }
       |
       |        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() { return ${descClz(udt)}.INSTANCE; }
       |
       |$setterImpls
       |
       |$untaggedUnion
       |        public void modify( java.lang.String fieldName, java.lang.Object val ) {
       |            switch ( fieldName ) {  $genericSetterCases
       |                default :
       |                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException( schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField, "Attempt to set unknown field " + fieldName );
       |            }
       |        }
       |
       |$keySetter
       |        public ${toJavaVersionedClassName(udt)}${typeParams(udt)} build() {
       |$buildBody
       |        }
       |
       |        private void clearMissingFlag( short flag ) {
       |            ${enforceNoFieldSet(udt, fieldCount)}
       |            __missingFields.clear( flag );
       |        }
       |
       |$toStr
       |
       |$streamingAsserts
       |    }
       |    //</editor-fold>
       |
      """.stripMargin
  }

  private def newMutableClass(udt: IUdtBaseNode, args: String): String = {
    if (!requiresMutable)
      return ""

    val localFields = getLocalFields(udt)

    val parentMutators = includesAndExtends(udt, "", true, ".Mutable")
    val parentMutatorStrs = if (parentMutators.size > 0) parentMutators.mkString(", ", ", ", "") else ""
    val mutableIfcMethods = localFields.filter(f => !f._2.hasNonLiteralExpression).
      map(f => {
        val s = mutableSetterDecl(f._2, "void")

        val fdt = toJavaTypeName(f._2.dataType, false, true)
        val fn = pascalCase(f._2.name)
        s"""
           |$s
           |
           |default ${fdt} get${fn}() {
           |    return _get("$fn", () -> ${defaultForType(f._2.dataType)} );
           |}
           |""".stripMargin
      }).mkString("")

    s"""
       |    //<editor-fold defaultstate="collapsed" desc="Mutable support">
       |    interface Mutable$args extends com.schemarise.alfa.runtime.AlfaMutable $parentMutatorStrs {
       |$mutableIfcMethods
       |    }
       |
       |    static class _MutableImpl implements Mutable {
       |        private java.util.Map< String, Object > _data = new java.util.HashMap< String, Object >();
       |
       |        public java.util.Map< String, Object > _data() {
       |            return _data;
       |        }
       |    }
       |    //</editor-fold>
       |""".stripMargin
  }

  private def defaultForType(tx: IDataType): String = {
    tx match {
      case t: IEnclosingDataType =>
        val d = defaultForType(t.componentType)
        s"java.util.Optional.ofNullable($d)"

      case t: IListDataType =>
        "new java.util.ArrayList()"

      case t: ISetDataType =>
        "new java.util.HashSet()"

      case t: IMapDataType =>
        "new java.util.HashMap()"

      case t: IUdtDataType =>
        toJavaTypeName(t, false, false) + ".newMutable()"

      case _ => "null"
    }
  }

  private def mutableSetterDecl(f: IField, retType: String, term: String = ""): String = {
    val n = pascalCase(f.name)
    val fn = f.name
    val dt = toJavaTypeName(f.dataType, false, true)

    val whenMap: Option[String] = f.dataType.whenMap(t => {
      val kdt = toJavaTypeName(t.keyType, false, true)
      val vdt = toJavaTypeName(t.valueType, false, true)

      s"""        default $retType put$n( $kdt k, $vdt v ) {
         |           _put( "$fn", k, v);
         |        }
         |
         |        default $retType putAll$n( $dt all ) {
         |           _putAll( "$fn", all);
         |        }
         |
         |        """.stripMargin
    })

    val whenSet: Option[String] = f.dataType.whenSet(t => {
      val cdt = toJavaTypeName(t.componentType, false, true)

      s"""        default $retType add$n( $cdt e ) {
         |           _addToSet( "$fn", e);
         |        }
         |        default $retType addAll$n( $dt all ) {
         |           _addAllToSet( "$fn", all);
         |        }
         |        """.stripMargin
    })

    val whenSeq: Option[String] = f.dataType.whenList(t => {
      val cdt = toJavaTypeName(t.componentType, false, true)

      s"""        default $retType add$n( $cdt e ) {
         |           _addToList( "$fn", e);
         |        }
         |
         |        default $retType addAll$n( $dt all ) {
         |           _addAllToList( "$fn", all);
         |        }
         |        """.stripMargin
    })

    val whenCompressed: Option[String] = f.dataType.whenEncCompress(t => {
      val compType = toJavaTypeName(t.componentType, false, true)
      val setter = s"set${pascalCase(n)}"

      s"""        $retType $setter( $dt v );
         |        $retType $setter( $compType v );""".stripMargin
    }
    )

    val whenEncrypted: Option[String] = f.dataType.whenEncEncrypt(t => {
      val compType = toJavaTypeName(t.componentType, false, true)
      val setter = s"set${pascalCase(n)}"

      s"""        $retType $setter( $dt v );
         |        $retType $setter( $compType v );""".stripMargin
    }
    )


    val whenOptional: Option[String] = f.dataType.whenEncOptional(t => {
      val compType = toJavaTypeName(t.componentType, false, true)
      val setter = s"set${pascalCase(n)}"

      s"""        default $retType $setter( $dt v ) {
         |            _set( "$fn", v);
         |        }
         |        default $retType $setter( $compType v ) {
         |            _set( "$fn", java.util.Optional.of( v ) );
         |        }
         |        """.stripMargin
    }
    )

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
      s"""        default $retType set${pascalCase(n)}( $dt v )$term {
         |            _set( "$fn", v );
         |        }
         |""".stripMargin
    }
  }


  private def getConversionProcessor(t: IDataType): String = {
    val jType = toJavaTypeName(t, true, false)

    val proc =
      if (t.isScalar) {
        val tn = jType.split("\\.").last
        s"com.schemarise.alfa.runtime.codec.Converters.${tn}Processor"
      }
      else if (t.isUdt)
        s"new com.schemarise.alfa.runtime.codec.Converters.UdtSupplierConsumer<$jType>()"

      else if (t.isMap())
        "Map"

      else if (t.isSet())
        "Set"

      else if (t.isList()) {
        val compType = t.whenList(_.componentType).get
        val compTypeName = toJavaTypeName(compType, true, false)
        val typeBuilder = modelClassPrinter.buildDeclAndDataType(t)

        val elementProcessor = getConversionProcessor(compType)
        s"new com.schemarise.alfa.runtime.codec.Converters.ListSupplierConsumer< $compTypeName >( ${typeBuilder._2}, $elementProcessor )"
      }

      else
        ""

    proc
  }

  private def setterImpl(udt: IUdtBaseNode, f: IField): String = {
    val n = pascalCase(f.name)
    val fn = localFieldName(n)
    val dt = toJavaTypeName(f.dataType)

    val jdoc = javadoc(f)

    val builderIfc = builderIfcClzOnly(udt)

    val mod = if (f.hasNonLiteralExpression) "private" else "public"

    val whenMap: Option[String] = f.dataType.whenMap(t => {
      val kdt = toJavaTypeName(t.keyType)
      val vdt = toJavaTypeName(t.valueType)

      s"""
         |        private void create$n() { this.$fn = new java.util.LinkedHashMap<>(); }
         |
         |        ${jdoc}$mod $builderIfc put$n( $kdt k, $vdt v ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Key $n", k);
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Value $n", v);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.put( k, v );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc putAll$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.putAll( all );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        private $builderIfc set$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          $fn = all;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         """.stripMargin
    })

    val whenSet: Option[String] = f.dataType.whenSet(t => {
      val cdt = toJavaTypeName(t.componentType)

      s"""
         |        private void create$n() { this.$fn = new java.util.LinkedHashSet<>(); }
         |
         |        ${jdoc}$mod $builderIfc add$n( $cdt e ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", e);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.add( e );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc addAll$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.addAll( all );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        private $builderIfc set$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          this.$fn = all;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |         """.stripMargin
    })

    val whenSeq: Option[String] = f.dataType.whenList(t => {
      val cdt = toJavaTypeName(t.componentType)

      s"""
         |        private void create$n() { this.$fn = new java.util.ArrayList<>(); }
         |
         |        ${jdoc}$mod $builderIfc add$n( $cdt e ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", e);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.add( e );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc set$n( int index, $cdt e ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", e);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.set( index, e );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc addAll$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          if ( this.$fn == null ) create$n();
         |          this.$fn.addAll( all );
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        private $builderIfc set$n( $dt all ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", all);
         |          this.$fn = all;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |         """.stripMargin
    })

    val whenCompressed: Option[String] = f.dataType.whenEncCompress(t => {
      val compType = toJavaTypeName(t.componentType)
      val setter = s"set${pascalCase(n)}"

      val handler = s"return $setter(com.schemarise.alfa.runtime.utils.Utils.defaultCompressedFromValue( ${getConversionProcessor(t.componentType)}, __builderConfig, v ) );"

      s"""
         |        ${jdoc}$mod $builderIfc $setter( $dt v ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", v);
         |          this.$fn = v;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc $setter( $compType v ) {
         |          $handler
         |        }
         |
          """.stripMargin
    }
    )

    val whenEncrypted: Option[String] = f.dataType.whenEncEncrypt(t => {
      val compType = toJavaTypeName(t.componentType)
      val setter = s"set${pascalCase(n)}"

      val handler = s"return $setter( com.schemarise.alfa.runtime.utils.Utils.defaultEncryptedFromValue( ${getConversionProcessor(t.componentType)}, __builderConfig, v ) );"

      s"""
         |        ${jdoc}$mod $builderIfc $setter( $dt v ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", v);
         |          this.$fn = v;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc $setter( $compType v ) {
         |          $handler
         |        }
         |
          """.stripMargin
    }
    )


    val whenOptional: Option[String] = f.dataType.whenEncOptional(t => {
      val compType = toJavaTypeName(t.componentType)
      val setter = s"set${pascalCase(n)}"

      s"""
         |        ${jdoc}$mod $builderIfc $setter( $dt v ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", v);
         |          this.$fn = v;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
         |        ${jdoc}$mod $builderIfc $setter( $compType v ) {
         |          this.$fn = java.util.Optional.ofNullable(v);
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
         |
          """.stripMargin
    }
    )
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
      s"""
         |        ${jdoc}$mod $builderIfc set${pascalCase(n)}( $dt v ) {
         |          com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("$n", v);
         |          this.$fn = v;
         |          clearMissingFlag( ${maskField(udt, n)} );
         |          return this;
         |        }
      """.stripMargin
    }
  }

}
