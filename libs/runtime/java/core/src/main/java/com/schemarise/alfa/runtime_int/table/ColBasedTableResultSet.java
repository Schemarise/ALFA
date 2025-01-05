package com.schemarise.alfa.runtime_int.table;

import schemarise.alfa.runtime.model.ColBasedTable;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;


class ColBasedTableResultSet extends BaseResultSet {
    private final ColBasedTable _data;
    private int currentRow = -1;

    public ColBasedTableResultSet(ColBasedTable cd) {
        super(cd.getDef());
        _data = cd;
    }

    @Override
    public int getRow() throws SQLException {
        return currentRow;
    }

    @Override
    public boolean next() throws SQLException {
        currentRow++;
        return currentRow < _data.getRowCount();
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public String getString(int sqlColumnIndex) throws SQLException {
        Optional<String> v = _data.getColData().get(sqlColumnIndex - 1).getStrings().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return null;
    }

    @Override
    public boolean getBoolean(int sqlColumnIndex) throws SQLException {
        Optional<Boolean> v = _data.getColData().get(sqlColumnIndex - 1).getBooleans().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return false;
    }

    @Override
    public short getShort(int sqlColumnIndex) throws SQLException {
        Optional<Short> v = _data.getColData().get(sqlColumnIndex - 1).getShorts().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return 0;
    }

    @Override
    public int getInt(int sqlColumnIndex) throws SQLException {
        Optional<Integer> v = _data.getColData().get(sqlColumnIndex - 1).getInts().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return 0;
    }

    @Override
    public long getLong(int sqlColumnIndex) throws SQLException {
        Optional<Long> v = _data.getColData().get(sqlColumnIndex - 1).getLongs().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return 0L;
    }

    @Override
    public double getDouble(int sqlColumnIndex) throws SQLException {
        Optional<Double> v = _data.getColData().get(sqlColumnIndex - 1).getDoubles().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return Double.NaN;
    }

    @Override
    public BigDecimal getBigDecimal(int sqlColumnIndex, int scale) throws SQLException {
        Optional<BigDecimal> v = _data.getColData().get(sqlColumnIndex - 1).getDecimals().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return null;
    }

    @Override
    public byte[] getBytes(int sqlColumnIndex) throws SQLException {
        Optional<byte[]> v = _data.getColData().get(sqlColumnIndex - 1).getBinaries().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return null;
    }

    @Override
    public Date getDate(int sqlColumnIndex) throws SQLException {
        Optional<LocalDate> v = _data.getColData().get(sqlColumnIndex - 1).getDates().get(currentRow);
        if (v.isPresent())
            return Date.valueOf(v.get());
        else
            return null;
    }

    @Override
    public Time getTime(int sqlColumnIndex) throws SQLException {
        Optional<LocalTime> v = _data.getColData().get(sqlColumnIndex - 1).getTimes().get(currentRow);
        if (v.isPresent())
            return Time.valueOf(v.get());
        else
            return null;
    }

    @Override
    public Timestamp getTimestamp(int sqlColumnIndex) throws SQLException {
        Optional<LocalDateTime> v = _data.getColData().get(sqlColumnIndex - 1).getDatetimes().get(currentRow);
        if (v.isPresent())
            return Timestamp.valueOf(v.get());
        else
            return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return getString(findColumn(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return getBoolean(findColumn(columnLabel));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return getByte(findColumn(columnLabel));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return getShort(findColumn(columnLabel));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return getInt(findColumn(columnLabel));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getLong(findColumn(columnLabel));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return getDouble(findColumn(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return getBigDecimal(findColumn(columnLabel), scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return getBytes(findColumn(columnLabel));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return getDate(findColumn(columnLabel));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return getTime(findColumn(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return getTimestamp(findColumn(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(int sqlColumnIndex) throws SQLException {
        Optional<BigDecimal> v = _data.getColData().get(sqlColumnIndex - 1).getDecimals().get(currentRow);
        if (v.isPresent())
            return v.get();
        else
            return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return getBigDecimal(findColumn(columnLabel));
    }

    @Override
    public String toString() {
        return _data.toString();
    }
}
