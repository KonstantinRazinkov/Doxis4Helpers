package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.metaDataComponents.*;
import com.ser.foldermanager.IFolder;
import com.ser.sedna.client.bluelineimpl.SEDNABluelineAdapterFactory;
import com.sersolutions.doxis4helpers.commons.types.ContentFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Class for working with Doxis4 Information objects
 */
public class InformationObjects {
    /**
     * Get all content from Information object
     * @param source Information object
     *               @see IInformationObject
     *               @see IDocument
     * @return List of Content files
     * @see ContentFile
     * @throws Exception if there was no IDocument object
     */
    public static List<ContentFile> GetAllContent(IInformationObject source) throws Exception {
        List<ContentFile> result = new ArrayList<>();
        if ((source instanceof IDocument) == false) {
            throw new Exception("Can't get content from non-document InformationObject type");
        }
        IDocument doc = (IDocument) source;
        for (int representation = 0; representation < doc.getRepresentationCount(); representation++) {
            IRepresentation docRepresentation = doc.getRepresentation(representation);

            for (int part = 0; part < docRepresentation.getPartDocumentCount(); part++) {
                IDocumentPart docPart =  docRepresentation.getPartDocument(part);
                ContentFile contentFile = new ContentFile(docPart.getFilename(),docPart.getRawData(), docPart.getMimeType(), docRepresentation.getType(), docRepresentation.getSignatures());
                result.add(contentFile);
            }
        }
        return result;
    }


    /**
     * Init new Document object
     * @param session Doxis4 Session object
     *                @see ISession
     * @param className Archive class name, FQN or ID
     * @return ready to use IDocument object
     * @see IDocument
     * @throws Exception if there will be some problems with getting Archive class of document or archive dialog "default"
     */
    public static IDocument InitDocument(ISession session, String className) throws Exception {
        IArchiveClass archiveClass;
        IArchiveDlg archiveDlg;

        archiveClass = Classes.GetArchiveClass(session, className);

        archiveDlg  = archiveClass.getArchiveDlg("default");

        IDocument doc = session.getDocumentServer().getClassFactory().getDocumentInstance(archiveDlg, session);
        return doc;
    }

    /**
     * Init new Folder object
     * @param session Doxis4 Session object
     *                @see ISession
     * @param className Archive class name, FQN or ID
     * @return ready to use IDocument object
     * @see IDocument
     * @throws Exception if there will be some problems with getting Archive class of document or archive dialog "default"
     */
    public static IFolder InitFolder(ISession session, String className) throws Exception {
        IArchiveFolderClass archiveClass;
        IArchiveDlg archiveDlg;

        archiveClass = Classes.GetArchiveFolderClass(session, className);
        archiveDlg  = archiveClass.getArchiveDlg("default");

        IFolder newFolder = session.getFolderConnection().createFolder();
        newFolder.init(archiveClass);
        return newFolder;
    }


    /**
     * Add content to Doxis4 Document and removes all others contents if it was settled before
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param doc Document object
     *            @see IDocument
     * @param content byte array with file to be added to Document
     * @param fileName file name
     * @param enableFulltext enable fulltext for this file
     * @return same Document object
     * @throws Exception if there will be some problems while adding new content to document
     */
    public static IDocument SetContentToDocument(ISession session, IDocument doc, byte[] content, String fileName, boolean enableFulltext) throws Exception {

        if (doc.getRepresentationCount() == 0) doc.addRepresentation("Representation 1", "Representation 1");
        IDocumentImportFilter filter = session.getDocumentServer().getDocumentImportFilter(IDocumentImportFilter.FILE);

        filter.init(new ByteArrayInputStream(content), fileName);

        // Retrieve a content object from this filter. (Note: The filter
        // might
        // return multiple
        // content objects, so better use a loop).
        IDocumentPart docPart = filter.getNextDocumentPart();

        docPart.setFulltext(enableFulltext);
        // Now add the content object to the document.
        if (doc.getRepresentation(0).getPartDocumentCount() > 0)
        {
            doc.getRepresentation(0).removeAllPartDocuments();
        }
        doc.getRepresentation(0).addPartDocument(docPart);
        // Close the filter
        filter.close();

        return doc;
    }

