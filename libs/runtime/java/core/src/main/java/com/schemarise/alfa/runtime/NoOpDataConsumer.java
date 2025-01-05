package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class NoOpDataConsumer extends DataConsumer {

    @Override
    public void consume(ScalarDataType dt, int v) {

    }

    @Override
    public void consume(ScalarDataType dt, String v) {

    }

    @Override
    public void consume(ScalarDataType dt, double v) {

    }

    @Override
    public void consume(ScalarDataType dt, float v) {

    }

    @Override
    public void consume(ScalarDataType dt, short v) {

    }

    @Override
    public void consume(ScalarDataType dt, long v) {

    }

    @Override
    public void consume(ScalarDataType dt, byte v) {

    }

    @Override
    public void consume(ScalarDataType dt, byte[] v) {

    }

    @Override
    public void consume(ScalarDataType dt, char v) {

    }

    @Override
    public void consume(ScalarDataType dt, boolean v) {

    }

    @Override
    public void consume(ScalarDataType dt, BigDecimal v) {

    }

    @Override
    public void consume(ScalarDataType dt, LocalDate v) {

    }

    @Override
    public void consume(ScalarDataType dt, LocalDateTime v) {

    }

    @Override
    public void consume(ScalarDataType dt, ZonedDateTime v) {

    }

    @Override
    public void consume(ScalarDataType dt, LocalTime v) {

    }

    @Override
    public void consume(ScalarDataType dt, Duration v) {

    }

    @Override
    public void consume(ScalarDataType dt, NormalizedPeriod v) {

    }

    @Override
    public void consume(ScalarDataType dt, UUID v) {

    }

    @Override
    public void consume(ScalarDataType dt, URI v) {

    }

    @Override
    public void consume(ScalarDataType dt, UnionUntypedCase v) {

    }

    @Override
    public <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer) {

    }

    @Override
    public <T> void consume(CompressedDataType dt, Compressed v, BiConsumer<T, DataConsumer> elementConsumer) {

    }

    @Override
    public <T> void consume(EncryptedDataType dt, Encrypted<T> v, BiConsumer<T, DataConsumer> elementConsumer) {

    }

    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {

    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer) {

    }

    @Override
    public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {

    }

    @Override
    public <T> void consume(StreamDataType dt, List<T> f1, BiConsumer<T, DataConsumer> consumer) {

    }

    @Override
    public <T> void consume(FutureDataType dt, Future<T> f1, BiConsumer<T, DataConsumer> consumer) {

    }

    @Override
    public <T> void consume(MetaDataType dt, T f1) {

    }


    @Override
    public <T> void consume(TabularDataType dt, ITable f1, BiConsumer<T, DataConsumer> consumer) {

    }
}
