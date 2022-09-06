package com.sersolutions.doxis4helpers.edm.diadoc;

import Diadoc.Api.DiadocApi;
import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.OrganizationProtos;
import com.ser.blueline.*;
import com.ser.blueline.signature.ISignature;
import com.ser.foldermanager.FMLinkType;
import com.ser.foldermanager.IElement;
import com.ser.foldermanager.IFolder;
import com.ser.foldermanager.INode;
import com.sersolutions.doxis4helpers.commons.Descriptors;
import com.sersolutions.doxis4helpers.commons.InformationObjects;
import com.sersolutions.doxis4helpers.edm.datatypes.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by drazdov on 25.06.2018.
 */
public class ArchiveDocumentToDoxisProcess {

    public static Logger log = LogManager.getLogger(DiadocConnector.class);
    private com.sersolutions.doxis4helpers.edm.datatypes.EDMDocument edmDocument;
    private ISession session;
    private IFolder doxisFolder;
    private String documentClassID;
    private String documentDatabase;
    private String folderClassID;
    private String folderDatabase;

    private Settings edmSettings;

    public ArchiveDocumentToDoxisProcess(ISession session, EDMDocument EDMDocument, String documentClassID, String documentDatabase)
    {
        this.session = session;
        this.edmDocument = EDMDocument;
        this.documentClassID = documentClassID;
        this.documentDatabase = documentDatabase;
    }

    public ArchiveDocumentToDoxisProcess(ISession session, Settings edmSettings)
    {
        this.session = session;
        this.edmSettings = edmSettings;

    }

    private IFolder initFolder(EDMDocument edmDocument, EDMFile edmFile) throws Exception
    {
        this.doxisFolder = InformationObjects.InitFolder(session, edmSettings.getFolderClass());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassNumber(), edmDocument.getNumber().length() > 49 ?
                edmDocument.getNumber().substring(0, 49) : edmDocument.getNumber());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassDate(), edmDocument.getDate());
        //Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassState(), edmDocument.getState());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassDirection(), edmFile.getDirection());
        //Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassDocType(), edmFile.getDocType());
        //Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassTechType(), edmFile.getTechType().toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocSenderOrgName", this.edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocSenderOrgINN", this.edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocRecieverBoxID", this.edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocRecieverOrgName", this.edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocRecieverOrgINN", this.edmDocument.getRecieverOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocEntitysIDs", edmFile.getEntityID(), true);
        Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocPackageID", edmFile.getPackageID());
        doxisFolder.commit();
        return doxisFolder;
    }

    private IFolder updateFolder(IFolder doxisFolder, EDMFile edmFile) throws Exception
    {
        if ("".equals(Descriptors.GetDescriptorValue(session, doxisFolder, edmSettings.getDescriptorClassNumber())))
            Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassNumber(), edmDocument.getNumber().length() > 49 ?
                    edmDocument.getNumber().substring(0, 49) : edmDocument.getNumber());

        if ("".equals(Descriptors.GetDescriptorValue(session, doxisFolder, edmSettings.getDescriptorClassDate())))
            Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassDate(), edmDocument.getDate());

        if ("".equals(Descriptors.GetDescriptorValue(session, doxisFolder, edmSettings.getDescriptorClassState())))
            Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassState(), edmDocument.getState());

        if ("".equals(Descriptors.GetDescriptorValue(session, doxisFolder, edmSettings.getDescriptorClassDirection())))
            Descriptors.SetDescriptorToInformationObject(session, doxisFolder, edmSettings.getDescriptorClassDirection(), edmFile.getDirection());

        boolean haveThisEntity = false;
        String[] oldEntitys = Descriptors.GetDescriptorValues(session, doxisFolder, "DiadocEntitysIDs");
        if (oldEntitys != null)
        {
            for (String entity : oldEntitys)
            {
                if (edmFile.getEntityID().equals(entity))
                {
                    haveThisEntity = true;
                    break;
                }
            }
        }
        if (!haveThisEntity)
        {
            Descriptors.SetDescriptorToInformationObject(session, doxisFolder, "DiadocEntitysIDs", edmFile.getEntityID(), true);
        }

        doxisFolder.commit();
        return doxisFolder;
    }

    public IInformationObject[] FindThisFileInDoxis(EDMEventRecord edmFile)
    {
        return FindThisFileInDoxis(edmFile.getEntityID(), edmFile.getEventID(), edmFile.getParentEntityID(), edmFile.getRootEntityID(),
                edmFile.getDocType(),edmFile.getTechType().toString());
    }

    public IInformationObject[] FindThisFileInDoxis(String diadocEntityID, String diadocEventID, String diadocParentEntityID,
                                                    String diadocRootEntityID, String diadocDocType, String diadocTechType)
    {

        IInformationObject[] findDocuments;
        ConcurrentMap<String, String> searchFields = new ConcurrentHashMap<>();

        if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocEntityID)) searchFields.put("DiadocEntityID", diadocEntityID);
