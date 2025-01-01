package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.AlfaRuntimeException;
import com.schemarise.alfa.runtime.DataSupplier;
import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class JsonReader implements IntImpl.JsonReaderIfc {
    private static JsonReader self = new JsonReader();
    private JsonCodecConfig defaultRc = JsonCodecConfig.builder().build();

    public static JsonReader getInstance() {
        return self;
    }

    public <T extends AlfaObject> T read(String s) throws IOException {
        return read(defaultRc, s);
    }

    public <T extends AlfaObject> T read(JsonCodecConfig jwc, String s) throws IOException {
        return read(jwc, new ByteArrayInputStream(s.getBytes()));
    }

    public DataSupplier reader(JsonCodecConfig jwc, InputStream is) throws IOException {
        JsonParser jParser = jwc.getJsonFactory().createParser(is);
        DataSupplier p = IntImpl.jsonDataSupplier(jwc, jParser);
        jParser.nextToken();
        return p;
    }

    public <T extends AlfaObject> T read(JsonCodecConfig cc, InputStream is) throws IOException {
        JsonParser jParser = cc.getJsonFactory().createParser(is);

        DataSupplier p = IntImpl.jsonDataSupplier(cc, jParser);
        Optional<Class> v = cc.getAssignableToClass();
        T o = p.objectValue(v);
        return o;
    }

    public <T extends AlfaObject> T read(JsonCodecConfig cc, JsonParser jParser) throws IOException {
        DataSupplier p = IntImpl.jsonDataSupplier(cc, jParser);
        Optional<Class> v = cc.getAssignableToClass();
        T o = p.objectValue(v);
        return o;
    }


    public <T extends AlfaObject> Stream<Either<T, ValidationAlert.ValidationAlertBuilder>> readToStream(JsonCodecConfig cc, InputStream is) throws IOException {
        JsonParser jParser = cc.getJsonFactory().createParser(is);

        DataSupplier p = IntImpl.jsonDataSupplier(cc, jParser);
        Optional<Class> v = cc.getAssignableToClass();

        AtomicInteger ai = new AtomicInteger(0);
        Iterator<Either<T, ValidationAlert.ValidationAlertBuilder>> it = new Iterator<Either<T, ValidationAlert.ValidationAlertBuilder>>() {
            @Override
            public boolean hasNext() {
                int counter = ai.getAndIncrement();

                if (counter == 1)
                    return true;
                else {
                    try {
                        JsonToken nt = jParser.nextToken();
                        return nt == JsonToken.START_OBJECT;
                    } catch (IOException e) {
                        throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
                    }
                }
            }

            @Override
            public Either<T, ValidationAlert.ValidationAlertBuilder> next() {
                try {
                    AlfaObject o = p.objectValue(v);
                    return Either.builder().setLeft(o).build();
                } catch (AlfaRuntimeException re) {
                    JsonLocation loc = jParser.getCurrentLocation();
                    return Either.builder().setRight(
                            re.toValidationAlert("Failed parsing JSON. Approx location " + loc)
                    ).build();

                } catch (Throwable t) {
                    JsonLocation loc = jParser.getCurrentLocation();
                    return Either.builder().setRight(
                            ValidationAlert.builder().
                                    setMessage("Failed parsing JSON. Approx location " + loc + ".\n" + t.getMessage()).
                                    setViolatedConstraint(Optional.of(ConstraintType.DataFormatError))
                    ).build();
                }
            }
        };

        Spliterator<Either<T, ValidationAlert.ValidationAlertBuilder>> si = Spliterators.spliteratorUnknownSize(it, Spliterator.CONCURRENT);
        Stream<Either<T, ValidationAlert.ValidationAlertBuilder>> s = StreamSupport.stream(si, true);
        return s;
    }
}
