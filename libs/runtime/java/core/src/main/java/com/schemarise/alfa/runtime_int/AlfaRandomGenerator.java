package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Record;
import com.schemarise.alfa.runtime.Union;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.Converters;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;
import com.schemarise.alfa.runtime.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AlfaRandomGenerator extends DataSupplier implements IntImpl.AlfaRandomGeneratorIfc {
    private final List<String> allTypes;
    private Random r = new Random();
    private Stack<IDataType> currentTypeStack = new Stack<>();
    private final IBuilderConfig cfg;

    public AlfaRandomGenerator(IBuilderConfig c, List<String> allTypes) {
        super(new CodecConfig.Builder().build());
        this.allTypes = allTypes;
        this.cfg = c;
    }

    public boolean randomizable(String typeName) {
        try {
            ClassUtils.ClassMeta cm = ClassUtils.getMeta(typeName);
            Class<?> tc = ClassUtils.getMeta(typeName).getTypeClass();
            return cm.hasBuilderClass(); //&& ClassUtils.isInstantiable(tc, true);
        } catch (RuntimeException cnf) {
            return false;
        }
    }

    @Override
    public <T extends AlfaObject> T randomWithValues(String typeName, Map<String, Object> values) {
        int attempts = 0;

        while (true) {
            Object value = null;
            try {
                attempts++;
                value = _random(typeName, values);
                return (T) value;
            } catch (AlfaRuntimeException e) {
                if (attempts > 100)
                    throw new AlfaRuntimeException(ConstraintType.Unknown, "Vaildation on " + typeName + " fails after 10000 attempts", e);

            }
        }
    }

    public <T extends AlfaObject> T random(String typeName) {
        return randomWithValues(typeName, Collections.emptyMap());
    }

    private <T extends AlfaObject> T _random(String typeName, Map<String, Object> values) {
        ClassUtils.ClassMeta meta = ClassUtils.getMeta(typeName);

        if (meta.getModel() != null && meta.getModel().getUdtDataType().getUdtType() == UdtMetaType.traitType) {
            final ClassUtils.ClassMeta finalMeta = meta;
            List<String> compat = allTypes.stream().filter(t -> {
                ClassUtils.ClassMeta tm = ClassUtils.getMeta(t);

                var m = tm.getModel();
                if ( m != null ) {
                    UdtMetaType ut = m.getUdtDataType().getUdtType();
                    if (tm.getModel() != null &&
                            ut != UdtMetaType.traitType &&
                            ut != UdtMetaType.libraryType &&
                            ut != UdtMetaType.testcaseType) {
                        return finalMeta.isTypeAssignable(tm);
                    }
                }
                return false;
            }).collect(Collectors.toList());

            if (compat.size() > 0) {
                meta = ClassUtils.getMeta(compat.get(r.nextInt((compat.size()))));
            } else
                throw new AlfaRuntimeException("Failed to find trait implementation for " + typeName);
        }

        TypeDescriptor model = meta.getModel();

        if (model == null) // services
            throw new UnsupportedOperationException();

        if (model.getUdtDataType().getUdtType() == UdtMetaType.enumType) {
            EnumDataType.EnumDataTypeBuilder et = EnumDataType.builder().setSynthFullyQualifiedName(typeName);
            meta.getModel().getAllFieldsMeta().keySet().forEach(f -> et.addFields(f));
            return enumValue(et.build());
        }

        Builder builder = meta.getNewBuilder(cfg);

        Set<String> fnames = model.getAllFieldsMeta().keySet();

        UdtMetaType mt = meta.getModel().getUdtDataType().getUdtType();

        if (mt == UdtMetaType.unionType || mt == UdtMetaType.untaggedUnionType) {
            int d = r.nextInt(fnames.size());
            String randFName = fnames.toArray(new String[0])[d];
            fnames = new HashSet<String>();
            fnames.add(randFName);
        }

        for (String fn : fnames) {
            if (values.containsKey(fn)) {
                builder.modify(fn, values.get(fn));
            } else {

                BiConsumer<Builder, DataSupplier> c = model.getFieldConsumer(fn).get();
                currentTypeStack.push(model.getAllFieldsMeta().get(fn).getDataType());
                c.accept(builder, this);
                currentTypeStack.pop();
            }
        }

        if (builder instanceof EntityBuilder) {
            EntityBuilder eb = (EntityBuilder) builder;

            String keyClassName = null;
            for (Method m : builder.getClass().getMethods()) {
                if (m.getName().equals("set$key") && !m.getParameters()[0].getType().getName().equals("java.lang.Object")) {
                    keyClassName = m.getParameters()[0].getType().getName();
                    break;
                }
            }

            if (keyClassName == null)
                throw new RuntimeException("set$key method not found for " + keyClassName);

            AlfaObject k = random(keyClassName);
            eb.set$key(k);
        }

        return (T) builder.build();
    }

    @Override
    public int intValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setIntValue(Integer.MAX_VALUE).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setIntValue(Integer.MIN_VALUE).build());

        int mx = ((Number) max.caseValue()).intValue();
        int mn = ((Number) min.caseValue()).intValue();

        return r.ints(1, mn, mx).findFirst().getAsInt();
    }

    @Override
    public String stringValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setLongValue(5).build());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max.getLongValue(); i++) {
            sb.append((char) (97 + r.nextInt(26)));
        }
        return sb.toString();
    }

