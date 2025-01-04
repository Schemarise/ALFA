package com.schemarise.alfa.runtime_int.table;

import com.schemarise.alfa.runtime.AlfaObject;
import com.schemarise.alfa.runtime.TypeDescriptor;
import com.schemarise.alfa.runtime.codec.CodecConfig;
import com.schemarise.alfa.runtime.codec.IMapBasedRecord;
import com.schemarise.alfa.runtime.codec.MapBasedDataSupplier;
import com.schemarise.alfa.runtime.utils.ClassUtils;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class TableCodecImpl implements IntImpl.TableCodecIfc {

    private static final TableCodecImpl self = new TableCodecImpl();

    public static TableCodecImpl getInstance() {
        return self;
    }

    @Override
    public Table toTable(AlfaObject alfaObject) {
        AlfaFlattener f = new AlfaFlattener(alfaObject);
        return f.flatten();
    }

    @Override
    public Table toTable(Collection<? extends AlfaObject> alfaObjects) {

        List<AlfaObject> l = new ArrayList<>();
        alfaObjects.forEach(e -> l.add(e));
        AlfaFlattener f = new AlfaFlattener(l);
        return f.flatten();
    }

    public <T extends AlfaObject> T importRowBasedObject(CodecConfig bc,
                                                         String expectedType,
                                                         Optional<List<String>> optDataColumnNames,
                                                         List<Object> rowBasedData,
                                                         String sourceLineInfo,
                                                         Map<String, Function<Object, Object>> preProcessors) {

        ClassUtils.ClassMeta cm = ClassUtils.getMeta(expectedType);

        List<String> dataColumnNames;

        if (optDataColumnNames.isPresent())
            dataColumnNames = optDataColumnNames.get();
        else {
            TypeDescriptor desc = cm.getModel();
            dataColumnNames = desc.getAllFieldNames();
        }

        Map<String, Object> row = new HashMap<>();

        int total = Math.min(dataColumnNames.size(), rowBasedData.size());
        for (int i = 0; i < total; i++) {
            row.put(dataColumnNames.get(i), rowBasedData.get(i));
        }

//        IntStream.range(0, rowBasedData.size()).forEach(i -> ));

        bc.getAssertListener().setCurrentSourceInfo(sourceLineInfo);
        bc.getAssertListener().setCurrentTypeName(expectedType);

        AlfaObject mbs = _getAlfaObject(bc, expectedType, preProcessors, cm, row);
        return (T) mbs;
    }

    private AlfaObject _getAlfaObject(CodecConfig bc, String expectedType, Map<String, Function<Object, Object>> preProcessors, ClassUtils.ClassMeta cm, Map<String, Object> row) {
        return new MapBasedDataSupplier(bc, new RowBasedData(expectedType, preProcessors, row)).objectValue(Optional.of(cm.getTypeClass()));
    }

    public <T extends AlfaObject> Stream<T> importRowBasedObjects(CodecConfig bc,
                                                                  String expectedType,
                                                                  Optional<List<String>> optDataColumnNames,
                                                                  Stream<List<Object>> rowBasedData,
                                                                  Map<String, Function<Object, Object>> preProcessors) {

        List<String> dataColumnNames;

        if (optDataColumnNames.isPresent())
            dataColumnNames = optDataColumnNames.get();
        else {
            TypeDescriptor desc = ClassUtils.getMeta(expectedType).getModel();
            dataColumnNames = desc.getAllFieldNames();
        }

        Stream<T> str = rowBasedData.parallel().map(e -> {
            return importRowBasedObject(bc, expectedType, Optional.of(dataColumnNames), e, "<stream>", preProcessors);
        });

        return str;
    }


    static class RowBasedData implements IMapBasedRecord {

        private final Map<String, Object> data;
        private final String typeName;
        private final Map<String, Function<Object, Object>> preProcessors;

        public RowBasedData(String typeName, Map<String, Function<Object, Object>> preProcessors, Map<String, Object> dat) {
            this.typeName = typeName;
            this.data = dat;
            this.preProcessors = preProcessors;
        }

        @Override
        public String getFullName() {
            return typeName;
        }

        @Override
        public Set<String> getFields() {
            return data.keySet();
        }

        @Override
        public Object get(String fieldName) {
            Object res = data.get(fieldName);

            if (preProcessors.containsKey(fieldName)) {
                Function<Object, Object> e = preProcessors.get(fieldName);

                return e.apply(res);
            }
            return res;
        }
    }
}
