package com.schemarise.alfa.generators.exporters.markdown

import schemarise.alfa.runtime.model.{Namespace, NativeType, UdtDataType, UdtMetaType, UdtVersionedName}
import com.schemarise.alfa.compiler.ast.model.{ICompilationUnitArtifact, IUdtBaseNode, IdentifiableNode}
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, Nodes}
import com.schemarise.alfa.compiler.ast.nodes.{Testcase, UdtBaseNode}
import com.schemarise.alfa.compiler.tools.graph._
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}
import com.schemarise.alfa.generators.common.CompilerToRuntimeTypes
import net.sourceforge.plantuml.{FileFormat, FileFormatOption, SourceStringReader}

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import scala.collection.JavaConverters._
import scala.collection.mutable

object MarkdownUtils {
  private def umldecorator(t: UdtBaseNode): String = {
    val x = t.nodeType match {
      case Nodes.Annotation => "A,90CCDE"
      case Nodes.Entity => "E,A09BCC"
      case Nodes.Enum => "E,86B0BE"
      case Nodes.Key => "K,67931A"
      case Nodes.Library => "L,88A02C"
      case Nodes.Transform => "M,88A0FE"
      case Nodes.Record => "R,D9B01C"
      case Nodes.Service => "S,754F71"
      case Nodes.Trait => "T,995579"
      case Nodes.Union => "U,418798"
      case Nodes.NativeUdt => "N,985241"
      case Nodes.Testcase => "X,985241"
      case Nodes.Method => "M,485241"
    }

    s"""<< ($x) >>"""
  }

  def plantumlHeader(): String = {
    s"""
       |@startuml
       |!pragma layout smetana
       |
       |skinparam ClassBackgroundColor #BUSINESS
       |hide empty methods
       |hide empty fields
       |skinparam backgroundColor transparent
       |set namespaceSeparator none
       |
       |""".stripMargin
  }

  def writeSvgAndCreateLink(outputDir : Path, fullyQualifiedName: String, uml: String) = {

    val reader = new SourceStringReader(uml)
    val os = new ByteArrayOutputStream()
    reader.outputImage(os, new FileFormatOption(FileFormat.SVG))
    os.close()

    // The XML is stored into svg
    val svg = new String(os.toByteArray(), Charset.forName("UTF-8"))

    val svgFile = s"images/$fullyQualifiedName.svg"
    VFS.write(outputDir.resolve(svgFile),svg)

    s"<img src='$svgFile'/>\n"
  }
}

class MarkdownUtils(cua: ICompilationUnitArtifact, c2r: CompilerToRuntimeTypes) {

  val spacing = "&nbsp;&nbsp;"

  def udtAsLink(n: UdtDataType, showFullName: Boolean, largeIcon: Boolean, asHtml: Boolean, htmlLink : Boolean): String = {
    val fullName = n.getFullyQualifiedName
    val name = if (showFullName) fullName else fullName.split("\\.").last

    val deco = decorator(n.getUdtType, largeIcon)

    val udtDetails = c2r.getUdtDetails(fullName)

    if (udtDetails.isResult) {
      val udt = udtDetails.getResult

      val extension = if ( htmlLink ) "html" else "md"

      if (n.getUdtType == UdtMetaType.nativeUdtType) {
        udt.asInstanceOf[NativeType].getAliasedType
      }
      else if (asHtml)
        s"<a href='UDT-$fullName.$extension'>$deco&nbsp;$name</a>"
      else
        s"$deco [$name](UDT-$fullName.$extension)"
    }
    else
      fullName
  }

  def udtAsLink(n: UdtVersionedName, showFullName: Boolean, largeIcon: Boolean, asHtml: Boolean): String = {
    val fullName = n.getFullyQualifiedName
    val name = if (showFullName) fullName else fullName.split("\\.").last

    val deco = decorator(n.getUdtType, largeIcon)

    val udt = c2r.getUdtDetails(fullName).getResult

    if (n.getUdtType == UdtMetaType.nativeUdtType) {
      udt.asInstanceOf[NativeType].getAliasedType
    }
    else if (asHtml)
      s"<a href='UDT-$fullName.md'>$deco&nbsp;$name</a>"
    else
      s"$deco [$name](UDT-$fullName.md)"
  }

  def decorator(t: UdtMetaType, large: Boolean): String = {
    val sz = if ( large ) "-lg" else ""
    val f = s"images/${t.value()}$sz.svg"
    s"<img src='$f'/>"
  }