//        if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocEventID)) searchFields.put("DiadocEventID", diadocEventID);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocParentEntityID)) searchFields.put("DiadocParentEntityID", diadocParentEntityID);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocRootEntityID)) searchFields.put("DiadocRootEntityID", diadocRootEntityID);
        //if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocDocType)) searchFields.put("DiadocDocType", diadocDocType);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(diadocTechType)) searchFields.put("DiadocTechType", diadocTechType);
        return InformationObjects.FindInformationObjects(session, "", "8d56cdc9-87d3-41c0-b1fb-67ce42250631", searchFields);
    }

    private void AddFile(EDMDocument edmDocument, EDMFile edmFile) throws Exception
    {
        log.debug(String.format("Adding file with EntityID='%s', docnum='%s', docdate='%s', doctype='%s', techType='%s'", edmFile.getEntityID(), edmFile.getNumber(), edmFile.getDate(), edmFile.getDocType(), edmFile.getTechType()));
        IFolder workFolder = null;
        ConcurrentHashMap<String, String> fields = new ConcurrentHashMap<>(5);
        IInformationObject[] foundParents = null ;

        IInformationObject[] checkOldFiles = FindThisFileInDoxis(edmFile);
        if (checkOldFiles != null && checkOldFiles.length > 0)
        {
            log.debug(String.format("We have same document %d times", checkOldFiles.length));
            for (IInformationObject checkOldFile : checkOldFiles)
            {
                if (!(checkOldFile instanceof  IDocument)) continue;
                IDocument checkOldDoc = (IDocument) checkOldFile;
                IDocument newVersion = session.getDocumentServer().getNewDocumentVersion(checkOldDoc, session);
                Descriptors.CopyAllDescriptors(session, checkOldDoc, newVersion, false);
                InformationObjects.AddContentToDocument(session, newVersion, 0, checkOldDoc.getRepresentation(0).getDescription(),
                    checkOldDoc.getPartDocument(0,0).getRawData(),checkOldDoc.getPartDocument(0,0).getFilename(), false);
                newVersion.commit();
                log.trace("Doxis4 new version updated");
            }
            return;
        }

        if (edmFile.getRootEntityID() != null && !"".equals(edmFile.getRootEntityID()))
        {
            fields.put("DiadocEntitysIDs", edmFile.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmFile.getRootEntityID() != null && !"".equals(edmFile.getRootEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntityID", edmFile.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmFile.getPackageID() != null && !"".equals(edmFile.getPackageID()))
        {
            fields.clear();
            fields.put("DiadocPackageID", edmFile.getPackageID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null)
        {
            log.debug("Can't find package folder, create new one");
            workFolder = initFolder(edmDocument, edmFile);
        }
        else
        {
            log.debug("Found some package folder, use it");
            workFolder = updateFolder(workFolder, edmFile);
        }

        IDocument doxisDocument = null;

        if (edmFile.getTechType() == EDMFile.TechType.printform || edmFile.getTechType() == EDMFile.TechType.protocol)
        {
            log.debug("Sometimes we have double print forms and protocols, searching for the same documents");
            fields.clear();
            fields.put("DiadocEntityID", edmFile.getEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, "", edmSettings.getDocumentClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    log.debug(String.format("It looks like we found something by entity, count='%d', searching for same tech type", foundParents.length));
                    for (IInformationObject found : foundParents)
                    {
                        try {
                            if (!(found instanceof IDocument)) continue;;
                            IDocument checkDocument = (IDocument) found;
                            String checkTechType = Descriptors.GetDescriptorValue(session, checkDocument, edmSettings.getDescriptorClassTechType());

                            if (checkTechType.equals(edmFile.getTechType().toString()))
                            {
                                log.debug(String.format("We have same teshtype='%s', skipping", checkTechType));
                                return;

                            }
                        }catch (Exception ex) {
                            log.trace("Error on checking same documents");
                        }
                    }
                    log.debug("We can't find any double, go on");

                }
            }

        }


        doxisDocument = null;
        doxisDocument = searchForThisDocument(fields, doxisDocument, edmFile.getEntityID(), edmFile.getEventID(), edmFile.getTechType(), edmFile.getDocType());

        if (doxisDocument == null)
        {
            log.trace("Init Doxis4 document");
            doxisDocument = InformationObjects.InitDocument(session, "DiadocDocument");
        }

        log.trace("Fill descriptors of Doxis4 document");
        String fileNumber = edmFile.getNumber();
        if (StringUtils.isBlank(fileNumber)) {
            fileNumber = edmFile.getFileName();
            fileNumber = fileNumber.length() > 49 ? fileNumber.substring(0, 49) : fileNumber;
        }

        DocumentProtos.Document diadocDocument = null;
        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration-prod");
        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(), edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        diadocConnector.Connect();
        DiadocApi api = diadocConnector.GetApi();
        EDMFile.diadocApi = api;
        EDMSignature.diadocApi = api;
        OrganizationProtos.Box currentBox = DiadocConnector.GetCurrentBoxBySettings(edmSettings, diadocConnector);

        DocumentProtos.Document document = null;
        try {
            document = api.getDocumentClient().getDocument(currentBox.getBoxId(), edmFile.getMessageID(), edmFile.getEntityID());
        } catch(Exception exception){
            log.error(exception);
        }
        String fileArrivalDate = "";
        if (document != null) {
            long ticks = document.getCreationTimestampTicks();

            Date date = new Date((long) (ticks - 621355968000000000L) / 10000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            fileArrivalDate = sdf.format(date);
            int year = Integer.parseInt(fileArrivalDate.substring(6, fileArrivalDate.length()));
            if (year < 2000 || year > 2100) {
                ticks = document.getDeliveryTimestampTicks();

                date = new Date((long) (ticks - 621355968000000000L) / 10000);
                sdf = new SimpleDateFormat("dd.MM.yyyy");
                fileArrivalDate = sdf.format(date);
            }
        }
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassNumber(), fileNumber.length() > 49 ? fileNumber.substring(0, 49) : fileNumber);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDate(), edmFile.getDate());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDiadocDate(), fileArrivalDate);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassState(), EDMSignature.SignatureState.NOTSIGNED.toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDocType(), edmFile.getDocType());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassTechType(), edmFile.getTechType().toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDirection(), edmFile.getDirection());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocPackageID", edmFile.getPackageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityID", edmFile.getEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEventID", edmFile.getEventID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocMessageID", edmFile.getMessageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocParentEntityID", edmFile.getParentEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRootEntityID", edmFile.getRootEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgName", edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgINN", edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverBoxID", edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgName", edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgINN", edmDocument.getRecieverOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocTotalValue", edmDocument.getTotal());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocVatValue", edmDocument.getVat());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocHierarhyLevel", edmFile.getHierarchy());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityAuthor", edmFile.getAuthor());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityTarget", edmFile.getTarget());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRequestType", edmFile.getRequesttype());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSignerBoxID", edmFile.getSignerBoxID());

        INode folderNode = null;

        if (edmFile.getTechType() == EDMFile.TechType.original ||
                edmFile.getTechType() == EDMFile.TechType.printform ||
                edmFile.getTechType() == EDMFile.TechType.protocol)
        {

            log.trace("Adding file as normal document");
            folderNode = workFolder.getNodeByID(edmSettings.getFolderNodeDocuments());
            InformationObjects.AddContentToDocument(session, doxisDocument, 0, "Документ", edmFile.getContent(), edmFile.getFileName(), false);
            log.trace("Archive Doxis4 document");
            session.getDocumentServer().archiveDocument(doxisDocument, session);

        }
        else
        {
            log.trace("Adding file as tech document");
            folderNode = workFolder.getNodeByID(edmSettings.getFolderNodeTech());
            InformationObjects.AddContentToDocument(session, doxisDocument, 0, "Технические данные", edmFile.getContent(), edmFile.getFileName(), false);
            log.trace("Archive Doxis4document");
            session.getDocumentServer().archiveDocument(doxisDocument, session);
        }
        doxisDocument.commit();

        log.debug("File archived as Doxis4 document successfully");
        try
        {
            folderNode.getElements().addNew(FMLinkType.DOCUMENT).setLink(doxisDocument.getID());
            workFolder.commit();
            log.trace("Doxis4 document was added to the package");
        }
        catch (Exception ex)
        {
            workFolder.refresh(true);
        }
    }

    private void AddSignature(EDMDocument edmDocument, EDMSignature edmSignature) throws Exception
    {
        IFolder workFolder = null;
        log.debug(String.format("Adding signature with EntityID='%s', docnum='%s', docdate='%s'", edmSignature.getEntityID(), edmSignature.getNumber(), edmSignature.getDate()));

        ConcurrentHashMap<String, String> fields = new ConcurrentHashMap<>(5);
        IInformationObject[] foundParents = null;
        List<IDocument> toLinkList = new ArrayList();

        IInformationObject[] checkOldFiles = FindThisFileInDoxis(edmSignature);
        if (checkOldFiles != null && checkOldFiles.length > 0)
        {
            log.debug(String.format("We have same document %d times", checkOldFiles.length));
            for (IInformationObject checkOldFile : checkOldFiles)
            {
                if (!(checkOldFile instanceof  IDocument)) continue;
                IDocument checkOldDoc = (IDocument) checkOldFile;
                IDocument newVersion = session.getDocumentServer().getNewDocumentVersion(checkOldDoc, session);
                Descriptors.CopyAllDescriptors(session, checkOldDoc, newVersion, false);
                InformationObjects.AddContentToDocument(session, newVersion, 0, checkOldDoc.getRepresentation(0).getDescription(),
                        checkOldDoc.getPartDocument(0,0).getRawData(),checkOldDoc.getPartDocument(0,0).getFilename(), false);
                newVersion.commit();
                log.trace("Doxis4 new version updated");
            }
            return;
        }

        if (edmSignature.getRootEntityID() != null && !"".equals(edmSignature.getRootEntityID()))
        {
            fields.put("DiadocEntitysIDs", edmSignature.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmSignature.getRootEntityID() != null && !"".equals(edmSignature.getRootEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntityID", edmSignature.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmSignature.getPackageID() != null && !"".equals(edmSignature.getPackageID()))
        {
            fields.clear();
            fields.put("DiadocPackageID", edmSignature.getPackageID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }

        IDocument doxisDocument = null;
        List<IDocument> parentsList = null;
        if (foundParents != null) {
            parentsList = new ArrayList<>(foundParents.length + 5);
        } else {
            parentsList = new ArrayList<>(5);
        }
        if (edmSignature.getParentEntityID() != null && !"".equals(edmSignature.getParentEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntityID", edmSignature.getParentEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, "", edmSettings.getDocumentClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    for (IInformationObject foundParent : foundParents){
                        if (foundParent instanceof IDocument) {
                            parentsList.add((IDocument) foundParent);
                        }
                    }
                }
            }
        }
        INode folderNode = null;

        if (foundParents != null) {
            log.trace(String.format("Found %d parents as Doxis4 documents", foundParents.length));
            for (IInformationObject parent : foundParents) {
                if (parent == null) continue;
                if (!(parent instanceof IDocument)) continue;
                doxisDocument = (IDocument) parent;
                if (Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityID").equalsIgnoreCase(Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocRootEntityID")))
                    continue;
                List<String> cancelList = new ArrayList<>();
                cancelList.add(EDMEventRecord.TechType.cancellation.toString().toLowerCase());
                cancelList.add(EDMEventRecord.TechType.xmlSignatureRejection.toString().toLowerCase());
                cancelList.add(EDMEventRecord.TechType.signatureRequestRejection.toString().toLowerCase());

                String descriptorTechType = Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassTechType()).toLowerCase();
                String descriptorDocType = Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassDocType()).toLowerCase();

                if (!cancelList.contains(descriptorDocType) && !cancelList.contains(descriptorTechType)) {
                    fields.clear();
                    fields.put("DiadocEntityID", edmSignature.getParentEntityID());
                    IInformationObject[] foundParentsForSignature = InformationObjects.FindInformationObjects(session, "", edmSettings.getDocumentClass(), fields);
                    if (foundParentsForSignature != null) {
                        if (foundParentsForSignature.length > 0) {
                            for (IInformationObject foundParentForSignature : foundParentsForSignature) {
                                if (foundParentForSignature instanceof IDocument) {
                                    boolean needAdd = true;
                                    for (IDocument doc : parentsList) {
                                        if (foundParentForSignature.getDocumentID().equals(doc.getDocumentID())) {
                                            needAdd = false;
                                        }
                                    }
                                    if (needAdd) {
                                        parentsList.add((IDocument) foundParentForSignature);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            parentsList = new ArrayList<>(5);
        }

        if (workFolder != null)
        {

            folderNode = workFolder.getNodeByID(edmSettings.getFolderNodeDocuments());
            log.trace(String.format("Found Doxis4 package, there is %d documents inside", folderNode.getElements().getCount2()));
            for (int elem = 0; elem < folderNode.getElements().getCount2(); elem++)
            {
                IElement element = folderNode.getElements().getItem2(elem);
                if (element.getLinkType() == FMLinkType.DOCUMENT)
                {
                    try
                    {
                        boolean alreadyhave = false;
                        for (IDocument parent : parentsList)
                        {
                            if (parent.getDocumentID().getID().equals(element.getLink()))
                            {
                                alreadyhave = true;
                                break;
                            }
                        }
                        if (alreadyhave) continue;
                        IDocument loadDocument = session.getDocumentServer().getDocument4ID(element.getLink(), session);
                        if (loadDocument != null)
                        {
                            parentsList.add(loadDocument);
                        }
                    }catch (Exception ex) {}
                }
            }
        }



        IDocument mainParent = null;
        if (parentsList != null)
        {
            ISignature newsig = null;
            log.trace(String.format("Parent list was formed, there is %d documents", parentsList.size()));
            for (IDocument parent : parentsList)
            {
                try {
                    doxisDocument = parent;

                    if (!(Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityID").equalsIgnoreCase(edmSignature.getParentEntityID())))
                    {
                        continue;
                    }

                    if (edmSignature.getSignerBoxID().equalsIgnoreCase("00000000000000000000000000000000@diadoc.ru") &&
                        !Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityID").equalsIgnoreCase(edmSignature.getParentEntityID())) continue;

                    boolean haveThisSignature =false;
                    boolean haveOthersideSignatures =false;

                    toLinkList.add(doxisDocument);

                    for (IRepresentation representation : doxisDocument.getRepresentationList())
                    {
                        newsig = session.getDocumentServer().getSignatureInstance(1, 1, edmSignature.getContent(), new Date());
                        if (newsig == null) continue;

                        if (!haveOthersideSignatures)
                        {
                            ISignature[] oldSignatures = representation.getSignatures();
                            if (oldSignatures != null)
                            {
                                for (ISignature oldSignature : oldSignatures)
                                {
                                    if (oldSignature == null) continue;
                                    if (!newsig.getIssuerName().equals(oldSignature.getIssuerName()) || !newsig.getSubjectName().equals(oldSignature.getSubjectName()))
                                    {
                                        haveOthersideSignatures = true;
                                        break;
                                    }
                                    else
                                    {
                                        if (newsig.getSignature() != null && Arrays.equals(newsig.getSignature(), oldSignature.getSignature()))
                                        {
                                            haveThisSignature = true;
                                            break;
                                        }
                                        if (newsig.getIssuerName().equals(oldSignature.getIssuerName()) && newsig.getSubjectName().equals(oldSignature.getSubjectName()))
                                        {
                                            haveThisSignature = true;
                                            break;
                                        }
                                    }
                                }
                            }

                        }

                        if (!haveThisSignature)
                        {
                            log.debug(String.format("Adding signature to the Doxis4 document: EntityID='%s', docnum='%s', docdate='%s', doctype='%s', techtype='%s'",
                                    Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityID"),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassNumber()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassDate()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassDocType()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassTechType())
                                    )
                            );
                            representation.addSignature(newsig);
                            haveThisSignature = true;
                            break;
                        }
                        else
                        {
                            log.trace(String.format("Doxis4 document already have this signature: EntityID='%s', docnum='%s', docdate='%s', doctype='%s', techtype='%s'",
                                    Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityID"),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassNumber()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassDate()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassDocType()),
                                    Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassTechType())
                                    )
                            );
                        }
                    }


                    if (!EDMSignature.SignatureState.DOUBLESIGNED.toString().equals(Descriptors.GetDescriptorValue(session, doxisDocument, edmSettings.getDescriptorClassState())))
                    {
                        String newSignatureState = (haveOthersideSignatures)? EDMSignature.SignatureState.DOUBLESIGNED.toString() : EDMSignature.SignatureState.SIGNED.toString();
                        log.debug(String.format("Changing signature status in the doucment to the '%s'", newSignatureState));
                        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassState(), newSignatureState);

                    }
                    if (StringUtils.isBlank(Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocEntityAuthor"))
                    && !Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocParentEntityID").equalsIgnoreCase(
                            Descriptors.GetDescriptorValue(session, doxisDocument, "DiadocRootEntityID")
                    ))
                    {
                        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityAuthor", edmSignature.getAuthor());
                    }
                    doxisDocument.commit();

                }
                catch (Exception ex)
                {
                    log.trace(ex);
                }
            }
        }

        doxisDocument = null;
        doxisDocument = searchForThisDocument(fields, doxisDocument, edmSignature.getEntityID(), edmSignature.getEventID(), edmSignature.getTechType(), edmSignature.getDocType());

        if (doxisDocument == null)
        {
            log.trace("Init Doxis4 document");
            doxisDocument = InformationObjects.InitDocument(session, "DiadocDocument");
        }

        String filename = null;
        if (edmDocument.getFileList() != null)
        {
            for (EDMFile attachedFile : edmDocument.getFileList())
            {
                if (edmSignature.getParentEntityID().equals( attachedFile.getEntityID()))
                {
                    filename = attachedFile.getFileName();
                    int dotIndex = filename.lastIndexOf(".");
                    if (dotIndex > 0)   filename = filename.substring(0, dotIndex);
                    filename = filename + ".sgn";
                    filename = filename.length() > 49 ? filename.substring(0, 49) : filename;
                    break;
                }
            }
        }
        if (filename == null && edmDocument.getFileName() != null && edmDocument.getFileName().length() > 4)
        {
            filename = edmDocument.getFileName();
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex > 0)   filename = filename.substring(0, dotIndex);
            filename = filename + ".sgn";
            filename = filename.length() > 49 ? filename.substring(0, 49) : filename;

        }

            //log.trace("As we don't have main parent - fill descriptors by information from signature");
        log.trace("Fill descriptors for signature document ");

        String fileDate = edmDocument.getDate();
        DocumentProtos.Document diadocDocument = null;
        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration-prod");
        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(), edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        diadocConnector.Connect();
        DiadocApi api = diadocConnector.GetApi();
        EDMFile.diadocApi = api;
        EDMSignature.diadocApi = api;
        OrganizationProtos.Box currentBox = DiadocConnector.GetCurrentBoxBySettings(edmSettings, diadocConnector);
        DocumentProtos.Document document = null;
        String messageID = "";
        String entityID = "";
        if (edmDocument.getSignatureList().size() > 0) {
            messageID = edmDocument.getSignatureList().get(0).getMessageID();
            entityID = edmDocument.getSignatureList().get(0).getEntityID();
        }
        if (StringUtils.isNotBlank(messageID) && StringUtils.isNotBlank(entityID)) {
            try {
                document = api.getDocumentClient().getDocument(currentBox.getBoxId(), messageID, entityID);
            } catch (Exception exception) {
                log.error(exception);
            }

            if (StringUtils.isBlank(fileDate)) {

                if (document != null) {
                    long ticks = document.getCreationTimestampTicks();

                    Date date = new Date((long) (ticks - 621355968000000000L) / 10000);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    fileDate = sdf.format(date);
                    int year = Integer.parseInt(fileDate.substring(6, fileDate.length()));
                    if (year < 2000 || year > 2100) {
                        ticks = document.getDeliveryTimestampTicks();

                        date = new Date((long) (ticks - 621355968000000000L) / 10000);
                        sdf = new SimpleDateFormat("dd.MM.yyyy");
                        fileDate = sdf.format(date);
                    }
                }
            }
        }

        String fileArrivalDate = "";
        if (document != null) {
            long ticks = document.getCreationTimestampTicks();

            Date date = new Date((long) (ticks - 621355968000000000L) / 10000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            fileArrivalDate = sdf.format(date);
            int year = Integer.parseInt(fileArrivalDate.substring(6, fileArrivalDate.length()));
            if (year < 2000 || year > 2100) {
                ticks = document.getDeliveryTimestampTicks();

                date = new Date((long) (ticks - 621355968000000000L) / 10000);
                sdf = new SimpleDateFormat("dd.MM.yyyy");
                fileArrivalDate = sdf.format(date);
            }
        }

        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassNumber(), StringUtils.isBlank(edmDocument.getNumber())? filename : (edmDocument.getNumber().length() > 49 ?
                edmDocument.getNumber().substring(0, 49) : edmDocument.getNumber()));
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDate(), edmDocument.getDate());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDiadocDate(), fileDate);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDiadocDate(), fileArrivalDate);
        //Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassState(), edmDocument.getState());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassTechType(), EDMFile.TechType.signature.toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDocType(), EDMFile.TechType.signature.toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocPackageID", edmSignature.getPackageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityID", edmSignature.getEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEventID", edmSignature.getEventID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocMessageID", edmSignature.getMessageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocParentEntityID", edmSignature.getParentEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRootEntityID", edmSignature.getRootEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgName", edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgINN", edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverBoxID", edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgName", edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgINN", edmDocument.getRecieverOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDirection(), edmSignature.getDirection());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocTotalValue", edmDocument.getTotal());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocVatValue", edmDocument.getVat());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocHierarhyLevel", edmSignature.getHierarchy());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityAuthor", edmSignature.getAuthor());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityTarget", edmSignature.getTarget());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRequestType", edmSignature.getRequesttype());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSignerBoxID", edmSignature.getSignerBoxID());

        if (filename == null)
        {
            filename = "signature.sgn";
        }

        log.trace("Add file to the Doxis4 document");
        InformationObjects.AddContentToDocument(session, doxisDocument, 0, "Технические данные", edmSignature.getContent(), filename, false);

        log.trace("Archiving Doxis4 document");
        session.getDocumentServer().archiveDocument(doxisDocument, session);
        log.debug("Doxis4 document archived with the file from Diadoc");


        if (workFolder != null)
        {
            folderNode = workFolder.getNodeByID(edmSettings.getFolderNodeTech());
            try
            {
                folderNode.getElements().addNew(FMLinkType.DOCUMENT).setLink(doxisDocument.getID());
                workFolder.commit();
                log.trace("Doxis4 document was added to the Doxis4 package");
            }
            catch (Exception ex)
            {
                workFolder.refresh(true);
            }
        }
        try {
            for (IDocument doc : toLinkList) {
                ILink relationship = session.getDocumentServer().createLink(session, doxisDocument.getID(), null, doc.getID());
                relationship.commit();
            }
        } catch (Exception ex){
            log.error(ex.getMessage());
        }
    }

    private IDocument searchForThisDocument(ConcurrentHashMap<String, String> fields, IDocument doxisDocument, String entityID, String eventID, EDMEventRecord.TechType techType, String docType) {
        IInformationObject[] foundParents;
        log.trace("Try to find this Doxis4 document");
        fields.clear();
        fields.put("DiadocEntityID", entityID);
//        fields.put("DiadocEventID", eventID);
        switch (techType)
        {
            case protocol:
            case printform:
            case original:
                fields.put("DiadocTechType", techType.toString());
            default:
                fields.put("DiadocTechType", EDMEventRecord.TechType.technicalxml.toString());
        }
        fields.put("DiadocDocType", docType);
        foundParents = InformationObjects.FindInformationObjects(session, "", edmSettings.getDocumentClass(), fields);
        if (foundParents != null && foundParents.length > 0) {
            for (IInformationObject found : foundParents) {
                if (!(found instanceof IDocument)) continue;
                log.trace("Found this Doxis4 document");
                doxisDocument = (IDocument) found;
                doxisDocument = session.getDocumentServer().getNewDocumentVersion(doxisDocument, session);
                break;
            }
        }
        return doxisDocument;
    }

    private void AddEventRecord(EDMDocument edmDocument, EDMEventRecord edmEventRecord) throws Exception
    {
        log.debug(String.format("Adding Event Record with EntityID='%s', docnum='%s', docdate='%s', doctype='%s', techType='%s'", edmEventRecord.getEntityID(), edmEventRecord.getNumber(), edmEventRecord.getDate(), edmEventRecord.getDocType(), edmEventRecord.getTechType()));
        IFolder workFolder = null;
        ConcurrentHashMap<String, String> fields = new ConcurrentHashMap<>(5);
        IInformationObject[] foundParents = null ;

        IInformationObject[] checkOldFiles = FindThisFileInDoxis(edmEventRecord);
        if (checkOldFiles != null && checkOldFiles.length > 0)
        {
            log.debug(String.format("We have same document %d times", checkOldFiles.length));
            for (IInformationObject checkOldFile : checkOldFiles)
            {
                if (!(checkOldFile instanceof  IDocument)) continue;
                IDocument checkOldDoc = (IDocument) checkOldFile;
                IDocument newVersion = session.getDocumentServer().getNewDocumentVersion(checkOldDoc, session);
                Descriptors.CopyAllDescriptors(session, checkOldDoc, newVersion, false);
                //InformationObjects.AddContentToDocument(session, newVersion, 0, checkOldDoc.getRepresentation(0).getDescription(),
                //        checkOldDoc.getPartDocument(0,0).getRawData(),checkOldDoc.getPartDocument(0,0).getFilename(), false);
                newVersion.commit();
                log.trace("Doxis4 new version updated");
            }
            return;
        }

        if (edmEventRecord.getEntityID() != null && !"".equals(edmEventRecord.getEntityID()))
        {
            fields.put("DiadocEntitysIDs", edmEventRecord.getEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmEventRecord.getRootEntityID() != null && !"".equals(edmEventRecord.getRootEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntitysIDs", edmEventRecord.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmEventRecord.getRootEntityID() != null && !"".equals(edmEventRecord.getRootEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntityID", edmEventRecord.getRootEntityID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }
        if (workFolder == null && edmEventRecord.getEntityID() != null && !"".equals(edmEventRecord.getEntityID()))
        {
            fields.clear();
            fields.put("DiadocEntityID", edmEventRecord.getEntityID());
            foundParents =  InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }

        if (workFolder == null && edmEventRecord.getPackageID() != null && !"".equals(edmEventRecord.getPackageID()))
        {
            fields.clear();
            fields.put("DiadocPackageID", edmEventRecord.getPackageID());
            foundParents = InformationObjects.FindInformationObjects(session, edmSettings.getFolderClass(), edmSettings.getFolderClass(), fields);
            if (foundParents != null)
            {
                if (foundParents.length > 0)
                {
                    workFolder = (IFolder) foundParents[0];
                }
            }
        }


        IDocument doxisDocument = null;

        doxisDocument = null;
        doxisDocument = searchForThisDocument(fields, doxisDocument, edmEventRecord.getEntityID(), edmEventRecord.getEventID(), edmEventRecord.getTechType(), edmEventRecord.getDocType());

        if (doxisDocument == null)
        {
            log.trace("Init Doxis4 document");
            doxisDocument = InformationObjects.InitDocument(session, "DiadocDocument");
        }
        DocumentProtos.Document diadocDocument = null;
        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration-prod");
        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(), edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        diadocConnector.Connect();
        DiadocApi api = diadocConnector.GetApi();
        EDMFile.diadocApi = api;
        EDMSignature.diadocApi = api;
        OrganizationProtos.Box currentBox = DiadocConnector.GetCurrentBoxBySettings(edmSettings, diadocConnector);
        DocumentProtos.Document document = null;
        try {
            document = api.getDocumentClient().getDocument(currentBox.getBoxId(), edmEventRecord.getMessageID(), edmEventRecord.getEntityID());
        } catch(Exception exception){
            log.error(exception);
        }
        log.trace("Fill descriptors of Doxis4 document");
        String fileNumber = edmEventRecord.getNumber();
        fileNumber = fileNumber.length() > 49 ? fileNumber.substring(0, 49) : fileNumber;
        String fileDate = edmEventRecord.getDate();
        if (StringUtils.isBlank(fileDate)){

            if (document != null) {
                long ticks = document.getCreationTimestampTicks();

                Date date = new Date((long) (ticks - 621355968000000000L) / 10000);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                fileDate = sdf.format(date);
                int year = Integer.parseInt(fileDate.substring(6, fileDate.length()));
                if (year < 2000 || year > 2100) {
                    ticks = document.getDeliveryTimestampTicks();

                    date = new Date((long) (ticks - 621355968000000000L) / 10000);
                    sdf = new SimpleDateFormat("dd.MM.yyyy");
                    fileDate = sdf.format(date);
                }
            }
        }

        String fileArrivalDate = "";
        if (document != null) {
            long ticks = document.getCreationTimestampTicks();

            Date date = new Date((long) (ticks - 621355968000000000L) / 10000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            fileArrivalDate = sdf.format(date);
            int year = Integer.parseInt(fileArrivalDate.substring(6, fileArrivalDate.length()));
            if (year < 2000 || year > 2100) {
                ticks = document.getDeliveryTimestampTicks();

                date = new Date((long) (ticks - 621355968000000000L) / 10000);
                sdf = new SimpleDateFormat("dd.MM.yyyy");
                fileArrivalDate = sdf.format(date);
            }
        }

        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassNumber(), fileNumber);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDate(), edmEventRecord.getDate());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDiadocDate(), fileDate);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDiadocDate(), fileArrivalDate);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassState(), EDMSignature.SignatureState.NOTSIGNED.toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDocType(), edmEventRecord.getDocType());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassTechType(), edmEventRecord.getTechType().toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, edmSettings.getDescriptorClassDirection(), edmEventRecord.getDirection());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocPackageID", edmEventRecord.getPackageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityID", edmEventRecord.getEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEventID", edmEventRecord.getEventID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocMessageID", edmEventRecord.getMessageID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocParentEntityID", edmEventRecord.getParentEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRootEntityID", edmEventRecord.getRootEntityID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgName", edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgINN", edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverBoxID", edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgName", edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgINN", edmDocument.getRecieverOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocTotalValue", edmDocument.getTotal());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocVatValue", edmDocument.getVat());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocHierarhyLevel", edmEventRecord.getHierarchy());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityAuthor", edmEventRecord.getAuthor());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocEntityTarget", edmEventRecord.getTarget());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRequestType", edmEventRecord.getRequesttype());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSignerBoxID", edmEventRecord.getSignerBoxID());

        INode folderNode = null;


        log.trace("Archive Doxis4document");
        session.getDocumentServer().archiveDocument(doxisDocument, session);
        doxisDocument.commit();

        log.debug("File archived as Doxis4 document successfully");
        try
        {

            if (workFolder != null)
            {
                log.trace("Adding file as tech document");
                folderNode = workFolder.getNodeByID(edmSettings.getFolderNodeEvents());
                folderNode.getElements().addNew(FMLinkType.DOCUMENT).setLink(doxisDocument.getID());
                workFolder.commit();
                log.trace("Doxis4 document was added to the package");
            }
        }
        catch (Exception ex)
        {
            //workFolder.refresh(true);
        }
    }


    public void Archive(EDMDocument edmDocument) throws Exception
    {
        this.edmDocument = edmDocument;
        if (this.edmDocument.getFileList() != null)
        {
            for (EDMFile edmFile :  edmDocument.getFileList())
            {
                AddFile(edmDocument, edmFile);
            }
        }

        if (this.edmDocument.getEventRecordsList() != null)
        {
            for (EDMEventRecord edmEventRecord : edmDocument.getEventRecordsList())
            {
                AddEventRecord(edmDocument, edmEventRecord);
            }
        }

        if (this.edmDocument.getSignatureList() != null)
        {
            for (EDMSignature edmSignature : edmDocument.getSignatureList())
            {
                AddSignature(edmDocument, edmSignature);
            }
        }
    }
