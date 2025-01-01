package com.schemarise.alfa.generators.common

import com.schemarise.alfa.compiler.ast.model.NoOpNodeVisitor
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode
import com.schemarise.alfa.compiler.ast.model.types.IEnclosingDataType

class NonConvertibleToBuilderVisitor extends NoOpNodeVisitor {
  var convertableToBuilder = true

  override def enter(e: IEnclosingDataType): Mode = {
    e.whenEncFuture(f => {
      convertableToBuilder = false
    })
    e.whenEncStream(f => {
      convertableToBuilder = false
    })
    super.enter(e)
  }
}