  private def dataTypeStr(dt: IDataType) = {

    val str = dt.toString

    val idx = str.indexOf("\n")

    val t = if (idx > 0)
      str.substring(0, idx) + ". . ."
    else if (str.length > 40)
      str.substring(0, 40) + ". . ."
    else
      str

    if (dt.isVoid()) "" else " : " + t
  }

  private def matchingNamespace(e: UdtBaseNode, i: IdentifiableNode) = {

    if (i.isInstanceOf[UdtBaseNode]) {
      val ku = i.asInstanceOf[UdtBaseNode]
      val nsmatch = ku.name.namespace.name.equals(e.name.namespace.name) // only show field types which are in the same namespace
      nsmatch
    }
    else {
      val ku = i.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType]
      val nsmatch = ku.udt.name.namespace.name.equals(e.name.namespace.name) // only show field types which are in the same namespace
      nsmatch

    }

  }


  def generateIndexPageMermaid(l: ILogger, cua: ICompilationUnitArtifact, ns: Namespace) = {
    val indexPageUml = new StringBuilder()

    val udtsx = cua.graph.topologicalOrPermittedOrdered().get.
      filter(e => e.isInstanceOf[UdtBaseNode]).
      map(e => e.asInstanceOf[UdtBaseNode]).
      filter(e => e.namespaceNode.name.equals(ns.getQualifiedName)).
      filter(e => e.isKey || !e.isSynthetic).
      filter(e => e.isKey || e.writeAsModuleDefinition)

    val summaryUdts = udtsx.filter(e => !e.isEnum && !e.isTestcase && !e.isAnnotation)

    val sizeOfItemsToShow =
      if (summaryUdts.size < 10)
        10
      else if (summaryUdts.size < 25)
        5
      else if (summaryUdts.size < 50)
        3
      else
        0

    val dotDefs = summaryUdts.map(e => {

      val localFields = e.allFields.filter(f => e.localFieldNames.contains(f._1))

      val locals = localFields.map(f => "    " + f._1)

      val fields =
        if (sizeOfItemsToShow == 0)
          "" // dont show any
        else if (locals.size > sizeOfItemsToShow)
          locals.take(sizeOfItemsToShow).mkString("\n") +
            s"\n    ...${locals.size - sizeOfItemsToShow} more fields"
        else
          locals.mkString("\n")

      val methodsList =
        if (e.isService) {
          e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Service].getMethodSignatures().toList.sortBy(e => e._1).map(m => {
            s"    ${m._1}()"
          })

        } else if (e.isLibrary) {
          e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Library].getMethodSignatures.toList.sortBy(e => e._1).map(m => {
            s"    ${m._1}()"
          })

        } else if (e.isTransform) {
          e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Transformer].getMethodSignatures.toList.sortBy(e => e._1).map(m => {
            s"    ${m._1}()"
          })

        } else if (e.isTestcase) {
          e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Testcase].getMethodSignatures.toList.sortBy(e => e._1).map(m => {
            s"    ${m._1}()"
          })
        }
        else {
          Seq.empty
        }

      val methods =
        if (sizeOfItemsToShow == 0)
          "" // dont show any
        else if (methodsList.size > sizeOfItemsToShow)
          methodsList.take(sizeOfItemsToShow).mkString("\n") +
            s"\n    ...(${methodsList.size - sizeOfItemsToShow} more methods)"
        else
          methodsList.mkString("\n")

      val name = e.name.fullyQualifiedName

      val result =
        s"""class ${nameOnly(name)} ${MarkdownUtils.umldecorator(e)} {
           |
           |$fields${methods}
           |}
      """.stripMargin

      result

    }).mkString("\n")


    val links = summaryUdts.map(e => {

      val incsx = e.includes.
        filter(i => matchingNamespace(e, i.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType])).
        map(i => {
          nameOnly(i.fullyQualifiedName) + " <|.. " + nameOnly(e.name.fullyQualifiedName)
        })

      val extendz = e.extendsDef.map(e => e).
        filter(i => matchingNamespace(e, i.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType])).
        map(i => {
          nameOnly(i.fullyQualifiedName) + " <|-- " + nameOnly(e.name.fullyQualifiedName)
        })

      val entToKeys = cua.graph.outgoingEdgeNodes(e, IsEntityKeyEdgePredicate).
        filter(i => matchingNamespace(e, i))
        .map(i => {
          val ku = i.asInstanceOf[UdtBaseNode]
          //if (ku.writeAsModuleDefinition)
          nameOnly(ku.name.fullyQualifiedName) + " <.. " + nameOnly(e.name.fullyQualifiedName) + " : Key"
          //else
          //  ""
        })

      val outEdges = cua.graph.outgoingEdgeNodes(e, new IsFieldDataTypePredicate(new Vertex(e))).
        filter(i => {
          val ku = i.asInstanceOf[UdtBaseNode]
          !ku.isSynthetic && !ku.isEnum
        }).
        filter(i => matchingNamespace(e, i))

      val fieldDts = outEdges.
        filter(e => outEdges.length < 10).
        map(i => {
          val ku = i.asInstanceOf[UdtBaseNode]

          // if synth key link to entity
          if (ku.isKey && !ku.writeAsModuleDefinition && ku.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Key].entityName.isDefined) {
            val k = ku.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Key]
            nameOnly(e.name.fullyQualifiedName) + " -- " + nameOnly(k.entityName.get)
          } else {
            nameOnly(e.name.fullyQualifiedName) + " -- " + nameOnly(ku.name.fullyQualifiedName)
          }
        })

      val tests = cua.graph.outgoingEdgeNodes(e, IsTestcasePredicate).
        filter(i => matchingNamespace(e, i))
        .map(i => {
          nameOnly(e.asInstanceOf[Testcase].targetUdt.get.name.fullyQualifiedName) + ".." + nameOnly(e.name.fullyQualifiedName)
        })

      val links = e.allAccessibleLinkages().values.map(l => {
        val tgtFqn = l.targetType.fullyQualifiedName
        val multi = if (l.isList) "{" else ">"
        val linkStyle = if (l.isOptional) "line.dashed;" else "line.dotted;"

        nameOnly(e.name.fullyQualifiedName) + " --" + multi + " " + nameOnly(tgtFqn) + s" #gray;${linkStyle}text:gray : \uD83D\uDD17 //" + l.name + "//"
      })


      val entries = (tests ++ fieldDts ++ entToKeys ++ incsx ++ extendz ++ links).mkString("\n")
      entries

    }).mkString("\n")


    indexPageUml.append(
      s"""
         |$dotDefs
         |$links
      """.stripMargin)

    indexPageUml.toString()

  }

  def generateClassUml(cua: ICompilationUnitArtifact, udt: IUdtBaseNode): String = {
    val uml = new StringBuilder()

    var related = new mutable.ListBuffer[IUdtBaseNode]()
    related += udt

    val incs = cua.graph.outgoingEdgeNodes(udt, IsIncludesOnlyPredicate).
      filter(e => udt.includes.filter(x => x.fullyQualifiedName == e.asInstanceOf[UdtBaseNode].name.fullyQualifiedName).size > 0).
      map(i => {
        related += i.asInstanceOf[UdtBaseNode]
        nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName) + " <|.. " + nameOnly(udt.name.fullyQualifiedName)
      })

    val extendz = cua.graph.outgoingEdgeNodes(udt, IsExtendsOnlyPredicate).
      filter(e => udt.extendsDef.get.fullyQualifiedName == e.asInstanceOf[UdtBaseNode].name.fullyQualifiedName).
      map(i => {
        related += i.asInstanceOf[UdtBaseNode]
        nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName) + " <|-- " + nameOnly(udt.name.fullyQualifiedName)
      })

    val included = cua.graph.incomingEdgeNodes(udt, IsIncludesOnlyPredicate).
      filter(e => e.asInstanceOf[UdtBaseNode].includes.filter(x => x.fullyQualifiedName == udt.name.fullyQualifiedName).size > 0).
      map(i => {
        related += i.asInstanceOf[UdtBaseNode]
        nameOnly(udt.name.fullyQualifiedName) + " <|.. " + nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName)
      })

    val extended = cua.graph.incomingEdgeNodes(udt, IsExtendsOnlyPredicate).
      filter(e => e.asInstanceOf[UdtBaseNode].extendsDef.get.fullyQualifiedName == udt.name.fullyQualifiedName).
      map(i => {
        related += i.asInstanceOf[UdtBaseNode]
        nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName) + " --|> " + nameOnly(udt.name.fullyQualifiedName)
      })

    val entToKeys = cua.graph.outgoingEdgeNodes(udt, IsEntityKeyEdgePredicate).map(i => {
      related += i.asInstanceOf[UdtBaseNode]
      nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName) + " <.. " + nameOnly(udt.name.fullyQualifiedName) + " : Key"
    })

    val keyToEnt = cua.graph.incomingEdgeNodes(udt, IsEntityKeyEdgePredicate).map(i => {
      related += i.asInstanceOf[UdtBaseNode]
      nameOnly(i.asInstanceOf[UdtBaseNode].name.fullyQualifiedName) + " ..> " + nameOnly(udt.name.fullyQualifiedName) + " : Key"
    })

    val modelVal = c2r.getUdtDetails(udt.name.fullyQualifiedName).getResult

    // Exclude Entity pointed by the Key - that will be picked separately
    val entityToKey = if (udt.isKey) {
      val k = udt.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Key]
      k.entityName
    }
    else
      None

    val referencedFrom = if (modelVal.getReferencedInFieldTypeFrom.isPresent)
      modelVal.getReferencedInFieldTypeFrom.get().asScala.
        filter(
          e => !(entityToKey.isDefined && e.getFullyQualifiedName == entityToKey.get)). // key relations are shown separately
        map(i => {
          related += cua.getUdt(i.getFullyQualifiedName).get
          nameOnly(i.getFullyQualifiedName) + " --> " + nameOnly(udt.name.fullyQualifiedName)
        })
    else
      List.empty

    val tests = cua.graph.outgoingEdgeNodes(udt, IsTestcasePredicate).map(i => {
      related += i.asInstanceOf[UdtBaseNode]
      nameOnly(udt.asInstanceOf[Testcase].targetUdt.get.name.fullyQualifiedName) + " .. " + nameOnly(udt.name.fullyQualifiedName)
    })

    val linksDefs = (tests ++ referencedFrom ++ entToKeys ++ keyToEnt ++ incs ++ extendz ++ included ++ extended)

    val links = linksDefs.mkString("\n")

    val dotDefs = related.filter(e => !e.isSynthetic).distinct.
      map(e => {
        val root = e.equals(udt)

        val localFields = e.allFields.filter(f => root && e.localFieldNames.contains(f._1))
        val nonlocalFields = e.allFields.filter(f => root && !e.localFieldNames.contains(f._1)).filter(e => false)

        // Make showing datatype configuable

        val local = limitAndFormatFields(localFields.map(f => s"  ${f._1}" ).toList, 30 )
        val nonlocal = limitAndFormatFields(nonlocalFields.map(f => s"  ${f._1}").toList, 30 )

        val methodSigs = {
          if (!root) {
            Seq.empty
          }
          else if (e.isService) {
            e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Service].getMethodSignatures()
          }
          else if (e.isLibrary) {
            e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Library].getMethodSignatures()
          }
          else if (e.isTestcase) {
            e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Testcase].getMethodSignatures()
          }
          else if (e.isTransform) {
            e.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Transformer].getMethodSignatures()
          }
          else {
            Seq.empty
          }
        }

        val methods =
          methodSigs.toList.
            sortBy(e => e._1).
            map(m => {
              // make showing full method configurable?
//              "  " + m._1 + m._2.formals.map(f => f._1 + dataTypeStr(f._2.dataType)).mkString("( ", ", ", " )") + dataTypeStr(m._2.returnType)
              "  " + m._1 + "()"
            }).mkString("\n")

        val name = e.name.fullyQualifiedName

        val result =
          s"""class ${nameOnly(name)} ${MarkdownUtils.umldecorator(e.asInstanceOf[UdtBaseNode])} {
             |
             |$local
             |${if (nonlocalFields.size > 0) "-- Included --" else ""}
             |$nonlocal
             |$methods
             |}
       """.stripMargin

        result
      }
      ).mkString("\n")

    uml.append(
      s"""
         |$dotDefs
         |$links
      """.stripMargin)

    uml.toString()
  }

  private def limitAndFormatFields(l: List[String], i: Int ) : String = {

    val f = l.take(i).mkString("\n")

    if ( l.size > i ) {
      val a = s"${l.size - i } more fields"
      f + s"\n  ...$a\n"
    } else
      f
  }

  def classDiagram(outputDir : Path, udt: IUdtBaseNode) = {
    val defs = generateClassUml(cua, udt)

    val uml = s"""${MarkdownUtils.plantumlHeader()}
       |
       |$defs
       |
       |@enduml
       |""".stripMargin

    MarkdownUtils.writeSvgAndCreateLink( outputDir, udt.name.fullyQualifiedName, uml)
  }

  private def nameOnly(s: String): String = {

    val split = s.split("\\.")
    val ns = split.dropRight(1).mkString(".")

    c2r.getAllNamespaces.getNamespaces.keySet().forEach(e => {
      if (ns == e)
        return split.last
    })

    s
  }
}
