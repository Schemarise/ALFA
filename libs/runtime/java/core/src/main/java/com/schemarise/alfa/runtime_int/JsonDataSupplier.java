package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Enum;
import com.schemarise.alfa.runtime.Key;

import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.json.IJsonParserWrapper;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;
import com.schemarise.alfa.runtime.utils.MRUHashMap;
import com.schemarise.alfa.runtime.utils.Utils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import static schemarise.alfa.runtime.model.asserts.ConstraintType.DataFormatError;

final class JsonDataSupplier extends DataSupplier {
    private final JsonCodecConfig jsonCfg;

    public JsonDataSupplier(JsonCodecConfig jwc, JsonParser parser) {
        super(jwc);
        this.jsonCfg = jwc;
        this.parser = new JsonParserWrapper(parser);
    }

    @Override
    public int intValue(ScalarDataType scalarDataType) {
        int i;

        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            i = Integer.valueOf(getSafeText());
        } else
            i = parser.getIntValue();

        return i;
    }

    @Override
    public java.lang.String stringValue(ScalarDataType scalarDataType) {

//        if ( getCodecConfig().isXmlFeaturesEnabled() ) {
//            while ( parser.currentToken() != JsonToken.VALUE_STRING )
//                parser.nextToken();
//        }
//
//        if (parser.currentToken() != JsonToken.VALUE_STRING) {
//            if ( getCodecConfig().getJsonReaderRecovery().isPresent() ) {
//                var custom = getCodecConfig().getJsonReaderRecovery().get().stringValue(parser);
//                if ( custom != null ) {
//                    return custom;
//                }
//            }
//
//            throw new AlfaRuntimeException(ConstraintType.InvalidTypeForField,
//                    "Expected string, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());
//        }
//        String s = parser.getText();
//
//        if (getCodecConfig().isVerbose()) {
//            log("read string:" + s);
//        }

        return getSafeText();
    }

    protected JsonCodecConfig getCodecConfig() {
        return (JsonCodecConfig) super.getCodecConfig();
    }

    @Override
    public double doubleValue(ScalarDataType scalarDataType) {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
            return Double.valueOf(getSafeText());
        } else
            return parser.getDoubleValue();
    }

    @Override
    public short shortValue(ScalarDataType scalarDataType) {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            return Short.valueOf(getSafeText());
        } else
            return parser.getShortValue();
    }

    @Override
    public long longValue(ScalarDataType scalarDataType) {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            return Long.valueOf(getSafeText());
        } else
            return parser.getLongValue();
    }

