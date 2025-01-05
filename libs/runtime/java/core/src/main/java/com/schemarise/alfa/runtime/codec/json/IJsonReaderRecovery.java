package com.schemarise.alfa.runtime.codec.json;

import schemarise.alfa.runtime.model.ScalarDataType;

/**
 * Implementations can decide what to do to recover from a JSON Reader if the object expected is
 * not whats given. E.g. String expected, object given.
 */
public interface IJsonReaderRecovery {
    String stringValue(IJsonParserWrapper parser);
}
