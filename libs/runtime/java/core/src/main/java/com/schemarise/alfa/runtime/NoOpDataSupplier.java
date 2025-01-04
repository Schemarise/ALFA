package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.codec.CodecConfig;
import schemarise.alfa.runtime.model.*;
import com.schemarise.alfa.runtime.utils.ClassUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

public class NoOpDataSupplier extends DataSupplier {
    protected NoOpDataSupplier(CodecConfig jwc) {
        super(jwc);
    }

    @Override
    public int intValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public String stringValue(ScalarDataType scalarDataType) {
        return null;
    }

    @Override
    public double doubleValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public short shortValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public long longValue(ScalarDataType sdt) {
        return 0;
    }

//    @Override
//    public BigDecimal bigDecimalValue(ScalarDataType sdt) {
//        return null;
//    }

    @Override
    public byte byteValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public char charValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public boolean booleanValue(ScalarDataType sdt) {
        return false;
    }

    @Override
    public BigDecimal decimalValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public LocalDate dateValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public LocalDateTime datetimeValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public ZonedDateTime datetimetzValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public LocalTime timeValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public float floatValue(ScalarDataType sdt) {
        return 0;
    }

    @Override
    public byte[] binaryValue(ScalarDataType sdt) {
        return new byte[0];
    }

    @Override
    public Duration durationValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public NormalizedPeriod periodValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public UUID uuidValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public URI uriValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public String patternValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public UnionUntypedCase voidValue(ScalarDataType sdt) {
        return null;
    }

    @Override
    public <T extends Record> T tupleValue(TupleDataType t) {
        return null;
    }

    @Override
    public <T extends Enum> T enumValue(EnumDataType t) {
        return null;
    }

    @Override
    public <T extends Union> T unionValue(UnionDataType t) {
        return null;
    }

    @Override
    public <T> T metaValue(MetaDataType t) {
        return null;
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t) {
        return null;
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<Class> clz) {
        return null;
    }

    @Override
    public <T extends AlfaObject> T objectValue(UdtDataType t, Map<String, Function> templateFieldSuppliers) {
        return null;
    }

    @Override
    public <T extends AlfaObject> T objectValue(Optional<ClassUtils.ClassMeta> cm, Map<String, Function> templateFieldSuppliers) {
        return null;
    }

    @Override
    public <K, V> Map<K, V> mapValue(MapDataType t, Function<DataSupplier, K> kc, Function<DataSupplier, V> vc) {
        return null;
    }

    @Override
    public <T> Set<T> setValue(SetDataType f, Function<DataSupplier, T> consumer) {
        return null;
    }

    @Override
    public <T> List<T> listValue(ListDataType f, Function<DataSupplier, T> consumer) {
        return null;
    }

    @Override
    public <T> Optional<T> optionalValue(OptionalDataType f, Function<DataSupplier, T> c) {
        return Optional.empty();
    }

    @Override
    public <T> Compressed compressedValue(Function<DataSupplier, T> compressedConsumer) {
        return null;
    }

    @Override
    public <T> Encrypted<T> encryptedValue(Function<DataSupplier, T> encryptedConsumer) {
        return null;
    }

//    @Override
//    public <T extends Key> T keyValue(KeyDataType dt) {
//        return null;
//    }

    @Override
    public <T> List<T> streamValue(StreamDataType std, Function<DataSupplier, T> c) {
        return null;
    }

    @Override
    public <T> Future<T> futureValue(FutureDataType fdt, Function<DataSupplier, T> c) {
        return null;
    }

    @Override
    public <T> ITable tableValue(TabularDataType tdt, Function<DataSupplier, T> c) {
        return null;
    }

    @Override
    public <L, R> Pair<L, R> pairValue(PairDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        return null;
    }

    @Override
    public <L, R> Either<L, R> eitherValue(EitherDataType edt, Function<DataSupplier, L> lc, Function<DataSupplier, R> rc) {
        return null;
    }
}
