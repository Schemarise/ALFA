package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.Pair;
import schemarise.alfa.runtime.model.Try;
import schemarise.alfa.runtime.model.asserts.*;
import com.schemarise.alfa.runtime.codec.Converters;
import schemarise.alfa.runtime.model.UdtDataType;
import schemarise.alfa.runtime.model.UdtVersionedName;
import schemarise.alfa.runtime.model.path.Path;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static schemarise.alfa.runtime.model.asserts.ConstraintType.*;

public class Utils {

    private static Map<String, Pattern> _patterns = new HashMap<>();

    public static boolean or(boolean l, boolean r) {
        return l || r;
    }

    public static DataQualityType constraintTypeToDqType(ConstraintType ct) {

        switch (ct) {
            case MandatoryFieldNotSet:
                return DataQualityType.Completeness;

            case OutsidePermittedRange:
                return DataQualityType.Accuracy;

            case DataFormatError:
                return DataQualityType.Conformity;

            case Unknown:
                return DataQualityType.Unclassified;

            case UnknownField:
                return DataQualityType.Conformity;

            case InvalidConstant:
                return DataQualityType.Conformity;

            case UserDefinedAssert:
                return DataQualityType.Unclassified;

            case InvalidTypeForField:
                return DataQualityType.Conformity;

            case Duplicate:
                return DataQualityType.Uniqueness;

            case InvalidPattern:
                return DataQualityType.Conformity;

            case InvalidDecimalScale:
                return DataQualityType.Accuracy;

            case InvalidDecimalPrecision:
                return DataQualityType.Accuracy;

            default:
                return DataQualityType.Unclassified;
        }
    }

    public static void validateCollectionSize(IValidationListener listener, Supplier<PathCreator> path, Collection<?> l, Integer min, Integer max) {
        validate(listener, path, l.size(), min, max);
    }

    public static void validateCollectionSize(IValidationListener listener, Supplier<PathCreator> path, Map<?, ?> l, Integer min, Integer max) {
        validate(listener, path, l.size(), min, max);
    }

    private static String reportablePathToFieldName(Path p) {
        return p.getField();
    }

    private static String reportablePathIdentification(Path p) {
        String val = "";

        if (p.getElement().isScalarValue())
            val = p.getElement().getScalarValue();

        return val;
    }


    public static void validateDecimalScaleAndPrecision(IValidationListener listener, Supplier<PathCreator> path, BigDecimal value, int precision, int scale) {
        if (value.scale() > scale)
            listener.addFailure(ValidationAlert.builder().
                    setSeverity(SeverityType.Error).
                    setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                    setViolatedConstraint(Optional.of(ConstraintType.InvalidDecimalScale)).
                    setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                    setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                    setMessage("Invalid value " + value + " that exceeds the decimal scale '" + scale + "'")
            );

        if (value.precision() > precision)
            listener.addFailure(ValidationAlert.builder().
                    setSeverity(SeverityType.Error).
                    setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                    setViolatedConstraint(Optional.of(ConstraintType.InvalidDecimalPrecision)).
                    setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                    setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                    setMessage("Invalid value " + value + " that exceeds the decimal precision '" + precision + "'")
            );
    }

    public static <T extends Comparable> void validateScalarRange(IValidationListener listener, Supplier<PathCreator> path, T value, T min, T max) {
        validate(listener, path, value, min, max);
    }

    public static <T extends Comparable> void validateScalarRange(IValidationListener listener, Supplier<PathCreator> path, Optional<T> value, T min, T max) {
        if (value.isPresent())
            validate(listener, path, value.get(), min, max);
    }

//    public static void validateScalarString(Supplier<PathCreator> path, String value, Integer min, Integer max ) {
//        validate(path, value.length(), min, max );
//    }


//    public static URI newURI(String v) {
//        try {
//            return new URI(v);
//        } catch (URISyntaxException e) {
//            throw new AlfaRuntimeException(e);
//        }
//    }

//    public static void validateUri(Supplier<PathCreator> path, URI value, String[] uriProtocols ) {
//        boolean matched = false;
//
//        for (int i = 0; i <uriProtocols.length; i++) {
//            if (value.getScheme().equals(uriProtocols[i])) {
//                matched = true;
//                break;
//            }
//        }
//
//        if ( ! matched && uriProtocols.length > 0 )
//            throw new AlfaConstraintError(path.get()  + " expected URI scheme '" + Arrays.asList(uriProtocols) + "' in value '" + value.toString() + "' protocol '" + value.getScheme() + "'" );
//    }

    public static Pattern getOrCreatePattern(String pattern) {
        Pattern pat = _patterns.get(pattern);
        if (pat == null) {
            pat = Pattern.compile(pattern);
            _patterns.put(pattern, pat);
        }

        return pat;
    }

