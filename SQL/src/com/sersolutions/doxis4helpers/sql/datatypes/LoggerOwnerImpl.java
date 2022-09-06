package com.sersolutions.doxis4helpers.sql.datatypes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerOwnerImpl implements ILoggerOwner {
    public String getCurrentLoadingName()
    {
        return "";
    }

    public void writeLog(ILoggerOwner.LogLevel logLevel, String text) {
        writeLog(String.format("%s > %s", logLevel.toString(), text));
    }
    public void writeLog(String text) {
        String outText;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

        outText = String.format("%s > %s\r\n", sdf.format (new Date()), text);
        System.out.println(outText);
    }
}
