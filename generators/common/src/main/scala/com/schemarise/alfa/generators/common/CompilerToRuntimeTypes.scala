package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.AlfaInternalException
import com.schemarise.alfa.compiler.ast.CrcUtils
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.types.Scalars.ScalarType
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model.types._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{ErrorableDataType, TypeDefedDataType, UdtOrTypeDefedDataType}
import com.schemarise.alfa.compiler.ast.nodes.{AssertDeclaration, Locatable, SyntheticRecord}
import com.schemarise.alfa.compiler.tools.graph._
import com.schemarise.alfa.compiler.types.AnnotationTargetType
import com.schemarise.alfa.compiler.utils.{ILogger, VFS}
import com.schemarise.alfa.runtime.{Builder, BuilderConfig, NormalizedPeriod}
import schemarise.alfa.runtime._
import schemarise.alfa.runtime.model.{IDataType => _, _}

import java.nio.file.Path
import java.time._
import java.util
import java.util.{Collections, Comparator, Optional}
import scala.collection.JavaConverters._

object CompilerToRuntimeTypes {
  def create(logger: ILogger, cua: ICompilationUnitArtifact, usedTypeAliases: Boolean = false): CompilerToRuntimeTypes = {
    new CompilerToRuntimeTypes(logger, cua, usedTypeAliases)
  }
}

class CompilerToRuntimeTypes protected(logger: ILogger, cua: ICompilationUnitArtifact, usedTypeAliases: Boolean = false) extends HierarchyService {

  protected val nameIndex: Map[String, IUdtVersionName] = cua.getUdtVersionNames().map(e => e.fullyQualifiedName -> e).toMap
  protected val udtDetailsIndex: Map[String, Try[ModelBaseNode]] =
    cua.getUdtVersionNames().map(e => e.fullyQualifiedName -> fetchUdtDetails(e.fullyQualifiedName)).toMap

  def convert(tp: ITypeParameter): schemarise.alfa.runtime.model.TypeParameter = {
    val b = schemarise.alfa.runtime.model.TypeParameter.builder()
    b.setName(tp.name.fullyQualifiedName)
    b.build()
  }

  def convert(ut: UdtType): UdtMetaType = {
    UdtMetaType.valueOf(ut.toString() + "Type")
  }

  def convert(n: IUdtVersionName): UdtVersionedName = {
    val b = UdtVersionedName.builder().setFullyQualifiedName(n.fullyQualifiedName)
    if (n.version.isDefined)
      b.setVersion(n.version.get)
    if (n.typeParameters.size > 0) {
      val l: util.List[schemarise.alfa.runtime.model.TypeParameter] = new java.util.ArrayList[schemarise.alfa.runtime.model.TypeParameter]()
      n.typeParameters.foreach(p => l.add(convert(p._1)))
      b.setTypeParameters(Optional.of(l))
    }

    val udtopt = cua.getUdt(n.fullyQualifiedName, true)
    val udt = udtopt.get

    if (udt.isSynthetic) {
      b.setIsSynthetic(true)

      if (udt.isInstanceOf[SyntheticRecord]) {
        val sr = udt.asInstanceOf[SyntheticRecord]
        if (sr.udtParent.isDefined) {
          val re = convert(sr.udtParent.get.name)
          b.setAssociatedNonSyntheticUdt(Optional.of(re))
        }
      }
    }
    b.setUdtType(convert(n.udtType))

    b.build()
  }

  def udtToHierarchInfo(d: IUdtBaseNode): HierarchyUdtInfo = {
    val b = HierarchyUdtInfo.builder()
    b.setName(convert(d.name))
    b.build()
  }

  private def updateParent(hierarchy: util.Map[String, util.Set[String]], parent: INamespaceNode, ns: INamespaceNode) = {

    val set = if (hierarchy.containsKey(parent.name)) {
      hierarchy.get(parent.name)
    } else {
      val n = new util.TreeSet[String]()
      hierarchy.put(parent.name, n)
      n
    }
    set.add(ns.name)
  }

  override def getImmediateHierarchy(_namespace: String): Hierarchy = getHierarchy(Some(_namespace))

  override def getCompleteHierarchy(): Hierarchy = getHierarchy(None)

