// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model.diff;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.JavaExporter")
public enum UdtEntryType implements com.schemarise.alfa.runtime.Enum {
    Field("Field", java.util.Optional.empty()),
    Method("Method", java.util.Optional.empty()),
    Assert("Assert", java.util.Optional.empty()),
    Function("Function", java.util.Optional.empty()),
    Publish("Publish", java.util.Optional.empty()),
    Consume("Consume", java.util.Optional.empty()),
    Scope("Scope", java.util.Optional.empty()),
    ReachableType("ReachableType", java.util.Optional.empty());

    private final java.lang.String value;
    private final java.util.Optional<java.lang.String> lexical;

    private static java.util.Map<java.lang.String, UdtEntryType> mappings;

    UdtEntryType(java.lang.String v, java.util.Optional<java.lang.String> lex) {
        value = v;
        lexical = lex;
    }

    public static UdtEntryType fromValue(java.lang.String v) {
        if (mappings == null) {
            java.util.Map<java.lang.String, UdtEntryType> m = new java.util.HashMap<>();
            for (UdtEntryType c : UdtEntryType.values()) {
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
        return schemarise.alfa.runtime.model.diff.UdtEntryType.UdtEntryTypeDescriptor.INSTANCE;
    }

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class UdtEntryTypeDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.diff.UdtEntryType";
        public static schemarise.alfa.runtime.model.diff.UdtEntryType.UdtEntryTypeDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.diff.UdtEntryType
                                .UdtEntryTypeDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_FIELD = 0;
        public static final String FIELD_FIELD = "Field";

        public static final short FIELD_ID_METHOD = 1;
        public static final String FIELD_METHOD = "Method";

        public static final short FIELD_ID_ASSERT = 2;
        public static final String FIELD_ASSERT = "Assert";

        public static final short FIELD_ID_FUNCTION = 3;
        public static final String FIELD_FUNCTION = "Function";

        public static final short FIELD_ID_PUBLISH = 4;
        public static final String FIELD_PUBLISH = "Publish";

        public static final short FIELD_ID_CONSUME = 5;
        public static final String FIELD_CONSUME = "Consume";

        public static final short FIELD_ID_SCOPE = 6;
        public static final String FIELD_SCOPE = "Scope";

        public static final short FIELD_ID_REACHABLETYPE = 7;
        public static final String FIELD_REACHABLETYPE = "ReachableType";

        private schemarise.alfa.runtime.model.ScalarDataType _fieldType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _methodType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _assertType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _functionType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _publishType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _consumeType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _scopeType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _reachableTypeType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        public java.util.Optional<
                        java.util.function.Function<UdtEntryType, java.util.function.Supplier>>
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

        public com.schemarise.alfa.runtime.FieldMeta fieldMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _fieldType,
                        FIELD_FIELD,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta methodMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _methodType,
                        FIELD_METHOD,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta _assertMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _assertType,
                        FIELD_ASSERT,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta functionMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _functionType,
                        FIELD_FUNCTION,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta publishMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _publishType,
                        FIELD_PUBLISH,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta consumeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _consumeType,
                        FIELD_CONSUME,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta scopeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _scopeType,
                        FIELD_SCOPE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta reachableTypeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _reachableTypeType,
                        FIELD_REACHABLETYPE,
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
        public com.schemarise.alfa.runtime.Builder builder(
                com.schemarise.alfa.runtime.IBuilderConfig cc) {
            throw new UnsupportedOperationException();
        }

        public UdtEntryTypeDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<
                        java.lang.String, com.schemarise.alfa.runtime.FieldMeta<UdtEntryType>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_FIELD, fieldMeta);
                                        put(FIELD_METHOD, methodMeta);
                                        put(FIELD_ASSERT, _assertMeta);
                                        put(FIELD_FUNCTION, functionMeta);
                                        put(FIELD_PUBLISH, publishMeta);
                                        put(FIELD_CONSUME, consumeMeta);
                                        put(FIELD_SCOPE, scopeMeta);
                                        put(FIELD_REACHABLETYPE, reachableTypeMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<UdtEntryType>>
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
                case FIELD_ID_METHOD:
                    return FIELD_METHOD;
                case FIELD_ID_ASSERT:
                    return FIELD_ASSERT;
                case FIELD_ID_FUNCTION:
                    return FIELD_FUNCTION;
                case FIELD_ID_PUBLISH:
                    return FIELD_PUBLISH;
                case FIELD_ID_CONSUME:
                    return FIELD_CONSUME;
                case FIELD_ID_SCOPE:
                    return FIELD_SCOPE;
                case FIELD_ID_REACHABLETYPE:
                    return FIELD_REACHABLETYPE;
                default:
                    throw new com.schemarise.alfa.runtime.AlfaRuntimeException(
                            "Unknown field id " + id);
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
            schemarise.alfa.runtime.model.diff.UdtEntryType{Field,Method,Assert,Function,Publish,Consume,Scope,ReachableType}
            schemarise.alfa.runtime.model.diff.UdtEntryType{Field,Method,Assert,Function,Publish,Consume,Scope,ReachableType}
            */
            return "6097f1e4:";
        }
    }
    // </editor-fold>
}
