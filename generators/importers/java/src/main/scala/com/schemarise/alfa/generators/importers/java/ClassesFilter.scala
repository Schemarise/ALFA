package com.schemarise.alfa.generators.importers.java

import com.schemarise.alfa.compiler.AlfaCompiler
import com.schemarise.alfa.compiler.utils.{LexerUtils, StdoutLogger}
import org.reflections.Reflections
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import java.lang.reflect._
import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
class ClassesFilter ( packages : List[String], baseClassName : Option[String] ) {

  private val urls = packages.map( s => ClasspathHelper.forPackage(s).asScala.head )
  private val reflections = new Reflections(new ConfigurationBuilder().setUrls(urls:_*))
  private val baseClass = if ( baseClassName.isDefined ) Class.forName(baseClassName.get) else classOf[Object]
  
  private val openGammaClasses = reflections.
    getSubTypesOf(baseClass).asScala.
    filter(e => !e.isInterface && !Modifier.isAbstract(e.getModifiers)).
    filter(e => !e.getTypeName.endsWith("Function"))

  private val openGammaClassNames = openGammaClasses.map(e => normalizeClassName(e))
  private val pendingProcessingTypes = new util.Stack[String]()
  openGammaClasses.map(e => pendingProcessingTypes.push(e.getTypeName))

  private val processedTypeNames = new ListBuffer[String]()

  private val sb = new StringBuffer()

  def genAll() = {
    while (!pendingProcessingTypes.empty()) {
      val t = pendingProcessingTypes.pop()

      if (!processedTypeNames.contains(t)) {
        processedTypeNames.append(t)
        gen(t)
      }
    }

    val ac = new AlfaCompiler()
    val cua = ac.compile(sb.toString)

    println(sb.toString)

    val stdout = new StdoutLogger()
    println(cua.getErrors.mkString("\n"))
  }

  private def gen(t: String): Unit = {
    val c = Class.forName(t)

    val ifcs = extractIncludes(c)
    ifcs.foreach(e => {
      addPendingProcessingTypes(e)
    })

    printRecord(c, ifcs, classFields(c))
  }

  private def classFields(c: Class[_]): Map[String, String] = {

    val propMethods = c.getDeclaredMethods.toList.filter(m => {
        (m.getName.startsWith("get") && m.getParameterCount == 0) ||
          (m.getName.startsWith("is") && m.getParameterCount == 0 && m.getReturnType == classOf[Boolean])
      }).
      filter(e => !e.isBridge && !e.isDefault).
      map(m => {
        val n = m.getName
        val propName = if (n.startsWith("get"))
          n.substring(3)
        else
          n.substring(2)

        propName -> toAlfaTypeName(m.getGenericReturnType)
      }).toMap.
      filter(e => e._2 != null && !e._2.contains("null") && !e._2.contains("? extends"))

    propMethods
  }

  private def normalizeClassName(i: Type): String = {
    val raw = i match {
      case parameterizedType: ParameterizedType =>
        normalizeClassName(parameterizedType.getRawType)
      case _ =>
        normalizeClassName(i.asInstanceOf[Class[_]])
    }

    raw
  }

  private def normalizeClassName(t: Class[_]) = {
    t.getName
  }

  private def validAlfaTypeName(n: String) = {
    n.replace('$', '_')
  }

  private def printRecord(t: Class[_], ifcs: Seq[Type], props: Map[String, String]): Unit = {

    val cn = normalizeClassName(t)

    val baseIfcs = ifcs.map(e => {
      if (e.isInstanceOf[ParameterizedType]) {
        val pt = e.asInstanceOf[ParameterizedType]
        val rt = pt.getRawType
        rt
      }
      else {
        e
      }
    })

    val jifcs = baseIfcs.map(e => e.asInstanceOf[Class[_]]).filter(e => e.isInterface).map(e => validAlfaTypeName(normalizeClassName(e)))
    val jclasses = baseIfcs.map(e => e.asInstanceOf[Class[_]]).filter(e => !e.isInterface).map(e => validAlfaTypeName(normalizeClassName(e)))

    val extendz = if (jclasses.isEmpty) "" else " extends " + jclasses.head + " "
    val incs = if (ifcs.size == 0) "" else " includes " + jifcs.mkString(", ")

    val decl = if (t.isInterface) "trait" else "record"

    sb.append(s"$decl ${validAlfaTypeName(cn)} $extendz $incs {\n")
    props.foreach(e => {
      val fn = LexerUtils.validAlfaIdentifier(e._1)
      sb.append("  " + fn + " : " + e._2 + "\n")
    })
    sb.append("}\n\n")
  }

