package com.schemarise.alfa.runtime.codec.json;

import schemarise.alfa.runtime.model.Pair;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonToken;

import java.math.BigDecimal;

public interface IJsonParserWrapper {
    int getIntValue();

    String getText();

    double getDoubleValue();

    short getShortValue();

    long getLongValue();

    byte getByteValue();

    byte[] getBinaryValue();

    char[] getTextCharacters();

    boolean getBooleanValue();

    void pushBackLastToken();

    Pair<Integer, Integer> getOffset();

    JsonToken nextToken();

    JsonToken currentToken();

    void skipChildren();

    String getCurrentName();

    JsonLocation getCurrentLocation();

    BigDecimal getBigDecimalValue();

    String getCurrentLocationStr();
}
