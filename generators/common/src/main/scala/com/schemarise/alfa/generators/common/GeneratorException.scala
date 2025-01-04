package com.schemarise.alfa.generators.common

class GeneratorException(msg: String, inner: Throwable) extends Exception(msg, inner) {
  def this(msg: String) {
    this(msg, null)
  }

  def this() {
    this(null, null)
  }
}

class MissingParameter(msg: String, inner: Exception) extends GeneratorException(msg, inner) {
  def this(m: String) {
    this(m, null)
  }
}

class AlfaHttpException(msg: String, inner: Throwable) extends GeneratorException(msg, inner) {
  def this(msg: String) {
    this(msg, null)
  }
}