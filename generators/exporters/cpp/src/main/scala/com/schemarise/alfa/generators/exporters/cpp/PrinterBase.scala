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
 
package com.schemarise.alfa.generators.exporters.cpp

import com.schemarise.alfa.compiler.ast.UdtVersionedName
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.utils.ILogger
import com.schemarise.alfa.generators.common.TextWriter
import com.schemarise.alfa.runtime.AlfaRuntimeException

import java.nio.file.Path
import scala.collection.mutable.ListBuffer

abstract class PrinterBase(logger: ILogger, outputDir: Path) extends TextWriter(logger) {

  def outputDirectory: Path = outputDir

  val builderClz = dollar + "Builder"
  val modelClz = dollar + "TypeDescriptor"


  def cppHeaderFileName(n: UdtVersionedName): String = {
    val s = n.fullyQualifiedName.split("\\.")
    val fn = s.last

    s.dropRight(1).mkString("", "/", "/") + fn + ".h"
  }

  def cppCodeFileName(n: UdtVersionedName): String = {
    val s = n.fullyQualifiedName.split("\\.")
    val fn = s.last

    s.dropRight(1).mkString("", "/", "/") + fn + ".cpp"
  }

  def cppNamespace(n: UdtVersionedName): String = {
    val s = n.fullyQualifiedName.split("\\.")
    s.dropRight(1).mkString(".")
  }

  def getNonExtendedFields(udt: IUdtBaseNode) = {
    val localFields = udt.localFieldNames.toSet ++ udt.includesClosureFieldNames.toSet
    udt.allFields.filter(f => localFields.contains(f._1))
  }

  def udtEntityKeyType(udt: IUdtBaseNode) = {
    if (udt.isInstanceOf[IEntity] && udt.asInstanceOf[IEntity].key.isDefined)
      udt.asInstanceOf[IEntity].keyType
    else
      None
  }

  def doc(f: IDocAndAnnotated): String = {
    val docs = f.docs.mkString("")
    if (docs.trim.length > 0)
      s"""/** $docs */"""
    else
      ""
  }

  def localFieldDecl(udt: IUdtBaseNode) = {
    udt.allFields.map(f => "        " + typeAndName(f._2)).mkString("\n")
  }

  def accessorMethod(f: IField, classPrefix : String): String = {
    val n = f.name
    val dt = toCppTypeName(f.dataType)

    s"${doc(f)}const $dt ${classPrefix}get${pascalCase(n)}() const"
  }

  def fileHeader(udt: UdtBaseNode, forHeaderFile : Boolean = true ) : String = {
    val defn = "ALFA_" + udt.name.fullyQualifiedName.replace('.', '_').toUpperCase()

    val nss = cppNamespace(udt.name).split("\\.")

    if ( forHeaderFile ) {
      s"""#ifndef $defn
         |#define $defn
         |
         |${cppIncludes(udt)}
         |
         |${nss.map( n => s"namespace $n {") mkString("", "\n", "")}
         |""".stripMargin
    }
    else {
      s"""
         |#include "${udt.name.fullyQualifiedName.split("\\.").mkString("/")}.h"
         |
         |${nss.map(n => s"namespace $n {") mkString("", "\n", "")}
         |""".stripMargin
    }
  }

  def fileFooter(udt : UdtBaseNode, endif : Boolean = true) : String = {
    val nss = cppNamespace(udt.name).split("\\.")
    val nsEnd = nss.map( n => "}" ).mkString("\n")

    s"""
       |$nsEnd
       |
       |${if ( endif ) "#endif" else ""}
       |""".stripMargin
  }

  def mutatorMethod(f: IField, retType: String, classPrefix : String): String = {
    val n = f.name
    val dt = toCppTypeName(f.dataType, isConst=true)

    s"${doc(f)}$retType& ${classPrefix}set${pascalCase(n)}( $dt v ) "
  }

