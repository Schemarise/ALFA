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

package com.schemarise.alfa.generators.importers.idl

import com.schemarise.alfa.compiler.utils.{ILogger, LexerUtils, VFS}
import com.schemarise.alfa.compiler.utils.antlr.CustomAntlrInputStream
import com.schemarise.alfa.generators.common.TextWriter
import com.schemarise.alfa.generators.importers.idl.IDLParser.{Interface_declContext, ModuleContext}
import org.antlr.v4.runtime.{CommonTokenStream, RuleContext}
import org.antlr.v4.runtime.tree.{ParseTree, TerminalNode}

import java.nio.file.Path
import java.util
import scala.collection.JavaConverters._

class IDLReader(logger: ILogger, outputDir: Path) extends TextWriter(logger) {
  private val modulesStack = new util.Stack[ModuleStackRecord]()
  private val interfaceStack = new util.Stack[Interface_declContext]()
  private val constDeclarationScope = new StringBuilder()
  private val declarationScope = new util.Stack[StringBuilder]()

  private val allAlfaTypeDefs = new util.HashMap[String, String]()
  private val allFullyQualifiedTypes = new util.HashSet[String]()

  // Types that have been moved from IDL interfaces etc, out to module context
  private val typeRenamedMap = new util.HashMap[String, String]()

  private var tokens: CommonTokenStream = null

  def read(inputFile: Path) = {
    val script = VFS.read(inputFile)
    val is = new CustomAntlrInputStream(Some(inputFile), script)
    tokens = new CommonTokenStream(new IDLLexer(is))
    val parser = new IDLParser(tokens)

    writeFile(inputFile, processSpec(inputFile, parser.specification()))
  }

  private def writeFile(inputFile: Path, str: String) = {
    val fname = inputFile.getFileName.toString.split("\\.").head
    enterFile(fname + ".alfa", false)
    writeln(str)
    exitFile()
  }

  private def processSpec(inputFile: Path, spec: IDLParser.SpecificationContext): String = {
    val imports = spec.import_decl()
    val definitions = spec.definition()
    declarationScope.push(new StringBuilder())

    val imps = imports.asScala.map(d => processImport(inputFile, d)).filter(e => !e.isBlank).mkString("\n")

    val defs = definitions.asScala.
      map(d => processDefn(d)).
      filter(e => !e.isBlank).mkString("\n\n")

    val typedefs =
      if (allAlfaTypeDefs.isEmpty) {
        ""
      } else {
        allAlfaTypeDefs.asScala.map(e => s"  ${e._1} = ${e._2}").mkString("\ntypedefs {\n", "\n", "\n}\n\n")
      }

    val decls = declarationScope.pop().toString()

    imps ++ constDeclarationScope.toString() ++ typedefs ++ decls ++ defs
  }

  private def processImport(parentFile: Path, d: IDLParser.Import_declContext): String = {
    val is = d.imported_scope()

    if (is.scoped_name() != null) {
      ???
    }
    else {
      val str = is.STRING_LITERAL().getText
      val f = parentFile.getParent.resolve(str.substring(1, str.length - 1))
      read(f)
    }
    ""
  }

  private def processDefn(d: IDLParser.DefinitionContext): String = {
    val annapps = d.annapps()

    val value = d.value()
    val type_id_decl = d.type_id_decl()
    val type_prefix_decl = d.type_prefix_decl()
    val event = d.event()
    val component = d.component()
    val home_decl = d.home_decl()
    val annotation_decl = d.annotation_decl()

    val body =
      processModule(d.module()) ++
        processInterfaceOrFwdIfc(d.interface_or_forward_decl()) ++
        processType(d.type_decl()) ++
        processConstDecl(d.const_decl()) ++
        processExcept(d.except_decl())

    body.trim
  }