    /**
     * Add content to Doxis4 Document
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param doc Document object
     *            @see IDocument
     * @param representationNo number of representationNo.
     * @param representationName name of this representationNo
     * @param content byte array with file to be added to Document
     * @param fileName file name
     * @param enableFulltext enable fulltext for this file
     * @return same Document object
     * @throws Exception if there will be some problems while adding new content to document
     */
    public static IDocument AddContentToDocument(ISession session, IDocument doc, int representationNo, String representationName, byte[] content, String fileName, boolean enableFulltext) throws Exception {
        if (representationNo < 0) representationNo = 0;

        IRepresentation representation = null;
        if (doc.getRepresentationCount() <= representationNo) {
            doc.addRepresentation(representationName, representationName);
        }
        representation = doc.getRepresentation(representationNo);

        IDocumentImportFilter filter = session.getDocumentServer().getDocumentImportFilter(IDocumentImportFilter.FILE);

        filter.init(new ByteArrayInputStream(content), fileName);

        // Retrieve a content object from this filter. (Note: The filter
        // might
        // return multiple
        // content objects, so better use a loop).
        IDocumentPart docPart = filter.getNextDocumentPart();

        docPart.setFulltext(enableFulltext);
        // Now add the content object to the document.

        representation.addPartDocument(docPart);
        // Close the filter
        filter.close();

        return doc;
    }


