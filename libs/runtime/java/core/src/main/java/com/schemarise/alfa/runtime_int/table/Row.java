package com.schemarise.alfa.runtime_int.table;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class Row {
    private final Map<String, Object> cols;

    Row(Row row) {
        cols = new LinkedHashMap<>(row.cols);
    }

    Row() {
        cols = new LinkedHashMap<>();
    }

    void setValue(String col, Object v) {
        Object old = cols.put(col, v);
        if (old != null && !old.equals(v))
            throw new IllegalStateException("Col:" + col + " old:" + old + " new:" + v);
    }

    @Override
    public String toString() {
        return stringify(cols.keySet());
    }

    public String stringify(Set<String> colNames) {
        StringBuffer sb = new StringBuffer();

        AtomicInteger ai = new AtomicInteger(0);
        int sz = colNames.size();

        colNames.stream().forEach(cn -> {
            Object o = cols.get(cn);
            if (o != null)
                sb.append(Table.fmtCol(o));
            else
                sb.append(Table.fmtCol(""));

            if (ai.incrementAndGet() < sz)
                sb.append("|");
        });

        return sb.toString();
    }

    Object getValue(String colName) {
        return cols.get(colName);
    }

    public Collection<Object> getValues() {
        return cols.values();
    }
}
