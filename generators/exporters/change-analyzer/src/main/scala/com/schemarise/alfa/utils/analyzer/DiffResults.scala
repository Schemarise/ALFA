package com.schemarise.alfa.utils.analyzer

class DiffResults[T](l: Set[T], r: Set[T]) {
  val deleted = l.filter(e => !r.contains(e))
  val added = r.filter(e => !l.contains(e))
  val unchanged = l.filter(e => r.contains(e))
}
