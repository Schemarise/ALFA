package com.schemarise.alfa.runtime_int.mstream;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.Logger;
import com.schemarise.alfa.runtime.utils.stream.*;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventPoller;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MultiStream<T extends AlfaObject, PR> implements IMultiStream<T, PR> {
    private final Stream<T> under;
    private final Map<String, StreamProcessor<T, PR>> processors = new HashMap<>();
    private final String streamType;
    private final Disruptor<ValueEvent> disruptor;
    private final RingBuffer<ValueEvent> ringBuffer;

    public MultiStream(String streamType, Stream<T> s) {
        this(streamType, s, 1024);
    }

    public MultiStream(String streamType, Stream<T> s, int bufSize) {
        this.under = s;
        this.streamType = streamType;

        disruptor = new Disruptor<>(
                ValueEvent.EVENT_FACTORY,
                bufSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        ringBuffer = disruptor.start();
    }

    private Stream<T> getStreamCopy() {
        EventPoller<ValueEvent> poller = ringBuffer.newPoller();
        Stream<T> s = StreamSupport.stream(new QueueSpliterator<T>(poller), true);
        return s;
    }

    @Override
    public void addProcessor(String name, StreamProcessor<T, PR> c) {
        StreamProcessor<T, PR> old = processors.put(name, c);
        if (old != null)
            throw new RuntimeException("Processor " + name + "already exists");
    }

    @Override
    public <R> MultistreamResults<R> executeAll(Function<Map<String, PR>, List<R>> resultConverter) {
        if (processors.isEmpty())
            throw new RuntimeException("No processors supplied");

        if (processors.size() == 1) {
            StreamProcessor<T, PR> sp = processors.values().iterator().next();
            PR res = sp.process(this.under);

            Map<String, PR> resultsMap = new HashMap<>();
            resultsMap.put(processors.keySet().iterator().next(), res);

            long start = System.nanoTime();
            List<R> r = resultConverter.apply(resultsMap);
            long end = System.nanoTime();

            Logger.getOrCreateDefault().debug("Completed assertAll '" + processors.keySet().iterator().next() + "' " + (end - start) / 1000000 + "ms");

            return new MultistreamResults(r == null ? -1 : r.size(), r);
        } else
            return runMulti(resultConverter);
    }


    private class Entry<K, V> implements Map.Entry<K, V> {

        private K k;
        private V v;

        public Entry(K k, V v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

        @Override
        public V setValue(V value) {
            v = value;
            return v;
        }
    }

    private <R> MultistreamResults<R> runMulti(Function<Map<String, PR>, List<R>> resultConverter) {
        AtomicInteger ai = new AtomicInteger();

        ExecutorService exes = Executors.newFixedThreadPool(processors.size(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("ALFA-MS-Proc-" + streamType + "-" + ai.incrementAndGet());
                return t;
            }
        });

        Runnable r = () -> {
            under.forEach(e -> deposit(e));
            deposit(null);
        };

        Thread t = new Thread(r);
        t.setName("ALFA-MS-Ctrl-" + streamType);
        t.setDaemon(true);
        t.start();


        List<Future<Entry<String, PR>>> futures = processors.entrySet().stream().map(p -> {

            Stream<T> s = getStreamCopy();

            Future<Entry<String, PR>> c = exes.submit((Callable<Entry<String, PR>>) () -> {
                PR r1 = p.getValue().process(s);
                return new Entry(p.getKey(), r1);
            });
            return c;
        }).collect(Collectors.toList());

        List<Entry<String, PR>> resultsList = futures.stream().map(f -> {
            try {
                long start = System.nanoTime();
                Entry<String, PR> e = f.get();
                long end = System.nanoTime();

                Logger.getOrCreateDefault().debug("Completed assertAll '" + e.getKey() + "' " + (end - start) / 1000000 + "ms");

                return e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        exes.shutdown();

        Map<String, PR> resultsMap = resultsList.stream().
                filter(e -> e.getValue() != null).
                collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue()));

        List<R> l = resultConverter.apply(resultsMap);

        MultistreamResults msr = new MultistreamResults(l == null ? -1 : l.size(), l);

        return msr;
    }

    private synchronized void deposit(T e) {
        long sequenceId = ringBuffer.next();
        ValueEvent ve = ringBuffer.get(sequenceId);
        ve.setValue(e);
        ringBuffer.publish(sequenceId);
    }
}
