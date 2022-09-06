package com.sersolutions.doxis4helpers.sql.datatypes;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for working with SQL rows
 */
public class SqlRow implements Cloneable {
    List<Object> columns;
    List<String> columnNames;
    int columnsCount;
    ILoggerOwner loggerOwner;
    Configuration configuration;

    public SqlRow()throws NotImplementedException {
        throw new NotImplementedException();
    }

    public SqlRow(ILoggerOwner loggerOwner, Configuration configuration) {
        this.loggerOwner = loggerOwner;
        this.configuration = configuration;
    }

    @Override
    public SqlRow clone()
    {
        return clone(false);
    }

    /**
     * Clones SqlRow (makes copy)
     * @param clean defines whether to clean row cells or not
     * @return new SqlRow object
     *             @see com.sersolutions.doxis4helpers.sql.datatypes.SqlRow
     */
    public SqlRow clone(boolean clean) {
        SqlRow newRow = new SqlRow(this.loggerOwner, this.configuration);
        newRow.columns = new ArrayList<>(this.columns.size());
        newRow.columnNames = new ArrayList<>(this.columnNames.size());
        newRow.columnsCount = this.columnsCount;
        for (int columnNo = 0; columnNo < columnsCount; columnNo++) {
            Object value = columns.get(columnNo);
            newRow.columnNames.add(columnNames.get(columnNo));
            if (!clean) newRow.columns.add(columns.get(columnNo));
            else {
                if (value == null) {
                    newRow.columns.add(null);
                }
                if (value instanceof Integer) {
                    newRow.columns.add(new Integer(0));
                }
                if (value instanceof Float) {
                    newRow.columns.add(new Float(0.0f));
                }
                if (value instanceof Double) {
                    newRow.columns.add(new Double(0.0));
                }
                if (value instanceof String) {
                    newRow.columns.add("");
                }
                if (value instanceof Date) {
                    newRow.columns.add(new Date(0));
                }
            }
        }
        return newRow;
    }

    public void SetColumns(List<String> columnNames, List<Object> columns) {
        this.columns = columns;
        this.columnNames = columnNames;
        this.columnsCount = columns.size();
    }

    public List<Object> getColumns() {
        return columns;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Gets column by its name
     * @param columnName Column name
     * @return
     */
    public Object getColumnByName(String columnName) {
        if (columnNames == null) return null;
        for (int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
            String name = columnNames.get(columnNumber);
            if (name == null) continue;
            if (name.equalsIgnoreCase(columnName)) return columns.get(columnNumber);
        }
        return null;
    }

    public boolean AdditionalAnalyseSelect(ResultSet resultSet) throws  SQLException {
        return false;
    }

    public void ProcessSelectResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        columns = new ArrayList<>(rsmd.getColumnCount());
        columnNames = new ArrayList<>(rsmd.getColumnCount());
        columnsCount = 0;

        boolean columnResponsed = true;
        while (columnResponsed) {
            columnsCount++;

            if (AdditionalAnalyseSelect(resultSet)) {
                columnNames.add(columnsCount-1, rsmd.getColumnName(columnsCount));
                continue;
            }
            try {
                Date testDate = resultSet.getDate(columnsCount);
                Timestamp timestamp = resultSet.getTimestamp(columnsCount);
                if (timestamp != null) {
                    testDate = new Date(timestamp.getTime());
                }

                loggerOwner.writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Column № %d loaded with data type Date", columnsCount));
                columns.add(columnsCount-1, testDate);

                columnNames.add(columnsCount - 1, rsmd.getColumnName(columnsCount));
                continue;
            } catch (Exception ex) {

            }

            try {
                if (rsmd.getColumnClassName(columnsCount).equals(Double.class.getName())) {
                    double testDouble = resultSet.getDouble(columnsCount);
                    columns.add(columnsCount-1, testDouble);
                    loggerOwner.writeLog(ILoggerOwner.LogLevel.TRACE, String.format("КColumn № %d loaded with data type Double", columnsCount));
                    columnNames.add(columnsCount - 1, rsmd.getColumnName(columnsCount));
                    continue;
                }
            } catch (Exception ex) {

            }
            try {
                if (rsmd.getColumnClassName(columnsCount).equals(Float.class.getName())) {
                    float testFloat = resultSet.getFloat(columnsCount);
                    columns.add(columnsCount-1, testFloat);
                    loggerOwner.writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Column № %d loaded with data type Float", columnsCount));
                    columnNames.add(columnsCount - 1, rsmd.getColumnName(columnsCount));
                    continue;
                }
            } catch (Exception ex) {

            }
            try {
                if (rsmd.getColumnClassName(columnsCount).equals(Integer.class.getName())) {
                    int testInt = resultSet.getInt(columnsCount);
                    columns.add(columnsCount-1, testInt);
                    loggerOwner.writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Column № %d loaded with data type Integer", columnsCount));
                    columnNames.add(columnsCount - 1, rsmd.getColumnName(columnsCount));
                    continue;
                }
            } catch (Exception ex) {

            }
            try {
                String testString = resultSet.getString(columnsCount);
                columns.add(columnsCount-1, testString);
                loggerOwner.writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Column № %d loaded with data type String", columnsCount));
                columnNames.add(columnsCount - 1, rsmd.getColumnName(columnsCount));
                continue;
            } catch (Exception ex) {

            }

            columnResponsed = false;
        }

        columnsCount = columns.size();
    }

