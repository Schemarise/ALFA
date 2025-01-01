package com.schemarise.alfa.compiler.ast.nodes

import com.schemarise.alfa.compiler.ast.model.{IDocumentation, IStatement, IToken}
import com.schemarise.alfa.compiler.ast.model.expr.{IBlockExpression, IExpression, IObjectExpression}
import com.schemarise.alfa.compiler.ast.model.types.{IDataType, IUdtDataType}

object Expression {
  val DollarKey = "$key"
}

class Expression extends IExpression with IObjectExpression with IBlockExpression {

  override def dataType: IDataType = ???

  override def docs: Seq[IDocumentation] = ???

  override val location: IToken = ???

  override def value: Map[String, IExpression] = ???

  override def udtDataType: IUdtDataType = ???

  override val statements: Seq[IStatement] = Seq.empty
}