    public static void validateWithPattern(IValidationListener listener, Supplier<PathCreator> path, String value, String origPattern) {

        String pattern = origPattern.replace("\\\\", "\\");

        Pattern pat = _patterns.get(pattern);

        if (pat == null) {
            try {
                pat = Pattern.compile(pattern);
                _patterns.put(pattern, pat);
            } catch (Exception e) {
                listener.addFailure(ValidationAlert.builder().
                        setSeverity(SeverityType.Error).
                        setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                        setViolatedConstraint(Optional.of(ConstraintType.InvalidPattern)).
                        setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                        setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                        setMessage("Invalid pattern '" + origPattern + "'")
                );
            }
        }

        if (!pat.matcher(value).matches())
            listener.addFailure(ValidationAlert.builder().
                    setSeverity(SeverityType.Error).
                    setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                    setViolatedConstraint(Optional.of(ConstraintType.InvalidPattern)).
                    setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                    setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                    setMessage("Value '" + value + "' does not conform to regex pattern '" + origPattern + "'")
            );
    }

    public static void validateScalarRange(IValidationListener listener, Supplier<PathCreator> path, byte[] value, int min, int max) {
        validate(listener, path, value.length, min, max);
    }

//    private static <T extends Comparable> void validate(Supplier<PathCreator> fieldPath, T sz, Optional<T> min, Optional<T> max) {
//        if ( min != null && sz.compareTo(min.get()) < 0 ) {
//            throw new AlfaConstraintError( "Validation failed on " + fieldPath + ". Minimum size " + min.get() + ", result size " + sz );
//        }
//        if ( max != null && sz.compareTo(max) > 0 ) {
//            throw new AlfaConstraintError( "Validation failed on " + fieldPath + ". Maximum size " + max + ", result size " + sz );
//        }
//    }

    private static <T extends Comparable> void validate(IValidationListener listener, Supplier<PathCreator> path, T sz, T min, T max) {

        if ((min != null && sz.getClass() != min.getClass()) || (max != null && sz.getClass() != max.getClass())) {
            validate(listener, path, ((Number) sz).doubleValue(), min == null ? min : ((Number) min).doubleValue(), max == null ? max : ((Number) max).doubleValue());
            return;
        }

        if (min != null && sz.compareTo(min) < 0) {
            listener.addFailure(ValidationAlert.builder().
                    setSeverity(SeverityType.Error).
                    setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                    setViolatedConstraint(Optional.of(ConstraintType.OutsidePermittedRange)).
                    setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                    setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                    setMessage("Minimum " + min + ", result " + sz)
            );
        }

        if (max != null && sz.compareTo(max) > 0) {
            listener.addFailure(ValidationAlert.builder().
                    setSeverity(SeverityType.Error).
                    setDataQualityCategory(Optional.of(DataQualityType.Accuracy)).
                    setViolatedConstraint(Optional.of(ConstraintType.OutsidePermittedRange)).
                    setFieldName(Optional.of(reportablePathToFieldName(path.get().toReportablePath()))).
                    setIdentification(Optional.of(reportablePathIdentification(path.get().toReportablePath()))).
                    setMessage("Maximum " + max + ", result " + sz)
            );
        }
    }

