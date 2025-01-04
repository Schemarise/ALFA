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
package com.schemarise.alfa.compiler.ast.model

trait IDocAndAnnotated extends INode with IDocumented {

  def annotations: Seq[IAnnotation]

  def localAndInheritedAnnotations: Seq[IAnnotation] = annotations

  def annotationsMap: Map[IUdtVersionName, IAnnotation] = {
    annotations.map(a => a.versionedName -> a).toMap
  }

  def annotationValue(annotationName: String, annotationAttribute: String) = {

    val ann = annotations.filter(e => e.versionedName.fullyQualifiedName.equals(annotationName)).headOption
    if (ann.isDefined && ann.get.objectExpression.isDefined) {
      ann.get.objectExpression.get.value.get(annotationAttribute)
    }
    else
      None
  }
}