  private def getHierarchy(nsName: Option[String]): Hierarchy = {
    val b = Hierarchy.builder()
    b.putAllNsUdts(Collections.emptyMap())

    cua.graph.namespacesTopologicallyOrPermittedOrdered().get.foreach(ns => {
      val comp = new Comparator[HierarchyUdtInfo]() {
        override def compare(o1: HierarchyUdtInfo, o2: HierarchyUdtInfo): Int =
          o1.getName.getFullyQualifiedName.compareTo(o2.getName.getFullyQualifiedName)
      }

      val udtDepsSet = new util.TreeSet[HierarchyUdtInfo](comp)
      val outgoingUdtEdges = cua.graph.namespaceOutgoingEdgeNodes(ns, IsNamespaceUdtPredicate)
      outgoingUdtEdges.map(_.asInstanceOf[IUdtBaseNode]).foreach(d => udtDepsSet.add(udtToHierarchInfo(d)))

      if (nsName.isEmpty || (nsName.isDefined && nsName.get.equals(ns.name)))
        b.putNsUdts(ns.name, udtDepsSet)
    })

    val allNs = new util.LinkedHashSet[String]()
    allNs.add("") // root
    cua.graph.namespacesTopologicallyOrPermittedOrdered().get.foreach(ns => allNs.add(ns.name))

    val nsHierarchySet = new util.TreeMap[String, util.Set[String]]()
    val nspaces = cua.graph.namespacesTopologicallyOrPermittedOrdered().get.foreach(ns => {

      val optParentNs = ns.parentNamespaces.reverse.find(e => allNs.contains(e.name))
      if (optParentNs.isEmpty)
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Parent HAS TO exist?!")
      val parentNs = optParentNs.get

      if (nsName.isEmpty || (nsName.isDefined && nsName.get.equals(parentNs.name)))
        updateParent(nsHierarchySet, optParentNs.get, ns)
    })

    b.putAllNsHierarchy(nsHierarchySet)

    //    outgoingNSEdges.map( _.asInstanceOf[com.schemarise.alfa.compiler.ast.Namespace]).foreach( d => nsDepsSet.add(d.name))

    b.build()
  }

  private def readSrc(l: Locatable): String = {
    if (l.location.getSourcePath.isDefined) {
      val lines = VFS.read(l.location.getSourcePath.get)
      lines.substring(l.location.getStartInStream, l.location.getEndInStream + 1)
    }
    else
      ""
  }

  private def convertTestcase(u: ITestcase): schemarise.alfa.runtime.model.Testcase = {
    val lib = convertLibrary(u)

    val srv = schemarise.alfa.runtime.model.Testcase.builder()

    if (lib.getIncludes.isPresent)
      srv.setIncludes(lib.getIncludes)

    srv.putAllMethods(lib.getMethods)
    srv.setName(lib.getName)
    srv.setDoc(lib.getDoc)
    srv.setSource(lib.getSource)
    srv.setContainerName(lib.getContainerName)
    srv.setContainerType(lib.getContainerType)
    srv.setChecksum(lib.getChecksum)
    srv.build()
  }

  private def populateMethodContainer(u: IUdtBaseNode): MethodsContainer = {

    val mc = MethodsContainer.builder()

    if (u.isLibrary)
      mc.setContainerType(MethodsContainerType.Library)
    else if (u.isService)
      mc.setContainerType(MethodsContainerType.Service)
    else if (u.isTestcase)
      mc.setContainerType(MethodsContainerType.Testcase)
    else if (u.isTransform)
      mc.setContainerType(MethodsContainerType.Transform)

    val ctr = u.constructorFormals.map(f => f._1 -> convertFormal(f._2)).asJava
    if (ctr.size() > 0)
      mc.putAllConstructorFormals(ctr)

    mc.setContainerName(convert(u.name))

    mc.build()
  }

  private def convertLibrary(u: IUdtBaseNode): schemarise.alfa.runtime.model.Library = {
    val srv = schemarise.alfa.runtime.model.Library.builder()
    populate(u, srv)

    val mc = populateMethodContainer(u)

    srv.setContainerType(mc.getContainerType)
    srv.putAllMethods(mc.getMethods)
    srv.putAllConstructorFormals(mc.getConstructorFormals)

    val edgeso = cua.graph.incomingEdgeNodes(u, new IsIncludesPredicate(new Vertex(u)))
    if (edgeso.size > 0) {
      var deps: util.List[UdtVersionedName] = new util.ArrayList[UdtVersionedName]()
      edgeso.foreach(i => {
        val u = i.asInstanceOf[IUdtBaseNode]
        deps.add(convert(u.name))
      })

      deps.sort(new Comparator[UdtVersionedName] {
        override def compare(o1: UdtVersionedName, o2: UdtVersionedName): Int = {
          return o1.getFullyQualifiedName.compareTo(o2.getFullyQualifiedName)
        }
      })

      srv.setIncludedFrom(Optional.of(deps))
    }

    srv.setContainerName(mc.getContainerName)
    srv.build()
  }

  private def convertService(u: IService): schemarise.alfa.runtime.model.Service = {
    val srv = schemarise.alfa.runtime.model.Service.builder()
    populate(u, srv)

    val mc = populateMethodContainer(u)

    srv.setContainerType(mc.getContainerType)
    srv.putAllMethods(mc.getMethods)
    srv.putAllConstructorFormals(mc.getConstructorFormals)

    val edgeso = cua.graph.incomingEdgeNodes(u, new IsIncludesPredicate(new Vertex(u)))
    if (edgeso.size > 0) {
      var deps: util.List[UdtVersionedName] = new util.ArrayList[UdtVersionedName]()
      edgeso.foreach(i => {
        val u = i.asInstanceOf[IUdtBaseNode]
        deps.add(convert(u.name))
      })

      deps.sort(new Comparator[UdtVersionedName] {
        override def compare(o1: UdtVersionedName, o2: UdtVersionedName): Int = {
          return o1.getFullyQualifiedName.compareTo(o2.getFullyQualifiedName)
        }
      })

      srv.setIncludedFrom(Optional.of(deps))
    }

    srv.setContainerName(mc.getContainerName)

    srv.build()
  }

