package com.schemarise.alfa.runtime.codec;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Converters {
    public static <T> SupplierConsumer<T> createSupplierConsumer(IDataType dt) {
        if (dt instanceof ScalarDataType) {
            ScalarDataType t = (ScalarDataType) dt;
            switch (t.getScalarType()) {
                case stringType:
                    return (SupplierConsumer<T>) StringProcessor;
                case doubleType:
                    return (SupplierConsumer<T>) DoubleProcessor;
                case intType:
                    return (SupplierConsumer<T>) IntProcessor;
            }
        } else if (dt instanceof ListDataType) {
            ListDataType t = (ListDataType) dt;
            IDataType ct = t.getComponentType();
            SupplierConsumer<Object> csc = createSupplierConsumer(ct);
            return new ListSupplierConsumer(t, csc);
        }

        throw new AlfaRuntimeException("Unhandled creator for type " + dt);
    }

//    public static void main( String []s ) {
//        for (int i = 0; i < ScalarType.values().length; i++) {
//            ScalarType v = ScalarType.values()[i];
//            String tn = v.name().replaceAll("Type", "");
//            String cap = tn.substring(0, 1).toUpperCase() + tn.substring(1);
//
//            print("    public static ScalarDataType DataType" + cap + " = ScalarDataType.builder().setScalarType(ScalarType." + tn + "Type).build();");
//            print("    public static SupplierConsumer<" + cap + "> " + cap + "Processor = new SupplierConsumer<" + cap + ">(");
//            print("            DataType" + cap + ",");
//            print("            (supplier) -> supplier." + tn + "Value( DataType" + cap + " ),");
//            print("            (value, consumer) -> consumer.consume( DataType" + cap + ", value) );\n");
//        }
//    }
//
//    private static void print( String s ) {
//        System.out.println(s);
//    }

    public static class UdtSupplierConsumer<T extends AlfaObject> extends SupplierConsumer<T> {
        public UdtSupplierConsumer() {
            super(
                    (supplier) -> supplier.objectValue(Optional.empty()),
                    (value, consumer) -> consumer.consume(value)
            );
        }
    }

    public static class ListSupplierConsumer<T> extends SupplierConsumer<List<T>> {
        public ListSupplierConsumer(ListDataType ldt, SupplierConsumer<T> elementSupplierConsumer) {
            super(
                    (supplier) -> supplier.listValue(ldt, elementSupplierConsumer.getSupplier()),
                    (value, consumer) -> consumer.consume(ldt, value, elementSupplierConsumer.getConsumer())
            );
        }
    }

    public static class SetSupplierConsumer<T> extends SupplierConsumer<Set<T>> {
        public SetSupplierConsumer(SetDataType ldt, SupplierConsumer<T> elementSupplierConsumer) {
            super(
                    (supplier) -> supplier.setValue(ldt, elementSupplierConsumer.getSupplier()),
                    (value, consumer) -> consumer.consume(ldt, value, elementSupplierConsumer.getConsumer())
            );
        }
    }

    public static class MapSupplierConsumer<K, V> extends SupplierConsumer<Map<K, V>> {
        public MapSupplierConsumer(MapDataType ldt, SupplierConsumer<K> kSupplierConsumer, SupplierConsumer<V> vSupplierConsumer) {
            super(
                    (supplier) -> supplier.mapValue(ldt, kSupplierConsumer.getSupplier(), vSupplierConsumer.getSupplier()),
                    (value, consumer) -> consumer.consume(ldt, value, kSupplierConsumer.getConsumer(), vSupplierConsumer.getConsumer())
            );
        }
    }


    public static class SupplierConsumer<T> {
        private final IDataType dataType;
        private final Function<DataSupplier, T> supplier;
        private final BiConsumer<T, DataConsumer> consumer;

        SupplierConsumer(IDataType dt, Function<DataSupplier, T> supplier, BiConsumer<T, DataConsumer> consumer) {
            this.dataType = dt;
            this.supplier = supplier;
            this.consumer = consumer;
        }

        SupplierConsumer(Function<DataSupplier, T> supplier, BiConsumer<T, DataConsumer> consumer) {
            this.dataType = null;
            this.supplier = supplier;
            this.consumer = consumer;
        }

        SupplierConsumer(BiConsumer<T, DataConsumer> consumer) {
            this.dataType = null;
            this.supplier = null;
            this.consumer = consumer;
        }

        public SupplierConsumer(Function<DataSupplier, T> supplier) {
            this.dataType = null;
            this.supplier = supplier;
            this.consumer = null;
        }

        public BiConsumer<T, DataConsumer> getConsumer() {
            return this.consumer;
        }

        public Function<DataSupplier, T> getSupplier() {
            return this.supplier;
        }
    }

    public static ScalarDataType DataTypeString = ScalarDataType.builder().setScalarType(ScalarType.stringType).build();
    public static SupplierConsumer<String> StringProcessor = new SupplierConsumer<String>(
            DataTypeString,
            (supplier) -> supplier.stringValue(DataTypeString),
            (value, consumer) -> consumer.consume(DataTypeString, value));

    public static ScalarDataType DataTypeShort = ScalarDataType.builder().setScalarType(ScalarType.shortType).build();
    public static SupplierConsumer<Short> ShortProcessor = new SupplierConsumer<Short>(
            DataTypeShort,
            (supplier) -> supplier.shortValue(DataTypeShort),
            (value, consumer) -> consumer.consume(DataTypeShort, value));

    public static ScalarDataType DataTypeInt = ScalarDataType.builder().setScalarType(ScalarType.intType).build();
    public static SupplierConsumer<Integer> IntProcessor = new SupplierConsumer<Integer>(
            DataTypeInt,
            (supplier) -> supplier.intValue(DataTypeInt),
            (value, consumer) -> consumer.consume(DataTypeInt, value));

    public static ScalarDataType DataTypeLong = ScalarDataType.builder().setScalarType(ScalarType.longType).build();
    public static SupplierConsumer<Long> LongProcessor = new SupplierConsumer<Long>(
            DataTypeLong,
            (supplier) -> supplier.longValue(DataTypeLong),
            (value, consumer) -> consumer.consume(DataTypeLong, value));

    public static ScalarDataType DataTypeBoolean = ScalarDataType.builder().setScalarType(ScalarType.booleanType).build();
    public static SupplierConsumer<Boolean> BooleanProcessor = new SupplierConsumer<Boolean>(
            DataTypeBoolean,
            (supplier) -> supplier.booleanValue(DataTypeBoolean),
            (value, consumer) -> consumer.consume(DataTypeBoolean, value));

    public static ScalarDataType DataTypeDate = ScalarDataType.builder().setScalarType(ScalarType.dateType).build();
    public static SupplierConsumer<LocalDate> DateProcessor = new SupplierConsumer<LocalDate>(
            DataTypeDate,
            (supplier) -> supplier.dateValue(DataTypeDate),
            (value, consumer) -> consumer.consume(DataTypeDate, value));

    public static ScalarDataType DataTypeDatetime = ScalarDataType.builder().setScalarType(ScalarType.datetimeType).build();
    public static SupplierConsumer<LocalDateTime> DatetimeProcessor = new SupplierConsumer<LocalDateTime>(
            DataTypeDatetime,
            (supplier) -> supplier.datetimeValue(DataTypeDatetime),
            (value, consumer) -> consumer.consume(DataTypeDatetime, value));

    public static ScalarDataType DataTypeTime = ScalarDataType.builder().setScalarType(ScalarType.timeType).build();
    public static SupplierConsumer<LocalTime> TimeProcessor = new SupplierConsumer<LocalTime>(
            DataTypeTime,
            (supplier) -> supplier.timeValue(DataTypeTime),
            (value, consumer) -> consumer.consume(DataTypeTime, value));

    public static ScalarDataType DataTypeDuration = ScalarDataType.builder().setScalarType(ScalarType.durationType).build();
    public static SupplierConsumer<Duration> DurationProcessor = new SupplierConsumer<Duration>(
            DataTypeDuration,
            (supplier) -> supplier.durationValue(DataTypeDuration),
            (value, consumer) -> consumer.consume(DataTypeDuration, value));

    public static ScalarDataType DataTypeDouble = ScalarDataType.builder().setScalarType(ScalarType.doubleType).build();
    public static SupplierConsumer<Double> DoubleProcessor = new SupplierConsumer<Double>(
            DataTypeDouble,
            (supplier) -> supplier.doubleValue(DataTypeDouble),
            (value, consumer) -> consumer.consume(DataTypeDouble, value));

//    public static ScalarDataType DataTypeFloat = ScalarDataType.builder().setScalarType(ScalarType.floatType).build();
//    public static SupplierConsumer<Float> FloatProcessor = new SupplierConsumer<Float>(
//            DataTypeFloat,
//            (supplier) -> supplier.floatValue( DataTypeFloat ),
//            (value, consumer) -> consumer.consume( DataTypeFloat, value) );

    public static ScalarDataType DataTypeBinary = ScalarDataType.builder().setScalarType(ScalarType.binaryType).build();
    public static SupplierConsumer<byte[]> BinaryProcessor = new SupplierConsumer<byte[]>(
            DataTypeBinary,
            (supplier) -> supplier.binaryValue(DataTypeBinary),
            (value, consumer) -> consumer.consume(DataTypeBinary, value));

//    public static ScalarDataType DataTypeByte = ScalarDataType.builder().setScalarType(ScalarType.byteType).build();
//    public static SupplierConsumer<Byte> ByteProcessor = new SupplierConsumer<Byte>(
//            DataTypeByte,
//            (supplier) -> supplier.byteValue( DataTypeByte ),
//            (value, consumer) -> consumer.consume( DataTypeByte, value) );

    public static ScalarDataType DataTypeDecimal = ScalarDataType.builder().setScalarType(ScalarType.decimalType).build();
    public static SupplierConsumer<BigDecimal> DecimalProcessor = new SupplierConsumer<BigDecimal>(
            DataTypeDecimal,
            (supplier) -> supplier.decimalValue(DataTypeDecimal),
            (value, consumer) -> consumer.consume(DataTypeDecimal, value));

    public static ScalarDataType DataTypeUuid = ScalarDataType.builder().setScalarType(ScalarType.uuidType).build();
    public static SupplierConsumer<UUID> UuidProcessor = new SupplierConsumer<UUID>(
            DataTypeUuid,
            (supplier) -> supplier.uuidValue(DataTypeUuid),
            (value, consumer) -> consumer.consume(DataTypeUuid, value));

//    public static ScalarDataType DataTypeChar = ScalarDataType.builder().setScalarType(ScalarType.charType).build();
//    public static SupplierConsumer<Character> CharProcessor = new SupplierConsumer<Character>(
//            DataTypeChar,
//            (supplier) -> supplier.charValue( DataTypeChar ),
//            (value, consumer) -> consumer.consume( DataTypeChar, value) );
//
//    public static ScalarDataType DataTypeUri = ScalarDataType.builder().setScalarType(ScalarType.uriType).build();
//    public static SupplierConsumer<URI> UriProcessor = new SupplierConsumer<URI>(
//            DataTypeUri,
//            (supplier) -> supplier.uriValue( DataTypeUri ),
//            (value, consumer) -> consumer.consume( DataTypeUri, value) );

}
