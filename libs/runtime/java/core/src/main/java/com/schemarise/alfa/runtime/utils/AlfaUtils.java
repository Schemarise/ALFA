package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import schemarise.alfa.runtime.model.*;
import schemarise.alfa.runtime.model.annotation.db.StorageMode;
import schemarise.alfa.runtime.model.annotation.db.TableDef;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Alfa utility methods for constructing various Alfa based objects
 */
public class AlfaUtils {

    private AlfaUtils() {
    }

    /**
     * Give an estimate of the space consumed to keep this object in memory
     *
     * @param alfaObject Object to estimate space for
     * @return Value in bytes indicating space consumed
     */
    public static long estimateSize(AlfaObject alfaObject) {
        SizeEstimator se = new SizeEstimator(alfaObject);
        return se.getEstimatedSize();
    }

    public static void applyStreamingAsserts(String typeName, IBuilderConfig bc, Stream<AlfaObject> stream) {
        ClassUtils.getMeta(typeName).getNewBuilder(bc).applyStreamingAsserts(stream, bc.getExcludeAsserts());
    }

    public static void notNull(String name, Object o) {
        if (o == null)
            throw new AlfaRuntimeException("Value " + name + " is null");
    }

    /**
     * Give an estimate of the space consumed to keep this object in memory
     *
     * @param strings    Cache of strings
     * @param alfaObject Object to estimate space for
     * @return Value in bytes indicating space consumed
     */
    public static long estimateSize(Set<String> strings, AlfaObject alfaObject) {
        SizeEstimator se = new SizeEstimator(strings, alfaObject);
        return se.getEstimatedSize();
    }

    /**
     * Return a Builder for the object, i.e. a mutable version of the immutable object
     *
     * @param cfg        Builder configuration to be used to construct the object
     * @param alfaObject Immutable object that will be used to craete the newBuilder
     * @return A Builder for the object
     */
    public static <T extends Builder> T toBuilder(IBuilderConfig cfg, AlfaObject alfaObject) {
        ToBuilderCreator dc = new ToBuilderCreator(cfg, alfaObject);
        return (T) dc.newBuilder();
    }

    /**
     * Utility method to create a try failure object with the given message
     *
     * @param msg Message to be used for the failure reason
     * @param <T> Class representing the type expectedd in the Try value
     * @return A Try object with a failure value set
     */
    public static <T> Try<T> createTryFailure(String msg) {
        TryFailure tf = TryFailure.builder().setMessage(msg).build();
        return Try.builder().setFailure(tf).build();
    }

    /**
     * Utility method to create a try object with the given object as the valid Try result
     *
     * @param value Value to be used as the try result
     * @param <T>   Class representing the value
     * @return A Try object with a result value set
     */
    public static <T> Try<T> createTryValue(T value) {
        return Try.builder().setResult(value).build();
    }


    /**
     * Utility method to create am either left object with the given object as the left value
     *
     * @param value Value to be used as the either left result
     * @param <L>   Class representing the value
     * @return An Either object with a left value set
     */
    public static <L, R> Either<L, R> createEitherLeft(L value) {
        return Either.builder().setLeft(value).build();
    }

    /**
     * Utility method to create am either left object with the given object as the right value
     *
     * @param value Value to be used as the either right result
     * @param <L>   Class representing the value
     * @return An Either object with a right value set
     */
    public static <L, R> Either<L, R> createEitherRight(R value) {
        return Either.builder().setRight(value).build();
    }

    private static Function<Map<String, Void>, List<ValidationAlert>> assertAllResultBuilder
            = s -> {
        // TODO DO WE NEED TO LOOP?
        s.forEach((k, v) -> {
        });
        return null;
    };

    public static <T, Void> Function<T, Void> buildAssertAllResult() {
        return (Function<T, Void>) assertAllResultBuilder;
    }