  private def processType(td: IDLParser.Type_declContext): String = {
    if (td == null) {
      ""
    }
    else {
      if (td.bitmask_type() != null) {
        ???
      }

      if (td.bitset_type() != null) {
        ???
      }

      if (td.constr_forward_decl() != null) {
        // we ignore fwd decls
        val name = td.constr_forward_decl().ID().getText
        val ns = currentNamespace()
        allFullyQualifiedTypes.add(ns + name)
      }

      processTypeDef(td.type_declarator()) ++
        processStruct(td.struct_type()) ++
        processUnion(td.union_type()) ++
        processEnum(td.enum_type())
    }
  }

  private def processAnnotations(a: IDLParser.AnnappsContext): String = {
    ""
  }

  private def processAnnotations(a: java.util.List[IDLParser.AnnappsContext]): String = {
    ""
  }

  private def processEnum(s: IDLParser.Enum_typeContext,
                          inline: Boolean = false,
                          appendAndReturnTypeName: Boolean = false): String = {
    if (s == null)
      return ""

    val name = s.identifier().ID().getText

    val enumConsts =
      s.enumerator().asScala.map(e => {
        val id = e.identifier()
        val docs = getDocs(id.ID())

        val anns = processAnnotations(id.annapps())
        val const = id.ID().getText
        indent( "   ", s"$docs$anns$const" )
      })

    if (inline) {
      s"""enum<${enumConsts.mkString(",")} >""".stripMargin
    }
    else {
      val docs = getDocs(s.KW_ENUM())

      registerUdt(name)
      s"""
         |${docs}enum $name {
         |${enumConsts.mkString(",\n")}
         |}
         |""".stripMargin
    }

  }

  private def processUnion(s: IDLParser.Union_typeContext,
                           inline: Boolean = false,
                           appendAndReturnTypeName: Boolean = false): String = {
    if (s == null)
      return ""

    val name = s.identifier().ID().getText

    registerUdt(name)

    val cases =
      s.switch_body().case_stmt().asScala.map(c => {
        val dt = processTypeSpec(c.element_spec().type_spec())
        val n = processDeclarator(c.element_spec().declarator())

        val lbls = c.case_label().asScala.map(lbl => {
          if (lbl.KW_DEFAULT() != null) "default" else processConstExp(lbl.const_exp())
        }).mkString(", ")

        //        s"""  $n : $dt /* $lbls */""".stripMargin
        s"""  $n : $dt""".stripMargin
      })

    if (inline) {
      s"""union<${cases.mkString(", ")} >""".stripMargin
    }
    else {
      val docs = getDocs(s.KW_UNION())

      s"""
         |${docs}union $name {
         |${cases.mkString("\n")}
         |}
         |""".stripMargin
    }
  }

  private def processStruct(s: IDLParser.Struct_typeContext,
                            inline: Boolean = false,
                            appendAndReturnTypeName: Boolean = false): String = {
    if (s == null)
      return ""

    val name = s.identifier().ID().getText
    if (s.scoped_name() != null) {
      ???
    }

    val fields = s.member_list().member().asScala.map(m => {
      processMember(m)
    })

    if (inline) {
      s"""tuple< ${fields.mkString(", ")} >""".stripMargin
    }
    else {
      registerUdt(name)

      val docs = getDocs(s.KW_STRUCT())
      val decl =
        s"""${docs}record $name {
           |${fields.mkString("\n")}
           |}
           |""".stripMargin

      if (appendAndReturnTypeName) {
        this.declarationScope.peek().append(decl)
        currentNamespace() + name
      }
      else {
        decl
      }
    }
  }

