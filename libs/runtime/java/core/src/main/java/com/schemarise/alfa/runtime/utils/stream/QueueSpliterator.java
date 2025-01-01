package com.schemarise.alfa.runtime.utils.stream;

import com.schemarise.alfa.runtime.Holder;
import com.lmax.disruptor.EventPoller;

import java.util.Spliterator;
import java.util.function.Consumer;

public final class QueueSpliterator<T> implements Spliterator<T> {
    private final EventPoller<ValueEvent> poller;

    public QueueSpliterator(EventPoller<ValueEvent> poller) {
        this.poller = poller;
    }

    @Override
    public int characteristics() {
        return Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    private Holder<Boolean> endOfStream = new Holder<>(false);

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        if (endOfStream.getValue()) {
            return false;
        }

        try {
            EventPoller.PollState ps = poller.poll(new EventPoller.Handler<ValueEvent>() {
                @Override
                public boolean onEvent(ValueEvent ve, long sequence, boolean endOfBatch) throws Exception {
                    if (ve.getValue() == null) {
                        endOfStream.setValue(true);
                        return false;
                    }

                    action.accept((T) ve.getValue());
                    return true;
                }
            });

            return true;

        } catch (final Exception e) {
            throw new RuntimeException("interrupted", e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }
}
