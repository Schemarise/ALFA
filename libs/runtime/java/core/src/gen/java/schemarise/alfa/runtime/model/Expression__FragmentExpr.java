// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface Expression__FragmentExpr extends com.schemarise.alfa.runtime.Record {

    schemarise.alfa.runtime.model.IDataType getExprType();

    java.lang.String getUdtName();

    java.util.List<schemarise.alfa.runtime.model.NamedExpression> getArgs();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static Expression__FragmentExprBuilder builder() {
        return new schemarise.alfa.runtime.model.Expression__FragmentExpr
                ._Expression__FragmentExprBuilderImpl();
    }

    public static Expression__FragmentExprBuilder builder(
            com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.Expression__FragmentExpr
                                    .Expression__FragmentExprDescriptor.INSTANCE);
        else
            return new schemarise.alfa.runtime.model.Expression__FragmentExpr
                    ._Expression__FragmentExprBuilderImpl(bc);
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
    public interface Expression__FragmentExprBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        Expression__FragmentExprBuilder setExprType(schemarise.alfa.runtime.model.IDataType v);

        schemarise.alfa.runtime.model.IDataType getExprType();

        Expression__FragmentExprBuilder setUdtName(java.lang.String v);

        java.lang.String getUdtName();

        Expression__FragmentExprBuilder addArgs(schemarise.alfa.runtime.model.NamedExpression e);

        Expression__FragmentExprBuilder addAllArgs(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all);

        java.util.List<schemarise.alfa.runtime.model.NamedExpression> getArgs();

        Expression__FragmentExpr build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _Expression__FragmentExprBuilderImpl extends _Expression__FragmentExpr__Base__
            implements Expression__FragmentExprBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _Expression__FragmentExprBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _Expression__FragmentExprBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(3);
            __missingFields.set(0, 3);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Expression__FragmentExpr
                    .Expression__FragmentExprDescriptor.INSTANCE;
        }

        public Expression__FragmentExprBuilder setExprType(
                schemarise.alfa.runtime.model.IDataType v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("ExprType", v);
            this._exprType = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_EXPRTYPE);
            return this;
        }

        public Expression__FragmentExprBuilder setUdtName(java.lang.String v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("UdtName", v);
            this._udtName = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_UDTNAME);
            return this;
        }

        private void createArgs() {
            this._args = new java.util.ArrayList<>();
        }

        public Expression__FragmentExprBuilder addArgs(
                schemarise.alfa.runtime.model.NamedExpression e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Args", e);
            if (this._args == null) createArgs();
            this._args.add(e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_ARGS);
            return this;
        }

        public Expression__FragmentExprBuilder setArgs(
                int index, schemarise.alfa.runtime.model.NamedExpression e) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Args", e);
            if (this._args == null) createArgs();
            this._args.set(index, e);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_ARGS);
            return this;
        }

        public Expression__FragmentExprBuilder addAllArgs(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Args", all);
            if (this._args == null) createArgs();
            this._args.addAll(all);
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_ARGS);
            return this;
        }

        private Expression__FragmentExprBuilder setArgs(
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> all) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Args", all);
            this._args = all;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.FIELD_ID_ARGS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "ExprType":
                    setExprType((schemarise.alfa.runtime.model.IDataType) val);
                    break;
                case "udtName":
                    setUdtName((java.lang.String) val);
                    break;
                case "args":
                    setArgs((java.util.List<schemarise.alfa.runtime.model.NamedExpression>) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public Expression__FragmentExpr build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    3,
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            .Expression__FragmentExprDescriptor.INSTANCE);

            Expression__FragmentExpr obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (Expression__FragmentExpr)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(
                                                builderConfig(),
                                                descriptor(),
                                                _exprType,
                                                _udtName,
                                                com.schemarise.alfa.runtime.utils.VectorCloner
                                                        .immutableList(
                                                                builderConfig(),
                                                                schemarise.alfa.runtime.model
                                                                        .Expression__FragmentExpr
                                                                        .Expression__FragmentExprDescriptor
                                                                        .INSTANCE
                                                                        ._argsSupplierInner1,
                                                                _args));
            else
                obj =
                        new schemarise.alfa.runtime.model.Expression__FragmentExpr
                                ._Expression__FragmentExprConcrete(
                                _exprType,
                                _udtName,
                                com.schemarise.alfa.runtime.utils.VectorCloner.immutableList(
                                        builderConfig(),
                                        schemarise.alfa.runtime.model.Expression__FragmentExpr
                                                .Expression__FragmentExprDescriptor.INSTANCE
                                                ._argsSupplierInner1,
                                        _args));

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.Expression__FragmentExpr",
                    new java.lang.String[] {"ExprType", "udtName", "args"},
                    new java.lang.Object[] {_exprType, _udtName, _args});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _Expression__FragmentExprConcrete extends _Expression__FragmentExpr__Base__
            implements Expression__FragmentExpr {

        private _Expression__FragmentExprConcrete() {
            super();
        }

        private _Expression__FragmentExprConcrete(
                schemarise.alfa.runtime.model.IDataType _exprType,
                java.lang.String _udtName,
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> _args) {
            super(_exprType, _udtName, _args);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _Expression__FragmentExpr__Base__ {
        public schemarise.alfa.runtime.model.IDataType _exprType;
        public java.lang.String _udtName;
        public java.util.List<schemarise.alfa.runtime.model.NamedExpression> _args;

        public _Expression__FragmentExpr__Base__() {}

        public _Expression__FragmentExpr__Base__(
                schemarise.alfa.runtime.model.IDataType _exprType,
                java.lang.String _udtName,
                java.util.List<schemarise.alfa.runtime.model.NamedExpression> _args) {
            this._exprType = _exprType;
            this._udtName = _udtName;
            this._args = _args;
        }

        public schemarise.alfa.runtime.model.IDataType getExprType() {
            return _exprType;
        }

        public java.lang.String getUdtName() {
            return _udtName;
        }

        public java.util.List<schemarise.alfa.runtime.model.NamedExpression> getArgs() {
            return _args;
        }

        public int hashCode() {
            return java.util.Objects.hash(_exprType, _udtName, _args);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.Expression__FragmentExpr",
                    new java.lang.String[] {"ExprType", "udtName", "args"},
                    new java.lang.Object[] {_exprType, _udtName, _args});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o
                    instanceof
                    schemarise.alfa.runtime.model.Expression__FragmentExpr
                            ._Expression__FragmentExprConcrete)) return false;
            schemarise.alfa.runtime.model.Expression__FragmentExpr._Expression__FragmentExprConcrete
                    rhs =
                            (schemarise.alfa.runtime.model.Expression__FragmentExpr
                                            ._Expression__FragmentExprConcrete)
                                    o;
            return java.util.Objects.equals(_exprType, rhs._exprType)
                    && java.util.Objects.equals(_udtName, rhs._udtName)
                    && java.util.Objects.equals(_args, rhs._args);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Expression__FragmentExpr
                    .Expression__FragmentExprDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "ExprType":
                    return _exprType;
                case "udtName":
                    return _udtName;
                case "args":
                    return _args;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {
            getExprType().validate(__builderConfig);

            java.util.stream.IntStream.range(0, getArgs().size())
                    .forEach(
                            e0 -> {
                                getArgs().get(e0).validate(__builderConfig);
                            });

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class Expression__FragmentExprDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.Expression__FragmentExpr";
        public static schemarise.alfa.runtime.model.Expression__FragmentExpr
                        .Expression__FragmentExprDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.Expression__FragmentExpr
                                .Expression__FragmentExprDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_EXPRTYPE = 0;
        public static final String FIELD_EXPRTYPE = "ExprType";

        public static final short FIELD_ID_UDTNAME = 1;
        public static final String FIELD_UDTNAME = "udtName";

        public static final short FIELD_ID_ARGS = 2;
        public static final String FIELD_ARGS = "args";

        private schemarise.alfa.runtime.model.UdtDataType _exprTypeType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.IDataType")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.traitType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _udtNameType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.stringType)
                        .build();

        private schemarise.alfa.runtime.model.ListDataType _argsType =
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
                        schemarise.alfa.runtime.model.Expression__FragmentExpr
                                ._Expression__FragmentExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _exprTypeConsumer =
                        (builder, supplier) -> {
                            builder.setExprType(_exprTypeConsumerInner1.apply(supplier));
                        };
        /* -- Consumer string -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                _udtNameConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.stringValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _udtNameType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__FragmentExpr
                                ._Expression__FragmentExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _udtNameConsumer =
                        (builder, supplier) -> {
                            builder.setUdtName(_udtNameConsumerInner1.apply(supplier));
                        };
        /* -- Consumer list< schemarise.alfa.runtime.model.NamedExpression > -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        java.util.List<schemarise.alfa.runtime.model.NamedExpression>>
                _argsConsumerInner1 =
                        (supplierInner1) -> {
                            java.util.function.Function<
                                            com.schemarise.alfa.runtime.DataSupplier,
                                            schemarise.alfa.runtime.model.NamedExpression>
                                    _argsConsumerInner2 =
                                            (supplierInner2) -> {
                                                return supplierInner2.objectValue(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _argsType)
                                                                        .getComponentType()));
                                            };
                            return supplierInner1.listValue(
                                    ((schemarise.alfa.runtime.model.ListDataType) _argsType),
                                    _argsConsumerInner2);
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__FragmentExpr
                                ._Expression__FragmentExprBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _argsConsumer =
                        (builder, supplier) -> {
                            builder.addAllArgs(_argsConsumerInner1.apply(supplier));
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
                        schemarise.alfa.runtime.model.Expression__FragmentExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _exprTypeSupplier =
                        (p, consumer) -> {
                            _exprTypeSupplierInner1.accept(p.getExprType(), consumer);
                        };
        /* -- Supplier string -- */
        java.util.function.BiConsumer<java.lang.String, com.schemarise.alfa.runtime.DataConsumer>
                _udtNameSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _udtNameType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__FragmentExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _udtNameSupplier =
                        (p, consumer) -> {
                            _udtNameSupplierInner1.accept(p.getUdtName(), consumer);
                        };
        /* -- Supplier list< schemarise.alfa.runtime.model.NamedExpression > -- */
        java.util.function.BiConsumer<
                        java.util.List<schemarise.alfa.runtime.model.NamedExpression>,
                        com.schemarise.alfa.runtime.DataConsumer>
                _argsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            java.util.function.BiConsumer<
                                            schemarise.alfa.runtime.model.NamedExpression,
                                            com.schemarise.alfa.runtime.DataConsumer>
                                    _argsSupplierInner2 =
                                            (pInner2, consumerInner2) -> {
                                                consumerInner2.consume(
                                                        ((schemarise.alfa.runtime.model.UdtDataType)
                                                                ((schemarise.alfa.runtime.model
                                                                                        .ListDataType)
                                                                                _argsType)
                                                                        .getComponentType()),
                                                        pInner2);
                                            };
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ListDataType) _argsType),
                                    pInner1,
                                    _argsSupplierInner2);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Expression__FragmentExpr,
                        com.schemarise.alfa.runtime.DataConsumer>
                _argsSupplier =
                        (p, consumer) -> {
                            _argsSupplierInner1.accept(p.getArgs(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                Expression__FragmentExpr, com.schemarise.alfa.runtime.DataConsumer>>
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
        public com.schemarise.alfa.runtime.FieldMeta udtNameMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_udtNameSupplier),
                        java.util.Optional.of(_udtNameConsumer),
                        java.util.Optional.of(_udtNameSupplierInner1),
                        java.util.Optional.of(_udtNameConsumerInner1),
                        _udtNameType,
                        FIELD_UDTNAME,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta argsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_argsSupplier),
                        java.util.Optional.of(_argsConsumer),
                        java.util.Optional.of(_argsSupplierInner1),
                        java.util.Optional.of(_argsConsumerInner1),
                        _argsType,
                        FIELD_ARGS,
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
        public Expression__FragmentExprBuilder builder() {
            return new schemarise.alfa.runtime.model.Expression__FragmentExpr
                    ._Expression__FragmentExprBuilderImpl();
        }

        @Override
        public Expression__FragmentExprBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.Expression__FragmentExpr
                    ._Expression__FragmentExprBuilderImpl(cc);
        }

        public Expression__FragmentExprDescriptor() {
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
                        com.schemarise.alfa.runtime.FieldMeta<Expression__FragmentExpr>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_EXPRTYPE, exprTypeMeta);
                                        put(FIELD_UDTNAME, udtNameMeta);
                                        put(FIELD_ARGS, argsMeta);
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
                        com.schemarise.alfa.runtime.FieldMeta<Expression__FragmentExpr>>
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
                case FIELD_ID_UDTNAME:
                    return FIELD_UDTNAME;
                case FIELD_ID_ARGS:
                    return FIELD_ARGS;
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
            schemarise.alfa.runtime.model.Expression__FragmentExpr{ExprType:schemarise.alfa.runtime.model.IDataType[4404d6a0];args:list<schemarise.alfa.runtime.model.NamedExpression[7e92a8e5]>;udtName:string;}
            schemarise.alfa.runtime.model.Expression__FragmentExpr{ExprType:schemarise.alfa.runtime.model.IDataType[5fbc41ae];args:list<schemarise.alfa.runtime.model.NamedExpression[7e92a8e5]>;udtName:string;}
            */
            return "2072faf3:daaed510";
        }
    }
    // </editor-fold>

}