  private def populate(u: IUdtBaseNode, udtBuilder: schemarise.alfa.runtime.model.ModelBaseNode.ModelBaseNodeBuilder): Unit = {
    val src = Union__ModelBaseNode__Source.builder().setContents(readSrc(u.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode])).build()
    udtBuilder.setSource(src)
    //    udtBuilder.setSrcLocation( Optional.of(makeLocation(u.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode])))
    udtBuilder.setName(convert(u.name))

    val sig = u.checksum()
    udtBuilder.setChecksum(sig)
    u.modelId().map(e => udtBuilder.setModelId(Optional.of(e)))

    if (u.isInternal) {
      udtBuilder.addModifiers(ModifierType.Internal)
    }

    if (u.isTrait) {
      val t = u.asInstanceOf[ITrait]
      if (t.scope.length > 0) {
        val b: util.List[UdtDataType] = new java.util.ArrayList[UdtDataType]()
        t.scope.foreach(sc => b.add(convert(sc).asInstanceOf[UdtDataType]))
        udtBuilder.setScope(Optional.of(b))
      }
    }

    if (u.includes.size > 0) {
      val b: util.List[UdtDataType] = new java.util.ArrayList[UdtDataType]()

      u.includes.foreach(inc => b.add(convert(inc).asInstanceOf[UdtDataType]))
      udtBuilder.setIncludes(Optional.of(b))
    }

    var local = new util.ArrayList[String]()
    u.localFieldNames.foreach(f => local.add(f))


    val includesEdges = cua.graph.incomingEdgeNodes(u, new IsIncludesPredicate(new Vertex(u)))
    if (includesEdges.size > 0) {
      var deps: util.List[UdtVersionedName] = new util.ArrayList[UdtVersionedName]()
      includesEdges.foreach(i => {
        val u = i.asInstanceOf[IUdtBaseNode]

        deps.add(convert(u.name))
      })

      deps.sort(new Comparator[UdtVersionedName] {
        override def compare(o1: UdtVersionedName, o2: UdtVersionedName): Int = {
          return o1.getFullyQualifiedName.compareTo(o2.getFullyQualifiedName)
        }
      })

      udtBuilder.setIncludedFrom(Optional.of(deps))
    }

    val fieldRefEdges = cua.graph.incomingEdgeNodes(u, new IsFieldDataTypeOrKeyEdgePredicate(new Vertex(u)))
    if (fieldRefEdges.size > 0) {
      var deps: util.List[UdtVersionedName] = new util.ArrayList[UdtVersionedName]()
      fieldRefEdges.foreach(i => {
        val u = i.asInstanceOf[IUdtBaseNode]

        deps.add(convert(u.name))
      })

      deps.sort(new Comparator[UdtVersionedName] {
        override def compare(o1: UdtVersionedName, o2: UdtVersionedName): Int = {
          return o1.getFullyQualifiedName.compareTo(o2.getFullyQualifiedName)
        }
      })

      udtBuilder.setReferencedInFieldTypeFrom(Optional.of(deps))
    }

    if (!u.docs.isEmpty) {
      udtBuilder.setDoc(Optional.of(u.docs.mkString("")))
    }

