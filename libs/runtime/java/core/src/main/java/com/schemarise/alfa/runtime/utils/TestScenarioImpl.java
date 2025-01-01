package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.Either;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.asserts.ValidationAlert;
import schemarise.alfa.test.Scenario;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestScenarioImpl implements Scenario {
    @Override
    public boolean failsOn(String _description, Supplier _testBody, String _expectedErrorFieldPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean failsWith(String _description, Supplier _testBody, String _expectedErrorMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlfaObject loadObjectFromCsv(String _typeName, String _pathOrUrl) {
        try {
            return Alfa.jsonCodec().importObj(new FileInputStream(_pathOrUrl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AlfaObject> loadObjectsFromCsv(String _typeName, String _pathOrUrl, int _headerLineNo, String _colDelimiter) {
        try {
            Stream<Either<AlfaObject, ValidationAlert.ValidationAlertBuilder>> stm =
                    Alfa.jsonCodec().importObjects(new FileInputStream(_pathOrUrl));

            return stm.map(e -> e.getLeft()).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void given(String _description, Entity _data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void withServiceResults(String _description, Service _srv, Map<String, List<String>> _results) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fails(String _description, Supplier _testBody) {
        try {
            _testBody.get();
            System.err.println("Expected to fail:" + _description);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public AlfaObject copyWith(AlfaObject _toCopy, AlfaObject _toOverride) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> listFiles(String _pathOrUrl, String _ext) {
        try {
            return _listFiles(_pathOrUrl, _ext);
        } catch (IOException e) {
            throw new RuntimeException("Failed in listFiles " + _pathOrUrl + ", " + _ext, e);
        }
    }

    @Override
    public void assertTrue(String _description, Supplier<Boolean> _testBody) {
        try {
            Boolean res = _testBody.get();
            if (!res)
                throw new AlfaRuntimeException("Expected result to be True:" + _description);
        } catch (Exception e) {
            throw new AlfaRuntimeException(ConstraintType.Unknown, "Expected to succeed:" + _description, e);
        }
    }

    @Override
    public boolean succeeds(String _description, Supplier _testBody) {
        try {
            _testBody.get();
            return true;
        } catch (Exception e) {
            System.err.println("Expected to succeed:" + _description);
            return false;
        }
    }

    @Override
    public AlfaObject random(String _typeName) {
        return randomWith(_typeName, Collections.emptyMap());
    }

    @Override
    public AlfaObject loadObjectFromJSON(String _typeName, String _pathOrUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlfaObject randomWith(AlfaObject _builderObject) {
        String tn = _builderObject.descriptor().getUdtDataType().getFullyQualifiedName();

        Map<String, Object> values = new HashMap();
        _builderObject.descriptor().getAllFieldsMeta().keySet().stream().forEach(fn -> {
            Object v = _builderObject.get(fn);
            if (v != null)
                values.put(fn, v);
        });

        return randomWith(tn, values);
    }

    private List<String> _listFiles(String _pathOrUrl, String _ext) throws IOException {
        Path p = Paths.get(_pathOrUrl);
        if (!Files.exists(p)) {
            String cp = Paths.get(".").toFile().getCanonicalPath();
            throw new AlfaRuntimeException("Path does not exist " + _pathOrUrl + ". Current dir " + cp);
        }

        Pattern m = Pattern.compile(_ext);

        List<String> paths = Files.list(p)
                .filter(x -> m.matcher(x.getFileName().toString()).find())
                .map(x -> x.toString())
                .collect(Collectors.toList());
        return paths;
    }

    private <T extends AlfaObject> T randomWith(String _typeName, Map<String, Object> values) {
        AlfaRandomizer r = new AlfaRandomizer();
        return r.randomWithValues(_typeName, values);
    }
}