//    @Override
//    public BigDecimal bigDecimalValue(ScalarDataType scalarDataType) {
//        return BigDecimal.valueOf(r.nextDouble());
//    }

    @Override
    public double doubleValue(ScalarDataType scalarDataType) {
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setDoubleValue(Double.MIN_VALUE).build());
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setDoubleValue(Double.MAX_VALUE).build());

        return ThreadLocalRandom.current().nextDouble(min.getDoubleValue(), max.getDoubleValue());
    }

    @Override
    public short shortValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setIntValue(Short.MAX_VALUE).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setIntValue(Short.MIN_VALUE).build());

        int mx = ((Number) max.caseValue()).intValue();
        int mn = ((Number) min.caseValue()).intValue();

        return (short) r.ints(1, mn, mx).findFirst().getAsInt();
    }

    @Override
    public long longValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setLongValue(Long.MAX_VALUE).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setLongValue(Long.MIN_VALUE).build());

        long mx = ((Number) max.caseValue()).longValue();
        long mn = ((Number) min.caseValue()).longValue();

        return r.longs(1, mn, mx).findFirst().getAsLong();
    }

    @Override
    public byte byteValue(ScalarDataType scalarDataType) {
        return ("" + r.nextInt()).getBytes()[0];
    }

    @Override
    public char charValue(ScalarDataType scalarDataType) {
        return (char) (r.nextInt(26) + 'a');
    }

    @Override
    public boolean booleanValue(ScalarDataType scalarDataType) {
        return r.nextInt(2) == 0;
    }

    @Override
    public BigDecimal decimalValue(ScalarDataType scalarDataType) {
        double d = doubleValue(scalarDataType);
        BigDecimal bd = new BigDecimal(d);

        if (scalarDataType.getPrecision().isPresent()) {
            String s = bd.toPlainString().substring(0, scalarDataType.getPrecision().get().getPrecision());
            return new BigDecimal(s);
        }
        return bd;
    }

    @Override
    public LocalDate dateValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setDateValue(LocalDate.of(2100, 1, 1)).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setDateValue(LocalDate.of(1000, 1, 1)).build());

        long mn = ((LocalDate) min.caseValue()).toEpochDay();
        long mx = ((LocalDate) max.caseValue()).toEpochDay();

        long ldate = r.longs(1, mn, mx).findFirst().getAsLong();

        return LocalDate.ofEpochDay(ldate);
    }

    @Override
    public LocalDateTime datetimeValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setDatetimeValue(LocalDateTime.of(2100, 1, 1, 0, 0)).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setDatetimeValue(LocalDateTime.of(1000, 1, 1, 0, 0)).build());

        long mn = ((LocalDateTime) min.caseValue()).toEpochSecond(ZoneOffset.UTC);
        long mx = ((LocalDateTime) max.caseValue()).toEpochSecond(ZoneOffset.UTC);

        long ldate = r.longs(1, mn, mx).findFirst().getAsLong();

        return LocalDateTime.ofEpochSecond(ldate, 0, ZoneOffset.UTC);
    }

    @Override
    public ZonedDateTime datetimetzValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setDatetimetzValue(ZonedDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setDatetimetzValue(ZonedDateTime.of(1000, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())).build());

        long mn = ((LocalDateTime) min.caseValue()).toEpochSecond(ZoneOffset.UTC);
        long mx = ((LocalDateTime) max.caseValue()).toEpochSecond(ZoneOffset.UTC);

        long ldate = r.longs(1, mn, mx).findFirst().getAsLong();

        return ZonedDateTime.of(LocalDateTime.ofEpochSecond(ldate, 0, ZoneOffset.UTC), ZoneId.systemDefault());
    }

    @Override
    public LocalTime timeValue(ScalarDataType scalarDataType) {
        RangeValue max = scalarDataType.getMax().orElse(RangeValue.builder().setTimeValue(LocalTime.of(23, 59, 59)).build());
        RangeValue min = scalarDataType.getMin().orElse(RangeValue.builder().setTimeValue(LocalTime.of(0, 0, 0)).build());

        long mn = ((LocalTime) min.caseValue()).toSecondOfDay();
        long mx = ((LocalTime) max.caseValue()).toSecondOfDay();

        long ltime = r.longs(1, mn, mx).findFirst().getAsLong();

        return LocalTime.ofSecondOfDay(ltime);
    }

    @Override
    public float floatValue(ScalarDataType scalarDataType) {
        return r.nextFloat();
    }

    @Override
    public byte[] binaryValue(ScalarDataType scalarDataType) {
        return ("" + r.nextInt()).getBytes();
    }

    @Override
    public Duration durationValue(ScalarDataType scalarDataType) {
        return Duration.ofDays(r.nextInt(3650));
    }

    @Override
    public NormalizedPeriod periodValue(ScalarDataType scalarDataType) {
        return NormalizedPeriod.of(Period.ofMonths(r.nextInt(12)));
    }

    @Override
    public UUID uuidValue(ScalarDataType scalarDataType) {
        return UUID.randomUUID();
    }

    @Override
    public URI uriValue(ScalarDataType scalarDataType) {
        String proto = scalarDataType.getStrPattern().orElse("http");

        return URI.create(proto + "://192.168.0.1:" + r.nextInt(50000));
    }

    @Override
    public String patternValue(ScalarDataType scalarDataType) {
        return "notarandompattern";
    }

    @Override
    public UnionUntypedCase voidValue(ScalarDataType scalarDataType) {
        return UnionUntypedCase.getInstance();
    }

    @Override
    public <T extends com.schemarise.alfa.runtime.Enum> T enumValue(EnumDataType t) {
        return _enumValue(t.getSynthFullyQualifiedName(), t.getFields().size());
    }

    private <T extends com.schemarise.alfa.runtime.Enum> T _enumValue(String n, int size) {
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(n);
        int i = 0;
        if (size > 1)
            r.nextInt(size - 1);

        try {
            Field field = cm.getTypeClass().getDeclaredFields()[i];

            java.lang.Enum v = java.lang.Enum.valueOf((Class<java.lang.Enum>) field.getType(), field.getName());
            return (T) v;
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.InvalidConstant, e);
        }
    }

    @Override
    public <T extends Union> T unionValue(UnionDataType t) {
        String n = t.getSynthFullyQualifiedName();
        if (randomizable(n))
            return random(n);
        else {
            // find derivation
            throw new RuntimeException();
        }
    }

    @Override
    public <T> T metaValue(MetaDataType t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t) {
        return objectValue(t, Collections.emptyMap());
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<Class> clz) {
        return objectValue(Optional.empty(), Collections.emptyMap());
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<ClassUtils.ClassMeta> cm, Map<String, Function> templateFieldSuppliers) {
        throw new RuntimeException();
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t, Map<String, Function> templateFieldSuppliers) {
        String n = t.getFullyQualifiedName();
        ClassUtils.ClassMeta m = ClassUtils.getMeta(n);

        if (randomizable(n))
            return random(n);

        else if (NativeAlfaObject.class.isAssignableFrom(m.getTypeClass())) {
            Builder v = m.getNewBuilder(BuilderConfig.getInstance());
            v.modify(null, "ignore");
            return (T) v.build();
        } else {
            if (m.getModel().getUdtDataType().getUdtType() == UdtMetaType.enumType) {

                return _enumValue(n, m.getModel().getAllFieldsMeta().size());

            } else {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public <T extends Record> T tupleValue(TupleDataType t) {
        String n = t.getSynthFullyQualifiedName();
        if (randomizable(n))
            return random(n);
        else {
            // find derivation
            throw new RuntimeException();
        }
    }

    public <T> Optional<T> optionalValue(OptionalDataType f, Function<DataSupplier, T> c) {
        // 10% empty optionals
        if (r.nextInt(10) == 5) {
            return Optional.empty();
        } else {
            currentTypeStack.push(f.getComponentType());
            T r = c.apply(this);
            currentTypeStack.pop();
            Optional<T> v = Optional.ofNullable(r);
            return v;
        }
    }


    @Override
    public <T> Try<T> tryValue(TryDataType f, Function<DataSupplier, T> c) {
        // 10% checked
        if (r.nextInt(10) == 1) {
            TryFailure fmsg = TryFailure.builder().setMessage("Failed on checked value").build();
            return Try.builder().setFailure(fmsg).build();
        } else {
            currentTypeStack.push(f.getComponentType());
            T res = c.apply(this);
            Try v = Try.builder().setResult(res).build();
            return v;
        }
    }

    @Override
    public <T> Compressed compressedValue(CompressedDataType f, Function<DataSupplier, T> compressedConsumer) {
        T v = compressedConsumer.apply(this);
        Converters.SupplierConsumer<T> cs = Converters.createSupplierConsumer(f.getComponentType());
        return DefaultCompressed.fromValue(cs, BuilderConfig.getInstance(), v);
    }

    @Override
    public <T> Compressed compressedValue(Function<DataSupplier, T> compressedConsumer) {
        throw new IllegalStateException();
    }

    @Override
    public <T> Encrypted<T> encryptedValue(Function<DataSupplier, T> encryptedConsumer) {
        return DefaultEncrypted.fromValue(encryptedConsumer, binaryValue(null));
    }

    @Override
    public <T> List<T> streamValue(StreamDataType std, Function<DataSupplier, T> c) {
        T v = c.apply(this);
        return Stream.of(v).collect(Collectors.toList());
    }

    @Override
    public <T> Future<T> futureValue(FutureDataType fdt, Function<DataSupplier, T> c) {
        T v = c.apply(this);
        CompletableFuture<T> f = new CompletableFuture<T>();
        f.complete(v);
        return f;
    }

    @Override
    public <T> ITable tableValue(TabularDataType tdt, Function<DataSupplier, T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <L, R> Pair<L, R> pairValue(PairDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        L l = lc.apply(this);
        R r = rc.apply(this);
        Pair v = Pair.builder().setRight(r).setLeft(l).build();
        return v;
    }

    @Override
    public <L, R> Either<L, R> eitherValue(EitherDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        if (r.nextInt(10) % 2 == 0) {
            L l = lc.apply(this);
            Either v = Either.builder().setLeft(l).build();
            return v;
        } else {
            R r = rc.apply(this);
            Either v = Either.builder().setRight(r).build();
            return v;
        }
    }

    @Override
    public <K, V> Map<K, V> mapValue(MapDataType t, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {
        int max = t.getSizeMax().orElse(5);
        Map<K, V> l = new LinkedHashMap<>();
        for (int i = 0; i < max; i++) {
            currentTypeStack.push(t.getKeyType());
            K k = kc.apply(this);
            Utils.assertNotNull(k);
            currentTypeStack.pop();

            currentTypeStack.push(t.getValueType());
            V v = vc.apply(this);
            Utils.assertNotNull(v);
            currentTypeStack.pop();

            l.put(k, v);
        }
        return l;
    }

    @Override
    public <T> Set<T> setValue(SetDataType f, Function<DataSupplier, T> consumer) {
        int max = f.getSizeMax().orElse(5);
        Set<T> l = new LinkedHashSet<>();
        for (int i = 0; i < max; i++) {
            currentTypeStack.push(f.getComponentType());
            T en = consumer.apply(this);
            Utils.assertNotNull(en);
            l.add(en);
            currentTypeStack.pop();
        }
        return l;
    }

    @Override
    public <T> List<T> listValue(ListDataType f, Function<DataSupplier, T> consumer) {
        int max = f.getSizeMax().orElse(5);

        List<T> l = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            currentTypeStack.push(f.getComponentType());
            T en = consumer.apply(this);
            Utils.assertNotNull(en);
            l.add(en);
            currentTypeStack.pop();
        }
        return l;
    }

    public IBuilderConfig codecConfig() {
        return cfg;
    }
}
