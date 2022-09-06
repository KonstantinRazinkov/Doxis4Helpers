package com.sersolutions.doxis4helpers.edm.diadoc;

import Diadoc.Api.DiadocApi;
import Diadoc.Api.print.models.DocumentProtocolResult;
import Diadoc.Api.print.models.PrintFormResult;
import Diadoc.Api.document.DocumentsFilter;
import Diadoc.Api.Proto.Documents.DocumentListProtos;
import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.Employees.EmployeeProtos;
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;
import Diadoc.Api.Proto.OrganizationProtos;

import Diadoc.Api.Proto.UserProtos;
import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;
import com.ser.foldermanager.IFolder;
import com.sersolutions.doxis4helpers.commons.Descriptors;
import com.sersolutions.doxis4helpers.commons.GlobalValueLists;
import com.sersolutions.doxis4helpers.commons.InformationObjects;
import com.sersolutions.doxis4helpers.edm.datatypes.*;
import com.sersolutions.doxis4helpers.edm.IEDMConnector;
import com.sersolutions.doxis4helpers.edm.diadoc.objects.SendMessageProcess;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * Created by drazdov on 22.06.2018.
 */
public class DiadocConnector implements IEDMConnector {
    public static Logger log = LogManager.getLogger(DiadocConnector.class);

    private DiadocApi api;

    private String apiURL;
    private String apiKey;
    private String login;
    private String pass;
    private String cert;
    private Boolean isConnected;

    ConcurrentMap<OrganizationProtos.Organization, List<UserProtos.UserV2>> organizationUsers;


    public DiadocApi GetApi() {return api;}
    public Boolean IsConnected() {return isConnected;}
    public static void SelfCheck(DiadocConnector connector) throws Exception
    {
        if (connector == null)
        {
            throw new Exception("Diadoc connector is null!");
        }
        if (!connector.IsConnected())
        {
            throw new Exception("Diadoc connector not connected");
        }
    }
    public DiadocConnector(String api, String apiURL)
    {
        this.apiKey = api;
        this.apiURL = apiURL;
        this.isConnected = false;
        organizationUsers= new ConcurrentHashMap<>(10);
    }

    public DiadocConnector(String api, String apiURL, String login, String pass)
    {
        this(api,apiURL);

        this.login  = login;
        this.pass = pass;
    }
    public DiadocConnector(String api, String apiURL,  String cert)
    {
        this(api, apiURL);
        this.cert = cert;
    }

    public Boolean Connect() throws Exception
    {
        StringBuilder error = new StringBuilder();
        if (apiKey == null || "".equals(apiKey))
        {
            error.append("\nApi key is null!");
        }

        if (((login == null || "".equals(login)) && (pass == null || "".equals(pass))) )
        {
            error.append("\nLogin, pass is null!");
        }
        if (error.length() > 0) throw new Exception(error.toString());


        api = new DiadocApi(apiKey, apiURL);
        api.getAuthClient().authenticate(login, pass);
        isConnected = true;
        return true;

    }

    public List<UserProtos.UserV2>  LoadOrganizationUsers(OrganizationProtos.Organization organization) throws Exception
    {
        List<OrganizationProtos.Box> organizationBoxes = organization.getBoxesList();
        List<UserProtos.UserV2> usersList = new ArrayList<>(1000);
        for (OrganizationProtos.Box organizationBox : organizationBoxes)
        {
            EmployeeProtos.EmployeeList localEmployeeList = api.getEmployeeClient().getEmployees(organizationBox.getBoxId());
            if (localEmployeeList == null) continue;
            List<EmployeeProtos.Employee> employees = localEmployeeList.getEmployeesList();
            for (EmployeeProtos.Employee employee : employees)
            {
                usersList.add(employee.getUser());
            }
        }
        return usersList;
    }

    public enum GetOrganizationUsersLoadType
    {
        Lookup,
        LoadIfNotLoaded,
        Reload
    }

    public List<UserProtos.UserV2>  GetOrganizationUsers(OrganizationProtos.Organization organization, GetOrganizationUsersLoadType loadType) throws Exception
    {
        if (this.organizationUsers.containsKey(organization))
        {
            if (loadType == GetOrganizationUsersLoadType.Reload)
            {
                List<UserProtos.UserV2>  userList =  LoadOrganizationUsers(organization);
                this.organizationUsers.replace(organization, userList);
                return userList;
            }
            return this.organizationUsers.get(organization);
        }
        if (loadType != GetOrganizationUsersLoadType.Lookup)
        {
            List<UserProtos.UserV2>  userList =  LoadOrganizationUsers(organization);
            this.organizationUsers.put(organization, userList);
            return userList;
        }
        return null;
    }
    public void SetOrganizationUsers(OrganizationProtos.Organization organization, List<UserProtos.UserV2> organizationUsers)
    {
        if (this.organizationUsers.containsKey(organization))
        {
            this.organizationUsers.replace(organization, organizationUsers);
        }
        else
        {
            this.organizationUsers.put(organization, organizationUsers);
        }
    }

    public List<OrganizationProtos.Organization> GetOrganizations(String INN, String KPP) throws Exception
    {
        OrganizationProtos.OrganizationList organzsationList = api.getOrganizationClient().getOrganizationsByInnKpp(INN, KPP);
        if (organzsationList == null) throw new Exception("Organization list is null");
        if (organzsationList.getOrganizationsCount() == 0) throw new Exception("Organization list is 0 size");

        return organzsationList.getOrganizationsList();
    }

    public OrganizationProtos.Organization GetOrganization(String INN, String KPP) throws Exception
    {
        return GetOrganizations(INN, KPP).get(0);
    }

    public OrganizationProtos.Organization GetOrganization(String ID) throws Exception
    {
        OrganizationProtos.Organization organization = null;
        try
        {
            organization = api.getOrganizationClient().getOrganizationById(ID);

        }
        catch (Exception ex)
        {

        }
        if (organization == null)
        {
            organization = api.getOrganizationClient().getOrganizationByInn(ID);
            if (organization == null) throw new Exception("Organization is null");
        }

        return organization;
    }

    public OrganizationProtos.Box GetOrganizationBox(OrganizationProtos.Organization organization) throws Exception
    {
        if (organization.getBoxesCount() == 0)
        {
            throw new Exception("There is no boxes in this organization");
        }
        return organization.getBoxes(0);
    }

