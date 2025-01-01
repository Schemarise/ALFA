package com.schemarise.alfa.runtime.codec;

import com.schemarise.alfa.runtime.DataConsumer;
import com.schemarise.alfa.runtime.NoOpDataConsumer;
import schemarise.alfa.runtime.model.OptionalDataType;

import java.util.Optional;
import java.util.function.BiConsumer;

public class OptSetChecker extends NoOpDataConsumer {
    private boolean isPresent;

    @Override
    public <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        isPresent = v.isPresent();
    }

    public boolean isSet() {
        return isPresent;
    }
}
