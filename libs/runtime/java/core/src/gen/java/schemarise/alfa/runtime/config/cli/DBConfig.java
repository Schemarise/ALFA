// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.config.cli;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface DBConfig extends com.schemarise.alfa.runtime.Record {

    java.lang.String getType();

    java.lang.String getUrl();

    java.util.Optional<java.lang.String> getName();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static DBConfigBuilder builder() {
        return new schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl();
    }

    public static DBConfigBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor
                                    .INSTANCE);
        else return new schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl(bc);
    }

    public default <B extends com.schemarise.alfa.runtime.Builder> B toBuilder(
            com.schemarise.alfa.runtime.IBuilderConfig bc) {
        return com.schemarise.alfa.runtime.utils.AlfaUtils.toBuilder(bc, this);
    }

    public default <B extends com.schemarise.alfa.runtime.Builder> B toBuilder() {
        return toBuilder(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Interface">
    public interface DBConfigBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        DBConfigBuilder setType(java.lang.String v);

        java.lang.String getType();

        DBConfigBuilder setUrl(java.lang.String v);

        java.lang.String getUrl();

        DBConfigBuilder setName(java.util.Optional<java.lang.String> v);

        DBConfigBuilder setName(java.lang.String v);

        java.util.Optional<java.lang.String> getName();

        DBConfig build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _DBConfigBuilderImpl extends _DBConfig__Base__ implements DBConfigBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _DBConfigBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _DBConfigBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(3);
            __missingFields.set(0, 3);
            setName(java.util.Optional.empty());
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.INSTANCE;
        }

        public DBConfigBuilder setType(java.lang.String v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Type", v);
            this._type = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.FIELD_ID_TYPE);
            return this;
        }

        public DBConfigBuilder setUrl(java.lang.String v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Url", v);
            this._url = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.FIELD_ID_URL);
            return this;
        }

        public DBConfigBuilder setName(java.util.Optional<java.lang.String> v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Name", v);
            this._name = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.FIELD_ID_NAME);
            return this;
        }

        public DBConfigBuilder setName(java.lang.String v) {
            this._name = java.util.Optional.ofNullable(v);
            clearMissingFlag(
                    schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.FIELD_ID_NAME);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "Type":
                    setType((java.lang.String) val);
                    break;
                case "Url":
                    setUrl((java.lang.String) val);
                    break;
                case "Name":
                    setName((java.util.Optional<java.lang.String>) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public DBConfig build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    3,
                    schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.INSTANCE);

            DBConfig obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (DBConfig)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(builderConfig(), descriptor(), _type, _url, _name);
            else
                obj =
                        new schemarise.alfa.runtime.config.cli.DBConfig._DBConfigConcrete(
                                _type, _url, _name);

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.config.cli.DBConfig",
                    new java.lang.String[] {"Type", "Url", "Name"},
                    new java.lang.Object[] {_type, _url, _name});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _DBConfigConcrete extends _DBConfig__Base__ implements DBConfig {

        private _DBConfigConcrete() {
            super();
        }

        private _DBConfigConcrete(
                java.lang.String _type,
                java.lang.String _url,
                java.util.Optional<java.lang.String> _name) {
            super(_type, _url, _name);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _DBConfig__Base__ {
        public java.lang.String _type;
        public java.lang.String _url;
        public java.util.Optional<java.lang.String> _name;

        public _DBConfig__Base__() {}

        public _DBConfig__Base__(
                java.lang.String _type,
                java.lang.String _url,
                java.util.Optional<java.lang.String> _name) {
            this._type = _type;
            this._url = _url;
            this._name = _name;
        }

        public java.lang.String getType() {
            return _type;
        }

        public java.lang.String getUrl() {
            return _url;
        }

        public java.util.Optional<java.lang.String> getName() {
            return _name;
        }

        public int hashCode() {
            return java.util.Objects.hash(_type, _url, _name);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.config.cli.DBConfig",
                    new java.lang.String[] {"Type", "Url", "Name"},
                    new java.lang.Object[] {_type, _url, _name});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof schemarise.alfa.runtime.config.cli.DBConfig._DBConfigConcrete))
                return false;
            schemarise.alfa.runtime.config.cli.DBConfig._DBConfigConcrete rhs =
                    (schemarise.alfa.runtime.config.cli.DBConfig._DBConfigConcrete) o;
            return java.util.Objects.equals(_type, rhs._type)
                    && java.util.Objects.equals(_url, rhs._url)
                    && java.util.Objects.equals(_name, rhs._name);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "Type":
                    return _type;
                case "Url":
                    return _url;
                case "Name":
                    return _name;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class DBConfigDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.config.cli.DBConfig";
        public static schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor INSTANCE =
                new schemarise.alfa.runtime.config.cli.DBConfig.DBConfigDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_TYPE = 0;
        public static final String FIELD_TYPE = "Type";

        public static final short FIELD_ID_URL = 1;
        public static final String FIELD_URL = "Url";

        public static final short FIELD_ID_NAME = 2;
        public static final String FIELD_NAME = "Name";

        private schemarise.alfa.runtime.model.ScalarDataType _typeType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.stringType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _urlType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.stringType)
                        .build();

        private schemarise.alfa.runtime.model.OptionalDataType _nameType =
                schemarise.alfa.runtime.model.OptionalDataType.builder()
                        .setComponentType(
                                schemarise.alfa.runtime.model.ScalarDataType.builder()
                                        .setScalarType(
                                                schemarise.alfa.runtime.model.ScalarType.stringType)
                                        .build())
                        .build();

        /* -- Consumer string -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                _typeConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.stringValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _typeType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _typeConsumer =
                        (builder, supplier) -> {
                            builder.setType(_typeConsumerInner1.apply(supplier));
                        };
        /* -- Consumer string -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                _urlConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.stringValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _urlType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _urlConsumer =
                        (builder, supplier) -> {
                            builder.setUrl(_urlConsumerInner1.apply(supplier));
                        };
        /* -- Consumer string ? -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier, java.util.Optional<java.lang.String>>
                _nameConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                                    _nameConsumerInner2 =
                                            (supplierInner2) -> {
                                                return supplierInner2.stringValue(
                                                        ((schemarise.alfa.runtime.model.ScalarDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .OptionalDataType)
                                                                                _nameType)
                                                                        .getComponentType()));
                                            };
                            return supplierInner1.optionalValue(
                                    ((schemarise.alfa.runtime.model.OptionalDataType) _nameType),
                                    _nameConsumerInner2);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _nameConsumer =
                        (builder, supplier) -> {
                            builder.setName(_nameConsumerInner1.apply(supplier));
                        };

        /* -- Supplier string -- */
        java.util.function.BiConsumer<java.lang.String, com.schemarise.alfa.runtime.DataConsumer>
                _typeSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _typeType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig,
                        com.schemarise.alfa.runtime.DataConsumer>
                _typeSupplier =
                        (p, consumer) -> {
                            _typeSupplierInner1.accept(p.getType(), consumer);
                        };
        /* -- Supplier string -- */
        java.util.function.BiConsumer<java.lang.String, com.schemarise.alfa.runtime.DataConsumer>
                _urlSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _urlType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig,
                        com.schemarise.alfa.runtime.DataConsumer>
                _urlSupplier =
                        (p, consumer) -> {
                            _urlSupplierInner1.accept(p.getUrl(), consumer);
                        };
        /* -- Supplier string ? -- */
        java.util.function.BiConsumer<
                        java.util.Optional<java.lang.String>,
                        com.schemarise.alfa.runtime.DataConsumer>
                _nameSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            java.lang.String,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _nameSupplierInner2 =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.ScalarDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .OptionalDataType)
                                                                                _nameType)
                                                                        .getComponentType()),
                                                        pInner2);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.OptionalDataType) _nameType),
                                    pInner1,
                                    _nameSupplierInner2);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.config.cli.DBConfig,
                        com.schemarise.alfa.runtime.DataConsumer>
                _nameSupplier =
                        (p, consumer) -> {
                            _nameSupplierInner1.accept(p.getName(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                DBConfig, com.schemarise.alfa.runtime.DataConsumer>>
                getFieldSupplier(java.lang.String fieldName) {
            return _fieldsMeta_.get(fieldName).getSupplier();
        }

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                com.schemarise.alfa.runtime.Builder,
                                com.schemarise.alfa.runtime.DataSupplier>>
                getFieldConsumer(java.lang.String fieldName) {
            return _fieldsMeta_.get(fieldName).getConsumer();
        }

        public com.schemarise.alfa.runtime.FieldMeta typeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_typeSupplier),
                        java.util.Optional.of(_typeConsumer),
                        java.util.Optional.of(_typeSupplierInner1),
                        java.util.Optional.of(_typeConsumerInner1),
                        _typeType,
                        FIELD_TYPE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta urlMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_urlSupplier),
                        java.util.Optional.of(_urlConsumer),
                        java.util.Optional.of(_urlSupplierInner1),
                        java.util.Optional.of(_urlConsumerInner1),
                        _urlType,
                        FIELD_URL,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta nameMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_nameSupplier),
                        java.util.Optional.of(_nameConsumer),
                        java.util.Optional.of(_nameSupplierInner1),
                        java.util.Optional.of(_nameConsumerInner1),
                        _nameType,
                        FIELD_NAME,
                        java.util.Optional.empty());

        @Override
        public boolean hasBuilder() {
            return true;
        }

        @Override
        public boolean convertableToBuilder() {
            return true;
        }

        @Override
        public DBConfigBuilder builder() {
            return new schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl();
        }

        @Override
        public DBConfigBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.config.cli.DBConfig._DBConfigBuilderImpl(cc);
        }

        public DBConfigDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<DBConfig>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_TYPE, typeMeta);
                                        put(FIELD_URL, urlMeta);
                                        put(FIELD_NAME, nameMeta);
                                    }
                                });

        @Override
        public schemarise.alfa.runtime.model.UdtDataType getUdtDataType() {
            return _asUdtType_;
        }

        @Override
        public java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> getAsserts() {
            return _asserts_;
        }

        @Override
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<DBConfig>>
                getAllFieldsMeta() {
            return _fieldsMeta_;
        }

        @Override
        /** Not applicable - this is not an entity or a key directly linked to an entity */
        public java.util.Optional<com.schemarise.alfa.runtime.TypeDescriptor> getEntityKeyModel() {
            return java.util.Optional.empty();
        }

        @Override
        public java.lang.String fieldIdName(int id) {
            switch (id) {
                case FIELD_ID_TYPE:
                    return FIELD_TYPE;
                case FIELD_ID_URL:
                    return FIELD_URL;
                case FIELD_ID_NAME:
                    return FIELD_NAME;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Unknown field id " + id);
            }
        }

        public boolean hasAbstractTypeFieldsInClosure() {
            return false;
        }

        public java.util.Set<java.lang.String> getImmediateDescendants() {
            return java.util.Collections.emptySet();
        }

        public java.util.Optional<
                        java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Expression>>
                getAnnotations() {
            return java.util.Optional.empty();
        }

        public java.util.Set<schemarise.alfa.runtime.model.ModifierType> getModifiers() {
            return new java.util.HashSet<schemarise.alfa.runtime.model.ModifierType>() {
                {
                }
            };
        }

        public java.lang.String getChecksum() {
            /*
            schemarise.alfa.runtime.config.cli.DBConfig{Name:optional<string;>;Type:string;Url:string;}
            schemarise.alfa.runtime.config.cli.DBConfig{Type:string;Url:string;}
            */
            return "68f3392b:899b895e";
        }
    }
    // </editor-fold>

}