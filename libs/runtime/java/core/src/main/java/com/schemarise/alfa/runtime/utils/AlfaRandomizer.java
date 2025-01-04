package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.IBuilderConfig;
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.*;

/**
 * Utility class that generates a random Alfa object give its type name
 */
public class AlfaRandomizer {

    final private IntImpl.AlfaRandomGeneratorIfc d;

    public AlfaRandomizer() {
        this(Collections.emptyList());
    }

    public AlfaRandomizer(IBuilderConfig c) {
        this(c, Collections.emptyList());
    }

    /**
     * Create a randomiser with list of permitted type to be used as trait implementations
     * to be used when creating a random trait.
     *
     * @param allTypes
     */
    public AlfaRandomizer(List<String> allTypes) {
        this(JsonCodecConfig.builder().build(), allTypes);
    }

    /**
     * Create a randomiser with list of permitted type to be used as trait implementations
     * to be used when creating a random trait.
     *
     * @param allTypes
     */
    public AlfaRandomizer(IBuilderConfig c, List<String> allTypes) {
        d = IntImpl.alfaRandomGenerator(c, allTypes);
    }

    /**
     * Is it possible to randomise the given type
     *
     * @param typeName Name of Alfa type
     * @return True if a random instance of the typename can be created
     */
    public boolean randomizable(java.lang.String typeName) {
        return d.randomizable(typeName);
    }

    /**
     * Create a random version of the object
     *
     * @param typeName Name of Alfa type
     * @param <T>      Type parameter for AlfaObject
     * @return Instance of generated object
     */
    public /*! @cond x */ <T extends AlfaObject> /*! @endcond */ T random(java.lang.String typeName) {
        return d.random(typeName);
    }

    public /*! @cond x */ <T extends AlfaObject> /*! @endcond */ T randomWithValues(java.lang.String typeName, Map<String, Object> values) {
        return d.randomWithValues(typeName, values);
    }

    public /*! @cond x */ <T extends AlfaObject> /*! @endcond */ T randomWithValues(Builder builder) {

        Map<String, Object> values = new HashMap<>();

        builder.descriptor().getAllFieldsMeta().keySet().stream().forEach(
                fn -> {
                    Object fieldVal = builder.get(fn);
                    if (fieldVal != null)
                        values.put(fn, fieldVal);
                });

        return randomWithValues(builder.descriptor().getUdtDataType().getFullyQualifiedName(), values);
    }

    public IBuilderConfig codecConfig() {
        return d.codecConfig();
    }
}
