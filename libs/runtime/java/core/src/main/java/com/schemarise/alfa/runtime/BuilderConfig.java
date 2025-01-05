package com.schemarise.alfa.runtime;

import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.*;

/**
 * Alfa runtime Builder configuration settings that control behaviour of the build method
 */
public class BuilderConfig implements IBuilderConfig {
    private static IBuilderConfig singleton = new BuilderConfigBuilder().build();
    private final boolean shouldValidateOnBuild;
    private final boolean assertMandatoryFieldsSet;
    private final boolean shouldCloneCollectionsOnBuild;
    private final Map<String, ServiceFactory> serviceFactories;
    private final RuntimeContext runtimeContext;
    private final boolean skipUnknownFields;
    private final boolean verbose;
    private final Optional<IBuilderFactory> builderFactory;
    private final IValidationListener assertListener;
    private final Set<String> excludeAsserts;


    public BuilderConfig(RuntimeContext runtimeContext, Map<String, ServiceFactory> serviceFactories,
                         boolean shouldValidateOnBuild, boolean assertMandatoryFieldsSet,
                         boolean shouldCloneCollectionsOnBuild,
                         boolean skipUnknownFields,
                         Optional<IBuilderFactory> builderFactory,
                         boolean verbose, IValidationListener assertListener,
                         Set<String> excludeAsserts) {
        this.shouldValidateOnBuild = shouldValidateOnBuild;
        this.assertMandatoryFieldsSet = assertMandatoryFieldsSet;
        this.shouldCloneCollectionsOnBuild = shouldCloneCollectionsOnBuild;
        this.runtimeContext = runtimeContext;
        this.serviceFactories = serviceFactories;
        this.skipUnknownFields = skipUnknownFields;
        this.builderFactory = builderFactory;
        this.verbose = verbose;
        this.assertListener = assertListener;
        this.excludeAsserts = excludeAsserts;
    }

    public boolean shouldValidateOnBuild() {
        return shouldValidateOnBuild;
    }

    @Override
    public boolean shouldSkipAssert(String n) {
        return excludeAsserts.contains(n);
    }

    public boolean assertMandatoryFieldsSet() {
        return assertMandatoryFieldsSet;
    }

    public Set<String> getExcludeAsserts() {
        return excludeAsserts;
    }

    public boolean shouldCloneCollectionsOnBuild() {
        return shouldCloneCollectionsOnBuild;
    }

    @Override
    public <T extends ServiceFactory> T getServiceFactory(Class<T> factoryClass) {
        ServiceFactory e = serviceFactories.get(factoryClass.getTypeName());
        if (e == null)
            throw new ServiceFactoryException("Unable to find implementation for " + factoryClass + ". Ensure it is registered in BuilderConfig.");
        else
            return (T) e;
    }

    @Override
    public boolean isSkipUnknownFields() {
        return skipUnknownFields;
    }

    @Override
    public String getMetaFieldPrefix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<IBuilderFactory> getCustomBuilderFactory() {
        return this.builderFactory;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public IValidationListener getAssertListener() {
        return assertListener;
    }

    /**
     * Get the default configuration instance
     *
     * @return A Builder configuration
     */
    public static IBuilderConfig getInstance() {
        return singleton;
    }

    /**
     * Get the runtime context assigned for this configuration
     *
     * @return A runtime context object
     */
    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    /**
     * Get a builder to construct a BuilderConfig
     *
     * @return
     */
    public static BuilderConfigBuilder newBuilder() {
        return new BuilderConfigBuilder();
    }

    public static BuilderConfigBuilder builder() {
        return new BuilderConfigBuilder();
    }

    public static class BuilderConfigBuilder {
        private boolean shouldValidateOnBuild = true;
        private boolean assertMandatoryFieldsSet = true;
        private boolean shouldCloneCollectionsOnBuild = true;
        private Map<String, ServiceFactory> serviceFactories = new HashMap<String, ServiceFactory>();
        private RuntimeContext runtimeContext = IntImpl.defaultRuntimeContext();
        private boolean skipUnknownFields;
        private Optional<IBuilderFactory> builderFactory = Optional.empty();
        private boolean verbose;
        private IValidationListener assertListener = new DefaultValidationListener();
        private Set<String> excludeAsserts = new HashSet<>();

        public void addExcludeAsserts(String excludeAssert) {
            this.excludeAsserts.add(excludeAssert);
        }

        public void setExcludeAsserts(Set<String> excludeAsserts) {
            this.excludeAsserts.addAll(excludeAsserts);
        }

        public BuilderConfigBuilder setShouldValidateOnBuild(boolean shouldValidateOnBuild) {
            this.shouldValidateOnBuild = shouldValidateOnBuild;
            return this;
        }

        public BuilderConfigBuilder setAssertListener(IValidationListener assertListener) {
            this.assertListener = assertListener;
            return this;
        }

        public BuilderConfigBuilder setSkipUnknownFields(boolean skipUnknownFields) {
            this.skipUnknownFields = skipUnknownFields;
            return this;
        }

        public BuilderConfigBuilder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public BuilderConfigBuilder setAssertMandatoryFieldsSet(boolean assertMandatoryFieldsSet) {
            this.assertMandatoryFieldsSet = assertMandatoryFieldsSet;
            return this;
        }

        public BuilderConfigBuilder setBuilderFactory(IBuilderFactory builderFactory) {
            this.builderFactory = Optional.ofNullable(builderFactory);
            return this;
        }

        public BuilderConfigBuilder setShouldCloneCollectionsOnBuild(boolean shouldCloneCollectionsOnBuild) {
            this.shouldCloneCollectionsOnBuild = shouldCloneCollectionsOnBuild;
            return this;
        }

        /**
         * Add a ServiceFactory implementation to be used by this BuliderConfig
         * Given an Alfa definition:
         * {@code service DataManager {} }
         * <p>
         * Use:
         * {@code
         * IBuilderConfig bc = BuilderConfig.builder().addServiceFactory( DataManager.Factory.class, new DataManagerFactoryImpl() ).build();
         * }
         */
        public BuilderConfigBuilder addServiceFactory(Class<? extends ServiceFactory> ifc, ServiceFactory sfImpl) {
            serviceFactories.put(ifc.getTypeName(), sfImpl);
            return this;
        }

        public BuilderConfigBuilder setRuntimeContext(RuntimeContext rc) {
            this.runtimeContext = rc;
            return this;
        }

        public IBuilderConfig build() {
            return new BuilderConfig(runtimeContext, serviceFactories,
                    this.shouldValidateOnBuild, this.assertMandatoryFieldsSet,
                    this.shouldCloneCollectionsOnBuild, this.skipUnknownFields,
                    this.builderFactory, this.verbose, this.assertListener, excludeAsserts);
        }
    }
}
