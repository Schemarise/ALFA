package com.schemarise.alfa.runtime;

/***
 * Base interface for a external Alfa object implementation.
 * Any Java class that needs to be used as a `native` type in Alfa, needs to implement this interfasce.
 */
public interface ExternalAlfaObject extends AlfaObject {
//    default boolean isNative() { return true; }

    String encodeToString();
}
