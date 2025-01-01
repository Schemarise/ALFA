package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.SizeEstimator;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;

final class SizeEstimateGenerator extends NoOpDataConsumer implements IntImpl.SizeEstimatorIfc {

    private long sizeAccumulator;
    private Set<String> strings;

    public SizeEstimateGenerator(Set<String> strings, AlfaObject so) {
        this.strings = strings;
        consume(so.descriptor().getUdtDataType(), so);
    }

    public SizeEstimateGenerator(AlfaObject so) {
        this.strings = new HashSet<>();

        consume(so.descriptor().getUdtDataType(), so);
        strings.clear();
    }

    @Override
    public void consume(ScalarDataType dt, int v) {
        sizeAccumulator += Integer.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, String v) {
        if (!strings.contains(v)) {
            strings.add(v);
            sizeAccumulator += v.getBytes().length;
        }
    }

    @Override
    public void consume(ScalarDataType dt, double v) {
        sizeAccumulator += Integer.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, short v) {
        sizeAccumulator += Short.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, long v) {
        sizeAccumulator += Long.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, byte v) {
        sizeAccumulator += Byte.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, byte[] v) {
        sizeAccumulator += Byte.SIZE * v.length;
    }

    @Override
    public void consume(ScalarDataType dt, char v) {
        sizeAccumulator += Character.SIZE;
    }

    @Override
    public void consume(ScalarDataType dt, boolean v) {
        sizeAccumulator += 1;
    }

    @Override
    public void consume(ScalarDataType dt, BigDecimal v) {
        sizeAccumulator += 56;
    }

    @Override
    public void consume(ScalarDataType dt, LocalDate v) {
        sizeAccumulator += Integer.SIZE * 2;
    }

    @Override
    public void consume(ScalarDataType dt, ZonedDateTime v) {
        sizeAccumulator += Integer.SIZE * 3;
    }

    @Override
    public void consume(ScalarDataType dt, LocalDateTime v) {
        sizeAccumulator += Integer.SIZE * 4;
    }

    @Override
    public void consume(ScalarDataType dt, LocalTime v) {
        sizeAccumulator += Integer.SIZE * 2;
    }

    @Override
    public void consume(ScalarDataType dt, NormalizedPeriod v) {
        sizeAccumulator += Integer.SIZE * 2;
    }

    @Override
    public void consume(ScalarDataType dt, Duration v) {
        sizeAccumulator += Integer.SIZE * 2;
    }

    @Override
    public void consume(ScalarDataType dt, UUID v) {
        sizeAccumulator += Long.SIZE * 2;
    }

    @Override
    public void consume(ScalarDataType dt, URI v) {
        consume(dt, v.getHost());
        consume(dt, v.getPath());
        consume(dt, v.getFragment());
        consume(dt, v.getAuthority());
        consume(dt, v.getPort());
        consume(dt, v.getQuery());
        consume(dt, v.getScheme());
    }

    @Override
    public <T> void consume(EncryptedDataType dt, Encrypted<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        sizeAccumulator += v.getEncodedBytes().length;
    }

    @Override
    public <T> void consume(CompressedDataType dt, Compressed v, BiConsumer<T, DataConsumer> elementConsumer) {
        sizeAccumulator += v.getEncodedBytes().length;
    }

    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        sizeAccumulator += Integer.SIZE * v.size(); // For Map.Entry objects
        super.consume(dt, v, keyConsumer, valueConsumer);
    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        sizeAccumulator += Integer.SIZE * v.size(); // For Map.Entry objects
        super.consume(dt, v, elementConsumer);
    }

    @Override
    public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        super.consume(dt, v, elementConsumer);
    }

    public long getEstimatedSize() {
        return sizeAccumulator;
    }
}
