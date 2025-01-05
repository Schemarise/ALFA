package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.BuilderConfig;
import com.schemarise.alfa.runtime.Compressed;
import com.schemarise.alfa.runtime.DataSupplier;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.codec.Converters;

import java.util.function.Function;

final class DefaultCompressed<T> extends DefaultCustomEncodedType<T> implements Compressed<T> {
    private DefaultCompressed(Function<DataSupplier, T> c, byte[] compressedBytes) {
        super(c, compressedBytes);
    }

    private DefaultCompressed(Converters.SupplierConsumer<T> conv, IBuilderConfig builderConfig, T o) {
        super(conv, builderConfig, o);
    }

    /**
     * Used internally to compress encoded bytes
     */
    @Override
    protected byte[] encode(IBuilderConfig builderConfig, byte[] data) {
        return builderConfig.getRuntimeContext().compress(data);
    }

    /**
     * Used internally to uncompress encoded bytes
     */
    @Override
    protected byte[] decode(IBuilderConfig builderConfig, byte[] data) {
        return builderConfig.getRuntimeContext().uncompress(data);
    }

    /**
     * Create an compressed<T> value.
     * The Converters class has a full list of predefined converters that can be used.
     * Example usage: DefaultCompressed.fromValue(Converters.StringProcessor, BuilderConfig.getInstance(), "7643 7654 2343 4321");
     *
     * @param converter       The converters to use to encode/decode the unencodedObject before being compressed
     * @param builderConfig   The configuration to be used to compress the object
     * @param unencodedObject The object to be encoded
     * @param <T>             The type of object being encoded
     * @return An object where the unencodedObject is compressed internally into a byte[ ]
     * @see com.schemarise.alfa.runtime.codec.Converters
     */
    public static <T> Compressed fromValue(Converters.SupplierConsumer<T> converter, IBuilderConfig builderConfig, T unencodedObject) {
        return new DefaultCompressed<T>(converter, builderConfig, unencodedObject);
    }

    /**
     * For internal use by the encoder/decoder classes
     */
    public static <T> Compressed fromValue(Function<DataSupplier, T> c, byte[] v) {
        return new DefaultCompressed(c, v);
    }

    @Override
    public Object get(String fieldName) {
        throw new IllegalStateException();
    }

}
