package com.sersolutions.doxis4helpers.sql;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.String;
import java.lang.Throwable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.sersolutions.doxis4helpers.sql.datatypes.*;

/**
 * Class for working with SQL Connection
 */

public class SqlConnector {
    Connection connection;
    Properties connectionProperties;
    Configuration configuration;

    List<String> codepages;
    int rowsCount=0;

    private ILoggerOwner loggerOwner;

    public int getRowsCount() {
        return rowsCount;
    }

    /**
     * Initializes connection to SQL
     * @param loggerOwner ILoggerOwner object
     *                    @see com.sersolutions.doxis4helpers.sql.datatypes.ILoggerOwner
     * @param configuration Configuration for connection
     *                      @see com.sersolutions.doxis4helpers.sql.datatypes.Configuration
     * @throws Exception if something goes wrong
     */
    public SqlConnector(ILoggerOwner loggerOwner, Configuration configuration) throws Exception {
        this.setLoggerOwner(loggerOwner);
        this.configuration = configuration;
        Open();
    }

    /**
     * Opens connection to DataBase
     * @throws Exception if something goes wrong
     */
    public void Open() throws Exception {
        if (configuration.getSqlClass() != null && !"".equalsIgnoreCase(configuration.getSqlClass())) {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Have driver class for DBMS: '%s', trying to load...", configuration.getSqlClass()));
            try {
                Class.forName(configuration.getSqlClass());
                getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "Driver class for DBMS was loaded");
            } catch (ClassNotFoundException ex) {
                getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format("Can't load driver class for DBMS. Description: %s\nReason: %s", ex.getMessage(), ex.getCause()));
            }

        } else {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "Driver class for DBMS was not defined");
        }

        connectionProperties = new Properties();
        if (configuration.getSqlLogin() != null && !"".equalsIgnoreCase(configuration.getSqlLogin())) {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, String.format("DB username defined: '%s'", configuration.getSqlLogin()));

            connectionProperties.put("user", configuration.getSqlLogin());
        } else {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "DB username was not defined");
        }
        if (configuration.getSqlPass() != null && !"".equalsIgnoreCase(configuration.getSqlPass()))
        {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, String.format("DB password defined: (hashcode - %d)",  configuration.getSqlPass().hashCode()));

            connectionProperties.put("password", configuration.getSqlPass());
        } else {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "DB password was not defined");
        }

        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, String.format("Trying to connect to DBMS with connection string: '%s'", configuration.getSqlConnectionString()));
        try {
            connection  = DriverManager.getConnection(configuration.getSqlConnectionString(), connectionProperties);
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, String.format("Connection to DBMS successful! DB catalog: '%s'", connection.getCatalog()));
        } catch (java.lang.Throwable ex) {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format("Can't connect to DBMS! Error: %s", ex.getMessage()));
            throw new Exception("Error while connection to DB!", ex);
        }

    }

    /**
     * Checks connection status
     * @return boolean value of connection
     */
    public boolean CheckStatus() {
        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "Check connection to DBMS");
        try {
            if (connection.getCatalog() == null || "".equals(connection.getCatalog())) throw new Exception();
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Connection to DBMS checked! DB catalog: '%s'", connection.getCatalog()));
            return true;
        } catch (Throwable ex) {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, "Have no connection to DBMS!");
            return false;
        }

    }

    /**
     * Closes connection to DataBase
     */
    public void Close() {
        try {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, "Closing connection to DBMS");
            this.connection.close();
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, "Connection to DBMS succesfully closed");
        } catch (Exception ex) {
                getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format("Can't close DMBS connection! Error: %s", ex.getMessage()));
        }
        this.connection = null;

    }

    /**
     * Executes query to DataBase
     * @param sqlQuery string with SQL Query
     * @return List of SqlRows from DataBase
     *                 @see com.sersolutions.doxis4helpers.sql.datatypes.SqlRow
     * @throws Exception if something goes wrong
     */
    public List<SqlRow> Query(String sqlQuery) throws Exception {
        List<SqlRow> result = new ArrayList<>(10);
//        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, String.format("Trying to make a query '%s'", sqlQuery));

        Statement sqlStatement;
        ResultSet resultSet;
        try {
            sqlStatement = connection.createStatement();
        } catch (SQLException ex) {
            String errorMessage = String.format("Error while initializing SQL statement: %s", ex.getMessage());
//            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format("Error while initializing SQL statement: %s", errorMessage));
            throw new Exception (errorMessage, ex);

        }
        try {
            resultSet = sqlStatement.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            String errorMessage = String.format("Error while executing SQL query: %s", ex.getMessage());
//            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format(errorMessage));
            throw new Exception (errorMessage, ex);

        }
//        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, "SQL Query executed successfully");
        rowsCount=0;

        while (resultSet.next()) {
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.TRACE, String.format("Got line № %d", rowsCount + 1));
            SqlRow sqlRowProcessor = new SqlRow(getLoggerOwner(), configuration);
            sqlRowProcessor.ProcessSelectResultSet(resultSet);
            result.add(rowsCount, sqlRowProcessor);
            rowsCount++;
        }

        if (rowsCount == 0) {
//            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, "Have no results!");
        } else {
//            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, String.format("Total selected rows: %d", rowsCount));
        }
        return result;
    }

    /**
     * Executes SQL query (example: insert, drop, delete etc.)
     * @param insertQuery string with SQL query
     * @return boolean execution result value
     * @throws Exception if something goes wrong
     */
    public boolean Execute(String insertQuery) throws Exception {
        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, String.format("Trying to make a command '%s'", insertQuery));

        Statement sqlStatement;
        ResultSet resultSet;
        try {
            sqlStatement = connection.createStatement();
        } catch (SQLException ex) {
            String errorMessage = String.format("Error while initializing SQL statement: %s", ex.getMessage());
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format("Error while initializing SQL statement: %s", errorMessage));
            throw new Exception (errorMessage, ex);
        }
        try {
            sqlStatement.execute(insertQuery);
        } catch (SQLException ex) {
            String errorMessage = String.format("Error while executing SQL command: %s", ex.getMessage());
            getLoggerOwner().writeLog(ILoggerOwner.LogLevel.ERROR, String.format(errorMessage));
            throw new Exception (errorMessage, ex);
        }
        getLoggerOwner().writeLog(ILoggerOwner.LogLevel.DEBUG, "Command executed successfully");

        return true;
    }

    public ILoggerOwner getLoggerOwner() {
        return loggerOwner;
    }

    public void setLoggerOwner(ILoggerOwner loggerOwner) {
        this.loggerOwner = loggerOwner;
    }
}
