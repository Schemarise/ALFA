// *********************************************************************************************************
//      DO NOT EDIT. This file has been generated by the Schemarise Alfa toolset. See
// www.schemarise.com
// *********************************************************************************************************
package schemarise.alfa.runtime.model.asserts;

@javax.annotation.Generated("com.schemarise.alfa.generators.exporters.java.Java8Exporter")
public enum ConstraintType implements com.schemarise.alfa.runtime.Enum {
    MandatoryFieldNotSet("MandatoryFieldNotSet", java.util.Optional.empty()),
    OutsidePermittedRange("OutsidePermittedRange", java.util.Optional.empty()),
    InvalidPattern("InvalidPattern", java.util.Optional.empty()),
    InvalidConstant("InvalidConstant", java.util.Optional.empty()),
    Duplicate("Duplicate", java.util.Optional.empty()),
    InvalidTypeForField("InvalidTypeForField", java.util.Optional.empty()),
    UnknownField("UnknownField", java.util.Optional.empty()),
    InvalidDecimalScale("InvalidDecimalScale", java.util.Optional.empty()),
    InvalidDecimalPrecision("InvalidDecimalPrecision", java.util.Optional.empty()),
    DataFormatError("DataFormatError", java.util.Optional.empty()),
    UserDefinedAssert("UserDefinedAssert", java.util.Optional.empty()),
    Unknown("Unknown", java.util.Optional.empty());

    private final java.lang.String value;
    private final java.util.Optional<java.lang.String> lexical;

    private static java.util.Map<java.lang.String, ConstraintType> mappings;

    ConstraintType(java.lang.String v, java.util.Optional<java.lang.String> lex) {
        value = v;
        lexical = lex;
    }

