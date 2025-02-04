package com.schemarise.alfa.runtime;

public abstract class DefaultExternalAlfaObject implements ExternalAlfaObject {
    private final String value;

    public DefaultExternalAlfaObject(String s) {
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
