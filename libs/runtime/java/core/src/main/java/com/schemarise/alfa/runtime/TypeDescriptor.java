package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Expression;
import schemarise.alfa.runtime.model.IDataType;
import schemarise.alfa.runtime.model.ModifierType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class contains metadata and utilities that aide generic algorithms to be applied to process
 * data for the class this descriptor is representing.
 * For internal use by the encoder/decoder and other utilities.
 */
public interface TypeDescriptor {
    /**
     * For internal use by the encoder/decoder classes
     */
    <T extends AlfaObject> Optional<BiConsumer<T, DataConsumer>> getFieldSupplier(String fieldName);


    Optional<Map<String, Expression>> getAnnotations();

    /**
     * For internal use by the encoder/decoder classes
     */
    Optional<BiConsumer<Builder, DataSupplier>> getFieldConsumer(String fieldName);

    /**
     * For internal use by the encoder/decoder classes
     */
    <T extends AlfaObject> java.util.Map<String, FieldMeta<T>> getAllFieldsMeta();

    List<String> getAllFieldNames();

    /**
     * For internal use by the encoder/decoder classes
     */
    schemarise.alfa.runtime.model.UdtDataType getUdtDataType();

    /**
     * For internal use by the encoder/decoder classes
     */
    Optional<TypeDescriptor> getEntityKeyModel();

    /**
     * For internal use by the encoder/decoder classes
     */
    boolean hasAbstractTypeFieldsInClosure();

    /**
     * Does the object support toBuilder() to create a mutable $Builder object instance from an immutable concrete instance
     *
     * @return
     */
    boolean convertableToBuilder();

    /**
     * For internal use by the encoder/decoder classes
     */
    boolean hasBuilder();

    /**
     * Which field who's type is a union, contains a field by the given name
     *
     * @param name target union field
     * @return
     */
    Optional<String> getFieldContainingNestedUnionField(String name);

    /**
     * For internal use by the encoder/decoder classes
     */
    Builder builder();

    /**
     * For internal use by the encoder/decoder classes
     */
    Builder builder(IBuilderConfig cc);

    default String fieldIdName(int id) {
        return "";
    }

    java.util.Set<String> getImmediateDescendants();

    java.util.Set<String> getAllDescendants();

    List<String> getFieldAssignableToTypeName(String toFullyQualifiedName);

    java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> getAsserts();

    default Set<ModifierType> getModifiers() {
        return Collections.emptySet();
    }

    default String getChecksum() {
        return "";
    }

    default java.util.Optional<java.lang.String> getModelId() {
        return Optional.empty();
    }

}
