package com.schemarise.alfa.runtime;

import java.util.stream.Stream;

/**
 * Base interface for all Builder implementations in Alfa objects
 */
public interface Builder {

    /**
     * Construct an immutable instance of the object represented by the data in this newBuilder
     *
     * @param <T> Class representing the target value
     * @return An immutable object
     */
    <T extends AlfaObject> T build();

    /**
     * Generic method to assign a value to the newBuilder's field
     *
     * @param fieldName Field name of the field being modified
     * @param val       Value to assign to the field. AlfaRuntimeException may be thrown if values
     *                  not compatible.
     */
    void modify(String fieldName, Object val);


    /**
     * Generic method to get current value of a builder
     *
     * @param fieldName Field name of the field being accessed
     * @return Field value
     */
    Object get(String fieldName);

    TypeDescriptor descriptor();

    default <T extends AlfaObject> void applyStreamingAsserts(Stream<T> records, java.util.Set<java.lang.String> excludeAsserts) {
        // force stream to be read
        records.count();
    }
}
