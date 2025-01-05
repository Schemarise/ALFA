package com.schemarise.alfa.runtime;

/**
 * Class representing an encrypted< T > object. T can be any Alfa supported type which is securely encrypted
 * by the implementation in BuilderConfig. Only BuilderConfig.getRuntime() has the keys and implementation
 * to encrypt/decrypt the data.
 *
 * @param <T> The type of object being encrypted.
 */
public interface Encrypted<T> extends CustomEncodedType<T> {
}
