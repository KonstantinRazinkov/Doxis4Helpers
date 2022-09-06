package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.IStringMatrix;
import com.ser.blueline.modifiablemetadata.IStringMatrixModifiable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class for working with Global Value Lists (GVL)
 */
public class GlobalValueLists {
    public static ConcurrentMap<String, List<List<String>>> cachedGVLs;
    public static ConcurrentMap<String, IStringMatrix> cachedGVLs2 = new ConcurrentHashMap<>();
    private static final Logger log = LogManager.getLogger(GlobalValueLists.class);

    /**
     * Get cached earlier global value list as List of List of string (rows, columns)
     * @param name FQN of global value list
     * @return List of List of Strings (rows, columns)
     */
    public static List<List<String>> GetCachedGVL(String name) {
        if (cachedGVLs == null) cachedGVLs = new ConcurrentHashMap<>(2);
        if (cachedGVLs.containsKey(name)) return cachedGVLs.get(name);
        return null;
    }

    /**
     * Get cached earlier global value list as List of List of string (rows, columns)
     * Version 2
     * @param name FQN of global value list
     * @return IStringMatrix
     */
    public static IStringMatrix getCachedGVL2(String name) {
        return cachedGVLs2.getOrDefault(name, null);
    }

    /**
     * Cache global value list from Doxis4 to List of List of Strings (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN of global value list
     * @return List of List of String (rows, columns)
     */
    public static List<List<String>>  CacheGVL(ISession doxis4Session, String gvlName) {
        Object oldCachedGVL =GetCachedGVL(gvlName);
        if (GetCachedGVL(gvlName) != null) {
            cachedGVLs.remove(gvlName);
        }
        IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
        List<List<String>> newCachedGVL = new ArrayList<>(doxis4Matrix.getRowCount()+1);
        for (int rowNo = 0; rowNo < doxis4Matrix.getRowCount(); rowNo++) {
            List<String> line = new ArrayList<>(doxis4Matrix.getColumnCount());
            for (int colNo = 0; colNo < doxis4Matrix.getColumnCount(); colNo++) {
                line.add(colNo, doxis4Matrix.getValue(rowNo, colNo));
            }
            newCachedGVL.add(rowNo, line);
        }
        cachedGVLs.put(gvlName, newCachedGVL);
        return newCachedGVL;
    }