  def localUnionFieldDecl(udt: IUdtBaseNode) =
    udt.allFields.map(f => "    " + unionTypeAndName(f._2)).mkString("\n")

  def typeAndName(f: IField): String = {
    val n = localFieldName(f.name)
    val dt = toCppTypeName(f.dataType, false, false)

    val init = if ( f.dataType.isUdt && ! f.dataType.isUdtEnum && ! f.dataType.isUdtTrait ) {
      s" = ${_toCppTypeName(f.dataType)}::Default"
    }
    else {
      ""
    }

    s"$dt $n$init;"
  }

  def unionTypeAndName(f: IField): String = {
    val n = localFieldName(f.name)
    val dt = toCppTypeName(f.dataType)

    s"$n: alfa.builtin.Optional< $dt >"
  }

  def toTypeScriptQualifiedTypeName(u: IUdtBaseNode): String = {
    u.name.fullyQualifiedName
  }

  def toCppTypeName(dt: IDataType, isConst : Boolean = false, useRef : Boolean = true): String = {
    val res = _toCppTypeName(dt)

    val c = if ( isConst ) "const " else ""
    val r = if ( useRef && ! ( dt.isVoid() || dt.isScalarNumeric || dt.isScalarBoolean ) ) "&" else ""

    s"$c$res$r"
  }
  def _toCppTypeName(dataType: IDataType): String = {
    dataType match {
      case t: IEnclosingDataType =>
        if (t.isTry) "Try<" + toCppTypeName(t.componentType) + ">"
        else if (t.isEither) {
          val edt = t.asInstanceOf[IEitherDataType]
          s"Either< ${toCppTypeName(edt.left, useRef=false)}, ${toCppTypeName(edt.right, useRef=false)} >"
        }
        else if (t.isCompress) "DefaultCompressed[" + toCppTypeName(t.componentType) + "]"
        else if (t.isEncrypt) "DefaultEncrypted[" + toCppTypeName(t.componentType) + "]"
        else if (t.isFuture) "java.util.concurrent.Future< " + toCppTypeName(t.componentType) + " >"
        //          else if ( t.isKey ) {
        //            val ent = t.componentType.asInstanceOf[UdtDataType].resolvedType.get.asInstanceOf[IEntity]
        //            qualifiedTypeNameWithTypeArgs(ent.keyType.get)
        //          }
        else if (t.isOptional) "std::optional< " + toCppTypeName(t.componentType, useRef=false) + " >"
        else if (t.isStream) "java.util.Stream< " + toCppTypeName(t.componentType) + " >"
        else if (t.isTabular) "alfa.rt.ITable.ITable< " + toCppTypeName(t.componentType) + " >"
        else
          throw new AlfaRuntimeException("Unhandle enclosed type " + t.encType.toString)

      case t: IEnumDataType =>
        qualifiedClassName(t.syntheticEnum().name)

      case t: IMapDataType =>
        s"std::map<${toCppTypeName(t.keyType, useRef=false)}, ${toCppTypeName(t.valueType, useRef=false)}>"

      case t: IScalarDataType =>
        t.scalarType match {
          case Scalars.binary => "bytes"
          case Scalars.boolean => "bool"
          //            case Scalars.byte => "bytes"
          //            case Scalars.char => "std::string"
          case Scalars.date => "schemarise::alfa::Date"
          case Scalars.datetime => "schemarise::alfa::Datetime"
          case Scalars.decimal => "decimal"
          case Scalars.double => "double"
          case Scalars.duration => "duration"
          case Scalars.period => "period"
          //            case Scalars.float => "FLOAT"
          case Scalars.`int` => "int"
          case Scalars.long => "long"
          //            case Scalars.pattern => "std::string"
          case Scalars.short => "short"
          case Scalars.string => "std::string"
          case Scalars.time => "schemarise::alfa::Time"
          //            case Scalars.uri => "URI"
          case Scalars.uuid => "std::string"
          case Scalars.void => "void"
        }

      case t: IListDataType =>
        s"std::vector< ${toCppTypeName(t.componentType, useRef=false)} >"

      case t: ISetDataType =>
        s"std::set< ${toCppTypeName(t.componentType, useRef=false)} >"

      case t: ITabularDataType =>
        s"AlfaTable< ${t.targetUdt.fullyQualifiedName} >"

      case t: ITupleDataType =>
        qualifiedClassName(t.syntheticRecord.name)

      case t: IUdtDataType =>
        val tmpl = if (t.typeArguments.isDefined) {
          t.typeArguments.get.map(e => toCppTypeName(e)).mkString("< ", ", ", " >")
        } else ""

        val qcn = qualifiedClassName(t.udt.name) + tmpl

        //          if ( t.isUdtEnum && wrapEnum ) {
        //            s"schemarise::alfa::Enum< $qcn >"
        //          }
        //          else
        {
          qcn
        }

      case t: IUnionDataType =>
        qualifiedClassName(t.syntheticUnion.name)

      case t: IMetaDataType =>
        "AlfaObject"

      case t: ITypeParameterDataType =>
        t.parameterName
    }
  }

