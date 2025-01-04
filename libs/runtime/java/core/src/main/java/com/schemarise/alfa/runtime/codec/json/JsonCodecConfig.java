package com.schemarise.alfa.runtime.codec.json;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime_int.IntImpl;
import com.fasterxml.jackson.core.JsonFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Configuration class containing settings that can be used to control how Alfa convert to and fro between JSON
 */
public final class JsonCodecConfig extends CodecConfig {
    private final boolean skipUnknownFields;
    private final ClassLoader customClassLoader;
    private final JsonFactory jsonFactory;

    private final JsonTypeWriteMode writeTypeMode;
    private final boolean writeStringifiedNumbers;

    private static JsonCodecConfig singleton = new JsonCodecConfig.Builder().build();

    private final boolean writeEmptyOptionalAsNull;
    private final boolean ignoreDateFormat;
    private final boolean allowOutOfOrderTypeSpecifier;
    private final boolean writeCheckSum;

    private final boolean writeModelId;
    private final Optional<IJsonReaderRecovery> jsonReaderRecovery;
    private final Function<String, String> fieldNameMapping;

    private JsonCodecConfig(Optional<Class> assignableToClass,
                            boolean shouldValidateOnBuild,
                            boolean assertMandatoryFieldsSet,
                            boolean shouldCloneCollectionsOnBuild,
                            RuntimeContext runtimeContext,
                            boolean skipUnknownFields,
                            ClassLoader customClassLoader,
                            JsonFactory jsonFactory,
                            String metaFieldPrefix,
                            Map<String, ServiceFactory> serviceFactories,
                            boolean writeDetectCycles,
                            boolean writeStringifiedNumbers,
                            JsonTypeWriteMode writeTypeMode,
                            boolean writeMapAsObject,
                            boolean writeEntityKeyAsObject,
                            boolean writeEmptyOptionalAsNull,
                            boolean verbose,
                            IValidationListener assertListener,
                            Set<String> excludeAsserts,
                            boolean ignoreDateFormat,
                            boolean allowOutOfOrderTypeSpecifier,
                            ExecutorService executorService,
                            boolean writeCheckSum,
                            Optional<IJsonReaderRecovery> jsonReaderRecovery,
                            Function<String, String> fieldNameMapping,
                            boolean writeModelId
    ) {
        super(serviceFactories, runtimeContext, assignableToClass, shouldValidateOnBuild, assertMandatoryFieldsSet,
                shouldCloneCollectionsOnBuild, writeDetectCycles, skipUnknownFields, metaFieldPrefix, verbose, writeMapAsObject,
                writeEntityKeyAsObject, assertListener, excludeAsserts, executorService);

        this.writeTypeMode = writeTypeMode;
        this.writeStringifiedNumbers = writeStringifiedNumbers;

        this.skipUnknownFields = skipUnknownFields;
        this.customClassLoader = customClassLoader;
        this.jsonFactory = jsonFactory;
        this.writeEmptyOptionalAsNull = writeEmptyOptionalAsNull;
        this.ignoreDateFormat = ignoreDateFormat;
        this.allowOutOfOrderTypeSpecifier = allowOutOfOrderTypeSpecifier;
        this.writeCheckSum = writeCheckSum;
        this.jsonReaderRecovery = jsonReaderRecovery;
        this.fieldNameMapping = fieldNameMapping;
        this.writeModelId = writeModelId;
    }

    public Function<String, String> getFieldNameMapper() {
        return fieldNameMapping;
    }

    public boolean isWriteCheckSum() {
        return writeCheckSum;
    }

    public boolean isWriteModelId() {
        return writeModelId;
    }

    public boolean isOutOfOrderTypeSpecifierAllowed() {
        return allowOutOfOrderTypeSpecifier;
    }

    public boolean isWriteEmptyOptionalAsNull() {
        return writeEmptyOptionalAsNull;
    }


    public JsonTypeWriteMode getWriteTypeMode() {
        return writeTypeMode;
    }

    public boolean isWriteStringifiedNumbers() {
        return writeStringifiedNumbers;
    }

