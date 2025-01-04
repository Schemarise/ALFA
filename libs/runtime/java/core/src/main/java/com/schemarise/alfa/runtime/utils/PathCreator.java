package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig;
import com.schemarise.alfa.runtime.codec.json.JsonTypeWriteMode;
import schemarise.alfa.runtime.model.path.Path;
import schemarise.alfa.runtime.model.path.PathElement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class PathCreator {

    private Path.PathBuilder _path;
    private static Set<Class> primitives = new HashSet<>();

    static {
        primitives.add(String.class);
        primitives.add(Long.class);
        primitives.add(Integer.class);
        primitives.add(Boolean.class);
        primitives.add(Double.class);
        primitives.add(Float.class);
        primitives.add(Short.class);
        primitives.add(LocalDate.class);
        primitives.add(Duration.class);
        primitives.add(LocalDateTime.class);
        primitives.add(LocalTime.class);
    }

    public PathCreator(String field) {
        _path = Path.builder().setField(field);
    }

    public PathCreator mapKeyElement(Object k) {
        _path.setElement(PathElement.builder().addAllMapKey(objToPath(k)).build());
        return this;
    }

    private List<PathElement> objToPath(Object k) {
        List<PathElement> pel = new ArrayList<>();

        if (primitives.contains(k.getClass())) {
            pel.add(PathElement.builder().setScalarValue(String.valueOf(k)).build());
        } else {
            pel.add(PathElement.builder().setScalarValue("<unhandled path " +
                    k.getClass().getSimpleName() + ">").build());
        }

        return pel;
    }

    public PathCreator mapEntryElement(Map.Entry v) {
        // TODO build map
        return this;
    }

    public PathCreator listIdxElement(int idx) {
        // TODO build list
        return this;
    }

    public PathCreator setEntryElement(Object e0) {
        // TODO build object
        return this;
    }

    private static JsonCodecConfig jcc = JsonCodecConfig.builder().setWriteTypeMode(JsonTypeWriteMode.NeverWriteType).build();

    public Path toReportablePath() {
        Path p = _path.build();

        return p;
    }

    public String toString() {
        return _path.toString();
    }

    public PathCreator scalarElement(Object v) {
        _path.setElement(PathElement.builder().setScalarValue(String.valueOf(v)).build());
        return this;
    }
}
