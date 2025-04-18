// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface Expression__TupleExpr extends com.schemarise.alfa.runtime.Record {

    schemarise.alfa.runtime.model.IDataType getExprType();

    java.util.List<schemarise.alfa.runtime.model.NamedExpression> getExpr();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static Expression__TupleExprBuilder builder() {
        return new schemarise.alfa.runtime.model.Expression__TupleExpr
                ._Expression__TupleExprBuilderImpl();
    }

    public static Expression__TupleExprBuilder builder(
            com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.Expression__TupleExpr
                                    .Expression__TupleExprDescriptor.INSTANCE);
        else
            return new schemarise.alfa.runtime.model.Expression__TupleExpr
                    ._Expression__TupleExprBuilderImpl(bc);
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
    public interface Expression__TupleExprBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        Expression__TupleExprBuilder setExprType(schemarise.alfa.runtime.model.IDataType v);

        schemarise.alfa.runtime.model.IDataType getExprType();

        Expression__TupleExprBuilder addExpr(schemarise.alfa.runtime.model.NamedExpression e);

        Expression__TupleExprBuilder addAllExpr(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all);

        java.util.List<schemarise.alfa.runtime.model.NamedExpression> getExpr();

        Expression__TupleExpr build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _Expression__TupleExprBuilderImpl extends _Expression__TupleExpr__Base__
            implements Expression__TupleExprBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _Expression__TupleExprBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _Expression__TupleExprBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Expression__TupleExpr
                    .Expression__TupleExprDescriptor.INSTANCE;
        }

        public Expression__TupleExprBuilder setExprType(schemarise.alfa.runtime.model.IDataType v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("ExprType", v);
            this._exprType = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.FIELD_ID_EXPRTYPE);
            return this;
        }

        private void createExpr() {
            this._expr = new java.util.ArrayList<>();
        }

        public Expression__TupleExprBuilder addExpr(
                schemarise.alfa.runtime.model.NamedExpression e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Expr", e);
            if (this._expr == null) createExpr();
            this._expr.add(e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.FIELD_ID_EXPR);
            return this;
        }

        public Expression__TupleExprBuilder setExpr(
                int index, schemarise.alfa.runtime.model.NamedExpression e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Expr", e);
            if (this._expr == null) createExpr();
            this._expr.set(index, e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.FIELD_ID_EXPR);
            return this;
        }

        public Expression__TupleExprBuilder addAllExpr(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Expr", all);
            if (this._expr == null) createExpr();
            this._expr.addAll(all);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.FIELD_ID_EXPR);
            return this;
        }

        private Expression__TupleExprBuilder setExpr(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Expr", all);
            this._expr = all;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.FIELD_ID_EXPR);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "ExprType":
                    setExprType((schemarise.alfa.runtime.model.IDataType) val);
                    break;
                case "expr":
                    setExpr((java.util.List<schemarise.alfa.runtime.model.NamedExpression>) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public Expression__TupleExpr build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    2,
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            .Expression__TupleExprDescriptor.INSTANCE);

            Expression__TupleExpr obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (Expression__TupleExpr)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(
                                                builderConfig(),
                                                descriptor(),
                                                _exprType,
                                                com.schemarise.alfa.runtime.utils.VectorCloner
                                                        .immutableList(
                                                                builderConfig(),
                                                                schemarise.alfa.runtime.model
                                                                        .Expression__TupleExpr
                                                                        .Expression__TupleExprDescriptor
                                                                        .INSTANCE
                                                                        ._exprSupplierInner1,
                                                                _expr));
            else
                obj =
                        new schemarise.alfa.runtime.model.Expression__TupleExpr
                                ._Expression__TupleExprConcrete(
                                _exprType,
                                com.schemarise.alfa.runtime.utils.VectorCloner.immutableList(
                                        builderConfig(),
                                        schemarise.alfa.runtime.model.Expression__TupleExpr
                                                .Expression__TupleExprDescriptor.INSTANCE
                                                ._exprSupplierInner1,
                                        _expr));

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.Expression__TupleExpr",
                    new java.lang.String[] {"ExprType", "expr"},
                    new java.lang.Object[] {_exprType, _expr});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _Expression__TupleExprConcrete extends _Expression__TupleExpr__Base__
            implements Expression__TupleExpr {

        private _Expression__TupleExprConcrete() {
            super();
        }

        private _Expression__TupleExprConcrete(
                schemarise.alfa.runtime.model.IDataType _exprType,
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> _expr) {
            super(_exprType, _expr);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _Expression__TupleExpr__Base__ {
        public schemarise.alfa.runtime.model.IDataType _exprType;
        public java.util.List<schemarise.alfa.runtime.model.NamedExpression> _expr;

        public _Expression__TupleExpr__Base__() {}

        public _Expression__TupleExpr__Base__(
                schemarise.alfa.runtime.model.IDataType _exprType,
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> _expr) {
            this._exprType = _exprType;
            this._expr = _expr;
        }

        public schemarise.alfa.runtime.model.IDataType getExprType() {
            return _exprType;
        }

        public java.util.List<schemarise.alfa.runtime.model.NamedExpression> getExpr() {
            return _expr;
        }

        public int hashCode() {
            return java.util.Objects.hash(_exprType, _expr);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.Expression__TupleExpr",
                    new java.lang.String[] {"ExprType", "expr"},
                    new java.lang.Object[] {_exprType, _expr});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.Expression__TupleExpr
                            ._Expression__TupleExprConcrete)) return false;
            schemarise.alfa.runtime.model.Expression__TupleExpr._Expression__TupleExprConcrete rhs =
                    (schemarise.alfa.runtime.model.Expression__TupleExpr
                                    ._Expression__TupleExprConcrete)
                            o;
            return java.util.Objects.equals(_exprType, rhs._exprType)
                    && java.util.Objects.equals(_expr, rhs._expr);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Expression__TupleExpr
                    .Expression__TupleExprDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "ExprType":
                    return _exprType;
                case "expr":
                    return _expr;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            getExprType().validate(__builderConfig);
            java.util.stream.IntStream.range(0, getExpr().size())
                    .forEach(
                            e0 -> {
                                getExpr().get(e0).validate(__builderConfig);
                            });

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class Expression__TupleExprDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.Expression__TupleExpr";
        public static schemarise.alfa.runtime.model.Expression__TupleExpr
                        .Expression__TupleExprDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.Expression__TupleExpr
                                .Expression__TupleExprDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_EXPRTYPE = 0;
        public static final String FIELD_EXPRTYPE = "ExprType";

        public static final short FIELD_ID_EXPR = 1;
        public static final String FIELD_EXPR = "expr";

        private schemarise.alfa.runtime.model.UdtDataType _exprTypeType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.IDataType")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.traitType)
                        .build();

        private schemarise.alfa.runtime.model.ListDataType _exprType =
                schemarise.alfa.runtime.model.ListDataType.builder()
                        .setComponentType(
                                schemarise.alfa.runtime.model.UdtDataType.builder()
                                        .setFullyQualifiedName(
                                                "schemarise.alfa.runtime.model.NamedExpression")
                                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                                        .build())
                        .build();

        /* -- Consumer schemarise.alfa.runtime.model.IDataType -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        schemarise.alfa.runtime.model.IDataType>
                _exprTypeConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _exprTypeType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__TupleExpr
                                ._Expression__TupleExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _exprTypeConsumer =
                        (builder, supplier) -> {
                            builder.setExprType(_exprTypeConsumerInner1.apply(supplier));
                        };
        /* -- Consumer list< schemarise.alfa.runtime.model.NamedExpression > -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        java.util.List<schemarise.alfa.runtime.model.NamedExpression>>
                _exprConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier,
                                            schemarise.alfa.runtime.model.NamedExpression>
                                    _exprConsumerInner2 =
                                            (supplierInner2) -> {
                                                return supplierInner2.objectValue(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _exprType)
                                                                        .getComponentType()));
                                            };
                            return supplierInner1.listValue(
                                    ((schemarise.alfa.runtime.model.ListDataType) _exprType),
                                    _exprConsumerInner2);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__TupleExpr
                                ._Expression__TupleExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _exprConsumer =
                        (builder, supplier) -> {
                            builder.addAllExpr(_exprConsumerInner1.apply(supplier));
                        };

        /* -- Supplier schemarise.alfa.runtime.model.IDataType -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.IDataType,
                        com.schemarise.alfa.runtime.DataConsumer>
                _exprTypeSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _exprTypeType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__TupleExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _exprTypeSupplier =
                        (p, consumer) -> {
                            _exprTypeSupplierInner1.accept(p.getExprType(), consumer);
                        };
        /* -- Supplier list< schemarise.alfa.runtime.model.NamedExpression > -- */
        java.util.function.BiConsumer<
                        java.util.List<schemarise.alfa.runtime.model.NamedExpression>,
                        com.schemarise.alfa.runtime.DataConsumer>
                _exprSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            schemarise.alfa.runtime.model.NamedExpression,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _exprSupplierInner2 =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _exprType)
                                                                        .getComponentType()),
                                                        pInner2);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ListDataType) _exprType),
                                    pInner1,
                                    _exprSupplierInner2);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__TupleExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _exprSupplier =
                        (p, consumer) -> {
                            _exprSupplierInner1.accept(p.getExpr(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                Expression__TupleExpr, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta exprTypeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_exprTypeSupplier),
                        java.util.Optional.of(_exprTypeConsumer),
                        java.util.Optional.of(_exprTypeSupplierInner1),
                        java.util.Optional.of(_exprTypeConsumerInner1),
                        _exprTypeType,
                        FIELD_EXPRTYPE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta exprMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_exprSupplier),
                        java.util.Optional.of(_exprConsumer),
                        java.util.Optional.of(_exprSupplierInner1),
                        java.util.Optional.of(_exprConsumerInner1),
                        _exprType,
                        FIELD_EXPR,
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
        public Expression__TupleExprBuilder builder() {
            return new schemarise.alfa.runtime.model.Expression__TupleExpr
                    ._Expression__TupleExprBuilderImpl();
        }

        @Override
        public Expression__TupleExprBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.Expression__TupleExpr
                    ._Expression__TupleExprBuilderImpl(cc);
        }

        public Expression__TupleExprDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Expression__TupleExpr>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_EXPRTYPE, exprTypeMeta);
                                        put(FIELD_EXPR, exprMeta);
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
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Expression__TupleExpr>>
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
                case FIELD_ID_EXPRTYPE:
                    return FIELD_EXPRTYPE;
                case FIELD_ID_EXPR:
                    return FIELD_EXPR;
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
            schemarise.alfa.runtime.model.Expression__TupleExpr{ExprType:schemarise.alfa.runtime.model.IDataType[4404d6a0];expr:list<schemarise.alfa.runtime.model.NamedExpression[7e92a8e5]>;}
            schemarise.alfa.runtime.model.Expression__TupleExpr{ExprType:schemarise.alfa.runtime.model.IDataType[5fbc41ae];expr:list<schemarise.alfa.runtime.model.NamedExpression[7e92a8e5]>;}
            */
            return "731feeb5:2d5db7ca";
        }
    }
    // </editor-fold>

}
