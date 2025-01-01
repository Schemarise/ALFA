package com.schemarise.alfa.runtime;

import java.util.Optional;

/**
 * The base class for all Alfa generated enums.
 */
public interface Enum extends AlfaObject {
    //    /**
//     * Returns true to indicate this is an enum
//     * @return true
//     */
//    default boolean isEnum() {
//        return true;
//    }
    default Object get(String b) {
        throw new IllegalStateException("Cannot get on an enum");
    }

    default Optional<String> getLexicalValue() {
        return null;
    }
}
