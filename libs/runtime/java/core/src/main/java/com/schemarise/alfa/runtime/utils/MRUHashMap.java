package com.schemarise.alfa.runtime.utils;

import java.util.LinkedHashMap;
import java.util.Map;

// https://stackoverflow.com/questions/583852/how-to-implement-a-most-recently-used-cache

public class MRUHashMap<K, V> extends LinkedHashMap<K, V> {

    private static int maxSize;

    public MRUHashMap(int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }
}