  private def qualifiedClassName(t: IUdtVersionName) = {
    cppFqClassName(t.fullyQualifiedName)
  }

  def cppFqClassName(s: String) =
    s.replace(".", "::")


  def cppIncludes(u: UdtBaseNode): String = {

    val deps = new ListBuffer[IUdtDataType]()

    u.traverse(new NoOpNodeVisitor() {
      override def enter(e: IUdtDataType): Mode = {
        deps += e
        super.enter(e)
      }

      override def enter(e: IEnumDataType): Mode = {
        deps += e.syntheticEnum().asInstanceOf[UdtBaseNode].asDataType
        super.enter(e)
      }

      override def enter(e: ITupleDataType): Mode = {
        deps += e.syntheticRecord.asInstanceOf[UdtBaseNode].asDataType
        super.enter(e)
      }
    })

    val d = deps.map(e => e.udt.name).map(e => {
      val p = e.fullyQualifiedName.replace(".", "/")
      s"""#include "${p}.h" """
    }).toSet.mkString("\n")

    s"""
       |#include "schemarise_alfa.h"
       |$d
    """.stripMargin
  }

  private def valStmt(t: IDataType, path: String): String = {
    val checks = ListBuffer[String]()

    t match {
      case e: IEnclosingDataType =>
        if (e.isOptional) {
          val d = valStmt(e.componentType, path + ".value()")
          val c = s"$path"

          if (d.size > 0)
            checks += s" $c && ( $d ) "
        }

      case e: IScalarDataType =>
        e.scalarType match {
          case Scalars.int | Scalars.long | Scalars.short | Scalars.double | Scalars.decimal =>
            if (e.min.isDefined)
              checks += s"$path >= ${e.min.get.toString}"

            if (e.max.isDefined)
              checks += s"$path < ${e.max.get.toString}"

          case Scalars.string =>
            if (e.min.isDefined)
              checks += s"$path.length() >= ${e.min.get.toString}"

            if (e.max.isDefined)
              checks += s"$path.length() < ${e.max.get.toString}"

          case _ =>
        }

      case e: IListDataType =>
        if (e.min.isDefined)
          checks += s"$path.size() >= ${e.min.get.toString}"

        if (e.max.isDefined)
          checks += s"$path.size() < ${e.max.get.toString}"

      case e: IMapDataType =>
        if (e.min.isDefined)
          checks += s"$path.size() >= ${e.min.get.toString}"

        if (e.max.isDefined)
          checks += s"$path.size() < ${e.max.get.toString}"

      case e: ISetDataType =>
        if (e.min.isDefined)
          checks += s"$path.size() >= ${e.min.get.toString}"

        if (e.max.isDefined)
          checks += s"$path.size() < ${e.max.get.toString}"

      case _ =>
    }

    checks.map(e => s"!($e)").mkString(" || ")
  }

