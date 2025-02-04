package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.Entity;
import com.schemarise.alfa.runtime.Enum;
import com.schemarise.alfa.runtime.Key;
import com.schemarise.alfa.runtime.Union;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import com.schemarise.alfa.runtime.codec.OptSetChecker;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.json.JsonTypeWriteMode;
import com.fasterxml.jackson.core.JsonGenerator;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

final class JsonDataConsumer extends DataConsumer {
    private Map<String, DateTimeFormatter> dateFormatMap = new HashMap<>();
    private Set<String> checksumsWritten = new HashSet<>();

    JsonDataConsumer(JsonCodecConfig jwc, JsonGenerator jGenerator, OutputStream stream) {
        this.jGenerator = new JsonGeneratorWrapper(jGenerator);
        this.jsonCfg = jwc;
        this.stream = stream;
    }


    public OutputStream closeAndGetBuffer() {
        try {
            jGenerator.close();
            stream.close();
            return stream;
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    @Override
    public void consume(IDataType dst, AlfaObject v, Map<String, BiConsumer> templatedFieldConsumer) {
        nestedLevel++;

        if (v == null && !jsonCfg.assertMandatoryFieldsSet()) {
            // null fields allowed
            jGenerator.writeNull();
            return;
        }

        if (v instanceof com.schemarise.alfa.runtime.Enum) {
            Enum ae = (Enum) v;

            if (ae.getLexicalValue().isPresent())
                jGenerator.writeString(ae.getLexicalValue().get());
            else
                jGenerator.writeString(v.toString());

            return;
        } else if (v instanceof ExternalAlfaObject) {
            ExternalAlfaObject nao = (ExternalAlfaObject) v;
            jGenerator.writeString(nao.encodeToString());
        } else if (v instanceof Union && !((Union) v).isTagged()) {
            Union u = (Union) v;

            Optional<BiConsumer<AlfaObject, DataConsumer>> fs = v.descriptor().getFieldSupplier(u.caseName());
            if (!fs.isPresent())
                throw new AlfaRuntimeException("Supplier not available for field '" + u.caseName() + "'");

            BiConsumer<AlfaObject, DataConsumer> sup = fs.get();
            sup.accept(v, this);

            return;
        } else {
            if (this.jsonCfg.isWriteDetectCycles()) {
                if (visited.contains(v)) {
                    throw new AlfaRuntimeException("Cycle detected");
                }
                visited.add(v);
            }

            jGenerator.writeStartObject();

            boolean writeType = false;

            if (this.jsonCfg.getWriteTypeMode() != JsonTypeWriteMode.NeverWriteType) {
                if ((nestedLevel == 1 && this.jsonCfg.getWriteTypeMode() != JsonTypeWriteMode.NoRootAndMinimal))
                    writeType = true;

                else if (nestedLevel > 1) {
                    if (jsonCfg.getWriteTypeMode() == JsonTypeWriteMode.AlwaysWriteType)
                        writeType = true;
                    else {
                        if (dst instanceof UdtDataType) {
                            UdtDataType udtDt = (UdtDataType) dst;
                            if (udtDt.getUdtType() == UdtMetaType.traitType)
                                writeType = true;
                        }
                    }
                }
            }

            if (writeType) {
                String ver = "";
                if (v.descriptor().getUdtDataType().getVersion().isPresent()) {
                    ver = "@" + v.descriptor().getUdtDataType().getVersion().get();
                }


                String tname = v.descriptor().getUdtDataType().getFullyQualifiedName() + ver;
                jGenerator.writeStringField(this.jsonCfg.getMetaFieldPrefix() + MetaFieldTypeName, tname);

                if (jsonCfg.isWriteModelId() && v.descriptor().getModelId().isPresent()) {
                    jGenerator.writeStringField(this.jsonCfg.getMetaFieldPrefix() + MetaModelId, v.descriptor().getModelId().get());
                }

                if (jsonCfg.isWriteCheckSum() && !checksumsWritten.contains(tname)) {
                    jGenerator.writeStringField(this.jsonCfg.getMetaFieldPrefix() + MetaFieldChecksum, v.descriptor().getChecksum());
                    checksumsWritten.add(tname);
                }
            }

            if (v instanceof Union) {
                com.schemarise.alfa.runtime.Union u = (com.schemarise.alfa.runtime.Union) v;
                java.lang.String c = u.caseName();
                jGenerator.writeFieldName(c);

                BiConsumer sup = templatedFieldConsumer.get(c);
                if (sup != null) {
                    Object cv = u.caseValue();
                    sup.accept(cv, this);
                } else {
                    Optional<BiConsumer<AlfaObject, DataConsumer>> fs = v.descriptor().getFieldSupplier(c);
                    if (!fs.isPresent()) {
                        throw new AlfaRuntimeException("Supplier not available for field '" + c + "'");
                    }
                    sup = fs.get();
                    sup.accept(v, this);
                }

            } else {
                if (v instanceof Entity) {
                    com.schemarise.alfa.runtime.Entity ent = (com.schemarise.alfa.runtime.Entity) v;
                    Optional<? extends com.schemarise.alfa.runtime.Key> k = ent.get$key();
                    if (k.isPresent()) {
                        if (jsonCfg.isWriteEntityKeyAsObject()) {
                            Key ek = k.get();
                            jGenerator.writeFieldName(this.jsonCfg.getMetaFieldPrefix() + "key");

                            declaredAsStack.push(ek.descriptor().getUdtDataType());
                            consume(ek);
                            declaredAsStack.pop();
                        } else {
                            Key ek = k.get();
                            writeObjectFields(ek);
                        }
                    }
                }

                writeObjectFields(v);
            }

            jGenerator.writeEndObject();

            if (this.jsonCfg.isWriteDetectCycles()) {
                visited.remove(v);
            }
        }
        nestedLevel--;
    }

    private void writeObjectFields(AlfaObject v) {
        OptSetChecker checker = new OptSetChecker();

        for (String fn : v.descriptor().getAllFieldsMeta().keySet()) {
            BiConsumer<AlfaObject, DataConsumer> sup = v.descriptor().getFieldSupplier(fn).get();

            FieldMeta<AlfaObject> fmeta = v.descriptor().getAllFieldsMeta().get(fn);
            IDataType fdt = fmeta.getDataType();
            if (fdt instanceof OptionalDataType) {
                sup.accept(v, checker);
                if (!checker.isSet()) {
                    if (jsonCfg.isWriteEmptyOptionalAsNull()) {
                        jGenerator.writeFieldName(fn);
                        jGenerator.writeNull();
                    } else
                        continue;
                }
            }

            jGenerator.writeFieldName(fn);

            declaredAsStack.push(fdt);
            sup.accept(v, this);
            declaredAsStack.pop();
        }
    }

    @Override
    public void consume(ScalarDataType dt, int v) {
        if (jsonCfg.isWriteStringifiedNumbers())
            jGenerator.writeString(String.valueOf(v));
        else
            jGenerator.writeNumber(v);
    }

    @Override
    public void consume(ScalarDataType dt, java.lang.String v) {
        jGenerator.writeString(v);
    }

    @Override
    public void consume(ScalarDataType dt, float v) {
        if (jsonCfg.isWriteStringifiedNumbers())
            jGenerator.writeString(String.valueOf(v));
        else
            jGenerator.writeNumber(v);
    }

    @Override
    public void consume(ScalarDataType dt, double v) {
        if (jsonCfg.isWriteStringifiedNumbers())
            jGenerator.writeString(String.valueOf(v));
        else
            jGenerator.writeNumber(v);
    }

    @Override
    public void consume(ScalarDataType dt, short v) {
        jGenerator.writeNumber(v);
    }

    @Override
    public void consume(ScalarDataType dt, long v) {
        if (jsonCfg.isWriteStringifiedNumbers())
            jGenerator.writeString(String.valueOf(v));
        else
            jGenerator.writeNumber(v);
    }

    @Override
    public void consume(ScalarDataType dt, byte[] v) {
        jGenerator.writeBinary(v);
    }

    @Override
    public void consume(ScalarDataType dt, byte v) {
        jGenerator.writeBinary(new byte[]{v});
    }

    @Override
    public void consume(ScalarDataType dt, char v) {
        jGenerator.writeString("" + v);
    }

    @Override
    public void consume(ScalarDataType dt, boolean v) {
        jGenerator.writeBoolean(v);
    }

    @Override
    public void consume(ScalarDataType dt, BigDecimal v) {
        jGenerator.writeNumber(v);
    }

    private DateTimeFormatter getFormat(String fmt) {
        DateTimeFormatter df = dateFormatMap.get(fmt);
        if (df == null) {
            df = DateTimeFormatter.ofPattern(fmt);
            dateFormatMap.put(fmt, df);
        }
        return df;
    }

    @Override
    public void consume(ScalarDataType dt, LocalDate v) {
        if (v == null)
            jGenerator.writeNull();
        else {
            if (dt.getStrPattern().isPresent() && !jsonCfg.isIgnoreDateFormat()) {
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                jGenerator.writeString(df.format(v));
            } else
                jGenerator.writeString(v.format(DateTimeFormatter.ISO_DATE));
        }
    }

    @Override
    public void consume(ScalarDataType dt, LocalDateTime v) {
        if (v == null)
            jGenerator.writeNull();
        else {
            if (dt.getStrPattern().isPresent() && !jsonCfg.isIgnoreDateFormat()) {
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                jGenerator.writeString(df.format(v));
            } else
                jGenerator.writeString(v.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    @Override
    public void consume(ScalarDataType dt, ZonedDateTime v) {
        if (v == null)
            jGenerator.writeNull();
        else {
            if (dt.getStrPattern().isPresent() && !jsonCfg.isIgnoreDateFormat()) {
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                jGenerator.writeString(df.format(v));
            } else
                jGenerator.writeString(v.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
    }


    @Override
    public void consume(ScalarDataType dt, LocalTime v) {
        if (v == null)
            jGenerator.writeNull();
        else {
            if (dt.getStrPattern().isPresent() && !jsonCfg.isIgnoreDateFormat()) {
                DateTimeFormatter df = getFormat(dt.getStrPattern().get());
                jGenerator.writeString(df.format(v));
            } else
                jGenerator.writeString("" + v.format(DateTimeFormatter.ISO_TIME));
        }
    }

    @Override
    public void consume(ScalarDataType dt, java.time.Duration v) {
        if (v == null)
            jGenerator.writeNull();
        else
            jGenerator.writeString("" + v);
    }

    @Override
    public void consume(ScalarDataType dt, NormalizedPeriod v) {
        if (v == null)
            jGenerator.writeNull();
        else
            jGenerator.writeString("" + v);
    }

    @Override
    public void consume(ScalarDataType dt, UUID v) {
        if (v == null)
            jGenerator.writeNull();
        else
            jGenerator.writeString("" + v);
    }

    @Override
    public void consume(ScalarDataType dt, URI v) {
        if (v == null)
            jGenerator.writeNull();
        else
            jGenerator.writeString("" + v);
    }

    @Override
    public void consume(ScalarDataType dt, UnionUntypedCase v) {
        jGenerator.writeNull();
    }

    @Override
    public <T> void consume(OptionalDataType dt, Optional<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        if (v.isPresent()) {
            declaredAsStack.push(dt.getComponentType());
            elementConsumer.accept(v.get(), this);
            declaredAsStack.pop();
        } else {
            jGenerator.writeNull();
        }
    }

    @Override
    public <T> void consume(CompressedDataType dt, Compressed v, BiConsumer<T, DataConsumer> elementConsumer) {
        // If compressed write compressed value
//        if ( v.hasCompressed()) {
        jGenerator.writeBinary(v.getEncodedBytes());
//        }
//        else {
//            declaredAsStack.push(dt.getComponentType());
//            elementConsumer.accept((T)v.getValue(jsonCfg), this );
//            declaredAsStack.pop();
//        }
    }

    @Override
    public <T> void consume(EncryptedDataType dt, Encrypted<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        // If compressed write compressed value
//        if ( v.hasEncrypted()) {
        jGenerator.writeBinary(v.getEncodedBytes());
//        }
//        else {
//            declaredAsStack.push(dt.getComponentType());
//            elementConsumer.accept((T) v.getValue(jsonCfg), this );
//            declaredAsStack.pop();
//        }
    }

    @Override
    public <K, V> void consume(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        if (dt.getKeyType() instanceof ScalarDataType && jsonCfg.isWriteMapAsObject()) {
            consumeScalarMapKeyed(dt, v, keyConsumer, valueConsumer);
        } else {
            if (dt.getKeyType() instanceof UdtDataType) {
                UdtDataType udt = (UdtDataType) dt.getKeyType();
                ClassUtils.ClassMeta cm = ClassUtils.getMeta(udt.getFullyQualifiedName());
                if (cm.getModel().getUdtDataType().getUdtType() == UdtMetaType.enumType) {
                    consumeScalarMapKeyed(dt, v, keyConsumer, valueConsumer);
                    return;
                }
            }
            jGenerator.writeStartArray();

            String kn = dt.getKeyName().orElse(CompoundMapKeyField);
            String vn = dt.getValueName().orElse(CompoundMapValField);

            v.entrySet().forEach(e -> {
                jGenerator.writeStartObject();
                jGenerator.writeFieldName(kn);

                declaredAsStack.push(dt.getKeyType());
                keyConsumer.accept(e.getKey(), this);
                declaredAsStack.pop();

                jGenerator.writeFieldName(vn);

                declaredAsStack.push(dt.getValueType());
                valueConsumer.accept(e.getValue(), this);
                declaredAsStack.pop();

                jGenerator.writeEndObject();
            });
            jGenerator.writeEndArray();
        }
    }

    private <K, V> void consumeScalarMapKeyed(MapDataType dt, Map<K, V> v, BiConsumer<K, DataConsumer> keyConsumer, BiConsumer<V, DataConsumer> valueConsumer) {
        if (v == null && !jsonCfg.assertMandatoryFieldsSet()) {
            jGenerator.writeNull();
            return;
        }

        jGenerator.writeStartObject();
        v.entrySet().forEach(e -> {
            declaredAsStack.push(dt.getKeyType());
            keyConsumer.accept(e.getKey(), scalarConsumer);
            declaredAsStack.pop();

            declaredAsStack.push(dt.getValueType());
            valueConsumer.accept(e.getValue(), this);
            declaredAsStack.pop();
        });
        jGenerator.writeEndObject();
    }

    @Override
    public <T> void consume(SetDataType dt, Set<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        if (v == null && !jsonCfg.assertMandatoryFieldsSet()) {
            jGenerator.writeNull();
            return;
        }

        jGenerator.writeStartArray();

        v.forEach(e -> {
            declaredAsStack.push(dt.getComponentType());
            elementConsumer.accept(e, this);
            declaredAsStack.pop();
        });

        jGenerator.writeEndArray();

    }

    @Override
    public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
        jGenerator.writeStartArray();
        v.forEach(e -> {
            declaredAsStack.push(dt.getComponentType());
            elementConsumer.accept(e, this);
            declaredAsStack.pop();
        });
        jGenerator.writeEndArray();
    }

    @Override
    public <T> void consume(StreamDataType dt, List<T> f1, BiConsumer<T, DataConsumer> elementConsumer) {
        jGenerator.writeStartArray();
        f1.forEach(e -> {
            declaredAsStack.push(dt.getComponentType());
            elementConsumer.accept(e, this);
            declaredAsStack.pop();
        });
        jGenerator.writeEndArray();
    }

    @Override
    public <T> void consume(FutureDataType dt, Future<T> f1, BiConsumer<T, DataConsumer> consumer) {
        try {
            declaredAsStack.push(dt.getComponentType());
            consumer.accept(f1.get(), this);
            declaredAsStack.pop();
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.Unknown, e);
        }
    }

    @Override
    public <T> void consume(MetaDataType dt, T f1) {
        switch (dt.getMetaType()) {
            case Udt:
            case Key:
            case Trait:
            case Entity:
            case Record:
            case Union:
                consume((AlfaObject) f1);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public <T> void consume(TabularDataType dt, ITable f1, BiConsumer<T, DataConsumer> consumer) {
        throw new UnsupportedOperationException();
    }


    class ScalarConsumer extends NoOpDataConsumer {
        @Override
        public void consume(UdtDataType dt, AlfaObject v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, int v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, String v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, double v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, float v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, short v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, long v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, byte v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, byte[] v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, char v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, boolean v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, BigDecimal v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, LocalDate v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, LocalDateTime v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, LocalTime v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, Duration v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, UUID v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, URI v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }

        @Override
        public void consume(ScalarDataType dt, UnionUntypedCase v) {
            jGenerator.writeFieldName(String.valueOf(v));
        }
    }

    static String CompoundMapKeyField = "key";
    static String CompoundMapValField = "val";
    static String MetaFieldTypeName = "type";
    static String MetaFieldChecksum = "csum";
    static String MetaModelId = "modelid";

    static String MetaFieldVersion = "ver";

    static String MetaFieldId = "id";

    static String MetaFieldIdRef = "idref";

    static Set<String> MetaFieldNames = new HashSet<>(Arrays.asList(
            MetaFieldTypeName, MetaFieldChecksum, MetaModelId, MetaFieldVersion, MetaFieldId, MetaFieldIdRef, CompoundMapKeyField));

    private final OutputStream stream;
    private JsonGeneratorWrapper jGenerator;
    private final JsonCodecConfig jsonCfg;
    private Set<AlfaObject> visited = new HashSet<>();
    private Stack<IDataType> declaredAsStack = new Stack<IDataType>();
    private int nestedLevel = 0;
    ScalarConsumer scalarConsumer = new ScalarConsumer();

}
