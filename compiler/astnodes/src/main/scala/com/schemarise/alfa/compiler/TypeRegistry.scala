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
package com.schemarise.alfa.compiler

import java.util.concurrent.atomic.AtomicInteger
import com.schemarise.alfa.compiler.antlr.AlfaParser.ExpressionUnitContext
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.err._
import com.schemarise.alfa.compiler.tools.repo.ArtifactEntry
import com.schemarise.alfa.compiler.utils.{BuiltinModelTypes, TokenImpl}

import scala.collection.JavaConverters._
import scala.collection.mutable.{MultiMap, _}

class TypeRegistry(context: Context) {

  /**
   * Unfragmented declarations of a UDT by fully-qualified name and version
   */
  private val udtsByVersionedName = HashMap[UdtVersionedName, UdtBaseNode]()

  /**
   * UDT by fully-qualified name
   */
  private val udtByName = HashMap[LocatableString, UdtBaseNode]()

  private val currentImports = new java.util.Stack[scala.collection.immutable.Map[String, UdtVersionedName]]()

  private val currentConsts = new java.util.Stack[scala.collection.immutable.Map[StringNode, ExpressionUnitContext]]()


  /**
   * Only UDT parts declared as fragment
   */
  val udtFragmentsOnly = new HashMap[UdtVersionedName, ArrayBuffer[UdtBaseNode]]
  //    with MultiMap[UdtVersionedName, UdtBaseNode]

  /**
   * Only UDT parts declared as fragment
   */
  val udtsFromRepositories = new HashMap[ArtifactEntry, ArrayBuffer[UdtBaseNode]]
  //    with MultiMap[ArtifactEntry, UdtBaseNode]

  /**
   * Template types for lookup
   */
  private val templatedUdt = HashMap[LocatableString, UdtBaseNode]()

  private val currentTypeParameters = new java.util.Stack[scala.Seq[TypeParameter]]()

  private val currentCallStack = new java.util.Stack[CallStackFrame]()

  private val services = Map[UdtVersionedName, Service]()
  private val libraries = Map[UdtVersionedName, Library]()
  private val dataproducts = Map[UdtVersionedName, Dataproduct]()

  private val testcases = Map[UdtVersionedName, Testcase]()
  private val annotations = Map[UdtVersionedName, AnnotationDecl]()
  private val extensionDecls = Map[UdtVersionedName, ExtensionDecl]()
  private val extensions = Map[UdtVersionedName, Extension]()

  private val allFields = Map[String, Field]()

  private val allTypeDefs = new ListBuffer[TypeDefedDataType]()
  private val allTypeDefsByName = Map[LocatableString, DataType]()
  private val templatedTypeDefsByName = Map[LocatableString, UdtName]()

  private val transformers = Map[IMethodSignature, Transformer]()

  private val LimitMsg = " evaluation limit reached"

  private val allNamespaces = new HashMap[INamespaceNode, Set[NamespaceNode]] with MultiMap[INamespaceNode, NamespaceNode]

  private val assertCounter = new AtomicInteger(0)

  def fields() = allFields.values.toList

  def typedefs() = allTypeDefs.toList

  def registerNamespace(node: NamespaceNode): Unit =
    allNamespaces.addBinding(Namespace(node.nameNode.text), node)

  def registerNamespace(node: Namespace): Unit =
    allNamespaces.addBinding(node, NamespaceNode(nameNode = node.nodeId.asStringNode, isSynthetic = true))

  def allUserDeclarations = {
    val d = udtByName.filter(e =>
        !e._2.isSynthetic && BuiltinModelTypes.DoesNotInclude(e._2.name.fullyQualifiedName)).
      keys.map(e => e.name).toArray

    d
  }

  def getDataproduct(vn: UdtVersionedName, errorIfNotExists: Boolean = true): Option[Dataproduct] = {
    val dp = dataproducts.get(vn)
    if (dp.isEmpty) {
      context.addResolutionError(vn.location, UnknownDataproduct, vn.fullyQualifiedName)
    }

    dp
  }

