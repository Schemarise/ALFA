package com.schemarise.alfa.runtime.codec;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Enum;
import com.schemarise.alfa.runtime.Key;
import com.schemarise.alfa.runtime.Record;
import com.schemarise.alfa.runtime.Union;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapBasedDataSupplier extends DataSupplier {

    protected final Stack<Object> values = new Stack<>();
    private static Map<String, LocalDate> datesCache = new ConcurrentHashMap<>();

    public MapBasedDataSupplier(CodecConfig jwc, IMapBasedRecord gr) {
        super(jwc);
        values.push(gr);
    }

    @Override
    public int intValue(ScalarDataType scalarDataType) {
        Object v = values.peek();
        if (v instanceof Integer)
            return (int) v;
        else
            return Integer.parseInt(v.toString());
    }

    @Override
    public String stringValue(ScalarDataType scalarDataType) {
        return values.peek().toString();
    }

    @Override
    public double doubleValue(ScalarDataType scalarDataType) {
        Object v = values.peek();
        if (v instanceof Double)
            return (double) v;
        else
            return Double.parseDouble(v.toString());
    }

    @Override
    public short shortValue(ScalarDataType scalarDataType) {
        Object v = values.peek();
        if (v instanceof Short)
            return (short) v;
        else
            return Short.parseShort(v.toString());
    }

    @Override
    public long longValue(ScalarDataType scalarDataType) {
        Object v = values.peek();
        if (v instanceof Short)
            return (long) values.peek();
        else
            return Long.parseLong(v.toString());
    }

//    @Override
//    public BigDecimal bigDecimalValue(ScalarDataType scalarDataType) {
//        return new BigDecimal(values.peek().toString() );
//    }

    @Override
    public byte byteValue(ScalarDataType scalarDataType) {
        byte[] v = (byte[]) values.peek();
        return v[0];
    }

    @Override
    public char charValue(ScalarDataType scalarDataType) {
        return values.peek().toString().charAt(0);
    }

    @Override
    public boolean booleanValue(ScalarDataType scalarDataType) {
        Object v = values.peek();
        if (v instanceof Boolean)
            return (boolean) values.peek();
        else
            return Boolean.parseBoolean(v.toString());
    }

    private Map<String, DateTimeFormatter> datetimeFormatMap = new HashMap<>();

    private DateTimeFormatter getFormat(String fmt) {
        DateTimeFormatter df = datetimeFormatMap.get(fmt);
        if (df == null) {
            df = DateTimeFormatter.ofPattern(fmt);
            datetimeFormatMap.put(fmt, df);
        }
        return df;
    }

    @Override
    public BigDecimal decimalValue(ScalarDataType scalarDataType) {
        return new BigDecimal(values.peek().toString());
    }

    @Override
    public LocalDate dateValue(ScalarDataType dt) {
        String val = values.peek().toString();

        LocalDate sdt = datesCache.get(val);

        if (sdt != null)
            return sdt;
        else {
            if (dt.getStrPattern().isPresent()) {
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                sdt = LocalDate.parse(values.peek().toString(), df);
            } else
                sdt = LocalDate.parse(values.peek().toString());

            datesCache.put(val, sdt);
            return sdt;
        }
    }

    @Override
    public LocalDateTime datetimeValue(ScalarDataType scalarDataType) {
        return LocalDateTime.parse(values.peek().toString());
    }


    @Override
    public ZonedDateTime datetimetzValue(ScalarDataType scalarDataType) {
        return ZonedDateTime.parse(values.peek().toString());
    }

    @Override
    public LocalTime timeValue(ScalarDataType scalarDataType) {
        return LocalTime.parse(values.peek().toString());
    }

    @Override
    public float floatValue(ScalarDataType scalarDataType) {
        return (float) values.peek();
    }

    @Override
    public byte[] binaryValue(ScalarDataType scalarDataType) {
        return (byte[]) values.peek();
    }

    @Override
    public Duration durationValue(ScalarDataType scalarDataType) {
        return Duration.parse(values.peek().toString());
    }

    @Override
    public NormalizedPeriod periodValue(ScalarDataType scalarDataType) {
        return NormalizedPeriod.of(values.peek().toString());
    }

    @Override
    public UUID uuidValue(ScalarDataType scalarDataType) {
        return UUID.fromString(values.peek().toString());
    }

    @Override
    public URI uriValue(ScalarDataType scalarDataType) {
        return URI.create(values.peek().toString());
    }

    @Override
    public String patternValue(ScalarDataType scalarDataType) {
        return values.peek().toString();
    }

    @Override
    public UnionUntypedCase voidValue(ScalarDataType scalarDataType) {
        return UnionUntypedCase.getInstance();
    }

    @Override
    public <T extends Record> T tupleValue(TupleDataType t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Enum> T enumValue(EnumDataType t) {
        String enConst = (String) values.peek();
        T d = (T) ClassUtils.getByEnumConst(t.getSynthFullyQualifiedName(), enConst);
        return d;
    }

    @Override
    public <T extends Union> T unionValue(UnionDataType t) {
        IMapBasedRecord gr = (IMapBasedRecord) values.peek();
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(gr.getFullName());
        return objectValue(Optional.of(cm), Collections.emptyMap());
//        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T metaValue(MetaDataType t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t) {
        return objectValue(t, Collections.emptyMap());
    }

    public <T extends Enum> T enumValue(UdtDataType t) {
        String enConst = (String) values.peek();
        // Avro doesnt support enum consts by value with spaces, so read only consts which are valid identifiers
        return (T) ClassUtils.getByEnumConst(t.getFullyQualifiedName(), enConst);
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t, Map<String, Function> templateFieldSuppliers) {
        if (t.getUdtType() == UdtMetaType.enumType) {
            return enumValue(t);
        }

        IMapBasedRecord gr = (IMapBasedRecord) convertObject(values.peek());
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(gr.getFullName());
        return objectValue(Optional.of(cm), templateFieldSuppliers);
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<ClassUtils.ClassMeta> cmOpt, Map<String, Function> templateFieldSuppliers) {

        ClassUtils.ClassMeta cm = cmOpt.get();
        return readType(cm, templateFieldSuppliers);
    }


    private Object processUnionField(Builder builder, String fieldName, IDataType unionTypeMeta) {
        if (unionTypeMeta instanceof OptionalDataType) {
            OptionalDataType odt = (OptionalDataType) unionTypeMeta;
            Object obj = processUnionField(builder, fieldName, odt.getComponentType());
            return Optional.of(obj);
        } else if (unionTypeMeta instanceof ListDataType) {
            ListDataType odt = (ListDataType) unionTypeMeta;
            Object obj = processUnionField(builder, fieldName, odt.getComponentType());
            return Arrays.asList(obj);
        } else if (unionTypeMeta instanceof UnionDataType) {
            UnionDataType udt = (UnionDataType) unionTypeMeta;

            ClassUtils.ClassMeta unionMeta = ClassUtils.getMeta(udt.getSynthFullyQualifiedName());
            FieldMeta<AlfaObject> unionFieldMeta = unionMeta.getModel().getAllFieldsMeta().get(fieldName);

            Builder unionBuilder = unionMeta.getNewBuilder(BuilderConfig.getInstance());

            fieldValue(unionFieldMeta.getField(), unionBuilder, unionFieldMeta.getConsumer().get());

            return unionBuilder.build();
        } else {
            UdtDataType udt = (UdtDataType) unionTypeMeta;

            ClassUtils.ClassMeta unionMeta = ClassUtils.getMeta(udt.getFullyQualifiedName());
            FieldMeta<AlfaObject> unionFieldMeta = unionMeta.getModel().getAllFieldsMeta().get(fieldName);

            Builder unionBuilder = unionMeta.getNewBuilder(BuilderConfig.getInstance());

            fieldValue(unionFieldMeta.getField(), unionBuilder, unionFieldMeta.getConsumer().get());

            return unionBuilder.build();
        }
    }

    private <T extends AlfaObject> T readType(ClassUtils.ClassMeta cm, Map<String, Function> templateFieldSuppliers) {
        Builder builder = cm.getNewBuilder(super.getCodecConfig());
        IMapBasedRecord gr = (IMapBasedRecord) convertObject(values.peek());

        String keyField = getCodecConfig().getMetaFieldPrefix() + "key";

        TypeDescriptor _keyDesc = null;
        ClassUtils.ClassMeta _keyMeta = null;
        Builder _kbuilder = null;

        if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.entityType) {
            _keyDesc = cm.getModel().getEntityKeyModel().get();
            _keyMeta = ClassUtils.getMeta(_keyDesc.getUdtDataType().getFullyQualifiedName());
            _kbuilder = _keyMeta.getNewBuilder(getCodecConfig());
        }

        final TypeDescriptor keyDesc = _keyDesc;
        final ClassUtils.ClassMeta keyMeta = _keyMeta;
        final Builder kbuilder = _kbuilder;

        Holder<Key> k = new Holder<>();

        List<String> errors = new ArrayList<>();

        for (String fieldName : gr.getFields()) {
            try {
                if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.entityType && fieldName.equals(keyField)) {
                    Object ak = gr.get(keyField);
                    values.push(ak);
                    k.setValue(objectValue(keyDesc.getUdtDataType(), Collections.emptyMap()));
                    values.pop();
                } else if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.entityType &&
                        !getCodecConfig().isWriteEntityKeyAsObject() &&
                        keyDesc.getAllFieldsMeta().containsKey(fieldName)
                ) {
                    readTypeField(keyMeta, templateFieldSuppliers, kbuilder, gr, fieldName);
                } else if (gr.get(fieldName) == null && cm.getModel().getAllFieldsMeta().containsKey(fieldName) &&
                        !(cm.getModel().getAllFieldsMeta().get(fieldName).getDataType() instanceof OptionalDataType)) {
                    throw new Exception("Mandatory value missing");
                } else {
                    readTypeField(cm, templateFieldSuppliers, builder, gr, fieldName);
                }
            } catch (Exception t) {
                String msg = t.getMessage() == null ? t.getClass().getName() : t.getMessage();
                String err = "Field:" + fieldName + "; Value:" + gr.get(fieldName) + "; Error:" + msg;

                if (Logger.getOrCreateDefault().isTraceEnabled())
                    t.printStackTrace();

                errors.add(err);
            }
        }

        if (errors.size() > 0) {
            throw new RuntimeException(String.join(" | ", errors));
        }

        if (k.isSet()) {
            com.schemarise.alfa.runtime.EntityBuilder eb = (com.schemarise.alfa.runtime.EntityBuilder) builder;
            eb.set$key(k.getValue());
        } else if (kbuilder != null) {
            com.schemarise.alfa.runtime.EntityBuilder eb = (com.schemarise.alfa.runtime.EntityBuilder) builder;
            eb.set$key(kbuilder.build());
        }


        return (T) builder.build();
    }

    private void readTypeField(ClassUtils.ClassMeta cm, Map<String, Function> templateFieldSuppliers, Builder builder, IMapBasedRecord gr, String fieldName) {
        Function tmpl = templateFieldSuppliers.get(fieldName);

        if (tmpl != null) {
            Object val = tmpl.apply(this);
            builder.modify(fieldName, val);
        } else {
            FieldMeta<AlfaObject> fieldMeta = cm.getModel().getAllFieldsMeta().get(fieldName);

            if (this.getCodecConfig().isSkipUnknownFields() && fieldMeta == null) {
                // skip
            } else {
                if (fieldMeta == null) {

//                            Optional<String> unionFieldContainer = cm.getModel().getFieldContainingNestedUnionField(fieldName);
//
//                            if (unionFieldContainer.isPresent()) {
//                                FieldMeta<AlfaObject> unionTypeMeta = cm.getModel().getAllFieldsMeta().get(unionFieldContainer.get());
//                                Object ao = processUnionField(builder, unionFieldContainer.get(), unionTypeMeta.getDataType());
//                                builder.modify(unionFieldContainer.get(), ao);
//                            }


                    throw new AlfaRuntimeException(ConstraintType.UnknownField,
                            "Unknown field '" + fieldName + "' in " + gr.getFullName());

                }

                values.push(convertObject(gr.get(fieldName)));

                fieldValue(fieldMeta.getField(), builder, fieldMeta.getConsumer().get());

                values.pop();
            }
        }
    }

    protected Object convertObject(Object o) {
        return o;
    }

    @Override
    public <K, V> Map<K, V> mapValue(MapDataType t, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {
        Object o = values.peek();

        if (o instanceof Map) {
            Map l = (Map) values.peek();

            Map<K, V> res = new LinkedHashMap<>(l.size());
            l.forEach((k, v) -> {
                values.push(parseStringToType(t.getKeyType(), k));
                K decodedKey = kc.apply(this);
                values.pop();

                values.push(v);
                V decodedVal = vc.apply(this);
                values.pop();

                res.put(decodedKey, decodedVal);
            });
            return res;
        } else {
            List l = (List) values.peek();

            Map<K, V> res = new LinkedHashMap<>(l.size());
            l.forEach((e) -> {

                IMapBasedRecord rec = (IMapBasedRecord) convertObject(e);

                String keyName = t.getKeyName().orElse("key");
                String valName = t.getValueName().orElse("val");

                Object k = rec.get(keyName);
                Object v = rec.get(valName);

                values.push(k);
                K decodedKey = kc.apply(this);
                values.pop();

                values.push(v);
                V decodedVal = vc.apply(this);
                values.pop();

                res.put(decodedKey, decodedVal);
            });
            return res;

        }
    }

    private Object parseStringToType(IDataType keyType, Object k) {
        ScalarDataType sdt = (ScalarDataType) keyType;

        String s = k.toString();

        switch (sdt.getScalarType()) {
            case stringType:
                return s;
            case intType:
                return Integer.parseInt(s);
            case shortType:
                return Short.parseShort(s);
            case longType:
                return Long.parseLong(s);
            case doubleType:
                return Double.parseDouble(s);
            case decimalType:
                return new BigDecimal(s);
//            case uriType:
//                return URI.create(s);
            case uuidType:
                return UUID.fromString(s);
            case datetimeType:
                return LocalDateTime.parse(s);
            case dateType:
                return LocalDate.parse(s);
            case timeType:
                return LocalTime.parse(s);
            case durationType:
                return NormalizedPeriod.of(Period.parse(s));
            case booleanType:
                return Boolean.parseBoolean(s);
//            case floatType:
//                return Float.parseFloat(s);
//            case patternType:
//                return s;
        }

        throw new AlfaRuntimeException("Unhandled type " + s);
    }

    @Override
    public <T> Set<T> setValue(SetDataType f, Function<DataSupplier, T> consumer) {
        List l = (List) values.peek();

        Set<T> res = new LinkedHashSet<>(l.size());
        l.forEach(e -> {
            values.push(e);
            res.add(consumer.apply(this));
            values.pop();
        });
        return res;
    }

    @Override
    public <T> List<T> listValue(ListDataType f, Function<DataSupplier, T> consumer) {
        Object o = values.peek();

        // value read may not always be a List when reading XML - of only 1 item is read, it will be a single object

        if (o instanceof List) {
            List l = (List) values.peek();

            List<T> res = new ArrayList<>(l.size());
            l.forEach(e -> {
                values.push(e);
                res.add(consumer.apply(this));
                values.pop();
            });
            return res;
        } else {
            List<T> res = new ArrayList<>(1);
            res.add(consumer.apply(this));
            return res;
        }
    }

    @Override
    public <T> Optional<T> optionalValue(OptionalDataType f, Function<DataSupplier, T> c) {
        Object p = values.peek();

        if (p != null) {
            // empty string makes sense for string, but other scalars having "" means no value
            if (p instanceof String && ((String) p).length() == 0) {
                if (f.getComponentType() instanceof ScalarDataType &&
                        ((ScalarDataType) f.getComponentType()).getScalarType() != ScalarType.stringType)
                    return Optional.empty();
            }

            return Optional.of(c.apply(this));
        }
        return Optional.empty();
    }

    @Override
    public <T> Compressed compressedValue(Function<DataSupplier, T> compressedConsumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Encrypted<T> encryptedValue(Function<DataSupplier, T> encryptedConsumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> streamValue(StreamDataType std, Function<DataSupplier, T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> futureValue(FutureDataType fdt, Function<DataSupplier, T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ITable tableValue(TabularDataType tdt, Function<DataSupplier, T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <L, R> Pair<L, R> pairValue(PairDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <L, R> Either<L, R> eitherValue(EitherDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        throw new UnsupportedOperationException();
    }

}
