// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model.blk;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public enum CheckType implements com.schemarise.alfa.runtime.Enum {
    String("String", java.util.Optional.empty()),
    Number("Number", java.util.Optional.empty()),
    Boolean("Boolean", java.util.Optional.empty());

    private final java.lang.String value;
    private final java.util.Optional<java.lang.String> lexical;

    private static java.util.Map<java.lang.String, CheckType> mappings;

    CheckType(java.lang.String v, java.util.Optional<java.lang.String> lex) {
        value = v;
        lexical = lex;
    }

    public static CheckType fromValue(java.lang.String v) {
        if (mappings == null) {
            java.util.Map<java.lang.String, CheckType> m = new java.util.HashMap<>();
            for (CheckType c : CheckType.values()) {
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
        return schemarise.alfa.runtime.model.blk.CheckType.CheckTypeDescriptor.INSTANCE;
    }

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class CheckTypeDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.blk.CheckType";
        public static schemarise.alfa.runtime.model.blk.CheckType.CheckTypeDescriptor INSTANCE =
                new schemarise.alfa.runtime.model.blk.CheckType.CheckTypeDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_STRING = 0;
        public static final String FIELD_STRING = "String";

        public static final short FIELD_ID_NUMBER = 1;
        public static final String FIELD_NUMBER = "Number";

        public static final short FIELD_ID_BOOLEAN = 2;
        public static final String FIELD_BOOLEAN = "Boolean";

        private schemarise.alfa.runtime.model.ScalarDataType _stringType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _numberType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _booleanType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        public java.util.Optional<
                        java.util.function.Function<CheckType, java.util.function.Supplier>>
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

        public com.schemarise.alfa.runtime.FieldMeta stringMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _stringType,
                        FIELD_STRING,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta numberMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _numberType,
                        FIELD_NUMBER,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta _booleanMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _booleanType,
                        FIELD_BOOLEAN,
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

        public CheckTypeDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<CheckType>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_STRING, stringMeta);
                                        put(FIELD_NUMBER, numberMeta);
                                        put(FIELD_BOOLEAN, _booleanMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<CheckType>>
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
                case FIELD_ID_STRING:
                    return FIELD_STRING;
                case FIELD_ID_NUMBER:
                    return FIELD_NUMBER;
                case FIELD_ID_BOOLEAN:
                    return FIELD_BOOLEAN;
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
            schemarise.alfa.runtime.model.blk.CheckType{String,Number,Boolean}
            schemarise.alfa.runtime.model.blk.CheckType{String,Number,Boolean}
            */
            return "465bdc5c:";
        }
    }
    // </editor-fold>
}