    /**
     * Find information objects with using of default query dialog, list of descriptor names and information object
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param archiveClassID Name, FQN or ID of Archive [Folder] class
     * @param queryClassID Name, FQN or ID of Query class
     * @param fields Map of Name of Field - Value of descriptors that must be used for searching
     * @return array of information objects that will be found. Returns null if nothing found.
     */
    public static IInformationObject[] FindInformationObjects(ISession session, String archiveClassID, String queryClassID, ConcurrentMap<String, String> fields) {
        try {
            List<IValueDescriptor> descriptorsFields = new ArrayList<>();
            IDescriptor descriptorDef;
            IValueDescriptor descriptorValue;

            for (Map.Entry<String, String> field : fields.entrySet()) {
                String value = field.getValue();
                descriptorDef =  Descriptors.GetDescriptorDefinition(session, field.getKey());
                if (descriptorDef != null) {
                    ISerClassFactory factory = session.getDocumentServer().getClassFactory();
                    IValueDescriptor valueDescriptor = factory.getValueDescriptorInstance(descriptorDef);
                    if (valueDescriptor != null) {
                        valueDescriptor.setValue(value);
                        descriptorsFields.add(valueDescriptor);
                    }
                }
            }
            return FindInformationObjects(session, archiveClassID, queryClassID, descriptorsFields);
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * Find information objects with using of default query dialog, list of descriptor names and information object
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param archiveClassID Name, FQN or ID of Archive [Folder] class
     * @param queryClassID Name, FQN or ID of Query class
     * @param fields names of descriptors of information object, that must be used for searching
     * @param informationObject information object with values
     *                          @see IInformationObject
     * @return array of information objects that will be found. Returns null if nothing found.
     */
    public static IInformationObject[] FindInformationObjects(ISession session, String archiveClassID, String queryClassID, List<String> fields, IInformationObject informationObject) {
        try {
            List<IValueDescriptor> descriptorsFields = new ArrayList<>();
            IDescriptor descriptorDef;
            IValueDescriptor descriptorValue;

            for (String field : fields) {
                descriptorDef = Descriptors.GetDescriptorDefinition(session, field);
                if (descriptorDef != null) {
                    descriptorValue = informationObject.getDescriptor(descriptorDef);
                    if (descriptorValue != null) {
                        descriptorsFields.add(descriptorValue);
                    }
                }
            }

            return FindInformationObjects(session, archiveClassID, queryClassID, descriptorsFields);
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * Find information objects with using of default query dialog and list of Value descriptors
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param archiveClassID Name, FQN or ID of Archive [Folder] class
     * @param queryClassID Name, FQN or ID of Query class
     * @param fields value descriptors of information object
     *               @see IValueDescriptor
     * @return array of information object that will be found. Returns null if nothing found.
     */
    public static IInformationObject[] FindInformationObjects(ISession session, String archiveClassID, String queryClassID, List<IValueDescriptor> fields) {
        try {
            IDocumentServer documentServer = session.getDocumentServer();
             IArchiveClass archiveClass = Classes.GetArchiveClass(session, archiveClassID);
            IArchiveFolderClass archiveFolderClass = Classes.GetArchiveFolderClass(session, archiveClassID);
            String typeID = (archiveClass != null)? archiveClass.getID() : ((archiveFolderClass != null)? archiveFolderClass.getID() : "");
            StringBuilder sql = new StringBuilder();
            if (!"".equals(typeID)) {
                sql.append( String.format("TYPE = '%s' ", typeID));
            } else {
                sql.append("1 = 1");
            }
            for (IValueDescriptor field : fields) {
                if (field.getDescriptor().getMultiValueType() == 0) {
                    sql.append(String.format("AND %s = '%s' ", field.getName().toUpperCase(), field.getStringValues()[0].replaceAll("'", "''")));
                } else {
                    sql.append(String.format("AND %s LIKE '*%s*' ", field.getName().toUpperCase(), field.getStringValues()[0].replaceAll("'", "''")));
                }
            }
            return FindInformationObjects(session, archiveClassID, queryClassID, sql.toString());
        } catch (Exception ex) {

        }
        return  null;
    }

    /**
     * Find information objects with using of default query dialog and sql query
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param archiveClassID Name, FQN or ID of Archive [Folder] class
     * @param queryClassID Name, FQN or ID of Query class
     * @param sql sql-query with params of searching
     * @return array of information object that will be found. Returns null if nothing found.
     */
    public static IInformationObject[] FindInformationObjects(ISession session, String archiveClassID, String queryClassID, String sql) {
        try {
            IDocumentServer documentServer = session.getDocumentServer();
            IQueryClass queryClass = Classes.GetQueryClass(session, queryClassID);

            ISerClassFactory sednaFactory = SEDNABluelineAdapterFactory.getInstance();
            IQueryExpression queryExpression = sednaFactory.getExpressionInstance(sql);

            IQueryParameter qPar = sednaFactory.getQueryParameterInstance(session, queryClass.getQueryDlg("default"), queryExpression);
            //qPar.setHitLimit(20);
            qPar.setCurrentVersionOnly(true);

            IDocumentHitList hits =  session.getDocumentServer().query(qPar, session);

            IInformationObject[] documents= hits.getInformationObjects();

            return documents;

        } catch (Exception ex) {

        }

        return  null;

    }

    /**
     * Which method must be used for searching of information objects
     */
    public static enum FindInformationObjectsBySQLType {
        ByDBName,
        ByQueryDialogID
    }

    /**
     * Find information objects with using of ready SQL-query
     * @param session Doxis4 Session Object
     *                @see ISession
     * @param archiveType Type of searching information object
     *                    @see FindInformationObjectsBySQLType
     * @param archiveName Name of DB Name
     * @param sql SQL query
     * @param hitLimit limit of documents count. -1 for default value
     * @return Array of founded Information object
     * @throws Exception if there are some problems with searching
     */
    public static IInformationObject[] FindInformationObjectsBySQL(ISession session, FindInformationObjectsBySQLType archiveType, String archiveName, String sql, int hitLimit) throws Exception {
        ISerClassFactory sednaFactory = SEDNABluelineAdapterFactory.getInstance();
        IQueryExpression queryExpression = sednaFactory.getExpressionInstance(sql);
        IQueryParameter qPar = null;
        switch (archiveType) {
            case ByDBName:
                String[] dbs={archiveName};
                qPar = sednaFactory.getQueryParameterInstance(dbs, queryExpression, null, null);
                break;
            case ByQueryDialogID:
                IQueryDlg dlg = session.getDocumentServer().getQueryClass(archiveName, session).getQueryDlg("webCube-Query");
                if (dlg == null) {
                    dlg = session.getDocumentServer().getQueryClass(archiveName, session).getQueryDlgs()[0];
                }
                qPar = sednaFactory.getQueryParameterInstance(session, dlg, queryExpression);
                break;
        }

        if (hitLimit != -1) qPar.setHitLimit(hitLimit);

        return  session.getDocumentServer().query(qPar, session).getDocuments();
    }

    public static Date FindScheduledDateFromRoots(ITask task, Date lowestDate) {
        Date currentDate=null;
        if (lowestDate == null) {
            lowestDate = task.getProcessInstance().findRootTask().getScheduledEndDate();
        }
        if (task == null) return null;
        if (task.getScheduledEndDate() != null) return task.getScheduledEndDate();
        ITask previousTask = null;
        Long prevTaskNum = task.getPreviousTaskNumericID();
        if (prevTaskNum != null) {
            IProcessInstance processInstance = task.getProcessInstance();
            previousTask = processInstance.findTaskByNumericID(prevTaskNum);

        }

        if (previousTask == null) {
            previousTask = task.getLoadedParentTask();
        }
        if (previousTask != null) {
            if (previousTask.getScheduledEndDate() != null) {
                currentDate = previousTask.getScheduledEndDate();
                if (lowestDate == null) lowestDate = currentDate;
                if (lowestDate.compareTo(currentDate) > 0) lowestDate = currentDate;
            }

            currentDate = FindScheduledDateFromRoots(previousTask, lowestDate);
            if (currentDate != null) lowestDate = currentDate;
        }
        return lowestDate;
    }

}
