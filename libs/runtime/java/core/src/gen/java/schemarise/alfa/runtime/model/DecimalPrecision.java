// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface DecimalPrecision extends com.schemarise.alfa.runtime.Record {

    int getScale();

    int getPrecision();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static DecimalPrecisionBuilder builder() {
        return new schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionBuilderImpl();
    }

    public static DecimalPrecisionBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.DecimalPrecision
                                    .DecimalPrecisionDescriptor.INSTANCE);
        else
            return new schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionBuilderImpl(
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
    public interface DecimalPrecisionBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        DecimalPrecisionBuilder setScale(int v);

        int getScale();

        DecimalPrecisionBuilder setPrecision(int v);

        int getPrecision();

        DecimalPrecision build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _DecimalPrecisionBuilderImpl extends _DecimalPrecision__Base__
            implements DecimalPrecisionBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _DecimalPrecisionBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _DecimalPrecisionBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                    .INSTANCE;
        }

        public DecimalPrecisionBuilder setScale(int v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Scale", v);
            this._scale = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                            .FIELD_ID_SCALE);
            return this;
        }

        public DecimalPrecisionBuilder setPrecision(int v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Precision", v);
            this._precision = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                            .FIELD_ID_PRECISION);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "scale":
                    setScale((int) val);
                    break;
                case "precision":
                    setPrecision((int) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public DecimalPrecision build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    2,
                    schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                            .INSTANCE);

            DecimalPrecision obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (DecimalPrecision)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(builderConfig(), descriptor(), _scale, _precision);
            else
                obj =
                        new schemarise.alfa.runtime.model.DecimalPrecision
                                ._DecimalPrecisionConcrete(_scale, _precision);

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.DecimalPrecision",
                    new java.lang.String[] {"scale", "precision"},
                    new java.lang.Object[] {_scale, _precision});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _DecimalPrecisionConcrete extends _DecimalPrecision__Base__
            implements DecimalPrecision {

        private _DecimalPrecisionConcrete() {
            super();
        }

        private _DecimalPrecisionConcrete(int _scale, int _precision) {
            super(_scale, _precision);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _DecimalPrecision__Base__ {
        public int _scale;
        public int _precision;

        public _DecimalPrecision__Base__() {}

        public _DecimalPrecision__Base__(int _scale, int _precision) {
            this._scale = _scale;
            this._precision = _precision;
        }

        public int getScale() {
            return _scale;
        }

        public int getPrecision() {
            return _precision;
        }

        public int hashCode() {
            return java.util.Objects.hash(_scale, _precision);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.DecimalPrecision",
                    new java.lang.String[] {"scale", "precision"},
                    new java.lang.Object[] {_scale, _precision});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionConcrete))
                return false;
            schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionConcrete rhs =
                    (schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionConcrete) o;
            return _scale == rhs._scale && _precision == rhs._precision;
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                    .INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "scale":
                    return _scale;
                case "precision":
                    return _precision;
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

    public static final class DecimalPrecisionDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.DecimalPrecision";
        public static schemarise.alfa.runtime.model.DecimalPrecision.DecimalPrecisionDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.DecimalPrecision
                                .DecimalPrecisionDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_SCALE = 0;
        public static final String FIELD_SCALE = "scale";

        public static final short FIELD_ID_PRECISION = 1;
        public static final String FIELD_PRECISION = "precision";

        private schemarise.alfa.runtime.model.ScalarDataType _scaleType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.intType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _precisionType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.intType)
                        .build();

        /* -- Consumer int -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.Integer>
                _scaleConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.intValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _scaleType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _scaleConsumer =
                        (builder, supplier) -> {
                            builder.setScale(_scaleConsumerInner1.apply(supplier));
                        };
        /* -- Consumer int -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.Integer>
                _precisionConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.intValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _precisionType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _precisionConsumer =
                        (builder, supplier) -> {
                            builder.setPrecision(_precisionConsumerInner1.apply(supplier));
                        };

        /* -- Supplier int -- */
        java.util.function.BiConsumer<java.lang.Integer, com.schemarise.alfa.runtime.DataConsumer>
                _scaleSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _scaleType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecimalPrecision,
                        com.schemarise.alfa.runtime.DataConsumer>
                _scaleSupplier =
                        (p, consumer) -> {
                            _scaleSupplierInner1.accept(p.getScale(), consumer);
                        };
        /* -- Supplier int -- */
        java.util.function.BiConsumer<java.lang.Integer, com.schemarise.alfa.runtime.DataConsumer>
                _precisionSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _precisionType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecimalPrecision,
                        com.schemarise.alfa.runtime.DataConsumer>
                _precisionSupplier =
                        (p, consumer) -> {
                            _precisionSupplierInner1.accept(p.getPrecision(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                DecimalPrecision, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta scaleMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_scaleSupplier),
                        java.util.Optional.of(_scaleConsumer),
                        java.util.Optional.of(_scaleSupplierInner1),
                        java.util.Optional.of(_scaleConsumerInner1),
                        _scaleType,
                        FIELD_SCALE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta precisionMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_precisionSupplier),
                        java.util.Optional.of(_precisionConsumer),
                        java.util.Optional.of(_precisionSupplierInner1),
                        java.util.Optional.of(_precisionConsumerInner1),
                        _precisionType,
                        FIELD_PRECISION,
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
        public DecimalPrecisionBuilder builder() {
            return new schemarise.alfa.runtime.model.DecimalPrecision
                    ._DecimalPrecisionBuilderImpl();
        }

        @Override
        public DecimalPrecisionBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.DecimalPrecision._DecimalPrecisionBuilderImpl(
                    cc);
        }

        public DecimalPrecisionDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<DecimalPrecision>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_SCALE, scaleMeta);
                                        put(FIELD_PRECISION, precisionMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<DecimalPrecision>>
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
                case FIELD_ID_SCALE:
                    return FIELD_SCALE;
                case FIELD_ID_PRECISION:
                    return FIELD_PRECISION;
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
            schemarise.alfa.runtime.model.DecimalPrecision{precision:int;scale:int;}
            schemarise.alfa.runtime.model.DecimalPrecision{precision:int;scale:int;}
            */
            return "4fb9a62f:";
        }
    }
    // </editor-fold>

}