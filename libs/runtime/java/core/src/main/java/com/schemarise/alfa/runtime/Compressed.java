package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.codec.Converters;

import java.util.function.Function;

/**
 * Class representing an compressed< T > object. T can be any Alfa supported type which is compressed
 * by the implementation in BuilderConfig. Only BuilderConfig.getRuntime() has the implementation
 * to compress and uncompress.
 *
 * @param <T> The type of object being compressed.
 */
public interface Compressed<T> extends CustomEncodedType<T> {
}
