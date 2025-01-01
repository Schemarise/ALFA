package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.utils.stream.MultistreamResults;

import java.util.Collections;
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

//    public abstract class EntityBuilder extends Builder {
//        protected EntityBuilder(RuntimeContext ctx) {
//            super(ctx);
//        }
//    }

    /**
     * Generic method to get current value of a builder
     *
     * @param fieldName Field name of the field being accessed
     * @return Field value
     */
    Object get(String fieldName);

    TypeDescriptor descriptor();

//    default <T extends AlfaObject> List<String> applyStreamingAsserts(Stream<T> records) {
//        // force stream to be read
//        long c = records.count();
//        return Collections.emptyList();
//    }


    default <T extends AlfaObject> void applyStreamingAsserts(Stream<T> records, java.util.Set<java.lang.String> excludeAsserts) {
        // force stream to be read
        records.count();
    }

//    /**
//     * Interface representing an Entity's newBuilder class.
//     * @param <K> Type of key for this Entity
//     */
//    interface EntityBuilder<K> {
//        /**
//         * Method to assign a key to this entity. Note this is not called setKey to avoid
//         * a potential conflict with a field named 'Key' and having a method named setKey().
//         * @param key Key to be set for this entity
//         * @return The current newBuilder instance
//         */
//        Builder key(K key );
//
//        K assignedKey();
//    }
}
