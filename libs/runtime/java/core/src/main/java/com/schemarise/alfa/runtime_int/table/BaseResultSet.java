package com.schemarise.alfa.runtime_int.table;

import com.schemarise.alfa.runtime.AlfaRuntimeException;
import schemarise.alfa.runtime.model.ColumnDef;
import schemarise.alfa.runtime.model.TableDef;
import schemarise.alfa.runtime.model.ScalarDataType;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

abstract class BaseResultSet implements ResultSet {
    protected final Map<String, Integer> nameToColIndex = new HashMap<>();
    protected final Map<Integer, String> colIndexToName = new HashMap<>();
    protected final Map<Integer, ScalarDataType> colIndexToType = new HashMap<>();
    private final ResultSetMetaData rsmd;
    private final TableDef tableDef;


    public BaseResultSet(TableDef def) {
        tableDef = def;
        for (int i = 0; i < def.getColumns().size(); i++) {
            ColumnDef c = def.getColumns().get(i);
            addColumn(c);
            colIndexToType.put(i, c.getDataType());
        }
        rsmd = new BaseResultSetMetaData();
    }

    protected int addColumn(ColumnDef c) {
        if (nameToColIndex.containsKey(c.getName()))
            throw new AlfaRuntimeException("Column exists " + c.getName());

        int i = nameToColIndex.size() + 1;
        nameToColIndex.put(c.getName(), i);
        colIndexToName.put(i, c.getName());
        return i;
    }

    @Override
    public int findColumn(String columnLabel) {
        Integer c = nameToColIndex.get(columnLabel);

        if (c == null)
            return -1;

        return c;
    }

    @Override
    public boolean next() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean wasNull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return rsmd;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirst() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean last() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public class BaseResultSetMetaData implements ResultSetMetaData {

        @Override
        public int getColumnCount() throws SQLException {
            return tableDef.getColumns().size();
        }

        @Override
        public boolean isAutoIncrement(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCaseSensitive(int column) throws SQLException {
            return true;
        }

        @Override
        public boolean isSearchable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isCurrency(int column) throws SQLException {
            return false;
        }

        @Override
        public int isNullable(int column) throws SQLException {
            return 0;
        }

        @Override
        public boolean isSigned(int column) throws SQLException {
            return false;
        }

        @Override
        public int getColumnDisplaySize(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getColumnLabel(int column) throws SQLException {
            return colIndexToName.get(column);
        }

        @Override
        public String getColumnName(int column) throws SQLException {
            return colIndexToName.get(column);
        }

        @Override
        public String getSchemaName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getPrecision(int column) throws SQLException {
            return 0;
        }

        @Override
        public int getScale(int column) throws SQLException {
            return 0;
        }

        @Override
        public String getTableName(int column) throws SQLException {
            return null;
        }

        @Override
        public String getCatalogName(int column) throws SQLException {
            return null;
        }

        @Override
        public int getColumnType(int column) throws SQLException {
            switch (colIndexToType.get(column).getScalarType()) {
                case stringType:
                    return Types.LONGVARCHAR;
                case intType:
                    return Types.INTEGER;
                case doubleType:
                    return Types.DOUBLE;
                case longType:
                    return Types.INTEGER;
                case shortType:
                    return Types.SMALLINT;
                case datetimeType:
                    return Types.TIMESTAMP;
                case dateType:
                    return Types.DATE;
            }
            throw new AlfaRuntimeException("Unhandle " + getColumnTypeName(column));
        }

        @Override
        public String getColumnTypeName(int column) throws SQLException {
            String n = colIndexToType.get(column).getScalarType().name();
            return n.replaceAll("Type", "");
        }

        @Override
        public boolean isReadOnly(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public boolean isDefinitelyWritable(int column) throws SQLException {
            return false;
        }

        @Override
        public String getColumnClassName(int column) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }


}
