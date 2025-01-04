package com.schemarise.alfa.runtime_int.mstream;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.utils.stream.QueueSpliterator;
import com.schemarise.alfa.runtime.utils.stream.ValueEvent;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventPoller;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BlockingStream<T extends AlfaObject> {
    private final Disruptor<ValueEvent> disruptor;
    private final RingBuffer<ValueEvent> ringBuffer;
    private final Stream<AlfaObject> s;

    public BlockingStream() {
        this(1024);
    }

    public BlockingStream(int bufSize) {
        disruptor = new Disruptor<>(
                ValueEvent.EVENT_FACTORY,
                bufSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        ringBuffer = disruptor.start();

        EventPoller<ValueEvent> poller = ringBuffer.newPoller();
        s = StreamSupport.stream(new QueueSpliterator<>(poller), true);
    }

    /**
     * Needs to be synchorized otherwise seem to loose msgs when using having multi-thread writers
     */
    public synchronized void deposit(T e) {
        long sequenceId = ringBuffer.next();
        ValueEvent ve = ringBuffer.get(sequenceId);
        ve.setValue(e);
        ringBuffer.publish(sequenceId);
    }

    public Stream<T> getStream() {
        return (Stream<T>) s;
    }
}
