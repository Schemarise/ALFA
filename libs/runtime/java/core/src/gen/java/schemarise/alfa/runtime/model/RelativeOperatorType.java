// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public enum RelativeOperatorType implements com.schemarise.alfa.runtime.Enum {
    LessThan("LessThan", java.util.Optional.empty()),
    LessThanEqualTo("LessThanEqualTo", java.util.Optional.empty()),
    GreaterThan("GreaterThan", java.util.Optional.empty()),
    GreaterThanEqualTo("GreaterThanEqualTo", java.util.Optional.empty());

    private final java.lang.String value;
    private final java.util.Optional<java.lang.String> lexical;

    private static java.util.Map<java.lang.String, RelativeOperatorType> mappings;

    RelativeOperatorType(java.lang.String v, java.util.Optional<java.lang.String> lex) {
        value = v;
        lexical = lex;
    }

    public static RelativeOperatorType fromValue(java.lang.String v) {
        if (mappings == null) {
            java.util.Map<java.lang.String, RelativeOperatorType> m = new java.util.HashMap<>();
            for (RelativeOperatorType c : RelativeOperatorType.values()) {
                m.put(c.value, c);
            }
            mappings = m;
        }

        return mappings.get(v);
    }

    public java.util.Optional<String> getLexicalValue() {
        return lexical;
    }

    public java.lang.String value() {
        return value;
    }

    public com.schemarise.alfa.runtime.TypeDescriptor descriptor() {
        return schemarise.alfa.runtime.model.RelativeOperatorType.RelativeOperatorTypeDescriptor
                .INSTANCE;
    }

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class RelativeOperatorTypeDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.RelativeOperatorType";
        public static schemarise.alfa.runtime.model.RelativeOperatorType
                        .RelativeOperatorTypeDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.RelativeOperatorType
                                .RelativeOperatorTypeDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_LESSTHAN = 0;
        public static final String FIELD_LESSTHAN = "LessThan";

        public static final short FIELD_ID_LESSTHANEQUALTO = 1;
        public static final String FIELD_LESSTHANEQUALTO = "LessThanEqualTo";

        public static final short FIELD_ID_GREATERTHAN = 2;
        public static final String FIELD_GREATERTHAN = "GreaterThan";

        public static final short FIELD_ID_GREATERTHANEQUALTO = 3;
        public static final String FIELD_GREATERTHANEQUALTO = "GreaterThanEqualTo";

        private schemarise.alfa.runtime.model.ScalarDataType _lessThanType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _lessThanEqualToType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _greaterThanType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _greaterThanEqualToType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        public java.util.Optional<
                        java.util.function.Function<
                                RelativeOperatorType, java.util.function.Supplier>>
                getFieldSupplier(java.lang.String fieldName) {
            return java.util.Optional.empty();
        }

        public java.util.Optional<
                        java.util.function.BiConsumer<
                                com.schemarise.alfa.runtime.Builder,
                                com.schemarise.alfa.runtime.DataSupplier>>
                getFieldConsumer(java.lang.String fieldName) {
            return java.util.Optional.empty();
        }

        public com.schemarise.alfa.runtime.FieldMeta lessThanMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _lessThanType,
                        FIELD_LESSTHAN,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta lessThanEqualToMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _lessThanEqualToType,
                        FIELD_LESSTHANEQUALTO,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta greaterThanMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _greaterThanType,
                        FIELD_GREATERTHAN,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta greaterThanEqualToMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _greaterThanEqualToType,
                        FIELD_GREATERTHANEQUALTO,
                        java.util.Optional.empty());

        @Override
        public boolean hasBuilder() {
            return false;
        }

        @Override
        public boolean convertableToBuilder() {
            return false;
        }

        @Override
        public com.schemarise.alfa.runtime.Builder builder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.schemarise.alfa.runtime.Builder builder(com.schemarise.alfa.runtime.IBuilderConfig cc) {
            throw new UnsupportedOperationException();
        }

        public RelativeOperatorTypeDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<RelativeOperatorType>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_LESSTHAN, lessThanMeta);
                                        put(FIELD_LESSTHANEQUALTO, lessThanEqualToMeta);
                                        put(FIELD_GREATERTHAN, greaterThanMeta);
                                        put(FIELD_GREATERTHANEQUALTO, greaterThanEqualToMeta);
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
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<RelativeOperatorType>>
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
                case FIELD_ID_LESSTHAN:
                    return FIELD_LESSTHAN;
                case FIELD_ID_LESSTHANEQUALTO:
                    return FIELD_LESSTHANEQUALTO;
                case FIELD_ID_GREATERTHAN:
                    return FIELD_GREATERTHAN;
                case FIELD_ID_GREATERTHANEQUALTO:
                    return FIELD_GREATERTHANEQUALTO;
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
            schemarise.alfa.runtime.model.RelativeOperatorType{LessThan,LessThanEqualTo,GreaterThan,GreaterThanEqualTo}
            schemarise.alfa.runtime.model.RelativeOperatorType{LessThan,LessThanEqualTo,GreaterThan,GreaterThanEqualTo}
            */
            return "4571a072:";
        }
    }
    // </editor-fold>
}