  private def getDocs(node: TerminalNode, left: Boolean = true): String = {
    val tok =
      if (left)
        tokens.getHiddenTokensToLeft(node.getSymbol.getTokenIndex, 2)
      else
        tokens.getHiddenTokensToRight(node.getSymbol.getTokenIndex, 2)

    if (tok == null) {
      return ""
    }

    val doc =
      tok.asScala.map(t => {
        val tmp = t.getText.substring(2).trim
        val doc =
          if (tmp.endsWith("*/"))
            tmp.dropRight(2)
          else
            tmp

        doc
      }).mkString("\n")

    if (doc.isBlank)
      ""
    else if ( doc.contains("\n") ) {
      s"/# $doc #/\n"
    } else {
      s"# $doc\n"
    }
  }

  private def currentInterfacePrefixed(): String = {
    if (interfaceStack.isEmpty) {
      ""
    }
    else {
      val ifcName = interfaceStack.peek().interface_header().identifier().ID().getText
      ifcName + "."
    }
  }

  private def registerUdt(name: String): Unit = {
    val namespace = currentNamespace()
    val ifcPrefix = currentInterfacePrefixed()

    val idlName = namespace + ifcPrefix + name
    val alfaName = namespace + name

    allFullyQualifiedTypes.add(alfaName)

    if (!idlName.equals(alfaName)) {
      typeRenamedMap.put(idlName, alfaName)
    }
  }

  private def processMember(m: IDLParser.MemberContext): String = {
    val doc = getDocRelativeTo1stChild(m)
    val dt = processTypeSpec(m.type_spec())

    val n = LexerUtils.validAlfaIdentifier(processDeclarators(m.declarators()))

    indent("    ", s"${doc}$n : $dt" )
  }

  private def processDeclarators(s: IDLParser.DeclaratorsContext): String = {
    s.declarator().asScala.map(d => {
      processDeclarator(d)
    }).mkString("")
  }

  private def processDeclarator(d: IDLParser.DeclaratorContext): String = {
    if (d.simple_declarator() != null)
      d.simple_declarator().ID().getText
    else {
      val ad = d.complex_declarator().array_declarator()
      val consts = ad.fixed_array_size().asScala.map(e => processPositiveIntConst(e.positive_int_const()))
      ad.ID().getText
    }
  }


  private def processTypeSpec(c: IDLParser.Type_specContext, inline: Boolean = true, appendAndReturnTypeName: Boolean = false): String = {
    if (c == null)
      return ""

    if (c.simple_type_spec() != null) {
      val t = c.simple_type_spec()
      processSimpleTypeSpec(t)
    }
    else if (c.constr_type_spec() != null) {
      processConstrTypeSpec(c.constr_type_spec(), inline, appendAndReturnTypeName)
    }
    else {
      ???
    }
  }

  private def processConstrTypeSpec(
                                     c: IDLParser.Constr_type_specContext,
                                     inline: Boolean,
                                     appendAndReturnTypeName: Boolean) = {

    if (c.bitmask_type() != null || c.bitset_type() != null) {
      ???
    }

    processStruct(c.struct_type(), inline, appendAndReturnTypeName) ++
      processUnion(c.union_type(), inline, appendAndReturnTypeName) ++
      processEnum(c.enum_type(), inline, appendAndReturnTypeName)
  }

  private def processSimpleTypeSpec(t: IDLParser.Simple_type_specContext): String = {
    if (t.scoped_name() != null) {
      processScopedName(t.scoped_name())
    }
    else if (t.base_type_spec() != null) {
      processBaseTypeSpec(t.base_type_spec())
    }
    else if (t.template_type_spec() != null) {
      processTemplateTypeSpec(t.template_type_spec())
    }
    else {
      ???
    }
  }

  private def processTemplateTypeSpec(e: IDLParser.Template_type_specContext) = {
    processSetType(e.set_type()) ++
      processSequenceType(e.sequence_type()) ++
      processMapType(e.map_type()) ++
      processStringType(e.string_type()) ++
      processWideStringType(e.wide_string_type()) ++
      processFixedPtType(e.fixed_pt_type())
  }


