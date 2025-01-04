package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Expression;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NoOpRuntimeContext implements RuntimeContext {

    private final IBuiltinFunctions builtins = IntImpl.createBuiltinFunctions(this);
    private ILogger logger = Logger.getOrCreateDefault();

    @Override
    public byte[] encrypt(byte[] inputData) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] inputData) {
        return new byte[0];
    }

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public byte[] compress(byte[] inputData) {
        return new byte[0];
    }

    @Override
    public byte[] uncompress(byte[] inputData) {
        return new byte[0];
    }

    @Override
    public <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName, Expression.CaseLambdaExpr condition, Map<String, Integer> sort, int limit, Optional<String> storeName) {
        return null;
    }

    @Override
    public <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok, Optional<String> storeName) {
        return Optional.empty();
    }

    @Override
    public <T extends Entity> void save(T entity, Optional<String> storeName) {
    }

    @Override
    public <T extends AlfaObject> void publish(String queueName, T alfaObj) {
    }

    @Override
    public <T extends AlfaObject> Boolean keyExists(String entityName, Key ok, Optional<String> storeName) {
        return null;
    }

    @Override
    public <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition, Optional<String> storeName) {
        return false;
    }

    @Override
    public IBuiltinFunctions getBuiltinFunctions() {
        return builtins;
    }
}
