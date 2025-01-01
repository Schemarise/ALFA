package com.schemarise.alfa.runtime;

import java.util.Optional;
import java.util.Set;

public interface IBuilderConfig {
    RuntimeContext getRuntimeContext();

    default IBuiltinFunctions builtins() {
        return getRuntimeContext().getBuiltinFunctions();
    }

    /**
     * Should the build() method on the Builder class validate the objects, e.g. constraints,
     * before the object is built.
     *
     * @return True ( default ) to validate on build.
     */
    boolean shouldValidateOnBuild();

    boolean shouldSkipAssert(String n);

    /**
     * Should the build() method on the Builder class assert if mandatory fields are set
     *
     * @return True ( default ) to assert mandatory fields are set on build.
     */
    boolean assertMandatoryFieldsSet();

    /**
     * Should collections ( Map, Set, List ) be cloned to ensure values are completely immutable
     * and avoid modifications to collections once build() has been called on the Alfa object.
     * This can be disabled if the source of the collections can be trusted not to modify the
     * underlying collections.
     * <p>
     * Nested collections are cloned so, list< list< int > > will cloned the inner list< int > values too.
     *
     * @return True ( default ) to clone collections on build.
     */
    boolean shouldCloneCollectionsOnBuild();

    <T extends ServiceFactory> T getServiceFactory(Class<T> factoryClass);

    /**
     * Should unknown fields incoming fields be skipped when
     * building an object
     *
     * @return
     */
    boolean isSkipUnknownFields();

    /**
     * Character prefixing special field names
     *
     * @return
     */
    String getMetaFieldPrefix();

    Optional<IBuilderFactory> getCustomBuilderFactory();

    /**
     * Epsilon value to be used for double comparisons
     *
     * @return
     */
    default double epsilonValue() {
        return 0.000001d;
    }


    boolean isVerbose();

    IValidationListener getAssertListener();

    Set<String> getExcludeAsserts();
}

