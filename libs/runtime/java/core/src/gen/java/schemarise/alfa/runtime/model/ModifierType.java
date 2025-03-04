// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise ALFA toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public enum ModifierType implements com.schemarise.alfa.runtime.Enum {
    Fragment("Fragment", java.util.Optional.empty()),
    Internal("Internal", java.util.Optional.empty());

    private final java.lang.String value;
    private final java.util.Optional<java.lang.String> lexical;

    private static java.util.Map<java.lang.String, ModifierType> mappings;

    ModifierType(java.lang.String v, java.util.Optional<java.lang.String> lex) {
        value = v;
        lexical = lex;
    }

    public static ModifierType fromValue(java.lang.String v) {
        if (mappings == null) {
            java.util.Map<java.lang.String, ModifierType> m = new java.util.HashMap<>();
            for (ModifierType c : ModifierType.values()) {
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
        return schemarise.alfa.runtime.model.ModifierType.ModifierTypeDescriptor.INSTANCE;
    }

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class ModifierTypeDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME = "schemarise.alfa.runtime.model.ModifierType";
        public static schemarise.alfa.runtime.model.ModifierType.ModifierTypeDescriptor INSTANCE =
                new schemarise.alfa.runtime.model.ModifierType.ModifierTypeDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_FRAGMENT = 0;
        public static final String FIELD_FRAGMENT = "Fragment";

        public static final short FIELD_ID_INTERNAL = 1;
        public static final String FIELD_INTERNAL = "Internal";

        private schemarise.alfa.runtime.model.ScalarDataType _fragmentType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _internalType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        public java.util.Optional<
                        java.util.function.Function<ModifierType, java.util.function.Supplier>>
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

        public com.schemarise.alfa.runtime.FieldMeta fragmentMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _fragmentType,
                        FIELD_FRAGMENT,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta internalMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _internalType,
                        FIELD_INTERNAL,
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

        public ModifierTypeDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<ModifierType>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_FRAGMENT, fragmentMeta);
                                        put(FIELD_INTERNAL, internalMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<ModifierType>>
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
                case FIELD_ID_FRAGMENT:
                    return FIELD_FRAGMENT;
                case FIELD_ID_INTERNAL:
                    return FIELD_INTERNAL;
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
            schemarise.alfa.runtime.model.ModifierType{Fragment,Internal}
            schemarise.alfa.runtime.model.ModifierType{Fragment,Internal}
            */
            return "db5c8911:";
        }
    }
    // </editor-fold>
}