    public static ConstraintType fromValue(java.lang.String v) {
        if (mappings == null) {
            java.util.Map<java.lang.String, ConstraintType> m = new java.util.HashMap<>();
            for (ConstraintType c : ConstraintType.values()) {
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
        return schemarise.alfa.runtime.model.asserts.ConstraintType.ConstraintTypeDescriptor
                .INSTANCE;
    }

    // <editor-fold defaultstate="collapsed" desc="TypeDescriptor class">

    public static final class ConstraintTypeDescriptor
            extends com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor {
        public static java.lang.String TYPE_NAME =
                "schemarise.alfa.runtime.model.asserts.ConstraintType";
        public static schemarise.alfa.runtime.model.asserts.ConstraintType.ConstraintTypeDescriptor
                INSTANCE =
                        new schemarise.alfa.runtime.model.asserts.ConstraintType
                                .ConstraintTypeDescriptor();

        private schemarise.alfa.runtime.model.UdtDataType _asUdtType_ =
                schemarise.alfa.runtime.model.UdtDataType.builder()
                        .setUdtType(schemarise.alfa.runtime.model.UdtMetaType.enumType)
                        .setFullyQualifiedName(TYPE_NAME)
                        .build();
        public static final short FIELD_ID_MANDATORYFIELDNOTSET = 0;
        public static final String FIELD_MANDATORYFIELDNOTSET = "MandatoryFieldNotSet";

        public static final short FIELD_ID_OUTSIDEPERMITTEDRANGE = 1;
        public static final String FIELD_OUTSIDEPERMITTEDRANGE = "OutsidePermittedRange";

        public static final short FIELD_ID_INVALIDPATTERN = 2;
        public static final String FIELD_INVALIDPATTERN = "InvalidPattern";

        public static final short FIELD_ID_INVALIDCONSTANT = 3;
        public static final String FIELD_INVALIDCONSTANT = "InvalidConstant";

        public static final short FIELD_ID_DUPLICATE = 4;
        public static final String FIELD_DUPLICATE = "Duplicate";

        public static final short FIELD_ID_INVALIDTYPEFORFIELD = 5;
        public static final String FIELD_INVALIDTYPEFORFIELD = "InvalidTypeForField";

        public static final short FIELD_ID_UNKNOWNFIELD = 6;
        public static final String FIELD_UNKNOWNFIELD = "UnknownField";

        public static final short FIELD_ID_INVALIDDECIMALSCALE = 7;
        public static final String FIELD_INVALIDDECIMALSCALE = "InvalidDecimalScale";

        public static final short FIELD_ID_INVALIDDECIMALPRECISION = 8;
        public static final String FIELD_INVALIDDECIMALPRECISION = "InvalidDecimalPrecision";

        public static final short FIELD_ID_DATAFORMATERROR = 9;
        public static final String FIELD_DATAFORMATERROR = "DataFormatError";

        public static final short FIELD_ID_USERDEFINEDASSERT = 10;
        public static final String FIELD_USERDEFINEDASSERT = "UserDefinedAssert";

        public static final short FIELD_ID_UNKNOWN = 11;
        public static final String FIELD_UNKNOWN = "Unknown";

        private schemarise.alfa.runtime.model.ScalarDataType _mandatoryFieldNotSetType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _outsidePermittedRangeType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _invalidPatternType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _invalidConstantType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _duplicateType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _invalidTypeForFieldType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _unknownFieldType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _invalidDecimalScaleType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _invalidDecimalPrecisionType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _dataFormatErrorType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _userDefinedAssertType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        private schemarise.alfa.runtime.model.ScalarDataType _unknownType =
                schemarise.alfa.runtime.model.ScalarDataType.builder()
                        .setScalarType(schemarise.alfa.runtime.model.ScalarType.voidType)
                        .build();

        public java.util.Optional<
                        java.util.function.Function<ConstraintType, java.util.function.Supplier>>
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

        public com.schemarise.alfa.runtime.FieldMeta mandatoryFieldNotSetMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _mandatoryFieldNotSetType,
                        FIELD_MANDATORYFIELDNOTSET,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta outsidePermittedRangeMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _outsidePermittedRangeType,
                        FIELD_OUTSIDEPERMITTEDRANGE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta invalidPatternMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _invalidPatternType,
                        FIELD_INVALIDPATTERN,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta invalidConstantMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _invalidConstantType,
                        FIELD_INVALIDCONSTANT,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta duplicateMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _duplicateType,
                        FIELD_DUPLICATE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta invalidTypeForFieldMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _invalidTypeForFieldType,
                        FIELD_INVALIDTYPEFORFIELD,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta unknownFieldMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _unknownFieldType,
                        FIELD_UNKNOWNFIELD,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta invalidDecimalScaleMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _invalidDecimalScaleType,
                        FIELD_INVALIDDECIMALSCALE,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta invalidDecimalPrecisionMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _invalidDecimalPrecisionType,
                        FIELD_INVALIDDECIMALPRECISION,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta dataFormatErrorMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _dataFormatErrorType,
                        FIELD_DATAFORMATERROR,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta userDefinedAssertMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _userDefinedAssertType,
                        FIELD_USERDEFINEDASSERT,
                        java.util.Optional.empty());
        public com.schemarise.alfa.runtime.FieldMeta unknownMeta =
                new com.schemarise.alfa.runtime.FieldMeta(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        _unknownType,
                        FIELD_UNKNOWN,
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

        public ConstraintTypeDescriptor() {
            super.init();
        }

        protected java.util.Map<java.lang.String, schemarise.alfa.runtime.model.Assert> _asserts_ =
                java.util.Collections.unmodifiableMap(
                        new java.util.LinkedHashMap() {
                            {
                            }
                        });

        protected java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<ConstraintType>>
                _fieldsMeta_ =
                        java.util.Collections.unmodifiableMap(
                                new java.util.LinkedHashMap() {
                                    {
                                        put(FIELD_MANDATORYFIELDNOTSET, mandatoryFieldNotSetMeta);
                                        put(FIELD_OUTSIDEPERMITTEDRANGE, outsidePermittedRangeMeta);
                                        put(FIELD_INVALIDPATTERN, invalidPatternMeta);
                                        put(FIELD_INVALIDCONSTANT, invalidConstantMeta);
                                        put(FIELD_DUPLICATE, duplicateMeta);
                                        put(FIELD_INVALIDTYPEFORFIELD, invalidTypeForFieldMeta);
                                        put(FIELD_UNKNOWNFIELD, unknownFieldMeta);
                                        put(FIELD_INVALIDDECIMALSCALE, invalidDecimalScaleMeta);
                                        put(
                                                FIELD_INVALIDDECIMALPRECISION,
                                                invalidDecimalPrecisionMeta);
                                        put(FIELD_DATAFORMATERROR, dataFormatErrorMeta);
                                        put(FIELD_USERDEFINEDASSERT, userDefinedAssertMeta);
                                        put(FIELD_UNKNOWN, unknownMeta);
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
        public java.util.Map<java.lang.String, com.schemarise.alfa.runtime.FieldMeta<ConstraintType>>
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
                case FIELD_ID_MANDATORYFIELDNOTSET:
                    return FIELD_MANDATORYFIELDNOTSET;
                case FIELD_ID_OUTSIDEPERMITTEDRANGE:
                    return FIELD_OUTSIDEPERMITTEDRANGE;
                case FIELD_ID_INVALIDPATTERN:
                    return FIELD_INVALIDPATTERN;
                case FIELD_ID_INVALIDCONSTANT:
                    return FIELD_INVALIDCONSTANT;
                case FIELD_ID_DUPLICATE:
                    return FIELD_DUPLICATE;
                case FIELD_ID_INVALIDTYPEFORFIELD:
                    return FIELD_INVALIDTYPEFORFIELD;
                case FIELD_ID_UNKNOWNFIELD:
                    return FIELD_UNKNOWNFIELD;
                case FIELD_ID_INVALIDDECIMALSCALE:
                    return FIELD_INVALIDDECIMALSCALE;
                case FIELD_ID_INVALIDDECIMALPRECISION:
                    return FIELD_INVALIDDECIMALPRECISION;
                case FIELD_ID_DATAFORMATERROR:
                    return FIELD_DATAFORMATERROR;
                case FIELD_ID_USERDEFINEDASSERT:
                    return FIELD_USERDEFINEDASSERT;
                case FIELD_ID_UNKNOWN:
                    return FIELD_UNKNOWN;
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
            schemarise.alfa.runtime.model.asserts.ConstraintType{MandatoryFieldNotSet,OutsidePermittedRange,InvalidPattern,InvalidConstant,Duplicate,InvalidTypeForField,UnknownField,InvalidDecimalScale,InvalidDecimalPrecision,DataFormatError,UserDefinedAssert,Unknown}
            schemarise.alfa.runtime.model.asserts.ConstraintType{MandatoryFieldNotSet,OutsidePermittedRange,InvalidPattern,InvalidConstant,Duplicate,InvalidTypeForField,UnknownField,InvalidDecimalScale,InvalidDecimalPrecision,DataFormatError,UserDefinedAssert,Unknown}
            */
            return "bedbd588:";
        }
    }
    // </editor-fold>
}