  def getDataproducts() = {
    dataproducts
  }

  def getAllNamespaces() = {
    allNamespaces.keySet.toSeq
  }

  def getNamespaceMeta(node: INamespaceNode): Option[NodeMeta] = {
    val d = allNamespaces.get(node)

    if (d.isDefined) {
      val defs = d.get

      if (defs.size == 1) Some(defs.head.meta)
      else {
        val docs = defs.map(e => e.meta.docs).flatten
        val anns = defs.map(e => e.meta.annotations).flatten
        Some(NodeMeta(anns.toSeq, docs.toSeq))
      }
    }
    else
      None
  }

  def containsNamespace(node: Namespace): Boolean = allNamespaces.contains(node)

  def register(td: TypeDefedDataType): Unit = {
    val ls = LocatableString(td.newType.text)(td.newType.location)
    val un = UdtName(td.newType.text, None)(td.location, td.typeParams, None)

    safePut(allTypeDefsByName, ls, td.referencedType)

    if (td.typeParams.size > 0)
      safePut(templatedTypeDefsByName, ls, un)

    allTypeDefs += td
  }

  case class LocatableString(name: String)(iToken: IToken) extends Locatable {
    override val location: IToken = iToken

    override def toString: String = name
  }

  private def safePut[K <: Locatable, V <: Locatable](m: Map[K, V], k: K, v: V, warnOnDup: Boolean = false): Boolean = {
    if (!mapKeyExists(m, k, v, warnOnDup)) {
      m.put(k, v)
      true
    }
    else {
      false
    }
  }

  private def mapKeyExists[K <: Locatable, V <: Locatable](m: Map[K, V], k: K, v: V, warnOnDup: Boolean = false): Boolean = {
    val existing = m.get(k)
    if (existing.isDefined) {
      if (existing.get.equals(v) && warnOnDup)
        context.addResolutionWarning(ResolutionMessage(k.location, AlreadyDeclared)(None, List.empty, k, k.location, existing.get.location))
      else
        context.addResolutionError(ResolutionMessage(k.location, AlreadyDeclared)(None, List.empty, k, k.location, existing.get.location))
      true
    }
    else
      false
  }

  def registerFields(fs: Fields): Unit = {
    fs.fields.
      filter(f => allFields.contains(f.nameNode.text)).
      map(f =>
        context.addResolutionError(
          ResolutionMessage(f.location, AlreadyDeclared)(None, List.empty, f, f.location, allFields.get(f.nameNode.text))))

    fs.fields.
      filter(f => !allFields.contains(f.nameNode.text)).
      map(f => allFields.put(f.nameNode.text, f))
  }

  def getTransformersTo(o: UdtBaseNode): List[Transformer] = {
    val s = transformers.filter(e => {
      val rt = e._1.returnType
      if (rt.isUdt)
        rt.isAssignableFrom(o.asDataType)
      else if (rt.isList())
        rt.asInstanceOf[ListDataType].componentType.isAssignableFrom(o.asDataType)
      else if (rt.isEncTry())
        rt.asInstanceOf[EnclosingDataType].componentType.isAssignableFrom(o.asDataType)
      else
        false
    }).map(_._2).toList
    s
  }


  def getTransformers() = {
    transformers.values.toList
  }

  def getTransformer(ms: MethodSignature) = {
    transformers.get(ms)
  }

  def registerTransformer(t: Transformer) = {
    val tn = transformers.get(t.getTransformerSignature())

    if (tn.isDefined) {
      context.addResolutionError(t.location, TransformerExists, tn.get.location)
    }
    else
      transformers.put(t.getTransformerSignature, t)
  }

