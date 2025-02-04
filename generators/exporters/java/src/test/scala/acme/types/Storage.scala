package acme.types

import com.schemarise.alfa.runtime.{ExternalAlfaObject, TypeDescriptor}

class Storage extends ExternalAlfaObject {
  /**
   * Access the TypeDescriptor for this object
   *
   * @return TypeDescriptor for this object
   */
  override def descriptor(): TypeDescriptor = ???

  override def encodeToString(): String = ???

  override def get(fieldName: String): AnyRef = ???
}
