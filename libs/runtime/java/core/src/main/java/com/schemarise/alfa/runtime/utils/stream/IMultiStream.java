package com.schemarise.alfa.runtime.utils.stream;

import com.schemarise.alfa.runtime.AlfaObject;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IMultiStream<T extends AlfaObject, ProcResult> {
    void addProcessor(String name, StreamProcessor<T, ProcResult> c);

    <R> MultistreamResults<R> executeAll(Function<Map<String, ProcResult>, List<R>> resultConverter);
}