    public Optional<IJsonReaderRecovery> getJsonReaderRecovery() {
        return jsonReaderRecovery;
    }

    public boolean isIgnoreDateFormat() {
        return ignoreDateFormat;
    }

    /**
     * Get the default instance, with the following default settings.
     * <pre>
     *         Optional<Class> assignableToClass = Optional.empty();
     *         RuntimeContext runtimeContext = DefaultRuntimeContext.getInstance();
     *         boolean skipUnknownFields = false;
     *         ClassLoader customClassLoader = JsonCodecConfig.class.getClassLoader();
     * </pre>
     *
     * @return Default instance of deserialisation configuration
     */
    public static JsonCodecConfig getInstance() {
        return singleton;
    }

    /**
     * Use specified classloader to load classes
     *
     * @return Classloader used in configuration
     */
    public ClassLoader getCustomClassLoader() {
        return customClassLoader;
    }

    /**
     * FasterXML JsonFactory object
     *
     * @return JsonFactory used in configuration
     */
    public JsonFactory getJsonFactory() {
        return this.jsonFactory;
    }

    /**
     * Ignore unknown fields in the payload
     *
     * @return true if flag is set
     */
    public boolean isSkipUnknownFields() {
        return skipUnknownFields;
    }

    /**
     * Create JSON writer configuration newBuilder object
     *
     * @return Builder object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to aid building an instance of JsonCodecConfig
     */
    public static class Builder extends CodecConfig.Builder {
        private boolean shouldValidateOnBuild = true;
        private boolean assertMandatoryFieldsSet = true;
        private boolean shouldCloneCollectionsOnBuild = true;
        private Optional<Class> assignableToClass = Optional.empty();
        private RuntimeContext runtimeContext = IntImpl.defaultRuntimeContext();
        private boolean skipUnknownFields = false;
        private ClassLoader customClassLoader = JsonCodecConfig.class.getClassLoader();
        private JsonFactory jsonFactory = new JsonFactory();
        private String metaFieldPrefix = "$";

        private JsonTypeWriteMode writeTypeMode = JsonTypeWriteMode.AlwaysWriteType;
        private boolean writeDetectCycles = false;
        private boolean writeStringifiedNumbers = true;

        private Map<String, ServiceFactory> serviceFactories = new HashMap<String, ServiceFactory>();
        private boolean verbose = false;
        private boolean writeMapAsObject = true;
        private boolean writeEntityKeyAsObject = false;
        private boolean writeEmptyOptionalAsNull = false;
        private IValidationListener assertListener = new DefaultValidationListener();
        private Set<String> excludeAsserts = new HashSet<>();
        private boolean ignoreDateFormat;

        private static final ExecutorService defaultExecutorService = CodecConfig.Builder.defaultExecutorService;
        private ExecutorService userExecutorService = null;
        private boolean allowOutOfOrderTypeSpecifier;
        private boolean writeChecksum = false;
        private IJsonReaderRecovery jsonReaderRecovery;
        private Function<String, String> fieldNameMapping = (x) -> null;

        private boolean writeModelId = true;

        private Builder() {
        }

        public Builder setWriteModelId(boolean writeModelId) {
            this.writeModelId = writeModelId;
            return this;
        }

        public Builder setWriteChecksum(boolean writeChecksum) {
            this.writeChecksum = writeChecksum;
            return this;
        }

        public Builder setExecutorService(ExecutorService es) {
            userExecutorService = es;
            return this;
        }

        public Builder setExcludeAsserts(Set<String> excludeAsserts) {
            this.excludeAsserts = excludeAsserts;
            return this;
        }

        public Builder setAssertListener(IValidationListener assertListener) {
            this.assertListener = assertListener;
            return this;
        }

        public Builder setWriteMapAsJsonObject(boolean writeMapAsObject) {
            this.writeMapAsObject = writeMapAsObject;
            return this;
        }

        public Builder setIgnoreDateFormat(boolean ignoreDateFormat) {
            this.ignoreDateFormat = ignoreDateFormat;
            return this;
        }