  def registerUdt[T <: UdtBaseNode](r: T): T = {
    var continue = true

    if (r.name.fullyQualifiedName.size == 0)
      throw new AlfaInternalException("Blank named UDT")

    if (r.isFragment) {
      if (!udtFragmentsOnly.contains(r.versionedName))
        udtFragmentsOnly.put(r.versionedName, new ArrayBuffer[UdtBaseNode]())
      udtFragmentsOnly.get(r.versionedName).get += r
    }
    else {
      // dont register to namespace if a fragment. Only non-fragments will be registered to namespace
      registerNamespace(r.versionedName.namespace)

      continue = safePut(udtsByVersionedName, r.versionedName, r)

      // unversioned/unfragmented name should be unique
      if (continue && r.versionedName.versionNode.isEmpty) {
        val altKey = LocatableString(r.versionedName.fullyQualifiedName)(r.versionedName.location)
        continue = safePut(udtByName, altKey, r)

        if (continue && r.versionedName.typeParametersNode.isDefined) {
          continue = safePut(templatedUdt, altKey, r)
        }
      }
    }

    if (continue && r.isInstanceOf[AnnotationDecl]) {
      val a = r.asInstanceOf[AnnotationDecl]
      continue = safePut(annotations, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[Service] && !r.isFragment) {
      val a = r.asInstanceOf[Service]
      continue = safePut(services, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[Dataproduct]) {
      val a = r.asInstanceOf[Dataproduct]
      continue = safePut(dataproducts, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[Library]) {
      val a = r.asInstanceOf[Library]
      continue = safePut(libraries, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[Testcase]) {
      val a = r.asInstanceOf[Testcase]
      continue = safePut(testcases, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[ExtensionDecl]) {
      val a = r.asInstanceOf[ExtensionDecl]
      continue = safePut(extensionDecls, a.versionedName, a)
    }
    else if (continue && r.isInstanceOf[Extension]) {
      val a = r.asInstanceOf[Extension]
      continue = safePut(extensions, a.versionedName, a)
    }

    val repo = context.currentRepositoryEntry

    if (repo.isDefined) {
      udtsFromRepositories.put(repo.get, new ArrayBuffer[UdtBaseNode]())
      udtsFromRepositories.get(repo.get).get += r
    }

    r
  }

  def getField(fn: StringNode): Option[Field] = {
    val obj: Option[Field] = allFields.get(fn.text)
    if (obj.isDefined)
      Some(obj.get)
    else {
      val m = new ResolutionMessage(fn.location, GlobalFieldNotFound)(None, List.empty, fn.text)
      context.addResolutionError(m)
      None
    }
  }

  def getUdtVersionNames(): scala.collection.immutable.Set[IUdtVersionName] = {
    udtsByVersionedName.keys.toSet[IUdtVersionName]
  }

  def getUdtByVersionedName(u: IUdtVersionName) = {
    udtsByVersionedName.get(u.asInstanceOf[UdtVersionedName])
  }

  def getUdt(ref: UdtDataType): Option[UdtBaseNode] =
    getUdt(None, ref, true)

  def getTypeDef(ctx: Context, requestor: ResolvableNode, ref: UdtDataType): Option[DataType] = {
    val ks = ref.name.text
    val k = LocatableString(ks)(ref.location)
    val td = allTypeDefsByName.get(k)
    val tmplTd = templatedTypeDefsByName.get(k)

    if (td.isDefined) {
      if (tmplTd.isDefined) {
        if (!ref.typeArgumentsNode.isDefined) {
          val m = new ResolutionMessage(ref.location, TemplatingError)(None, List.empty, "Missing template parameter")
          context.addResolutionError(m)
          requestor.addError(m)
          None
        }

        if (tmplTd.get.typeParameters.get.size != ref.typeArgumentsNode.get.size) {
          val m = new ResolutionMessage(ref.location, TemplatingError)(None, List.empty, "Invalid number of type arguments")
          context.addResolutionError(m)
          requestor.addError(m)
          None
        }

        val typeArgs = ref.typeArgumentsNode.get
        val typeParms = tmplTd.get.typeParameters.get

        val m: scala.collection.immutable.Map[String, DataType] =
          typeParms.zip(typeArgs).
            groupBy(_._1.nameNode.text).
            map { case (k, v) => (k.toString, v.head._2) }

        val res = td.get.unwrapTypedef.templateInstantiate(ctx, m).asInstanceOf[DataType]
        Some(res)
      }
      else
        td
    }
    else if (getTypeParam(ks).isDefined) {
      val tp = getTypeParam(ks).get
      Some(tp.asDataType)
    }
    else
      None

  }

  def getFragments(requestor: Option[ResolvableNode], ref: UdtDataType): Seq[UdtBaseNode] = {

    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
      udtFragmentsOnly.get(n)
    }).filter(_.isDefined).headOption

    if (obj.isDefined && obj.get.isDefined)
      obj.get.get
    else
      Seq.empty
  }

  def getAnnotation(requestor: Option[ResolvableNode], ref: UdtDataType): Option[AnnotationDecl] = {
    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
      // context.logger.trace("Searching annotations for " + n.fullyQualifiedName)
      annotations.get(n)
    }).filter(_.isDefined).headOption

    val mi = getMatchingImport(ref)

    if (obj.isDefined)
      obj.get
    else if (mi.isDefined) {
      val vn = UdtVersionedName.apply(name = StringNode.create(mi.get.fullyQualifiedName))
      annotations.get(vn)
    }
    else {
      val l = context.registry.matchingUdts(ref, Some(UdtType.annotation))
      val m = new ResolutionMessage(ref.location, UnknownAnnotation)(None, l, ref.toString)
      context.addResolutionError(m)

      if (requestor.isDefined)
        requestor.get.addError(m)

      None
    }
  }

  def getExtension(requestor: Option[ResolvableNode], ref: UdtDataType): Option[ExtensionDecl] = {
    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
      // context.logger.trace("Searching extensions for " + n.fullyQualifiedName)
      extensionDecls.get(n)
    }).filter(_.isDefined).headOption

