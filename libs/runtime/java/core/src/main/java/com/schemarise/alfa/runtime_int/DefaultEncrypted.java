package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.BuilderConfig;
import com.schemarise.alfa.runtime.DataSupplier;
import com.schemarise.alfa.runtime.Encrypted;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.codec.Converters;

import java.util.function.Function;

class DefaultEncrypted<T> extends DefaultCustomEncodedType<T> implements Encrypted<T> {
    private DefaultEncrypted(Function<DataSupplier, T> c, byte[] encryptedBytes) {
        super(c, encryptedBytes);
    }

    private DefaultEncrypted(Converters.SupplierConsumer<T> conv, IBuilderConfig builderConfig, T o) {
        super(conv, builderConfig, o);
    }

    /**
     * Used internally to encrypt encoded bytes
     */
    @Override
    protected byte[] encode(IBuilderConfig builderConfig, byte[] data) {
        return builderConfig.getRuntimeContext().encrypt(data);
    }

    /**
     * Used internally to decrypt encoded bytes
     */
    @Override
    protected byte[] decode(IBuilderConfig builderConfig, byte[] data) {
        return builderConfig.getRuntimeContext().decrypt(data);
    }

    /**
     * Create an encrypted<T> value.
     * The Converters class has a full list of predefined converters that can be used.
     * Example usage: DefaultEncrypted.fromValue(Converters.StringProcessor, BuilderConfig.getInstance(), "7643 7654 2343 4321");
     *
     * @param convertor       The converters to use to encode/decode the unencodedObject before being encrypted
     * @param builderConfig   The configuration to be used to encrypt the object
     * @param unencodedObject The object to be encoded
     * @param <T>             The type of object being encoded
     * @return An object where the unencodedObject is encrypted internally into a byte[ ]
     * @see com.schemarise.alfa.runtime.codec.Converters
     */
    public static <T> DefaultEncrypted<T> fromValue(Converters.SupplierConsumer<T> convertor, IBuilderConfig builderConfig, T unencodedObject) {
        return new DefaultEncrypted(convertor, builderConfig, unencodedObject);
    }

    /**
     * For internal use by the encoder/decoder classes
     */
    public static <T> DefaultEncrypted<T> fromValue(Function<DataSupplier, T> c, byte[] v) {
        return new DefaultEncrypted(c, v);
    }

    @Override
    public Object get(String fieldName) {
        throw new IllegalStateException();
    }
}
