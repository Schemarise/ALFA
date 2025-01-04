package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.json.JsonTypeWriteMode;
import com.schemarise.alfa.runtime_int.IntImpl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The JsonCodec class contains utility methods that encode and decode Alfa objects to and from JSON.
 */
public class JsonCodec {

    /**
     * Configuration without $type or $key metadata, also ALFA maps are written as array with key and val fields
     */
    public static JsonCodecConfig CfgMapAsArrayWithNoMeta = JsonCodecConfig.builder().
            setWriteTypeMode(JsonTypeWriteMode.NeverWriteType).
            setWriteMapAsJsonObject(false).
            build();

    public JsonCodec() {
    }

    /**
     * Constructs a JSON representation of an Alfa object
     *
     * @param alfaObject The Alfa object to convert
     * @return JSON string representing the object
     * @throws IOException
     */
    public String toJsonString(AlfaObject alfaObject) throws IOException {
        Objects.requireNonNull(alfaObject);
        if (alfaObject instanceof Builder) {
            throw new RuntimeException("Unexpected Builder instance. Pass result of build() method");
        }
        return exportObj(JsonCodecConfig.getInstance(), alfaObject).toString();
    }

    /**
     * Constructs a JSON representation of an Alfa object
     *
     * @param jsonCfg    Use this configuration for conversion
     * @param alfaObject The Alfa object to convert
     * @return JSON string representing the object
     * @throws IOException
     */
    public String toJsonString(JsonCodecConfig jsonCfg, AlfaObject alfaObject) throws IOException {
        Objects.requireNonNull(alfaObject);
        return exportObj(jsonCfg, alfaObject).toString();
    }

    /**
     * Write JSON representation of an Alfa object to an OutputStream
     *
     * @param jsonCodecConfig JSON conversion settings
     * @param alfaObject      The Alfa object to convert
     * @return OutputStream containing JSON representation of Alfa object
     * @throws IOException
     */
    public OutputStream exportObj(JsonCodecConfig jsonCodecConfig, AlfaObject alfaObject) throws IOException {
        Objects.requireNonNull(alfaObject);
        return IntImpl.getJsonWriterInstance().write(jsonCodecConfig, alfaObject);
    }

    public void exportObj(JsonCodecConfig jsonCodecConfig, AlfaObject alfaObject, OutputStream os) throws IOException {
        Objects.requireNonNull(alfaObject);
        Objects.requireNonNull(os);
        IntImpl.getJsonWriterInstance().write(jsonCodecConfig, alfaObject, os);
    }

    /**
     * Construct a formatted JSON string representing the Alfa object
     *
     * @param alfaObject The Alfa object to convert
     * @return JSON pretty-printed object
     * @throws IOException
     */
    public String toFormattedJson(AlfaObject alfaObject) throws IOException {
        Objects.requireNonNull(alfaObject);
        return IntImpl.getJsonWriterInstance().asFormattedJson(alfaObject);
    }

    public String toFormattedJson(JsonCodecConfig cc, AlfaObject alfaObject) throws IOException {
        Objects.requireNonNull(alfaObject);
        return IntImpl.getJsonWriterInstance().asFormattedJson(cc, alfaObject);
    }

    /**
     * Construct an Alfa object from a JSON string
     *
     * @param json JSON containing the string representation of the object
     * @param <T>  Class/type of Alfa object that will be decoded
     * @return An Alfa object
     * @throws IOException
     */
    public <T extends AlfaObject> T fromJsonString(String json) throws IOException {
        Objects.requireNonNull(json);
        return IntImpl.getJsonReaderInstance().read(json);
    }

    /**
     * Construct an Alfa object from a JSON string
     *
     * @param readerCfg JSON reader configuration
     * @param json      JSON containing the string representation of the object
     * @param <T>       Class/type of Alfa object that will be decoded
     * @return An Alfa object
     * @throws IOException
     */
    public <T extends AlfaObject> T fromJsonString(JsonCodecConfig readerCfg, String json) throws IOException {
        Objects.requireNonNull(json);
        return IntImpl.getJsonReaderInstance().read(readerCfg, json);
    }

    /**
     * Similar to fromJsonString method, but on any errors throws AlfaRuntimeException
     *
     * @param json JSON containing the string representation of the object
     * @param <T>  Class/type of Alfa object that will be decoded
     * @return An Alfa object
     */
    public <T extends AlfaObject> T uncheckedFromJson(String... json) {
        Objects.requireNonNull(json);

        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < json.length; i++) {
                sb.append(json[i]);
            }
            return IntImpl.getJsonReaderInstance().read(sb.toString());
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public <T extends AlfaObject> T uncheckedFromJson(InputStream is) {
        Objects.requireNonNull(is);
        try {
            return IntImpl.getJsonReaderInstance().read(JsonCodecConfig.getInstance(), is);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    /**
     * Construct an Alfa object by decoding JSON from an InputStream
     *
     * @param jsonCodecConfig JSON conversion settings
     * @param inputStream     Stream containing encoded JSON
     * @param <T>             Class/type of Alfa object that will be decoded
     * @return An Alfa object
     * @throws IOException
     */
    public <T extends AlfaObject> T importObj(JsonCodecConfig jsonCodecConfig, InputStream inputStream) throws IOException {
        Objects.requireNonNull(jsonCodecConfig);
        Objects.requireNonNull(inputStream);

        return IntImpl.getJsonReaderInstance().read(jsonCodecConfig, inputStream);
    }

    public <T extends AlfaObject> T importObj(InputStream inputStream) throws IOException {
        return importObj(Alfa.jsonCodecConfigDefault(), inputStream);
    }

    public <T extends AlfaObject> T importObj(JsonCodecConfig jsonCodecConfig, JsonParser jsonParser) throws IOException {
        Objects.requireNonNull(jsonCodecConfig);
        Objects.requireNonNull(jsonParser);

        return IntImpl.getJsonReaderInstance().read(jsonCodecConfig, jsonParser);
    }

    public <T extends AlfaObject> Stream<Either<AlfaObject, ValidationAlert.ValidationAlertBuilder>> importObjects(JsonCodecConfig jsonCodecConfig, InputStream is) throws IOException {
        return IntImpl.getJsonReaderInstance().readToStream(jsonCodecConfig, is);
    }

    public <T extends AlfaObject> Stream<Either<AlfaObject, ValidationAlert.ValidationAlertBuilder>> importObjects(InputStream is) throws IOException {
        return importObjects(Alfa.jsonCodecConfigDefault(), is);
    }

    public DataConsumer jsonDataConsumer(JsonCodecConfig wc, JsonGenerator jGenerator, OutputStream stream) {
        return IntImpl.jsonDataConsumer(wc, jGenerator, stream);
    }

    public DataSupplier jsonDataSupplier(JsonCodecConfig cc, JsonParser jParser) {
        return IntImpl.jsonDataSupplier(cc, jParser);
    }
}
