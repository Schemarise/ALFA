package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.AlfaRuntimeException;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.math.BigDecimal;

final class JsonGeneratorWrapper {
    private final JsonGenerator jGenerator;

    public JsonGeneratorWrapper(JsonGenerator jGenerator) {
        this.jGenerator = jGenerator;
    }

    public void writeStartObject() {
        try {
            jGenerator.writeStartObject();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void close() {
        try {
            jGenerator.close();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeStringField(String s, String fullyQualifiedName) {
        try {
            jGenerator.writeStringField(s, fullyQualifiedName);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeFieldName(String name) {
        try {
            jGenerator.writeFieldName(name);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeEndObject() {
        try {
            jGenerator.writeEndObject();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeNumber(int v) {
        try {
            jGenerator.writeNumber(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeString(String v) {
        try {
            jGenerator.writeString(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeBinary(byte[] bytes) {
        try {
            jGenerator.writeBinary(bytes);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeNumber(BigDecimal v) {
        try {
            jGenerator.writeNumber(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeBoolean(boolean v) {
        try {
            jGenerator.writeBoolean(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeNumber(long v) {
        try {
            jGenerator.writeNumber(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeNumber(double v) {
        try {
            jGenerator.writeNumber(v);
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeNull() {
        try {
            jGenerator.writeNull();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeStartArray() {
        try {
            jGenerator.writeStartArray();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public void writeEndArray() {
        try {
            jGenerator.writeEndArray();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }
}
