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

//    /**
//     * Is this an Alfa Record object
//     * @return true if this is a Record
//     */
//    default boolean isRecord() {
//        return false;
//    }
//
//    /**
//     * Is this an Alfa Key object
//     * @return true if this is a Key
//     */
//    default boolean isKey() {
//        return false;
//    }
//
//    /**
//     * Is this an Alfa Union object
//     * @return true if this is a Union
//     */
//    default boolean isUnion() {
//        return false;
//    }
//
//    /**
//     * Is this an Alfa Enum object
//     * @return true if this is a Enum
//     */
//    default boolean isEnum() {
//        return false;
//    }
//
//    /**
//     * Is this an Alfa Trait object
//     * @return true if this is a Trait
//     */
//    default boolean isTrait() {
//        return false;
//    }
//
//    /**
//     * Is this an Alfa Entity object
//     * @return true if this is a Entity
//     */
//    default boolean isEntity() {
//        return false;
//    }
//
//    /**
//     * Is this an alfa native object implemented outside the Alfa runtime
//     * @return true if this is a native object
//     */
//    default boolean isNative() { return false; }
}
