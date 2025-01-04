package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Entity;
import com.schemarise.alfa.runtime.Key;
import com.schemarise.alfa.runtime.Union;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.codec.OptSetChecker;
import schemarise.alfa.runtime.model.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

class ToBuilderCreator extends NoOpDataConsumer {

    private final IBuilderConfig codecConfig;
    private Stack<Object> valueStack = new Stack<Object>();
    private Stack<Builder> buildersStack = new Stack<Builder>();
    private Builder lastBuilder;

    public ToBuilderCreator(IBuilderConfig cc, AlfaObject so) {
        codecConfig = cc;
        consume(so);
    }

    @Override
    public <T> void consume(CompressedDataType dt, Compressed v, BiConsumer<T, DataConsumer> elementConsumer) {
        valueStack.push(v);
    }

    @Override
    public <T> void consume(EncryptedDataType dt, Encrypted<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        valueStack.push(v);
    }

    @Override
    public <T> void consume(StreamDataType dt, List<T> f1, BiConsumer<T, DataConsumer> consumer) {
        super.consume(dt, f1, consumer);
    }

    @Override
    public <T> void consume(FutureDataType dt, Future<T> f1, BiConsumer<T, DataConsumer> consumer) {
        super.consume(dt, f1, consumer);
    }

    @Override
    public <T> void consume(TabularDataType dt, ITable f1, BiConsumer<T, DataConsumer> consumer) {
        super.consume(dt, f1, consumer);
    }

    @Override
    public <T> void consume(TryDataType dt, Try<T> v, BiConsumer<T, DataConsumer> c) {
        super.consume(dt, v, c);
    }

    @Override
    public <L, R> void consume(EitherDataType dt, Either<L, R> v, BiConsumer<L, DataConsumer> leftConsumer, BiConsumer<R, DataConsumer> rightConsumer) {
        super.consume(dt, v, leftConsumer, rightConsumer);
    }

    public Builder newBuilder() {
        return lastBuilder;
    }

    public void consume(ScalarDataType dt, int v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, java.lang.String v) {
        valueStack.push(v);
    }


    @Override
    public void consume(ScalarDataType dt, double v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, float v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, short v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, long v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, byte v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, byte[] v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, char v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, boolean v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, BigDecimal v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalDate v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, ZonedDateTime v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalDateTime v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalTime v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, Duration v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, NormalizedPeriod v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, UUID v) {
        valueStack.push(v);
    }

    @Override
    public void consume(ScalarDataType dt, URI v) {
        valueStack.push(v);
    }

    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> map, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        LinkedHashMap m = new LinkedHashMap(map.size());
        map.forEach((k, v) -> {
            keyConsumer.accept(k, this);
            Object nk = valueStack.pop();

            int x = valueStack.size();
            valueConsumer.accept(v, this);
            int y = valueStack.size();
            Object nv = valueStack.pop();

            m.put(nk, nv);
        });

        valueStack.push(m);
    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> set, BiConsumer<T, DataConsumer> elementConsumer) {
        Set s = new LinkedHashSet(set.size());

        set.forEach(e -> {
            elementConsumer.accept(e, this);
            s.add(valueStack.pop());
        });

        valueStack.push(s);
    }

    @Override
    public <T> void consume(ListDataType dt, List<T> list, BiConsumer<T, DataConsumer> elementConsumer) {
        List s = new ArrayList(list.size());

        list.forEach(e -> {
            elementConsumer.accept(e, this);
            s.add(valueStack.pop());
        });

        valueStack.push(s);
    }

    @Override
    public <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        if (v.isPresent()) {
            elementConsumer.accept(v.get(), this);
            valueStack.push(Optional.of(valueStack.pop()));
        } else {
            valueStack.push(Optional.empty());
        }
    }

    @Override
    public void consume(ScalarDataType dt, UnionUntypedCase v) {
        valueStack.push(v);
    }

    public void consume(IDataType dt, AlfaObject v, Map<String, BiConsumer> templatedFieldConsumer) {

        if (!v.descriptor().hasBuilder()) {
            // enum
            valueStack.push(v);
            return;
        }

        buildersStack.push(v.descriptor().builder(codecConfig));

        if (v instanceof com.schemarise.alfa.runtime.Union) {
            com.schemarise.alfa.runtime.Union u = (Union) v;
            java.lang.String c = u.caseName();

            BiConsumer sup = templatedFieldConsumer.get(c);
            if (sup != null) {
                Object cv = u.caseValue();
                sup.accept(cv, this);
            } else {
                Optional<BiConsumer<AlfaObject, DataConsumer>> o = v.descriptor().getFieldSupplier(c);
                if (o.isPresent())
                    o.get().accept(v, this);
                else
                    throw new AlfaRuntimeException("Not consuming field");
            }
            buildersStack.peek().modify(c, valueStack.pop());
        } else {
            if (v instanceof com.schemarise.alfa.runtime.Entity) {
                com.schemarise.alfa.runtime.Entity ent = (Entity) v;
                Optional<? extends Key> k = ent.get$key();
                if (k.isPresent()) {
                    consume(k.get());
                    ((com.schemarise.alfa.runtime.EntityBuilder) buildersStack.peek()).set$key(valueStack.pop());
                }
            }

            OptSetChecker checker = new OptSetChecker();

            for (java.lang.String fn : v.descriptor().getAllFieldsMeta().keySet()) {
                BiConsumer<AlfaObject, DataConsumer> sup = v.descriptor().getFieldSupplier(fn).get();

                if (v.descriptor().getAllFieldsMeta().get(fn).getDataType() instanceof OptionalDataType) {
                    sup.accept(v, checker);
                    if (!checker.isSet())
                        continue;
                }

                sup.accept(v, this);

                buildersStack.peek().modify(fn, valueStack.pop());
            }
        }
        Builder b = buildersStack.pop();
        lastBuilder = b;

        if (buildersStack.size() > 0)
            valueStack.push(b.build());
    }
}