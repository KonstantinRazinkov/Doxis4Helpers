package com.sersolutions.doxis4helpers.notifications.email;

import com.ser.blueline.*;
import com.sersolutions.doxis4helpers.commons.*;
/**
 * Class for holding connection information to Mail server
 */
public class Connector {
    private String host;
    private String imaphost;
    private String port;
    private String login;
    private String pass;
    private String fromAddress;
    private String sentFolderName;
    private String imapPort;
    private String transportProtocol;
    private String startTlsEnable;
    private String startTlsRequired;
    private String timeOut;
    private String imapProtocol;

    public String getHost() {return host;}
    public String getPort() {return port;}

    public Integer getPortInt() {return Integer.parseInt(port);}
    public Integer getImapPortInt() {return Integer.parseInt(imapPort);}
    public String getLogin() {return login;}
    public String getPass() {return pass;}
    public String getFromAddress() {return  fromAddress;}
    public String getImaphost(){return imaphost;}
    public String getSentFolderName(){return sentFolderName;}
    public String getImapPort() {return imapPort;}
    public String getTransportProtocol() {return transportProtocol;}
    public String getStartTlsEnable() {return startTlsEnable;}
    public String getStartTlsRequired() {return startTlsRequired;}
    public String getTimeOut() {return timeOut;}

    public String getImapProtocol() {return imapProtocol;}

    /**
     * Init from some global value list
     * @param doxis4session Doxis4 Session object
     *                      @see com.ser.blueline.ISession
     * @param configGVLName global value list with configuration of SMTP + IMAP
     * @return connection info
     */
    public static Connector init(ISession doxis4session, String configGVLName) {
        if (configGVLName ==null || "".equals(configGVLName)) return  null;

        Connector mailConnector = new Connector();

        mailConnector.host = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "HOST", 0, 1, true);
        mailConnector.imaphost = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "IMAPHOST", 0, 1, true);
        mailConnector.port =  GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "PORT", 0, 1, true);
        mailConnector.login = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "LOGIN", 0, 1, true);
        mailConnector.pass = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "PASS", 0, 1, true);
        mailConnector.fromAddress = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "FROMADDRESS", 0, 1, true);
        mailConnector.sentFolderName = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "SENTFOLDER", 0, 1, true);
        mailConnector.imapPort = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "IMAPPORT", 0, 1, true);
        mailConnector.transportProtocol = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "TRANSPORTPROTOCOL", 0, 1, true);
        mailConnector.startTlsEnable = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "STARTTLSENABLE", 0, 1, true);
        mailConnector.startTlsRequired = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "STARTTLSREQUIRED", 0, 1, true);
        mailConnector.timeOut = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "TIMEOUT", 0, 1, true);
        mailConnector.imapProtocol = GlobalValueLists.GetValueFromGlobalValueList(doxis4session, configGVLName, "IMAPPROTOCOL", 0, 1, true);

        mailConnector.host = ("HOST".equals(mailConnector.host))? "localhost" : mailConnector.host;
        mailConnector.imaphost = ("IMAPHOST".equals(mailConnector.imaphost))? "localhost" : mailConnector.imaphost;
        mailConnector.port = ("PORT".equals(mailConnector.port))? "587" : mailConnector.port;
        mailConnector.login = ("LOGIN".equals(mailConnector.login))? null : mailConnector.login;
        mailConnector.pass = ("PASS".equals(mailConnector.pass))? null : mailConnector.pass;
        mailConnector.fromAddress = ("FROMADDRESS".equals(mailConnector.pass))? null : mailConnector.fromAddress;
        mailConnector.sentFolderName = ("SENTFOLDER".equals(mailConnector.sentFolderName))? "Sent" : mailConnector.sentFolderName;
        mailConnector.imapPort = ("IMAPPORT".equals(mailConnector.imapPort))? "993" : mailConnector.imapPort;
        mailConnector.transportProtocol = ("TRANSPORTPROTOCOL".equals(mailConnector.transportProtocol))? "smtp" : mailConnector.transportProtocol;
        mailConnector.startTlsEnable = ("STARTTLSENABLE".equals(mailConnector.startTlsEnable))? "false" : mailConnector.startTlsEnable;
        mailConnector.startTlsRequired = ("STARTTLSREQUIRED".equals(mailConnector.startTlsRequired))? "false" : mailConnector.startTlsRequired;
        mailConnector.timeOut = ("TIMEOUT".equals(mailConnector.timeOut))? "5000" : mailConnector.timeOut;
        mailConnector.imapProtocol = ("IMAPPROTOCOL".equals(mailConnector.imapProtocol))? "imap" : mailConnector.imapProtocol;

        return  mailConnector;
    }
}