    public <T extends com.schemarise.alfa.runtime.AlfaObject> java.util.stream.Stream<java.lang.String> validate(
            com.schemarise.alfa.runtime.IBuilderConfig __builderConfig,
            java.util.stream.Stream<T> _rows) {
        java.util.stream.Stream<java.lang.String> results =
                _rows.map(
                                e -> {
                                    try {
                                        e.validate(__builderConfig);
                                        return java.util.Optional.of((java.lang.String) null);
                                    } catch (AlfaRuntimeException ve) {
                                        java.util.Optional<java.lang.String> res =
                                                java.util.Optional.of(ve.getMessage());
                                        return res;
                                    }
                                })
                        .filter(e -> e.isPresent())
                        .map(e -> e.get());

        return results;
    }

    public static Optional<TableDef> getTableAnnotation(AlfaObject ao) {
        return getTableAnnotation(ao.descriptor());
    }

    public static Optional<TableDef> getTableAnnotation(TypeDescriptor descriptor) {
        if (!descriptor.getAnnotations().isPresent() || descriptor.getAnnotations().get().get("alfa.db.Table") == null)
            return Optional.empty();

        Map<String, IExpression> fields = descriptor.getAnnotations().get().get("alfa.db.Table").getObjectExpr().getFieldValues();

        TableDef.TableDefBuilder b = TableDef.builder();

        if (fields.containsKey("Name")) {
            String d = (String) exprCaseValue(fields.get("Name"));
            b.setName(d.length() == 0 ? Optional.empty() : Optional.of(d));
        }

        if (fields.containsKey("PartitionExpression"))
            b.setPartitionExpression((String) exprCaseValue(fields.get("PartitionExpression")));

        if (fields.containsKey("PayloadColumnName"))
            b.setPayloadColumnName((String) exprCaseValue(fields.get("PayloadColumnName")));

        if (fields.containsKey("Schema")) {
            String d = (String) exprCaseValue(fields.get("Schema"));
            b.setSchema(d.length() == 0 ? Optional.empty() : Optional.of(d));
        }

        if (fields.containsKey("StorageMode")) {
            String v = (String) exprCaseValue(fields.get("StorageMode"));
            String[] s = v.split("\\.");
            b.setStorageMode(StorageMode.valueOf(s[s.length - 1]));
        }

        if (fields.containsKey("Queryable")) {
            b.addAllQueryable((Set<String>) exprCaseValue(fields.get("Queryable")));
        }

        if (fields.containsKey("ClusterFields")) {
            b.addAllClusterFields((Set<String>) exprCaseValue(fields.get("ClusterFields")));
        }

        if (fields.containsKey("PartitionFields")) {
            b.addAllPartitionFields((Set<String>) exprCaseValue(fields.get("PartitionFields")));
        }

        if (fields.containsKey("AllOptions")) {
            b.addAllOptions((Set<String>) exprCaseValue(fields.get("AllOptions")));
        }

        return Optional.of(b.build());
    }

    private static Object exprCaseValue(IExpression e) {
        if (e instanceof Expression.CaseLiteralExpr) {
            Expression__LiteralExpr l = ((Expression.CaseLiteralExpr) e).getLiteralExpr();
            if (l.getExprType() == ScalarType.stringType) {
                return l.getValue().substring(1, l.getValue().length() - 1);
            }
            return l.getValue();
        } else if (e instanceof Expression.CaseQualifiedIdentifierExpr) {
            Expression__QualifiedIdentifierExpr l = ((Expression.CaseQualifiedIdentifierExpr) e).getQualifiedIdentifierExpr();
            return String.join(".", l.getPath().toArray(new String[]{}));
        } else if (e instanceof Expression.CaseIdentifierExpr) {
            Expression__IdentifierExpr l = ((Expression.CaseIdentifierExpr) e).getIdentifierExpr();
            return l.getFieldName();
        } else if (e instanceof Expression.CaseSetExpr) {
            Expression__SetExpr l = ((Expression.CaseSetExpr) e).getSetExpr();
            Set coll = new LinkedHashSet();

            l.getExpr().stream().forEach(se -> {
                coll.add(exprCaseValue(se));
            });

            return coll;
        }

        throw new AlfaRuntimeException("Unhandled annotation expression translation " + e);
    }

}