    public OrganizationProtos.Box GetOrganizationBox(String ID) throws Exception
    {
        OrganizationProtos.Organization organization = GetOrganization(ID);
        return GetOrganizationBox(organization);
    }

    public OrganizationProtos.Box GetOrganizationBox(String INN, String KPP) throws Exception
    {
        OrganizationProtos.Organization organization = GetOrganization(INN, KPP);
        return GetOrganizationBox(organization);
    }

    public OrganizationProtos.Organization GetCurrentOrganization() throws  Exception
    {
        return api.getOrganizationClient().getMyOrganizations().getOrganizations(0);
    }
    public OrganizationProtos.Box GetCurrentOrganizationBox()  throws Exception
    {
        return GetOrganizationBox(api.getOrganizationClient().getMyOrganizations().getOrganizations(0));
    }

    public String SendDocument(ISession session, String documentID, String recieverOrgID, String message, Boolean isDraft) throws Exception
    {
        return SendDocumentToDiadoc(session, documentID, recieverOrgID, message, isDraft);
    }
    public static String SendDocumentToDiadoc(ISession session, String documentID, String recieverOrgID, String message, Boolean isDraft) throws Exception
    {
        return SendDocumentToDiadoc(session, documentID, null, recieverOrgID, message, isDraft);
    }
    public static String SendDocumentToDiadoc(ISession session, String documentID, String senderBoxId, String recieverOrgID, String message, Boolean isDraft) throws Exception
    {
        IInformationObject informationObject = null;
        IProcessInstance processInstance=null;
        ITask task=null;

        try
        {
            informationObject =  session.getDocumentServer().getDocument4ID(documentID, session);
        }
        catch (Exception ex)
        {
        }

        if (informationObject == null)
        {
            try
            {
                informationObject = session.getBpmService().findProcessInstance(documentID);
            }
            catch (Exception ex)
            {
                informationObject = session.getBpmService().findTask(documentID);
            }
        }

        if (informationObject instanceof IProcessInstance)
        {
            processInstance = (IProcessInstance) informationObject;
            informationObject = session.getDocumentServer().getDocument4ID(processInstance.getMainInformationObjectID(), session);

        }
        else if (informationObject instanceof ITask)
        {
            task = (ITask) informationObject;
            informationObject = session.getDocumentServer().getDocument4ID(task.getProcessInstance().getMainInformationObjectID(), session);
        }


        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration");
      /*  String apiKey = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "APIKEY", 1);
        String url = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "URL", 1);
        String login = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "LOGIN", 1);
        String pass = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "PASS", 1);
        String archiveClasStateDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "INBOXDOCUMENTSTATEDESCRIPTOR", 1);
*/
        if (recieverOrgID == null || "".equalsIgnoreCase(recieverOrgID))
        {
            recieverOrgID = Descriptors.GetDescriptorValue(session, task, "DiadocRecieverOrgINN");
        }

        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(),edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        diadocConnector.Connect();
        SendMessageProcess sendMessageProcess = new SendMessageProcess(diadocConnector);
        sendMessageProcess.AddDocuments(informationObject);
        sendMessageProcess.SetMessage(message);

        sendMessageProcess.SetSenderBox((senderBoxId == null ||"".equals(senderBoxId))? GetCurrentBoxBySettings(edmSettings, diadocConnector) : diadocConnector.GetOrganizationBox(senderBoxId));
        sendMessageProcess.SetRecieverBox(diadocConnector.GetOrganizationBox(recieverOrgID));
        String returnResult = sendMessageProcess.Send(isDraft);

        if (processInstance != null)
        {
            Descriptors.SetDescriptorToInformationObject(session, processInstance, edmSettings.getDocumentStateDescriptor(), sendMessageProcess.GetState());
            Descriptors.SetDescriptorToInformationObject(session, processInstance,  "DiadocSenderBoxID", sendMessageProcess.GetSenderBox().getBoxId());
            Descriptors.SetDescriptorToInformationObject(session, processInstance, "DiadocSenderOrgName", sendMessageProcess.GetSenderBox().getOrganization().getFullName());
            //DoxisHelper.SetDescriptorToInformationObject(session, processInstance, "DiadocSenderOrgINN", sendMessageProcess.GetSenderBox().getOrganization().getInn());
            Descriptors.SetDescriptorToInformationObject(session, processInstance, "DiadocRecieverBoxID", sendMessageProcess.GetRecieverBox().getBoxId());
            Descriptors.SetDescriptorToInformationObject(session, processInstance, "DiadocRecieverOrgName", sendMessageProcess.GetRecieverBox().getOrganization().getFullName());
            Descriptors.SetDescriptorToInformationObject(session, processInstance, "DiadocDocumentID", returnResult);
            //DoxisHelper.SetDescriptorToInformationObject(session, processInstance, "DiadocRecieverOrgINN", sendMessageProcess.GetRecieverBox().getOrganization().getInn());
            processInstance.commit();
        }

        if (task != null)
        {
            Descriptors.SetDescriptorToInformationObject(session, task, edmSettings.getDocumentStateDescriptor(), sendMessageProcess.GetState());
            Descriptors.SetDescriptorToInformationObject(session, task, "DiadocSenderBoxID", sendMessageProcess.GetSenderBox().getBoxId());
            Descriptors.SetDescriptorToInformationObject(session, task, "DiadocSenderOrgName", sendMessageProcess.GetSenderBox().getOrganization().getFullName());
            //DoxisHelper.SetDescriptorToInformationObject(session, task, "DiadocSenderOrgINN", sendMessageProcess.GetSenderBox().getOrganization().getInn());
            Descriptors.SetDescriptorToInformationObject(session, task, "DiadocRecieverBoxID", sendMessageProcess.GetRecieverBox().getBoxId());
            Descriptors.SetDescriptorToInformationObject(session, task, "DiadocRecieverOrgName", sendMessageProcess.GetRecieverBox().getOrganization().getFullName());
            Descriptors.SetDescriptorToInformationObject(session, task, "DiadocDocumentID", returnResult);
            //DoxisHelper.SetDescriptorToInformationObject(session, task, "DiadocRecieverOrgINN", sendMessageProcess.GetRecieverBox().getOrganization().getInn());
            task.commit();
        }

        return returnResult;
    }

    public OrganizationProtos.Organization GetOrganizationByBoxID(String boxId) throws Exception
    {
        return api.getOrganizationClient().getBox(boxId).getOrganization();
    }

