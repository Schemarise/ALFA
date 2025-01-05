package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Expression;
import schemarise.alfa.runtime.model.IExpression;
import schemarise.alfa.runtime.model.annotation.db.TableDef;
import com.schemarise.alfa.runtime.utils.AlfaUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersistenceSupport {
    /**
     * Query an underlying store for given entity type based on the condition specified.
     *
     * @param entityName Entity being queried
     * @param condition  This is lambda that accepts an object of the entity being queried and applies a filter - the SQL 'where clause'.
     * @param storeName
     * @return List of entities
     */
    <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName, Expression.CaseLambdaExpr condition, Map<String, Integer> sort, int limit, Optional<String> storeName);

    <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok, Optional<String> storeName);

    <T extends Entity> void save(T entity, Optional<String> storeName);

    <T extends AlfaObject> Boolean keyExists(String entityName, Key ok, Optional<String> storeName);

    <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition, Optional<String> storeName);


    default <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName, Expression.CaseLambdaExpr condition,
                                                 Map<String, Integer> sort, int limit) {
        return query(currentObject, entityName, condition, sort, limit, Optional.empty());
    }

    default <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName, Expression.CaseLambdaExpr condition,
                                                 Map<String, Integer> sort, int limit, String storeName) {
        return query(currentObject, entityName, condition, sort, limit, Optional.of(storeName));
    }

    default <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok, String storeName) {
        return lookup(entityName, ok, Optional.of(storeName));
    }

    default <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok) {
        return lookup(entityName, ok, Optional.empty());
    }

    default <T extends Entity> void save(T entity) {
        save(entity, Optional.empty());
    }

    default <T extends Entity> void save(T entity, String storeName) {
        save(entity, Optional.of(storeName));
    }

    default <T extends AlfaObject> Boolean keyExists(String entityName, Key ok, String storeName) {
        return keyExists(entityName, ok, Optional.of(storeName));
    }

    default <T extends AlfaObject> Boolean keyExists(String entityName, Key ok) {
        return keyExists(entityName, ok, Optional.empty());
    }

    default <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition) {
        return exists(entityName, condition, Optional.empty());
    }

    default <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition, String storeName) {
        return exists(entityName, condition, Optional.of(storeName));
    }

    default Optional<TableDef> getTableAnnotation(TypeDescriptor ao) {
        return AlfaUtils.getTableAnnotation(ao);
    }
}
