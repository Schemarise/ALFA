package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.DataConsumer;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

final class JsonWriter implements IntImpl.JsonWriterIfc {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonWriter self = new JsonWriter();

    public static JsonWriter getInstance() {
        return self;
    }

    public OutputStream write(AlfaObject so) throws IOException {
        return write(JsonCodecConfig.builder().build(), so);
    }

    public DataConsumer writer(JsonCodecConfig wc) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JsonGenerator jGenerator = wc.getJsonFactory().createGenerator(stream, JsonEncoding.UTF8);
        DataConsumer writer = IntImpl.jsonDataConsumer(wc, jGenerator, stream);
        return writer;
    }

    public OutputStream write(JsonCodecConfig jwc, AlfaObject so) throws IOException {
        OutputStream stream = new ByteArrayOutputStream();
        write(jwc, so, stream);
        stream.close();
        return stream;
    }

    public void write(JsonCodecConfig jwc, AlfaObject so, OutputStream stream) throws IOException {
        JsonGenerator jGenerator = jwc.getJsonFactory().createGenerator(stream);
        DataConsumer writer = IntImpl.jsonDataConsumer(jwc, jGenerator, stream);
        writer.consume(so);
        jGenerator.close();
    }

    public String asFormattedJson(AlfaObject so) throws IOException {
        return asFormattedJson(JsonCodecConfig.builder().build(), so);
    }

    public String asFormattedJson(JsonCodecConfig ctx, AlfaObject so) throws IOException {
        String unformatted = write(ctx, so).toString();
        Object json = mapper.readValue(unformatted, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
