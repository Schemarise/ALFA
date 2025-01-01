package com.schemarise.alfa.runtime;

/**
 * Interface representing an Entity's newBuilder class.
 *
 * @param <K> Type of key for this Entity
 */
public interface EntityBuilder<K> {
    /**
     * Method to assign a key to this entity. Note this is not called setKey to avoid
     * a potential conflict with a field named 'Key' and having a method named setKey().
     *
     * @param key Key to be set for this entity
     * @return The current newBuilder instance
     */
    Builder set$key(K key);

    K assignedKey();
}