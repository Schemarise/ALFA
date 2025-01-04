package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.IUdtDataType
import com.schemarise.alfa.compiler.ast.model.{IUdtBaseNode, NoOpNodeVisitor, NodeVisitMode}

import java.util

class ReferencesTraitVisitor(visited: util.HashSet[String]) extends NoOpNodeVisitor {
  var hasTraitRef = false

  override def enter(e: IUdtDataType): Mode = {
    if (e.udt.isTrait) {
      hasTraitRef = true
      NodeVisitMode.Break
    }
    else {

      if (!visited.contains(e.fullyQualifiedName)) {
        visited.add(e.fullyQualifiedName);
        e.udt.traverse(this)
      }

      NodeVisitMode.Continue
    }
  }
}

object ReferencesTraitVisitor {
  def hasTraitRef(udt: IUdtBaseNode): Boolean = {
    val v = new ReferencesTraitVisitor(new util.HashSet[String]())
    udt.traverse(v)
    return v.hasTraitRef
  }
}