    public enum GenerateMode {
        Safe,
        Unsafe
    }

    public String GenerateInsertLine()
    {
        return this.GenerateInsertLine(GenerateMode.Safe);
    }

    public String GenerateInsertLine(GenerateMode generateMode) {
        StringBuilder names = new StringBuilder(columnNames.size());
        StringBuilder values = new StringBuilder(columns.size());

        names.append("(");
        values.append("VALUES (");
        for (int columnNo = 0; columnNo < columnNames.size(); columnNo++) {
            if (columnNo> 0) {
                names.append(", ");
                values.append(", ");
            }
            names.append(columnNames.get(columnNo));

            Object value = columns.get(columnNo);
            if (value == null) {
                values.append("NULL");
            }
            if (value instanceof Integer) {
                values.append((int) value);
            }
            if (value instanceof Float) {
                values.append((float) value);
            }
            if (value instanceof Double) {
                values.append((double) value);
            }
            if (value instanceof String) {
                String stringValue = (String) value;
                if (generateMode == GenerateMode.Safe) {
                    stringValue = stringValue.replace("'", " ").replace("\\", " ").replace("/", " ");
                }
                values.append(String.format("N'%s'", stringValue));
            }
            if (value instanceof Date) {
                java.sql.Timestamp sqTimestamp = new java.sql.Timestamp(((Date)value).getTime());

                values.append(String.format(configuration.getDateTimeConversionCode(), sqTimestamp.toString().replace(" ", "T"))); //TODO: Переделать! Надо задавать формат в настройках!
            }
        }
        names.append(")");
        values.append(")");
        return String.format("%s %s", names.toString(), values.toString());
    }

    public String GenerateUpdateLine(String keyColumns)
    {
        return GenerateUpdateLine(keyColumns, GenerateMode.Safe);
    }
    public String GenerateUpdateLine(String keyColumns, GenerateMode generateMode) {
        keyColumns = keyColumns.toUpperCase();
        if (!keyColumns.contains(";")) {
            String[] keys = new String[1];
            keys[0] = keyColumns;
            return GenerateUpdateLine(new ArrayList<String>(Arrays.asList(keys)), generateMode);
        }
        return GenerateUpdateLine(new ArrayList<String>(Arrays.asList(keyColumns.split(";"))), generateMode);
    }

    public String GenerateUpdateLine(List<String> keyColumns) {
        return GenerateUpdateLine(keyColumns, GenerateMode.Safe);
    }
    public String GenerateUpdateLine(List<String> keyColumns, GenerateMode generateMode) {
        StringBuilder updateColumns = new StringBuilder(columnNames.size());
        StringBuilder whereColumns = new StringBuilder(keyColumns.size());
        StringBuilder currentBuiler = null;
        int updateColumnsCount = 0;
        int keyColumnsCount = 0;

        Object value;
        for (int columnNo = 0; columnNo < columnNames.size(); columnNo++) {
            value = columns.get(columnNo);
            if (keyColumns.contains(columnNames.get(columnNo).toUpperCase())) {
                if (keyColumnsCount > 0) {
                    whereColumns.append("AND ");
                }
                whereColumns.append(columnNames.get(columnNo));
                if (value == null) {
                    whereColumns.append(" IS NULL");
                } else {
                    whereColumns.append(" = ");
                }
                currentBuiler = whereColumns;
                keyColumnsCount++;
            } else {
                if (updateColumnsCount> 0) {
                    updateColumns.append(", ");
                }
                updateColumns.append(columnNames.get(columnNo));
                updateColumns.append(" = ");
                currentBuiler = updateColumns;
                updateColumnsCount++;
            }
            if (value instanceof Integer) {
                currentBuiler.append((int) value);
            }
            if (value instanceof Float) {
                currentBuiler.append((float) value);
            }
            if (value instanceof Double) {
                currentBuiler.append((double) value);
            }
            if (value instanceof String) {
                String stringValue = (String) value;
                if (generateMode == GenerateMode.Safe) {
                    stringValue = stringValue.replace("'", " ").replace("\\", " ").replace("/", " ");
                }
                currentBuiler.append(String.format("N'%s'", stringValue));
            }
            if (value instanceof Date) {
                java.sql.Timestamp sqTimestamp = new java.sql.Timestamp(((Date)value).getTime());
                currentBuiler.append(String.format(configuration.getDateTimeConversionCode(), sqTimestamp.toString().replace(" ", "T")));
            }
        }
        return String.format("SET %s WHERE %s", updateColumns.toString(), whereColumns.toString());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int columnNo = 0; columnNo < columnNames.size(); columnNo++) {

            result.append(columnNames.get(columnNo));

            result.append(" = ");
            Object value = columns.get(columnNo);
            if (value == null) {
                result.append("NULL");
            }
            if (value instanceof Float) {
                result.append((float) value);
            }
            if (value instanceof Double) {
                result.append((double) value);
            }
            if (value instanceof Integer) {
                result.append((int) value);
            }
            if (value instanceof String) {
                result.append(String.format("'%s'", ((String) value).replace("'", " ").replace("\\", " ").replace("/", " ")));
            }
            if (value instanceof Date) {
                result.append(new Timestamp(((Date)value).getTime()).toString());
            }

            result.append("\n");
        }

        return result.toString();
    }
}
