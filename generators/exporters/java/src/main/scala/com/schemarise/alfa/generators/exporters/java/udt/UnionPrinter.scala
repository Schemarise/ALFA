package com.schemarise.alfa.generators.exporters.java.udt

import java.nio.file.Path

import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IUdtBaseNode}
import com.schemarise.alfa.compiler.ast.model.types.IVectorDataType
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IField}
import com.schemarise.alfa.compiler.ast.model.types.IVectorDataType
import com.schemarise.alfa.compiler.ast.nodes.Union
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes

class UnionPrinter(logger: ILogger, outputDir: Path, cua: ICompilationUnitArtifact, compilerToRt: CompilerToRuntimeTypes, reqMutable: Boolean)
  extends RecordPrinter(logger, outputDir, cua, compilerToRt, reqMutable) {
  override val mandatoryInclude = "com.schemarise.alfa.runtime.Union"

  override protected def auxiliaryCode(udt: IUdtBaseNode) = {
    val model = mp.print(udt)
    val builder = bp.print(udt)

    val clzImm = toJavaVersionedClassName(udt)

    val reqdFields = udt.includesClosureFieldNames ++ udt.localFieldNames

    val setterFields = udt.allFields.filter(f => reqdFields.contains(f._1))

    val fieldCases = setterFields.map(f => fieldCase(udt, clzImm, f._2)).mkString("")

    s"""
       |$fieldCases
       |
       |    //<editor-fold defaultstate="collapsed" desc="Builder class">
       |$builder
       |    //</editor-fold>
       |
       |    //<editor-fold defaultstate="collapsed" desc="TypeDescriptor class">
       |$model
       |    //</editor-fold>
       |
      """.stripMargin
  }

  override def accessorMethod(clz: String, f: IField): String = {
    val n = f.name
    val dt = toJavaTypeName(f.dataType)

    s"""
       |    public default $dt get${pascalCase(n)}( ) {
       |      throw new IllegalStateException( "Accessing field $n is not supported in " + getClass().getSimpleName() );
       |    }
       |
       |    public default boolean is${pascalCase(n)}( ) { return false; }
      """.stripMargin
  }

  private def fieldCase(u: IUdtBaseNode, str: String, f: IField) = {
    val n = s"Case${f.name}"
    val dt = toJavaTypeName(f.dataType)
    val localField = localFieldName(f.name)

    val args = if (u.name.typeParameters.size > 0)
      u.name.typeParameters.map(_._1.name.fullyQualifiedName).mkString("< ", ", ", " >")
    else ""

    val tagged = u.asInstanceOf[Union].isTagged

    s"""
       |    //<editor-fold defaultstate="collapsed" desc="$n">
       |    static final class $n$args implements $str {
       |        private $dt $localField;
       |
       |        private $n( com.schemarise.alfa.runtime.IBuilderConfig __builderConfig, $dt v ) {
       |            this.$localField = v;
       |            ${iip.validateField(f)}
       |        }
       |        public $dt get${pascalCase(f.name)}() { return this.$localField; }
       |        public boolean is${pascalCase(f.name)}() { return true; }
       |        public java.lang.String caseName() { return "${f.name}"; }
       |        public java.lang.Object caseValue() { return $localField; }
       |
       |        public boolean isTagged() {
       |            return $tagged;
       |        }
       |
       |        public java.lang.Object get( java.lang.String f ) {
       |            if ( f.equals("${f.name}") )
       |                return $localField;
       |            else
       |                throw new IllegalStateException("Cannot access ${f.name}");
       |        }
       |
       |        public void traverse( com.schemarise.alfa.runtime.Visitor v ) { }
       |
       |        public int hashCode() { return com.schemarise.alfa.runtime.utils.Utils.unionHashCode( this ); }
       |
       |        public java.lang.String toString() { return com.schemarise.alfa.runtime.utils.Utils.unionToString( this ); }
       |
       |        public boolean equals(Object o) { return com.schemarise.alfa.runtime.utils.Utils.unionEquals( this, o ); }
       |
       |        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
       |            return ${descClz(u)}.INSTANCE;
       |        }
       |    }
       |    //</editor-fold>
       |
     """.stripMargin
  }

  private def whenVector(t: IVectorDataType, caseClass: String, dt: String, n: String, modMethods: String, implClass: String) = {
    //    val validate =  if ( t.valuesRange.isDefined ) {
    //      val vr = t.valuesRange.get
    //      s"""com.schemarise.alfa.runtime.utils.Utils.validateCollectionSize( "$n", all, ${vr._1}, ${vr._2}, """
    //    } else
    //      ""
    //       |            ${if (validate.size > 0) validate + "all.size() );" else ""}

    s"""
       |        public $caseClass build$n( $dt all ) {
       |            $dt m = new java.util.$implClass<>();
       |            m.$modMethods( all );
       |            return new $caseClass( m );
       |        }
         """.stripMargin
  }

  private def buildImpl(immClz: String, f: IField): String = {
    val n = f.name
    val dt = toJavaTypeName(f.dataType)
    val caseClass = "Case" + f.name

    val whenMap: Option[String] = f.dataType.whenMap(t => {
      whenVector(t, caseClass, dt, n, "putAll", "HashMap")
    })

    val whenSet: Option[String] = f.dataType.whenSet(t => {
      whenVector(t, caseClass, dt, n, "addAll", "HashSet")
    })

    val whenSeq: Option[String] = f.dataType.whenList(t => {
      whenVector(t, caseClass, dt, n, "addAll", "ArrayList")
    })


    val vectors = "" +
      (if (whenMap.isDefined) whenMap.get else "") +
      (if (whenSet.isDefined) whenSet.get else "") +
      (if (whenSeq.isDefined) whenSeq.get else "")


    if (vectors.length > 0)
      vectors
    else {
      s"""        public $caseClass build$n( $dt v ) { return new $caseClass( v ); }
      """.stripMargin
    }

  }

}