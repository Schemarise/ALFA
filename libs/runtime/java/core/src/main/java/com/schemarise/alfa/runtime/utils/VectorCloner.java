package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.DataConsumer;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.NoOpDataConsumer;
import schemarise.alfa.runtime.model.IDataType;
import schemarise.alfa.runtime.model.ListDataType;
import schemarise.alfa.runtime.model.MapDataType;
import schemarise.alfa.runtime.model.SetDataType;

import java.util.*;
import java.util.function.BiConsumer;

public class VectorCloner extends NoOpDataConsumer {
    private Stack<Object> entries = new Stack<>();

    public static <T> List<T> immutableList(IBuilderConfig bc, BiConsumer<List<T>, DataConsumer> supp, List<T> f1) {

        // if not asserting mandatory, there can be null fields
        if (f1 == null)
            return f1;

        if (!bc.shouldCloneCollectionsOnBuild()) {
            return Collections.unmodifiableList(f1);
        }

        VectorCloner consumer = new VectorCloner();
        supp.accept(f1, consumer);
        List<T> l = (List<T>) consumer.entries.pop();
        return l;
    }

    public static <T> Set<T> immutableSet(IBuilderConfig bc, BiConsumer<Set<T>, DataConsumer> supp, Set<T> f1) {
        // if not asserting mandatory, there can be null fields
        if (f1 == null)
            return f1;

        if (!bc.shouldCloneCollectionsOnBuild()) {
            return Collections.unmodifiableSet(f1);
        }

        VectorCloner consumer = new VectorCloner();
        supp.accept(f1, consumer);
        Set<T> l = (Set<T>) consumer.entries.pop();
        return l;
    }

    public static <K, V> Map<K, V> immutableMap(IBuilderConfig bc, BiConsumer<Map<K, V>, DataConsumer> supp, Map<K, V> f1) {
        // if not asserting mandatory, there can be null fields
        if (f1 == null)
            return f1;

        if (!bc.shouldCloneCollectionsOnBuild()) {
            return Collections.unmodifiableMap(f1);
        }

        VectorCloner consumer = new VectorCloner();
        supp.accept(f1, consumer);
        Map<K, V> l = (Map<K, V>) consumer.entries.pop();
        return l;
    }

    private boolean isVector(IDataType t) {
        return t instanceof ListDataType || t instanceof MapDataType || t instanceof SetDataType;
    }

    @Override
    public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        List result;

        IDataType ct = dt.getComponentType();

        if (isVector(ct)) {
            result = new ArrayList(v.size());
            v.forEach(e -> {
                elementConsumer.accept(e, this);
                result.add(entries.pop());
            });
        } else {
            result = new ArrayList(v);
        }

        entries.push(Collections.unmodifiableList(result));
    }

    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        Map result;

        IDataType ct = dt.getValueType();

        if (isVector(ct)) {
            result = new LinkedHashMap(v.size());
            v.entrySet().forEach(e -> {
                valueConsumer.accept(e.getValue(), this);
                result.put(e.getKey(), entries.pop());
            });
        } else {
            result = new LinkedHashMap(v);
        }

        entries.push(Collections.unmodifiableMap(result));
    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        Set result;

        IDataType ct = dt.getComponentType();

        if (isVector(ct)) {
            result = new HashSet(v.size());
            v.forEach(e -> {
                elementConsumer.accept(e, this);
                result.add(entries.pop());
            });
        } else {
            result = new HashSet(v);
        }

        entries.push(Collections.unmodifiableSet(result));
    }
}
