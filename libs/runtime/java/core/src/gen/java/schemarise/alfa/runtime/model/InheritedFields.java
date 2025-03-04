// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface InheritedFields extends com.schemarise.alfa.runtime.Record {

    schemarise.alfa.runtime.model.UdtVersionedName getName();

    java.util.List<java.lang.String> getFields();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static InheritedFieldsBuilder builder() {
        return new schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl();
    }

    public static InheritedFieldsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                                    .INSTANCE);
        else
            return new schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl(
                    bc);
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
    public interface InheritedFieldsBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        InheritedFieldsBuilder setName(schemarise.alfa.runtime.model.UdtVersionedName v);

        schemarise.alfa.runtime.model.UdtVersionedName getName();

        InheritedFieldsBuilder addFields(java.lang.String e);

        InheritedFieldsBuilder addAllFields(java.util.List<java.lang.String> all);

        java.util.List<java.lang.String> getFields();

        InheritedFields build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _InheritedFieldsBuilderImpl extends _InheritedFields__Base__
            implements InheritedFieldsBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _InheritedFieldsBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _InheritedFieldsBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor.INSTANCE;
        }

        public InheritedFieldsBuilder setName(schemarise.alfa.runtime.model.UdtVersionedName v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Name", v);
            this._name = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .FIELD_ID_NAME);
            return this;
        }

        private void createFields() {
            this._fields = new java.util.ArrayList<>();
        }

        public InheritedFieldsBuilder addFields(java.lang.String e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Fields", e);
            if (this._fields == null) createFields();
            this._fields.add(e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .FIELD_ID_FIELDS);
            return this;
        }

        public InheritedFieldsBuilder setFields(int index, java.lang.String e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Fields", e);
            if (this._fields == null) createFields();
            this._fields.set(index, e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .FIELD_ID_FIELDS);
            return this;
        }

        public InheritedFieldsBuilder addAllFields(java.util.List<java.lang.String> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Fields", all);
            if (this._fields == null) createFields();
            this._fields.addAll(all);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .FIELD_ID_FIELDS);
            return this;
        }

        private InheritedFieldsBuilder setFields(java.util.List<java.lang.String> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Fields", all);
            this._fields = all;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .FIELD_ID_FIELDS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "Name":
                    setName((schemarise.alfa.runtime.model.UdtVersionedName) val);
                    break;
                case "Fields":
                    setFields((java.util.List<java.lang.String>) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public InheritedFields build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    2,
                    schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                            .INSTANCE);

            InheritedFields obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (InheritedFields)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(
                                                builderConfig(),
                                                descriptor(),
                                                _name,
                                                com.schemarise.alfa.runtime.utils.VectorCloner
                                                        .immutableList(
                                                                builderConfig(),
                                                                schemarise.alfa.runtime.model
                                                                        .InheritedFields
                                                                        .InheritedFieldsDescriptor
                                                                        .INSTANCE
                                                                        ._fieldsSupplierInner1,
                                                                _fields));
            else
                obj =
                        new schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsConcrete(
                                _name,
                                com.schemarise.alfa.runtime.utils.VectorCloner.immutableList(
                                        builderConfig(),
                                        schemarise.alfa.runtime.model.InheritedFields
                                                .InheritedFieldsDescriptor.INSTANCE
                                                ._fieldsSupplierInner1,
                                        _fields));

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.InheritedFields",
                    new java.lang.String[] {"Name", "Fields"},
                    new java.lang.Object[] {_name, _fields});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _InheritedFieldsConcrete extends _InheritedFields__Base__
            implements InheritedFields {

        private _InheritedFieldsConcrete() {
            super();
        }

        private _InheritedFieldsConcrete(
                schemarise.alfa.runtime.model.UdtVersionedName _name,
                java.util.List<java.lang.String> _fields) {
            super(_name, _fields);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _InheritedFields__Base__ {
        public schemarise.alfa.runtime.model.UdtVersionedName _name;
        public java.util.List<java.lang.String> _fields;

        public _InheritedFields__Base__() {}

        public _InheritedFields__Base__(
                schemarise.alfa.runtime.model.UdtVersionedName _name,
                java.util.List<java.lang.String> _fields) {
            this._name = _name;
            this._fields = _fields;
        }

        public schemarise.alfa.runtime.model.UdtVersionedName getName() {
            return _name;
        }

        public java.util.List<java.lang.String> getFields() {
            return _fields;
        }

        public int hashCode() {
            return java.util.Objects.hash(_name, _fields);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.InheritedFields",
                    new java.lang.String[] {"Name", "Fields"},
                    new java.lang.Object[] {_name, _fields});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsConcrete))
                return false;
            schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsConcrete rhs =
                    (schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsConcrete) o;
            return java.util.Objects.equals(_name, rhs._name)
                    && java.util.Objects.equals(_fields, rhs._fields);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "Name":
                    return _name;
                case "Fields":
                    return _fields;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            getName().validate(__builderConfig);

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class InheritedFieldsDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.InheritedFields";
        public static schemarise.alfa.runtime.model.InheritedFields.InheritedFieldsDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.InheritedFields
                                .InheritedFieldsDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_NAME = 0;
        public static final String FIELD_NAME = "Name";

        public static final short FIELD_ID_FIELDS = 1;
        public static final String FIELD_FIELDS = "Fields";

        private schemarise.alfa.runtime.model.UdtDataType _nameType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.UdtVersionedName")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .build();

        private schemarise.alfa.runtime.model.ListDataType _fieldsType =
                schemarise.alfa.runtime.model.ListDataType.builder()
                        .setComponentType(
                                schemarise.alfa.runtime.model.ScalarDataType.builder()
                                        .setScalarType(
                                                schemarise.alfa.runtime.model.ScalarType.stringType)
                                        .build())
                        .build();

        /* -- Consumer schemarise.alfa.runtime.model.UdtVersionedName -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        schemarise.alfa.runtime.model.UdtVersionedName>
                _nameConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _nameType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _nameConsumer =
                        (builder, supplier) -> {
                            builder.setName(_nameConsumerInner1.apply(supplier));
                        };
        /* -- Consumer list< string > -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier, java.util.List<java.lang.String>>
                _fieldsConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                                    _fieldsConsumerInner2 =
                                            (supplierInner2) -> {
                                                return supplierInner2.stringValue(
                                                        ((schemarise.alfa.runtime.model.ScalarDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _fieldsType)
                                                                        .getComponentType()));
                                            };
                            return supplierInner1.listValue(
                                    ((schemarise.alfa.runtime.model.ListDataType) _fieldsType),
                                    _fieldsConsumerInner2);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _fieldsConsumer =
                        (builder, supplier) -> {
                            builder.addAllFields(_fieldsConsumerInner1.apply(supplier));
                        };

        /* -- Supplier schemarise.alfa.runtime.model.UdtVersionedName -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.UdtVersionedName,
                        com.schemarise.alfa.runtime.DataConsumer>
                _nameSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _nameType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.InheritedFields,
                        com.schemarise.alfa.runtime.DataConsumer>
                _nameSupplier =
                        (p, consumer) -> {
                            _nameSupplierInner1.accept(p.getName(), consumer);
                        };
        /* -- Supplier list< string > -- */
        java.util.function.BiConsumer<
                        java.util.List<java.lang.String>, com.schemarise.alfa.runtime.DataConsumer>
                _fieldsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            java.lang.String,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _fieldsSupplierInner2 =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.ScalarDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _fieldsType)
                                                                        .getComponentType()),
                                                        pInner2);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ListDataType) _fieldsType),
                                    pInner1,
                                    _fieldsSupplierInner2);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.InheritedFields,
                        com.schemarise.alfa.runtime.DataConsumer>
                _fieldsSupplier =
                        (p, consumer) -> {
                            _fieldsSupplierInner1.accept(p.getFields(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                InheritedFields, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta nameMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_nameSupplier),
                        java.util.Optional.of(_nameConsumer),
                        java.util.Optional.of(_nameSupplierInner1),
                        java.util.Optional.of(_nameConsumerInner1),
                        _nameType,
                        FIELD_NAME,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta fieldsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_fieldsSupplier),
                        java.util.Optional.of(_fieldsConsumer),
                        java.util.Optional.of(_fieldsSupplierInner1),
                        java.util.Optional.of(_fieldsConsumerInner1),
                        _fieldsType,
                        FIELD_FIELDS,
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
        public InheritedFieldsBuilder builder() {
            return new schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl();
        }

        @Override
        public InheritedFieldsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.InheritedFields._InheritedFieldsBuilderImpl(
                    cc);
        }

        public InheritedFieldsDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<InheritedFields>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_NAME, nameMeta);
                                        put(FIELD_FIELDS, fieldsMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<InheritedFields>>
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
                case FIELD_ID_NAME:
                    return FIELD_NAME;
                case FIELD_ID_FIELDS:
                    return FIELD_FIELDS;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException("Unknown field id " + id);
            }
        }

        public boolean hasAbstractTypeFieldsInClosure() {
            return true;
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
            schemarise.alfa.runtime.model.InheritedFields{Fields:list<string>;Name:schemarise.alfa.runtime.model.UdtVersionedName[dd07c725];}
            schemarise.alfa.runtime.model.InheritedFields{Fields:list<string>;Name:schemarise.alfa.runtime.model.UdtVersionedName[4c83e4bb];}
            */
            return "3f0b5a94:154a0cd5";
        }
    }
    // </editor-fold>

}
