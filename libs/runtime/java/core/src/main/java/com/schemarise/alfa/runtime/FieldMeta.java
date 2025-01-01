package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Expression;
import schemarise.alfa.runtime.model.IDataType;
import schemarise.alfa.runtime.model.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class FieldMeta<T> {
    private final Field field;
    private final Optional<BiConsumer<T, DataConsumer>> fieldTypeSupplier;
    private final Optional<BiConsumer<Builder, DataSupplier>> fieldTypeConsumer;
    private final Optional<Map<String, Expression>> annotations;
    private final Optional<BiConsumer<Builder, DataSupplier>> fieldConsumer;
    private final Optional<BiConsumer<T, DataConsumer>> fieldSupplier;

    public FieldMeta(Optional<BiConsumer<T, DataConsumer>> supplier,
                     Optional<BiConsumer<Builder, DataSupplier>> consumer,
                     Optional<BiConsumer<T, DataConsumer>> fieldTypeSupplier,
                     Optional<BiConsumer<Builder, DataSupplier>> fieldTypeConsumer,
                     IDataType type,
                     String name,
                     Optional<java.util.Map<String, Expression>> annotations) {
        this.field = Field.builder().setName(name).setDataType(type).build();
        this.fieldConsumer = consumer;
        this.fieldSupplier = supplier;
        this.fieldTypeSupplier = fieldTypeSupplier;
        this.fieldTypeConsumer = fieldTypeConsumer;
        this.annotations = annotations;
    }

    public FieldMeta(Optional<BiConsumer<T, DataConsumer>> supplier,
                     Optional<BiConsumer<Builder, DataSupplier>> consumer,
                     Optional<BiConsumer<T, DataConsumer>> fieldTypeSupplier,
                     Optional<BiConsumer<Builder, DataSupplier>> fieldTypeConsumer,
                     Field f) {
        this.field = f;
        this.fieldConsumer = consumer;
        this.fieldSupplier = supplier;
        this.fieldTypeSupplier = fieldTypeSupplier;
        this.fieldTypeConsumer = fieldTypeConsumer;
        this.annotations = Optional.empty();
    }

    public Optional<Map<String, Expression>> getAnnotations() {
        return this.annotations;
    }

    public boolean hasAnnotation(String a) {
        return annotations.isPresent() && annotations.get().containsKey(a);
    }

    public Field getField() {
        return field;
    }

    public IDataType getDataType() {
        return field.getDataType();
    }

    public Optional<BiConsumer<Builder, DataSupplier>> getConsumer() {
        return fieldConsumer;
    }

    public Optional<BiConsumer<T, DataConsumer>> getSupplier() {
        return fieldSupplier;
    }

    public Optional<BiConsumer<Builder, DataSupplier>> getFieldTypeConsumer() {
        return fieldTypeConsumer;
    }

    public Optional<BiConsumer<T, DataConsumer>> getFieldTypeSupplier() {
        return fieldTypeSupplier;
    }
}
