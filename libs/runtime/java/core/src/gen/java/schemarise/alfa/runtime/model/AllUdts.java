// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface AllUdts extends com.schemarise.alfa.runtime.Record {

    java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> getUdts();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static AllUdtsBuilder builder() {
        return new schemarise.alfa.runtime.model.AllUdts._AllUdtsBuilderImpl();
    }

    public static AllUdtsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(bc, schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.INSTANCE);
        else return new schemarise.alfa.runtime.model.AllUdts._AllUdtsBuilderImpl(bc);
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
    public interface AllUdtsBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        AllUdtsBuilder addUdts(schemarise.alfa.runtime.model.UdtVersionedName e);

        AllUdtsBuilder addAllUdts(
                java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> all);

        java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> getUdts();

        AllUdts build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _AllUdtsBuilderImpl extends _AllUdts__Base__ implements AllUdtsBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _AllUdtsBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _AllUdtsBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(1);
            __missingFields.set(0, 1);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.INSTANCE;
        }

        private void createUdts() {
            this._udts = new java.util.ArrayList<>();
        }

        public AllUdtsBuilder addUdts(schemarise.alfa.runtime.model.UdtVersionedName e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Udts", e);
            if (this._udts == null) createUdts();
            this._udts.add(e);
            clearMissingFlag(schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.FIELD_ID_UDTS);
            return this;
        }

        public AllUdtsBuilder setUdts(int index, schemarise.alfa.runtime.model.UdtVersionedName e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Udts", e);
            if (this._udts == null) createUdts();
            this._udts.set(index, e);
            clearMissingFlag(schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.FIELD_ID_UDTS);
            return this;
        }

        public AllUdtsBuilder addAllUdts(
                java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Udts", all);
            if (this._udts == null) createUdts();
            this._udts.addAll(all);
            clearMissingFlag(schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.FIELD_ID_UDTS);
            return this;
        }

        private AllUdtsBuilder setUdts(
                java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Udts", all);
            this._udts = all;
            clearMissingFlag(schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.FIELD_ID_UDTS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "udts":
                    setUdts((java.util.List<schemarise.alfa.runtime.model.UdtVersionedName>) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public AllUdts build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    1,
                    schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.INSTANCE);

            AllUdts obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (AllUdts)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(
                                                builderConfig(),
                                                descriptor(),
                                                com.schemarise.alfa.runtime.utils.VectorCloner
                                                        .immutableList(
                                                                builderConfig(),
                                                                schemarise.alfa.runtime.model
                                                                        .AllUdts.AllUdtsDescriptor
                                                                        .INSTANCE
                                                                        ._udtsSupplierInner1,
                                                                _udts));
            else
                obj =
                        new schemarise.alfa.runtime.model.AllUdts._AllUdtsConcrete(
                                com.schemarise.alfa.runtime.utils.VectorCloner.immutableList(
                                        builderConfig(),
                                        schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor
                                                .INSTANCE
                                                ._udtsSupplierInner1,
                                        _udts));

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.AllUdts",
                    new java.lang.String[] {"udts"},
                    new java.lang.Object[] {_udts});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _AllUdtsConcrete extends _AllUdts__Base__ implements AllUdts {

        private _AllUdtsConcrete() {
            super();
        }

        private _AllUdtsConcrete(
                java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> _udts) {
            super(_udts);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _AllUdts__Base__ {
        public java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> _udts;

        public _AllUdts__Base__() {}

        public _AllUdts__Base__(
                java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> _udts) {
            this._udts = _udts;
        }

        public java.util.List<schemarise.alfa.runtime.model.UdtVersionedName> getUdts() {
            return _udts;
        }

        public int hashCode() {
            return java.util.Objects.hash(_udts);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.AllUdts",
                    new java.lang.String[] {"udts"},
                    new java.lang.Object[] {_udts});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof schemarise.alfa.runtime.model.AllUdts._AllUdtsConcrete))
                return false;
            schemarise.alfa.runtime.model.AllUdts._AllUdtsConcrete rhs =
                    (schemarise.alfa.runtime.model.AllUdts._AllUdtsConcrete) o;
            return java.util.Objects.equals(_udts, rhs._udts);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "udts":
                    return _udts;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            java.util.stream.IntStream.range(0, getUdts().size())
                    .forEach(
                            e0 -> {
                                getUdts().get(e0).validate(__builderConfig);
                            });

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class AllUdtsDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.AllUdts";
        public static schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor INSTANCE =
                new schemarise.alfa.runtime.model.AllUdts.AllUdtsDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_UDTS = 0;
        public static final String FIELD_UDTS = "udts";

        private schemarise.alfa.runtime.model.ListDataType _udtsType =
                schemarise.alfa.runtime.model.ListDataType.builder()
                        .setComponentType(
                                schemarise.alfa.runtime.model.UdtDataType.builder()
                                        .setFullyQualifiedName(
                                                "schemarise.alfa.runtime.model.UdtVersionedName")
                                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                                        .build())
                        .build();

        /* -- Consumer list< schemarise.alfa.runtime.model.UdtVersionedName > -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        java.util.List<schemarise.alfa.runtime.model.UdtVersionedName>>
                _udtsConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier,
                                            schemarise.alfa.runtime.model.UdtVersionedName>
                                    _udtsConsumerInner2 =
                                            (supplierInner2) -> {
                                                return supplierInner2.objectValue(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _udtsType)
                                                                        .getComponentType()));
                                            };
                            return supplierInner1.listValue(
                                    ((schemarise.alfa.runtime.model.ListDataType) _udtsType),
                                    _udtsConsumerInner2);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.AllUdts._AllUdtsBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _udtsConsumer =
                        (builder, supplier) -> {
                            builder.addAllUdts(_udtsConsumerInner1.apply(supplier));
                        };

        /* -- Supplier list< schemarise.alfa.runtime.model.UdtVersionedName > -- */
        java.util.function.BiConsumer<
                        java.util.List<schemarise.alfa.runtime.model.UdtVersionedName>,
                        com.schemarise.alfa.runtime.DataConsumer>
                _udtsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            schemarise.alfa.runtime.model.UdtVersionedName,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _udtsSupplierInner2 =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _udtsType)
                                                                        .getComponentType()),
                                                        pInner2);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ListDataType) _udtsType),
                                    pInner1,
                                    _udtsSupplierInner2);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.AllUdts,
                        com.schemarise.alfa.runtime.DataConsumer>
                _udtsSupplier =
                        (p, consumer) -> {
                            _udtsSupplierInner1.accept(p.getUdts(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<AllUdts, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta udtsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_udtsSupplier),
                        java.util.Optional.of(_udtsConsumer),
                        java.util.Optional.of(_udtsSupplierInner1),
                        java.util.Optional.of(_udtsConsumerInner1),
                        _udtsType,
                        FIELD_UDTS,
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
        public AllUdtsBuilder builder() {
            return new schemarise.alfa.runtime.model.AllUdts._AllUdtsBuilderImpl();
        }

        @Override
        public AllUdtsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.AllUdts._AllUdtsBuilderImpl(cc);
        }

        public AllUdtsDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<AllUdts>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_UDTS, udtsMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<AllUdts>>
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
                case FIELD_ID_UDTS:
                    return FIELD_UDTS;
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
            schemarise.alfa.runtime.model.AllUdts{udts:list<schemarise.alfa.runtime.model.UdtVersionedName[dd07c725]>;}
            schemarise.alfa.runtime.model.AllUdts{udts:list<schemarise.alfa.runtime.model.UdtVersionedName[4c83e4bb]>;}
            */
            return "60b20fc4:61433f94";
        }
    }
    // </editor-fold>

}
