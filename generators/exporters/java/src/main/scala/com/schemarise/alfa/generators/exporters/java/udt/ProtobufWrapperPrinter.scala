package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.types.{IDataType, IUdtDataType, Scalars}
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IField}
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.ast.nodes.{EnumDecl, UdtBaseNode}
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import com.schemarise.alfa.generators.exporters.java.PrinterBase

class ProtobufWrapperPrinter(logger: ILogger, outputDir: Path,
                             cua: ICompilationUnitArtifact, c2r: CompilerToRuntimeTypes) extends PrinterBase(logger, outputDir, cua) {
  def enumPrint(udt: UdtBaseNode): Unit = {
    val clz = toJavaVersionedClassName(udt)
    val builder = clz + "Builder"
    val pkg = toJavaPackageName(udt)
    val fqAlfaClz = s"$pkg.$clz"
    val fqAlfaBuilder = s"$fqAlfaClz.${clz}Builder"

    val fqPbOuterClass = s"$pkg.${clz}OuterClass.${clz}"
    val fqPbOuterClassBuilder = s"$pkg.${clz}OuterClass.${clz}.Builder"

    val pbWrapClz = clz + "AlfaProtoBridge"

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + pbWrapClz)

    enterFile(f)


    val enumConsts =
      udt.allFields.values.map(e => {
        s"""    public static $fqPbOuterClass ${e.name} = $fqPbOuterClass.newBuilder().setValue($fqPbOuterClass.Type.valueOf("${e.name}")).build();"""
      }).mkString("\n")

    writeln(
      s"""package $pkg;
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |public final class $pbWrapClz {
         |$enumConsts

         |    public static $fqPbOuterClass alfaToPb($fqAlfaClz in) {
         |        switch ( in ) {
         |${
        udt.allFields.values.map(e => {
          s"""        case ${e.name}: return ${e.name};  """
        }).mkString("\n")
      }
         |        }
         |        throw new RuntimeException("Unhandle constant " + in);
         |    }
         |
         |    public static $fqAlfaClz pbToAlfa($fqPbOuterClass in) {
         |        switch ( in.getValue() ) {
         |${
        udt.allFields.values.map(e => {
          s"""        case ${e.name} : return $fqAlfaClz.${e.name};  """
        }).mkString("\n")
      }
         |        }
         |        throw new RuntimeException("Unhandle constant " + in);
         |    }
         |}
      """.stripMargin)

    exitFile()
  }

  def udtPrint(udt: UdtBaseNode): Unit = {
    val clz = toJavaVersionedClassName(udt)
    val builder = clz + "Builder"
    val pkg = toJavaPackageName(udt)
    val fqAlfaClz = s"$pkg.$clz"
    val fqAlfaBuilder = s"$fqAlfaClz.${clz}Builder"

    val fqPbOuterClass = s"$pkg.${clz}OuterClass.${clz}"
    val fqPbOuterClassBuilder = s"$pkg.${clz}OuterClass.${clz}.Builder"

    val pbWrapClz = clz + "AlfaProtoBridge"

    val getters = udt.allFields.values.map(f => {
      pbAccessorMethod("pbuffer", f)
    }).mkString("\n")
    val builderSetters = udt.allFields.values.map(f => {
      pbSetterMethod("pbuilder", f, fqAlfaBuilder, udt)
    }).mkString("\n")
    val builderGetters = udt.allFields.values.map(f => {
      pbAccessorMethod("pbuilder", f)
    }).mkString("\n")

    val f = toJavaFileName(toJavaPackageName(udt) + "/" + pbWrapClz)

    val setBits = udt.allFields.values.map(f => {
      if (f.dataType.isMap()) {
        s"""
           |        if ( pbuilder.get${pascalCase(f.name)}Map() != null ) clearMissingFlag(${maskField(udt, f.name)});
         """.stripMargin
      }
      else if (f.dataType.isVector()) {
        s"""
           |        if ( pbuilder.get${pascalCase(f.name)}List() != null ) clearMissingFlag(${maskField(udt, f.name)});
         """.stripMargin
      }
      else {
        s"""
           |        if ( pbuilder.has${pascalCase(f.name)}() ) clearMissingFlag(${maskField(udt, f.name)});
         """.stripMargin
      }
    }).mkString("\n")

    enterFile(f)

    writeln(
      s"""package $pkg;
         |
         |@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
         |public final class $pbWrapClz {
         |
         |    public final static class $clz implements $fqAlfaClz {
         |        private final $fqPbOuterClass pbuffer;
         |
         |        public $clz($fqPbOuterClass o) {
         |            pbuffer = o;
         |        }
         |
         |        public $fqPbOuterClass buffer() {
         |            return pbuffer;
         |        }
         |
         |$getters
         |
         |        public <B extends com.schemarise.alfa.runtime.Builder> B tobuilder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
         |            return toBuilder();
         |        }
         |
         |        public static $fqAlfaBuilder builder() {
         |            return new $pbWrapClz.$builder( $fqPbOuterClass.newBuilder() );
         |        }
         |
         |        @Override
         |        public <B extends com.schemarise.alfa.runtime.Builder> B toBuilder() {
         |            return (B) new $pbWrapClz.$builder();
         |        }
         |
         |        @Override
         |        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
         |            return $fqAlfaClz.${clz}Descriptor.INSTANCE;
         |        }
         |
         |        @Override
         |        public Object get(java.lang.String fieldName) {
         |            throw new UnsupportedOperationException();
         |        }
         |
         |        @Override
         |        public String toString() {
         |            return pbuffer.toString();
         |        }
         |    }
         |
         |    public final static class $builder implements $fqAlfaBuilder {
         |
         |        private final $fqPbOuterClassBuilder pbuilder;
         |        private final java.util.BitSet __missingFields;
         |        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;
         |
         |
         |        public $builder() {
         |            this( $fqPbOuterClass.newBuilder() );
         |        }
         |
         |        public $builder($fqPbOuterClassBuilder b) {
         |            this( b, com.schemarise.alfa.runtime.BuilderConfig.getInstance() );
         |        }
         |
         |        public $builder($fqPbOuterClassBuilder b, com.schemarise.alfa.runtime.IBuilderConfig bc) {
         |            pbuilder = b;
         |            __builderConfig = bc;
         |            __missingFields = new java.util.BitSet( ${udt.allFields.size} );
         |            __missingFields.set(0, ${udt.allFields.size} );
         |$setBits
         |        }
         |
         |        private void clearMissingFlag( short flag ) {
         |            __missingFields.clear( flag );
         |        }
         |
         |        @Override
         |        public $fqAlfaClz build() {
         |            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet( __builderConfig, __missingFields, ${udt.allFields.size}, ${descClz(udt)}.INSTANCE );
         |            return new $pbWrapClz.$clz(pbuilder.build());
         |        }
         |
         |$builderGetters
         |$builderSetters
         |
         |        @Override
         |        public void modify(java.lang.String fieldName, Object val) {
         |            throw new UnsupportedOperationException();
         |        }
         |
         |        @Override
         |        public Object get(java.lang.String fieldName) {
         |            throw new UnsupportedOperationException();
         |        }
         |
         |        @Override
         |        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
         |           return $fqAlfaClz.${clz}Descriptor.INSTANCE;
         |        }
         |
         |        @Override
         |        public String toString() {
         |            return pbuilder.toString();
         |        }
         |    }
         |}
      """.stripMargin)

    exitFile()
  }

  private def getPbWrapBuilderClz(udt: IUdtDataType): String = {
    udt.fullyQualifiedName + "AlfaProtoBridge." + udt.udt.name.name + "Builder"
  }


  private def getPbWrapClz(udt: IUdtDataType): String = {
    udt.fullyQualifiedName + "AlfaProtoBridge." + udt.udt.name.name
  }

  private def getPbBridgeClz(udt: IUdtDataType): String = {
    udt.fullyQualifiedName + "AlfaProtoBridge"
  }

  def convertAlfa2Pb(accessor: String, t: IDataType): String = {
    val dummy = "xxxxxx"
    t match {
      case dt: ScalarDataType =>
        dt.scalarType match {
          case Scalars.date =>
            s"java.time.LocalDate.ofEpochDay($accessor)"
          case _ =>
            accessor
        }
      case dt: ListDataType =>
        if (convertAlfa2Pb(dummy, dt.componentType) == dummy)
          accessor
        else {
          accessor + s".stream().map( e -> ${convertAlfa2Pb("e", dt.componentType)} ).collect(java.util.stream.Collectors.toList())"
        }
      case dt: MapDataType =>
        val keyaccess = if (convertAlfa2Pb(dummy, dt.keyType) == dummy)
          "java.util.Map.Entry::getKey"
        else
          s"e -> ${convertAlfa2Pb("e.getKey()", dt.keyType)}"

        val valueaccess = if (convertAlfa2Pb(dummy, dt.valueType) == dummy)
          "java.util.Map.Entry::getValue"
        else
          s"e -> ${convertAlfa2Pb("e.getValue()", dt.valueType)}"

        accessor + s".entrySet().stream().collect(java.util.stream.Collectors.toMap($keyaccess, $valueaccess))"


      case dt: UdtDataType =>
        if (dt.isUdtEnum)
          s"${getPbBridgeClz(dt.asInstanceOf[UdtDataType])}.alfaToPb( $accessor )"
        else {
          s"((${getPbWrapClz(dt)})$accessor).buffer()"
        }
    }
  }

  def convertPb2Alfa(accessor: String, t: IDataType): String = {
    val dummy = "xxxxxx"
    t match {
      case dt: ScalarDataType =>
        dt.scalarType match {
          case Scalars.date =>
            s"java.time.LocalDate.ofEpochDay($accessor)"
          case _ =>
            accessor
        }
      case dt: ListDataType =>
        if (convertPb2Alfa(dummy, dt.componentType) == dummy)
          accessor
        else {
          accessor + s".stream().map( e -> ${convertPb2Alfa("e", dt.componentType)} ).collect(java.util.stream.Collectors.toList())"
        }
      case dt: MapDataType =>
        val keyaccess = if (convertPb2Alfa(dummy, dt.keyType) == dummy)
          "java.util.Map.Entry::getKey"
        else
          s"e -> ${convertPb2Alfa("e.getKey()", dt.keyType)}"

        val valueaccess = if (convertPb2Alfa(dummy, dt.valueType) == dummy)
          "java.util.Map.Entry::getValue"
        else
          s"e -> ${convertPb2Alfa("e.getValue()", dt.valueType)}"

        accessor + s".entrySet().stream().collect(java.util.stream.Collectors.toMap($keyaccess, $valueaccess))"


      case dt: UdtDataType =>
        if (dt.isUdtEnum)
          s"${getPbBridgeClz(dt.asInstanceOf[UdtDataType])}.pbToAlfa( $accessor )"
        else {
          s"new ${getPbWrapBuilderClz(dt)}($accessor.toBuilder()).build()"
        }
    }
  }

  def pbAccessorMethod(local: String, f: IField): String = {
    val n = f.name
    val dt = toJavaTypeName(f.dataType)
    val dfltAccess = if (f.dataType.isList()) s"$local.get${pascalCase(n)}List()" else s"$local.get${pascalCase(n)}()"

    val expr = convertPb2Alfa(dfltAccess, f.dataType)

    s"""
       |        public $dt get${pascalCase(n)}() {
       |            return $expr;
       |        }
      """.stripMargin
  }

  def pbSetterMethod(local: String, f: IField, retType: String, udt: UdtBaseNode): String = {
    val n = pascalCase(f.name)
    val fn = localFieldName(f)
    val dt = toJavaTypeName(f.dataType)

    val dflt = s"$local.set$n"

    val unsupported = "{ throw new UnsupportedOperationException(); }"

    if (f.dataType.isMap()) {
      val t = f.dataType.asInstanceOf[MapDataType]
      val kdt = toJavaTypeName(t.keyType)
      val vdt = toJavaTypeName(t.valueType)

      return s"""        public $retType put$n( $kdt k, $vdt v ) {
           |            $local.put$n(${convertAlfa2Pb("k", t.keyType)}, ${convertAlfa2Pb("v", t.valueType)} );
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |
           |        public $retType putAll$n( $dt all ) {
           |            $local.putAll$n(${convertAlfa2Pb("all", t)});
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |        """.stripMargin
    }
    else if (f.dataType.isList()) {
      val t = f.dataType.asInstanceOf[ListDataType]
      val cdt = toJavaTypeName(t.componentType)

      return s"""        public $retType add$n( $cdt e ) {
           |            $local.add$n(${convertAlfa2Pb("e", t.componentType)});
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |
           |        public $retType addAll$n( $dt all ) {
           |            $local.addAll$n(${convertAlfa2Pb("all", f.dataType)});
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |        """.stripMargin
    }
    else if (f.dataType.isSet()) {
      val t = f.dataType.asInstanceOf[SetDataType]
      val cdt = toJavaTypeName(t.componentType)

      return s"""        public $retType add$n( $cdt e ) {
           |            $local.add$n(e);
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |
           |        public $retType addAll$n( $dt all ) {
           |            $local.addAll$n(all);
           |            clearMissingFlag( ${maskField(udt, n)} );
           |            return this;
           |        }
           |        """.stripMargin

    }
    else if (f.dataType.isInstanceOf[EnclosingDataType]) {
      val t = f.dataType.asInstanceOf[EnclosingDataType]

      if (t.isCompress) {
        val compType = toJavaTypeName(t.componentType)
        val setter = s"set${pascalCase(n)}"

        return s"""        public $retType $setter( $dt v );
             |
             |        public $retType $setter( $compType v );""".stripMargin
      }

      else if (t.isEncrypt) {
        val compType = toJavaTypeName(t.componentType)
        val setter = s"set${pascalCase(n)}"

        return s"""        public $retType $setter( $dt v );
             |
             |        public $retType $setter( $compType v );""".stripMargin
      }
    }

    var decl = s"""        $retType set${pascalCase(n)}( $dt v )""".stripMargin

    val expr =
      if (f.dataType.isScalarDate)
        s"$dflt( v.toEpochDay() )"
      else if (f.dataType.isUdtEnum)
        s"$dflt( ${getPbBridgeClz(f.dataType.asInstanceOf[UdtDataType])}.alfaToPb(v) )"
      else if (f.dataType.isUdt) {
        val u = f.dataType.asInstanceOf[UdtDataType]
        s"$dflt( ( ( ${getPbBridgeClz(u)}.${u.udt.name.name} ) v).buffer() )"
      }
      else
        dflt + "(v)"

    s"""
       |        public ${decl.trim()} {
       |            $expr;
       |            clearMissingFlag( ${maskField(udt, n)} );
       |            return this;
       |        }
      """.stripMargin
  }
}