  private def addPendingProcessingTypes(s: Type) = {
    val cn = normalizeClassName(s)
    if (!processedTypeNames.contains(cn)) {
      pendingProcessingTypes.push(cn)
    }
  }

  private def extractIncludes(c: Class[_]) = {
    c.getGenericInterfaces.
      filter(i => i.getTypeName.startsWith("com.opengamma")).
      filter(i => !i.getTypeName.endsWith("Builder")).toSeq
  }

  private def toAlfaTypeName(t: Type): String = {

    val tn = t.getTypeName

    val alfaType =
      if (tn == "int")
        "int"
      else if (tn == "long")
        "long"
      else if (tn == "int[]")
        "list<int>"
      else if (tn == "double")
        "double"
      else if (tn == "java.lang.Double")
        "double"
      else if (tn == "java.lang.Integer")
        "int"
      else if (tn == "double[][]")
        "list< list< double > >"
      else if (tn == "double[]")
        "list< double >"
      else if (tn == "long[]")
        "list< long >"
      else if (tn == "byte[]")
        "binary"
      else if (tn == "java.time.LocalDate")
        "date"
      else if (tn == "java.time.LocalTime")
        "time"
      else if (tn == "java.time.temporal.TemporalUnit") {
        "string"
      }
      else if (tn == "java.time.LocalDate[]")
        "list< date >"
      else if (tn == "java.time.Period")
        "period"
      else if (tn == "java.time.ZonedDateTime")
        "datetimetz"
      else if (tn == "java.time.ZoneId")
        "string"
      else if (tn == "java.time.DayOfWeek")
        "int(0,6)"
      else if (tn == "java.time.YearMonth")
        "string"
      else if (tn == "java.lang.String")
        "string"
      else if (tn == "java.net.URI")
        "string"
      else if (tn == "boolean")
        "boolean"
      else if (tn.startsWith("com.opengamma")) {
        addPendingProcessingTypes(t)
        validAlfaTypeName(normalizeClassName(t))
      }
      else if (tn.startsWith("com.google.common.collect.ImmutableSortedMap")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"map< ${toAlfaTypeName(d.head)}, ${toAlfaTypeName(d.last)} >"
      }
      else if (tn.startsWith("com.google.common.collect.ImmutableMap")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"map< ${toAlfaTypeName(d.head)}, ${toAlfaTypeName(d.last)} >"
      }
      else if (tn.startsWith("java.util.Map<")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"map< ${toAlfaTypeName(d.head)}, ${toAlfaTypeName(d.last)} >"
      }
      else if (tn.startsWith("com.google.common.collect.ImmutableList")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"list< ${toAlfaTypeName(d.head)} >"
      }
      else if (tn.startsWith("com.google.common.collect.ImmutableSet")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"set< ${toAlfaTypeName(d.head)} >"
      }
      else if (tn.startsWith("com.google.common.collect.ImmutableSortedSet")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"set< ${toAlfaTypeName(d.head)} >"
      }
      else if (tn.startsWith("java.util.List")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"list< ${toAlfaTypeName(d.head)} >"
      }
      else if (tn.startsWith("java.util.Set<")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"set< ${toAlfaTypeName(d.head)} >"
      }
      else if (tn == "java.util.Set") {
        null
      }
      else if (tn.startsWith("java.util.OptionalDouble")) {
        "double?"
      }
      else if (tn.startsWith("java.util.OptionalInt")) {
        "int?"
      }
      else if (tn.startsWith("java.util.Optional<")) {
        val d = t.asInstanceOf[ParameterizedType].getActualTypeArguments
        s"${toAlfaTypeName(d.head)}?"
      }
      else if (t.isInstanceOf[TypeVariable[_]]) {
        null
      }
      else if (tn.startsWith("java.lang.Class")) {
        "$udtName"
      }
      else if (tn.startsWith("java.lang.Object")) {
        null
      }
      else if (tn.startsWith("java.io.File")) {
        null
      }
      else if (tn.startsWith("java.util.function")) {
        null
      }
      else if (t.isInstanceOf[WildcardType]) {
        val wc = t.asInstanceOf[WildcardType]
        wc.getTypeName
      }
      else
        throw new RuntimeException()

    alfaType
  }


}
