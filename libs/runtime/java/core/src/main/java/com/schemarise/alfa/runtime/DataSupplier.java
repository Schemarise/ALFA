package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.codec.CodecConfig;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class DataSupplier {

    private Stack<String> currFieldNameStack = new Stack<>();
    private CodecConfig codecConfig;

    protected DataSupplier(CodecConfig jwc) {
        this.codecConfig = jwc;
    }

    protected CodecConfig getCodecConfig() {
        return codecConfig;
    }

    public abstract int intValue(ScalarDataType scalarDataType);

    public abstract java.lang.String stringValue(ScalarDataType scalarDataType);

    public abstract double doubleValue(ScalarDataType scalarDataType);

    public abstract short shortValue(ScalarDataType scalarDataType);

    public abstract long longValue(ScalarDataType scalarDataType);

//    public abstract BigDecimal bigDecimalValue(ScalarDataType scalarDataType);

    public abstract byte byteValue(ScalarDataType scalarDataType);

    public abstract char charValue(ScalarDataType scalarDataType);

    public abstract boolean booleanValue(ScalarDataType scalarDataType);

    public abstract BigDecimal decimalValue(ScalarDataType scalarDataType);

    public abstract LocalDate dateValue(ScalarDataType scalarDataType);

    public abstract LocalDateTime datetimeValue(ScalarDataType scalarDataType);

    public abstract ZonedDateTime datetimetzValue(ScalarDataType scalarDataType);

    public abstract LocalTime timeValue(ScalarDataType scalarDataType);

    public abstract float floatValue(ScalarDataType scalarDataType);

    public abstract byte[] binaryValue(ScalarDataType scalarDataType);

    public abstract java.time.Duration durationValue(ScalarDataType scalarDataType);

    public abstract NormalizedPeriod periodValue(ScalarDataType scalarDataType);

    public abstract UUID uuidValue(ScalarDataType scalarDataType);

    public abstract URI uriValue(ScalarDataType scalarDataType);

    public abstract String patternValue(ScalarDataType scalarDataType);

    public abstract UnionUntypedCase voidValue(ScalarDataType scalarDataType);

    public abstract <T extends Record> T tupleValue(TupleDataType t);

    public abstract <T extends Enum> T enumValue(EnumDataType t);

    public abstract <T extends Union> T unionValue(UnionDataType t);

    public abstract <T> T metaValue(MetaDataType t);

    public abstract <T extends AlfaObject> T objectValue(UdtDataType t);

    public <T extends AlfaObject> T objectValue(String fieldName, UdtDataType t) {
        currFieldNameStack.push(fieldName);
        AlfaObject result = objectValue(t);
        currFieldNameStack.pop();
        return (T) result;
    }

    public <T extends AlfaObject> T objectValue(Optional<Class> clz) {
        Optional<ClassUtils.ClassMeta> cm = Optional.empty();

        if (clz.isPresent()) {
            ClassUtils.ClassMeta m = ClassUtils.getMeta(clz.get().getName());
//            if ( ! m.getModel().hasAbstractTypeFieldsInClosure() ) {
            cm = Optional.of(m);
//            }
        }

        try {
            return objectValue(cm, Collections.emptyMap());
        } catch (AlfaRuntimeException are) {
            if (cm.isPresent())
                are.setValidationErrorTypeName(cm.get().getModel().getUdtDataType().getFullyQualifiedName());
            throw are;
        }
    }

    public abstract <T extends AlfaObject> T objectValue(UdtDataType t, Map<String, Function> templateFieldSuppliers);

    public abstract <T extends AlfaObject> T objectValue(Optional<ClassUtils.ClassMeta> cm, Map<String, Function> templateFieldSuppliers);

    public abstract <K, V> java.util.Map<K, V> mapValue(MapDataType t, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc);

    public <K, V> Map<K, V> mapValue(Field f, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {
        currFieldNameStack.push(f.getName());
        Map<K, V> r = mapValue((MapDataType) f.getDataType(), kc, vc);
        currFieldNameStack.pop();
        return r;
    }

    public abstract <T> java.util.Set<T> setValue(SetDataType f, Function<DataSupplier, T> consumer);

    public <T> Set<T> setValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        Set<T> result = setValue((SetDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return result;
    }

    public abstract <T> java.util.List<T> listValue(ListDataType f, Function<DataSupplier, T> consumer);

    public <T> List<T> listValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        List<T> result = listValue((ListDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return result;
    }

    public abstract <T> Optional<T> optionalValue(OptionalDataType f, Function<DataSupplier, T> c);

    public <T> Optional<T> optionalValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        Optional<T> v = optionalValue((OptionalDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

    public <T> Try<T> tryValue(TryDataType dt, Function<DataSupplier, T> c) {
        Try<T> res = null;

        Map<String, Function> m = new HashMap<>();
        m.put("Result", c);

        ClassUtils.ClassMeta cm = ClassUtils.getMeta(Try.class.getName());

        AlfaObject v = objectValue(Optional.of(cm), m);

        return (Try<T>) v;
    }

    public void fieldValue(Field field, Builder builder, BiConsumer supplier) {
        currFieldNameStack.push(field.getName());
        try {
            _fieldValue(field, builder, supplier);
        } catch (AlfaRuntimeException are) {
            are.setValidationErrorField(field.getName());
            throw are;
        } finally {
            currFieldNameStack.pop();
        }
    }


    private void _fieldValue(Field field, Builder builder, BiConsumer supplier) {
        supplier.accept(builder, this);
    }

    public <T> schemarise.alfa.runtime.model.Try<T> tryValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        schemarise.alfa.runtime.model.Try<T> v = tryValue((TryDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

    public abstract <T> Compressed compressedValue(Function<DataSupplier, T> compressedConsumer);

    public <T> Compressed compressedValue(CompressedDataType f, Function<DataSupplier, T> compressedConsumer) {
        return compressedValue(compressedConsumer);
    }

    public <T> Compressed compressedValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        Compressed v = compressedValue((CompressedDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

    public abstract <T> Encrypted<T> encryptedValue(Function<DataSupplier, T> encryptedConsumer);

    public <T> Encrypted<T> encryptedValue(EncryptedDataType f, Function<DataSupplier, T> encryptedConsumer) {
//        Encrypted<T> t = ctx.createEncrypted(null, encryptedConsumer.apply(this) );
        return encryptedValue(encryptedConsumer);
    }

    public <T> Encrypted<T> encryptedValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        Encrypted<T> v = encryptedValue((EncryptedDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

//    public abstract<T extends  com.schemarise.alfa.runtime.Key> T keyValue(KeyDataType dt);
//
//    public <T extends Key> T keyValue(Field f) {
//        currFieldNameStack.push( f.getName() );
//        T v = keyValue((KeyDataType) f.getDataType());
//        currFieldNameStack.pop();
//        return v;
//    }

    public abstract <T> List<T> streamValue(StreamDataType std, Function<DataSupplier, T> c);

    public <T> List<T> streamValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        List<T> v = streamValue((StreamDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

    public abstract <T> Future<T> futureValue(FutureDataType fdt, Function<DataSupplier, T> c);

    public <T> Future<T> futureValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        Future<T> v = futureValue((FutureDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }

    public abstract <T> ITable tableValue(TabularDataType tdt, Function<DataSupplier, T> c);

    protected String currentFieldName() {
        return currFieldNameStack.peek();
    }

    protected boolean hasCurrentFieldName() {
        return !currFieldNameStack.isEmpty();
    }

    public <T> ITable tableValue(Field f, Function<DataSupplier, T> c) {
        currFieldNameStack.push(f.getName());
        ITable v = tableValue((TabularDataType) f.getDataType(), c);
        currFieldNameStack.pop();
        return v;
    }


    public abstract <L, R> Pair<L, R> pairValue(PairDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc);

    public <L, R> Pair<L, R> pairValue(Field f, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        currFieldNameStack.push(f.getName());
        Pair<L, R> v = pairValue((PairDataType) f.getDataType(), lc, rc);
        currFieldNameStack.pop();
        return v;
    }

    public abstract <L, R> Either<L, R> eitherValue(EitherDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc);

    public <L, R> Either<L, R> eitherValue(Field f, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        currFieldNameStack.push(f.getName());
        Either<L, R> v = eitherValue((EitherDataType) f.getDataType(), lc, rc);
        currFieldNameStack.pop();
        return v;
    }

    public <T> T paramTypeValue() {
        throw new UnsupportedOperationException();
    }

//    public abstract <L,R> Either<L, R> eitherValue(EitherDataType f, Function<DataSupplier, LocalDate> c);
//
//    public <L,R> Either<L, R> eitherValue(Field f, Function<DataSupplier, LocalDate> c) {
//        currFieldNameStack.push( f );
//        Either<L, R> v = eitherValue((EitherDataType) f.getDataType(), c);
//        currFieldNameStack.pop();
//        return v;
//    }
}
