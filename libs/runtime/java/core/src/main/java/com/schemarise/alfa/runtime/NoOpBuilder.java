package com.schemarise.alfa.runtime;

public class NoOpBuilder implements Builder {
    @Override
    public <T extends AlfaObject> T build() {
        return null;
    }

    @Override
    public void modify(String fieldName, Object val) {
    }

    public Object get(String fieldName) {
        return null;
    }

    @Override
    public TypeDescriptor descriptor() {
        return null;
    }
}
