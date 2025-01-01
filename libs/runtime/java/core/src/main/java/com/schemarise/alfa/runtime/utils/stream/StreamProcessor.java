package com.schemarise.alfa.runtime.utils.stream;

import java.util.stream.Stream;

public interface StreamProcessor<T, R> {
    R process(Stream<T> s);
}
