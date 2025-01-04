package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.table.TableCodec;
import com.schemarise.alfa.runtime.utils.AlfaUtils;
import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.Try;

import java.util.Collection;

public class Alfa {
    private static final JsonCodec JSON_CODEC = new JsonCodec();
    private static final Logger LOGGER = new Logger();

    public static JsonCodec jsonCodec() {
        return JSON_CODEC;
    }

    public static ILogger createLogger(boolean debug, boolean trace) {
        return new Logger(debug, trace);
    }

    public static ILogger defaultLogger() {
        return LOGGER;
    }

    public static JsonCodecConfig jsonCodecConfigDefault() {
        return JsonCodecConfig.getInstance();
    }

    public static JsonCodecConfig.Builder jsonCodecConfigBuilder() {
        return JsonCodecConfig.builder();
    }

    public static ITable toTable(Collection<? extends AlfaObject> alfaObjects) {
        return TableCodec.toTable(alfaObjects);
    }

    public static ITable toTable(AlfaObject alfaObject) {
        return TableCodec.toTable(alfaObject);
    }

    public static <T> Try<T> createTryValue(T value) {
        return AlfaUtils.createTryValue(value);
    }

    public static <T> Try<T> createTryFailure(String msg) {
        return AlfaUtils.createTryFailure(msg);
    }

    public static <L, R> Either<L, R> createEitherLeft(L value) {
        return AlfaUtils.createEitherLeft(value);
    }

    public static <L, R> Either<L, R> createEitherRight(R value) {
        return AlfaUtils.createEitherRight(value);
    }
}
