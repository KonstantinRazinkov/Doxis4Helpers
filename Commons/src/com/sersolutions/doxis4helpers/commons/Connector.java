package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.*;
import com.ser.sedna.client.bluelineimpl.SEDNABluelineAdapterFactory;

/**
 *  Class for connection to Doxis4 server.
 *  <p>
 *      Don't need to use this if you already have some connection (when write scripts for AgentServer or webCube scripting)
 *  </p>
 */
public class Connector {
    private IDocumentServer _documentServer;
    private ISerClassFactory _classFactory;
    private ISession _session = null;

    /**
     * This method initializes the class factory and the document server
     * instance.
     *
     * @param archiveServerName Host name of the web application server that
     * hosts DOXiS4 CSB.
     *
     * @param archivePort Port number used by the web application server, for
     * example: 8080.
     *
     * @throws Exception in case of any error.
     */
    public void initServer(String archiveServerName, String archivePort, String tempDir) throws Exception {
        // Instantiate the class factory.
        // Class factoryClass =
        // Class.forName("de.serac.bluelineimpl.SERACClassFactory");
        // classFactory = (ISerClassFactory) factoryClass.newInstance();
        _classFactory = SEDNABluelineAdapterFactory.getInstance();

        // Instantiate a properties object filled with the values in the
        // configuration file name.
        IProperties properties = _classFactory.getPropertiesInstance();

        // Add/Change some properties for the connection to the server.
        properties.setProperty("Global", "ArchivServerName", archiveServerName);
        properties.setProperty("Global", "SeratioServerName", archiveServerName);
        properties.setProperty("Global", "ArchivPort", archivePort);
        properties.setProperty("Global", "SeratioPort", archivePort);
        properties.setProperty("Global", "TmpDir", tempDir);

        // Instantiate the DocumentServer now.
        _documentServer = _classFactory.getDocumentServerInstance(properties);
        
    }

    /**
     * This method closes the IDocumentServer object. It must be invoked, if the
     * IDocumentServer object is no longer needed to release all used resources
     *
     * @throws BlueLineException
     *             in case of errors
     */
    public void closeServer() {
        if (_documentServer != null) {
            try {
                _documentServer.logout(_session);
                _documentServer.close();

            } catch (BlueLineException e) {
                //  logger.error("Catched Exception", e);
            }
            _documentServer = null;

        }
    }

    /**
     * Logs on a user. After login session will be available by getSession() method.
     *
     * @param systemName
     *            The name of the organization\mandant\tenant to log on to.
     * @param userName
     *            The name of the user to log on.
     * @param password
     *            The password of the user.
     * @return boolean that means was connection successful or not
     *
     * @throws BlueLineException in case of any error.
     */
    public Boolean login(String systemName, String userName, String password) throws BlueLineException {

        // Retrieve the DOXiS4 CSB organization for the systemName
        ISystem system = _documentServer.getSystem(systemName);

        // Login to the server
        ITicket ticket = _documentServer.login(system, userName, password.toCharArray());

        // If the ticker state is valid create a session object.
        // Note: Other ticket states are not handled here.
        if (ticket.isValid()) {
            _session = _documentServer.createSession(ticket);
        }

        if (_session == null) return false;
        return true;
    }

    /**
     * This methods finishes the user session.
     *
     * @throws BlueLineException
     */
    public void logout() throws BlueLineException {
        _documentServer.logout(_session);
    }

    /**
     * This method checks if server was initialized
     * @return Boolean true or false
     */
    public Boolean isInit() {
        if (_documentServer == null) {
            return false;
        }
        return true;
    }

    /**
     * This method checks if some user already logged
     * @return Boolean true or false
     */
    public Boolean isLoggedIn() {
        if (_session == null) {
            return false;
        }
        return true;
    }

    /**
     * Gets session object after login
     * @return ISession object
     * @see ISession
     */
    public ISession getSession()
    {
        return _session;
    }

    /**
     * Gets DocumentServer object
     * @return IDocumentServer object
     * @see IDocumentServer
     */
    public IDocumentServer getDocumentServer()
    {
        return _documentServer;
    }

    /**
     * Gets SerClassFactory object
     * @return ISerClassFactory object
     * @see ISerClassFactory
     */
    public ISerClassFactory getClassFactory()
    {
        return _classFactory;
    }
}