    public  void  ProcessMessagesFromEDM(ISession session) throws Exception
    {
        DiadocConnector.ProcessMessagesFromDiadoc(session);
    }


    public enum FilterCategory
    {
        AnyInbound,
        AnyOutbound,
        AnyInternal,
        AnyInternalWaitingForSenderSignature
    }

    public static void ProcessDocumentsFromDiadocByDates(ISession session, Date fromDate, Date toDate, FilterCategory filterCategory) throws Exception
    {
        log.info("ProcessDocumentsFromDiadocByDates started");
        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration-prod");
        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(), edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        log.debug(String.format("Connect to diadoc: API='%s' LOGIN='%s' INN='%s' KPP='%s'", edmSettings.getApiKey(), edmSettings.getLogin(),
                edmSettings.getCurrentOrganizationInn(), edmSettings.getCurrentOrganizationKpp()));
        diadocConnector.Connect();
        log.debug(String.format("Connected to diadoc"));
        DiadocApi api = diadocConnector.GetApi();
        EDMFile.diadocApi = api;
        EDMSignature.diadocApi = api;
        OrganizationProtos.Box currentBox = GetCurrentBoxBySettings(edmSettings, diadocConnector);

        DocumentsFilter documentsFilter = new DocumentsFilter();

        if (filterCategory == FilterCategory.AnyInbound) documentsFilter.setFilterCategory("Any.Inbound");
        if (filterCategory == FilterCategory.AnyOutbound) documentsFilter.setFilterCategory("Any.Outbound");
        if (filterCategory == FilterCategory.AnyInternal) documentsFilter.setFilterCategory("Any.Internal");
        if (filterCategory == FilterCategory.AnyInternalWaitingForSenderSignature) documentsFilter.setFilterCategory("Any.InternalWaitingForSenderSignature");
        if (documentsFilter.getFilterCategory().equals("Any.InternalWaitingForSenderSignature")){
            fromDate = null;
            toDate = null;
        }
        String pattern = "dd.MM.yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        String fromDateString = df.format(fromDate);
        String toDateString = df.format(toDate);
        fromDateString = df.format(fromDate);
        toDateString = df.format(toDate);
        documentsFilter.setFromDocumentDate(fromDateString);
        documentsFilter.setToDocumentDate(toDateString);
//        documentsFilter.fromDocumentDate = fromDateString;
//        documentsFilter.toDocumentDate = toDateString;

        documentsFilter.setBoxId(currentBox.getBoxId());
        DocumentListProtos.DocumentList documentList = api.getDocumentClient().getDocuments(documentsFilter);

        DocumentsFilter documentsFilter2 = new DocumentsFilter();

        if (filterCategory == FilterCategory.AnyInbound) documentsFilter2.setFilterCategory("Any.Inbound");
        if (filterCategory == FilterCategory.AnyOutbound) documentsFilter2.setFilterCategory("Any.Outbound");
        if (filterCategory == FilterCategory.AnyInternal) documentsFilter2.setFilterCategory("Any.Internal");
        if (filterCategory == FilterCategory.AnyInternalWaitingForSenderSignature) documentsFilter2.setFilterCategory("Any.InternalWaitingForSenderSignature");
        if (documentsFilter2.getFilterCategory().equals("Any.InternalWaitingForSenderSignature")){
            fromDate = null;
            toDate = null;
        }
        fromDateString = df.format(fromDate);
        toDateString = df.format(toDate);
        documentsFilter2.setFromDocumentDate(fromDateString);
        documentsFilter2.setToDocumentDate(toDateString);

        documentsFilter2.setBoxId(currentBox.getBoxId());
        DocumentListProtos.DocumentList documentList2 = api.getDocumentClient().getDocuments(documentsFilter2);

//        List<DocumentProtos.Document> documentEnd = new ArrayList<>();
////        DocumentListProtos.DocumentList documentList = api.GetDocuments(documentsFilter);
//        while (documentList.getTotalCount() > 0){
//            if (StringUtils.isNotBlank(lastIndexKey)) {
//                documentsFilter.afterIndexKey = lastIndexKey;
//                documentList = api.GetDocuments(documentsFilter);
//            }
//            for (int documentNo = 0; documentNo < documentList.getDocumentsCount(); documentNo++) {
//                log.debug(String.format("Document № %d", documentNo));
//                DocumentProtos.Document document = documentList.getDocuments(documentNo);
//                if (document.getTitle().contains("287Г") || document.getFileName().contains("287Г")) documentEnd.add(document);
//            }
//            if (documentList.hasHasMoreResults()){
//                if (StringUtils.isNotBlank(lastIndexKey)) {
//                    documentsFilter.afterIndexKey = lastIndexKey;
//                }
//                documentList = api.GetDocuments(documentsFilter);
//                lastIndexKey = null;
//            }
//        }

        processDocList(session, edmSettings, diadocConnector, api, currentBox, documentsFilter, documentList);
        processDocList(session, edmSettings, diadocConnector, api, currentBox, documentsFilter2, documentList2);
    }

