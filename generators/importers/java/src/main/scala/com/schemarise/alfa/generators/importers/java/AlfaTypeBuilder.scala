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

package com.schemarise.alfa.generators.importers.java

import java.lang.annotation.{ElementType, Target}
import java.lang.reflect.{Modifier, ParameterizedType}

import com.schemarise.alfa.runtime.AlfaRuntimeException
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.NodeMeta
import com.schemarise.alfa.compiler.ast.model.types.Enclosed
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.types.AnnotationTargetType
import com.schemarise.alfa.compiler.types.AnnotationTargetType.TargetType
import com.schemarise.alfa.compiler.utils.ILogger

class AlfaTypeBuilder(logger: ILogger, ctx: Context, cl: ClassLoader, classNames: Seq[String]) {
  def genAlfaModel() = {
    classNames.filter(_.indexOf("$") == -1).foreach(c => {
      try {
        val z = Class.forName(c, false, cl)

        if (z.isEnum) {
          logger.debug("Generating enum " + c)
          createEnum(z)
        } else if (z.isInterface && !Modifier.isAbstract(z.getModifiers())) {
          // do nothing
          logger.debug("Skipping interface or abstract class " + c)
        }
        else if (z.isAnnotation) {
          logger.debug("Generating annotation " + c)
          createAnnotation(z)
        }
        else {
          logger.debug("Generating record " + c)
          createRecord(z)
        }
      } catch {
        case cnf: Throwable =>
          println("skipping " + c)
      }
    })
  }

  def createEnum(c: Class[_]): Unit = {

    val cn = c.getName

    val fields = c.getFields.array.
      map(m => {
        val n = m.getName

        val f = new Field(nameNode = StringNode.create(n), declDataType = ScalarDataType.voidType)
        new FieldOrFieldRef(f)
      })

    val r = new EnumDecl(nameNode = StringNode.create(c.getName), fieldsNode = fields, imports = Seq.empty)
    ctx.registry.registerUdt(r)
  }


  def createAnnotation(c: Class[_]): Unit = {

    val fields = c.getMethods.array.
      filter(m => m.getReturnType.getTypeName != "java.lang.Class").
      filter(f =>
        !List("equals", "toString", "hashCode", "annotationType").contains(f.getName) && f.getParameterCount == 0).
      map(m => {
        val t = javaToAlfaType(m.getGenericReturnType)
        val n = m.getName

        val f = new Field(nameNode = StringNode.create(n), declDataType = t)
        new FieldOrFieldRef(f)
      })

    val tgts = c.getAnnotations.toSeq.filter(a => {
        a.annotationType() == classOf[Target]
      }).
      map(a => {
        val targets: Seq[TargetType] = a.asInstanceOf[Target].value().toSeq.map(t => {
          t match {
            case ElementType.FIELD => AnnotationTargetType.Field
            case ElementType.METHOD => AnnotationTargetType.Field
            case ElementType.TYPE => AnnotationTargetType.Record
          }
        })
        targets
      }).flatten

    val r = new AnnotationDecl(nameNode = StringNode.create(c.getName), fields = fields, annotationTargets = tgts, imports = Seq.empty)

    ctx.registry.registerUdt(r)
  }

  def getNodeMetaAnnotations(annotations: Array[java.lang.annotation.Annotation]): NodeMeta = {
    NodeMeta.empty
  }

  def createRecord(c: Class[_]): Unit = {
    val fields = c.getMethods.array.
      filter(m => m.getReturnType.getTypeName != "java.lang.Class").
      filter(f => f.getName.startsWith("get") && f.getParameterCount == 0).
      map(m => {
        val t = javaToAlfaType(m.getGenericReturnType)
        val n = m.getName.drop(3)

        val nm = getNodeMetaAnnotations(m.getDeclaredAnnotations)

        val f = new Field(nameNode = StringNode.create(n), declDataType = t, rawNodeMeta = nm)
        new FieldOrFieldRef(f)
      })

    val nm = getNodeMetaAnnotations(c.getDeclaredAnnotations)

    val r = new Record(nameNode = StringNode.create(c.getName), fields = fields, nodeMeta = nm, imports = Seq.empty)

    ctx.registry.registerUdt(r)
  }

  def javaToAlfaType(c: java.lang.reflect.Type): DataType = {

    if (c.isInstanceOf[Class[_]] && c.asInstanceOf[Class[_]].isArray) {
      ListDataType(declComponentType = javaToAlfaType(c.asInstanceOf[Class[_]].getComponentType))()
    }
    else {
      c.getTypeName match {
        case "int" => ScalarDataType.intType
        case "java.lang.Integer" => ScalarDataType.intType

        case "long" => ScalarDataType.longType
        case "java.lang.Long" => ScalarDataType.longType

        case "short" => ScalarDataType.shortType
        case "java.lang.Short" => ScalarDataType.shortType

        case "double" => ScalarDataType.doubleType
        case "java.lang.Double" => ScalarDataType.doubleType

        case "float" => ScalarDataType.doubleType
        case "java.lang.Float" => ScalarDataType.doubleType

        case "java.math.BigDecimal" => ScalarDataType.decimalType

        case "java.lang.String" => ScalarDataType.stringType

        case "java.util.UUID" => ScalarDataType.uuidType

        case "java.util.Date" => ScalarDataType.dateType
        case "java.time.LocalDate" => ScalarDataType.dateType

        case "java.time.LocalDateTime" => ScalarDataType.datetimeType
        case "java.time.Instant" => ScalarDataType.datetimeType

        case "java.time.LocalTime" => ScalarDataType.timeType

        case _ =>
          if (c.isInstanceOf[ParameterizedType]) {
            val pt = c.asInstanceOf[ParameterizedType]
            val paramType = pt.getActualTypeArguments.head

            val rawName = pt.getRawType.getTypeName

            val st = rawName match {
              case "java.util.Optional" =>
                new EnclosingDataType(encType = Enclosed.opt, declComponentType = javaToAlfaType(paramType))

              case "java.util.List" | "java.util.ArrayList" | "java.util.LinkedList" =>
                ListDataType(declComponentType = javaToAlfaType(paramType))()

              case "java.util.Collection" =>
                ListDataType(declComponentType = javaToAlfaType(paramType))()

              case "java.util.Set" =>
                SetDataType(declComponentType = javaToAlfaType(paramType))()

              case "java.util.Map" | "java.util.HashMap" | "java.util.TreeMap" =>
                MapDataType(declKeyType = javaToAlfaType(paramType), declValueType = javaToAlfaType(pt.getActualTypeArguments.last))()

              case _ =>
                throw new AlfaRuntimeException("Unsupported type " + rawName)
            }
            st
          }
          else {
            UdtDataType.fromName(c.getTypeName)
          }
      }
    }
  }
}
