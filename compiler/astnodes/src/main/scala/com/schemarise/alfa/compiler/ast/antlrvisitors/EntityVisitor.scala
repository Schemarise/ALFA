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

import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.antlr.AlfaParser.EntityContext
import com.schemarise.alfa.compiler.ast.{NodeMeta, UdtName}
import com.schemarise.alfa.compiler.ast.nodes._
import com.schemarise.alfa.compiler.ast.nodes.datatypes.UdtDataType
import com.schemarise.alfa.compiler.err.{TopLevelKeyNameConflictWithEntityKey}

class EntityVisitor(resolveCtx: Context, namespace: NamespaceNode, imports: Seq[ImportDef]) extends WithContextVisitor[Entity](resolveCtx) {
  override def visitEntity(ctx: EntityContext): Entity = {
    val token: IToken = readToken(ctx)
    val meta: NodeMeta = readNodeMeta(resolveCtx, namespace, ctx.docAndAnnotations(), ctx.sameline_docstrings())
    val modifiers: Seq[ModifierNode] = readModifierNodes(ctx.modifiers())
    val name: StringNode = readStringNode(ctx.name)
    val versionNo: Option[IntNode] = readOptVersion(ctx.versionMarker())
    val typeParams = readTypeParameters(resolveCtx, ctx.typeParameters(), namespace)
    val includes: Seq[UdtDataType] = readIncludes(resolveCtx, ctx.optIncludesList(), namespace)
    val fields: Seq[FieldOrFieldRef] = readFieldOrFieldRefs(resolveCtx, namespace, ctx.children)

    val anonKeyFields: Seq[FieldOrFieldRef] = readOptionAnonymousKeys(ctx)
    val topLevelKeyRef: Option[UdtDataType] = readOptionKey(ctx)

    val keyName = StringNode(name.location, name.text + "Key")
    var keyRef: Option[UdtDataType] = None
    var key: Option[Key] = None

    var isUnion = ctx.isUnion != null

    if (anonKeyFields.size > 0) {
      keyRef = Some(new UdtDataType(name.location, namespace, keyName, None, None, true))
      val k = new Key(Some(resolveCtx), token, namespace, NodeMeta.empty, modifiers, keyName, typeParams, None,
        None, Seq.empty, anonKeyFields, Some(UdtName(namespace, name)), Seq.empty, Seq.empty, false, imports)

      resolveCtx.registry.registerUdt(k)
      key = Some(k)
    }
    else if (topLevelKeyRef.isDefined) {

      if (topLevelKeyRef.get.fullyQualifiedName.split("\\.").last == keyName.text.split("\\.").last) {
        resolveCtx.addResolutionError(topLevelKeyRef.get, TopLevelKeyNameConflictWithEntityKey, topLevelKeyRef.get.fullyQualifiedName, name.text)
      }
      else {
        keyRef = Some(new UdtDataType(name.location, namespace, keyName, None, None, true))
        val k = new Key(Some(resolveCtx), token, namespace, NodeMeta.empty, modifiers, keyName, typeParams, None,
          None, Seq(topLevelKeyRef.get), anonKeyFields, Some(UdtName(namespace, name)), Seq.empty, Seq.empty, false, Seq.empty)
        key = Some(k)

        if (resolveCtx.registry.getUdt(None, k.asDataType, false).isDefined) {
          val otherK = resolveCtx.registry.getUdt(k.asDataType).get
          resolveCtx.addResolutionError(otherK, TopLevelKeyNameConflictWithEntityKey, otherK.name.fullyQualifiedName, name.text)
        }
        else {
          resolveCtx.registry.registerUdt(k)
        }
      }
    }

    val extendz = readExtends(ctx.optExtends(), namespace)

    val o = new Entity(Some(resolveCtx), token, namespace, meta, modifiers, name, versionNo,
      typeParams, None,
      extendz, includes, fields, keyRef, key, anonKeyFields.size > 0, Seq.empty, Seq.empty, imports, isUnion)

    resolveCtx.registry.registerUdt(o)
  }

  def readOptionKey(ctx: EntityContext): Option[UdtDataType] =
    readOptionUdtDataType(ctx.vKey, namespace)

  def readOptionAnonymousKeys(ctx: EntityContext): Seq[FieldOrFieldRef] = {
    if (ctx.vAnonKey == null)
      List.empty
    else
      readFieldOrFieldRefs(resolveCtx, namespace, ctx.vAnonKey.children)
  }


}