  private def processBaseTypeSpec(ts: IDLParser.Base_type_specContext): String = {
    if (ts == null)
      return ""

    processFloatingPt(ts.floating_pt_type()) +
      processIntegerType(ts.integer_type()) +
      processCharType(ts.char_type()) +
      processWideCharType(ts.wide_char_type()) +
      processBoolType(ts.boolean_type()) +
      processOctetType(ts.octet_type()) +
      processAnyType(ts.any_type()) +
      processObjectType(ts.object_type()) +
      processValueBasedType(ts.value_base_type())
  }

  private def processPositiveIntConst(s: IDLParser.Positive_int_constContext): String = {
    processConstExp(s.const_exp())
  }

  private def processConstExp(c: IDLParser.Const_expContext): String = {
    if (c == null) {
      return ""
    }
    processOrExp(c.or_expr())
  }

  private def processOrExp(c: IDLParser.Or_exprContext): String = {
    c.xor_expr().asScala.map(x => processXorExpr(x)).mkString(" | ")
  }

  private def processXorExpr(c: IDLParser.Xor_exprContext): String = {
    c.and_expr().asScala.map(x => processAndExpr(x)).mkString(" ^ ")
  }

  private def processAndExpr(c: IDLParser.And_exprContext) = {
    c.shift_expr().asScala.map(x => processShiftExpr(x)).mkString(" && ")
  }

  private def processShiftExpr(c: IDLParser.Shift_exprContext) = {
    c.add_expr().asScala.map(x => processAddExpr(x)).mkString(" >><< ")
  }

  private def processAddExpr(c: IDLParser.Add_exprContext) = {
    c.mult_expr().asScala.map(x => processMultExpr(x)).mkString(" + ")
  }

  private def processMultExpr(c: IDLParser.Mult_exprContext) = {
    c.unary_expr().asScala.map(x => processUnaryExpr(x)).mkString(" * ")
  }

  private def processUnaryExpr(c: IDLParser.Unary_exprContext): String = {
    processPrimaryExpr(c.primary_expr())
  }

  private def processPrimaryExpr(c: IDLParser.Primary_exprContext): String = {
    if (c.literal() != null) {
      processLiteral(c.literal())
    }
    else if (c.scoped_name() != null) {
      processScopedName(c.scoped_name())
    }
    else if (c.const_exp() != null) {
      processConstExp(c.const_exp())
    }
    else {
      ???
    }
  }

  private def processLiteral(c: IDLParser.LiteralContext): String = {
    val lit =
      if (c.BOOLEAN_LITERAL() != null)
        c.getText.toLowerCase
      else if (c.STRING_LITERAL() != null) {
        val t = c.getText.substring(1, c.getText.length - 1)
        s""" "$t" """
      }
      else if (c.CHARACTER_LITERAL() != null) {
        val t = c.getText.substring(1, c.getText.length - 1)
        s""" "$t" """
      }
      else
        c.getText
    lit
  }

  private def processFixedPtType(c: IDLParser.Fixed_pt_typeContext): String = {
    if (c == null) {
      ""
    } else {
      val s = c.positive_int_const().asScala.map(e => processPositiveIntConst(e)).mkString(",")
      s"decimal($s)"
    }
  }

  private def processWideStringType(c: IDLParser.Wide_string_typeContext): String = {
    if (c == null) {
      ""
    } else {
      if (c.positive_int_const() != null) {
        processPositiveIntConst(c.positive_int_const())
      }

      s"string"
    }
  }

  private def processStringType(c: IDLParser.String_typeContext): String = {
    if (c == null) {
      ""
    } else {
      if (c.positive_int_const() != null) {
        processPositiveIntConst(c.positive_int_const())
      }

      s"string"
    }
  }

  private def processMapType(c: IDLParser.Map_typeContext): String = {
    if (c == null) {
      ""
    }
    else {
      val types = c.simple_type_spec().asScala
      val kt = processSimpleTypeSpec(types.head)
      val vt = processSimpleTypeSpec(types.last)

      if (c.positive_int_const() != null) {
        processPositiveIntConst(c.positive_int_const())
      }

      s"map<$kt,$vt>"
    }
  }