    if (!u.annotations.isEmpty) {
      val modelAnns = populateAnnotations(u)
      udtBuilder.setAnnotations(Optional.of(modelAnns))
    }
  }

  def populateAnnotations(u: IDocAndAnnotated): util.Map[String, util.Map[String, schemarise.alfa.runtime.model.IExpression]] = {
    val modelAnns = new util.HashMap[String, util.Map[String, schemarise.alfa.runtime.model.IExpression]]

    u.annotations.map(a => {
      if (a.objectExpression.isDefined) {
        val annArgs = new util.HashMap[String, schemarise.alfa.runtime.model.IExpression]

        modelAnns.put(a.versionedName.fullyQualifiedName, annArgs)
      }
      else
        modelAnns.put(a.versionedName.fullyQualifiedName, Collections.emptyMap())
    })
    modelAnns
  }

  def convert(u: IUdtBaseNode): schemarise.alfa.runtime.model.ModelBaseNode = {
    u match {
      case t: IDataproduct =>
        val b = Dataproduct.builder()
        b.setName(convert(u.name))
        b.addAllPublish(Collections.emptySet())
        b.putAllConsume(Collections.emptyMap())
        populate(u, b)
        b.build()

      case t: IService => convertService(t)
      case l: ILibrary => convertLibrary(l)
      case l: ITestcase => convertTestcase(l)
      case x => convertUdt(x)
    }
  }

  private def createEntityKey(k: IKey): EntityKey = {
    val ek = EntityKey.builder()

    if (k.isSynthetic) {
      k.allFields.map(f => {
        ek.addKeyFields(convertField(f._2))
      })
    }
    else {
      ek.setKeyRef(convert(k.asDataType).asInstanceOf[UdtDataType])
    }
    ek.build()
  }

  private def convertUdt(u: IUdtBaseNode): schemarise.alfa.runtime.model.ModelBaseNode = {
    var udtBuilder: schemarise.alfa.runtime.model.UdtBaseNode.UdtBaseNodeBuilder = null

    u match {
      case a: com.schemarise.alfa.compiler.ast.nodes.AnnotationDecl =>
        val ann = schemarise.alfa.runtime.model.AnnotationDecl.builder()
        a.annotationTargets.foreach(e => {
          val tgt = e match {
            case AnnotationTargetType.Record => UdtMetaType.recordType
            case AnnotationTargetType.Trait => UdtMetaType.recordType
            case AnnotationTargetType.Entity => UdtMetaType.entityType
            case AnnotationTargetType.Enum => UdtMetaType.enumType
            case AnnotationTargetType.Key => UdtMetaType.keyType
            case AnnotationTargetType.Field => UdtMetaType.fieldType
            case AnnotationTargetType.KeyField => UdtMetaType.keyFieldType
            case AnnotationTargetType.Method => UdtMetaType.methodType
            case AnnotationTargetType.Namespace => UdtMetaType.namespaceType
            case AnnotationTargetType.Service => UdtMetaType.serviceType
            case AnnotationTargetType.Union => UdtMetaType.unionType
            case AnnotationTargetType.Dataproduct => UdtMetaType.dataproductType
            case AnnotationTargetType.Type => UdtMetaType.userDefinedType
          }
          ann.addTargets(tgt)
        })
        if (a.annotationTargets.isEmpty) {
          ann.addAllTargets(Collections.emptyList())
        }

        val annotatedUdts = cua.graph.incomingEdgeNodes(u, new IsUdtAnnotationPredicate(new Vertex(u)))
        val annotatedFields = cua.graph.incomingEdgeNodes(u, new IsFieldAnnotationPredicate(new Vertex(u)))
        val annotatedMethods = cua.graph.incomingEdgeNodes(u, new IsMethodAnnotationPredicate(new Vertex(u)))

        ann.addAllReferencedFromUdts(Collections.emptySet())
        ann.putAllReferencedFromUdtAttribs(Collections.emptyMap())

        annotatedUdts.map(af => {
          ann.addReferencedFromUdts(convert(af.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.UdtBaseNode].name))
        })

        ann.putAllReferencedFromUdtAttribs(new util.HashMap[UdtVersionedName, util.List[String]]())
        annotatedFields.map(af => {
          val f = af.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Field]
          val udt = f.locateUdtParent
          val fname = convert(udt.name)

          var fields = ann.getReferencedFromUdtAttribs.get(fname)
          if (fields == null) {
            fields = new java.util.ArrayList[String]()
            ann.putReferencedFromUdtAttribs(fname, fields)
          }
          fields.add(f.name)
        })

        annotatedMethods.map(af => {
          val f = af.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.MethodSignature]
          val udt = f.locateUdtParent
          val fname = convert(udt.name)

          var fields = ann.getReferencedFromUdtAttribs.get(fname)
          if (fields == null) {
            fields = new java.util.ArrayList[String]()
            ann.putReferencedFromUdtAttribs(fname, fields)
          }
          fields.add(f.name.fullyQualifiedName)
        })

        udtBuilder = ann

      case t: IRecord =>
        udtBuilder = schemarise.alfa.runtime.model.Record.builder()

      case t: ITrait =>
        udtBuilder = schemarise.alfa.runtime.model.Trait.builder()

      case t: IUnion =>
        val ub = schemarise.alfa.runtime.model.Union.builder()

        ub.putAllAllFields(Collections.emptyMap())

        if (t.unionType == UnionType.Tagged) {
          ub.setUnionType(UnionType.Tagged)
        }
        else {
          ub.setUnionType(UnionType.Untagged)
        }

        udtBuilder = ub

      case t: IEnum =>
        udtBuilder = schemarise.alfa.runtime.model.Enum.builder()

      case t: IEntity =>
        val eb = schemarise.alfa.runtime.model.Entity.builder()
        if (t.key.isDefined) {
          eb.setKey(Optional.of(createEntityKey(t.key.get)))
        }
        udtBuilder = eb

      case t: IKey =>
        udtBuilder = schemarise.alfa.runtime.model.Key.builder()

      case t: INativeUdt =>
        val nb = schemarise.alfa.runtime.model.NativeType.builder()
        nb.setAliasedType(t.aliasedName)
        udtBuilder = nb

      case t: com.schemarise.alfa.compiler.ast.nodes.ExtensionDecl =>
        val nb = ExtensionDef.builder()
        udtBuilder = nb

      case t: com.schemarise.alfa.compiler.ast.nodes.Extension =>
        val nb = Extension.builder()

        val oe = t.objectExpression.get
        nb.putAllAllFields(util.Collections.emptyMap())
        udtBuilder = nb

      case _ => throw new AlfaInternalException("Unsupported udt " + u)

    }

    if (u.extendsDef.isDefined) {
      udtBuilder.setExtends(convert(u.extendsDef.get).asInstanceOf[UdtDataType])
    }
    populate(u, udtBuilder)

    udtBuilder.setIsSynthetic(u.isSynthetic)

    if (u.allFields.size > 0) {
      val m = new util.LinkedHashMap[String, Field]()
      u.allFields.map(e => {
        m.put(e._1, convertField(e._2))
      })
      udtBuilder.putAllAllFields(m)
    }
    else {
      udtBuilder.putAllAllFields(Collections.emptyMap())
    }

    val local = new util.ArrayList[String]()
    u.localFieldNames.foreach(f => local.add(f))
    udtBuilder.addAllLocalFieldNames(local)

    udtBuilder.asInstanceOf[Builder].build()
  }

  def convertField(f: IField): schemarise.alfa.runtime.model.Field = {
    val b = schemarise.alfa.runtime.model.Field.builder()
    convertAttribute(f, b)

    if (f.enumLexical.isDefined)
      b.setEnumLexical(Optional.of(f.enumLexical.get))

    b.build()
  }

  private def convertFormal(f: IFormal): schemarise.alfa.runtime.model.Formal = {
    val b = schemarise.alfa.runtime.model.Formal.builder()
    convertAttribute(f, b)
    b.build()
  }

  private def convertAttribute(f: IField, b: IAttribute.IAttributeBuilder): Unit = {
    b.setName(f.name)

    if (usedTypeAliases) {
      b.setDataType(convert(f.declaredAsDataType))
    }
    else {
      b.setDataType(convert(f.dataType))
    }

    //    b.setSrcLocation(Optional.of(makeLocation(fieldNode)))

    if (f.declaredAsDataType != f.dataType && f.declaredAsDataType.isInstanceOf[UdtOrTypeDefedDataType]) {
      val td = f.declaredAsDataType.asInstanceOf[UdtOrTypeDefedDataType]

      var refType = TypeDefDataType.builder().setTypeName(td.name.text).setFinalType(convert(td.target.get))

      if (td.typeArguments.isDefined) {
        val l: java.util.List[schemarise.alfa.runtime.model.IDataType] = new java.util.ArrayList[schemarise.alfa.runtime.model.IDataType]()
        td.typeArguments.get.foreach(e => l.add(convert(e)))
        refType.setTypeArguments(l)

      }

      //      b.setDeclTypeDefDataType(Optional.of(refType.build())) xxx
    }

    if (f.docs.size > 0) {
      b.setDoc(Optional.of(f.docs.map(e => e.text).mkString("")))
    }

    if (!f.annotations.isEmpty) {
      val modelAnns = populateAnnotations(f)
      b.setAnnotations(Optional.of(modelAnns))
    }
  }

  private def makeRangeValue(st: ScalarType, v: Any): RangeValue = {
    st match {
      case Scalars.date =>
        RangeValue.builder().setDateValue(v.asInstanceOf[LocalDate]).build()
      case Scalars.datetime =>
        RangeValue.builder().setDatetimeValue(v.asInstanceOf[LocalDateTime]).build()
      case Scalars.datetimetz =>
        RangeValue.builder().setDatetimetzValue(v.asInstanceOf[ZonedDateTime]).build()
      case Scalars.time =>
        RangeValue.builder().setTimeValue(v.asInstanceOf[LocalTime]).build()
      case Scalars.duration =>
        RangeValue.builder().setDurationValue(v.asInstanceOf[Duration]).build()
      case Scalars.period =>
        val np = NormalizedPeriod.of(v.asInstanceOf[NormalizedAstPeriod].getUnder)
        RangeValue.builder().setPeriodValue(np).build()
      case Scalars.double =>
        RangeValue.builder().setDoubleValue(v.asInstanceOf[Double]).build()
      case Scalars.decimal =>
        RangeValue.builder().setDoubleValue(v.asInstanceOf[Double]).build()
      case Scalars.int =>
        RangeValue.builder().setIntValue(Integer.parseInt(v.toString)).build()
      case _ =>
        RangeValue.builder().setLongValue(java.lang.Long.parseLong(v.toString)).build()
    }
  }

  def convert(idt: IDataType, typeDefedName: java.util.Optional[String] = java.util.Optional.empty()): schemarise.alfa.runtime.model.IDataType = {

    idt match {
      case t: com.schemarise.alfa.compiler.ast.nodes.datatypes.ScalarDataType =>
        val st = t.scalarType.toString
        val b = ScalarDataType.builder()
        b.setScalarType(schemarise.alfa.runtime.model.ScalarType.valueOf(st + "Type"))

        if (t.min.isDefined)
          b.setMin(java.util.Optional.of(makeRangeValue(t.scalarType, t.min.get)))
        if (t.max.isDefined)
          b.setMax(java.util.Optional.of(makeRangeValue(t.scalarType, t.max.get)))

        if (t.scalarType == Scalars.decimal && t.precision.isDefined) {
          val dp = Optional.of(DecimalPrecision.builder().setPrecision(t.precision.get).setScale(t.scale.get).build())
          b.setPrecision(dp)
        }

        if ((t.scalarType == Scalars.date ||
          t.scalarType == Scalars.datetime ||
          t.scalarType == Scalars.datetimetz ||
          t.scalarType == Scalars.time) && t.dateFormat.isDefined) {
          b.setStrPattern(Optional.of(t.dateFormat.get))
        }

        if (t.scalarType == Scalars.string && t.stringPattern.isDefined) {
          b.setStrPattern(Optional.of(t.stringPattern.get))
        }

        b.setTypeDefName(typeDefedName)
        b.build()

      case t: IListDataType =>
        val lt = t.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.ListDataType]

        val b = ListDataType.builder()

        if (usedTypeAliases)
          b.setComponentType(convert(lt.declComponentType))
        else
          b.setComponentType(convert(t.componentType))

        if (t.min.isDefined)
          b.setSizeMin(t.min.get.asInstanceOf[Int])
        if (t.max.isDefined)
          b.setSizeMax(t.max.get.asInstanceOf[Int])

        b.setTypeDefName(typeDefedName)
        b.build()

      case t: ISetDataType =>
        val b = schemarise.alfa.runtime.model.SetDataType.builder(BuilderConfig.getInstance())

        b.setComponentType(convert(t.componentType))
        if (t.min.isDefined)
          b.setSizeMin(t.min.get.asInstanceOf[Int])
        if (t.max.isDefined)
          b.setSizeMax(t.max.get.asInstanceOf[Int])

        b.setTypeDefName(typeDefedName)
        b.build()

      case t: IMapDataType =>
        val b = schemarise.alfa.runtime.model.MapDataType.builder(BuilderConfig.getInstance())

        val mt = t.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.MapDataType]

        if (usedTypeAliases)
          b.setKeyType(convert(mt.declKeyType))
        else
          b.setKeyType(convert(t.keyType))

        if (usedTypeAliases)
          b.setValueType(convert(mt.declValueType))
        else
          b.setValueType(convert(t.valueType))

        if (t.keyName.isDefined)
          b.setKeyName(Optional.of(t.keyName.get))
        if (t.valueName.isDefined)
          b.setValueName(Optional.of(t.valueName.get))

        if (t.min.isDefined)
          b.setSizeMin(t.min.get.asInstanceOf[Int])
        if (t.max.isDefined)
          b.setSizeMax(t.max.get.asInstanceOf[Int])

        b.setTypeDefName(typeDefedName)
        b.build()

      case t: IUdtDataType =>
        val b = schemarise.alfa.runtime.model.UdtDataType.builder(BuilderConfig.getInstance()).setFullyQualifiedName(t.fullyQualifiedName)

        t.udt.whenEnum(_ => b.setUdtType(UdtMetaType.enumType))
        t.udt.whenUnion(_ => {
          val u = t.udt.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.Union]
          if (u.unionType == AstUnionType.Tagged)
            b.setUdtType(UdtMetaType.unionType)
          else
            b.setUdtType(UdtMetaType.untaggedUnionType)
        })
        t.udt.whenEntity(_ => b.setUdtType(UdtMetaType.entityType))
        t.udt.whenKey(_ => b.setUdtType(UdtMetaType.keyType))
        t.udt.whenRecord(_ => b.setUdtType(UdtMetaType.recordType))
        t.udt.whenNativeUdt(_ => b.setUdtType(UdtMetaType.nativeUdtType))
        t.udt.whenTrait(_ => b.setUdtType(UdtMetaType.traitType))
        t.udt.whenService(_ => b.setUdtType(UdtMetaType.serviceType))
        t.udt.whenLibrary(_ => b.setUdtType(UdtMetaType.libraryType))
        t.udt.whenTestcase(_ => b.setUdtType(UdtMetaType.testcaseType))
        t.udt.whenAnnotationDecl(_ => b.setUdtType(UdtMetaType.annotationType))

        if (t.version.isDefined)
          b.setVersion(t.version.get)

        if (t.typeArguments.isDefined) {
          var tas: util.Map[String, model.IDataType] = new util.LinkedHashMap[String, model.IDataType]()

          t.typeParamsToArgs.get.keys.foreach(k => {
            tas.put(k, convert(t.typeParamsToArgs.get.get(k).get))
          })
          b.setTypeArguments(Optional.of(tas))
        }

        b.setTypeDefName(typeDefedName)
        b.build()

      case t: IEitherDataType =>
        val l = convert(t.left)
        val r = convert(t.right)
        EitherDataType.builder(BuilderConfig.getInstance()).setLeftComponentType(l).
          setTypeDefName(typeDefedName).
          setRightComponentType(r).build()

      case t: IPairDataType =>
        val l = convert(t.left)
        val r = convert(t.right)
        PairDataType.builder(BuilderConfig.getInstance()).setLeftComponentType(l).
          setTypeDefName(typeDefedName).
          setRightComponentType(r).build()

      case t: ITupleDataType =>
        val b = TupleDataType.builder(BuilderConfig.getInstance())
        b.setSynthFullyQualifiedName(t.syntheticRecord.name.fullyQualifiedName)
        b.putAllFields(new util.HashMap[String, Field]())

        val anns = populateAnnotations(t)
        b.setAnnotations(Optional.of(anns))

        t.syntheticRecord.allFields.foreach(f => b.putFields(f._1, convertField(f._2)))
        b.setSyntheticFieldNames(t.syntheticRecord.allFields.filter(f => f._1.startsWith("_")).size > 0)

        b.setTypeDefName(typeDefedName).build()

      case t: IEnumDataType =>
        val b = EnumDataType.builder(BuilderConfig.getInstance())
        b.setSynthFullyQualifiedName(t.syntheticEnum().name.fullyQualifiedName)
        t.syntheticEnum.allFields.foreach(f => b.addFields(f._1))
        b.setTypeDefName(typeDefedName).build()

      case t: IUnionDataType =>
        val b = UnionDataType.builder(BuilderConfig.getInstance())
        b.setSynthFullyQualifiedName(t.syntheticUnion.name.fullyQualifiedName)


        b.putAllFields(Collections.emptyMap())
        t.syntheticUnion.allFields.foreach(f => b.putFields(f._1, convertField(f._2)))

        //        t.untaggedTypes.foreach( t => b.addTypes( convert(t) ) )

        if (t.unionType == UnionType.Tagged)
          b.setUnionType(UnionType.Tagged)
        else
          b.setUnionType(UnionType.Untagged)

        b.setTypeDefName(typeDefedName).build()

      case t: IEnclosingDataType =>

        val lt = t.asInstanceOf[com.schemarise.alfa.compiler.ast.nodes.datatypes.EnclosingDataType]


        val e = if (usedTypeAliases)
          convert(lt.declComponentType)
        else
          convert(t.componentType)

        t.encType match {
          case Enclosed.compress =>
            CompressedDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.encrypt =>
            EncryptedDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.future =>
            FutureDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          //          case Enclosed.key =>
          //             KeyDataType.builder(BuilderConfig.getInstance()).setComponentType(e).build()
          case Enclosed.opt =>
            OptionalDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.key =>
            KeyDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.stream =>
            StreamDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.`table` =>
            TabularDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
          case Enclosed.try_ =>
            TryDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setComponentType(e).build()
        }

      case t: ITypeParameterDataType =>
        TypeParameterDataType.builder(BuilderConfig.getInstance()).setTypeDefName(typeDefedName).setParamName(t.parameterName).build()

      case t: TypeDefedDataType =>
        val ft = convert(t.referencedType)
        TypeDefDataType.builder().setTypeName(t.newType.text).setFinalType(ft).build();

      case t: IMetaDataType =>
        val v = t.metaType.toString.substring(1)
        val en = v.substring(0, 1).toUpperCase() + v.substring(1)
        MetaDataType.builder(BuilderConfig.getInstance()).
          setMetaType(schemarise.alfa.runtime.model.MetaType.fromValue(en)).setTypeDefName(typeDefedName).
          build()

      case x: ILambdaDataType =>
        val b = LambdaDataType.builder()

        if (x.argTypes.size == 0)
          b.addAllArgTypes(Collections.emptyList())

        x.argTypes.foreach(t => {
          b.addArgTypes(convert(t))
        })
        b.setResultType(convert(x.resultType)).build()

      case x: IAnyDataType =>
        AnyDataType.builder().build()

      case x: ErrorableDataType =>
        val c = x.cause

        val info = if (c.isLeft)
          (c.left.get.location.toString, c.left.get.formattedMessage)
        else
          (c.right.get.location.toString, c.right.get.toString)

        ErrorDataType.builder().setLocation(info._1).setMessage(info._2).build()

      case x: UdtOrTypeDefedDataType =>
        val typeDefName = x.name.text
        val n = if (x.isTypeDefDeclared) typeDefName else null
        val converted = convert(x.targetType(), java.util.Optional.ofNullable(n))
        converted

      case t: IDataType =>
        throw new com.schemarise.alfa.compiler.AlfaInternalException("Not supported datatype " + t + " " + t.getClass)
    }
  }

  private def fetchUdtDetails(_name: String): Try[ModelBaseNode] = {
    val v = searchUdts(_name)

    if (v.isEmpty)
      createTryFailure("UDT not found " + _name)
    else {
      val entry = v.get(0);

      val udt = if (entry.getTypeParameters.isPresent) {
        cua.getUdtWithParams(entry.getFullyQualifiedName, entry.getTypeParameters.get().asScala.map(tn => tn.getName))
      }
      else
        cua.getUdt(entry.getFullyQualifiedName)

      createTryObject[schemarise.alfa.runtime.model.ModelBaseNode](convert(udt.get))
    }
  }

  override def getUdtDetails(_name: String): Try[ModelBaseNode] = {
    val e = udtDetailsIndex.get(_name)

    if (e.isDefined)
      e.get
    else
      createTryFailure("UDT not found " + _name)
  }

  override def searchUdts(_text: String): util.List[UdtVersionedName] = {

    val ret = new util.ArrayList[UdtVersionedName]()

    val e = nameIndex.get(_text)
    if (e.isDefined)
      ret.add(convert(e.get))

    ret
  }

  override def getAllUdts: AllUdts = {
    var names = cua.getUdtVersionNames()

    var b = AllUdts.builder()

    names.toList.sortBy(e => e.fullyQualifiedName).foreach(n => {
      val v = convert(n)
      val syn = v.getIsSynthetic
      if (syn.isEmpty || (syn.isPresent && !syn.get()))
        b.addUdts(v)
    })
    if (names.isEmpty)
      b.addAllUdts(Collections.emptyList())

    b.build()

  }

  override def getAllNamespaces = {
    val h = cua.getNamespaces()
    val result = new util.LinkedHashMap[String, Namespace]()

    h.sortBy(e => e.name).foreach(ns => {
      val m = cua.getNamespaceMeta(ns.name)
      val b = Namespace.builder()
      b.setQualifiedName(ns.name)

      val udts = cua.getUdtVersionNames().toList.
        sortBy(e => e.fullyQualifiedName).filter(e => {
          e.namespace.name == ns.name
        }).
        toList.sortBy(e => e.fullyQualifiedName).
        filter(e => {
          val v = convert(e)
          val syn = v.getIsSynthetic
          syn.isEmpty || (syn.isPresent && !syn.get())
        })

      b.setUdtCount(udts.size)

      if (m.isDefined && m.get.docs.size > 0) {
        b.setDoc(Optional.of(m.get.docs.map(e => e.text.trim).toSet.mkString("")))
      }
      result.put(ns.name, b.build())
    })

    AllNamespaces.builder().putAllNamespaces(result).build()
  }


  private def createTryFailure[T](msg: String): Try[T] = {
    val tf = TryFailure.builder.setMessage(msg).build
    Try.builder.setFailure(tf).build.asInstanceOf[Try[T]]
  }

  private def createTryObject[T](o: T): Try[T] = Try.builder().setResult(o).build.asInstanceOf[Try[T]]

  override def getAllNamespaceSummaries: AllNamespaceSummary = {
    val h = cua.getNamespaces()

    val result = new util.LinkedHashMap[String, NamespaceSummary]()
    h.sortBy(w => w.name).foreach(ns => {
      val m = cua.getNamespaceMeta(ns.name)
      val b = NamespaceSummary.builder()
      b.setQualifiedName(ns.name)

      val checksums = new util.ArrayList[String]()

      val udts = cua.getUdtVersionNames().filter(e => {
          e.namespace.name == ns.name
        }).toList.sortBy(e => e.fullyQualifiedName).filter(e => {
          val v = convert(e)
          val syn = v.getIsSynthetic
          syn.isEmpty || (syn.isPresent && !syn.get())
        }).
        map(e => {
          makeUdtSummaryAndChecksums(checksums, e)
        })

      b.addAllUdts(udts.asJava)

      if (m.isDefined && m.get.docs.size > 0) {
        b.setDoc(Optional.of(m.get.docs.map(e => e.text.trim).toSet.mkString("")))
      }
      result.put(ns.name, b.build())

    })

    AllNamespaceSummary.builder().putAllNamespaces(result).build()
  }

  private def makeUdtSummaryAndChecksums(checksums: util.ArrayList[String], e: IUdtVersionName) = {
    val udt = cua.getUdt(e.fullyQualifiedName).get
    val d = UdtSummary.builder().
      setFullyQualifiedName(udt.name.fullyQualifiedName).
      setAttribCount(Math.max(udt.allFields.size, udt.getMethodSignatures().size))

    if (udt.docs.size > 0) {
      val txt = udt.docs.mkString("\n").trim
      d.setDoc(txt.split("\\.").head)
    }
    
    if (udt.isInternal)
      d.setIsInternal(true)

    if (udt.name.version.isDefined) {
      d.setVersion(udt.name.version.get)
    }
    d.setUdtType(convert(udt.name.udtType))

    if (udt.isSynthetic) {
      d.setIsSynthetic(true)
    }

    checksums.add(udt.checksum())

    d.build()
  }

  override def getNamespaceDetails(ns: String): NamespaceDetails = {
    val m = cua.getNamespaceMeta(ns)
    val b = NamespaceDetails.builder()
    b.setQualifiedName(ns)

    val checksums = new util.ArrayList[String]()

    val udts = cua.getUdtVersionNames().toList.sortBy(e => e.fullyQualifiedName).filter(e => {
      e.namespace.name == ns
    }).map(e => {
      makeUdtSummaryAndChecksums(checksums, e)
    })

    b.setChecksum(CrcUtils.crc(checksums.asScala.sorted.mkString("")))

    b.addAllUdts(udts.asJava)

    if (m.isDefined && m.get.docs.size > 0) {
      b.setDoc(Optional.of(m.get.docs.map(e => e.text.trim).toSet.mkString("")))
    }
    b.build()
  }

  def convertAssert(ad: IAssertDeclaration): Assert = {
    val impl = ad.asInstanceOf[AssertDeclaration]

    val ab = Assert.builder()
    ab.setContainerType(MethodsContainerType.Assert)
    ab.setName(ad.name)
    ab.setContainerName(convert(impl.parentUdt().name))
    ab.build()
  }
}
