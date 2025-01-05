package com.schemarise.alfa.runtime_int.table;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.*;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class Table implements ITable {
    private final UdtDataType srcUdtType;
    private int currentRow = -1;
    private List<Row> rows = new ArrayList<>();
    private LinkedHashMap<String, ScalarDataType> columns = new LinkedHashMap<>();
    private static int ColFormatWidth = 30;

    public Table(UdtDataType udtDataType) {
        srcUdtType = udtDataType;
    }

    public static Table merge(ITable... tables) {
        return merge(Arrays.asList(tables));
    }

    public static Table merge(List<ITable> tables) {
        Table res = new Table(((Table) tables.get(0)).srcUdtType);

        tables.forEach(t -> {
            res.columns.putAll(((Table) t).columns);
            res.rows.addAll(((Table) t).rows);
        });

        return res;
    }

    void insertRow() {
        Row r = new Row();
        rows.add(r);
        currentRow++;
    }

    void update(ScalarDataType dt, String colName, Object v) {
        Row row = rows.get(currentRow);
        row.setValue(colName, v);
        ScalarDataType oldDt = columns.put(colName, dt);
        if (oldDt != null && !oldDt.equals(dt))
            throw new IllegalStateException();
    }

    void cloneAndAdvanceRow(Row preVectorExpansionRow) {
        if (currentRow == -1)
            throw new IllegalStateException();

        Row newRow = new Row(preVectorExpansionRow);

        rows.add(newRow);

        currentRow++;
    }

    private void print(Object o) {
        System.out.print(o);
    }

    private void println(Object o) {
        System.out.println(o);
    }

    public String toCsvString() {
        StringBuffer sb = new StringBuffer();
        String cols = String.join(",", columns.keySet().stream().collect(Collectors.toList()));
        sb.append(cols + "\n");

        rows.stream().forEach(r -> {
            String row = String.join(",", r.getValues().stream().map(e -> e.toString()).collect(Collectors.toList()));
            sb.append(row + "\n");
        });

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        String cols = String.join("|", columns.keySet().stream().map(e -> fmtCol(e)).collect(Collectors.toList()));
        println(cols);

        String uline = new String(new char[ColFormatWidth]).replace('\0', '=');
        for (int i = 0; i < columns.size(); i++) {
            print(uline);
            if (i + 1 < columns.size())
                print("|");
        }

        println("");

        rows.forEach(r -> {
            println(r.stringify(columns.keySet()));
        });

        return sb.toString();
    }

    static String fmtCol(Object o) {
        String s = o.toString();
        if (s.length() > ColFormatWidth)
            s = "..." + s.substring(s.length() - ColFormatWidth + 3);

        return String.format("%1$-" + ColFormatWidth + "s", s);
    }

    @Override
    public Set<String> getColumnNames() {
        return columns.keySet();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    public Object getValue(int row, String colName) {
        Row r = rows.get(row);
        return r.getValue(colName);
    }

    @Override
    public Collection<Object> getValues(int row) {
        Row r = rows.get(row);
        return r.getValues();
    }

    Row getCurrentRowObjectCopy() {
        Row r = rows.get(this.currentRow);
        return new Row(r);
    }

    @Override
    public ScalarDataType getColumnType(String cname) {
        return columns.get(cname);
    }

    public ResultSet getResultSet() {
        ColBasedTable c = getColBasedTable();
        ColBasedTableResultSet crs = new ColBasedTableResultSet(c);
        return crs;
    }

    public ColBasedTable getColBasedTable() {
        ColBasedTable.ColBasedTableBuilder b = ColBasedTable.builder();
        b.setRowCount(getRowCount());

        TableDef.TableDefBuilder td = TableDef.builder();

        td.setUdtName(UdtVersionedName.builder().
                setUdtType(srcUdtType.getUdtType()).
                setFullyQualifiedName(srcUdtType.getFullyQualifiedName()).build());

        List<ColumnData> cd = new ArrayList<>();
        td.addAllColumns(new ArrayList<>());

        columns.forEach((cn, ct) -> {
            td.addColumns(ColumnDef.builder().setDataType(ct).setName(cn).build());

            List col = new ArrayList();
            rows.forEach(r -> {
                Object cellval = r.getValue(cn);
                cellval = cellval == UnionUntypedCase.getInstance() ? true : cellval;
                col.add(Optional.ofNullable(cellval));
            });

            ColumnData.ColumnDataBuilder colData = ColumnData.builder();
            colData.modify(colNameForType(ct), col);
            cd.add(colData.build());
        });
        b.setDef(td.build());

        b.addAllColData(cd);

        return b.build();
    }

    private String colNameForType(ScalarDataType ct) {
        switch (ct.getScalarType()) {
            case stringType:
                return "strings";
            case intType:
                return "ints";
            case longType:
                return "longs";
            case doubleType:
                return "doubles";
            case booleanType:
                return "booleans";
            case datetimeType:
                return "datetimes";
            case timeType:
                return "times";
            case dateType:
                return "dates";
            case uriType:
                return "uris";
            case charType:
                return "chars";
            case floatType:
                return "floats";
            case voidType:
                return "voids";
            case decimalType:
                return "decimals";
            case shortType:
                return "shorts";
            case byteType:
                return "bytes";
            case binaryType:
                return "binaries";
            case uuidType:
                return "uuids";
            case durationType:
                return "durations";
            default:
                throw new AlfaRuntimeException("Unhandled " + ct);
        }
    }
}