    private static void processDocList(ISession session, Settings edmSettings, DiadocConnector diadocConnector, DiadocApi api,
                                       OrganizationProtos.Box currentBox, DocumentsFilter documentsFilter,
                                       DocumentListProtos.DocumentList documentList) throws Exception {
        String lastIndexKey = "";
        int documentTotalNo = 0;
        boolean needProcess = documentList.getTotalCount() > 0;
        while (needProcess) {
            while (documentList.getTotalCount() > 0) {
                if (StringUtils.isNotBlank(lastIndexKey)) {
                    documentsFilter.setAfterIndexKey(lastIndexKey);
                    documentList = api.getDocumentClient().getDocuments(documentsFilter);
                }
                log.info(String.format("Got documents count: %d", documentList.getDocumentsCount()));
                for (int documentNo = 0; documentNo < documentList.getDocumentsCount(); documentNo++) {
                    log.debug(String.format("Document № %d", documentNo));
                    DocumentProtos.Document document = documentList.getDocuments(documentNo);
                    log.debug(String.format("Document %s № %s from %s", document.getDocumentType(), document.getDocumentNumber(), document.getDocumentDate()));
                    String messageID = document.getMessageId();
                    String entityID = document.getEntityId();
                    log.debug(String.format("Getting message with MessageID = %s, EntityID = %s", messageID, entityID));

                    DiadocMessage_GetApiProtos.Message message = api.getMessageClient().getMessage(currentBox.getBoxId(), messageID, entityID);
                    List<DiadocMessage_GetApiProtos.Entity> entitiesList = message.getEntitiesList();
                    log.debug(String.format("Total entities = %d", entitiesList.size()));
                    List<EDMDocument> edmDocumentList = new ArrayList<>(2);

                    ArchiveDocumentToDoxisProcess archiveDocumentToDoxisProcess = new ArchiveDocumentToDoxisProcess(session, edmSettings);

                    for (DiadocMessage_GetApiProtos.Entity entity : entitiesList) {
                        if (IsAlreadyLoaded(edmDocumentList, entity)) continue;

                        EDMDocument newEDMDocument = processEntity(archiveDocumentToDoxisProcess, diadocConnector, api, currentBox, null, "", messageID, message.getFromBoxId(), message.getToBoxId(), entity);
                        if (newEDMDocument != null) edmDocumentList.add(newEDMDocument);
                    }

                    if (edmDocumentList.size() > 0) {
                        for (EDMDocument edmDocument : edmDocumentList) {
                            try {
                                archiveDocumentToDoxisProcess.Archive(edmDocument);

                            } catch (Exception e) {

                            }
                        }
                    }
                    lastIndexKey = document.getIndexKey();
                }
                documentTotalNo = documentTotalNo + documentList.getDocumentsCount();
                if (documentTotalNo == 1000) documentTotalNo = 0;
            }
            needProcess = false;
            if (documentList.hasHasMoreResults()){
                if (StringUtils.isNotBlank(lastIndexKey)) {
                    documentsFilter.setAfterIndexKey(lastIndexKey);
                }
                documentList = api.getDocumentClient().getDocuments(documentsFilter);
                needProcess = true;
                lastIndexKey = null;
            }
        }
    }

