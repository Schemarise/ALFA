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

import com.schemarise.alfa.compiler.antlr.AlfaParser.{BracesExpressionContext, ListExpressionContext, LiteralExpressionContext, NewExpressionContext, ValueMapContext, ValueMapEntryContext}
import com.schemarise.alfa.compiler.ast.model.{IToken, IdentifiableNode, NodeVisitor}
import com.schemarise.alfa.compiler.ast.model.expr.IExpression
import com.schemarise.alfa.compiler.{AlfaInternalException, Context}
import com.schemarise.alfa.compiler.ast._
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.model.graph.NodeIdentity
import com.schemarise.alfa.compiler.ast.model.types.Nodes
import com.schemarise.alfa.compiler.ast.nodes.datatypes.{DataType, UdtDataType}
import com.schemarise.alfa.compiler.utils.{LexerUtils, TokenImpl}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object Field {
  def of(name: String, dataType: DataType, parent: BaseNode, isSynthetic: Boolean = false): Field = {
    val e = TokenImpl.empty
    val f = new Field(e, NodeMeta.empty, false, StringNode(e, name), dataType, None, None, isSynthetic)
    f.parent = parent
    f
  }

  var separator = ""
}

class Field(val location: IToken = TokenImpl.empty,
            val rawNodeMeta: NodeMeta = NodeMeta.empty,
            val isConst: Boolean = false,
            val nameNode: StringNode,
            val declDataType: DataType,
            enumLexicalNode: Option[StringNode] = None,
            rawExpression: Option[IExpression] = None,
            val isSynthetic: Boolean = false
           )
  extends BaseNode with ResolvableNode with TemplateableNode
    with DocumentableNode
    with TraversableNode with IdentifiableNode with IField {
  private var _annotations = rawNodeMeta.annotations

  override def nodeType: Nodes.NodeType = Nodes.Field

  if (nameNode.text.trim.length == 0)
    throw new AlfaInternalException("Empty field name")

  override def resolvableInnerNodes() = {
    asSeq(rawNodeMeta, _dataTypeNode) ++ _annotations
  }

  override def enumLexical = if (enumLexicalNode.isDefined) Some(enumLexicalNode.get.text) else None

  override def name = nameNode.text

  override def dataType = _dataTypeNode

  def declaredAsDataType = _declDataTypeNode

  override def expression: Option[IExpression] = expressionNode

  def hasExpression = rawExpression.isDefined

  def expressionNode = {
    assertPreResolved(None)

    // can only access expresion node after field resolve
    assertResolved(None)

      None
  }

  override def hasNonLiteralExpression: Boolean = {
    false
  }

  def hasLiteralValue = {
    false
  }

  private var _dataTypeNode: DataType = declDataType
  private var _declDataTypeNode: DataType = declDataType

  override protected def resolve(ctx: Context): Unit = {
    // So we preresolve annotations at the UDT level
    _annotations = localAndUdtLevelAnnotations(ctx)
    _annotations.foreach(a => a.startPreResolve(ctx, this))

    super.resolve(ctx)
  }

  override protected def postResolve(ctx: Context): Unit = {
    if (rawExpression.isDefined) {
      // field exp can reference other fields
      ctx.registry.pushUdtFields(ctx, this.locateUdtParent)
      ctx.registry.popUdtFields()
    }

    super.postResolve(ctx)
  }

  override def preResolve(ctx: Context): Unit = {
    super.preResolve(ctx)
    _declDataTypeNode = _dataTypeNode
    if (!_dataTypeNode.hasErrors)
      _dataTypeNode = _dataTypeNode.unwrapTypedef

    val skipWarning = isSynthetic

    if (!skipWarning && !ctx.shouldIgnoreWarnings)
      SynthNames.assertNameWarnings(ctx, nameNode.location, name)
  }

  override def toString: String = {
    val c = if (isConst) "const" else ""

    val startDoc = rawNodeMeta.topDocsToString("")
    val ann = NodeMeta.annotationsString(annotations, "")
    val sameLnDoc = rawNodeMeta.samelineDocsToString()

    val nn = nameNode.text
    val fn = LexerUtils.validAlfaIdentifier(nn)

    val lex = if (enumLexical.isDefined) "( \"" + enumLexical.get + "\" )" else ""

    val dt = if (_dataTypeNode.isVoid()) {
      ""
    } else if (declDataType.isEncKey()) {
      " : " + declDataType.toString
    }
    else {
      " : " + _dataTypeNode.toString
    }

    val end = if (startDoc.trim.length > 0) "\n" else ""
    startDoc + c + ann + fn + lex + dt + sameLnDoc + Field.separator + end
  }

  override def templateInstantiate(resolveCtx: Context, templateArgs: Map[String, DataType]): Field = {
    val dt = _dataTypeNode.templateInstantiate(resolveCtx, templateArgs).asInstanceOf[DataType]
    new Field(location, rawNodeMeta, isConst, nameNode, dt, enumLexicalNode, rawExpression)
  }

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      _dataTypeNode.traverse(v)
      v.exit(this)
    }
  }

  def nodeId = NodeIdentity(getClass.getSimpleName, nameNode.text)


  def asFieldRef: FieldOrFieldRef = {
    new FieldOrFieldRef(this)
  }

  override def docs = rawNodeMeta.docs

  def resolvedNodeMeta(): NodeMeta = {
    rawNodeMeta.withAlternateAnnotations(_annotations)
  }

  def annotations: Seq[Annotation] = _annotations

  private def localAndUdtLevelAnnotations(ctx: Context): Seq[Annotation] = {
    val r = rawNodeMeta.annotations

    val udtAnns = new  ListBuffer[Annotation]

    if (hasUdtParent && !locateUdtParent.annotationsMap.isEmpty) {
      val udtParent = locateUdtParent
      val fieldAnns = udtParent.annotationsMap.filter( e => e._1.fullyQualifiedName == IAnnotation.Meta_Field_Annotations ).values.headOption

      if ( fieldAnns.isDefined ) {
        val ann = fieldAnns.get.asInstanceOf[Annotation]

        val annExp = ann.valueCtx.get

        val es = annExp.namedExpression().get(0).expr.asInstanceOf[BracesExpressionContext].children.asScala.filter( _.isInstanceOf[ValueMapContext])

        es.foreach( exp => {
          val vm = exp.asInstanceOf[ValueMapContext].valueMapEntry()

          vm.asScala.foreach( vme => {
            val tgtAnns = vme.entrykey.asInstanceOf[ListExpressionContext].expressionSequence().expressionUnit().asScala.
              filter( e => e.isInstanceOf[NewExpressionContext]).
              map( e => {
                val ne = e.asInstanceOf[NewExpressionContext]
                new Annotation( namespace=udtParent.namespaceNode, nameNode = StringNode.create(ne.udtName.idOnly().id.getText), valueCtx = Some( ne.args ) )
              } )

            val fields = vme.entryvalue.asInstanceOf[ListExpressionContext].expressionSequence().expressionUnit().asScala.
              map( e => e.asInstanceOf[LiteralExpressionContext].literal().idOnly().id.getText)

            if (fields.contains( name ) ) {
              tgtAnns.foreach( an => udtAnns.append(an) )
            }
          })
        })

      }
    }
    r ++ udtAnns
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Field]

  override def equals(other: Any): Boolean = other match {
    case that: Field =>
      (that canEqual this) &&
        nameNode == that.nameNode &&
        declDataType == that.declDataType
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(nameNode, declDataType)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def docNodes: Seq[IDocumentation] = rawNodeMeta.docs
}