    if (obj.isDefined)
      obj.get
    else {
      val m = new ResolutionMessage(ref.location, UnknownExtension)(None, List.empty, ref.toString)
      context.addResolutionError(m)

      if (requestor.isDefined)
        requestor.get.addError(m)

      None
    }
  }

  def getUdt(requestor: Option[ResolvableNode], ref: UdtDataType): Option[UdtBaseNode] =
    getUdt(requestor, ref, true)

  private def typeArgsCompatible(udt: UdtBaseNode, ref: UdtDataType): Boolean = {
    val taMatch = if (udt.typeParamsNode.isEmpty) {
      ref.typeArguments.isEmpty
    } else if (ref.typeArguments.isEmpty) {
      false
    }
    else {
      udt.typeParamsNode.get.size == ref.typeArguments.get.size
    }

    if (!taMatch) {
      val m = new ResolutionMessage(ref.location, TypeArgsMismatchWarning)(None, List.empty, ref.toString)
      context.addResolutionWarning(m)
    }
    taMatch
  }

  def getLibrary(requestor: Option[ResolvableNode], ref: UdtDataType): Option[Library] = {
    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
        // context.logger.trace("Searching UdtByVersion for " + n.fullyQualifiedName)
        libraries.get(n)
      }).
      filter(_.isDefined).
      filter(udto => typeArgsCompatible(udto.get, ref)).
      headOption

    if (obj.isDefined)
      obj.get
    else
      None
  }

  def getService(requestor: Option[ResolvableNode], ref: UdtDataType): Option[Service] = {
    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
        // context.logger.trace("Searching UdtByVersion for " + n.fullyQualifiedName)
        services.get(n)
      }).
      filter(_.isDefined).
      filter(udto => typeArgsCompatible(udto.get, ref)).
      headOption

    if (obj.isDefined)
      obj.get
    else
      None
  }

  def matchingUdts(ref: UdtDataType, udtType: Option[UdtType]): List[String] = {
    // do with both full name (including namespace) and name only
    _matchingUdts(ref, udtType) ++ _matchingUdts(UdtDataType.fromName(ref.name.text), udtType)
  }

  private def _matchingUdts(ref: UdtDataType, udtType: Option[UdtType]): List[String] = {

    def matchName(vn: UdtVersionedName): Boolean = {
      if (udtType.isEmpty)
        vn.fullyQualifiedName.contains(ref.fullyQualifiedName)
      else
        udtType.get == vn.udtType && vn.fullyQualifiedName.contains(ref.fullyQualifiedName)
    }

    val udts = udtsByVersionedName.keySet.filter(e => matchName(e)).map(_.fullyQualifiedName).toList

    val imps: List[String] =
      if (currentImports.empty())
        List.empty
      else {
        val im = currentImports.peek()
        im.values.filter(e => matchName(e)).map(_.fullyQualifiedName).toList
      }

    udts ++ imps
  }

  def getUdt(requestor: Option[ResolvableNode], ref: UdtDataType, logError: Boolean, allowNameOnlyMatch: Boolean = false): Option[UdtBaseNode] = {

    //    if ( logError && ref.location.getStartLine < 0)
    //      throw new AlfaInternalException("Get UDT with empty location")

    val obj = ref.getPossibleUdtVersionedNames().view.map(n => {
        // context.logger.trace("Searching UdtByVersion for " + n.fullyQualifiedName)
        udtsByVersionedName.get(n)
      }).
      filter(_.isDefined).
      filter(udto => typeArgsCompatible(udto.get, ref)).
      headOption

    if (obj.isDefined)
      obj.get
    else {
      val tobj: Option[(UdtVersionedName, Option[UdtBaseNode])] = ref.getPossibleUdtVersionedNames().view.map(n => {
          // context.logger.trace("Searching UdtTemplates for " + n.fullyQualifiedName)
          (n, templatedUdt.get(LocatableString(n.fullyQualifiedName)(n.location)))
        }
        ).filter(_._2.isDefined).
        filter(udto => typeArgsCompatible(udto._2.get, ref)).
        headOption

      if (tobj.isDefined) {
        tobj.get._2
        // templateInstantiate( tobj.get._2.get, tobj.get._1 ) BEBU
      }
      else {
        // context.logger.trace("Searching TypeParameters for " + ref.name)
        val typeParam = getTypeParam(ref.name.text)

        val ls = LocatableString(ref.fullyQualifiedName)(TokenImpl.empty)

        val imported = getMatchingImport(ref)

        if (typeParam.isDefined) {
          typeParam
        }
        else if (imported.isDefined) {
          getUdt(requestor, imported.get, logError)
        }
        // if looking for a specific version we shouldnt pick by name
        else if (allowNameOnlyMatch && udtByName.contains(ls)) {
          udtByName.get(ls)
        }
        else {

          //          Lazy mode, find types from dependencies, but no need now as context loads all // TODO should this be configurable?

          //          // context.logger.trace("Searching dependencies for " + ref.udtName.name )
          //          val extDef : Option[ UdtBaseNode ] = context.lookupUdtInDependencies( requestor, ref, logError )
          //
          //          if ( extDef.isDefined ) {
          //            extDef
          //          } else

          if (logError) {
            val l = matchingUdts(ref, None)
            val m = new ResolutionMessage(ref.location, UnknownType)(None, l, ref.toString)
            context.addResolutionError(m)

            if (requestor.isDefined)
              requestor.get.addError(m)

            None
          }
          else
            None
        }
      }
    }
  }

  def pushTypeParameters(typeParams: Option[scala.Seq[TypeParameter]]): Unit = {
    if (typeParams.isDefined) {
      typeParams.get.foreach(e => {
        if (currentTypeParameters.contains(e.nameNode.text)) {
          context.addResolutionError(ResolutionMessage(e.location, DuplicateEntry)(None, List.empty, "type parameter", e.nameNode.text))
        }
      })

      currentTypeParameters.push(typeParams.get)
    }
  }

  def popTypeParameters(typeParams: Option[scala.Seq[TypeParameter]]): Unit = {
    if (typeParams.isDefined)
      currentTypeParameters.pop()
  }

  private def getTypeParam(n: String): Option[TypeParameter] = {
    val p1 = currentTypeParameters.asScala.reverse.
      map(e => e.filter(f => f.name.name.equals(n)))

    val p2 = p1.filter(_.size == 1).flatten.headOption
    p2
  }

  def pushLambdaSignature(ctx: Context, formals: scala.collection.Seq[(StringNode, DataType)]): Unit = {
    val sf = new CallStackFrame(false)

    val currIdentifiers = if (currentCallStack.empty()) Nil else currentCallStack.peek().allIdentifiers
    currentCallStack.push(sf)

    currIdentifiers.foreach(f => pushAccessibleIdentifier(ctx, f._1, f._2))
    formals.foreach(f => pushAccessibleIdentifier(ctx, f._1, f._2))
  }

  //  def pushAssert( ctx : Context, assertDeclaration: AssertDeclaration) : Unit = {
  //    val sf = new CallStackFrame()
  //    currentCallStack.push(sf)
  //    val udt = assertDeclaration.parentUdt()
  //    udt.allAccessibleFields().foreach( f => pushAccessibleIdentifier( ctx, f._2.nameNode, f._2.dataType ) )
  //  }

  def pushUdtFields(ctx: Context, udt: UdtBaseNode): Unit = {
    val sf = new CallStackFrame(true)
    currentCallStack.push(sf)
    if (udt.isEntity) {
      val e = udt.asInstanceOf[Entity]
      if (e.key.isDefined) {
        pushAccessibleIdentifier(ctx, StringNode.create(Expression.DollarKey), e.key.get.asDataType)
      }
    }
    if (!udt.hasErrors) {
      udt.allAccessibleFields().foreach(f => pushAccessibleIdentifier(ctx, f._2.nameNode, f._2.dataType))
    }
  }

  //  def popAssert() :Unit  = {
  //    currentCallStack.pop()
  //  }

  def pushBlock(): Unit = {
    val sf = new CallStackFrame(false)
    currentCallStack.push(sf)
  }

  def peekCurrentCallStackFrame() = {
    currentCallStack.peek().allIdentifiers.toMap
  }

  def popBlock(): Unit = {
    currentCallStack.pop()
  }

  def popUdtFields(): Unit = {
    currentCallStack.pop()
  }

  def pushMethodSignature(ctx: Context, sig: MethodSignature, udt: UdtBaseNode): Unit = {
    val sf = new CallStackFrame(false)
    currentCallStack.push(sf)

    // push all fields and method args as identifiers
    sig.formals.foreach(f => pushAccessibleIdentifier(ctx, f._2.nameNode, f._2.dataType))
    udt.allAccessibleFields().foreach(f => pushAccessibleIdentifier(ctx, f._2.nameNode, f._2.dataType))
    // udt.allAccessibleMethods().foreach( f => pushAccessibleIdentifier(ctx, f._1, f._2.signature.returnType))
  }

  def popMethodSignature(): Unit = {
    currentCallStack.pop()
  }

  def popLambdaSignature(): Unit = {
    currentCallStack.pop()
  }

  def getCurrentImportNames() = {
    if (!currentImports.empty() && !currentImports.peek().isEmpty) {
      currentImports.peek().map(e => e._1).toList.sorted
    }
    else
      List.empty
  }

  def getCurrentImports() = {
    if (!currentImports.empty() && !currentImports.peek().isEmpty) {
      currentImports.peek().map(e => e._2).toList
    }
    else
      List.empty
  }

  def getCurrentConsts(): scala.collection.immutable.Map[StringNode, ExpressionUnitContext] = {
    if (!currentConsts.empty() && !currentConsts.peek().isEmpty) {
      val d: scala.collection.immutable.Map[StringNode, ExpressionUnitContext] = currentConsts.peek()
      d
    }
    else
      scala.collection.immutable.Map.empty
  }

  def getConst(sn: StringNode): Option[ExpressionUnitContext] = {
    if (!currentConsts.empty() && !currentConsts.peek().isEmpty) {
      currentConsts.peek().get(sn)
    }
    else
      None
  }

  def pushAccessibleIdentifier(ctx: Context, name: StringNode, t: IDataType): Unit = {
    val sf = currentCallStack.peek()
    sf.pushAccessibleIdentifier(ctx, name, t)
  }

  def popAccessibleIdentifier(name: StringNode): Unit = {
    val sf = currentCallStack.peek()
    sf.popAccessibleIdentifier(name)
  }

  //  def clearAccessibleIdentifiers() : Unit = {
  //    val sf = currentCallStack.peek()
  //    sf.clearAccessibleIdentifiers()
  //  }


  def accessibleIdentifiers(): List[String] = {
    if (currentCallStack.isEmpty)
      List.empty
    else {
      currentCallStack.asScala.map(e => e.allIdentifiers.map(_._1.text)).flatten.toList
    }
  }


  def getIdentifierDeclaration(name: String): Option[(StringNode, IDataType, Boolean)] = {
    if (currentCallStack.isEmpty) None
    else {
      val blocks = currentCallStack.elements()
      while (blocks.hasMoreElements) {
        val block = blocks.nextElement()
        val id = block.getIdentifierDeclaration(name)

        if (id.isDefined)
          return Some(id.get._1, id.get._2, block.isUdtStack)
      }

      None
    }
  }

  private class CallStackFrame(val isUdtStack: Boolean) {
    private val accessibleVars = HashMap[String, (StringNode, IDataType)]()

    def pushAccessibleIdentifier(ctx: Context, name: StringNode, t: IDataType): Unit = {
      val id = accessibleVars.get(name.text)
      if (id.isDefined) {
        val sf = currentCallStack.peek()
        ctx.addResolutionError(ResolutionMessage(name.location, IdentifierAlreadyDeclared)(None, List.empty, name, id.get._1.location))
      }
      else
        accessibleVars.put(name.text, (name, t))
    }

    def allIdentifiers = accessibleVars.values

    def popAccessibleIdentifier(name: StringNode): Unit = {
      accessibleVars.remove(name.text)
    }

    //    def clearAccessibleIdentifiers(): Unit = {
    //      accessibleVars.clear()
    //    }

    def getIdentifierDeclaration(name: String): Option[(StringNode, IDataType)] = {
      accessibleVars.get(name)
    }
  }

  private def getMatchingImport(ref: UdtDataType): Option[UdtDataType] = {
    val fullNameFound = _getMatchingImport(UdtDataType.fromName(ref.fullyQualifiedName))
    if (fullNameFound.isDefined)
      fullNameFound
    else
      _getMatchingImport(ref)
  }

  private def _getMatchingImport(ref: UdtDataType): Option[UdtDataType] = {

    val node = ref.name

    if (currentImports.empty() || node.text.length == 0)
      None
    else {
      val im = currentImports.peek()
      val matched = im.filter(x =>
        x._2.fullyQualifiedName.endsWith("." + node.text)
      ).headOption

      if (matched.isDefined)
        Some(matched.get._2.asUdtDataType.asInstanceOf[UdtDataType])
      else
        None
    }
  }

  def pushImports(ctx: Context, imports: scala.Seq[ImportDef]): Unit = {

    val all = imports.map(e => {
      val importString = e.name.text

      val matchingTypes = udtsByVersionedName.keySet.filter(k => {
        val inAll = e.hasWildcard && k.fullyQualifiedName.startsWith(importString)
        val exact = !e.hasWildcard && k.fullyQualifiedName.equals(importString)
        inAll || exact
      })

      if (matchingTypes.size == 0)
        ctx.addResolutionWarning(e.name.location, NoMatchingTypesForImport, e.name.text)

      //      matchingTypes.map( e => {
      //        val k = if ( e.fullyQualifiedName.equals(importString))
      //          e.fullyQualifiedName
      //        else
      //          e.fullyQualifiedName.substring(importString.length + 1)
      //
      //        k -> e
      //      } )
      matchingTypes.map(e => e.fullyQualifiedName -> e)

    }).flatten.toMap

    currentImports.push(all)
  }

  def pushConsts(constexprs: scala.collection.immutable.Map[StringNode, ExpressionUnitContext]): Unit = {
    currentConsts.push(constexprs)
  }

  def popImports(): Unit = {
    currentImports.pop()
  }


  def popConsts(): Unit = {
    currentConsts.pop()
  }
}