/*
    public void ArchiveAllInOne() throws  Exception
    {
        IDocument doxisDocument = InformationObjects.InitDocument(session, documentClassID);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocNumber, edmDocument.getNumber());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocDate, edmDocument.getDate());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocState, edmDocument.getState());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocType, edmDocument.getType().toString());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgName", edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgINN", edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverBoxID", edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgName", edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgINN", edmDocument.getRecieverOrgINN());

        for (EDMFile edmFile :  edmDocument.getFileList())
        {
            if (edmFile.getTechType() == EDMFile.TechType.original)
            {
                InformationObjects.AddContentToDocument(session, doxisDocument, 0, "Оригинал", edmFile.getContent(), edmFile.getFileName(), false);
            }
            else if (edmFile.getTechType() == EDMFile.TechType.technicalxml)
            {
                InformationObjects.AddContentToDocument(session, doxisDocument, 1, "Технические данные", edmFile.getContent(), edmFile.getFileName(), false);
            }

        }


        if (edmDocument.getSignatureList() != null)
        {
            for (EDMSignature edmSignature : edmDocument.getSignatureList())
            {
                    ISignature newsig = session.getDocumentServer().getSignatureInstance(1, 1, edmSignature.getContent(), new Date());
                    doxisDocument.getRepresentation(0).addSignature(newsig);
            }
        }


        session.getDocumentServer().archiveDocument(doxisDocument, session);
    }

    public void oldArchive() throws  Exception
    {
        IDocument doxisDocument = InformationObjects.InitDocument(session, documentClassID);
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocNumber, edmDocument.getNumber());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocDate, edmDocument.getDate());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, descriptorDocState, edmDocument.getState());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderBoxID", edmDocument.getSenderBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgName", edmDocument.getSenderOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocSenderOrgINN", edmDocument.getSenderOrgINN());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverBoxID", edmDocument.getRecieverBoxID());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgName", edmDocument.getRecieverOrgName());
        Descriptors.SetDescriptorToInformationObject(session, doxisDocument, "DiadocRecieverOrgINN", edmDocument.getRecieverOrgINN());

        InformationObjects.SetContentToDocument(session, doxisDocument, edmDocument.getContent(), edmDocument.getFileName(), false);

        if (edmDocument.haveSignature())
        {
            ISignature newsig = session.getDocumentServer().getSignatureInstance(0, 0, edmDocument.getSignature(), new Date());
            doxisDocument.getRepresentation(0).addSignature(newsig);
        }


        session.getDocumentServer().archiveDocument(doxisDocument, session);
    }
*/
}
