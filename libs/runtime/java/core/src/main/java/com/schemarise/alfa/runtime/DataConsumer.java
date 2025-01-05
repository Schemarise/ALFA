package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.OptSetChecker;
import schemarise.alfa.runtime.model.*;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public abstract class DataConsumer {
    private Stack<String> currField = new Stack<String>();

    private CodecConfig codecConfig;

    protected DataConsumer() {
        this(CodecConfig.defaultCodecConfig());
    }

    protected DataConsumer(CodecConfig jwc) {
        this.codecConfig = jwc;
    }

    public void init() {
        this.currField.clear();
    }

    protected CodecConfig getCodecConfig() {
        return codecConfig;
    }

    public abstract void consume(ScalarDataType dt, int v);

    public abstract void consume(ScalarDataType dt, String v);

    public abstract void consume(ScalarDataType dt, double v);

    public abstract void consume(ScalarDataType dt, float v);

    public abstract void consume(ScalarDataType dt, short v);

    public abstract void consume(ScalarDataType dt, long v);

    public abstract void consume(ScalarDataType dt, byte v);

    public abstract void consume(ScalarDataType dt, byte[] v);

    public abstract void consume(ScalarDataType dt, char v);

    public abstract void consume(ScalarDataType dt, boolean v);

    public abstract void consume(ScalarDataType dt, BigDecimal v);

    public abstract void consume(ScalarDataType dt, LocalDate v);

    public abstract void consume(ScalarDataType dt, LocalDateTime v);

    public abstract void consume(ScalarDataType dt, ZonedDateTime v);

    public abstract void consume(ScalarDataType dt, LocalTime v);

    public abstract void consume(ScalarDataType dt, java.time.Duration v);

    public abstract void consume(ScalarDataType dt, NormalizedPeriod v);

    public abstract void consume(ScalarDataType dt, UUID v);

    public abstract void consume(ScalarDataType dt, URI v);

    public abstract void consume(ScalarDataType dt, UnionUntypedCase v);

//    public abstract void consume( TupleDataType)

    public abstract <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer);
//    public abstract < T > void consume(Try< T > v, BiConsumer< T, DataConsumer> elementConsumer );
//    public abstract < L, R > void consume(Either< L, R> v, BiConsumer< L, DataConsumer> leftConsumer, BiConsumer< R, DataConsumer> rightConsumer );

    public abstract <T> void consume(CompressedDataType dt, Compressed v, BiConsumer<T, DataConsumer> elementConsumer);

    public abstract <T> void consume(EncryptedDataType dt, Encrypted<T> v, BiConsumer<T, DataConsumer> elementConsumer);

    public abstract <K, V> void consume(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer);

    public abstract <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer);

    public abstract <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer);

    public abstract <T> void consume(StreamDataType dt, List<T> f1, BiConsumer<T, DataConsumer> consumer);

    public abstract <T> void consume(FutureDataType dt, Future<T> f1, BiConsumer<T, DataConsumer> consumer);

    public abstract <T> void consume(MetaDataType dt, T f1);

    public abstract <T> void consume(TabularDataType dt, ITable f1, BiConsumer<T, DataConsumer> consumer);

    public void consume(AlfaObject v) {
        consume(v.descriptor().getUdtDataType(), v);
    }