  def validateFn(u: IUdtBaseNode): String = {
    val valFns = u.allFields.
      map(f => {
        val s = valStmt(f._2.dataType, localFieldName(f._1))
        if (s.trim.length > 0)
          s"""
             |                if ( $s )
             |                    throw std::runtime_error( "Validation failed on ${f._1}" );
           """.stripMargin
        else
          ""
      }).mkString("")

    valFns
  }

  def setAllMethod(u: IUdtBaseNode, declOnly : Boolean = false): String = {

    val fmls = u.allFields.
      map(f => {
        val t = toCppTypeName(f._2.dataType, isConst = true)
        val n = localFieldName(f._1)
        s"$t _$n"
      }).mkString(", ")

    val set = u.allFields.
      map(f => {
        val n = localFieldName(f._1)
        s"        set${pascalCase(f._1)}(_$n);"
      }).mkString("\n")

    if ( declOnly ) {
      s"""        void set($fmls);"""
    }
    else {
      val className = cppTypeName(u)
      s"""    void ${className}Builder::set($fmls) {
         |$set
         |    }
         |""".stripMargin
    }
  }

  def cppTypeName(u: IUdtBaseNode): String = {
    val vn = u.name
    val n = vn.fullyQualifiedName
    n.substring(n.lastIndexOf('.') + 1)
  }

  def typeNameOnly(u: IUdtDataType): String = {
    val n = u.fullyQualifiedName
    n.substring(n.lastIndexOf('.') + 1)
  }

  def qualifiedTypeNameWithTypeArgs(u: IUdtDataType): String = {
    val args = if (u.typeArguments.isDefined)
      u.typeArguments.get.map(ta => {

        toCppTypeName(ta, true)
      }).mkString("[", ", ", "]")
    else
      ""

    qualifiedClassName(u.udt.name) + args
  }

  def qualifiedTypeName(u: IUdtDataType): String = {
    toTypeScriptPackageName(u) + "." + typeNameOnly(u)
  }

  def typeParamsGeneric(u: IUdtBaseNode, needComma: Boolean, surroundBrackets: Boolean = false): String = {
    if (u.name.typeParameters.size > 0) {
      val c = if (needComma) ", " else ""

      val args: Seq[String] = u.name.typeParameters.map(_._1.name.fullyQualifiedName).toSeq
      val stmt = args.mkString(", ")

      val gstmt = "typing.Generic[" + stmt + "]" + c

      if (surroundBrackets) s"($gstmt)"
      else gstmt
    }
    else ""
  }


  def declTypeParams(u: IUdtBaseNode): String = {
    if (u.name.typeParameters.size > 0) {
      u.name.typeParameters.map(v => {
        val n = v._1.name.fullyQualifiedName
        s"$n = typing.TypeVar('$n', covariant=True)"
      }).mkString("\n")
    }
    else ""
  }

  def toTypeScriptPackageName(udt: IUdtBaseNode): String = {
    toTypeScriptPackageName(udt.asDataType)
  }

  def toTypeScriptPackageName(udt: IUdtDataType): String = {
    val n = udt.fullyQualifiedName

    if (n.indexOf('.') == -1)
      return "default_namespace"

    n.substring(0, n.lastIndexOf('.'))
  }

  def localFieldName(s: String): String =
    "_" + s

  def udtMetaType(udt: IUdtBaseNode): String = {
    var mt = ""
    udt.whenEntity(_ => mt = "entity")
    udt.whenKey(_ => mt = "key")
    udt.whenRecord(_ => mt = "record")
    udt.whenTrait(_ => mt = "trait")
    udt.whenUnion(u => mt = if (u.isTagged) "union" else "untaggedUnion")
    udt.whenEnum(_ => mt = "enum")
    udt.whenNativeUdt(_ => mt = "nativeUdt")

    "alfa.rt.model.UdtMetaType.UdtMetaType." + mt + "Type"
  }
}