  private def processSequenceType(c: IDLParser.Sequence_typeContext): String = {
    if (c == null) {
      ""
    }
    else {
      val s = processSimpleTypeSpec(c.simple_type_spec())

      val constrait = if (c.positive_int_const() != null) {
        val i = processPositiveIntConst(c.positive_int_const())
        s"(0,$i)"
      }
      else ""

      s"list<$s>$constrait"
    }
  }

  private def processSetType(c: IDLParser.Set_typeContext): String = {
    if (c == null) {
      ""
    }
    else {
      val s = processSimpleTypeSpec(c.simple_type_spec())

      if (c.positive_int_const() != null) {
        processPositiveIntConst(c.positive_int_const())
      }

      s"set<$s>"
    }
  }

  private def processValueBasedType(c: IDLParser.Value_base_typeContext): String = {
    if (c != null)
      ???
    else
      ""
  }

  private def processObjectType(c: IDLParser.Object_typeContext) = {
    if (c != null)
      "$record"
    else
      ""
  }

  private def processAnyType(c: IDLParser.Any_typeContext) = {
    if (c != null)
      "$udt"
    else
      ""
  }

  private def processWideCharType(c: IDLParser.Wide_char_typeContext) = {
    if (c != null)
      "string"
    else
      ""
  }

  private def processBoolType(c: IDLParser.Boolean_typeContext) = {
    if (c != null)
      "boolean"
    else
      ""
  }

  private def processOctetType(c: IDLParser.Octet_typeContext) = {
    if (c != null)
      "binary"
    else
      ""
  }

  private def processCharType(c: IDLParser.Char_typeContext) = {
    if (c != null)
      "string(1,1)"
    else
      ""
  }

  private def processIntegerType(c: IDLParser.Integer_typeContext) = {
    if (c == null)
      ""
    else if (c.signed_int() != null)
      "int"
    else if (c.unsigned_int() != null)
      "int(0,*)"
    else
      ???
  }

  private def processFloatingPt(c: IDLParser.Floating_pt_typeContext): String = {
    if (c == null)
      ""
    else
      "double"
  }

  private def processTypeDef(c: IDLParser.Type_declaratorContext): String = {
    if (c == null)
      return ""

    val newType = processDeclarators(c.declarators())
    val ts = processTypeSpec(c.type_spec(), false, true)

    declarationScope.peek()

    var currNamespace = currentNamespace()

    val tgt = if (allFullyQualifiedTypes.contains(currentNamespace() + ts)) {
      currNamespace + ts
    }
    else {
      ts
    }

    // if typedef in a module, as typedef moved out, any reference to typedef in module
    // needs to be renamed
    if (currNamespace != "") {
      typeRenamedMap.put(currNamespace + newType, newType)
    }

    allAlfaTypeDefs.put(newType, tgt)
    ""
  }

  private def currentNamespace() = {
    if (modulesStack.isEmpty) {
      ""
    }
    else {
      modulesStack.asScala.map(e => e.moduleName).mkString("", ".", ".")
    }
  }

  private def processInterfaceOrFwdIfc(i: IDLParser.Interface_or_forward_declContext): String = {
    if (i == null) {
      ""
    }
    else {
      val annapps = i.annapps()
      val fwd = i.forward_decl()

      val ifc = i.interface_decl()

      val output = if (ifc != null) {

        processInterface(ifc)
      }
      else {
        ""
      }

      output
    }
  }

