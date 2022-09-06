package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.IArchiveClass;
import com.ser.blueline.metaDataComponents.IArchiveFolderClass;
import com.ser.blueline.metaDataComponents.IQueryClass;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class with static functions to hold and get several kind of classes from Doxis4
 * @see IArchiveClass
 * @see IArchiveFolderClass
 * @see IQueryClass
 */
public class Classes {
    static ConcurrentMap<String, IArchiveClass> archiveClasses = null;
    static ConcurrentMap<String, IArchiveFolderClass> archiveFolderClasses = null;
    static ConcurrentMap<String, IQueryClass> queryClasses = null;

    /**
     * Gets all archive classes, that were requested before
     * @return Concurrent map of all archive classes
     * @see IArchiveClass
     * @see ConcurrentMap
     */
    static ConcurrentMap<String, IArchiveClass> getArchiveClasses() {
        if (archiveClasses == null) archiveClasses = new ConcurrentHashMap<>();
        return archiveClasses;
    }

    /**
     * Gets all archive folder classes, that were requested before
     * @return Concurrent map of all archive folder classes
     * @see IArchiveFolderClass
     * @see ConcurrentMap
     */
    static ConcurrentMap<String, IArchiveFolderClass> getArchiveFolderClasses() {
        if (archiveFolderClasses == null) archiveFolderClasses = new ConcurrentHashMap<>();
        return archiveFolderClasses;
    }

    /**
     * Gets all query classes, that were requested before
     * @return Concurrent map of all query classes
     * @see IQueryClass
     * @see ConcurrentMap
     */
    static ConcurrentMap<String, IQueryClass> getQueryClasses() {
        if (queryClasses == null) queryClasses = new ConcurrentHashMap<>();
        return queryClasses;
    }

    /**
     * Gets archive class from Doxis4 by class name or ID<p>After class will be requested - it will be stored in ConcurrentMap and for the next time it will be taken from that map</p>
     * @param session Doxis4 Session object
     *                @see ISession
     * @param className Class name or ID
     * @return Archive class object. Will return null if class will not be found
     * @see IArchiveClass
     * @throws Exception if there will be some errors with connection to Doxis4
     */
    public static IArchiveClass GetArchiveClass(ISession session, String className) throws Exception {
        IArchiveClass archiveClass;
        if (getArchiveClasses().containsKey(className)) {
            archiveClass = getArchiveClasses().get(className);
        } else {
            archiveClass = session.getDocumentServer().getArchiveClassByName(session, className);
            if (archiveClass == null) {
                archiveClass = session.getDocumentServer().getArchiveClass( className, session);
            }
        }
        return archiveClass;
    }

    /**
     * Gets archive folder class from Doxis4 by class name or ID<p>After class will be requested - it will be stored in ConcurrentMap and for the next time it will be taken from that map</p>
     * @param session Doxis4 Session object
     *                @see ISession
     * @param className Class name or ID
     * @return Archive folder class object. Will return null if class will not be found
     * @see IArchiveFolderClass
     * @throws Exception if there will be some errors with connection to Doxis4
     */
    public static IArchiveFolderClass GetArchiveFolderClass(ISession session, String className) throws Exception {
        IArchiveFolderClass archiveClass;
        if (getArchiveClasses().containsKey(className)) {
            archiveClass = getArchiveFolderClasses().get(className);
        } else {
            archiveClass = session.getDocumentServer().getArchiveFolderClassByName(session, className);
            if (archiveClass == null) {
                archiveClass = session.getDocumentServer().getArchiveFolderClass(className, session);
            }
        }
        return archiveClass;
    }

    /**
     * Gets query class from Doxis4 by class name or ID<p>After class will be requested - it will be stored in ConcurrentMap and for the next time it will be taken from that map</p>
     * @param session Doxis4 Session object
     *                @see ISession
     * @param className Class name or ID
     * @return Query class object. Will return null if class will not be found
     * @see IQueryClass
     * @throws Exception if there will be some errors with connection to Doxis4
     */
    public static IQueryClass GetQueryClass(ISession session, String className) throws Exception {
        IQueryClass queryClass;
        if (getArchiveClasses().containsKey(className)) {
            queryClass = getQueryClasses().get(className);
        } else {
            queryClass = session.getDocumentServer().getQueryClassByName(session, className);
            if (queryClass == null) {
                queryClass = session.getDocumentServer().getQueryClass(className, session);
            }
        }
        return queryClass;
    }
}
