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
package com.schemarise.alfa.compiler.ast.antlrvisitors

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser.{AnnotationContext, AnnotationsContext}
import com.schemarise.alfa.compiler.ast.nodes.{Annotation, NamespaceNode}

class AnnotationsVisitor(resolveCtx: Context, namespace: NamespaceNode) extends WithContextVisitor[Seq[Annotation]](resolveCtx) {
  private val annVisitor = new AnnotationVisitor
  //  private val annEntriesVisitor = new AnnotationEntriesVisitor
  //  private val annEntryVisitor = new AnnotationEntryVisitor

  override def visitAnnotations(ctx: AnnotationsContext): Seq[Annotation] = {
    j2sNoParseExcpStream(ctx.annotation()).map(annVisitor.visitAnnotation(_))
  }

  class AnnotationVisitor extends WithContextVisitor[Annotation](resolveCtx) {
    override def visitAnnotation(ctx: AnnotationContext): Annotation = {
      val name = readStringNode(ctx.annName)

      val vobj =
        if (ctx.namedExpressionSequence() == null) None
        else
          Some(ctx.namedExpressionSequence())

      new Annotation(
        readToken(ctx).appendAndCreate(name.location),
        namespace,
        name,
        vobj
      )
    }
  }

  //  class AnnotationEntriesVisitor extends AlfaWithContextVisitor[Seq[AnnotationEntry]](resolveCtx) {
  //    override def visitAnnotationValuesMap(ctx: AnnotationValuesMapContext): Seq[AnnotationEntry] = {
  //      if ( ctx == null )
  //        Seq.empty
  //      else
  //        j2sStream(ctx.annotationValueMapEntry()).map(annEntryVisitor.visitAnnotationValueMapEntry(_))
  //    }
  //  }
  //
  //  class AnnotationEntryVisitor extends AlfaWithContextVisitor[AnnotationEntry](resolveCtx)  {
  //    override def visitAnnotationValueMapEntry(ctx: AnnotationValueMapEntryContext): AnnotationEntry = {
  //      val name = readStringNode(ctx.ID())
  //      new AnnotationEntry(readToken(ctx), name, ctx.value() )
  //    }
  //  }

}

