package com.schemarise.alfa.runtime;

import java.util.Optional;

public class Holder<T> {
    private Optional<T> value = Optional.empty();

    public T getValue() {
        return value.get();
    }

    public boolean isSet() {
        return value.isPresent();
    }

    public Holder() {
    }

    public Holder(T v) {
        setValue(v);
    }

    public void setValue(T value) {
        this.value = Optional.of(value);
    }
}
