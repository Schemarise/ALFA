package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import com.schemarise.alfa.runtime.codec.Converters;
import com.schemarise.alfa.runtime.codec.json.*;
import schemarise.alfa.runtime.model.Assert;
import schemarise.alfa.runtime.model.Expression;
import schemarise.alfa.runtime.model.IDataType;
import schemarise.alfa.runtime.model.UdtDataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class DefaultCustomEncodedType<T> implements CustomEncodedType<T> {
    private Function<DataSupplier, T> supplier;
    private BiConsumer<T, DataConsumer> consumer;
    protected byte[] encodedBytes;

    protected DefaultCustomEncodedType(Function<DataSupplier, T> c, byte[] encodedBytes) {
        this.encodedBytes = encodedBytes;
        supplier = c;
    }

    protected DefaultCustomEncodedType(Converters.SupplierConsumer<T> conv, IBuilderConfig builderConfig, T o) {
        this.supplier = conv.getSupplier();
        this.consumer = conv.getConsumer();

        byte[] b = toBytes(builderConfig, o);
        encodedBytes = encode(builderConfig, b);
    }

    /**
     * Get the byte[] that have been encoded as per encoder implementation
     *
     * @return A byte[] representing the type being encoded
     */
    @Override
    public byte[] getEncodedBytes() {
        return encodedBytes;
    }

    protected abstract byte[] encode(IBuilderConfig builderConfig, byte[] data);

    protected abstract byte[] decode(IBuilderConfig builderConfig, byte[] data);

    private T toObject(IBuilderConfig cc, byte[] b) {
        try {
            InputStream stream = new ByteArrayInputStream(b);
            JsonCodecConfig jwc = JsonCodecConfig.builder().setRuntimeContext(cc.getRuntimeContext()).build();
            DataSupplier w = JsonReader.getInstance().reader(jwc, stream);

            T obj = supplier.apply(w);
            return obj;
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    private byte[] toBytes(IBuilderConfig cc, T under) {
        try {
            JsonCodecConfig jwc = JsonCodecConfig.builder().setRuntimeContext(cc.getRuntimeContext()).build();
            DataConsumer w = JsonWriter.getInstance().writer(jwc);
            this.consumer.accept((T) under, w);
            ByteArrayOutputStream buf = (ByteArrayOutputStream) w.closeAndGetBuffer();

            return buf.toByteArray();
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    /**
     * Decode and get the original value given the newBuilder configuration ( containing implementation to decode the value )
     */
    @Override
    public T getValue(IBuilderConfig builderConfig) {
        byte[] decoded = decode(builderConfig, encodedBytes);
        return toObject(builderConfig, decoded);
    }

    @Override
    public String toString() {
        return "\"" + Base64.getEncoder().encodeToString(encodedBytes) + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultCustomEncodedType<?> that = (DefaultCustomEncodedType<?>) o;
        return Arrays.equals(encodedBytes, that.encodedBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encodedBytes);
    }

    @Override
    public TypeDescriptor descriptor() {
        return model;
    }

    public static TypeDescriptor model = new TypeDescriptor() {

        @Override
        public Builder builder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder builder(IBuilderConfig cc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getImmediateDescendants() {
            return null;
        }

        @Override
        public Set<String> getAllDescendants() {
            return null;
        }

        @Override
        public List<String> getFieldAssignableToTypeName(String toFullyQualifiedName) {
            return null;
        }

        @Override
        public Map<String, Assert> getAsserts() {
            return Collections.emptyMap();
        }

        @Override
        public Optional<Function<AlfaObject, Supplier>> getFieldSupplier(String fieldName) {
            return null;
        }

        @Override
        public Optional<Map<String, Expression>> getAnnotations() {
            return Optional.empty();
        }

        @Override
        public Optional<BiConsumer<Builder, DataSupplier>> getFieldConsumer(String fieldName) {
            return null;
        }

        @Override
        public Optional<TypeDescriptor> getEntityKeyModel() {
            return Optional.empty();
        }

        @Override
        public <T extends AlfaObject> Map<String, FieldMeta<T>> getAllFieldsMeta() {
            return null;
        }

        @Override
        public List<String> getAllFieldNames() {
            return Collections.emptyList();
        }

        @Override
        public UdtDataType getUdtDataType() {
            return null;
        }

        @Override
        public boolean hasAbstractTypeFieldsInClosure() {
            return true;
        }

        @Override
        public boolean convertableToBuilder() {
            return false;
        }

        @Override
        public boolean hasBuilder() {
            return false;
        }

        @Override
        public Optional<String> getFieldContainingNestedUnionField(String name) {
            return Optional.empty();
        }

    };
}