  private def processInterface(ifc: IDLParser.Interface_declContext): String = {
    if (ifc == null) {
      ""
    }
    else {
      val hdr = ifc.interface_header()

      val docs = getDocs(hdr.KW_INTERFACE())

      val isAbstract = hdr.KW_ABSTRACT() != null
      val isLocal = hdr.KW_LOCAL() != null

      val inheritList =
        if (hdr.interface_inheritance_spec() != null) {
          hdr.interface_inheritance_spec().interface_name().asScala.map(e => processAScopedName(e.a_scoped_name()))
        }
        else {
          Seq.empty
        }
      val inherits = if (inheritList.nonEmpty) inheritList.mkString("// includes ", ", ", " ") else ""

      val ifcName = hdr.identifier().ID().getText

      interfaceStack.push(ifc)
      val bdy = processInterfaceBody(ifc.interface_body())
      interfaceStack.pop()


      s"""
         |@alfa.lang.IgnoreServiceWarnings
         |${docs}service $ifcName $inherits
         |{
         |$bdy
         |}
         |""".stripMargin
    }
  }

  private def processInterfaceBody(bdy: IDLParser.Interface_bodyContext): String = {

    val bodyDecls =
      bdy.export_().asScala.map(e => {

        val annapps = e.annapps()

        if (e.type_id_decl() != null) {
          ???
        }

        if (e.type_prefix_decl() != null) {
          ???
        }

        val defn =
          if (e.op_decl() != null) {
            processOperation(e.op_decl())
          }
          else if (e.const_decl() != null) {
            val t = processConstDecl(e.const_decl())
            declarationScope.peek().append(t)
            ""
          }
          else if (e.type_decl() != null) {
            val t = processType(e.type_decl())
            declarationScope.peek().append(t)
            ""
          }
          else if (e.except_decl() != null) {
            val t = processExcept(e.except_decl())
            declarationScope.peek().append(t)
            ""
          }
          else if (e.attr_decl() != null) {
            val attr = e.attr_decl()
            var doc = getDocRelativeTo1stChild(e.attr_decl())

            if (attr.readonly_attr_spec() != null) {
              val ro = attr.readonly_attr_spec()
              val dt = processParamTypeSpec(ro.param_type_spec())
              val at = ro.readonly_attr_declarator()
              val decls = at.simple_declarator().asScala

              if (decls.size > 1)
                throw new RuntimeException("Unhanded readonly attrib params")

              indent( "   ", s"${doc}get${decls.head.ID().getText}() : $dt" )
            }
            else {
              val spec = attr.attr_spec()
              val dt = processParamTypeSpec(spec.param_type_spec())
              val decls = spec.attr_declarator().simple_declarator().asScala

              if (decls.size > 1)
                throw new RuntimeException("Unhanded attrib params")

              indent( "    ",
                s"""${doc}get${decls.head.ID().getText}() : $dt
                   |set${decls.head.ID().getText}( v : $dt ) : void
                   |""".stripMargin )
            }
          }
          else {
            ???
          }

        defn
      }).filter(e => !e.isBlank).mkString("\n")

    bodyDecls
  }

  private def processConstDecl(c: IDLParser.Const_declContext) : String = {
    if ( c == null ) {
      return ""
    }
    val n = c.identifier().ID().getText
    val const = processConstExp(c.const_exp())

    val ns = currentNamespace().replace(".", "_")
    val ifc = currentInterfacePrefixed().replace(".", "_")

    val decl = s"const $ns$ifc$n = $const\n"
    constDeclarationScope.append(decl)

    ""
  }


  private def processOperation(c: IDLParser.Op_declContext): String = {
    val oneWay = c.op_attribute() != null
    val annotations = processAnnotations(c.op_type_spec().annapps())

    var doc = getDocRelativeTo1stChild(c)

    val raises = processRaisesExpr(c.raises_expr())
    val retType =
      if (c.op_type_spec().KW_VOID() != null)
        "void"
      else
        processParamTypeSpec(c.op_type_spec().param_type_spec())

    val name = c.identifier().ID().getText

    val params = if (c.parameter_decls().param_decl() != null) {
      c.parameter_decls().param_decl().asScala.map(x => processParamDecl(x)).mkString(", ")
    } else
      ""

    indent( "    ", s"$doc$name( $params ) : $retType$raises" )
  }

