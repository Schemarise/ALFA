// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model.path;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public interface Path extends com.schemarise.alfa.runtime.Record {

    java.lang.String getField();

    schemarise.alfa.runtime.model.path.PathElement getElement();

    // <editor-fold defaultstate="collapsed" desc="Builder support">
    public static PathBuilder builder() {
        return new schemarise.alfa.runtime.model.path.Path._PathBuilderImpl();
    }

    public static PathBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig bc) {
        if (bc.getCustomBuilderFactory().isPresent())
            return bc.getCustomBuilderFactory()
                    .get()
                    .builder(bc, schemarise.alfa.runtime.model.path.Path.PathDescriptor.INSTANCE);
        else return new schemarise.alfa.runtime.model.path.Path._PathBuilderImpl(bc);
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
    public interface PathBuilder
            extends com.schemarise.alfa.runtime.Builder, com.schemarise.alfa.runtime.AlfaObject {
        PathBuilder setField(java.lang.String v);

        java.lang.String getField();

        PathBuilder setElement(schemarise.alfa.runtime.model.path.PathElement v);

        schemarise.alfa.runtime.model.path.PathElement getElement();

        Path build();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder Impl class">
    final class _PathBuilderImpl extends _Path__Base__ implements PathBuilder {
        private final com.schemarise.alfa.runtime.IBuilderConfig __builderConfig;

        private java.util.BitSet __missingFields;

        private _PathBuilderImpl() {
            this(com.schemarise.alfa.runtime.BuilderConfig.getInstance());
        }

        private _PathBuilderImpl(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            __builderConfig = cc;
            __missingFields = new java.util.BitSet(2);
            __missingFields.set(0, 2);
        }

        protected com.schemarise.alfa.runtime.IBuilderConfig builderConfig() {
            return __builderConfig;
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.path.Path.PathDescriptor.INSTANCE;
        }

        public PathBuilder setField(java.lang.String v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Field", v);
            this._field = v;
            clearMissingFlag(schemarise.alfa.runtime.model.path.Path.PathDescriptor.FIELD_ID_FIELD);
            return this;
        }

        public PathBuilder setElement(schemarise.alfa.runtime.model.path.PathElement v) {
            com.schemarise.alfa.runtime.utils.AlfaUtils.notNull("Element", v);
            this._element = v;
            clearMissingFlag(
                    schemarise.alfa.runtime.model.path.Path.PathDescriptor.FIELD_ID_ELEMENT);
            return this;
        }

        public void modify(java.lang.String fieldName, java.lang.Object val) {
            switch (fieldName) {
                case "Field":
                    setField((java.lang.String) val);
                    break;
                case "Element":
                    setElement((schemarise.alfa.runtime.model.path.PathElement) val);
                    break;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            schemarise.alfa.runtime.model.asserts.ConstraintType.UnknownField,
                            "Attempt to set unknown field " + fieldName);
            }
        }

        public Path build() {

            com.schemarise.alfa.runtime.utils.Utils.assertMandatoryFieldsSet(
                    builderConfig(),
                    __missingFields,
                    2,
                    schemarise.alfa.runtime.model.path.Path.PathDescriptor.INSTANCE);

            Path obj;

            if (builderConfig().getCustomBuilderFactory().isPresent())
                obj =
                        (Path)
                                builderConfig()
                                        .getCustomBuilderFactory()
                                        .get()
                                        .create(builderConfig(), descriptor(), _field, _element);
            else obj = new schemarise.alfa.runtime.model.path.Path._PathConcrete(_field, _element);

            if (builderConfig().shouldValidateOnBuild()) obj.validate(builderConfig());

            return obj;
        }

        private void clearMissingFlag(short flag) {

            __missingFields.clear(flag);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.path.Path",
                    new java.lang.String[] {"Field", "Element"},
                    new java.lang.Object[] {_field, _element});
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Concrete class">

    final class _PathConcrete extends _Path__Base__ implements Path {

        private _PathConcrete() {
            super();
        }

        private _PathConcrete(
                java.lang.String _field, schemarise.alfa.runtime.model.path.PathElement _element) {
            super(_field, _element);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Base class">
    abstract class _Path__Base__ {
        public java.lang.String _field;
        public schemarise.alfa.runtime.model.path.PathElement _element;

        public _Path__Base__() {}

        public _Path__Base__(
                java.lang.String _field, schemarise.alfa.runtime.model.path.PathElement _element) {
            this._field = _field;
            this._element = _element;
        }

        public java.lang.String getField() {
            return _field;
        }

        public schemarise.alfa.runtime.model.path.PathElement getElement() {
            return _element;
        }

        public int hashCode() {
            return java.util.Objects.hash(_field, _element);
        }

        public java.lang.String toString() {
            return com.schemarise.alfa.runtime.utils.Utils.udtToString(
                    "schemarise.alfa.runtime.model.path.Path",
                    new java.lang.String[] {"Field", "Element"},
                    new java.lang.Object[] {_field, _element});
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof schemarise.alfa.runtime.model.path.Path._PathConcrete)) return false;
            schemarise.alfa.runtime.model.path.Path._PathConcrete rhs =
                    (schemarise.alfa.runtime.model.path.Path._PathConcrete) o;
            return java.util.Objects.equals(_field, rhs._field)
                    && java.util.Objects.equals(_element, rhs._element);
        }

        public void traverse(com.schemarise.alfa.runtime.Visitor v) {}

        public java.util.Optional<? extends com.schemarise.alfa.runtime.Key> get$key() {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
            return schemarise.alfa.runtime.model.path.Path.PathDescriptor.INSTANCE;
        }

        public Object get(java.lang.String fieldName) {
            switch (fieldName) {
                case "Field":
                    return _field;
                case "Element":
                    return _element;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Cannot get unknown field " + fieldName);
            }
        }

        public void validate(com.schemarise.alfa.runtime.IBuilderConfig __builderConfig) {

            getElement().validate(__builderConfig);

            // TODO
            // _key if exists, will be mandatory
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class PathDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.path.Path";
        public static schemarise.alfa.runtime.model.path.Path.PathDescriptor INSTANCE =
                new schemarise.alfa.runtime.model.path.Path.PathDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.recordType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_FIELD = 0;
        public static final String FIELD_FIELD = "Field";

        public static final short FIELD_ID_ELEMENT = 1;
        public static final String FIELD_ELEMENT = "Element";

        private schemarise.alfa.runtime.model.ScalarDataType _fieldType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.stringType)
                        .build();

        private schemarise.alfa.runtime.model.UdtDataType _elementType =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setFullyQualifiedName("schemarise.alfa.runtime.model.path.PathElement")
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.unionType)
                        .build();

        /* -- Consumer string -- */
        java.util.function.Function<com.schemarise.alfa.runtime.DataSupplier, java.lang.String>
                _fieldConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.stringValue(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _fieldType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.path.Path._PathBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _fieldConsumer =
                        (builder, supplier) -> {
                            builder.setField(_fieldConsumerInner1.apply(supplier));
                        };
        /* -- Consumer schemarise.alfa.runtime.model.path.PathElement -- */
        java.util.function.Function<
                        com.schemarise.alfa.runtime.DataSupplier,
                        schemarise.alfa.runtime.model.path.PathElement>
                _elementConsumerInner1 =
                        (supplierInner1) -> {
                            return supplierInner1.objectValue(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _elementType));
                        };
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.path.Path._PathBuilderImpl,
                        com.schemarise.alfa.runtime.DataSupplier>
                _elementConsumer =
                        (builder, supplier) -> {
                            builder.setElement(_elementConsumerInner1.apply(supplier));
                        };

        /* -- Supplier string -- */
        java.util.function.BiConsumer<java.lang.String, com.schemarise.alfa.runtime.DataConsumer>
                _fieldSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.ScalarDataType) _fieldType),
                                    pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.path.Path,
                        com.schemarise.alfa.runtime.DataConsumer>
                _fieldSupplier =
                        (p, consumer) -> {
                            _fieldSupplierInner1.accept(p.getField(), consumer);
                        };
        /* -- Supplier schemarise.alfa.runtime.model.path.PathElement -- */
        java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.path.PathElement,
                        com.schemarise.alfa.runtime.DataConsumer>
                _elementSupplierInner1 =
                        (pInner1, consumerInner1) -> {
                            consumerInner1.consume(
                                    ((schemarise.alfa.runtime.model.UdtDataType) _elementType), pInner1);
                        };
        private java.util.function.BiConsumer<
                        schemarise.alfa.runtime.model.path.Path,
                        com.schemarise.alfa.runtime.DataConsumer>
                _elementSupplier =
                        (p, consumer) -> {
                            _elementSupplierInner1.accept(p.getElement(), consumer);
                        };

        public java.util.Optional<
                        java.util.function.BiConsumer<Path, com.schemarise.alfa.runtime.DataConsumer>>
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

        public com.schemarise.alfa.runtime.FieldMeta fieldMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_fieldSupplier),
                        java.util.Optional.of(_fieldConsumer),
                        java.util.Optional.of(_fieldSupplierInner1),
                        java.util.Optional.of(_fieldConsumerInner1),
                        _fieldType,
                        FIELD_FIELD,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta elementMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.of(_elementSupplier),
                        java.util.Optional.of(_elementConsumer),
                        java.util.Optional.of(_elementSupplierInner1),
                        java.util.Optional.of(_elementConsumerInner1),
                        _elementType,
                        FIELD_ELEMENT,
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
        public PathBuilder builder() {
            return new schemarise.alfa.runtime.model.path.Path._PathBuilderImpl();
        }

        @Override
        public PathBuilder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            return new schemarise.alfa.runtime.model.path.Path._PathBuilderImpl(cc);
        }

        public PathDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Path>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_FIELD, fieldMeta);
                                        put(FIELD_ELEMENT, elementMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<Path>>
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
                case FIELD_ID_FIELD:
                    return FIELD_FIELD;
                case FIELD_ID_ELEMENT:
                    return FIELD_ELEMENT;
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
            schemarise.alfa.runtime.model.path.Path{Element:schemarise.alfa.runtime.model.path.PathElement[fee75bcd];Field:string;}
            schemarise.alfa.runtime.model.path.Path{Element:schemarise.alfa.runtime.model.path.PathElement[fee75bcd];Field:string;}
            */
            return "a168a1eb:";
        }
    }
    // </editor-fold>

}