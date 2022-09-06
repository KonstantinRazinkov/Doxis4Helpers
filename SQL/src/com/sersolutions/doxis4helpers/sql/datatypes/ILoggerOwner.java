package com.sersolutions.doxis4helpers.sql.datatypes;

public interface ILoggerOwner {
    enum LogLevel {
        ERROR, WARNING, INFO, DEBUG, TRACE
    }
    void writeLog(String text);
    void writeLog(LogLevel level, String text);
    String getCurrentLoadingName();
}