//    @Override
//    public BigDecimal bigDecimalValue(ScalarDataType scalarDataType) {
//        return parser.getBigDecimalValue();
//    }

    @Override
    public byte byteValue(ScalarDataType scalarDataType) {
        return parser.getByteValue();
    }

    @Override
    public char charValue(ScalarDataType scalarDataType) {
        return parser.getTextCharacters()[0];
    }

    @Override
    public boolean booleanValue(ScalarDataType scalarDataType) {
        var t = parser.currentToken();
        if (t != JsonToken.VALUE_FALSE && t != JsonToken.VALUE_TRUE) {
            return Boolean.valueOf(getSafeText());
        } else
            return parser.getBooleanValue();
    }

    @Override
    public BigDecimal decimalValue(ScalarDataType scalarDataType) {
        return parser.getBigDecimalValue();
    }

    @Override
    public LocalDate dateValue(ScalarDataType t) {
        return cachedDateValue(t, getSafeText());
    }

    @Override
    public LocalDateTime datetimeValue(ScalarDataType dt) {
        java.lang.String t = getSafeText();

        LocalDateTime sdt;

        String v = "yyyy-MM-dd'T'HH:mm:ss.SSS";

        try {
            if (dt.getStrPattern().isPresent() && !getCodecConfig().isIgnoreDateFormat()) {
                v = dt.getStrPattern().get();
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                sdt = datetimeValue(dt, t, df, v);
            } else {
                sdt = datetimeValue(dt, t, DateTimeFormatter.ISO_DATE_TIME, v);
            }
        } catch (DateTimeException e) {
            throw new AlfaRuntimeException(DataFormatError, "Failed expected format '" + v + "' for datetime value '" + t + "'", e);
        }

        return sdt;
    }


    @Override
    public ZonedDateTime datetimetzValue(ScalarDataType dt) {
        java.lang.String t = getSafeText();

        ZonedDateTime sdt;

        String v = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

        try {
            if (dt.getStrPattern().isPresent()) {
                v = dt.getStrPattern().get();
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                sdt = datetimetzValue(dt, t, df, v);
            } else {
                sdt = datetimetzValue(dt, t, DateTimeFormatter.ISO_DATE_TIME, v);
            }
        } catch (DateTimeException e) {
            throw new AlfaRuntimeException(DataFormatError, "Failed expected format '" + v + "' for datetimetz value '" + t + "'", e);
        }

        return sdt;
    }

    private LocalDateTime datetimeValue(ScalarDataType dt, String dateVal, DateTimeFormatter dtFmt, String fmtStr) {
        String k = dateVal + "|" + fmtStr;
        LocalDateTime ldt = datetimesCache.get(k);
        if (ldt != null)
            return ldt;
        else {
            ldt = LocalDateTime.parse(dateVal, dtFmt);
            datetimesCache.put(k, ldt);
            return ldt;
        }
    }

    private ZonedDateTime datetimetzValue(ScalarDataType dt, String dateVal, DateTimeFormatter dtFmt, String fmtStr) {
        String k = dateVal + "|" + fmtStr;
        ZonedDateTime ldt = datetimetzsCache.get(k);
        if (ldt != null)
            return ldt;
        else {
            ldt = ZonedDateTime.parse(dateVal, dtFmt);
            datetimetzsCache.put(k, ldt);
            return ldt;
        }
    }

    @Override
    public LocalTime timeValue(ScalarDataType scalarDataType) {
        java.lang.String t = getSafeText();

        try {
            return LocalTime.parse(t, DateTimeFormatter.ISO_TIME);
        } catch (DateTimeException e) {
            throw new AlfaRuntimeException(DataFormatError, "Failed expected format 'HH:mm[:ss.SSS]' for time value '" + e + "'", e);
        }
    }

    @Override
    public float floatValue(ScalarDataType scalarDataType) {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
            return Float.valueOf(getSafeText());
        } else {
            double d = parser.getDoubleValue();
            if (d > Float.MAX_VALUE)
                throw new NumberFormatException();

            return (float) d;
        }
    }

    @Override
    public byte[] binaryValue(ScalarDataType scalarDataType) {
        return parser.getBinaryValue();
    }

    @Override
    public java.time.Duration durationValue(ScalarDataType scalarDataType) {
        return java.time.Duration.parse(getSafeText());
    }

    @Override
    public NormalizedPeriod periodValue(ScalarDataType scalarDataType) {
        return NormalizedPeriod.of(getSafeText());
    }

    @Override
    public UUID uuidValue(ScalarDataType scalarDataType) {
        java.lang.String t = getSafeText();
        return UUID.fromString(t);
    }

    @Override
    public URI uriValue(ScalarDataType scalarDataType) {
        java.lang.String t = getSafeText();
        return URI.create(t);
    }

    public <T> Optional<T> optionalValue(OptionalDataType f, Function<DataSupplier, T> c) {
        if (parser.currentToken() == JsonToken.VALUE_NULL)
            return Optional.empty();
        else {
            T r = c.apply(this);
            Optional<T> v = Optional.ofNullable(r);
            return v;
        }
    }

    @Override
    public String patternValue(ScalarDataType scalarDataType) {
        String pat = getSafeText();
        return pat;
    }

    @Override
    public UnionUntypedCase voidValue(ScalarDataType scalarDataType) {
        return UnionUntypedCase.getInstance();
    }

    @Override
    public <T extends Enum> T enumValue(EnumDataType t) {
        java.lang.String enumConst = getSafeText();
        java.lang.String n = t.getSynthFullyQualifiedName();
        return (T) ClassUtils.getByEnumConst(n, enumConst);

//        ClassUtils.ClassMeta cm = ClassUtils.getMeta(n);
//
//        try {
//            java.lang.reflect.Field field = cm.getTypeClass().getDeclaredField(enumConst);
//            java.lang.Enum v = java.lang.Enum.valueOf((Class<java.lang.Enum>) field.getType(), enumConst);
//            return ( T ) v;
//        } catch (Exception e) {
//            throw new RuntimeException();
//        }
    }

    @Override
    public <T extends com.schemarise.alfa.runtime.Union> T unionValue(UnionDataType t) {
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(t.getSynthFullyQualifiedName());

        if (t.getUnionType() == UnionType.Tagged) {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                java.lang.String fieldname = parser.getCurrentName();
                if (fieldname != null && fieldname.equals(getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.MetaFieldTypeName)) {
                    parser.nextToken();
                    java.lang.String type = getSafeText();
                    return readType(Optional.of(cm), Optional.of(type), Collections.emptyMap());
                } else {
                    parser.pushBackLastToken();
                    return readType(Optional.of(cm), Optional.empty(), Collections.emptyMap());
                }
            }
        } else {
            return readUntaggedValue(t.getSynthFullyQualifiedName());
        }

        throw new AlfaRuntimeException(NoType);
    }

    @Override
    public <T> T metaValue(MetaDataType t) {
        return null;
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t) {
        try {
            return objectValue(t, Collections.emptyMap());
        } catch (AlfaRuntimeException a) {
            throw a.appendMessage(" at " + parser.getCurrentLocationStr());
        }
    }

    @Override
    public <T extends com.schemarise.alfa.runtime.Record> T tupleValue(TupleDataType t) {
        ClassUtils.ClassMeta cm = ClassUtils.getMeta(t.getSynthFullyQualifiedName());

        // read the $type of the tuple
        if (parser.currentToken() == JsonToken.START_OBJECT) {
            parser.nextToken();

            String fieldname = parser.getCurrentName();

            if (fieldname != null && fieldname.equals(this.getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.MetaFieldTypeName)) {
                parser.nextToken();
                java.lang.String type = getSafeText();
                if (!t.getSynthFullyQualifiedName().equals(type))
                    throw new AlfaRuntimeException(this.getCodecConfig().getMetaFieldPrefix() + "type " + type + " mismatch with supplied type " + t.getSynthFullyQualifiedName());
            } else {
                parser.pushBackLastToken();
            }
        }

        return readType(Optional.of(cm), Optional.empty(), Collections.emptyMap());

//        while (parser.nextToken() != JsonToken.END_OBJECT) {
//            java.lang.String fieldname = parser.getCurrentName();
//            if ( fieldname != null && fieldname.equals( getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.TypeMetaField) ) {
//                parser.nextToken();
//                java.lang.String type = parser.getText();
//
//                ClassUtils.ClassMeta cm = ClassUtils.getMeta(t.getSynthFullyQualifiedName());
//
//                return readType( Optional.of(cm), Optional.of(type), Collections.emptyMap() );
//            } else {
//                Field f = t.getFields().get(fieldname);
//                if ( f == null ) {
//                    throw new AlfaRuntimeException("Unknown tuple field " + fieldname);
//                }
//                IDataType dt = f.getDataType();
//
//                ClassUtils.ClassMeta cm = ClassUtils.getMeta(f..getSynthFullyQualifiedName());
//                return readType( Optional.of(cm), Optional.of(type), Collections.emptyMap() );
//            }
//        }
//
//        throw new AlfaRuntimeException(NoType);
    }


    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t, Map<String, Function> templateFieldSuppliers) {
        Optional<ClassUtils.ClassMeta> cm = Optional.of(ClassUtils.getMeta(t.getFullyQualifiedName()));
        return objectValue(cm, templateFieldSuppliers);
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<ClassUtils.ClassMeta> cm, Map<String, Function> templateFieldSuppliers) {

        if (cm.isPresent()) {
            UdtDataType ut = cm.get().getModel().getUdtDataType();

            if (ut.getUdtType() == UdtMetaType.enumType) {

                java.lang.String enConst = getSafeText();

//                if (parser.currentToken() != JsonToken.VALUE_STRING) {
//                    if (getCodecConfig().getJsonReaderRecovery().isPresent()) {
//                        var custom = getCodecConfig().getJsonReaderRecovery().get().stringValue(parser);
//                        if (custom != null) {
//                            enConst = custom;
//                        }
//                    }
//                }
//                else {
//                    enConst = getSafeText();
//                }

                if (enConst == null)
                    throw new AlfaRuntimeException("Could not read enum value " + parser.currentToken() + " at " + parser.getCurrentLocationStr());

                if (getCodecConfig().isVerbose())
                    log("Enum value:" + enConst);

                return (T) ClassUtils.getByEnumConst(ut.getFullyQualifiedName(), enConst);
            } else if (ut.getUdtType() == UdtMetaType.nativeUdtType) {
                java.lang.String nativeValue = getSafeText();

                if (getCodecConfig().isVerbose())
                    log("Native value:" + nativeValue);


                Builder b = cm.get().getNewBuilder(super.getCodecConfig());
                b.modify(null, nativeValue);
                return b.build();
            } else if (ut.getUdtType() == UdtMetaType.untaggedUnionType) {
                return readUntaggedValue(ut.getFullyQualifiedName());
            }
        }


        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                continue;
            }

            String fieldname = parser.getCurrentName();

            if (fieldname != null && fieldname.equals(this.getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.MetaFieldTypeName)) {
                parser.nextToken();
                java.lang.String type = getSafeText();
                return readType(cm, Optional.of(type), templateFieldSuppliers);
            } else if (cm.isPresent()) {
                parser.pushBackLastToken();
                return readType(cm, Optional.empty(), templateFieldSuppliers);
            }
        }

        if (cm.isPresent()) {
            Collection<FieldMeta<AlfaObject>> flds = cm.get().getModel().getAllFieldsMeta().values();
            long optFlds = flds.stream().filter(e -> e.getDataType() instanceof OptionalDataType).count();
            if (flds.size() == optFlds) // all fields optional and nothing set
                return cm.get().getNewBuilder(getCodecConfig()).build();
        }

        throw new AlfaRuntimeException("Did not see valid JSON object. Current token:" + parser.currentToken() + " at " + parser.getCurrentLocationStr());
    }

    private <T extends AlfaObject> T readUntaggedValue(String fqn) {
        int tkn = parser.currentToken().id();
        ClassUtils.ClassMeta ucm = ClassUtils.getMeta(fqn);

        UntaggedUnionBuilder ub = (UntaggedUnionBuilder) ucm.getNewBuilder(super.getCodecConfig());

        if (tkn == JsonToken.VALUE_STRING.id()) {
            ub.setByType(getSafeText());
        } else if (tkn == JsonToken.VALUE_NUMBER_INT.id()) {
            long l = parser.getLongValue();

            if (l < Integer.MAX_VALUE)
                ub.setByType((int) l);
            else
                ub.setByType(l);
        } else if (tkn == JsonToken.VALUE_NUMBER_FLOAT.id()) {
            ub.setByType(parser.getDoubleValue());
        } else if (tkn == JsonToken.VALUE_FALSE.id() || tkn == JsonToken.VALUE_TRUE.id()) {
            ub.setByType(parser.getBooleanValue());
        } else if (tkn == JsonToken.START_OBJECT.id()) {
            JsonToken ntSkip = parser.nextToken();
            String ns = getSafeText();

            String cname = fqn.substring(0, fqn.lastIndexOf(".") + 1) + ns;

            AlfaObject t = readType(Optional.of(ClassUtils.getMeta(cname)), Optional.empty(), Collections.emptyMap());

            JsonToken endSkip = parser.nextToken();

            ub.setByType(t);
        } else if (tkn == JsonToken.START_ARRAY.id()) {
            // TODO test untagged union with an array
        } else {
            throw new AlfaRuntimeException("Did not see valid JSON object in untagged value for type " + fqn + " at " + parser.getCurrentLocation());
        }

        return ub.build();
    }

