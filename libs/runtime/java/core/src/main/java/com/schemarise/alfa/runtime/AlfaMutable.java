package com.schemarise.alfa.runtime;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A mutable value is rarely used, likely temporary.
 * Field values are held in a map and created on demand, while
 * the generated MutableImpl classes present JavaBean style interface.
 * <p>
 * This enables nested objects to be accessed with getA().getB().getC() without
 * fear of null pointer, as the value will be created on demand if required.
 */
public interface AlfaMutable {
    java.util.Map<String, Object> _data();

    default void _set(String field, Object val) {
        _data().put(field, val);
    }

    default <T> T _get(String field, Supplier<T> defaultValue) {
        Function<? super String, ?> fn = (f) -> defaultValue.get();
        return (T) _data().computeIfAbsent(field, fn);
    }

    default void _addToList(String field, Object o) {
        var l = _get(field, () -> new ArrayList());
        l.add(o);
    }

    default void _addAllToList(String field, List all) {
        var l = _get(field, () -> new ArrayList());
        l.addAll(all);
    }

    default void _addToSet(String field, Object o) {
        var l = _get(field, () -> new ArrayList());
        l.add(o);
    }

    default void _addAllToSet(String field, Set all) {
        var l = _get(field, () -> new HashSet());
        l.addAll(all);
    }

    default void _put(String field, int k, int v) {
        var l = _get(field, () -> new HashMap());
        l.put(k, v);
    }

    default void _putAll(String field, Map all) {
        var l = _get(field, () -> new HashMap());
        l.putAll(all);
    }
}
