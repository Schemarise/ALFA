package com.schemarise.alfa.runtime.codec;

import java.util.Set;

public interface IMapBasedRecord {
    String getFullName();

    Set<String> getFields();

    Object get(String fieldName);
}
