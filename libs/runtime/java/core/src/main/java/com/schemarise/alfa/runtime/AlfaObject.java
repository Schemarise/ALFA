package com.schemarise.alfa.runtime;

/**
 * Base interface implemented by all Alfa Java objects corresponding to user-defined-types in Alfa.
 */
public interface AlfaObject {
    /**
     * Access the TypeDescriptor for this object
     *
     * @return TypeDescriptor for this object
     */
    TypeDescriptor descriptor();

    /**
     * Generic method to get current value of the field
     *
     * @param fieldName Field name of the field being accessed
     * @return Field value
     */
    Object get(String fieldName);

    default void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {

    }
}
