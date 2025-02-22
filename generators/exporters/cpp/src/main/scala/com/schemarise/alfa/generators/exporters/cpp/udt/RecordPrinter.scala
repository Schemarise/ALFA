package com.schemarise.alfa.generators.exporters.cpp.udt

import com.schemarise.alfa.compiler.AlfaInternalException
import com.schemarise.alfa.compiler.ast.model.IUdtBaseNode
import com.schemarise.alfa.compiler.ast.model.types.{Enclosed, Scalars}
import com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.generators.common.{CompilerToRuntimeTypes, TextWriter}
import com.schemarise.alfa.generators.exporters.cpp.PrinterBase

import java.nio.file.Path

class RecordPrinter(tw: TextWriter, outputDir: Path, compilerToRt: CompilerToRuntimeTypes, udt: UdtBaseNode) extends PrinterBase(tw.logger, outputDir) {

  private val cppNameOnly = cppTypeName(udt)
  private val cppBuilderNameOnly = cppNameOnly + "Builder"

  def print(): Unit = {
    printHeaderFile()
    printImpl()
  }

  private def printImpl(): Unit = {

    val validate = validateFn(udt)

    val toStr = udt.allFields.
      map(f => {
        val fn = localFieldName(f._1)
        if (f._2.dataType.isEncOptional()) {
          s"""
             |        if ( $fn )
             |            jw.visit($fn, "${f._1}");""".stripMargin
        }
        else
          s"""
             |        jw.visit($fn, "${f._1}");""".stripMargin
      }).mkString("")

    val equalsBody =
      if (udt.allFields.isEmpty) "true"
      else
        udt.allFields.keys.map(f => {
          val fn = localFieldName(f)
          s"$fn == other.$fn"
        }).mkString("\n", " &&\n", "")


    tw.enterFile(cppCodeFileName(udt.name))
    tw.writeln(
      s"""${fileHeader(udt, false)}
         |    // --- $cppNameOnly implementations ---
         |
         |    $cppNameOnly::$cppNameOnly() { }
         |
         |    const $cppNameOnly $cppNameOnly::Default = $cppNameOnly();
         |
         |${getSetMethods(udt, true, false)}
         |
         |    bool $cppNameOnly::operator==(const $cppNameOnly& other) const {
         |        return ${indent("            ", equalsBody)};
         |    }
         |
         |
         |    void $cppNameOnly::validate() {
         |$validate
         |    }
         |
         |    std::ostream& operator <<(std::ostream& os, const $cppNameOnly& p){
         |        return os;
         |    }
         |
         |    // --- $cppBuilderNameOnly implementations ---
         |
         |    $cppBuilderNameOnly::$cppBuilderNameOnly() {
         |        instance = $cppNameOnly();
         |    }
         |
         |${getSetMethods(udt, false, true)}
         |
         |${setAllMethod(udt)}
         |    $cppNameOnly ${cppBuilderNameOnly}::build() {
         |${udt.allFields.zipWithIndex.filter(f => f._1._2.dataType.isEncOptional()).map(f => s"        assigned_fields[${f._2}] = 1;").mkString("\n")}
         |        if ( assigned_fields.count() != assigned_fields.size() )
         |            throw std::runtime_error( "A mandatory field has not been set " + assigned_fields.to_string() );
         |
         |        instance.validate();
         |        return $cppNameOnly(instance);
         |    }
         |
         |${fileFooter(udt, false)}
         |""".stripMargin)
    tw.exitFile()
  }

  private def printHeaderFile() = {

    //    def ct(fn: String, id: Int) = {
    //      s"""            static std::string const FIELD_NAME${localFieldName(fn)} = "$fn";
    //         |            static int const FIELD_ID${localFieldName(fn)} = $id; """.stripMargin
    //    }
    //
    //    val consts =
    //      s"""${udt.allFields.zipWithIndex.map(f => s"${ct(f._1._1, f._2)}").mkString("\n")}
    //         |"""
    //        .stripMargin

    val exts = Seq("public schemarise::alfa::AlfaObject") ++ udt.includes.map( i => s"public ${i.fullyQualifiedName.replace(".", "::")}" )

    tw.enterFile(cppHeaderFileName(udt.name))
    tw.writeln(
      s"""${fileHeader(udt)}
         |
         |    class $cppBuilderNameOnly;
         |
         |    class $cppNameOnly : ${exts.mkString(", ")} {
         |
         |        friend class $cppBuilderNameOnly;
         |
         |        private:
         |        $cppNameOnly();
         |
         |        public:
         |        virtual ~$cppNameOnly() = default;
         |        const static $cppNameOnly Default;
         |
         |        // builders
         |        static $cppBuilderNameOnly builder();
         |        $cppBuilderNameOnly toBuilder();
         |
         |        // accessors
         |${getSetMethods(udt, true, false, true)}
         |
         |        // utilities
         |        bool operator==(const $cppNameOnly& other) const;
         |        friend std::ostream& operator <<(std::ostream& os, const $cppNameOnly& p);
         |        virtual void validate();
         |
         |        private:
         |        // data fields
         |${localFieldDecl(udt)}
         |    };
         |
         |    class $cppBuilderNameOnly {
         |        public:
         |        // constructors
         |        $cppBuilderNameOnly();
         |        virtual ~$cppBuilderNameOnly() = default;
         |
         |        // mutators
         |${getSetMethods(udt, false, true, true)}
         |${setAllMethod(udt, true)}
         |
         |        $cppNameOnly build();
         |
         |        private:
         |        ${cppNameOnly} instance;
         |        std::bitset<${udt.allFields.size}> assigned_fields;
         |    };
         |${fileFooter(udt)}
         |""".stripMargin)
    tw.exitFile()
  }

  def getSetMethods(udt: IUdtBaseNode, getter: Boolean, setter: Boolean, declOnly: Boolean = false) = {
    val n = cppTypeName(udt)

    udt.allFields.zipWithIndex.
      map(fx => {
        val fname = fx._1._1
        val fld = fx._1._2
        val fidx = fx._2

        val gprefix = if (declOnly) "" else s"$n::"
        val sprefix = if (declOnly) "" else s"${n}Builder::"

        val g = accessorMethod(fld, gprefix)
        val s = mutatorMethod(fld, n + "Builder", sprefix)

        val get = if (setter) ""
        else {
          if (declOnly) s"        $g;"
          else {
            s"""    $g {
               |        return ${localFieldName(fname)};
               |    }
           """.stripMargin
          }
        }

        val set = if (getter) ""
        else if (declOnly) s"        $s;"
        else {
          s"""
             |    $s {
             |        instance.${localFieldName(fname)} = v;
             |        assigned_fields[$fidx] = 1;
             |        return *this;
             |    }""".stripMargin
        }

        s"$get$set"
      }).mkString("\n")
  }/**
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
 

}