  private def getDocRelativeTo1stChild(node: ParseTree) : String = {
    if ( node.isInstanceOf[TerminalNode] ) {
      return getDocs(node.asInstanceOf[TerminalNode])
    }

    for ( i <- 0 until node.getChildCount ) {
      val c = node.getChild(i)
      val docs = getDocRelativeTo1stChild( c )
      if ( ! docs.isBlank )
        return docs
    }

    return ""
  }

  private def processRaisesExpr(c: IDLParser.Raises_exprContext): String = {
    if (c == null) {
      return ""
    }

    c.a_scoped_name().asScala.
      map(x => processAScopedName(x)).
      mkString(" raises ( ", ", ", " )")
  }

  private def processParamDecl(x: IDLParser.Param_declContext): String = {
    val attrAnn = processAnnotations(x.attrAnn)
    val direction = processParamAttribute(x.param_attribute())

    val typeSpecAnn = processAnnotations(x.typeSpecAnn)
    val dtype = processParamTypeSpec(x.param_type_spec())

    val n = x.simple_declarator().ID().getText
    s"$direction $n : $dtype"
  }

  private def processParamAttribute(c: IDLParser.Param_attributeContext) = {
    if (c.KW_IN() != null) "in"
    else if (c.KW_INOUT() != null) "inout"
    else if (c.KW_OUT() != null) "out"
    else
      ???
  }

  private def processParamTypeSpec(c: IDLParser.Param_type_specContext) = {
    val res =
      processBaseTypeSpec(c.base_type_spec()) ++
        processStringType(c.string_type()) ++
        processWideStringType(c.wide_string_type()) ++
        processScopedName(c.scoped_name())

    res
  }

  private def processExcept(c: IDLParser.Except_declContext): String = {
    if (c == null)
      return ""

    val mems = c.member().asScala.map(m => processMember(m)).mkString("\n")
    val n = c.identifier().ID().getText

    val docs = getDocs(c.KW_EXCEPTION())

    registerUdt(n)
    s"""
       |@alfa.lang.Exception
       |${docs}record $n {
       |$mems
       |}
       |""".stripMargin
  }

  private def processAScopedName(c: IDLParser.A_scoped_nameContext) = {
    processScopedName(c.scoped_name())
  }

  private def processScopedName(c: IDLParser.Scoped_nameContext): String = {
    if (c == null)
      return ""

    val n = c.ID().asScala.map(_.getSymbol.getText).mkString(".")

    val fqn =
      if (allAlfaTypeDefs.containsKey(n) || allFullyQualifiedTypes.contains(n)) {
        n
      } else if (allFullyQualifiedTypes.contains(currentNamespace() + n)) {
        currentNamespace() + n
      } else {
        n
      }

    val td = typeRename(fqn)
    td
  }

  private def typeRename(n: String): String = {
    val y = typeRenamedMap.get(n)

    if (y != null) {
      y
    }
    else {
      n
    }
  }

  private def processModule(m: ModuleContext) = {
    if (m != null) {
      val definitions = m.definition()

      val ns = m.identifier().ID().getText

      modulesStack.push(ModuleStackRecord(m))
      declarationScope.push(new StringBuilder())

      val result = definitions.asScala.map(d => processDefn(d).trim).filter(_.nonEmpty).mkString("\n\n")

      modulesStack.pop()
      val sb = declarationScope.pop()
      val additional = sb.toString

      val docs = getDocs(m.KW_MODULE())

      s"""${docs}namespace $ns
         |
         |$additional
         |$result
         |""".stripMargin
    }
    else
      ""
  }

  override def outputDirectory: Path = outputDir

  case class ModuleStackRecord(m: ModuleContext) {
    def moduleName = m.identifier().ID().getText
  }
}
