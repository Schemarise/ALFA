// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface DecisionExpression__RelativeExpr extends com.schemarise.alfa.runtime.Record {

    schemarise.alfa.runtime.model.RelativeOperatorType getOperator();

    schemarise.alfa.runtime.model.IExpression getRhs();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static DecisionExpression__RelativeExprBuilder builder() {
        return new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                ._DecisionExpression__RelativeExprBuilderImpl();
    }

    public static DecisionExpression__RelativeExprBuilder builder(
            com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                    .DecisionExpression__RelativeExprDescriptor.INSTANCE);
        else
            return new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                    ._DecisionExpression__RelativeExprBuilderImpl(bc);
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
    public interface DecisionExpression__RelativeExprBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        DecisionExpression__RelativeExprBuilder setOperator(
                schemarise.alfa.runtime.model.RelativeOperatorType v);

        schemarise.alfa.runtime.model.RelativeOperatorType getOperator();

        DecisionExpression__RelativeExprBuilder setRhs(schemarise.alfa.runtime.model.IExpression v);

        schemarise.alfa.runtime.model.IExpression getRhs();

        DecisionExpression__RelativeExpr build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _DecisionExpression__RelativeExprBuilderImpl
            extends _DecisionExpression__RelativeExpr__Base__
            implements DecisionExpression__RelativeExprBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _DecisionExpression__RelativeExprBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _DecisionExpression__RelativeExprBuilderImpl(
                com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                    .DecisionExpression__RelativeExprDescriptor.INSTANCE;
        }

        public DecisionExpression__RelativeExprBuilder setOperator(
                schemarise.alfa.runtime.model.RelativeOperatorType v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Operator", v);
            this._operator = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                            .DecisionExpression__RelativeExprDescriptor.FIELD_ID_OPERATOR);
            return this;
        }

        public DecisionExpression__RelativeExprBuilder setRhs(
                schemarise.alfa.runtime.model.IExpression v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Rhs", v);
            this._rhs = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                            .DecisionExpression__RelativeExprDescriptor.FIELD_ID_RHS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "operator":
                    setOperator((schemarise.alfa.runtime.model.RelativeOperatorType) val);
                    break;
                case "rhs":
                    setRhs((schemarise.alfa.runtime.model.IExpression) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public DecisionExpression__RelativeExpr build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    2,
                    schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                            .DecisionExpression__RelativeExprDescriptor.INSTANCE);

            DecisionExpression__RelativeExpr obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (DecisionExpression__RelativeExpr)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(builderConfig(), descriptor(), _operator, _rhs);
            else
                obj =
                        new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                ._DecisionExpression__RelativeExprConcrete(_operator, _rhs);

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr",
                    new java.lang.String[] {"operator", "rhs"},
                    new java.lang.Object[] {_operator, _rhs});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _DecisionExpression__RelativeExprConcrete
            extends _DecisionExpression__RelativeExpr__Base__
            implements DecisionExpression__RelativeExpr {

        private _DecisionExpression__RelativeExprConcrete() {
            super();
        }

        private _DecisionExpression__RelativeExprConcrete(
                schemarise.alfa.runtime.model.RelativeOperatorType _operator,
                schemarise.alfa.runtime.model.IExpression _rhs) {
            super(_operator, _rhs);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _DecisionExpression__RelativeExpr__Base__ {
        public schemarise.alfa.runtime.model.RelativeOperatorType _operator;
        public schemarise.alfa.runtime.model.IExpression _rhs;

        public _DecisionExpression__RelativeExpr__Base__() {}

        public _DecisionExpression__RelativeExpr__Base__(
                schemarise.alfa.runtime.model.RelativeOperatorType _operator,
                schemarise.alfa.runtime.model.IExpression _rhs) {
            this._operator = _operator;
            this._rhs = _rhs;
        }

        public schemarise.alfa.runtime.model.RelativeOperatorType getOperator() {
            return _operator;
        }

        public schemarise.alfa.runtime.model.IExpression getRhs() {
            return _rhs;
        }

        public int hashCode() {
            return java.util.Objects.hash(_operator, _rhs);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr",
                    new java.lang.String[] {"operator", "rhs"},
                    new java.lang.Object[] {_operator, _rhs});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                            ._DecisionExpression__RelativeExprConcrete)) return false;
            schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                            ._DecisionExpression__RelativeExprConcrete
                    rhs =
                            (schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                            ._DecisionExpression__RelativeExprConcrete)
                                    o;
            return java.util.Objects.equals(_operator, rhs._operator)
                    && java.util.Objects.equals(_rhs, rhs._rhs);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                    .DecisionExpression__RelativeExprDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "operator":
                    return _operator;
                case "rhs":
                    return _rhs;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            getOperator().validate(__builderConfig);
            getRhs().validate(__builderConfig);

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class DecisionExpression__RelativeExprDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr";
        public static schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                        .DecisionExpression__RelativeExprDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                .DecisionExpression__RelativeExprDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_OPERATOR = 0;
        public static final String FIELD_OPERATOR = "operator";

        public static final short FIELD_ID_RHS = 1;
        public static final String FIELD_RHS = "rhs";

        private schemarise.alfa.runtime.model.UdtDataType _operatorType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.RelativeOperatorType")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .build();

        private schemarise.alfa.runtime.model.UdtDataType _rhsType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.IExpression")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.traitType)
                        .build();

        /* -- Consumer schemarise.alfa.runtime.model.RelativeOperatorType -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        schemarise.alfa.runtime.model.RelativeOperatorType>
                _operatorConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _operatorType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                ._DecisionExpression__RelativeExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _operatorConsumer =
                        (builder, supplier) -> {
                            builder.setOperator(_operatorConsumerInner1.apply(supplier));
                        };
        /* -- Consumer schemarise.alfa.runtime.model.IExpression -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        schemarise.alfa.runtime.model.IExpression>
                _rhsConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _rhsType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                                ._DecisionExpression__RelativeExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _rhsConsumer =
                        (builder, supplier) -> {
                            builder.setRhs(_rhsConsumerInner1.apply(supplier));
                        };

        /* -- Supplier schemarise.alfa.runtime.model.RelativeOperatorType -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.RelativeOperatorType,
                        com.schemarise.alfa.runtime.DataConsumer>
                _operatorSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _operatorType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _operatorSupplier =
                        (p, consumer) -> {
                            _operatorSupplierInner1.accept(p.getOperator(), consumer);
                        };
        /* -- Supplier schemarise.alfa.runtime.model.IExpression -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.IExpression,
                        com.schemarise.alfa.runtime.DataConsumer>
                _rhsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _rhsType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _rhsSupplier =
                        (p, consumer) -> {
                            _rhsSupplierInner1.accept(p.getRhs(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                DecisionExpression__RelativeExpr,
                                com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta operatorMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_operatorSupplier),
                        java.util.Optional.of(_operatorConsumer),
                        java.util.Optional.of(_operatorSupplierInner1),
                        java.util.Optional.of(_operatorConsumerInner1),
                        _operatorType,
                        FIELD_OPERATOR,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta rhsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_rhsSupplier),
                        java.util.Optional.of(_rhsConsumer),
                        java.util.Optional.of(_rhsSupplierInner1),
                        java.util.Optional.of(_rhsConsumerInner1),
                        _rhsType,
                        FIELD_RHS,
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
        public DecisionExpression__RelativeExprBuilder builder() {
            return new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                    ._DecisionExpression__RelativeExprBuilderImpl();
        }

        @Override
        public DecisionExpression__RelativeExprBuilder builder(
                com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr
                    ._DecisionExpression__RelativeExprBuilderImpl(cc);
        }

        public DecisionExpression__RelativeExprDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String,
                        com.schemarise.alfa.runtime.FieldMeta<DecisionExpression__RelativeExpr>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_OPERATOR, operatorMeta);
                                        put(FIELD_RHS, rhsMeta);
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
        public java.util.Map<
                        java.lang.String,
                        com.schemarise.alfa.runtime.FieldMeta<DecisionExpression__RelativeExpr>>
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
                case FIELD_ID_OPERATOR:
                    return FIELD_OPERATOR;
                case FIELD_ID_RHS:
                    return FIELD_RHS;
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
            schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr{operator:schemarise.alfa.runtime.model.RelativeOperatorType[4571a072];rhs:schemarise.alfa.runtime.model.IExpression[5fb330b3];}
            schemarise.alfa.runtime.model.DecisionExpression__RelativeExpr{operator:schemarise.alfa.runtime.model.RelativeOperatorType[4571a072];rhs:schemarise.alfa.runtime.model.IExpression[5fb330b3];}
            */
            return "4e07b29:";
        }
    }
    // </editor-fold>

}