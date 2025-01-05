package com.schemarise.alfa.runtime.utils.stream;

import com.schemarise.alfa.runtime.AlfaObject;

import java.util.stream.Stream;

public interface IBlockingStream<T extends AlfaObject> {
    public void deposit(T e);
    public Stream<T> getStream();

}
