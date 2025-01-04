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

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.LocatableNodeIdentity
import com.schemarise.alfa.compiler.ast.model.types.Nodes.NodeType
import com.schemarise.alfa.compiler.ast.model.types.{IEnclosingDataType, Nodes, UdtType}
import com.schemarise.alfa.compiler.ast.model.types.UdtType.UdtType
import com.schemarise.alfa.compiler.ast.model._
import com.schemarise.alfa.compiler.ast.nodes.datatypes._
import com.schemarise.alfa.compiler.err.{ExpressionError, TransformerError}

class Transformer(resolveCtx: Context, val method: MethodDeclaration, imports: Seq[ImportDef]) extends UdtBaseNode(
  ctx = Some(resolveCtx),
  declaredRawName = StringNode.create("transform"),
  location = method.location,
  rawMethodDeclNodes = Seq(method),
  imports = imports
) with ITransform {

  override protected def createPartConcretizedTemplateableUdt(resolveCtx: Context, params: Option[Seq[TypeParameter]], typeArguments: Map[String, DataType]): UdtBaseNode = ???

  override def udtType: UdtType = UdtType.transform

  override def traverse(v: NodeVisitor): Unit = {
    if (v.enter(this) == NodeVisitMode.Continue) {
      traverseBody(v)
    }
    v.exit(this)
  }

  override def preResolve(ctx: Context): Unit = {
    ctx.registry.registerTransformer(this)
  }


  override def nodeType: NodeType = Nodes.Transform

  override def getMethodDecls(): Map[String, IMethodDeclaration] = Map("transform" -> method)

  override def getMethodSignatures(): Map[String, MethodSignature] = Map("transform" -> method.signature)

  def getTransformerSignature(): IMethodSignature = {
    method.signature
  }

  private def typeSig = {
    method.signature.formals.mapValues(_.dataType.unwrapTypedef).map(_._2) ++ List(method.signature.returnType)
  }

  override def nodeId: LocatableNodeIdentity = {
    val sig = typeSig.mkString(":")
    new LocatableNodeIdentity(getClass.getSimpleName, versionedName.fullyQualifiedName + sig)(versionedName.location)
  }

  override def toString: String = {
    val sb = new StringBuilder

    sb ++= annotationNodes.mkString("", "\n", "")

    if (docs.size > 0)
      sb ++= docs.map("  " + _.toString).mkString("\n/#\n ", "\n", "\n #/")

    //    val fmls = method.signature.formals
    //    val ret = method.signature.returnType

    //    sb ++= s"\ntransform( ${fmls.values.mkString(",")} ) : ${ret.toString} {"

    sb ++= method.toString

    //    sb ++= "\n}"

    sb.toString()
  }
}