    public static void ProcessMessagesFromDiadoc(ISession session) throws Exception
    {
        log.info("ProcessMessagesFromDiadoc started");
        Settings edmSettings = Settings.GenerateFromDoxis4(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration");
        DiadocConnector diadocConnector = new DiadocConnector(edmSettings.getApiKey(), edmSettings.getUrl(), edmSettings.getLogin(), edmSettings.getPass());
        log.debug(String.format("Connec to diadoc: API='%s' LOGIN='%s' INN='%s' KPP='%s'", edmSettings.getApiKey(), edmSettings.getLogin(),
                edmSettings.getCurrentOrganizationInn(), edmSettings.getCurrentOrganizationKpp()));
        diadocConnector.Connect();
        log.debug(String.format("Connected to diadoc"));
        DiadocApi api = diadocConnector.GetApi();
        EDMFile.diadocApi = api;
        EDMSignature.diadocApi = api;
        OrganizationProtos.Box currentBox = GetCurrentBoxBySettings(edmSettings, diadocConnector);



        String lastEventID = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1);

        if (lastEventID.equalsIgnoreCase(currentBox.getBoxId()) || lastEventID.equalsIgnoreCase("")  || lastEventID.equalsIgnoreCase(" "))
        {
            lastEventID = null;
        }
        log.info(String.format("Last event ID: %s", lastEventID));

        DiadocMessage_GetApiProtos.BoxEventList newEvents = api.getEventsClient().getNewEvents(currentBox.getBoxId(), lastEventID);

        log.info(String.format("Got events count: %d", newEvents.getEventsCount()));
        while (newEvents.getTotalCount() > 0){
            if (StringUtils.isNotBlank(lastEventID)) {
                newEvents = api.getEventsClient().getNewEvents(currentBox.getBoxId(), lastEventID);
            }
            if(newEvents.getEventsCount() > 0) {
                ArchiveDocumentToDoxisProcess archiveDocumentToDoxisProcess = new ArchiveDocumentToDoxisProcess(session, edmSettings);

                for (int eventNo = 0; eventNo < newEvents.getEventsCount(); eventNo++) {
                    log.debug(String.format("Info event: %d/%d", eventNo + 1, newEvents.getEventsCount()));
                    //EDMDocument edmDocument = null;
                    List<EDMDocument> edmDocumentList = new ArrayList<>(10);
                    DiadocMessage_GetApiProtos.BoxEvent currentEvent = newEvents.getEvents(eventNo);
                    DiadocMessage_GetApiProtos.Message message = currentEvent.getMessage();
                    DiadocMessage_GetApiProtos.MessagePatch patch = currentEvent.getPatch();

                    String eventID = currentEvent.getEventId();
                    String messageID = message.getMessageId();
                    String patchID = "";
                    if (patch != null) patchID = patch.getMessageId();



                    log.debug(String.format("Collect information about Event='%s' Message='%s' PatchMessage='%s'", eventID, messageID, patchID));

                    if (message.getToBoxId().equalsIgnoreCase(currentBox.getBoxId()) || message.getFromBoxId().equalsIgnoreCase(currentBox.getBoxId()))
                    {
                        if (message.getEntitiesCount() > 0)
                        {
                            log.debug(String.format("Found '%d' entities at Message from EEvent='%s' Message='%s' PatchMessage='%s'",  message.getEntitiesCount(), eventID, messageID, patchID));
                            List<DiadocMessage_GetApiProtos.Entity> entities = message.getEntitiesList();
                            for (DiadocMessage_GetApiProtos.Entity entity : entities)
                            {
                                if (IsAlreadyLoaded(edmDocumentList, entity)) continue;

                                EDMDocument newEDMDocument = processEntity(archiveDocumentToDoxisProcess, diadocConnector, api, currentBox, null, eventID, messageID, message.getFromBoxId(), message.getToBoxId(), entity);
                                if (newEDMDocument != null) edmDocumentList.add(newEDMDocument);
                            }
                        }
                    }
                    if (patch != null)
                    {
                        if (patch.getEntitiesCount() > 0 )
                        {
                            if (messageID == null || "".equals(messageID))
                            {
                                message = api.getMessageClient().getMessage(currentBox.getBoxId(), patch.getMessageId());
                                messageID = message.getMessageId();
                            }
                            log.debug(String.format("Found '%d' entities at PatchMessage from EEvent='%s' Message='%s' PatchMessage='%s'",  patch.getEntitiesCount(), eventID, messageID, patchID));
                            List<DiadocMessage_GetApiProtos.Entity> entities = patch.getEntitiesList();
                            for(DiadocMessage_GetApiProtos.Entity entityPatch : entities)
                            {
                                if (IsAlreadyLoaded(edmDocumentList, entityPatch)) continue;

                                EDMDocument newEDMDocument = processEntity(archiveDocumentToDoxisProcess, diadocConnector, api, currentBox, null, eventID, messageID, message.getFromBoxId(), message.getToBoxId(), entityPatch);
                                if (newEDMDocument != null) edmDocumentList.add(newEDMDocument);
                            }
                        } else {
                            if (patch.getEntityPatchesList().size() > 0){
                                for (DiadocMessage_GetApiProtos.EntityPatch entityPatch : patch.getEntityPatchesList()){
                                    if (entityPatch.getDocumentIsDeleted()){
                                        IDatabase diadocDocDatabase = session.getDatabase("DIADOCARCHIVE");
                                        IDatabase diadocFoldersDatabase = session.getDatabase("DIADOCARCHIVEFOLDERS");
                                        IDatabase diadocProcessesDatabase = session.getDatabase("BPM");
                                        IDocumentServer documentServer = session.getDocumentServer();
                                        String entityID = entityPatch.getEntityId();
                                        ConcurrentHashMap<String, String> fields = new ConcurrentHashMap<String, String>();
                                        fields.put("DiadocEntityID", entityID);
                                        IInformationObject[] informationObjects = InformationObjects.FindInformationObjects(session, "06022c02-4ebb-4eb8-a150-ebb9c11cdb67", "8d56cdc9-87d3-41c0-b1fb-67ce42250631", fields);
                                        boolean needDeletion = needDeletion(session, informationObjects);
                                        if (needDeletion) {
                                            for (IInformationObject iInformationObject : informationObjects) {
                                                if (iInformationObject instanceof IDocument) {
                                                    IDocument document = (IDocument) iInformationObject;
                                                    IInformationObject[] diadocReferencingDocumentObjects = documentServer.getReferencingInformationObjects(session, document, true, VersionIdentifier.CURRENT_VERSION, 0, false, diadocDocDatabase);
                                                    IProcessInstance processInstance = null;
                                                    IFolder folder = null;
                                                    for (IInformationObject referencingObject : diadocReferencingDocumentObjects) {
                                                        if (referencingObject instanceof IFolder)
                                                            folder = (IFolder) referencingObject;
                                                        if (referencingObject instanceof IProcessInstance)
                                                            processInstance = (IProcessInstance) referencingObject;
                                                        try {
                                                            if (referencingObject instanceof IDocument)
                                                                documentServer.deleteInformationObject(session, referencingObject);
                                                        } catch (Exception e) {
                                                            log.debug("Error appeared while trying to delete document: " + e);
                                                        }
                                                    }
                                                    ConcurrentHashMap<String, String> fieldsRelated = new ConcurrentHashMap<String, String>();
                                                    fieldsRelated.put("ObjectNumber", Descriptors.GetDescriptorValue(session, document, "ObjectNumber"));
                                                    fieldsRelated.put("DiadocMessageID", Descriptors.GetDescriptorValue(session, document, "DiadocMessageID"));
                                                    IInformationObject[] informationObjectsRelated = InformationObjects.FindInformationObjects(session, "06022c02-4ebb-4eb8-a150-ebb9c11cdb67", "8d56cdc9-87d3-41c0-b1fb-67ce42250631", fieldsRelated);
                                                    if (informationObjectsRelated != null) {
                                                        for (IInformationObject referencingObject : informationObjectsRelated) {
                                                            if (referencingObject instanceof IFolder)
                                                                folder = (IFolder) referencingObject;
                                                            if (referencingObject instanceof IProcessInstance)
                                                                processInstance = (IProcessInstance) referencingObject;
                                                            try {
                                                                if (referencingObject instanceof IDocument)
                                                                    documentServer.deleteInformationObject(session, referencingObject);
                                                            } catch (Exception e) {
                                                                log.debug("Error appeared while trying to delete document: " + e);
                                                            }
                                                        }
                                                    }
                                                    try {
                                                        if (document instanceof IDocument)
                                                            documentServer.deleteInformationObject(session, document);
                                                    } catch (Exception e) {
                                                        log.debug("Error appeared while trying to delete document: " + e);
                                                    }
                                                    if (processInstance != null)
                                                        documentServer.deleteInformationObject(session, processInstance);
                                                    if (folder != null) {
                                                        if (folder.getNodeByID("9978b001-199c-4a84-8f71-51cdf0f0e8b0") != null && folder.getNodeByID("9978b001-199c-4a84-8f71-51cdf0f0e8b0").getElements().getCount2() == 0) {
                                                            if (folder.getNodeByID("e876d53a-ae18-4c50-912d-e4cbd4d17fc8") != null && folder.getNodeByID("e876d53a-ae18-4c50-912d-e4cbd4d17fc8").getElements().getCount2() == 0) {
                                                                if (folder.getNodeByID("5f08c658-9c57-4466-81c1-34a8adeffdb1") != null && folder.getNodeByID("5f08c658-9c57-4466-81c1-34a8adeffdb1").getElements().getCount2() == 0) {
                                                                    documentServer.deleteInformationObject(session, folder);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

//                if (edmDocument != null)
                    if (edmDocumentList.size() > 0)
                    {
                        for (EDMDocument edmDocument : edmDocumentList)
                        {
                            archiveDocumentToDoxisProcess.Archive(edmDocument);
                        }
                    }
                    else
                    {
                        log.debug(String.format("No information about documents or signatures at Event='%s' Message='%s'", eventID, messageID));
                    }
                    GlobalValueLists.SetValueToGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1, currentEvent.getEventId());
                    lastEventID = currentEvent.getEventId();
                }
                log.info("ProcessMessagesFromDiadoc finished");
            }

        }
//        GlobalValueLists.SetValueToGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1, lastEventID);
    }

    private static boolean needDeletion(ISession session, IInformationObject[] informationObjects){
        boolean needDeletion = true;
        for (IInformationObject iInformationObject : informationObjects){
            if (iInformationObject instanceof IDocument) {
                IDocument document = (IDocument) iInformationObject;
                String docType = Descriptors.GetDescriptorValue(session, document, "DiadocDocType");
                if (docType.equalsIgnoreCase("Contract") || docType.equalsIgnoreCase("SupplementaryAgreement")){
                    if (Descriptors.GetDescriptorValue(session, document, "ObjectState").equalsIgnoreCase("DOUBLESIGNED")) needDeletion = false;
                }
            }
        }
        return needDeletion;
    }

    private static boolean IsAlreadyLoaded(List<EDMDocument> edmDocumentList, DiadocMessage_GetApiProtos.Entity entity) {
        boolean alreadyLoaded=false;
        for(EDMDocument oldEDMDocument : edmDocumentList)
        {
            for (EDMFile oldFile : oldEDMDocument.getFileList())
            {
                if (entity.getEntityId().equalsIgnoreCase(oldFile.getEntityID()))
                {
                    alreadyLoaded = true;
                    break;
                }
            }

            if (alreadyLoaded) break;

            for (EDMSignature oldSignature : oldEDMDocument.getSignatureList())
            {
                if (entity.getEntityId().equalsIgnoreCase(oldSignature.getEntityID()))
                {
                    alreadyLoaded = true;
                    break;
                }
            }
            if (alreadyLoaded) break;

            for (EDMEventRecord oldEventRecord : oldEDMDocument.getEventRecordsList())
            {
                if (entity.getEntityId().equalsIgnoreCase(oldEventRecord.getEntityID()))
                {
                    alreadyLoaded = true;
                    break;
                }
            }
            if (alreadyLoaded) break;
        }
        if (alreadyLoaded) return true;
        return false;
    }

    public static OrganizationProtos.Organization GetCurrentOrganizationBySettings(Settings edmSettings, DiadocConnector diadocConnector) throws Exception {
        OrganizationProtos.Organization organization = null;
        if (edmSettings.getCurrentOrganizationInn() != null && !"".equals(edmSettings.getCurrentOrganizationInn()) &&
                edmSettings.getCurrentOrganizationKpp() != null && !"".equals(edmSettings.getCurrentOrganizationKpp()))
        {
            try
            {
                organization =  diadocConnector.GetOrganization(edmSettings.getCurrentOrganizationInn(), edmSettings.getCurrentOrganizationKpp());
            }
            catch (Exception ex)
            {

            }
        }
        if (organization == null)
        {
            organization = diadocConnector.GetCurrentOrganization();
        } return organization;
    }


    public static OrganizationProtos.Box GetCurrentBoxBySettings(Settings edmSettings, DiadocConnector diadocConnector) throws Exception {
        OrganizationProtos.Box currentBox = null;
        if (edmSettings.getCurrentOrganizationInn() != null && !"".equals(edmSettings.getCurrentOrganizationInn()) &&
                edmSettings.getCurrentOrganizationKpp() != null && !"".equals(edmSettings.getCurrentOrganizationKpp()))
        {
            try
            {
                currentBox =  diadocConnector.GetOrganizationBox(edmSettings.getCurrentOrganizationInn(), edmSettings.getCurrentOrganizationKpp());
            }
            catch (Exception ex)
            {

            }
        }
        if (currentBox == null)
        {
            currentBox = diadocConnector.GetCurrentOrganizationBox();
        } return currentBox;
    }

    private  static DocumentProtos.Document findDocumentInfoRecursively(DiadocApi api, DiadocMessage_GetApiProtos.Entity currentEntity, String boxID, String messageID) throws  Exception
    {
        DocumentProtos.Document documentInfo = null;
        if (currentEntity == null) return null;
        if (currentEntity.getParentEntityId() == null) return  null;
        DiadocMessage_GetApiProtos.Message originalMessage = api.getMessageClient().getMessage(boxID, messageID, true);
        List<DiadocMessage_GetApiProtos.Entity> entityList = originalMessage.getEntitiesList();
        if (entityList != null)
        {
            for (DiadocMessage_GetApiProtos.Entity searchParentEntity : entityList)
            {
                if (!currentEntity.getParentEntityId().equals(searchParentEntity.getEntityId())) continue;;
                if (searchParentEntity.hasDocumentInfo()) return searchParentEntity.getDocumentInfo();
                documentInfo = findDocumentInfoRecursively(api, searchParentEntity, boxID, messageID);
                if (documentInfo !=null) return  documentInfo;
            }
        }
        return  currentEntity.getDocumentInfo();
    }
    private static EDMDocument processEntity(ArchiveDocumentToDoxisProcess archiveDocumentToDoxisProcess, DiadocConnector diadocConnector, DiadocApi api, OrganizationProtos.Box currentBox, EDMDocument edmDocument, String eventID, String messageID, String fromBoxID, String toBoxID, DiadocMessage_GetApiProtos.Entity entity) throws Exception
    {
        if (entity.hasAttachmentType())
        {
            DocumentProtos.Document documentInfo=null;
            byte[] content = null;



            if (entity.hasDocumentInfo())
            {
                documentInfo = entity.getDocumentInfo();

            }
            else
            {
                documentInfo = findDocumentInfoRecursively(api, entity, currentBox.getBoxId(), messageID);
            }

            try {

                if (documentInfo.hasIsDeleted()) if (documentInfo.getIsDeleted()) return edmDocument;
            }
            catch (Exception ex)
            {

            }

            IInformationObject[] doxis4Docs = archiveDocumentToDoxisProcess.FindThisFileInDoxis(entity.getEntityId(), eventID, entity.getParentEntityId(),
                    documentInfo.getEntityId(), documentInfo.getDocumentType().toString(), "");



            if (edmDocument == null) {
                edmDocument = new EDMDocument(diadocConnector, fromBoxID, toBoxID, documentInfo, content);
            }

            if (doxis4Docs != null && doxis4Docs.length > 0)
            {
                return edmDocument;
            }

            try
            {
                content = api.getDocumentClient().getEntityContent(currentBox.getBoxId(), messageID, entity.getEntityId());
            }
            catch (Exception ex) {}
            if (content == null || content.length < 100)
            {
                edmDocument.AddEventRecord(eventID, messageID, entity);
                return edmDocument;
            }

            if (entity.getEntityType() == DiadocMessage_GetApiProtos.EntityType.Attachment)
            {
                DiadocMessage_GetApiProtos.AttachmentType attachmentType = entity.getAttachmentType();
                //if (attachmentType == DiadocMessage_GetApiProtos.AttachmentType.UnknownAttachmentType) return  edmDocument;
                // if (!entity.getContent().hasData()) return  edmDocument;

                if (entity.getFileName() == null || "".equalsIgnoreCase(entity.getFileName()))
                {
                    edmDocument.AddEventRecord(eventID, messageID, entity);
                    return edmDocument;
                }

                if (entity.getFileName().toLowerCase().contains(".xml"))
                {
                    try
                    {
                        PrintFormResult printFormResult = api.getPrintFormClient().generatePrintForm(currentBox.getBoxId(), messageID, documentInfo.getEntityId());
                        Thread.sleep(2000); // Wait 5 seconds, because if we do same things too fast - we will have an error
                        edmDocument.AddFile(eventID, messageID, entity, entity.getFileName().toUpperCase().replace(".XML", ".pdf"), printFormResult.getContent().getBytes(), EDMFile.TechType.printform);


                        DocumentProtocolResult documentProtocolResult = api.getPrintFormClient().generateDocumentProtocol(currentBox.getBoxId(), messageID, documentInfo.getEntityId());
                        Thread.sleep(2000); // Wait 5 seconds, because if we do same things too fast - we will have an error
                        edmDocument.AddFile(eventID, messageID, entity, entity.getFileName().toUpperCase().replace(".XML", ".pdf"), documentProtocolResult.getDocumentProtocol().getPrintForm().toByteArray(), EDMFile.TechType.protocol);

                        edmDocument.AddFile(eventID, messageID, entity, entity.getFileName().toUpperCase().replace(".XML", ".sgn"), documentProtocolResult.getDocumentProtocol().getSignature().toByteArray(), EDMFile.TechType.signature);

                    }
                    catch (Exception generateEx)
                    {
                        System.out.print("");
                    }

                    edmDocument.AddFile(eventID, messageID, entity, content, Utils.FromDiadocTechType(entity.getAttachmentType(), EDMEventRecord.TechType.technicalxml));
                }
                else
                {
                    edmDocument.AddFile(eventID, messageID, entity, content, EDMFile.TechType.original);
                }
            }
            else if (entity.getEntityType() == DiadocMessage_GetApiProtos.EntityType.Signature)
            {
                edmDocument.AddSignature(currentBox.getBoxId(), eventID, messageID, entity, content);
            }

        }
        return edmDocument;
    }

    /*
    public static void ProcessMessagesFromDiadocArchiveAllInOne(ISession session) throws Exception
    {
        String apiKey = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "APIKEY", 1);
        String url = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "URL", 1);
        String login = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "LOGIN", 1);
        String pass = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "PASS", 1);
        String archiveClass = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTCLASS", 1);
        String archiveClassDB = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTDATABASE", 1);
        String archiveClassNumberDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTNUMBERDESCRIPTOR", 1);
        String archiveClassDateDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTDATEDESCRIPTOR", 1);
        String archiveClassStateDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTSTATEDESCRIPTOR", 1);
        String archiveClassTypeDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTTYPEDESCRIPTOR", 1);

        DiadocConnector diadocConnector = new DiadocConnector(apiKey, url, login, pass);
        diadocConnector.Connect();
        DiadocApi api = diadocConnector.GetApi();
        OrganizationProtos.Box currentBox = diadocConnector.GetCurrentOrganizationBox();

        String lastEventID = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1);
        if (lastEventID.equalsIgnoreCase(currentBox.getBoxId()))
        {
            lastEventID = null;
        }

        DiadocMessage_GetApiProtos.BoxEventList newEvents = api.GetNewEvents(currentBox.getBoxId(), lastEventID);

        if(newEvents.getEventsCount() > 0)
        {
            for (int eventNo = 0; eventNo < newEvents.getEventsCount(); eventNo++)
            {
                EDMDocument EDMDocument = null;
                DiadocMessage_GetApiProtos.BoxEvent currentEvent = newEvents.getEvents(eventNo);
                DiadocMessage_GetApiProtos.Message message = currentEvent.getMessage();
                if (message.getToBoxId().equalsIgnoreCase(currentBox.getBoxId()))
                {
                    if (message.getEntitiesCount() > 0)
                    {
                        List<DiadocMessage_GetApiProtos.Entity> entities = message.getEntitiesList();
                        for (DiadocMessage_GetApiProtos.Entity entity : entities)
                        {
                            if (entity.hasAttachmentType())
                            {


                                byte[] content = api.GetEntityContent(currentBox.getBoxId(), message.getMessageId(), entity.getEntityId());

                                if (content == null) continue;
                                if (content.length < 100) continue;


                                if (entity.getEntityType() == DiadocMessage_GetApiProtos.EntityType.Attachment)
                                {
                                    DiadocMessage_GetApiProtos.AttachmentType attachmentType = entity.getAttachmentType();
                                    /*
                                    if (attachmentType == DiadocMessage_GetApiProtos.AttachmentType.UniversalTransferDocument ||
                                            attachmentType == DiadocMessage_GetApiProtos.AttachmentType.UniversalTransferDocumentBuyerTitle ||
                                            attachmentType == DiadocMessage_GetApiProtos.AttachmentType.UniversalTransferDocumentRevision ||
                                            attachmentType == DiadocMessage_GetApiProtos.AttachmentType.UniversalCorrectionDocument ||
                                            attachmentType == Dia)
                                    {
                                    / *
                                        if (entity.getFileName() == null || "".equalsIgnoreCase(entity.getFileName())) continue;
                                        DocumentProtos.Document documentInfo = entity.getDocumentInfo();
                                        if (EDMDocument == null) EDMDocument = new EDMDocument(diadocConnector, message.getFromBoxId(), message.getToBoxId(), documentInfo, content);

                                        if (entity.getFileName().toLowerCase().contains(".xml"))
                                        {
                                            try
                                            {
                                                PrintFormResult printFormResult = api.GeneratePrintForm(currentBox.getBoxId(), message.getMessageId(), entity.getEntityId());

                                                EDMDocument.AddFile(printFormResult.getContent().getBytes(), printFormResult.getContent().getFileName(), EDMFile.TechType.original);
                                            }catch (Exception generateEx)
                                            {
                                                System.out.print("");
                                            }
                                            EDMDocument.AddFile(content, entity.getFileName(), EDMFile.TechType.technicalxml);
                                        }
                                        else
                                        {

                                            EDMDocument.AddFile(content, entity.getFileName(), EDMFile.TechType.original);
                                        }

                                }
                                else if (entity.getEntityType() == DiadocMessage_GetApiProtos.EntityType.Signature)
                                {
                                    SignatureInfoProtos.SignatureInfo signatureInfo = null;

                                    try {
                                        signatureInfo = api.GetSignatureInfo(currentBox.getBoxId(), message.getMessageId(), entity.getEntityId());
                                    } catch (Exception signatureEx) {}

                                    if (EDMDocument != null)
                                    {
                                        EDMDocument.AddSignature(content);
                                    }


                                }


                                //continue;
                            }
                        }
                    }
                }

                if (EDMDocument != null)
                {
                    if (EDMDocument.getFileList() != null)
                    {
                        ArchiveDocumentToDoxisProcess archiveDocumentToDoxisProcess = new ArchiveDocumentToDoxisProcess(session, EDMDocument, archiveClass, archiveClassDB);
                        archiveDocumentToDoxisProcess.SetDescriptorDocNumber(archiveClassNumberDescriptor);
                        archiveDocumentToDoxisProcess.SetDescriptorDocDate(archiveClassDateDescriptor);
                        archiveDocumentToDoxisProcess.SetDescriptorDocState(archiveClassStateDescriptor);
                        archiveDocumentToDoxisProcess.ArchiveAllInOne();
                    }
                }

                DiadocMessage_GetApiProtos.MessagePatch patch = currentEvent.getPatch();
                if (patch != null)
                {
                    if (patch.getEntitiesCount() > 0 )
                    {

                        List<DiadocMessage_GetApiProtos.Entity> entities = message.getEntitiesList();
                        for(DiadocMessage_GetApiProtos.Entity entityPatch : entities)
                        {
                            if (entityPatch.getSignerBoxId() != null && !"".equals(entityPatch.getSignerBoxId()))
                            {
                                OrganizationProtos.Organization organization = diadocConnector.GetOrganization(entityPatch.getSignerBoxId());
                                if (organization != null)
                                {
                                    if (organization.getInn().equalsIgnoreCase(diadocConnector.GetCurrentOrganization().getInn()))
                                    {
                                        continue;
                                    }

                                }
                            }
                        }
                    }
                }
                GlobalValueLists.SetValueToGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1, currentEvent.getEventId());

            }


        }



    }

    public static void  oldProcessMessagesFromDiadoc(ISession session) throws Exception
    {
        String apiKey = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "APIKEY", 1);
        String url = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "URL", 1);
        String login = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "LOGIN", 1);
        String pass = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "PASS", 1);
        String archiveClass = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTCLASS", 1);
        String archiveClassDB = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTDATABASE", 1);
        String archiveClassNumberDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTNUMBERDESCRIPTOR", 1);
        String archiveClassDateDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTDATEDESCRIPTOR", 1);
        String archiveClasStateDescriptor = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocConfiguration", "DOCUMENTSTATEDESCRIPTOR", 1);

        DiadocConnector diadocConnector = new DiadocConnector(apiKey, url, login, pass);
        diadocConnector.Connect();
        DiadocApi api = diadocConnector.GetApi();
        OrganizationProtos.Box currentBox = diadocConnector.GetCurrentOrganizationBox();

        String lastEventID = GlobalValueLists.GetValueFromGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1);
        if (lastEventID.equalsIgnoreCase(currentBox.getBoxId()))
        {
            lastEventID = null;
        }

        DiadocMessage_GetApiProtos.BoxEventList newEvents = api.GetNewEvents(currentBox.getBoxId(), lastEventID);

        if(newEvents.getEventsCount() > 0)
        {
            for (int eventNo = 0; eventNo < newEvents.getEventsCount(); eventNo++)
            {
                DiadocMessage_GetApiProtos.BoxEvent currentEvent = newEvents.getEvents(eventNo);
                DiadocMessage_GetApiProtos.Message message = currentEvent.getMessage();
                if (message.getToBoxId().equalsIgnoreCase(currentBox.getBoxId()))
                {
                    if (message.getEntitiesCount() > 0)
                    {
                        List<DiadocMessage_GetApiProtos.Entity> entities = message.getEntitiesList();
                        for (DiadocMessage_GetApiProtos.Entity entity : entities)
                        {
                            if (entity.hasAttachmentType()) {
                                byte[] content = api.GetEntityContent(currentBox.getBoxId(), message.getMessageId(), entity.getEntityId());
                                SignatureInfoProtos.SignatureInfo signatureInfo = null;

                                try {
                                    signatureInfo = api.GetSignatureInfo(currentBox.getBoxId(), message.getMessageId(), entity.getEntityId());
                                } catch (Exception signatureEx) {
                                }

                                if (content == null) continue;
                                if (content.length < 10) continue;
                                //if (entity.getFileName() == null || "".equalsIgnoreCase(entity.getFileName())) continue;

                                DocumentProtos.Document documentInfo = entity.getDocumentInfo();

                                EDMDocument EDMDocument = new EDMDocument(diadocConnector, message.getFromBoxId(), message.getToBoxId(), documentInfo, content);
                                if (signatureInfo != null)
                                {
                                    EDMDocument.SetSignature(signatureInfo.toByteArray(), null);

                                }


                                ArchiveDocumentToDoxisProcess archiveDocumentToDoxisProcess = new ArchiveDocumentToDoxisProcess(session, EDMDocument, archiveClass, archiveClassDB);
                                archiveDocumentToDoxisProcess.SetDescriptorDocNumber(archiveClassNumberDescriptor);
                                archiveDocumentToDoxisProcess.SetDescriptorDocDate(archiveClassDateDescriptor);
                                archiveDocumentToDoxisProcess.SetDescriptorDocState(archiveClasStateDescriptor);
                                archiveDocumentToDoxisProcess.ArchiveAllInOne();

                                continue;
                            }
                        }
                    }
                }

                DiadocMessage_GetApiProtos.MessagePatch patch = currentEvent.getPatch();
                if (patch != null)
                {
                    if (patch.getEntitiesCount() > 0 )
                    {

                        List<DiadocMessage_GetApiProtos.Entity> entities = message.getEntitiesList();
                        for(DiadocMessage_GetApiProtos.Entity entityPatch : entities)
                        {
                            if (entityPatch.getSignerBoxId() != null && !"".equals(entityPatch.getSignerBoxId()))
                            {
                                OrganizationProtos.Organization organization = diadocConnector.GetOrganization(entityPatch.getSignerBoxId());
                                if (organization != null)
                                {
                                    if (organization.getInn().equalsIgnoreCase(diadocConnector.GetCurrentOrganization().getInn()))
                                    {
                                        continue;
                                    }

                                }
                            }
                        }
                    }
                }
                GlobalValueLists.SetValueToGlobalValueList(session, "ru.sersolutions.helpers.diadoc/DiadocCache", currentBox.getBoxId(), 1, currentEvent.getEventId());

            }


        }



    }
*/
}