//    @Override
//    public <T extends  com.schemarise.alfa.runtime.Key> T keyValue(KeyDataType t) {
//        UdtDataType udt = (UdtDataType) t.getComponentType();
//
//        ClassUtils.ClassMeta emtityCm = ClassUtils.getMeta(udt.getFullyQualifiedName());
//        Optional<TypeDescriptor> keyModel = emtityCm.getModel().getEntityKeyModel();
//        ClassUtils.ClassMeta keyCm = ClassUtils.getMeta(keyModel.get().getUdtDataType().getFullyQualifiedName());
//
//        while (parser.nextToken() != JsonToken.END_OBJECT) {
//            java.lang.String fieldname = parser.getCurrentName();
//            if ( fieldname != null && fieldname.equals(JsonDataConsumer.TypeMetaField) ) {
//                parser.nextToken();
//                java.lang.String type = parser.getText();
//                return readType( Optional.of(keyCm), Optional.of(type), Collections.emptyMap() );
//            }
//            else {
//                parser.pushBackLastToken();
//                return readType( Optional.of(keyCm), Optional.empty(), Collections.emptyMap() );
//            }
//        }
//
//        throw new AlfaRuntimeException(NoType);
//    }

    private <K, V> Map<K, V> mapValueScalarKey(MapDataType dt, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {
        Map<K, V> m = new LinkedHashMap<>();
        ScalarAsFieldSupplier con = new ScalarAsFieldSupplier(getCodecConfig());

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() == JsonToken.START_OBJECT)
                continue;

            K k = kc.apply(con);

            JsonToken t2 = parser.nextToken();
            V v = vc.apply(this);
            V old = m.put(k, v);
            if (old != null) {
                throw new AlfaRuntimeException(ConstraintType.Duplicate, "Duplicate key in map " + k);
            }

        }

        return Collections.unmodifiableMap(m);
    }

    @Override
    public <K, V> Map<K, V> mapValue(MapDataType dt, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {

        boolean scalarKey = dt.getKeyType() instanceof ScalarDataType;

        if (scalarKey &&
                jsonCfg.isWriteMapAsObject() &&
                parser.currentToken() != JsonToken.START_OBJECT) {
            throw new AlfaRuntimeException("Expected map start, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());
        }

        if (parser.currentToken() == JsonToken.START_OBJECT) {
            return mapValueScalarKey(dt, kc, vc);
        } else if (parser.currentToken() == JsonToken.START_ARRAY) {
            if (dt.getKeyType() instanceof UdtDataType) {
                UdtDataType udt = (UdtDataType) dt.getKeyType();
                ClassUtils.ClassMeta cm = ClassUtils.getMeta(udt.getFullyQualifiedName());
                if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.enumType) {
                    return mapValueScalarKey(dt, kc, vc);
                }
            }

            long limit = Long.MAX_VALUE;
            if (dt.getSizeMax().isPresent())
                limit = dt.getSizeMax().get();

            long counter = 0;

            Map<K, V> m = new LinkedHashMap<>();

            String kn = dt.getKeyName().orElse(JsonDataConsumer.CompoundMapKeyField);
            String vn = dt.getValueName().orElse(JsonDataConsumer.CompoundMapValField);

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                parser.nextToken(); // skip {
                String keyf = parser.getCurrentName();
                if (!keyf.equals(kn))
                    throw new AlfaRuntimeException(ConstraintType.UnknownField, "Unknown map key field name " + keyf);

                parser.nextToken();
                K k = kc.apply(this);
                parser.nextToken();

                String valf = parser.getCurrentName();
                if (!valf.equals(vn))
                    throw new AlfaRuntimeException(ConstraintType.UnknownField, "Unknown map value field name " + valf);
                parser.nextToken();
                V v = vc.apply(this);

                parser.nextToken(); // skip }

                if (counter + 1 > limit) {
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(ConstraintType.OutsidePermittedRange, "Cannot insert to map<K,V>. Maximum limit " + limit +
                            " reached on field '" + currentFieldName() + "'.");
                }

                V old = m.put(k, v);
                if (old != null) {
                    throw new AlfaRuntimeException(ConstraintType.Duplicate, "Duplicate key in map " + k);
                }

                counter++;
            }
            return Collections.unmodifiableMap(m);
        } else {
            throw new AlfaRuntimeException("Expected map or array start, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());
        }
    }


    private void log(String m) {
        System.out.println(m);
    }
//    private <T extends AlfaObject> T readType(UdtDataType udt, Optional<java.lang.String> typeNameInData) {
//        Optional<ClassUtils.ClassMeta> cm = Optional.of( ClassUtils.getMeta( udt.getFullyQualifiedName() ) );
//        return readType( cm, typeNameInData, Collections.emptyMap());
//    }

    private <T extends AlfaObject> T readType(Optional<ClassUtils.ClassMeta> expectedType, Optional<java.lang.String> typeNameInData, Map<String, Function> templateFieldSuppliers) {
        ClassUtils.ClassMeta cm;

        if (expectedType.isPresent()) {
            if (typeNameInData.isPresent()) {
                ClassUtils.ClassMeta dataCm = ClassUtils.getMeta(typeNameInData.get());
                expectedType.get().assertCompatible(dataCm);
                cm = dataCm;
            } else
                cm = expectedType.get();
        } else if (typeNameInData.isPresent())
            cm = ClassUtils.getMeta(typeNameInData.get());
        else
            throw new AlfaRuntimeException("No expected type or type declared in the data");

        if (typeNameInData.isPresent() && cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.traitType) {
            throw new AlfaRuntimeException(ConstraintType.InvalidTypeForField,
                    "Trait cannot be deserialized as a type " + cm.getModel().getUdtDataType().getFullyQualifiedName());
        }

        Builder builder = cm.getNewBuilder(super.getCodecConfig());

        Builder keyBuilder = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                continue;
            }
            if (parser.currentToken() == null) {
                break;
            }
            java.lang.String fieldName = parser.getCurrentName();

            if (fieldName.startsWith(getCodecConfig().getMetaFieldPrefix()) &&
                    getCodecConfig().getFieldNameMapper().apply(fieldName) == null &&
                    !JsonDataConsumer.MetaFieldNames.contains(fieldName.substring(getCodecConfig().getMetaFieldPrefix().length()))) {
                // unknown meta field, skip
                continue;
            }

            if (fieldName.equals(this.getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.MetaFieldChecksum)) {
                parser.nextToken();
                TypeChecksum checksumInJson = new TypeChecksum(getSafeText());
                TypeChecksum localChecksum = new TypeChecksum(builder.descriptor().getChecksum());

                if (!localChecksum.equals(checksumInJson)) {
                    // 1st part must be mismatch if above comparison false
                    if (localChecksum.getMandatoryOnly().equals(checksumInJson.getMandatoryOnly())) {
                        // mandatory fields match, so we'll continue as only optional fields are different
                    } else {
                        // mandatory also mismatched
                        throw new AlfaRuntimeException(DataFormatError, "Incompatible checksums in JSON " + checksumInJson + " and locally " + localChecksum);
                    }
                }

                continue;
            }

            if (getCodecConfig().isVerbose())
                log("Type = " + cm.getModel().getUdtDataType().getFullyQualifiedName() + " FieldName = " + fieldName);

            String dollarType = this.getCodecConfig().getMetaFieldPrefix() + JsonDataConsumer.MetaFieldTypeName;

            if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.entityType && fieldName.equals(getCodecConfig().getMetaFieldPrefix() + "key")) {
                TypeDescriptor km = cm.getModel().getEntityKeyModel().get();

                Key k = objectValue(km.getUdtDataType(), Collections.emptyMap());
                com.schemarise.alfa.runtime.EntityBuilder eb = (com.schemarise.alfa.runtime.EntityBuilder) builder;
                eb.set$key(k);
            } else if (dollarType.equals(fieldName) && getCodecConfig().isOutOfOrderTypeSpecifierAllowed()) {
                JsonToken t = parser.nextToken();
                continue;
            } else if (fieldName != null) {
                JsonToken t = parser.nextToken();
                // Could be generated to be inlined

                Function tmpl = templateFieldSuppliers.get(fieldName);

                if (tmpl != null) {
                    Object val = tmpl.apply(this);
                    builder.modify(fieldName, val);
                } else {
                    FieldMeta<AlfaObject> fieldMeta = cm.getModel().getAllFieldsMeta().get(fieldName);

                    if (fieldMeta == null && getCodecConfig().getFieldNameMapper().apply(fieldName) != null) {
                        fieldName = getCodecConfig().getFieldNameMapper().apply(fieldName);
                        fieldMeta = cm.getModel().getAllFieldsMeta().get(fieldName);
                    }

                    if (fieldMeta == null) {
                        if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.entityType &&
                                cm.getModel().getEntityKeyModel().get().getAllFieldsMeta().keySet().contains(fieldName)) {
                            TypeDescriptor km = cm.getModel().getEntityKeyModel().get();

                            FieldMeta<AlfaObject> keyFieldMeta = km.getAllFieldsMeta().get(fieldName);

                            if (keyBuilder == null)
                                keyBuilder = km.builder(getCodecConfig());

                            fieldValue(keyFieldMeta.getField(), keyBuilder, keyFieldMeta.getConsumer().get());
                            continue;
                        }

                        if (this.getCodecConfig().isSkipUnknownFields()) {
                            parser.skipChildren();
                            continue;
                        }

                        if (cm.getModel().getAnnotations().isPresent()) {
                            if (cm.getModel().getAnnotations().get().containsKey("SkipUnknownFields")) {

                                if (getCodecConfig().isVerbose())
                                    log("Skipping unknown field as 'SkipUnknownFields' is set - " + fieldName);

                                parser.skipChildren();
                                continue;
                            }
                        }

                        ValidationAlert.ValidationAlertBuilder va = ValidationAlert.builder().
                                setViolatedConstraint(Optional.of(ConstraintType.UnknownField)).
                                setTypeName(Optional.of(cm.getTypeClass().getName())).
                                setSourceInfo(Optional.of(parser.getCurrentLocationStr())).
                                setFieldName(Optional.of(fieldName)).
                                setMessage("Unknown field for type");

                        getCodecConfig().getAssertListener().addFailure(va);
                        continue;
                    }

                    try {
                        fieldValue(fieldMeta.getField(), builder, fieldMeta.getConsumer().get());
                    } catch (AlfaRuntimeException tx) {
                        tx.setValidationErrorTypeName(cm.getModel().getUdtDataType().getFullyQualifiedName());
                        getCodecConfig().getAssertListener().addFailure(
                                tx.toValidationAlert("Failed to process field '" + fieldName + "'").
                                        setSourceInfo(Optional.of(parser.getCurrentLocationStr())));
                    } catch (Throwable tx) {
                        getCodecConfig().getAssertListener().addFailure(ValidationAlert.builder().
                                setMessage(tx.getClass().getSimpleName() + ":" + tx.getMessage()).
                                setViolatedConstraint(Optional.of(ConstraintType.DataFormatError)).
                                setExceptionDetails(Optional.of(com.schemarise.alfa.runtime.Logger.stacktraceToString(tx, 5))).
                                setSourceInfo(Optional.of(parser.getCurrentLocationStr())));
                    }
                }
            }
        }

        if (keyBuilder != null) {
            com.schemarise.alfa.runtime.EntityBuilder eb = (com.schemarise.alfa.runtime.EntityBuilder) builder;
            eb.set$key(keyBuilder.build());
        }

        T t = (T) builder.build();

        return t;
    }


    @Override
    public <T> Set<T> setValue(SetDataType mdt, Function<DataSupplier, T> c) {
        Set<T> l = new LinkedHashSet<>();

        long limit = Long.MAX_VALUE;
        if (mdt.getSizeMax().isPresent())
            limit = mdt.getSizeMax().get();

        long counter = 0;

        if (parser.currentToken() != JsonToken.START_ARRAY)
            throw new AlfaRuntimeException("Expected array start, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            T v = c.apply(this);
            Utils.assertNotNull(v);

            if (counter + 1 > limit) {
                throw new com.schemarise.alfa.runtime.AlfaRuntimeException(ConstraintType.OutsidePermittedRange,
                        "Cannot insert to set<T>. Maximum limit " + limit +
                                " reached on field '" + currentFieldName() + "'.");
            }

            l.add(v);
            counter++;
        }

        return Collections.unmodifiableSet(l);
    }

    @Override
    public <T> List<T> listValue(ListDataType mdt, Function<DataSupplier, T> c) {
        List<T> l = new ArrayList<>();

        long limit = Long.MAX_VALUE;
        if (mdt.getSizeMax().isPresent())
            limit = mdt.getSizeMax().get();

        long counter = 0;

        if (parser.currentToken() != JsonToken.START_ARRAY)
            throw new AlfaRuntimeException("Expected array start, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            T v = c.apply(this);
            Utils.assertNotNull(v);

            if (counter + 1 > limit) {
                throw new com.schemarise.alfa.runtime.AlfaRuntimeException(ConstraintType.OutsidePermittedRange,
                        "Cannot insert to list<T>. Maximum limit " + limit +
                                " reached on field '" + currentFieldName() + "'.");
            }

            l.add(v);
            counter++;
        }
        ((ArrayList<T>) l).trimToSize();

        return Collections.unmodifiableList(l);
    }

    @Override
    public <T> Compressed compressedValue(Function<DataSupplier, T> compressedConsumer) {
        byte[] bin = parser.getBinaryValue();
        return IntImpl.defaultCompressedFromValue(compressedConsumer, bin);
    }

    @Override
    public <T> Encrypted<T> encryptedValue(Function<DataSupplier, T> encryptedConsumer) {
        byte[] bin = parser.getBinaryValue();
        Encrypted<T> t = IntImpl.defaultEncryptedFromValue(encryptedConsumer, bin);
        return t;
    }

    @Override
    public <T> List<T> streamValue(StreamDataType std, Function<DataSupplier, T> c) {
        List<T> l = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            T v = c.apply(this);
            Utils.assertNotNull(v);
            l.add(v);
        }
        return (List<T>) Arrays.asList(l.toArray());
    }

    @Override
    public <T> Future<T> futureValue(FutureDataType fdt, Function<DataSupplier, T> c) {
        T t = c.apply(this);
        CompletableFuture<T> f = new CompletableFuture<T>();
        f.complete(t);
        return f;
    }

    @Override
    public <T> ITable tableValue(TabularDataType tdt, Function<DataSupplier, T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <L, R> Pair<L, R> pairValue(PairDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        Map<String, Function> m = new HashMap<>();

        m.put("Left", lc);
        m.put("Right", rc);
        AlfaObject ov = objectValue(Optional.empty(), m);
        return (Pair<L, R>) ov;
    }

    @Override
    public <L, R> Either<L, R> eitherValue(EitherDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        Map<String, Function> m = new HashMap<>();

        m.put("Left", lc);
        m.put("Right", rc);
        AlfaObject ov = objectValue(Optional.empty(), m);
        return (Either<L, R>) ov;
    }

//    private LocalDate getDate(String s) {
//        LocalDate d = localDates.get(s);
//        if (d == null) {
//            d = LocalDate.parse(s);
//            localDates.put(s, d);
//        }
//        return d;
//    }

    class ScalarAsFieldSupplier extends NoOpDataSupplier {

        @Override
        public <T extends AlfaObject> T objectValue(UdtDataType t) {
            String fn = parser.getCurrentName();
            return (T) ClassUtils.getByEnumConst(t.getFullyQualifiedName(), fn);
        }

        protected ScalarAsFieldSupplier(CodecConfig jwc) {
            super(jwc);
        }

        @Override
        public int intValue(ScalarDataType sdt) {
            Integer i = Integer.valueOf(parser.getCurrentName());
            if (getCodecConfig().isVerbose())
                log("read int:" + i);
            return i;
        }

        @Override
        public String stringValue(ScalarDataType scalarDataType) {
            String s = parser.getCurrentName();
            if (getCodecConfig().isVerbose())
                log("read string:" + s);
            return s;
        }

        @Override
        public double doubleValue(ScalarDataType sdt) {
            return Double.valueOf(parser.getCurrentName());
        }

        @Override
        public short shortValue(ScalarDataType sdt) {
            return Short.valueOf(parser.getCurrentName());
        }

        @Override
        public long longValue(ScalarDataType sdt) {
            return Long.valueOf(parser.getCurrentName());
        }

//        @Override
//        public BigDecimal bigDecimalValue(ScalarDataType sdt) {
//            return new BigDecimal(parser.getCurrentName());
//        }

        @Override
        public byte byteValue(ScalarDataType sdt) {
            return super.byteValue(sdt);
        }

        @Override
        public char charValue(ScalarDataType sdt) {
            return parser.getCurrentName().charAt(0);
        }

        @Override
        public boolean booleanValue(ScalarDataType sdt) {
            boolean v = Boolean.valueOf(parser.getCurrentName());
            if (getCodecConfig().isVerbose())
                log("read boolean:" + v);

            return v;
        }

        @Override
        public BigDecimal decimalValue(ScalarDataType sdt) {
            return new BigDecimal(parser.getCurrentName());
        }

        @Override
        public LocalDate dateValue(ScalarDataType dt) {
            return cachedDateValue(dt, parser.getCurrentName());
        }

        @Override
        public LocalDateTime datetimeValue(ScalarDataType sdt) {
            return LocalDateTime.parse(parser.getCurrentName());
        }

        @Override
        public ZonedDateTime datetimetzValue(ScalarDataType sdt) {
            return ZonedDateTime.parse(parser.getCurrentName());
        }


        @Override
        public LocalTime timeValue(ScalarDataType sdt) {
            return LocalTime.parse(parser.getCurrentName());
        }

        @Override
        public float floatValue(ScalarDataType sdt) {
            return Float.valueOf(parser.getCurrentName());
        }

        @Override
        public byte[] binaryValue(ScalarDataType sdt) {
            return super.binaryValue(sdt);
        }

        @Override
        public Duration durationValue(ScalarDataType sdt) {
            return Duration.parse(parser.getCurrentName());
        }

        @Override
        public UUID uuidValue(ScalarDataType sdt) {
            return UUID.fromString(parser.getCurrentName());
        }

        @Override
        public URI uriValue(ScalarDataType sdt) {
            return URI.create(parser.getCurrentName());
        }

        @Override
        public String patternValue(ScalarDataType sdt) {
            return parser.getCurrentName();
        }

        @Override
        public <T extends Enum> T enumValue(EnumDataType t) {
            return super.enumValue(t);
        }
    }

    private LocalDate cachedDateValue(ScalarDataType dt, String val) {

        LocalDate sdt = localDates.get(val);

        if (sdt != null)
            return sdt;
        else {
            String v = "yyyy-MM-dd";
            try {
                if (dt.getStrPattern().isPresent()) {
                    v = dt.getStrPattern().get();
                    DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                    sdt = dateValue(dt, val, df, v);
                } else
                    sdt = dateValue(dt, val, DateTimeFormatter.ISO_DATE, v);
            } catch (DateTimeException e) {
                throw new AlfaRuntimeException(DataFormatError, "Failed expected format '" + v + "' for date value '" + e + "'", e);
            }

            localDates.put(val, sdt);
            return sdt;
        }
    }

    private LocalDate dateValue(ScalarDataType dt, String dateVal, DateTimeFormatter dtFmt, String fmtStr) {
        String k = dateVal + "|" + fmtStr;
        LocalDate ldt = localDates.get(k);
        if (ldt != null)
            return ldt;
        else {
            ldt = LocalDate.parse(dateVal, dtFmt);
            localDates.put(k, ldt);
            return ldt;
        }
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

    private String getSafeText() {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            if (getCodecConfig().getJsonReaderRecovery().isPresent()) {
                var custom = getCodecConfig().getJsonReaderRecovery().get().stringValue(parser);
                if (custom != null) {
                    if (getCodecConfig().isVerbose()) {
                        log("read string:" + custom);
                    }
                    return interned(custom);
                } else if (getCodecConfig().isVerbose()) {
                    log("got null string");
                }
            }

            throw new AlfaRuntimeException(ConstraintType.InvalidTypeForField,
                    "Expected string, got " + parser.currentToken() + " at " + parser.getCurrentLocationStr());
        }
        String s = parser.getText();

        if (getCodecConfig().isVerbose()) {
            log("read string:" + s);
        }
        return interned(s);
    }

    private String interned(String s) {
        return s.length() < 50 ? s.intern() : s;
    }

    private final IJsonParserWrapper parser;
    private static final Map<String, LocalDate> localDates = new WeakHashMap<>();
    private static final String NoType = "Did not see $type field";
    private static Map<String, LocalDateTime> datetimesCache = new MRUHashMap(1000);
    private static Map<String, ZonedDateTime> datetimetzsCache = new MRUHashMap(1000);
}
