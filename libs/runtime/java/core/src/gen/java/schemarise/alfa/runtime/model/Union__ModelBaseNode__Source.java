// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface Union__ModelBaseNode__Source extends com.schemarise.alfa.runtime.Union {

    public default schemarise.alfa.runtime.model.Location getLoc() {
        throw new IllegalStateException(
                "Accessing field loc is not supported in " + getClass().getSimpleName());
    }

    public default boolean isLoc() {
        return false;
    }

    public default java.lang.String getContents() {
        throw new IllegalStateException(
                "Accessing field Contents is not supported in " + getClass().getSimpleName());
    }

    public default boolean isContents() {
        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Caseloc">
    static final class Caseloc implements Union__ModelBaseNode__Source {
        private schemarise.alfa.runtime.model.Location _loc;

        private Caseloc(
                com.schemarise.alfa.runtime.IBuilderConfig __builderConfig,
                schemarise.alfa.runtime.model.Location v) {
            this._loc = v;
            getLoc().validate(__builderConfig);
        }

        public schemarise.alfa.runtime.model.Location getLoc() {
            return this._loc;
        }

        public boolean isLoc() {
            return true;
        }

        public java.lang.String caseName() {
            return "loc";
        }

        public java.lang.Object caseValue() {
            return _loc;
        }

        public boolean isTagged() {
            return true;
        }

        public java.lang.Object get(java.lang.String f) {
            if (f.equals("loc")) return _loc;
            else throw new IllegalStateException("Cannot access loc");
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public int hashCode() {
            return com.schemarise.alfa.runtime.utils.Utils.unionHashCode(this);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.unionToString(this);
        }

        public boolean equals(Object o) {
            return com.schemarise.alfa.runtime.utils.Utils.unionEquals(this, o);
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    .Union__ModelBaseNode__SourceDescriptor.INSTANCE;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="CaseContents">
    static final class CaseContents implements Union__ModelBaseNode__Source {
        private java.lang.String _contents;

        private CaseContents(
                com.schemarise.alfa.runtime.IBuilderConfig __builderConfig, java.lang.String v) {
            this._contents = v;
        }

        public java.lang.String getContents() {
            return this._contents;
        }

        public boolean isContents() {
            return true;
        }

        public java.lang.String caseName() {
            return "Contents";
        }

        public java.lang.Object caseValue() {
            return _contents;
        }

        public boolean isTagged() {
            return true;
        }

        public java.lang.Object get(java.lang.String f) {
            if (f.equals("Contents")) return _contents;
            else throw new IllegalStateException("Cannot access Contents");
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public int hashCode() {
            return com.schemarise.alfa.runtime.utils.Utils.unionHashCode(this);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.unionToString(this);
        }

        public boolean equals(Object o) {
            return com.schemarise.alfa.runtime.utils.Utils.unionEquals(this, o);
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    .Union__ModelBaseNode__SourceDescriptor.INSTANCE;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder class">

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static Union__ModelBaseNode__SourceBuilder builder() {
        return new schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                ._Union__ModelBaseNode__SourceBuilderImpl();
    }

    public static Union__ModelBaseNode__SourceBuilder builder(
            com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(
                            bc,
                            schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                                    .Union__ModelBaseNode__SourceDescriptor.INSTANCE);
        else
            return new schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    ._Union__ModelBaseNode__SourceBuilderImpl(bc);
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
    public interface Union__ModelBaseNode__SourceBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        Union__ModelBaseNode__SourceBuilder setLoc(schemarise.alfa.runtime.model.Location v);

        Union__ModelBaseNode__SourceBuilder setContents(java.lang.String v);

        Union__ModelBaseNode__Source build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _Union__ModelBaseNode__SourceBuilderImpl
            implements Union__ModelBaseNode__SourceBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private schemarise.alfa.runtime.model.Location _loc;
        private java.lang.String _contents;

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "loc":
                    return _loc;
                case "Contents":
                    return _contents;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Request for unknown field " + fieldName);
            }
        }

        private java.util.BitSet __missingFields;

        private _Union__ModelBaseNode__SourceBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _Union__ModelBaseNode__SourceBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    .Union__ModelBaseNode__SourceDescriptor.INSTANCE;
        }

        public Union__ModelBaseNode__SourceBuilder setLoc(
                schemarise.alfa.runtime.model.Location v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Loc", v);
            this._loc = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                            .Union__ModelBaseNode__SourceDescriptor.FIELD_ID_LOC);
            return this;
        }

        public Union__ModelBaseNode__SourceBuilder setContents(java.lang.String v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Contents", v);
            this._contents = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                            .Union__ModelBaseNode__SourceDescriptor.FIELD_ID_CONTENTS);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "loc":
                    setLoc((schemarise.alfa.runtime.model.Location) val);
                    break;
                case "Contents":
                    setContents((java.lang.String) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public Union__ModelBaseNode__Source build() {

            if (!__missingFields.get(
                    schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                            .Union__ModelBaseNode__SourceDescriptor.FIELD_ID_LOC))
                return new Caseloc(__builderConfig, _loc);

            if (!__missingFields.get(
                    schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                            .Union__ModelBaseNode__SourceDescriptor.FIELD_ID_CONTENTS))
                return new CaseContents(__builderConfig, _contents);

            throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                    "Union case not assigned or more than 1 case assigned");
        }

        private void clearMissingFlag(short flag) {
            com.schemarise.alfa.runtime.utils.Utils.enforceNoFieldSet(
                    __missingFields,
                    schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                            .Union__ModelBaseNode__SourceDescriptor.INSTANCE,
                    2,
                    flag);
            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.Union__ModelBaseNode__Source",
                    new java.lang.String[] {"loc", "Contents"},
                    new java.lang.Object[] {_loc, _contents});
        }
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class Union__ModelBaseNode__SourceDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.Union__ModelBaseNode__Source";
        public static schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                        .Union__ModelBaseNode__SourceDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                                .Union__ModelBaseNode__SourceDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.unionType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_LOC = 0;
        public static final String FIELD_LOC = "loc";

        public static final short FIELD_ID_CONTENTS = 1;
        public static final String FIELD_CONTENTS = "Contents";

        private schemarise.alfa.runtime.model.UdtDataType _locType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.Location")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _contentsType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.stringType)
                        .build();

        /* -- Consumer schemarise.alfa.runtime.model.Location -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier, schemarise.alfa.runtime.model.Location>
                _locConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _locType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                                ._Union__ModelBaseNode__SourceBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _locConsumer =
                        (builder, supplier) -> {
                            builder.setLoc(_locConsumerInner1.apply(supplier));
                        };
        /* -- Consumer string -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                _contentsConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.stringValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _contentsType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                                ._Union__ModelBaseNode__SourceBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _contentsConsumer =
                        (builder, supplier) -> {
                            builder.setContents(_contentsConsumerInner1.apply(supplier));
                        };

        /* -- Supplier schemarise.alfa.runtime.model.Location -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Location,
                        com.schemarise.alfa.runtime.DataConsumer>
                _locSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _locType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Union__ModelBaseNode__Source,
                        com.schemarise.alfa.runtime.DataConsumer>
                _locSupplier =
                        (p, consumer) -> {
                            _locSupplierInner1.accept(p.getLoc(), consumer);
                        };
        /* -- Supplier string -- */
        java.util.function.BiConsumer<java.lang.String, com.schemarise.alfa.runtime.DataConsumer>
                _contentsSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _contentsType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.Union__ModelBaseNode__Source,
                        com.schemarise.alfa.runtime.DataConsumer>
                _contentsSupplier =
                        (p, consumer) -> {
                            _contentsSupplierInner1.accept(p.getContents(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                Union__ModelBaseNode__Source, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta locMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_locSupplier),
                        java.util.Optional.of(_locConsumer),
                        java.util.Optional.of(_locSupplierInner1),
                        java.util.Optional.of(_locConsumerInner1),
                        _locType,
                        FIELD_LOC,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta contentsMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_contentsSupplier),
                        java.util.Optional.of(_contentsConsumer),
                        java.util.Optional.of(_contentsSupplierInner1),
                        java.util.Optional.of(_contentsConsumerInner1),
                        _contentsType,
                        FIELD_CONTENTS,
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
        public Union__ModelBaseNode__SourceBuilder builder() {
            return new schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    ._Union__ModelBaseNode__SourceBuilderImpl();
        }

        @Override
        public Union__ModelBaseNode__SourceBuilder builder(
                com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.Union__ModelBaseNode__Source
                    ._Union__ModelBaseNode__SourceBuilderImpl(cc);
        }

        public Union__ModelBaseNode__SourceDescriptor() {
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
                        com.schemarise.alfa.runtime.FieldMeta<Union__ModelBaseNode__Source>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_LOC, locMeta);
                                        put(FIELD_CONTENTS, contentsMeta);
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
                        com.schemarise.alfa.runtime.FieldMeta<Union__ModelBaseNode__Source>>
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
                case FIELD_ID_LOC:
                    return FIELD_LOC;
                case FIELD_ID_CONTENTS:
                    return FIELD_CONTENTS;
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
            schemarise.alfa.runtime.model.Union__ModelBaseNode__Source{Contents:string;loc:schemarise.alfa.runtime.model.Location[946b249f];}
            schemarise.alfa.runtime.model.Union__ModelBaseNode__Source{Contents:string;loc:schemarise.alfa.runtime.model.Location[946b249f];}
            */
            return "b4a42718:";
        }
    }
    // </editor-fold>

}