    public static String udtToString(String udtName, String[] fields, Object[] objs) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n\t\"type\":\"" + udtName + "\",\n");
        for (int i = 0; i < fields.length; i++) {
            String val = fmtString(objs[i]);
            if (val != null) {
                sb.append("\t\"" + fields[i] + "\":" + val);
                if (i + i < fields.length)
                    sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("}");

        return sb.toString();
    }

    public static String fmtString(Object e) {
        if (e == null) {
            return "null";
        } else if (e instanceof Number || e instanceof AlfaObject) {
            return e.toString();
        } else if (e instanceof Map) {
            return fmtMapString((Map) e);
        } else if (e instanceof List) {
            return fmtListString((List) e);
        } else if (e instanceof Set) {
            return fmtSetString((Set) e);
        } else if (e instanceof Optional) {
            Optional o = (Optional) e;
            if (o != null && o.isPresent())
                return fmtString(o.get());
            else
                return null;
        } else {
            String s = e.toString(); // TODO Escape tokens
            return "\"" + s + "\"";
        }
    }

    private static String fmtMapString(Map<Object, Object> m) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        List<String> l = m.entrySet().stream().map(e -> {
            StringBuilder i = new StringBuilder();
            i.append("[");
            i.append(fmtString(e.getKey()));
            i.append(", ");
            i.append(fmtString(e.getValue()));
            i.append("]");
            return i.toString();
        }).collect(Collectors.toList());

        sb.append(String.join(", ", l));

        sb.append("]");

        return sb.toString();
    }


    private static String fmtSetString(Set<?> m) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        List<String> l = m.stream().map(e -> fmtString(e)).collect(Collectors.toList());
        sb.append(String.join(", ", l));
        sb.append("]");

        return sb.toString();
    }

    private static String fmtListString(List<?> m) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        List<String> l = m.stream().map(e -> fmtString(e)).collect(Collectors.toList());
        sb.append(String.join(", ", l));
        sb.append("]");

        return sb.toString();
    }

    public static boolean assertMandatoryFieldsSet(IBuilderConfig cc, BitSet missingFields, int totalFields, TypeDescriptor desc) {
        IValidationListener l = cc.getAssertListener();

        if (missingFields.isEmpty())
            return false;

        int nextBit = 0;
        List names = new ArrayList();

        while (true) {
            nextBit = missingFields.nextSetBit(nextBit);
            if (nextBit >= 0) {
                String name = desc.fieldIdName(nextBit);
                names.add(name);
            } else if (nextBit == -1 || nextBit + 1 == totalFields) {
                break;
            }

            nextBit++;
        }

        if (names.size() > 0 && !cc.assertMandatoryFieldsSet()) {
            l.addFailure(ValidationAlert.builder().
                    setViolatedConstraint(Optional.of(MandatoryFieldNotSet)).
                    setMessage("Mandatory fields " + names + " not set").
                    setTypeName(Optional.of(desc.getUdtDataType().getFullyQualifiedName())).
                    setDataQualityCategory(Optional.of(DataQualityType.Completeness)));

            return true;
        } else {
            long c = cc.getAssertListener().getErrorCount();
            String msg = "";
            if (c > 0) {
                msg = ". Found " + c + " errors. 1st error : " +
                        cc.getAssertListener().getValidationReport().getAlerts().get(0).getMessage();
            }

            throw new AlfaRuntimeException(MandatoryFieldNotSet, "Mandatory fields " + names + " not set" + msg);
        }
    }

    public static void enforceNoFieldSet(BitSet missingFields, TypeDescriptor desc, int totalFields, short fieldId) {
        BitSet copy = (BitSet) missingFields.clone();
        copy.flip(0, totalFields);

        // the one field that is set can be the one that is being checked, that is ok
        if (!copy.isEmpty() && missingFields.nextClearBit(0) != fieldId) {
            String alreadySet = desc.fieldIdName(missingFields.nextClearBit(0));
            String attempted = desc.fieldIdName(fieldId);

            throw new com.schemarise.alfa.runtime.AlfaRuntimeException(MandatoryFieldNotSet,
                    "Field " + alreadySet + " already set. Cannot set " + attempted + " in type " + desc.getUdtDataType().getFullyQualifiedName());
        }
    }

    public static void assertNotNull(Object k) {
        if (k == null)
            throw new AlfaRuntimeException("Null object encountered");
    }

    public static int unionHashCode(Union u) {
        return java.util.Objects.hash(u.caseName(), u.caseValue());
    }

    public static java.lang.String unionToString(Union u) {
        return "{"
                + "\"@type\":\"" + u.descriptor().getUdtDataType().getFullyQualifiedName() + "\","
                + "\""
                + u.caseName()
                + "\":"
                + u.caseValue()
                + ","
                + "}";
    }

    public static boolean unionEquals(Union lhs, Object o) {
        if (lhs == o) return true;
        if (!(lhs.getClass().isAssignableFrom(o.getClass()))) return false;
        Union rhs = (Union) o;
        return rhs.caseName().equals(lhs.caseName()) && rhs.caseValue().equals(lhs.caseValue());
    }

    public static UdtVersionedName toUdtVersionedName(UdtDataType udtDataType) {
        return UdtVersionedName.builder().
                setFullyQualifiedName(udtDataType.getFullyQualifiedName()).
                setUdtType(udtDataType.getUdtType()).build();
    }

    public static <T> Encrypted<T> defaultEncryptedFromValue(Converters.SupplierConsumer<T> convertor, IBuilderConfig builderConfig, T unencodedObject) {
        return IntImpl.defaultEncryptedFromValue(convertor, builderConfig, unencodedObject);
    }

    public static <T> Compressed<T> defaultCompressedFromValue(Converters.SupplierConsumer<T> convertor, IBuilderConfig builderConfig, T unencodedObject) {
        return IntImpl.defaultCompressedFromValue(convertor, builderConfig, unencodedObject);
    }

    public static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> (Pair<A, B>) Pair.builder().setLeft(as.get(i)).setRight(bs.get(i)).build())
                .collect(Collectors.toList());
    }

    public static <T> List<T> asList(T... l) {
        List<T> res = new ArrayList<>();

        for (int i = 0; i < l.length; i++) {
            res.add(l[i]);
        }

        return res;
    }

    public static <T> Optional<T> tryFlatMap(Try<T> t) {
        if (t.isResult())
            return Optional.of(t.getResult());
        else
            return Optional.empty();
    }

//    public static void compactor( BuilderConfig bc, AlfaObject o ) {
//        new ListCompactor(o);
//    }
//
//    private static class ListCompactor extends NoOpDataConsumer {
//        public ListCompactor(AlfaObject so ) {
//            if ( ! so.getClass().getName().startsWith("alfa") ) {
//                so.descriptor().getAllFieldsMeta().forEach( ( fk, fv ) -> {
//                    BiConsumer<AlfaObject, DataConsumer> sup = fv.getSupplier().get();
//                    sup.accept(so, this );
//                });
//            }
//        }
//
//        @Override
//        public <T> void consume(ListDataType dt, List<T> v, BiConsumer<T, DataConsumer> elementConsumer) {
//            if ( v instanceof ArrayList )
//                ((ArrayList) v).trimToSize();
//        }
//    }

}
