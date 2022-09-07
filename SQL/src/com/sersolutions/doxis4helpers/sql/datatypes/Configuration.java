package com.sersolutions.doxis4helpers.sql.datatypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;

/**
 * Class for setting configuration for SQL connection
 */

public class Configuration {
    String sqlClass;
    String sqlConnectionString;
    String sqlLogin;
    String sqlPass;


    String dateTimeConversionCode = "CONVERT(datetime,'%s', 127) ";
    String _previousFileName;

    public String getSqlClass() {return sqlClass;}
    public void setSqlClass(String sqlClass) {this.sqlClass = sqlClass;}
    public String getSqlConnectionString() {return sqlConnectionString;}
    public void setSqlConnectionString(String sqlConnectionString) {this.sqlConnectionString = sqlConnectionString;}
    public String getSqlLogin() {return sqlLogin;}
    public void setSqlLogin(String sqlLogin) {this.sqlLogin = sqlLogin;}
    public String getSqlPass() {return sqlPass;}
    public void setSqlPass(String sqlPass) {this.sqlPass = sqlPass;}
    public String getDateTimeConversionCode() {return dateTimeConversionCode;}
    public void setDateTimeConversionCode(String dateTimeConversionCode) {this.dateTimeConversionCode = dateTimeConversionCode;}

    /**
     * Loads SQL connection params (class, connectionString, login and pass) from XML file
     * @param fileName String value of configuration file to load
     * @throws Exception if something goes wrong
     */

    public void Load(String fileName) throws  Exception {
        File fileConfig = new File(fileName);
        if (!fileConfig.exists()) {
            throw new Exception(String.format("There is no config file %s", fileName));
        }

        if (!fileConfig.isFile()) {
            throw new Exception(String.format("%s is not a File!", fileName));
        }

        java.util.Properties configProps = new java.util.Properties();
        try {
            FileInputStream in = new FileInputStream(fileName);
            configProps.loadFromXML(in);
            in.close();
        } catch(Exception e) {
            System.out.println(String.format("Error while loading config file '%s'", fileName));
        }

        sqlClass = Objects.toString(configProps.getProperty("sql.class"), "");
        sqlConnectionString = Objects.toString(configProps.getProperty("sql.connectionString"), "");
        sqlLogin = Objects.toString(configProps.getProperty("sql.login"), "");
        sqlPass = Objects.toString(configProps.getProperty("sql.pass"), "");

        _previousFileName = fileName;
    }

    /**
     * Saves SQL connection params (class, connectionString, login and pass) to XML file
     * @param fileName String value of configuration file to load
     * @throws Exception if something goes wrong
     */
    public void Save(String fileName) throws Exception {
        File fileConfig = null;
        if (fileName == null || fileName == "") {
            fileName = _previousFileName;
        } else {
            fileConfig = new File(fileName);

            if (!fileConfig.exists() && _previousFileName != null && !"".equalsIgnoreCase(_previousFileName)) {
                fileName = _previousFileName;
            }
        }

        if (fileName == null || fileName == "") {
            throw  new Exception("There is no save file selected");
        }

        java.util.Properties configProps = new java.util.Properties();
        configProps.setProperty("sql.class", sqlClass);
        configProps.setProperty("sql.connectionString", sqlConnectionString);
        configProps.setProperty("sql.login", sqlLogin);
        configProps.setProperty("sql.pass", sqlPass);

        try {
            File create = new File(fileName);
            create.createNewFile();
            create = null;

            FileOutputStream out = new FileOutputStream(fileName);
            configProps.storeToXML(out, "");
            out.flush();
            out.close();
        } catch(Exception e) {
            System.out.println(String.format("Error while saving config file '%s'", fileName));
        }
    }
}