    /**
     * Cache global value list from Doxis4 to List of List of Strings (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlNameOrId FQN of global value list
     * @return IStringMatrix
     */
    public static IStringMatrix  cacheGVL2(ISession doxis4Session, String gvlNameOrId) {
        IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlNameOrId, doxis4Session);
        if (doxis4Matrix == null) {
            doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrixByID(gvlNameOrId, doxis4Session);
        }
        if (getCachedGVL2(gvlNameOrId) != null) {
            cachedGVLs2.replace(gvlNameOrId, doxis4Matrix);
        } else {
            cachedGVLs2.put(gvlNameOrId, doxis4Matrix);
        }
        return doxis4Matrix;
    }

    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list. By default, key column is 0
     * @param column number of value column in global value list
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int column) {
        return GetValueFromGlobalValueList(doxis4Session, gvlName, keyValue, 0, column);
    }

    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, int column) {
        return  GetValueFromGlobalValueList(doxis4Session, gvlName, keyValue, keycolumn, column, false);
    }

    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, int column, boolean cacheGVL) {
        int row;
        try {
            if (cacheGVL) {
                List<List<String>> cachedGVL = GetCachedGVL(gvlName);
                if (cachedGVL == null) {
                    cachedGVL = CacheGVL(doxis4Session, gvlName);
                }
                for (row = 0; row < cachedGVL.size(); row++) {
                    if (keyValue.equalsIgnoreCase(cachedGVL.get(row).get(keycolumn))) {
                        return cachedGVL.get(row).get(column);
                    }
                }
            } else {
                IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
                for (row = 0; row < doxis4Matrix.getRowCount(); row++) {
                    if (keyValue.equalsIgnoreCase(doxis4Matrix.getValue(row, keycolumn))) {
                        return doxis4Matrix.getValue(row, column);
                    }
                }
            }
        } catch (Exception ex) {

        }
        return  (keyValue == null)? "" : keyValue;
    }

    /**
     * Get array of values from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list. By default, key column is 0
     * @param column number of value column in global value list
     * @return List of string with values from global value list. Will return empty list if no results found.
     */
    public static List<String> GetValuesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int column) {
        return GetValuesFromGlobalValueList(doxis4Session, gvlName, keyValue, 0, column);
    }
    /**
     * Get array of values from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @return List of string with values from global value list. Will return empty list if no results found.
     */
    public static List<String> GetValuesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn,  int column) {
        return GetValuesFromGlobalValueList(doxis4Session, gvlName, keyValue, keycolumn, column, false);
    }

    /**
     * Get array of values from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return List of string with values from global value list. Will return empty list if no results found.
     */
    public static List<String> GetValuesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, int column, boolean cacheGVL) {
        List<String> result = new ArrayList<>();
        int row;
        try {
            if (cacheGVL) {
                List<List<String>> cachedGVL = GetCachedGVL(gvlName);
                if (cachedGVL == null) {
                    cachedGVL = CacheGVL(doxis4Session, gvlName);
                }
                result = new ArrayList<>(cachedGVL.size());
                for (row = 0; row < cachedGVL.size(); row++) {
                    String value = cachedGVL.get(row).get(keycolumn);
                    if (value == null || "".equals(value)) continue;
                    if (keyValue.contains(value) || "*".equals(keyValue)) {
                        String resultValue = cachedGVL.get(row).get(column);
                        if (!result.contains(resultValue)) {
                            result.add(resultValue);
                        }
                    }
                }
            } else {
                IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
                result = new ArrayList<>(doxis4Matrix.getRowCount());
                for (row = 0; row < doxis4Matrix.getRowCount(); row++) {
                    String value = doxis4Matrix.getValue(row, keycolumn);
                    if (value == null || "".equals(value)) continue;
                    if (keyValue.contains(value) || "*".equals(keyValue)) {
                        String resultValue = doxis4Matrix.getValue(row, column);
                        if (!result.contains(resultValue)) {
                            result.add(resultValue);
                        }
                    }
                }
            }
        } catch (Exception ex) {

        }
        return  result;
    }

    /**
     * Get row from global value list as List of string
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list. By default, key column is 0
     * @return List of strings (rows, columns). Will return empty list if no results found.
     */
    public static List<String> GetLineFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue) {
        return GetLineFromGlobalValueList(doxis4Session, gvlName, keyValue, 0);
    }

    /**
     * Get row from global value list as List of string
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @return List of strings (rows, columns). Will return empty list if no results found
     */
    public static List<String> GetLineFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn) {
        return  GetLineFromGlobalValueList(doxis4Session, gvlName, keyValue, keycolumn, false);
    }

    /**
     * Get row from global value list as List of string
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return List of strings (rows, columns). Will return empty list if no results found
     */
    public static List<String> GetLineFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, boolean cacheGVL) {
        List<String> result = null;
        int row;
        try {
            if (cacheGVL) {
                List<List<String>> cachedGVL = GetCachedGVL(gvlName);
                if (cachedGVL == null) {
                    cachedGVL = CacheGVL(doxis4Session, gvlName);
                }
                for (row = 0; row < cachedGVL.size(); row++) {
                    if (keyValue.equalsIgnoreCase(cachedGVL.get(row).get(keycolumn))) {
                        result = cachedGVL.get(row);
                    }
                }
            } else {
                IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
                for (row = 0; row < doxis4Matrix.getRowCount(); row++) {
                    if (keyValue.equalsIgnoreCase(doxis4Matrix.getValue(row, keycolumn))) {
                        result = new ArrayList<>(doxis4Matrix.getColumnCount());
                        for (int colNo = 0; colNo < doxis4Matrix.getColumnCount(); colNo++) {
                            result.add(colNo, doxis4Matrix.getValue(row, colNo));
                        }
                    }
                }
            }
        } catch (Exception ex) {

        }
        return  result;
    }

    /**
     * Get multiple rows from global value list as List of List of String (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list. By default, key column is 0
     * @return List of list of strings (rows, columns). Will return empty list if no results found
     */
    public static List<List<String>> GetLinesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue) {
        return GetLinesFromGlobalValueList(doxis4Session, gvlName, keyValue, 0);
    }

    /**
     * Get multiple rows from global value list as List of List of String (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list
     * @param keycolumn number of key column in global value list
     * @return List of list of strings (rows, columns). Will return empty list if no results found
     */
    public static List<List<String>> GetLinesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn) {
        return  GetLinesFromGlobalValueList(doxis4Session, gvlName, keyValue, keycolumn, false);
    }

    /**
     * Get multiple rows from global value list as List of List of String (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target rows in global value list
     * @param keycolumn number of key column in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return List of list of strings (rows, columns). Will return empty list if no results found
     */
    public static List<List<String>> GetLinesFromGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, boolean cacheGVL) {
        List<List<String>> result = new ArrayList<>();
        int row;
        try {
            if (cacheGVL) {
                List<List<String>> cachedGVL = GetCachedGVL(gvlName);
                if (cachedGVL == null) {
                    cachedGVL = CacheGVL(doxis4Session, gvlName);
                }
                result = new ArrayList<>(cachedGVL.size());
                for (row = 0; row < cachedGVL.size(); row++) {
                    String value = cachedGVL.get(row).get(keycolumn);
                    if (value == null || "".equals(value)) continue;
                    if (keyValue.contains(value) || "*".equals(keyValue)) {
                        result.add(cachedGVL.get(row));
                    }
                }
            } else {
                IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
                result = new ArrayList<>(doxis4Matrix.getRowCount());
                for (row = 0; row < doxis4Matrix.getRowCount(); row++) {
                    String value = doxis4Matrix.getValue(row, keycolumn);
                    if (value == null || "".equals(value)) continue;
                    if (keyValue.contains(value) || "*".equals(keyValue)) {
                        List<String> newRow = new ArrayList<>(doxis4Matrix.getColumnCount());
                        for (int colNo = 0; colNo < doxis4Matrix.getColumnCount(); colNo++) {
                            newRow.add(colNo, doxis4Matrix.getValue(row, colNo));
                        }
                        result.add(newRow);
                    }
                }
            }
        } catch (Exception ex) {

        }
        return  result;
    }

    /**
     * Get multiple rows from global value list as List of List of String (rows, columns)
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param conditions 'column name - value' pairs for searching target rows in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return List of list of strings (rows, columns). Will return empty list if no results found
     */
    public static List<List<String>> GetLinesFromGlobalValueList(ISession doxis4Session, String gvlName, Map<Integer, String> conditions, boolean cacheGVL) {
        List<List<String>> result = new ArrayList<>();
        IStringMatrix stringMatrix = null;
        try {
            if (cacheGVL) {
                stringMatrix = getCachedGVL2(gvlName);
                if (stringMatrix == null) {
                    stringMatrix = cacheGVL2(doxis4Session, gvlName);
                }
            } else {
                stringMatrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
            }
            for (int row = 0; row < stringMatrix.getRowCount(); row++) {
                boolean isRowMatchConditions = true;
                boolean isFiltered = false; //marker that filter is 'working' (columns in conditions exist in string matrix, otherwise there is a config error, do not return results)
                for (int column = 0; column < stringMatrix.getColumnCount(); column++) {
                    if (conditions.containsKey(column)) {
                        if (!conditions.get(column).equals(stringMatrix.getValue(row, column)) && !"*".equals(stringMatrix.getValue(row, column))) {
                            isRowMatchConditions = false;
                        }
                        isFiltered = true;
                    }
                }
                if (isFiltered && isRowMatchConditions) {
                    result.add(stringMatrix.getRow(row));
                }
            }
        } catch (Exception ex) {
            log.error(String.format("Error in GetLinesFromGlobalValueList. gvlName='%s'; conditions='%s' stringMatrix='%s'",gvlName, conditions, stringMatrix) , ex);
        }
        return result;
    }


    /**
     * Get Global Value List (GVL) Column number by its name
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN of global value list
     * @param columnName column name from global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return number of column. If column with such name not found - method will return -1
     */
    public static int GetColumnNumber(ISession doxis4Session, String gvlName, String columnName, boolean cacheGVL) {
        IStringMatrix stringMatrix = null;

        try {
            if (cacheGVL) {
                stringMatrix = getCachedGVL2(gvlName);
                if (stringMatrix == null) {
                    stringMatrix = cacheGVL2(doxis4Session, gvlName);
                }
            } else {
                stringMatrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
            }
            for (int column = 0; column < stringMatrix.getColumnCount(); column++) {
                if (stringMatrix.getColumnName(column).equals(columnName)) {
                    return column;
                }
            }
        } catch (Exception ex) {
            log.error(String.format("Error in GetColumnNumber. gvlName='%s'; columnName='%s' stringMatrix='%s'",gvlName, columnName, stringMatrix) , ex);
        }
        return -1;
    }

    /**
     * Sets value to global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN of global value list
     * @param keyValue key value to search row that must have new value. By default, key column is 0
     * @param column number of value column in global value list
     * @param value value that must be set to global value list
     * @return value that was settled up. If value could not be set - empty string will be returned.
     */
    public static String SetValueToGlobalValueList(ISession doxis4Session, String gvlName, String keyValue,  int column, String value) throws Exception {
        return SetValueToGlobalValueList(doxis4Session, gvlName, keyValue, 0, column, value);
    }

    /**
     * Sets value to global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN of global value list
     * @param keyValue key value to search row that must have new value
     * @param keyColumn number of key column in global value list
     * @param column number of value column in global value list
     * @param value value that must be set to global value list
     * @return value that was settled up. If value could not be set - empty string will be returned.
     */
    public static String SetValueToGlobalValueList(ISession doxis4Session, String gvlName, String keyValue, int keyColumn, int column, String value) {

        int row;
        try {
            List<List<String>> cachedGVL = GetCachedGVL(gvlName);
            IStringMatrix doxis4Matrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
            IStringMatrixModifiable doxis4ModifableMatrix = doxis4Matrix.getModifiableCopy(doxis4Session);
            for (row = 0; row < doxis4Matrix.getRowCount(); row++) {
                if (keyValue.equalsIgnoreCase(doxis4Matrix.getValue(row, keyColumn))) {
                    doxis4ModifableMatrix.setValue(row, column, value, true);
                    doxis4ModifableMatrix.commit();

                    if (cachedGVL != null) {
                        cachedGVL.get(row).set(column, value);
                    }
                    return value;
                }
            }

            doxis4ModifableMatrix.addRow(row);
            doxis4ModifableMatrix.setValue(row, 0, keyValue, true);
            doxis4ModifableMatrix.setValue(row, column, value, true);
            doxis4ModifableMatrix.commit();
            if (cachedGVL != null) {
                List<String> newRow = new ArrayList<>(doxis4Matrix.getColumnCount());
                newRow.add(column, value);
                cachedGVL.add(newRow);
            }
        } catch (Exception ex) {

        }
        return  (keyValue == null)? "" : keyValue;
    }


    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list. By deafault key column is 0
     * @param column number of value column in global value list
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList2(ISession doxis4Session, String gvlName, String keyValue, int column) {
        return GetValueFromGlobalValueList2(doxis4Session, gvlName, keyValue, 0, column);
    }

    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList2(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, int column) {
        return GetValueFromGlobalValueList2(doxis4Session, gvlName, keyValue, keycolumn, column, false);
    }

    /**
     * Get single value from global value list
     * @param doxis4Session Doxis4 Session object
     *                      @see ISession
     * @param gvlName FQN name of global value list
     * @param keyValue key value for searching target row in global value list
     * @param keycolumn number of key column in global value list
     * @param column number of value column in global value list
     * @param cacheGVL use local cache of global value list (global value list will be cached on first request, after that every request will use only local cache)
     * @return String value from global value list. Will return original value if no row found by key value.
     */
    public static String GetValueFromGlobalValueList2(ISession doxis4Session, String gvlName, String keyValue, int keycolumn, int column, boolean cacheGVL) {
        IStringMatrix stringMatrix;
        try {

            if (cacheGVL) {
                stringMatrix = getCachedGVL2(gvlName);
                if (stringMatrix == null) {
                    stringMatrix = cacheGVL2(doxis4Session, gvlName);
                }
            } else {
                stringMatrix = doxis4Session.getDocumentServer().getStringMatrix(gvlName, doxis4Session);
            }
            for (int row = 0; row < stringMatrix.getRowCount(); row++) {
                if (keyValue.equalsIgnoreCase(stringMatrix.getValue(row, keycolumn))) {
                    return stringMatrix.getValue(row, column);
                }
            }
        } catch (Exception ex) {
            log.error(String.format("Error in GetLinesFromGlobalValueList. gvlName='%s'; keyValue='%s' keycolumn='%s' column='%s'", gvlName, keyValue, keycolumn, column), ex);
        }
        return (keyValue == null) ? "" : keyValue;
    }
}
