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
 
package com.schemarise.alfa.generators.exporters.cpp.udt

import com.schemarise.alfa.compiler.ast.model.IEnum
import com.schemarise.alfa.compiler.ast.nodes.EnumDecl
import com.schemarise.alfa.compiler.utils.LangKeywords
import com.schemarise.alfa.generators.common.TextWriter
import com.schemarise.alfa.generators.exporters.cpp.PrinterBase

import java.nio.file.Path

class EnumPrinter(tw: TextWriter, outputDir: Path) extends PrinterBase(tw.logger, outputDir) {

  def print(udt: EnumDecl): Unit = {
    val clz = cppTypeName(udt)

    val en = udt
    val ns = cppNamespace(en.name)
    val fqn = cppFqClassName(en.name.fullyQualifiedName)

    val consts = udt.allFields.map(f => s"                ${validLangIdentifier(f._1, LangKeywords.cppKeywords)}").mkString(",\n")

    val switchCases = udt.allFields.map(f =>
      s"""
         |                    case ${validLangIdentifier(f._1, LangKeywords.cppKeywords)}:
         |                        return "${f._1}";""".stripMargin).mkString("")

    //    val suppconsts = udt.allFields.
    //      map(e => ( validIdentifier(e._1), e._1) ).
    //      map( f =>
    //        {
    //            val cfn = s"FIELD_NAME${localFieldName(f._1)}"
    //          s"""
    //             |        static inline std::string const $cfn = "${f._2}";
    //             |        inline const static schemarise::alfa::AlfaEnumConst ${f._1} = schemarise::alfa::AlfaEnumConst( $cfn, TYPE_NAME );""".stripMargin
    //        }).mkString("\n")

    val vector = udt.allFields.map(f => s"""                { "${f._1}", ${validLangIdentifier(f._1, LangKeywords.cppKeywords)} }""").mkString(",\n")


    tw.enterFile(cppHeaderFileName(en.name))

    val defn = "ALFA_TYPE_" + udt.name.fullyQualifiedName.replace('.', '_').toUpperCase()

    // https://stackoverflow.com/questions/21295935/can-a-c-enum-class-have-methods
    tw.writeln(
      s"""${fileHeader(udt)}
         |    class $clz : public schemarise::alfa::AlfaEnum {
         |        public:
         |            enum Value
         |            {
         |$consts
         |            };
         |
         |            $clz() = default;
         |
         |            constexpr $clz(Value en) : enumVal(en) { }
         |            constexpr operator Value() const { return enumVal; }
         |
         |            constexpr std::string_view type_name() const { return "${udt.name.fullyQualifiedName}"; }
         |
         |            inline std::string_view const_name() {
         |                switch( enumVal ) {$switchCases
         |                }
         |            }
         |
         |            static $clz toEnum(std::string_view s) {
         |                return string2enum.at( s );
         |            }
         |
         |        private:
         |            Value enumVal;
         |
         |            inline static const std::map< std::string_view, Value > string2enum = {
         |$vector
         |            };
         |    };
         |${fileFooter(udt)}
      """.stripMargin)

    tw.exitFile()
  }
}