//    public void consume(KeyDataType dt, AlfaObject v ) {
//        consume(dt, v, Collections.emptyMap());
//    }

    public void consume(TupleDataType dt, AlfaObject v) {
        consume(dt, v, Collections.emptyMap());
    }

    public void consume(UnionDataType dt, AlfaObject v) {
        consume(dt, v, Collections.emptyMap());
    }

    public void consume(EnumDataType dt, AlfaObject v) {
        consume(dt, v, Collections.emptyMap());
    }

    public void consume(UdtDataType dt, AlfaObject v) {
        consume(dt, v, Collections.emptyMap());
    }

    public <T> void consume(TryDataType dt, schemarise.alfa.runtime.model.Try<T> v, BiConsumer<T, DataConsumer> c) {
        Map<String, IDataType> templateTypes = new HashMap<>();
        templateTypes.put("Result", dt.getComponentType());

        Map<String, BiConsumer> m = new HashMap<>();
        m.put("Result", c);
        consumeTemplated(dt.getComponentType(), v, m, templateTypes);
    }


    public <L, R> void consume(PairDataType dt, Pair<L, R> v, BiConsumer<L, DataConsumer> leftConsumer, BiConsumer<R, DataConsumer> rightConsumer) {

        Map<String, BiConsumer> c = new HashMap<>();

        Map<String, IDataType> templateTypes = new HashMap<>();

        templateTypes.put("Left", dt.getLeftComponentType());
        templateTypes.put("Right", dt.getRightComponentType());

        c.put("Left", leftConsumer);
        consumeTemplated(dt.getLeftComponentType(), v, c, templateTypes);
        c.put("Right", rightConsumer);
        consumeTemplated(dt.getRightComponentType(), v, c, templateTypes);
    }


    public <L, R> void consume(EitherDataType dt, Either<L, R> v, BiConsumer<L, DataConsumer> leftConsumer, BiConsumer<R, DataConsumer> rightConsumer) {

        Map<String, BiConsumer> c = new HashMap<>();

        Map<String, IDataType> templateTypes = new HashMap<>();

        templateTypes.put("Left", dt.getLeftComponentType());
        templateTypes.put("Right", dt.getRightComponentType());

        if (v.isLeft()) {
            c.put("Left", leftConsumer);
            consumeTemplated(dt.getLeftComponentType(), v, c, templateTypes);
        } else {
            c.put("Right", rightConsumer);
            consumeTemplated(dt.getRightComponentType(), v, c, templateTypes);
        }
    }

    public String currentFieldName() {
        if (currField.isEmpty())
            return "";

        return currField.peek();
    }

    public void consumeTemplated(IDataType dt, AlfaObject v, Map<String, BiConsumer> templatedFieldConsumer, Map<String, IDataType> templatedTypes) {
        consume(dt, v, templatedFieldConsumer);
    }

    public void consume(IDataType dt, AlfaObject v, Map<String, BiConsumer> templatedFieldConsumer) {
        if (v instanceof Union) {
            Union u = (Union) v;
            java.lang.String c = u.caseName();

            BiConsumer sup = templatedFieldConsumer.get(c);

            IDataType fdt = v.descriptor().getAllFieldsMeta().get(c).getDataType();
            preConsumeField(c, fdt);

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

            postConsumeField(c, fdt, false);
        } else {
            if (v instanceof Entity) {
                Entity ent = (Entity) v;
                Optional<? extends Key> k = ent.get$key();
                if (k.isPresent()) {
//                    consumeFields(k.get());
                    String kf = codecConfig.getMetaFieldPrefix() + "key";

                    if (codecConfig.isWriteEntityKeyAsObject()) {
                        preConsumeField(kf, k.get().descriptor().getUdtDataType());
                        consume(k.get());
                        postConsumeField(kf, k.get().descriptor().getUdtDataType(), false);
                    } else {
                        consumeFields(k.get());
                    }
                }
            }

            consumeFields(v);
        }
    }

    private void consumeFields(AlfaObject v) {
        OptSetChecker checker = new OptSetChecker();

        getFieldsMeta(v).forEach((fk, fv) -> {
            if (fv.getSupplier().isPresent()) {
                boolean skip = false;
                BiConsumer<AlfaObject, DataConsumer> sup = fv.getSupplier().get();

                preConsumeField(fk, fv.getDataType());

                if (fv.getDataType() instanceof OptionalDataType) {
                    sup.accept(v, checker);
                    if (!checker.isSet())
                        skip = true;
                }

                if (!skip)
                    sup.accept(v, this);

                postConsumeField(fk, fv.getDataType(), skip);
            }
        });
    }

    protected <T extends AlfaObject> Map<String, FieldMeta<T>> getFieldsMeta(AlfaObject ao) {
        return ao.descriptor().getAllFieldsMeta();
    }

    public Map<String, FieldMeta> scalarsFirstFieldMeta(AlfaObject ao) {
        Map<String, FieldMeta<AlfaObject>> map = ao.descriptor().getAllFieldsMeta();

        List<Map.Entry<String, FieldMeta>> list = new ArrayList(map.entrySet());
        Comparator<Map.Entry<String, FieldMeta>> typeComp = new Comparator<Map.Entry<String, FieldMeta>>() {
            @Override
            public int compare(Map.Entry<String, FieldMeta> o1, Map.Entry<String, FieldMeta> o2) {
                boolean t1 = o1.getValue().getDataType() instanceof ScalarDataType;
                boolean t2 = o1.getValue().getDataType() instanceof ScalarDataType;

                if (t1 == t2 && t1)
                    return 0;
                else if (t1)
                    return -1;
                else
                    return 1;
            }
        };

        list.sort(typeComp);

        Map<String, FieldMeta> result = new LinkedHashMap<>();
        for (Map.Entry<String, FieldMeta> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void preConsumeField(String fk, IDataType dataType) {
        currField.push(fk);
    }

    public List<String> getFieldNamesHierarchy() {
        return currField.subList(0, currField.size());
    }

    public void postConsumeField(String fk, IDataType dataType, boolean skipped) {
        currField.pop();
    }

    public OutputStream closeAndGetBuffer() {
        throw new UnsupportedOperationException();
    }
}
