package com.schemarise.alfa.runtime;

/**
 * Interface representing an object that is capable of converting itself to a byte[] and creating an object T of itself.
 *
 * @param <T> The type of object, which can be any Alfa supported type.
 */
public interface CustomEncodedType<T> extends AlfaObject {
    byte[] getEncodedBytes();

    T getValue(IBuilderConfig builderConfig);
}
