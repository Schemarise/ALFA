package com.schemarise.alfa.runtime_int.table;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Enum;
import com.schemarise.alfa.runtime.Trait;
import com.schemarise.alfa.runtime.codec.Converters;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.Union;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class AlfaFlattener extends NoOpDataConsumer {
    private final List<? super AlfaObject> alfaObjects;
    private final Table table;
    private Stack<CurrentObjectInfo> vectorInfo = new Stack<>();
    private int nestedVectorLevel = 0;

    class CurrentObjectInfo {
        private Row preVectorExpansionRow;
        private boolean requiresDimensionCol = false;

        public void setPreVectorVisitRow(Row r) {
            if (preVectorExpansionRow != null)
                throw new IllegalStateException();

            preVectorExpansionRow = r;
        }

        public boolean hasPreVectorVisitRow() {
            return preVectorExpansionRow != null;
        }
    }

    public AlfaFlattener(List<AlfaObject> obs) {
        this.alfaObjects = obs;
        this.table = new Table(obs.get(0).descriptor().getUdtDataType());
    }

    public AlfaFlattener(AlfaObject ao) {
        this.alfaObjects = new ArrayList<>();
        alfaObjects.add(ao);
        this.table = new Table(ao.descriptor().getUdtDataType());
    }

    @Override
    public void consume(IDataType dt, AlfaObject v, Map<String, BiConsumer> templatedFieldConsumer) {
        CurrentObjectInfo co = new CurrentObjectInfo();
        vectorInfo.push(co);

        co.requiresDimensionCol = v.descriptor().getAllFieldsMeta().values().
                stream().map(e -> e.getDataType()).filter(e -> e instanceof IVectorDataType).count() > 1;

        if (v instanceof Union) {
            Union u = (Union) v;
            table.update(Converters.DataTypeString, currentFieldName() + "__Case", u.caseName());
        } else if (v instanceof Enum) {
            com.schemarise.alfa.runtime.Enum en = (Enum) v;
            table.update(Converters.DataTypeString, currentFieldName(), en.toString());
        } else if (v instanceof Trait) {
            table.update(Converters.DataTypeString, currentFieldName() + "__Type", v.descriptor().getUdtDataType().getFullyQualifiedName());
        } else if (v instanceof NativeAlfaObject) {
            NativeAlfaObject no = (NativeAlfaObject) v;
            table.update(Converters.DataTypeString, genColName(), no.encodeToString());
        }

        super.consume(dt, v, templatedFieldConsumer);
        vectorInfo.pop();
    }

    @Override
    protected <T extends AlfaObject> Map<String, FieldMeta<T>> getFieldsMeta(AlfaObject ao) {

        List<FieldMeta<AlfaObject>> nonVecs = ao.descriptor().getAllFieldsMeta().values().
                stream().filter(e -> !(e.getDataType() instanceof IVectorDataType)).collect(Collectors.toList());

        List<FieldMeta<AlfaObject>> vecs = ao.descriptor().getAllFieldsMeta().values().
                stream().filter(e -> e.getDataType() instanceof IVectorDataType).collect(Collectors.toList());

        List<FieldMeta<AlfaObject>> l = new ArrayList<>();
        l.addAll(nonVecs);
        l.addAll(vecs);

        Map<String, FieldMeta<T>> m = new LinkedHashMap<>();
        l.forEach(e -> {
            m.put(e.getField().getName(), (FieldMeta<T>) e);
        });

        return m;
    }

    public Table flatten() {
        table.insertRow();
        for (int i = 0; i < alfaObjects.size(); i++) {
            consume((AlfaObject) alfaObjects.get(i));
            if (i + 1 < alfaObjects.size())
                table.insertRow();
        }
        return table;
    }

    private String genColName() {
        StringJoiner sj = new StringJoiner("_");
        getFieldNamesHierarchy().forEach(f -> sj.add(f));
        return sj.toString();
    }

    @Override
    public void consume(ScalarDataType dt, String v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, double v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, float v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, short v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, long v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, byte v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, byte[] v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, char v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, boolean v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, BigDecimal v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalDate v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalDateTime v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, ZonedDateTime v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, LocalTime v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, Duration v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, NormalizedPeriod v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, UUID v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, URI v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, UnionUntypedCase v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public void consume(ScalarDataType dt, int v) {
        String cn = genColName();
        table.update(dt, cn, v);
    }

    @Override
    public <T> void consume(TryDataType dt, Try<T> v, BiConsumer<T, DataConsumer> c) {
        table.update(Converters.DataTypeBoolean, "__" + genColName() + "_IsFailure", v.isFailure());
        if (v.isResult()) {
            c.accept(v.getResult(), this);
        } else {
            consume(v.getFailure());
        }
    }

    @Override
    public <L, R> void consume(EitherDataType dt, Either<L, R> v, BiConsumer<L, DataConsumer> leftConsumer,
                               BiConsumer<R, DataConsumer> rightConsumer) {
        table.update(Converters.DataTypeBoolean, "__" + genColName() + "_IsLeft", v.isLeft());

        if (v.isLeft()) {
            preConsumeField("Left", dt.getLeftComponentType());
            leftConsumer.accept(v.getLeft(), this);
            postConsumeField("Left", dt.getLeftComponentType(), false);
        } else {
            preConsumeField("Right", dt.getRightComponentType());
            rightConsumer.accept(v.getRight(), this);
            postConsumeField("Right", dt.getLeftComponentType(), false);
        }
    }

    @Override
    public <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        table.update(Converters.DataTypeBoolean, "__" + genColName() + "_IsSet", v.isPresent());
        if (v.isPresent()) {
            elementConsumer.accept(v.get(), this);
        }
    }

    private String nestedLevel() {
        if (nestedVectorLevel == 1)
            return "";
        else
            return "_L" + (nestedVectorLevel - 1);
    }


    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> data, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        nestedVectorLevel++;

        CurrentObjectInfo co = preVectorProcessInit(data.size(), dt.getSizeMin());

        AtomicInteger row = new AtomicInteger(0);

        data.forEach((k, v) -> {
            vectorPreIterateInit(co, dt.getSizeMin(), row);

            vectorInfo.push(new CurrentObjectInfo());

            String kName = dt.getKeyName().orElse("Key") + nestedLevel();
            preConsumeField(kName, dt.getKeyType());
            keyConsumer.accept(k, this);
            postConsumeField(kName, dt.getKeyType(), false);

            String vName = dt.getValueName().orElse("Value") + nestedLevel();
            preConsumeField(vName, dt.getValueType());
            valueConsumer.accept(v, this);
            postConsumeField(vName, dt.getValueType(), false);

            vectorInfo.pop();
        });

        nestedVectorLevel--;
    }

    @Override
    public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        nestedVectorLevel++;

        final int sz = v.size();
        CurrentObjectInfo co = preVectorProcessInit(sz, dt.getSizeMin());

        AtomicInteger row = new AtomicInteger(0);

        IntStream.range(0, sz).forEach(_i -> {
            vectorPreIterateInit(co, dt.getSizeMin(), row);

            int i = sz - _i;
            table.update(Converters.DataTypeInt, "__" + genColName() + "__Id" + nestedLevel(), i);

            T e = v.get(i - 1);

            vectorInfo.push(new CurrentObjectInfo());
            elementConsumer.accept(e, this);
            vectorInfo.pop();
        });

        nestedVectorLevel--;
    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        nestedVectorLevel++;

        CurrentObjectInfo co = preVectorProcessInit(v.size(), dt.getSizeMin());

        AtomicInteger row = new AtomicInteger(0);
        final int sz = v.size();

        v.forEach(e -> {
            vectorPreIterateInit(co, dt.getSizeMin(), row);

            int i = sz - row.get() + 1;
            table.update(Converters.DataTypeInt, "__" + genColName() + "__Id" + nestedLevel(), i);

            vectorInfo.push(new CurrentObjectInfo());
            elementConsumer.accept(e, this);
            vectorInfo.pop();
        });

        nestedVectorLevel--;
    }

    private CurrentObjectInfo preVectorProcessInit(int dataSize, Optional<Integer> sizeMin) {
        CurrentObjectInfo co = vectorInfo.peek();

        // If another vector was written, start this on a new row, otherwise same row is fine
        if (co.hasPreVectorVisitRow()) {
            table.cloneAndAdvanceRow(co.preVectorExpansionRow);
        }

        // Save the row shape before we started looping for this vector. If another
        // vector is visited, it too will used the same non-vector col values
        if (!co.hasPreVectorVisitRow())
            co.setPreVectorVisitRow(table.getCurrentRowObjectCopy());

        if (co.requiresDimensionCol)
            table.update(Converters.DataTypeString, "__Dimension", currentFieldName());

        boolean emptyReqd = !sizeMin.isPresent() || (sizeMin.isPresent() && sizeMin.get() == 0);

        if (emptyReqd && dataSize == 0)
            table.update(Converters.DataTypeBoolean, "__" + genColName() + "_IsEmpty" + nestedLevel(), true);

        return co;
    }

    private void vectorPreIterateInit(CurrentObjectInfo co, Optional<Integer> sizeMin, AtomicInteger row) {
        if (row.getAndIncrement() > 0)
            table.cloneAndAdvanceRow(co.preVectorExpansionRow);

        boolean emptyReqd = !sizeMin.isPresent() || (sizeMin.isPresent() && sizeMin.get() == 0);

        if (emptyReqd)
            table.update(Converters.DataTypeBoolean, "__" + genColName() + "_IsEmpty" + nestedLevel(), false);

        if (co.requiresDimensionCol)
            table.update(Converters.DataTypeString, "__Dimension" + nestedLevel(), currentFieldName());
    }
}
