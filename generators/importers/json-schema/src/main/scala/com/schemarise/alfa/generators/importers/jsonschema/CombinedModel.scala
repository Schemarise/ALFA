package com.schemarise.alfa.generators.importers.jsonschema

import com.schemarise.alfa.compiler.utils.LexerUtils
import org.everit.json.schema.{CombinedSchema, ObjectSchema, ReferenceSchema}

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class CombinedModel(rootSchema: CombinedSchema, accessPrefix: String) {
  private val combinedSchemaStack = new util.Stack[Criterion]()

  val outputSchema = flattenObjectSchemas(rootSchema)
  val outputCriteria = combinedSchemaStack.peek()

  private def flattenObjectSchemas(cs: CombinedSchema): ObjectSchema = {

    combinedSchemaStack.push(new Criterion(cs.getCriterion, accessPrefix))

    var os1 = cs.getSubschemas.asScala.
      filter(z => z.isInstanceOf[ObjectSchema] || (z.isInstanceOf[ReferenceSchema] && z.asInstanceOf[ReferenceSchema].getReferredSchema.isInstanceOf[ObjectSchema])).
      map(z => {
        if (z.isInstanceOf[ReferenceSchema])
          z.asInstanceOf[ReferenceSchema].getReferredSchema.asInstanceOf[ObjectSchema]
        else
          z.asInstanceOf[ObjectSchema]
      })


    // when all, combine the object schemas into 1
    if (cs.getCriterion == CombinedSchema.ALL_CRITERION) {
      val builder = ObjectSchema.builder()

      os1.foreach(e => {
        e.getPropertySchemas.asScala.foreach(ex =>
          builder.addPropertySchema(ex._1, ex._2)
        )
        e.getRequiredProperties.asScala.foreach(x => builder.addRequiredProperty(x))
      })

      os1 = Seq(builder.build())
    }

    if (cs.getCriterion == CombinedSchema.ONE_CRITERION || cs.getCriterion == CombinedSchema.ANY_CRITERION) {
      os1.foreach(e => {
        combinedSchemaStack.push(new Criterion(CombinedSchema.ALL_CRITERION, accessPrefix))
        e.getRequiredProperties.forEach(p => combinedSchemaStack.peek().addField(p, true))
        val cr = combinedSchemaStack.pop()
        combinedSchemaStack.peek().addCriterion(cr)
      })
    }

    os1.foreach(e => {
      val reqd =
        // get reqd from all schemas at this level as reqd can be defined separately
        if (cs.getCriterion == CombinedSchema.ALL_CRITERION)
          os1.map(e =>
            e.getRequiredProperties.asScala
          ).flatten.toSet
        else
          e.getRequiredProperties.asScala.toSet

      // nest within an all
      if (combinedSchemaStack.size() > 1)
        combinedSchemaStack.push(new Criterion(CombinedSchema.ALL_CRITERION, accessPrefix))

      // Add the fields
      e.getPropertySchemas.keySet().forEach(k => {
        combinedSchemaStack.peek().addField(k, reqd.contains(k))
      })

      // add any criteria extracted
      if (combinedSchemaStack.size() > 1) {
        val cr = combinedSchemaStack.pop()
        combinedSchemaStack.peek().addCriterion(cr)
      }
    })

    // extract nested criterias
    val os2 = cs.getSubschemas.asScala.filter(_.isInstanceOf[CombinedSchema]).map(e => {
      val nestedOs = flattenObjectSchemas(e.asInstanceOf[CombinedSchema])
      val p = combinedSchemaStack.pop()
      combinedSchemaStack.peek().addCriterion(p)
      nestedOs
    })

    val oss = os1 ++ os2
    val propertySchemas = oss.map(e => e.getPropertySchemas.asScala.seq).flatten
    val builder = ObjectSchema.builder()

    // build an object representing the combined schema
    propertySchemas.foreach(e => {
      builder.addPropertySchema(e._1, e._2)
    })

    if (cs.getCriterion == CombinedSchema.ALL_CRITERION) {
      oss.foreach(ss => {
        ss.getRequiredProperties.forEach(p => builder.addRequiredProperty(p))
      })
    }

    builder.build()
  }

  class Criterion(val criterion: CombinedSchema.ValidationCriterion, accessPrefix: String) {
    private val fields = new util.HashMap[String, Boolean]()
    private val nested = new ListBuffer[Criterion]()

    def addCriterion(c: Criterion) = {
      // only add if any data available
      if (c.fields.size() > 0 || c.nested.size > 0) {

        if (c.fields.size() == 0 &&
          c.nested.size == 1 && c.criterion == CombinedSchema.ALL_CRITERION && c.nested.head.criterion == CombinedSchema.ALL_CRITERION) {
          nested += c.nested.head
        }
        else
          nested += c
      }
    }

    def addField(n: String, reqd: Boolean) = {
      fields.put(n, reqd)
    }

    def getFields() = fields.asScala

    def getNestedCriterion() = nested.toList

    def fmtString(indentStr: String): String = {
      val cx = nested.map(c => c.fmtString(indentStr + "    ")).mkString("")

      val cs = if (nested.size == 0)
        ""
      else
        s"""
           |${indent(cx, "    ")}""".stripMargin

      val fs = if (fields.size() > 0)
        s"""
           |    ${fields.asScala.map(e => e._1 + ":" + e._2).mkString(" ")}""".stripMargin
      else ""

      s"""
         |${criterion} {$fs$cs
         |}
       """.stripMargin
    }

    def toAssertText(mandatoryFieldNames: Seq[String], isRoot: Boolean = true): String = {

      val fieldConds =
        if (!isRoot && fields.size() > 0) {
          fields.asScala.filter(f => f._2 && !mandatoryFieldNames.contains(f._1)).map(e => {
            val f = LexerUtils.validAlfaIdentifier(e._1)
            s"isSome(${accessPrefix + f})"
          }).mkString(" && ")
        }
        else
          ""

      val nestStr =
        if (nested.size > 0) {
          val cond =
            if (criterion == CombinedSchema.ALL_CRITERION) {
              val d = nested.map(_.toAssertText(mandatoryFieldNames, false)).filter(_ != "").map(w => s"($w)").mkString(" && ")
              d
            }
            else if (criterion == CombinedSchema.ONE_CRITERION) {
              val c = nested.map(_.toAssertText(mandatoryFieldNames, false)).filter(_ != "")

              // negate all others
              val d = c.map(w => {
                val others = c.filter(_ != w).map(a => (s"!( $a )")).mkString(" && ")
                val append = if (others.size > 0) s" && $others" else ""
                s"($w$append)"
              }).mkString(" || ")
              d
            }
            else {
              val d = nested.map(_.toAssertText(mandatoryFieldNames, false)).filter(_ != "").map(w => s"($w)").mkString(" || ")
              d
            }

          if (isRoot && cond.size > 0) {
            val msg = cond.replace("isSome", "").
              replace("||", "or").
              replace("&&", "and").
              replace('(', ' ').
              replace(')', ' ').
              replace("  ", " ")
            s"""
               |        if (! ( $cond ) )
               |            raise error(Completeness, "Required field/combination is not set - $msg")
               """.stripMargin
          } else
            cond
        }
        else
          ""

      val res = fieldConds ++ nestStr

      res
    }

    override def toString: String = fmtString("")

    private def indent(s: String, indent: String) =
      indent + s.replaceAll("\n", s"\n$indent")
  }

}
