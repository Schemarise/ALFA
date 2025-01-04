package com.schemarise.alfa.runtime.utils.stream;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime_int.mstream.MultiStream;

import java.util.stream.Stream;

public final class MultistreamFactory {
    public static <T extends AlfaObject, R> IMultiStream<T, R> create(String streamType, Stream<T> s, int bufSize) {
        return new MultiStream<T, R>(streamType, s, bufSize);
    }

    public static <T extends AlfaObject, R> IMultiStream<T, R> create(String streamType, Stream<T> s) {
        return new MultiStream<T, R>(streamType, s);
    }
}