        public Builder setWriteEntityKeyAsObject(boolean writeEntityKeyAsObject) {
            this.writeEntityKeyAsObject = writeEntityKeyAsObject;
            return this;
        }

        public Builder setWriteEmptyOptionalAsNull(boolean writeEmptyOptionalAsNull) {
            this.writeEmptyOptionalAsNull = writeEmptyOptionalAsNull;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder setAssignableToClass(Optional<Class> assignableToClass) {
            this.assignableToClass = assignableToClass;
            return this;
        }

        public Builder setWriteTypeMode(JsonTypeWriteMode writeTypeMode) {
            this.writeTypeMode = writeTypeMode;
            return this;
        }

        public Builder setWriteDetectCycles(boolean writeDetectCycles) {
            this.writeDetectCycles = writeDetectCycles;
            return this;
        }

        public Builder setWriteStringifiedNumbers(boolean writeStringifiedNumbers) {
            this.writeStringifiedNumbers = writeStringifiedNumbers;
            return this;
        }

        public Builder setServiceFactories(Map<String, ServiceFactory> serviceFactories) {
            this.serviceFactories = serviceFactories;
            return this;
        }

        public Builder setShouldValidateOnBuild(boolean shouldValidateOnBuild) {
            this.shouldValidateOnBuild = shouldValidateOnBuild;
            return this;
        }

        public Builder unsafeDisableMandatoryFieldCheck() {
            this.assertMandatoryFieldsSet = false;
            return this;
        }

        public Builder setShouldCloneCollectionsOnBuild(boolean shouldCloneCollectionsOnBuild) {
            this.shouldCloneCollectionsOnBuild = shouldCloneCollectionsOnBuild;
            return this;
        }

        public Builder setAssignableToClass(Class assignableToClass) {
            this.assignableToClass = Optional.of(assignableToClass);
            return this;
        }

        public Builder setJsonFactory(JsonFactory jf) {
            this.jsonFactory = jf;
            return this;
        }

        public Builder setCustomClassLoader(ClassLoader customClassLoader) {
            this.customClassLoader = customClassLoader;
            return this;
        }

        public Builder setMetaFieldPrefix(String m) {
            metaFieldPrefix = m;
            return this;
        }

        public Builder setRuntimeContext(RuntimeContext runtimeContext) {
            this.runtimeContext = runtimeContext;
            return this;
        }

        public Builder setSkipUnknownFields(boolean skipUnknownFields) {
            this.skipUnknownFields = skipUnknownFields;
            return this;
        }

        public Builder addServiceFactory(Class<? extends ServiceFactory> ifc, ServiceFactory sfImpl) {
            serviceFactories.put(ifc.getTypeName(), sfImpl);
            return this;
        }

        public Builder setAllowOutOfOrderTypeSpecifier(boolean b) {
            this.allowOutOfOrderTypeSpecifier = b;
            return this;
        }

        public Builder setReaderRecovery(IJsonReaderRecovery rr) {
            this.jsonReaderRecovery = rr;
            return this;
        }

        public JsonCodecConfig build() {
            ExecutorService es = defaultExecutorService;
            if (userExecutorService != null)
                es = userExecutorService;


            return new JsonCodecConfig(
                    assignableToClass,
                    shouldValidateOnBuild,
                    assertMandatoryFieldsSet,
                    shouldCloneCollectionsOnBuild,
                    runtimeContext,
                    skipUnknownFields,
                    customClassLoader,
                    jsonFactory,
                    metaFieldPrefix,
                    serviceFactories,
                    writeDetectCycles,
                    writeStringifiedNumbers,
                    writeTypeMode,
                    writeMapAsObject,
                    writeEntityKeyAsObject,
                    writeEmptyOptionalAsNull,
                    verbose,
                    assertListener,
                    excludeAsserts,
                    ignoreDateFormat,
                    allowOutOfOrderTypeSpecifier,
                    es,
                    writeChecksum,
                    Optional.ofNullable(jsonReaderRecovery),
                    fieldNameMapping,
                    writeModelId
            );
        }

        public Builder setFieldNameMapper(Function<String, String> m) {
            this.fieldNameMapping = m;
            return this;
        }
    }
}
