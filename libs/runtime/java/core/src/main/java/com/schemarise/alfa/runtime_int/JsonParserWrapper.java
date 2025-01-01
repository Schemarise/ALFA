package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.AlfaRuntimeException;
import schemarise.alfa.runtime.model.Pair;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.math.BigDecimal;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

final class JsonParserWrapper implements com.schemarise.alfa.runtime.codec.json.IJsonParserWrapper {
    private final JsonParser parser;
    private boolean tokenPushedBack;


    public JsonParserWrapper(JsonParser parser) {
        this.parser = parser;
    }

    @Override
    public int getIntValue() {
        try {
            return parser.getIntValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public String getText() {
        try {
            return parser.getText();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public double getDoubleValue() {
        try {
            return parser.getDoubleValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public short getShortValue() {
        try {
            return parser.getShortValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public long getLongValue() {
        try {
            return parser.getLongValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }


    @Override
    public byte getByteValue() {
        try {
            return parser.getBinaryValue()[0];
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public byte[] getBinaryValue() {
        try {
            return parser.getBinaryValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public char[] getTextCharacters() {
        try {
            return parser.getTextCharacters();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public boolean getBooleanValue() {
        try {
            return parser.getBooleanValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public void pushBackLastToken() {
        tokenPushedBack = true;
    }

    @Override
    public Pair<Integer, Integer> getOffset() {
        JsonLocation loc = parser.getCurrentLocation();
        return Pair.builder().setLeft(loc.getLineNr()).setRight(loc.getColumnNr()).build();
    }

    @Override
    public JsonToken nextToken() {
        if (tokenPushedBack) {
            tokenPushedBack = false;
            return parser.currentToken();
        }

        try {
            JsonToken t = parser.nextToken();
            return t;
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public JsonToken currentToken() {
        return parser.currentToken();
    }

    @Override
    public void skipChildren() {
        try {
            parser.skipChildren();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public String getCurrentName() {
        try {
            return parser.getCurrentName();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return parser.getCurrentLocation();
    }

    @Override
    public BigDecimal getBigDecimalValue() {
        try {
            if (parser.currentToken() == VALUE_STRING) {
                return new BigDecimal(parser.getText());
            }
            return parser.getDecimalValue();
        } catch (IOException e) {
            throw new AlfaRuntimeException(ConstraintType.DataFormatError, "Failed to read " + getCurrentName(), e);
        }
    }

    @Override
    public String getCurrentLocationStr() {
        JsonLocation l = parser.getCurrentLocation();

        return
                "line:" + l.getLineNr() + " " +
                        "column:" + l.getColumnNr() + " " +
                        "offset:" + l.getByteOffset()
                ;
    }

//    public boolean isClosed() {
//        return parser.isClosed();
//    }
}
