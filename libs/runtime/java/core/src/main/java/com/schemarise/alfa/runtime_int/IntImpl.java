package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.Try;
import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.Converters;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import schemarise.alfa.runtime.model.DecisionTable__HitPolicy;
import schemarise.alfa.runtime.model.UdtDataType;
import com.schemarise.alfa.runtime.utils.AlfaUtils;
import com.schemarise.alfa.runtime.utils.BuiltinFunctionsImpl;
import com.schemarise.alfa.runtime_int.table.TableCodecImpl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

final public class IntImpl {

    public static IBuiltinFunctions createBuiltinFunctions(RuntimeContext rc) {
        return new BuiltinFunctionsImpl(rc);
    }

    public static RuntimeContext defaultRuntimeContext() {
        return DefaultRuntimeContext.getInstance();
    }

    public static RuntimeContext createRuntimeContext(PersistenceSupport ps, MessagingSupport ms) {
        return new DefaultRuntimeContext(ps, ms);
    }

    public static SizeEstimatorIfc sizeEstimateGenerator(Set<String> strings, AlfaObject so) {
        return new SizeEstimateGenerator(strings, so);
    }

    public static SizeEstimatorIfc sizeEstimateGenerator(AlfaObject so) {
        return new SizeEstimateGenerator(so);
    }

    public static <T> Encrypted<T> defaultEncryptedFromValue(Function<DataSupplier, T> encryptedConsumer, byte[] bin) {
        return DefaultEncrypted.fromValue(encryptedConsumer, bin);
    }

    public static <T> Encrypted<T> defaultEncryptedFromValue(Converters.SupplierConsumer<T> convertor, IBuilderConfig builderConfig, T unencodedObject) {
        return DefaultEncrypted.fromValue(convertor, builderConfig, unencodedObject);
    }

    public static <T> Compressed defaultCompressedFromValue(Function<DataSupplier, T> compressedConsumer, byte[] bin) {
        return DefaultCompressed.fromValue(compressedConsumer, bin);
    }

    public static <T> Compressed defaultCompressedFromValue(Converters.SupplierConsumer<T> converter, IBuilderConfig builderConfig, T unencodedObject) {
        return DefaultCompressed.fromValue(converter, builderConfig, unencodedObject);
    }

    public static AlfaRandomGeneratorIfc alfaRandomGenerator(IBuilderConfig c, List<String> allTypes) {
        return new AlfaRandomGenerator(c, allTypes);
    }

    public static DataSupplier jsonDataSupplier(JsonCodecConfig cc, JsonParser jParser) {
        return new JsonDataSupplier(cc, jParser);
    }

    public static DataConsumer jsonDataConsumer(JsonCodecConfig wc, JsonGenerator jGenerator, OutputStream stream) {
        return new JsonDataConsumer(wc, jGenerator, stream);
    }

    public static JsonWriterIfc getJsonWriterInstance() {
        return JsonWriter.getInstance();
    }

    public static TableCodecIfc getTableCodecInstance() {
        return TableCodecImpl.getInstance();
    }

    public static JsonReaderIfc getJsonReaderInstance() {
        return JsonReader.getInstance();
    }

    public static <T> Try<List<T>> getDecisionTableResults(List<Object> inputs, DecisionTable__HitPolicy hp, List<IDecisionExecTableRow> rules) {
        DecisionExecTable dt = new DecisionExecTable(inputs, hp, rules);
        List<T> output = dt.execute();

        if (output.size() > 0) {
            return AlfaUtils.createTryValue(output);
        } else {
            return AlfaUtils.createTryFailure("Decision table not satisfied");
        }
    }

    public static <T> Try<T> getDecisionTableResult(List<Object> inputs, DecisionTable__HitPolicy hp, List<IDecisionExecTableRow> rules) {
        DecisionExecTable dt = new DecisionExecTable(inputs, hp, rules);
        List<Object> output = dt.execute();

        if (output.size() == 1 && hp == DecisionTable__HitPolicy.unique)
            return AlfaUtils.createTryValue((T) output.get(0));

        else if (output.size() >= 1 && hp == DecisionTable__HitPolicy.first)
            return AlfaUtils.createTryValue((T) output.get(0));

        else if (output.size() >= 1 && hp == DecisionTable__HitPolicy.anyof)
            return AlfaUtils.createTryValue((T) output.get(0));

        else if (output.size() >= 1 && hp == DecisionTable__HitPolicy.all)
            return AlfaUtils.createTryValue((T) output);

        else
            return AlfaUtils.createTryFailure("Decision table not satisfied: " + hp + ". Result count: " + output.size());
    }

    public interface JsonWriterIfc {
        OutputStream write(JsonCodecConfig jwc, AlfaObject so) throws IOException;

        void write(JsonCodecConfig jwc, AlfaObject so, OutputStream stream) throws IOException;

        String asFormattedJson(AlfaObject so) throws IOException;

        String asFormattedJson(JsonCodecConfig ctx, AlfaObject so) throws IOException;
    }

    public interface JsonReaderIfc {
        <T extends AlfaObject> T read(String s) throws IOException;

        <T extends AlfaObject> T read(JsonCodecConfig jwc, String s) throws IOException;

        DataSupplier reader(JsonCodecConfig jwc, InputStream is) throws IOException;

        <T extends AlfaObject> T read(JsonCodecConfig cc, InputStream is) throws IOException;

        <T extends AlfaObject> T read(JsonCodecConfig cc, JsonParser is) throws IOException;

        <T extends AlfaObject> Stream<Either<T, ValidationAlert.ValidationAlertBuilder>> readToStream(JsonCodecConfig cc, InputStream is) throws IOException;
    }

    public interface AlfaRandomGeneratorIfc {
        boolean randomizable(String typeName);

        <T extends AlfaObject> T random(String typeName);

        <T extends AlfaObject> T randomWithValues(String typeName, Map<String, Object> values);

        IBuilderConfig codecConfig();
    }

    public interface SizeEstimatorIfc {
        void consume(UdtDataType dt, AlfaObject v);

        long getEstimatedSize();
    }

    public interface TableCodecIfc {
        ITable toTable(Collection<? extends AlfaObject> alfaObjects);

        ITable toTable(AlfaObject alfaObject);

        <T extends AlfaObject> Stream<T> importRowBasedObjects(CodecConfig bc,
                                                               String expectedType,
                                                               Optional<List<String>> optDataColumnNames,
                                                               Stream<List<Object>> rowBasedData,
                                                               Map<String, Function<Object, Object>> preProcessors);

        <T extends AlfaObject> T importRowBasedObject(CodecConfig bc,
                                                      String expectedType,
                                                      Optional<List<String>> optDataColumnNames,
                                                      List<Object> rowBasedData,
                                                      String sourceLineInfo,
                                                      Map<String, Function<Object, Object>> preProcessors);
    }


}
