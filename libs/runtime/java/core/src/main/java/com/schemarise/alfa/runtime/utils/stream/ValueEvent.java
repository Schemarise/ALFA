package com.schemarise.alfa.runtime.utils.stream;

import com.lmax.disruptor.EventFactory;

public final class ValueEvent<T> {
    private T value;
    public final static EventFactory EVENT_FACTORY
            = () -> new ValueEvent();

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}