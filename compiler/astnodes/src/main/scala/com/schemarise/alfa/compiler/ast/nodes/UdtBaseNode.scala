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
package com.schemarise.alfa.compiler.ast.nodes

import java.util
import com.schemarise.alfa.compiler.{AlfaInternalException, Context}
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.expr.IQualifiedStringLiteral
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.model.{IToken, IUdtBaseNode, IdentifiableNode, NodeVisitor, _}
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, TypeParameterDataType, UdtDataType}
import com.schemarise.alfa.compiler.err._
import com.schemarise.alfa.compiler.types.{AnnotationTargetType, Modifiers}
import com.schemarise.alfa.compiler.utils.{TextUtils, TokenImpl}
import org.jgraph.graph.DefaultEdge
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.JavaConverters._
import scala.collection.immutable.{ListMap, ListSet}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import com.schemarise.alfa.compiler.antlr.AlfaParser.ExpressionUnitContext
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.tools.repo.ArtifactEntry

abstract class UdtBaseNode(
                            ctx: Option[Context] = None,
                            val location: IToken = TokenImpl.empty,
                            declaredRawNamespace: NamespaceNode = NamespaceNode.empty,
                            rawNodeMeta: NodeMeta = NodeMeta.empty,
                            modifiersNode: Seq[ModifierNode] = Seq.empty,
                            val declaredRawName: StringNode,
                            val versionNo: Option[IntNode] = None,
                            val typeParamsNode: Option[Seq[TypeParameter]] = None,
                            val typeArgumentsNode: Option[Map[String, DataType]] = None,
                            rawExtendedNode: Option[UdtDataType] = None,
                            rawIncludesNode: Seq[UdtDataType] = Seq.empty,
                            rawFieldRefs: Seq[FieldOrFieldRef] = Seq.empty,
                            val rawMethodDeclNodes: Seq[MethodDeclaration] = Seq.empty,
                            assertNodes: Seq[AssertDeclaration] = Seq.empty,
                            linkageNodes: Seq[LinkageDeclaration] = Seq.empty,
                            imports: Seq[ImportDef] // = Seq.empty
                          )
  extends BaseNode
    with AnnotatableNode
    with DocumentableNode
    with ResolvableNode
    with IAssignable
    with TraversableNode
    with IdentifiableNode with IUdtBaseNode {

  def compUnitImports = imports


  val SkipGenAnnotation = "alfa.gen.Skip"

  private var _localAndFragFieldRefs: Seq[FieldOrFieldRef] = Seq.empty
  private var _localAndFragIncludes: Seq[UdtDataType] = Seq.empty
  private var _localAndFragExtends: Seq[UdtDataType] = Seq.empty
  private var _localAndFragAsserts: Seq[AssertDeclaration] = Seq.empty
  private var _localAndFragLinkages: Seq[LinkageDeclaration] = Seq.empty
  private var _localAndFragNodeMeta: Seq[NodeMeta] = Seq.empty
  private var _localAndFragMethodDecls: Seq[MethodDeclaration] = Seq.empty

  private def localAndFragIncludes = _localAndFragIncludes

  private def localAndFragExtends = _localAndFragExtends

  private def localAndFragAsserts = _localAndFragAsserts

  private def localAndFragLinkages = _localAndFragLinkages

  private def localAndFragFieldRefs = _localAndFragFieldRefs

  private def localAndFragNodeMeta = _localAndFragNodeMeta

  private def localAndFragMethodDecls = _localAndFragMethodDecls

  val repositoryEntry: Option[ArtifactEntry] = if (ctx.isDefined) ctx.get.currentRepositoryEntry else None

  override val isLoadedFromRepository = repositoryEntry.isDefined

  private val nsInName = if (this.nodeType == Nodes.Method) NamespaceNode.empty else declaredRawNamespace
  private val un = UdtName.create(nsInName, declaredRawName, typeParamsNode, typeArgumentsNode)
  private val vn = if (versionNo.isDefined) Some(versionNo.get) else None

  val versionedName = UdtVersionedName(un, vn)(Some(udtType))

  val namespaceNode: NamespaceNode =
    if (versionedName.namespace.name.equals(declaredRawNamespace.nameNode.text))
      declaredRawNamespace
    else
      new NamespaceNode(StringNode(declaredRawName.location, versionedName.namespace.name), true, declaredRawNamespace.location, NodeMeta.empty)

  private var _allAccessibleFields: ListMap[String, Field] = ListMap.empty
  private var _allAccessibleAsserts: ListMap[String, AssertDeclaration] = ListMap.empty
  private var _allAccessibleLinkages: ListMap[String, LinkageDeclaration] = ListMap.empty
  private var _declaredFields: ListSet[String] = ListSet.empty
  private var _localAndParentalAnnotations: Set[Annotation] = Set.empty

  private var defragState = ResolutionState.NotStarted
  private val modifiersSet = modifiersNode.map(e => e.modifier).toSet
  private var templated = mutable.Map[Seq[DataType], UdtBaseNode]()

  private val udtTestcases = new ListBuffer[Testcase]()

  private val accessibleConsts: Map[StringNode, ExpressionUnitContext] = if (ctx.isDefined) ctx.get.registry.getCurrentConsts() else Map.empty
  private var allAccessibleConsts: Map[StringNode, ExpressionUnitContext] = accessibleConsts

  private val accessibleImports: List[UdtVersionedName] = if (ctx.isDefined) ctx.get.registry.getCurrentImports() else List.empty
  private var allAccessibleImports: List[UdtVersionedName] = accessibleImports


  def registerTestcase(testcase: Testcase) = {
    udtTestcases += testcase
  }

  def getTestcases = udtTestcases.toList

  override def docNodes: Seq[IDocumentation] = localAndFragNodeMeta.map(_.docs).flatten

  override def annotationNodes = localAndFragNodeMeta.map(_.annotations).flatten

  def nodeMetas = _localAndFragNodeMeta

  def methodDecls = _localAndFragMethodDecls

  def fields = _localAndFragFieldRefs

  override def docs = docNodes

  override def annotations = annotationNodes

  override def includes: Seq[IUdtDataType] = localAndFragIncludes

  override def extendsDef: Option[IUdtDataType] = localAndFragExtends.headOption

  def isSynthetic = false

  def writeAsModuleDefinition = true

  override def allAsserts = allAccessibleAsserts()

  override def allLinkages = allAccessibleLinkages()

  def allSingularAsserts = allAccessibleAsserts().filter(!_._2.collectionAssert)

  def allVectorizedAsserts = allAccessibleAsserts().filter(_._2.collectionAssert)

  override def localAndInheritedAnnotations: Seq[IAnnotation] = _localAndParentalAnnotations.toSeq

  override def hashCode(): Int = nodeId.id.hashCode

  def isInternal = modifiersSet.contains(Modifiers.internal)

  def isFragment = modifiersSet.contains(Modifiers.fragment)

  def skipCodeGen = {
    annotationsMap.contains(UdtVersionedName(name = StringNode.create(SkipGenAnnotation)))
  }

  def rawDeclaredFields = rawFieldRefs


  private val _versions = new ListBuffer[UdtBaseNode]()

  def addVersion(node: UdtBaseNode) = {
    _versions += node
  }

  def versions = _versions.toList

  override def getMethodSignatures(): Map[String, IMethodSignature] = {
    allAccessibleMethods().map(e => (e._1, e._2.signature))
  }

  private def defragment(ctx: Context): Unit = {
    if (defragState == ResolutionState.NotStarted) {
      defragState = ResolutionState.Started

      _localAndFragFieldRefs ++= rawFieldRefs
      _localAndFragIncludes ++= rawIncludesNode
      _localAndFragExtends ++= rawExtendedNode
      _localAndFragAsserts ++= assertNodes
      _localAndFragLinkages ++= linkageNodes
      _localAndFragMethodDecls ++= rawMethodDeclNodes
      _localAndFragNodeMeta ++= Seq(rawNodeMeta)

      if (!isFragment) {
        val frags = ctx.registry.getFragments(Some(this), new UdtDataType(location = nodeId.location, name = nodeId.asStringNode))

        if (frags.size > 0) {
          frags.foreach(frag => {
            frag.startPreResolve(ctx, this.parent)
            if (!frag.hasErrors) {
              _localAndFragFieldRefs ++= frag.localAndFragFieldRefs
              _localAndFragIncludes ++= frag.localAndFragIncludes
              _localAndFragExtends ++= frag.localAndFragExtends
              _localAndFragAsserts ++= frag.localAndFragAsserts
              _localAndFragLinkages ++= frag.localAndFragLinkages
              _localAndFragNodeMeta ++= frag.localAndFragNodeMeta
              _localAndFragMethodDecls ++= frag.localAndFragMethodDecls

              allAccessibleConsts ++= frag.accessibleConsts
              allAccessibleImports ++= frag.accessibleImports
            }
          })
        }
      }

      defragState = ResolutionState.Completed
    }
  }

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[UdtBaseNode]) {
      val n = obj.asInstanceOf[UdtBaseNode]
      n.nodeId.id.equals(nodeId.id)
    }
    else
      false
  }

  def name = versionedName

  override def nodeId = // : StringNode = StringNode( versionedName.location, versionedName.fullyQualifiedName )
    new LocatableNodeIdentity(getClass.getSimpleName, versionedName.fullyQualifiedNameAndVersion)(versionedName.location)

  def instantiatable = typeParamsNode.size == 0

  def traverseBody(v: NodeVisitor): Unit = {
    rawNodeMeta.annotations.foreach(_.traverse(v))
    rawIncludesNode.foreach(_.traverse(v))

    if (isPreResolved())
      allAccessibleFields().values.foreach(_.traverse(v))
    else
      rawFieldRefs.filter(e => e.field.isDefined).foreach(_.field.get.traverse(v))

//    methodDecls.foreach(_.traverse(v))
//    allAccessibleAsserts().values.foreach(_.traverse(v))
  }

  override def resolvableInnerNodes() =
    asSeq(rawNodeMeta) ++
      //    asSeq(namespaceNode) ++ // THIS SHOULD ONLY BE RESOLVED BY THE COMP UNIT OTHERWISE CAUSES CYCLIC PRE-RESOLVE (CDM)
      (if (typeParamsNode.isDefined) typeParamsNode.get else Seq.empty) ++
      (if (typeArgumentsNode.isDefined) typeArgumentsNode.get.values else Seq.empty) ++
      rawIncludesNode ++
      rawFieldRefs ++
      _allAccessibleFields.values ++
      (if (isFragment) Seq.empty else localAndFragAsserts) ++
      (if (isFragment) Seq.empty else localAndFragLinkages) ++
      methodDecls ++
      _localAndParentalAnnotations

  def allAccessibleFields() = {
    // can be circular
    if (isBeingPreResolved()) {
      ListMap(fields.filter(f => f.field.isDefined).map(f => f.field.get).map(f => f.name -> f): _*)
    }
    else if (!isPreResolved()) {
      throw new AlfaInternalException(s"Cannot get allAccessibleFields of ${name} if not preResolved")
    }
    else {
      _allAccessibleFields
    }
  }


  def allAccessibleAsserts() = {
    // can cause stack overflow
    //    assertPreResolved(None)
    _allAccessibleAsserts
  }


  def allAccessibleLinkages() = {
    // can cause stack overflow
    //    assertPreResolved(None)
    _allAccessibleLinkages
  }


  def allAccessibleMethods() = {
    methodDecls.map(e => e.signature.nameNode.text -> e).toMap
  }

  override def allFields = allAccessibleFields()

  override def allAddressableFields = allAccessibleFields()

  override def localFieldNames = _declaredFields

  def validateUnique(ctx: Context, refs: Seq[StringNode], kind: String): Unit = {
    val dups = refs.groupBy(identity).filter(e => e._2.size >= 2).map(e => e._2.last)

    dups.foreach(d => {
      ctx.addResolutionError(ResolutionMessage(d.location, DuplicateEntry)(None, List.empty, kind, d.text))
    })
  }

  private def invalidInclude(t: UdtBaseNode): Boolean =
    !(t.nodeType == Nodes.Trait || t.nodeType == Nodes.Enum || t.nodeType == Nodes.Key)

  def validateVersionDecls(ctx: Context) = {
    if (versionedName.isVersioned) {
      var gapFound = false

      var v = versionedName.version.get
      (1 to v).toList.foreach(i => {
        val versionNoSearch = Some(new IntNode(Scalars.int, Some(i))(TokenImpl.empty))
        val udtName = new UdtDataType(name = StringNode.create(versionedName.fullyQualifiedName), versionNode = versionNoSearch)
        val udt = ctx.registry.getUdt(None, udtName, false)
        if (udt.isEmpty && !gapFound) {
          ctx.addResolutionError(ResolutionMessage(versionedName.versionNode.get.location, MissingVersionForUdt)(None, List.empty, v, versionedName.udtType, versionedName.fullyQualifiedName, i))
          gapFound = true
        }
      })

      val udtName = new UdtDataType(name = StringNode.create(versionedName.fullyQualifiedName))
      val udt = ctx.registry.getUdt(None, udtName, false)

      if (udt.isEmpty) {
        ctx.addResolutionError(ResolutionMessage(versionedName.versionNode.get.location, MissingNonVersionForUdt)(None, List.empty, v, versionedName.udtType, versionedName.fullyQualifiedName))
        gapFound = true
      }
      else {
        udt.get.addVersion(this)
      }
    }
  }

  override protected def postResolve(ctx: Context): Unit = {
    super.postResolve(ctx)

    validateVersionDecls(ctx)
  }


  def topologicallySortedFields = topologicallySorted(_allAccessibleFields)

  private def topologicallySorted(fields: ListMap[String, Field]): ListMap[String, Field] = {
    val graph = new DefaultDirectedGraph[Field, DefaultEdge](classOf[DefaultEdge])

    fields.foreach(f => graph.addVertex(f._2))

    if (!hasErrors) {
      fields.foreach(f => {
        if (f._2.expressionNode.isDefined) {
          val e = f._2.expressionNode.get
        }
      })

      val topoList = new TopologicalOrderIterator[Field, DefaultEdge](graph).asScala.toList

      ListMap(topoList.map(e => e.name -> e): _*)
    }
    else
      fields
  }

  private def checkInternalUsage(ctx: Context): Unit = {
    if (!isInternal) {
      this.traverse(new NoOpNodeVisitor() {
        override def enter(e: IUdtDataType): Mode = {
          val udt = e.asInstanceOf[UdtDataType].resolvedType
          if (udt.isDefined && udt.get.isInternal) {
            ctx.addResolutionError(e.asInstanceOf[UdtDataType], InternalTypeReferenced,
              udt.get.name.fullyQualifiedName, UdtBaseNode.this.name.fullyQualifiedName)
          }
          NodeVisitMode.Continue
        }
      })
    }
  }

  private val internalUseNodes = Set(Nodes.Key, Nodes.Trait, Nodes.Entity, Nodes.Record, Nodes.Service, Nodes.Library, Nodes.Union)

  override protected def resolve(ctx: Context): Unit = {
    if (isFragment) {
      val u = ctx.registry.getUdt(None, new UdtDataType(location = declaredRawName.location, name = StringNode.create(un.fullyQualifiedName)), false)

      if (u.isEmpty)
        ctx.addResolutionError(this, FragmentHasNoMatchingUdt, un.fullyQualifiedName)
    }

    extendsCycleCheck(ctx, this, this)

    ctx.registry.pushConsts(allAccessibleConsts)

    val allImports: Seq[ImportDef] = allAccessibleImports.
      map(e => ImportDef(StringNode.create(e.fullyQualifiedName), false)).toSet.toSeq

    ctx.registry.pushImports(ctx, allImports)

    super.resolve(ctx)

    ctx.registry.popConsts()
    ctx.registry.popImports()

    if (internalUseNodes.contains(nodeType))
      checkInternalUsage(ctx)

    if (!hasErrors) {

      val incFields = localAndFragIncludes.map(e => e.resolvedType.get.localFieldNames).flatten

      localAndFragFieldRefs.filter(_.field.isDefined).map(_.field.get).foreach(f => {
        if (incFields.contains(f.name)) {
          ctx.addResolutionWarning(f.location, FieldRedeclared, f.name)
        }
      })

      val extFields = localAndFragExtends.filter(_.resolvedType.isDefined).map(e => e.resolvedType.get.localFieldNames).flatten

      localAndFragFieldRefs.filter(_.field.isDefined).map(_.field.get).foreach(f => {
        if (extFields.contains(f.name)) {
          ctx.addResolutionWarning(f.location, FieldRedeclared, f.name)
        }
      })

      includes.map(_.asInstanceOf[UdtDataType]).filter(e => !e.hasErrors && e.udt.isInstanceOf[Trait]).foreach(e => {
        val ib = e.udt.asInstanceOf[Trait].scope
        val incInIncludedBy = ib.filter(e => e.fullyQualifiedName == this.name.fullyQualifiedName)
        if (!ib.isEmpty && incInIncludedBy.size == 0) {
          ctx.addResolutionError(e, IncludeTypeNotIncludedInIncludedByList, e.name.text, this.name.name, e.name.text)
        }
      })
    }
  }

  override def preResolve(ctx: Context): Unit = {
    // defrag has to be before any nodes preresolve
    defragment(ctx)

    if (!isExtension) {
      // extensions do not have comp unit
      val compUnit = locateCompUnitParent()
      ctx.registry.pushImports(ctx, compUnit.imports)
    }

    ctx.registry.pushTypeParameters(typeParamsNode)

    super.preResolve(ctx)

    if (!isSynthetic)
      SynthNames.assertNameWarnings(ctx, name.location, name.name)

    if (!isFragment) {

      val includesClosureSet = includesDependencyClosure(ctx, Set(this.name.fullyQualifiedName))
      // IMPORTANT we get templateInstantiated of includes/extends closure so any templated fields etc are resolved to the final type
      val preResolvedIncludesClosureSet = includesClosureSet.map(e => {
        val r = e // .templateInstantiated
        // Its enough to only preResolve
        r.startPreResolve(ctx, parent)
        r
      }).toSeq

      // IMPORTANT we get templateInstantiated of includes/extends closure so any templated fields etc are resolved to the final type
      val preResolvedExtends = localAndFragExtends.map(e => {
        val r = e // .templateInstantiated
        // Its enough to only preResolve
        r.startPreResolve(ctx, parent)
        if (r.hasErrors)
          None
        else
          Some(r.udt.asInstanceOf[UdtBaseNode])
      }).filter(_.isDefined).map(_.get)

      if (localAndFragExtends.size > 1) {
        preResolvedExtends.foreach(e => {
          ctx.addResolutionError(e, MultipleExtends, name.name)
        })
      }

      val incsAndExtends = preResolvedExtends.reverse ++ preResolvedIncludesClosureSet.reverse

      if (!isInstanceOf[MethodSignature] && ctx.registry.containsNamespace(Namespace(versionedName.fullyQualifiedName)))
        ctx.addResolutionError(ResolutionMessage(location, NameConflictsWithANamespace)(None, List.empty, versionedName.fullyQualifiedName))

      if (typeParamsNode.isDefined)
        typeParamsNode.get.
          map(tp => (tp, ctx.registry.getUdt(new UdtDataType(location = tp.location, name = tp.nameNode)))).
          filter(t => t._2.isDefined && !t._1.equals(t._2.get)). // not referring to itself
          foreach(t => {
            ctx.addResolutionError(ResolutionMessage(t._1.location, TemplateParamConflictsWithUdt)(None, List.empty, t._1.name, t._2.get.location))
          })

      _localAndFragIncludes.
        flatMap(f => f.resolvedType).
        filter(t => invalidInclude(t)).
        foreach(f => ctx.addResolutionError(new ResolutionMessage(location, IncludesOnlyTraits)(None, List.empty, f.versionedName.toString, f.versionedName.udtType)))

      _localAndFragExtends.
        flatMap(f => f.resolvedType).
        filter(t => !validExtends(t.udtType)).
        foreach(f =>
          ctx.addResolutionError(location, ExtendOnlySameType, this.udtType.toString, versionedName.toString, f.udtType, f.versionedName)
        )

      // ----------------- Fields check

      val allFieldsClosure = incsAndExtends.map(i => {
          val flds = localAndExtendsFields(ctx, i)
          flds
        }).flatten.
        filter(f => f.field.isDefined && !f.field.get.hasErrors).
        map(_.field.get)

      val allFieldsClosureMap = ListMap(allFieldsClosure.map(t => t.name -> t): _*)
      val allFieldsClosureMapLowercase = ListMap(allFieldsClosure.map(t => t.name.toLowerCase -> t): _*)

      val localFieldsMap = new mutable.LinkedHashMap[String, Field]()
      val localFieldsMapLowercase = new mutable.LinkedHashMap[String, Field]()

      _localAndFragFieldRefs.flatMap(e => e.field).map(f => {
        val fieldNameLC = f.name.toLowerCase

        if (allFieldsClosureMapLowercase.contains(fieldNameLC) &&
          !allFieldsClosureMapLowercase.get(fieldNameLC).get.dataType.isAssignableFrom(f.dataType)) {
          ctx.addResolutionError(ResolutionMessage(f.nameNode.location, DuplicateEntry)(None, List.empty, "field", f.name))
        }

        else if (localFieldsMapLowercase.contains(fieldNameLC)) {
          ctx.addResolutionError(ResolutionMessage(f.nameNode.location, DuplicateEntry)(None, List.empty, "field", f.nameNode.text))
        }

        else {
          localFieldsMap.put(f.name, f)
          localFieldsMapLowercase.put(fieldNameLC, f)
        }
      })

      // ----------------- Fields check end
      // ----------------- Asserts check start

      val allAssertsClosure = incsAndExtends.reverse.map(i => i.localAndFragAsserts).flatten.
        filter(f => !f.hasErrors)

      val allAssertsClosureMap = ListMap(allAssertsClosure.map(t => t.name -> t): _*)
      val allAssertsClosureMapLowercase = ListMap(allAssertsClosure.map(t => t.name.toLowerCase -> t): _*)

      val localAssertsMap = new mutable.LinkedHashMap[String, AssertDeclaration]()
      val localAssertsMapLowercase = new mutable.LinkedHashMap[String, AssertDeclaration]()

      _localAndFragAsserts.map(f => {
        val assertNameLC = f.name.toLowerCase

        if (allAssertsClosureMapLowercase.contains(assertNameLC)) {
          ctx.addResolutionError(ResolutionMessage(f.location, DuplicateEntry)(None, List.empty, "assert", f.name))
        }

        else if (localAssertsMapLowercase.contains(assertNameLC)) {
          ctx.addResolutionError(ResolutionMessage(f.location, DuplicateEntry)(None, List.empty, "assert", f.name))
        }

        else {
          localAssertsMap.put(f.name, f)
          localAssertsMapLowercase.put(assertNameLC, f)
        }
      })

      // ----------------- Asserts check end

      // ----------------- Linkages check start

      val allLinkagesClosure = incsAndExtends.reverse.map(i => i.localAndFragLinkages).flatten.
        filter(f => !f.hasErrors)

      val allLinkagesClosureMap = ListMap(allLinkagesClosure.map(t => t.name -> t): _*)
      val allLinkagesClosureMapLowercase = ListMap(allLinkagesClosure.map(t => t.name.toLowerCase -> t): _*)

      val localLinkagesMap = new mutable.LinkedHashMap[String, LinkageDeclaration]()
      val localLinkagesMapLowercase = new mutable.LinkedHashMap[String, LinkageDeclaration]()

      _localAndFragLinkages.map(f => {
        val linkageNameLC = f.name.toLowerCase

        if (allLinkagesClosureMapLowercase.contains(linkageNameLC)) {
          ctx.addResolutionError(ResolutionMessage(f.location, DuplicateEntry)(None, List.empty, "linkage", f.name))
        }

        else if (localLinkagesMapLowercase.contains(linkageNameLC)) {
          ctx.addResolutionError(ResolutionMessage(f.location, DuplicateEntry)(None, List.empty, "linkage", f.name))
        }

        else {
          localLinkagesMap.put(f.name, f)
          localLinkagesMapLowercase.put(linkageNameLC, f)
        }
      })

      // ----------------- Linkages check end



      _declaredFields = ListSet(localFieldsMap.keys.toSeq: _*)
      _allAccessibleFields = allFieldsClosureMap ++ localFieldsMap
      _allAccessibleAsserts = allAssertsClosureMap ++ localAssertsMap
      _allAccessibleLinkages = allLinkagesClosureMap ++ localLinkagesMap

      // Check includes dont result in duplicates where types dont match
      val grouped: Map[String, Seq[Field]] = allFieldsClosure.groupBy(_.name.toLowerCase)
      grouped.foreach(g => {
        if (g._2.size > 1) {
          val pairs = g._2.zip(g._2.tail)
          pairs.foreach(p => {
            if (!p._1.dataType.isAssignableFrom(p._2.dataType)) //  && !p._2.dataType.isAssignableFrom(p._1.dataType))
              ctx.addResolutionError(ResolutionMessage(location, IncludesCauseDuplicateEntry)(None, List.empty, p._1.name, p._1.dataType.toString, p._2.dataType.toString))
          })

          // if the includes duplicate field
          if (!localFieldsMapLowercase.contains(g._1)) {
            if (allFieldsClosure.filter(v => v.name.toLowerCase.equals(g._1)).groupBy(g => g.docs.mkString("")).size > 1)
              ctx.addResolutionWarning(ResolutionMessage(location, IncludesCauseFieldHiding)(None, List.empty, g._2.head.name, g._2.size))
          }
        }
      })

      if (typeParamsNode.isDefined)
        validateUnique(ctx, typeParamsNode.get.map(_.nodeId.asStringNode), "type parameter")

      // Are duplicate names specified
      validateUnique(ctx, _localAndFragAsserts.map(_.assertNameNode), "assert")
      validateUnique(ctx, _localAndFragLinkages.map(_.linkageNameNode), "linkage")
      validateUnique(ctx, _localAndFragNodeMeta.map(_.annotations).flatten.map(_.nameNode), "annotation")
      validateUnique(ctx, rawFieldRefs.map(_.nodeId.asStringNode), "field")
      validateUnique(ctx, rawIncludesNode.map(_.nodeId.asStringNode), "include")
      validateUnique(ctx, methodDecls.map(_.signature.nameNode), "function")

      val incAnnotations = incsAndExtends.map(i => {
        i.annotations
      }).flatten.toSet

      val incAnnNames = incAnnotations.map(_.versionedName.fullyQualifiedName).toSet
      annotations.foreach(a => {
        if (incAnnNames.contains(a.versionedName.fullyQualifiedName)) {
          //          ctx.addResolutionError(a, DuplicateAnnotationEntry, a.versionedName.fullyQualifiedName)
        }
      })

      val annFromNs = if (parent.isInstanceOf[NamespaceNode]) {
        val nsAnns = parent.asInstanceOf[NamespaceNode].annotations

        if (nsAnns.size > 0) {
          val applicableAnnotations = nsAnns.filter(a => {
            //              a.startPreResolve(ctx, parent)
            //              a.startResolve(ctx)

            val targets = a.resolvedAnnotationDeclType.get.asInstanceOf[AnnotationDecl].annotationTargets
            targets.contains(AnnotationTargetType.withEnumName(nodeType.toString.toLowerCase))
          })

          val annFromNs = applicableAnnotations.map(a => {
            a.createPartConcretizedTemplateableUdt(ctx, None, Map.empty)
          })

          annFromNs.foreach(a => a.startPreResolve(ctx, this))
          annFromNs
        }
        else
          Seq.empty
      }
      else
        Seq.empty

      _localAndParentalAnnotations =
      // filter out included annotations which redefined here
        incAnnotations.filter(a => annotations.filter(x => x.versionedName.equals(a.versionedName)).size == 0) ++
          annotations ++
          annFromNs
    }

    if (!isExtension) {
      ctx.registry.popImports()
    }

    ctx.registry.popTypeParameters(typeParamsNode)
  }

  private def localAndExtendsFields(ctx: Context, i: UdtBaseNode): Seq[FieldOrFieldRef] = {
    val flds = i.localAndFragFieldRefs

    val exFields: Seq[FieldOrFieldRef] =
      if (!i.isTrait) {
        val allFRefs = i.allFields.values.map(e => new FieldOrFieldRef(e)).toSeq
        allFRefs
      }
      else
        Seq.empty

    exFields ++ flds
  }

  private def validExtends(parent: UdtType): Boolean = {
    val child = this.udtType

    (parent, child) match {
      case (UdtType.`trait`, _) => false
      case (_, UdtType.`trait`) => false

      case (UdtType.key, UdtType.key) => true
      case (_, UdtType.key) => false
      case (UdtType.key, _) => false

      case (UdtType.union, UdtType.union) => true
      case (UdtType.union, UdtType.entity) => this.asInstanceOf[Entity].isEntityUnion
      case (UdtType.union, _) => false

      case (UdtType.entity, UdtType.entity) => true
      case (UdtType.record, UdtType.entity) => true

      case (UdtType.entity, UdtType.record) => false
      case (UdtType.record, UdtType.record) => true
    }
  }

  def asDataType: UdtDataType = {
    val udt = UdtDataType.fromUdtDataType(this)
    udt
  }

  //  protected def extendsDependencyClosure(ctx: Context, root: UdtBaseNode, onlyExt : Option[UdtDataType] ) : mutable.LinkedHashSet[UdtBaseNode] = {
  //    val deps : Seq[UdtDataType] = if ( onlyExt.isDefined ) {
  //      onlyExt.get.startPreResolve(ctx, parent)
  //      List(onlyExt.get)   // only extends to be resolved if specified
  //    }
  //    else if ( combinedExtendsNode.isDefined ) {
  //      combinedExtendsNode.get.startPreResolve(ctx, parent)
  //      List(combinedExtendsNode.get) ++ combinedLocalIncludes   // this is on an nested level below an extends, resolve everything
  //    }
  //    else
  //      combinedLocalIncludes
  //
  //    val incUdts = deps.map( i => {
  //      i.startPreResolve(ctx, parent )
  //      i.tmplResolvedOrResolvedType
  //    }).flatten
  //
  //    val collect = mutable.LinkedHashSet[UdtBaseNode]()
  //
  //    incUdts.foreach( t => {
  //      t.defragment(ctx)
  //
  //      if (root.equals(t))
  //        ctx.addResolutionError(new ResolutionMessage(location, CycleIncludesDetected)(versionedName.toString, collect.map(_.name).mkString(">")))
  //      else if (!collect.contains(t)) {
  //        collect += t
  //        collect ++= t.extendsDependencyClosure(ctx, root, None)
  //      }
  //    } )
  //    collect
  //  }

  //  private def extendsDependencyClosure(ctx: Context, root: UdtBaseNode): mutable.LinkedHashSet[UdtBaseNode] = {
  //    val deps = localAndFragExtends
  //
  //    val extendsUdts = deps.map(i => {
  //      i.startPreResolve(ctx, parent)
  //      i.tmplResolvedOrResolvedType
  //    }).flatten
  //
  //    val collect = mutable.LinkedHashSet[UdtBaseNode]()
  //    extendsUdts.foreach(t => {
  //      t.defragment(ctx)
  //
  //      if (root.equals(t))
  //        ctx.addResolutionError(new ResolutionMessage(location, CycleIncludesDetected)(List.empty, versionedName.toString, collect.map(_.name).mkString(">")))
  //      else if (!collect.contains(t)) {
  //        collect += t
  //        collect ++= t.extendsDependencyClosure(ctx, root)
  //      }
  //    })
  //    collect
  //  }

  private def extendsCycleCheck(ctx: Context, root: IUdtBaseNode, start: IUdtBaseNode): Unit = {

    if (start.extendsDef.isDefined && !start.extendsDef.get.asInstanceOf[UdtDataType].hasErrors) {
      val parent = start.extendsDef.get.udt

      if (root.equals(parent)) {
        ctx.addResolutionError(location, CycleExtendsDetected, root.name.fullyQualifiedName, start.name.fullyQualifiedName)
      }
      else {
        extendsCycleCheck(ctx, root, parent)
      }
    }
  }

  private def includesDependencyClosure(ctx: Context, visited: Set[String]): mutable.LinkedHashSet[UdtBaseNode] = {
    val deps = localAndFragIncludes

    val incUdts = deps.map(i => {
      i.startPreResolve(ctx, parent)
      i.tmplResolvedOrResolvedType
    }).flatten

    val collect = mutable.LinkedHashSet[UdtBaseNode]()
    incUdts.foreach(t => {
      t.defragment(ctx)

      if (visited.contains(t.name.fullyQualifiedName))
        ctx.addResolutionError(new ResolutionMessage(name.location, CycleIncludesDetected)(None, List.empty, versionedName.toString, visited.mkString("", " > ", " > ") + t.name.fullyQualifiedName))
      else if (!collect.contains(t)) {
        collect += t
        collect ++= t.includesDependencyClosure(ctx, visited ++ Set(t.name.fullyQualifiedName))
      }
    })
    collect
  }

  override def toString: String = {
    val sb = new StringBuilder

    sb ++= annotationNodes.mkString("", "\n", "")

    if (docs.size > 0)
      sb ++= docs.map("  " + _.toString).mkString("\n/#\n  ", "\n  ", "\n #/\n")


    var mods = modifiersNode.map(m => m.modifier.toString.toLowerCase()).mkString("", " ", " ")
    if (mods.trim.isBlank)
      mods = ""

    val v = if (versionNo.isDefined) " @ " + versionNo.get.number.get.toString else ""

    sb ++= "\n" + mods + name.udtType.toString.toLowerCase + " " +
      versionedName.fullyQualifiedName + TextUtils.mkString(typeParamsNode) + v

    sb ++= toStringIncludesAndBody

    sb.toString()
  }

  protected def toStringBeforeBody() = {
    ""
  }

  protected def toStringIncludesAndBody(): String = {
    val sb = new StringBuilder
    if (rawExtendedNode.isDefined) {
      sb ++= " extends " + rawExtendedNode.get.name.text
    }

    if (includes.size > 0) {
      sb ++= " includes "
      sb ++= includes.mkString("", ", ", "")
    }

    sb ++= toStringBeforeBody()

    sb ++= " {\n"

    sb ++= fields.mkString("", "\n", "").replaceAll("^", "  ").replaceAll("\n", "\n  ")

    //    fields.foreach(f => {
    //      sb ++= "\n" + f
    //    })

    rawMethodDeclNodes.foreach(f => {
      sb ++= "\n" + indent(f.toString, "    ")
    })


    allLinkages.foreach(f => {
      sb ++= "\n" + f
    })


    localAndFragAsserts.foreach(f => {
      sb ++= "\n" + indent(f.toString, "  ")
    })

    sb ++= "\n}\n"

    sb.toString()
  }


  protected def templateInstantiate(target: Option[UdtDataType], resolveCtx: Context, templateArgs: Map[String, DataType]): Option[UdtDataType] = {
    if (target.isDefined)
      Some(target.get.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[UdtDataType])
    else
      None
  }

  /**
   * Includes and Extends are template instantiated, fields should be left-as-is - i.e. templates if they are.
   */
  protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode

  def getAsTemplatableUdtAndTemplateInstantiated(resolveCtx: Context, typeArguments: Map[String, DataType]): UdtBaseNode = {
    val key = typeArguments.map(_._2).toSeq
    val existing = templated.get(key)

    if (existing.isDefined)
      existing.get
    else {

      val params = filterParamsFromArgs(typeArguments)

      val c = createPartConcretizedTemplateableUdt(resolveCtx, params, typeArguments) //.templateInstantiated

      c.startPreResolve(resolveCtx, this.parent)

      templated.put(key, c)

      // The templated of what is created also needs know the different templated usages of this Udt
      // Given Pair<L,R>, templated will hold all unique template usages of Pair<string,int>, Pair<double, double>, ...
      c.templated = templated
      c
    }
  }

  def assertTrue[T](b: Boolean) =
    if (!b)
      throw new com.schemarise.alfa.compiler.AlfaInternalException("Invalid internal condition")

  private def filterParamsFromArgs(tas: Map[String, DataType]) = {
    val p1 = tas.values.filter(e =>
        e.unwrapTypedef.
          isInstanceOf[TypeParameterDataType]).
      map(_.unwrapTypedef.
        asInstanceOf[TypeParameterDataType].tp)

    if (p1.size == 0)
      None
    else
      Some(p1.toSeq)
  }

  def isTemplated: Boolean = templated.size > 0

  def templateInstatiations: Seq[IUdtBaseNode] = {
    val l = templated.map(e => {
      val ti = e._2 // .templateInstantiated
      ti.startPreResolve(this.ctx.get, parent)
      ti
    }).toSeq

    l
  }

  override def isUnmodifiedAssignableFrom(other: IAssignable): Boolean = {
    other match {
      // TODO should check Trait inheritence
      case rhs: UdtBaseNode =>
        // TODO data : Pair< int, string > = { l = 1021.123, r = "abc" }. Pair<L,R> == Pair ?
        if (rhs.name.name.equals(name.name))
          true
        else if (rhs.includes.size > 0) {
          rhs.includes.filter(i => isAssignableFrom(i.udt.asInstanceOf[UdtBaseNode])).headOption.isDefined
        }
        else if (rhs.extendsDef.isDefined) {
          val e = rhs.extendsDef.get
          val res = isUnmodifiedAssignableFrom(e.udt.asInstanceOf[UdtBaseNode])
          res
        }
        else
          false

      case _ => false
    }
  }

  private var _fieldMetaAnnotation: Option[Map[Expression, Expression]] = None

  private var sig: Option[String] = None

  override def modelId(): Option[String] = {
    locateCompUnitParent().modelVersion.map(_.text)
  }

  def checksum() = {
    if (sig.isEmpty) {
      val all = _checksum(false)
      var mand = _checksum(true)
      if (mand.equals(all))
        mand = ""

      sig = Some(all + ":" + mand)
    }
    sig.get

  }


  private def _checksum(onlyMandatory: Boolean): String = {
    var m = new mutable.HashMap[String, String]
    __checksum(m, onlyMandatory)
    m.get(name.fullyQualifiedName).get
  }

  private def __checksum(visited: mutable.HashMap[String, String], onlyMandatory: Boolean): Unit = {
    if (visited.contains(name.fullyQualifiedName))
      return

    // put dummy to prevent stack overflow for recursive types

    visited.put(name.fullyQualifiedName, "")

    val fields = checksumCalcString(visited, onlyMandatory)

    val fullcrc = CrcUtils.crc(fields)

    // replace dummy at end
    visited.put(name.fullyQualifiedName, fullcrc)
  }

  def checksumCalcString(singleLine: Boolean, onlyMandatory: Boolean): String = {
    var m = new mutable.HashMap[String, String]
    var str = checksumCalcString(m, onlyMandatory)

    if (singleLine)
      str.replace("\n", "").replace("\r", "")
    else
      str
  }


  private def checksumCalcString(visited: mutable.HashMap[String, String], onlyMandatory: Boolean): String = {
    val buf = new ListBuffer[String]()

    // name first
    buf += name.fullyQualifiedNameAndVersion + "{\n"

    traverse(new NoOpNodeVisitor() {
      override def enter(e: IField): Mode = {
        if (onlyMandatory && (e.dataType.isEncOptional() || e.expression.isDefined)) {
          // if optional or expression defined for the field, it is regarded 'optional' as it wont break serialization
          NodeVisitMode.Break
        }
        else {
          val n = e.name
          buf += n + ":"
          e.dataType.traverse(this)

          if (e.expression.isDefined) {
            buf += "=" + e.expression.get.toString
          }

          buf += ";\n"
          NodeVisitMode.Break
        }
      }

      def enterUdt(e: IUdtBaseNode): Mode = {
        val fnames = e.allFields.keys.toList.sorted

        fnames.foreach(fn => {
          val f = e.allFields.get(fn).get
          f.traverse(this)
        })
        NodeVisitMode.Break
      }

      override def enter(e: IRecord): Mode = {
        enterUdt(e)
      }

      override def enter(e: IEntity): Mode = {
        enterUdt(e)
      }

      override def enter(e: IUnion): Mode = {
        enterUdt(e)
      }

      override def enter(e: IKey): Mode = {
        enterUdt(e)
      }

      override def enter(e: ITrait): Mode = {
        enterUdt(e)
      }

      override def enter(e: IAnnotationDecl): Mode = {
        enterUdt(e)
      }

      override def enter(e: IEnum): Mode = {
        buf += e.allFields.values.mkString(",")
        NodeVisitMode.Break
      }

      override def enter(e: IScalarDataType): Mode = {
        buf += e.scalarType.toString
        NodeVisitMode.Break
      }

      override def enter(e: IEnumDataType): Mode = {
        buf += "enum<"
        e.syntheticEnum.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: ITupleDataType): Mode = {
        buf += "tuple<"
        e.syntheticRecord.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: IListDataType): Mode = {
        buf += "list<"
        e.componentType.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: IMapDataType): Mode = {
        buf += "map<"
        e.keyType.traverse(this)
        buf += ","
        e.keyType.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: ISetDataType): Mode = {
        buf += "set<"
        e.componentType.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: IUnionDataType): Mode = {
        buf += "union<"
        e.syntheticUnion.traverse(this)
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: IEnclosingDataType): Mode = {
        buf += e.encType.toString + "<"
        e.parameterizedTypes.foreach(t => {
          t.traverse(this)
          buf += ";"
        })
        buf += ">"
        NodeVisitMode.Break
      }

      override def enter(e: IUdtDataType): Mode = {
        val udt = e.udt.asInstanceOf[UdtBaseNode]

        // sigs of types referenced
        udt.__checksum(visited, onlyMandatory)
        buf += udt.name.fullyQualifiedNameAndVersion + "[" + visited.get(udt.name.fullyQualifiedName).get + "]"
        NodeVisitMode.Break
      }
    })

    buf += "}"

    val preSig = buf.mkString
    preSig
  }
}
