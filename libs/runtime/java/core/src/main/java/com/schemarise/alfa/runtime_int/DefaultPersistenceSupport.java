package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.Expression;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultPersistenceSupport implements PersistenceSupport {
    private Path _localDbDir;

    @Override
    public <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName,
                                                Expression.CaseLambdaExpr condition, Map<String, Integer> sort, int limit, Optional<String> storeName) {
        throw new AlfaRuntimeException("Query for " + entityName +
                " not implemented in default. Supply custom  com.schemarise.alfa.runtime.RuntimeContext implementation.");
    }

    @Override
    public <T extends AlfaObject> Boolean keyExists(String entityName, Key ok, Optional<String> storeName) {
        Optional<AlfaObject> l = lookup(entityName, ok, Optional.empty());
        return l.isPresent();
    }

    @Override
    public <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition, Optional<String> storeName) {
        throw new AlfaRuntimeException("Exists for " + entityName +
                " not implemented in default. Supply custom  com.schemarise.alfa.runtime.RuntimeContext implementation.");
    }

    @Override
    public <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok, Optional<String> storeName) {
        Path entityPath = entityPath(localDbDir(), entityName);

        String ks = keyToDir(ok);
        Path keyedFile = entityPath.resolve(ks + ".json");

        try {
            if (Files.exists(keyedFile)) {
                List<String> l = Files.lines(keyedFile).collect(Collectors.toList());
                T obj = Alfa.jsonCodec().fromJsonString(String.join("", l));
                return Optional.of(obj);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Entity> void save(T entity, Optional<String> storeName) {
        String name = entity.descriptor().getUdtDataType().getFullyQualifiedName();

        Optional<? extends Key> k = entity.get$key();

        try {
            String json = Alfa.jsonCodec().toFormattedJson(entity);

            if (k.isPresent()) {
                Path entityPath = entityPath(localDbDir(), name);

                String ks = keyToDir(k.get());
                Path keyedFile = entityPath.resolve(ks + ".json");

                Files.write(keyedFile, json.getBytes());
            } else {
                Path entityPath = localDbDir().resolve(name + ".json");
                Files.write(entityPath, json.getBytes());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path localDbDir() {
        if (_localDbDir == null) {
            try {
                _localDbDir = Files.createTempDirectory("alfa-rt-local-db-");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(_localDbDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        return _localDbDir;
    }

    private String keyToDir(Key k) {
        List<String> l = k.descriptor().getAllFieldNames().stream().map(fn -> k.get(fn).toString()).collect(Collectors.toList());
        return String.join("-", l);
    }

    private Path entityPath(Path p, String ename) {
        Path ep = p.resolve(ename);
        try {
            Files.createDirectories(ep);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ep;
    }
}
