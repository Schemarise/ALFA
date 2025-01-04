package com.schemarise.alfa.runtime.codec;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class CodecConfig implements IBuilderConfig {
    private static CodecConfig instance = new Builder().build();
    private final RuntimeContext runtimeContext;
    private final boolean shouldValidateOnBuild;
    private final boolean shouldCloneCollectionsOnBuild;
    private final Map<String, ServiceFactory> serviceFactories;
    private final boolean isSkipUnknownFields;
    private final Optional<Class> assignableToClass;
    private final boolean assertMandatoryFieldsSet;
    private final boolean writeDetectCycles;
    private final String metaFieldPrefix;
    private final boolean verbose;
    private final boolean writeMapAsObject;
    private final boolean writeEntityKeyAsObject;
    private final IValidationListener assertListener;
    private final Set<String> excludeAsserts;
    private final ExecutorService executorService;

    public static final CodecConfig defaultCodecConfig() {
        return defaultCodecConfig(true);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final CodecConfig defaultCodecConfig(boolean clearValidationAlerts) {
        if (clearValidationAlerts)
            instance.getAssertListener().clear();

        return instance;
    }


    public CodecConfig(Map<String, ServiceFactory> serviceFactories,
                       RuntimeContext runtimeContext,
                       Optional<Class> assignableToClass,
                       boolean shouldValidateOnBuild,
                       boolean assertMandatoryFieldsSet,
                       boolean shouldCloneCollectionsOnBuild,
                       boolean writeDetectCycles,
                       boolean isSkipUnknownFields,
                       String metaFieldPrefix,
                       boolean verbose,
                       boolean writeMapAsObject,
                       boolean writeEntityKeyAsObject,
                       IValidationListener assertListener,
                       java.util.Set<java.lang.String> excludeAsserts,
                       ExecutorService executorService) {
        this.runtimeContext = runtimeContext;
        this.assignableToClass = assignableToClass;
        this.shouldValidateOnBuild = shouldValidateOnBuild;
        this.assertMandatoryFieldsSet = assertMandatoryFieldsSet;
        this.shouldCloneCollectionsOnBuild = shouldCloneCollectionsOnBuild;
        this.serviceFactories = serviceFactories;
        this.writeDetectCycles = writeDetectCycles;
        this.isSkipUnknownFields = isSkipUnknownFields;
        this.metaFieldPrefix = metaFieldPrefix;
        this.verbose = verbose;
        this.writeMapAsObject = writeMapAsObject;
        this.writeEntityKeyAsObject = writeEntityKeyAsObject;
        this.assertListener = assertListener;
        this.excludeAsserts = excludeAsserts;
        this.executorService = executorService;
    }

    public boolean isWriteDetectCycles() {
        return writeDetectCycles;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public boolean isWriteEntityKeyAsObject() {
        return writeEntityKeyAsObject;
    }

    public Optional<Class> getAssignableToClass() {
        return assignableToClass;
    }

    public boolean isWriteMapAsObject() {
        return writeMapAsObject;
    }

    @Override
    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    @Override
    public boolean shouldValidateOnBuild() {
        return shouldValidateOnBuild;
    }

    @Override
    public boolean shouldSkipAssert(String n) {
        return excludeAsserts.contains(n);
    }

    @Override
    public boolean assertMandatoryFieldsSet() {
        return assertMandatoryFieldsSet;
    }

    @Override
    public boolean shouldCloneCollectionsOnBuild() {
        return shouldCloneCollectionsOnBuild;
    }

    public <T extends ServiceFactory> T getServiceFactory(Class<T> factoryClass) {
        ServiceFactory e = serviceFactories.get(factoryClass.getTypeName());
        if (e == null)
            throw new ServiceFactoryException("Unable to find implementation for " + factoryClass + ". Ensure it is registered in BuilderConfig.");
        else
            return (T) e;
    }

    @Override
    public boolean isSkipUnknownFields() {
        return isSkipUnknownFields;
    }

    @Override
    public String getMetaFieldPrefix() {
        return this.metaFieldPrefix;
    }

    @Override
    public Optional<IBuilderFactory> getCustomBuilderFactory() {
        return Optional.empty();
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public IValidationListener getAssertListener() {
        return assertListener;
    }

    @Override
    public Set<String> getExcludeAsserts() {
        return excludeAsserts;
    }

    public static class Builder {
        private RuntimeContext runtimeContext = IntImpl.defaultRuntimeContext();
        private boolean shouldValidateOnBuild = true;
        private boolean shouldCloneCollectionsOnBuild = true;
        private Map<String, ServiceFactory> serviceFactories = new HashMap<>();
        private Optional<Class> assignableToClass = Optional.empty();
        private boolean assertMandatoryFieldsSet = true;
        private boolean writeDetectCycles = false;
        private boolean skipUnknownFields = false;
        private String metaFieldPrefix = "$";
        private boolean verbose = false;
        private boolean writeMapAsObject;
        private boolean writeEntityKeyAsObject = false;
        private IValidationListener assertListener = new DefaultValidationListener();
        private Set<String> excludeAsserts = new HashSet<>();

        public static final ExecutorService defaultExecutorService = createExecutorService();
        private ExecutorService userExecutorService;

        private static ExecutorService createExecutorService() {
            return Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()), new ThreadFactory() {
                AtomicLong al = new AtomicLong(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("ALFA-Executor-Pool-" + al.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            });
        }

        public Builder setExcludeAsserts(Set<String> excludeAsserts) {
            this.excludeAsserts = excludeAsserts;
            return this;
        }

        public Builder setExecutorService(ExecutorService es) {
            userExecutorService = es;
            return this;
        }

        public Builder setAssertListener(IValidationListener assertListener) {
            this.assertListener = assertListener;
            return this;
        }

        public Builder setWriteMapAsObject(boolean writeMapAsObject) {
            this.writeMapAsObject = writeMapAsObject;
            return this;
        }

        public Builder setWriteEntityKeyAsObject(boolean writeEntityKeyAsObject) {
            this.writeEntityKeyAsObject = writeEntityKeyAsObject;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder setMetaFieldPrefix(String metaFieldPrefix) {
            this.metaFieldPrefix = metaFieldPrefix;
            return this;
        }

        public Builder setSkipUnknownFields(boolean skipUnknownFields) {
            this.skipUnknownFields = skipUnknownFields;
            return this;
        }

        public Builder setRuntimeContext(RuntimeContext runtimeContext) {
            this.runtimeContext = runtimeContext;
            return this;
        }

        public Builder setShouldValidateOnBuild(boolean shouldValidateOnBuild) {
            this.shouldValidateOnBuild = shouldValidateOnBuild;
            return this;
        }

        public Builder setShouldCloneCollectionsOnBuild(boolean shouldCloneCollectionsOnBuild) {
            this.shouldCloneCollectionsOnBuild = shouldCloneCollectionsOnBuild;
            return this;
        }

        public Builder setServiceFactories(Map<String, ServiceFactory> serviceFactories) {
            this.serviceFactories = serviceFactories;
            return this;
        }

        public Builder setAssignableToClass(Optional<Class> assignableToClass) {
            this.assignableToClass = assignableToClass;
            return this;
        }


        public Builder unsafeDisableMandatoryFieldCheck() {
            this.assertMandatoryFieldsSet = false;
            return this;
        }

//        public Builder setAssertMandatoryFieldsSet(boolean assertMandatoryFieldsSet) {
//            this.assertMandatoryFieldsSet = assertMandatoryFieldsSet;
//            return this;
//        }

        public Builder setWriteDetectCycles(boolean writeDetectCycles) {
            this.writeDetectCycles = writeDetectCycles;
            return this;
        }

        public CodecConfig build() {
            ExecutorService es = defaultExecutorService;
            if (userExecutorService != null)
                es = userExecutorService;

            return new CodecConfig(serviceFactories, runtimeContext, assignableToClass,
                    shouldValidateOnBuild, assertMandatoryFieldsSet,
                    shouldCloneCollectionsOnBuild, writeDetectCycles, skipUnknownFields,
                    metaFieldPrefix, verbose, writeMapAsObject, writeEntityKeyAsObject,
                    assertListener, excludeAsserts, es);
        }
    }
}
