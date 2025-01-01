package com.schemarise.alfa.runtime.codec.json;

/***
 * Mode of writing $type in the JSON
 */
public enum JsonTypeWriteMode {
    /**
     * Write the type for all UDTs
     */
    AlwaysWriteType,

    /**
     * Never write type info - just plain json
     */
    NeverWriteType,

    /**
     * Do not write root type, and only write for traits - this assumes reader knows what
     * type is being read, and traits having type is mandatory
     */
    NoRootAndMinimal,

    /**
     * Reader doesnt know root type, so root is required. Otherwise only needed for traits.
     */
    RootAndMinimal
}
