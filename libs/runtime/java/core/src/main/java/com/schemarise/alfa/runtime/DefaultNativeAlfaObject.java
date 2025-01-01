package com.schemarise.alfa.runtime;

public abstract class DefaultNativeAlfaObject implements NativeAlfaObject {
    private final String value;

    public DefaultNativeAlfaObject(String s) {
        this.value = s;
    }

    @Override
    public String encodeToString() {
        return value;
    }

    @Override
    public Object get(String fieldName) {
        return value;
    }
}
