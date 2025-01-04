package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.ScalarDataType;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;

public interface ITable
{
    Set<String> getColumnNames();

    int getRowCount();

    int getColumnCount();

    Collection<Object> getValues(int row);

    Object getValue(int row, String colName);

    ScalarDataType getColumnType(String cname);

    ResultSet getResultSet();

    String toCsvString();
}
