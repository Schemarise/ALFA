// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model.diff;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
public interface Modifications extends com.schemarise.alfa.runtime.Record {

    java.util.Map<
                    schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                    java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
            getResults();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static ModificationsBuilder builder() {
        return new schemarise.alfa.runtime.model.diff.Modifications._ModificationsBuilderImpl();
    }

    public static ModificationsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                                    .INSTANCE);
        else
            return new schemarise.alfa.runtime.model.diff.Modifications._ModificationsBuilderImpl(
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
    public interface ModificationsBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        ModificationsBuilder putResults(
                schemarise.alfa.runtime.model.diff.ChangeCategoryType k,
                java.util.List<schemarise.alfa.runtime.model.diff.IModification> v);

        ModificationsBuilder putAllResults(
                java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                        all);

        java.util.Map<
                        schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                        java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                getResults();

        Modifications build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _ModificationsBuilderImpl extends _Modifications__Base__
            implements ModificationsBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _ModificationsBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _ModificationsBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(1);
            __missingFields.set(0, 1);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                    .INSTANCE;
        }

        private void createResults() {
            this._results = new java.util.LinkedHashMap<>();
        }

        public ModificationsBuilder putResults(
                schemarise.alfa.runtime.model.diff.ChangeCategoryType k,
                java.util.List<schemarise.alfa.runtime.model.diff.IModification> v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Key Results", k);
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Value Results", v);
            if (this._results == null) createResults();
            this._results.put(k, v);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                            .FIELD_ID_RESULTS);
            return this;
        }

        public ModificationsBuilder putAllResults(
                java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                        all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Results", all);
            if (this._results == null) createResults();
            this._results.putAll(all);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                            .FIELD_ID_RESULTS);
            return this;
        }

        private ModificationsBuilder setResults(
                java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                        all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Results", all);
            _results = all;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                            .FIELD_ID_RESULTS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "Results":
                    setResults(
                            (java.util.Map<
                                            schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                            java.util.List<
                                                    schemarise.alfa.runtime.model.diff
                                                            .IModification>>)
                                    val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public Modifications build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    1,
                    schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                            .INSTANCE);

            Modifications obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (Modifications)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(
                                                builderConfig(),
                                                descriptor(),
                                                com.schemarise.alfa.runtime.utils.VectorCloner
                                                        .immutableMap(
                                                                builderConfig(),
                                                                schemarise.alfa.runtime.model.diff
                                                                        .Modifications
                                                                        .ModificationsDescriptor
                                                                        .INSTANCE
                                                                        ._resultsSupplierInner1,
                                                                _results));
            else
                obj =
                        new schemarise.alfa.runtime.model.diff.Modifications._ModificationsConcrete(
                                com.schemarise.alfa.runtime.utils.VectorCloner.immutableMap(
                                        builderConfig(),
                                        schemarise.alfa.runtime.model.diff.Modifications
                                                .ModificationsDescriptor.INSTANCE
                                                ._resultsSupplierInner1,
                                        _results));

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.diff.Modifications",
                    new java.lang.String[] {"Results"},
                    new java.lang.Object[] {_results});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _ModificationsConcrete extends _Modifications__Base__ implements Modifications {

        private _ModificationsConcrete() {
            super();
        }

        private _ModificationsConcrete(
                java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                        _results) {
            super(_results);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _Modifications__Base__ {
        public java.util.Map<
                        schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                        java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                _results;

        public _Modifications__Base__() {}

        public _Modifications__Base__(
                java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                        _results) {
            this._results = _results;
        }

        public java.util.Map<
                        schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                        java.util.List<schemarise.alfa.runtime.model.diff.IModification>>
                getResults() {
            return _results;
        }

        public int hashCode() {
            return java.util.Objects.hash(_results);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.diff.Modifications",
                    new java.lang.String[] {"Results"},
                    new java.lang.Object[] {_results});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.diff.Modifications._ModificationsConcrete))
                return false;
            schemarise.alfa.runtime.model.diff.Modifications._ModificationsConcrete rhs =
                    (schemarise.alfa.runtime.model.diff.Modifications._ModificationsConcrete) o;
            return java.util.Objects.equals(_results, rhs._results);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                    .INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "Results":
                    return _results;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            getResults()
                    .keySet()
                    .forEach(
                            e0 -> {
                                e0.validate(__builderConfig);
                            });
            getResults()
                    .entrySet()
                    .stream()
                    .filter(e0 -> e0.getValue() != null)
                    .forEach(
                            e0 -> {
                                java.util.stream.IntStream.range(0, e0.getValue().size())
                                        .forEach(
                                                e1 -> {
                                                    e0.getValue().get(e1).validate(__builderConfig);
                                                });
                            });

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class ModificationsDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.diff.Modifications";
        public static schemarise.alfa.runtime.model.diff.Modifications.ModificationsDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.diff.Modifications
                                .ModificationsDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_RESULTS = 0;
        public static final String FIELD_RESULTS = "Results";

        private schemarise.alfa.runtime.model.MapDataType _resultsType =
                schemarise.alfa.runtime.model.MapDataType.builder()
                        .setKeyType(
                                schemarise.alfa.runtime.model.UdtDataType.builder()
                                        .setFullyQualifiedName(
                                                "schemarise.alfa.runtime.model.diff.ChangeCategoryType")
                                        .setUdtType(
                                                schemarise.alfa.runtime.model.UdtMetaType.enumType)
                                        .build())
                        .setValueType(
                                schemarise.alfa.runtime.model.ListDataType.builder()
                                        .setComponentType(
                                                schemarise.alfa.runtime.model.UdtDataType.builder()
                                                        .setFullyQualifiedName(
                                                                "schemarise.alfa.runtime.model.diff.IModification")
                                                        .setUdtType(
                                                                schemarise.alfa.runtime.model
                                                                        .UdtMetaType.traitType)
                                                        .build())
                                        .build())
                        .build();

        /* -- Consumer map< schemarise.alfa.runtime.model.diff.ChangeCategoryType , list< schemarise.alfa.runtime.model.diff.IModification > > -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>>
                _resultsConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier,
                                            schemarise.alfa.runtime.model.diff.ChangeCategoryType>
                                    _resultsConsumerInner2Key =
                                            (supplierInner2) -> {
                                                return supplierInner2.objectValue(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .MapDataType)
                                                                                _resultsType)
                                                                        .getKeyType()));
                                            };
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier,
                                            java.util.List<
                                                    schemarise.alfa.runtime.model.diff
                                                            .IModification>>
                                    _resultsConsumerInner2Val =
                                            (supplierInner2) -> {
                                                java.util.function.Function<
                                                                com.schemarise.alfa.runtime
                                                                        .DataSupplier,
                                                                schemarise.alfa.runtime.model.diff
                                                                        .IModification>
                                                        _resultsConsumerInner3 =
                                                                (supplierInner3) -> {
                                                                    return supplierInner3
                                                                            .objectValue(
                                                                                    ((schemarise
                                                                                                    .alfa
                                                                                                    .runtime
                                                                                                    .model
                                                                                                    .UdtDataType)
                                                                                            ((schemarise
                                                                                                                    .alfa
                                                                                                                    .runtime
                                                                                                                    .model
                                                                                                                    .ListDataType)
                                                                                                            ((schemarise
                                                                                                                                    .alfa
                                                                                                                                    .runtime
                                                                                                                                    .model
                                                                                                                                    .MapDataType)
                                                                                                                            _resultsType)
                                                                                                                    .getValueType())
                                                                                                    .getComponentType()));
                                                                };
                                                return supplierInner2.listValue(
                                                        ((schemarise.alfa.runtime.model
                                                                        .ListDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .MapDataType)
                                                                                _resultsType)
                                                                        .getValueType()),
                                                        _resultsConsumerInner3);
                                            };
                            return supplierInner1.mapValue(
                                    ((schemarise.alfa.runtime.model.MapDataType) _resultsType),
                                    _resultsConsumerInner2Key,
                                    _resultsConsumerInner2Val);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.diff.Modifications._ModificationsBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _resultsConsumer =
                        (builder, supplier) -> {
                            builder.putAllResults(_resultsConsumerInner1.apply(supplier));
                        };

        /* -- Supplier map< schemarise.alfa.runtime.model.diff.ChangeCategoryType , list< schemarise.alfa.runtime.model.diff.IModification > > -- */
        java.util.function.BiConsumer<
                        java.util.Map<
                                schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                java.util.List<schemarise.alfa.runtime.model.diff.IModification>>,
                        com.schemarise.alfa.runtime.DataConsumer>
                _resultsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            schemarise.alfa.runtime.model.diff.ChangeCategoryType,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _resultsSupplierInner2Key =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .MapDataType)
                                                                                _resultsType)
                                                                        .getKeyType()),
                                                        pInner2);
                                            };
                            java.util.function.BiConsumer<
                                            java.util.List<
                                                    schemarise.alfa.runtime.model.diff
                                                            .IModification>,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _resultsSupplierInner2Val =
                                            (pInner2, consumerInner2) -> {
                                                java.util.function.BiConsumer<
                                                                schemarise.alfa.runtime.model.diff
                                                                        .IModification,
                                                                com.schemarise.alfa.runtime
                                                                        .DataConsumer>
                                                        _resultsSupplierInner3 =
                                                                (pInner3, consumerInner3) -> {
                                                                    consumerInner3.consume(
                                                                            ((schemarise.alfa
                                                                                            .runtime
                                                                                            .model
                                                                                            .UdtDataType)
                                                                                    ((schemarise
                                                                                                            .alfa
                                                                                                            .runtime
                                                                                                            .model
                                                                                                            .ListDataType)
                                                                                                    ((schemarise
                                                                                                                            .alfa
                                                                                                                            .runtime
                                                                                                                            .model
                                                                                                                            .MapDataType)
                                                                                                                    _resultsType)
                                                                                                            .getValueType())
                                                                                            .getComponentType()),
                                                                            pInner3);
                                                                };
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model
                                                                        .ListDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .MapDataType)
                                                                                _resultsType)
                                                                        .getValueType()),
                                                        pInner2,
                                                        _resultsSupplierInner3);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.MapDataType) _resultsType),
                                    pInner1,
                                    _resultsSupplierInner2Key,
                                    _resultsSupplierInner2Val);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.diff.Modifications,
                        com.schemarise.alfa.runtime.DataConsumer>
                _resultsSupplier =
                        (p, consumer) -> {
                            _resultsSupplierInner1.accept(p.getResults(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                Modifications, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta resultsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_resultsSupplier),
                        java.util.Optional.of(_resultsConsumer),
                        java.util.Optional.of(_resultsSupplierInner1),
                        java.util.Optional.of(_resultsConsumerInner1),
                        _resultsType,
                        FIELD_RESULTS,
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
        public ModificationsBuilder builder() {
            return new schemarise.alfa.runtime.model.diff.Modifications._ModificationsBuilderImpl();
        }

        @Override
        public ModificationsBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.diff.Modifications._ModificationsBuilderImpl(
                    cc);
        }

        public ModificationsDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Modifications>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_RESULTS, resultsMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Modifications>>
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
                case FIELD_ID_RESULTS:
                    return FIELD_RESULTS;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Unknown field id " + id);
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
            schemarise.alfa.runtime.model.diff.Modifications{Results:map<schemarise.alfa.runtime.model.diff.ChangeCategoryType[de6e8578],schemarise.alfa.runtime.model.diff.ChangeCategoryType[de6e8578]>;}
            schemarise.alfa.runtime.model.diff.Modifications{Results:map<schemarise.alfa.runtime.model.diff.ChangeCategoryType[de6e8578],schemarise.alfa.runtime.model.diff.ChangeCategoryType[de6e8578]>;}
            */
            return "ca3e0c53:";
        }
    }
    // </editor-fold>

}