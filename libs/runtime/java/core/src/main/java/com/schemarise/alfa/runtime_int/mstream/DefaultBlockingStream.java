package com.schemarise.alfa.runtime_int.mstream;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.utils.stream.IBlockingStream;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

// To be implemented as a circular buffer queue
public class DefaultBlockingStream<T extends AlfaObject> implements IBlockingStream<T> {
    private List<T> items = new LinkedList<T>();
    private CountDownLatch l = new CountDownLatch(1);

    @Override
    public synchronized void deposit(T e) {
        if ( e == null ) {
            l.countDown();
        } else {
            items.add(e);
        }
    }

    @Override
    public Stream<T> getStream() {
        try {
            l.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return items.stream();
    }
}
