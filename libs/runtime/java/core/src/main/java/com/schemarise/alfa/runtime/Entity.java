package com.schemarise.alfa.runtime;

import java.util.Optional;

/**
 * Base interface of all generated entity objects
 */
public interface Entity extends AlfaObject {
    /**
     * Retrieves the key of this entity. Returned as optional as it may be a keyless entity
     *
     * @return Optional key of this entity
     */
    Optional<? extends Key> get$key();

//    /**
//     * Returns true to indicate this is an entity
//     * @return true
//     */
//    default boolean isEntity() {
//        return true;
//    }
}
