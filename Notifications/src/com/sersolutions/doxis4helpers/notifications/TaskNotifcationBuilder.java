package com.sersolutions.doxis4helpers.notifications;

import com.ser.blueline.*;
import com.ser.blueline.bpm.*;
import com.ser.blueline.metaDataComponents.ICustomIcon;
import com.ser.sedna.client.bluelineimpl.metadata.DescriptorFormat;
import com.sersolutions.doxis4helpers.commons.Descriptors;
import com.sersolutions.doxis4helpers.commons.InformationObjects;
import com.sersolutions.doxis4helpers.commons.Translation;
import com.sersolutions.doxis4helpers.commons.types.ContentFile;
import com.sersolutions.doxis4helpers.notifications.email.Sender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class for creation of notifications
 */
public class TaskNotifcationBuilder {
    static String defaultLanguage="en";
    static String defaultCountry="US";
    ResourceBundle messages;

    ISession doxis4Session;
    String smtpMailConfig;
    String doxis4ServerID;
    String doxis4WebCubeLink;

    String webCubeJSPAddress;
    String webCubeCustomImagesLibraryAddress;
    String mobileCubeHost;

    public enum SendMethod {
        NO,
        EMAIL,
        EMAILWITHFILES,
        EMAILTOGROUP,
        EMAILTOGROUPWITHFILES
    }

    public enum ReceiverMethod {
        TASKRECIPIENT,
        TASKINITIATOR
    }

    public static class RecipientDetails {
        private final List<String> addresses;
        private final String personName;
        private final String responsibleID;

        /**
         * @param addresses List of addresses to send notification
         * @param personName salutation of email
         * @param responsibleID ID of person to make decisions
         */
        public RecipientDetails(List<String> addresses, String personName, String responsibleID) {
            this.addresses = addresses;
            this.personName = personName;
            this.responsibleID = responsibleID;
        }

        public List<String> getAddresses() {
            return addresses;
        }

        public String getPersonName() {
            return personName;
        }

        public String getResponsibleID() {
            return responsibleID;
        }
    }

    public enum ShowDecisions {
        DISABLE,
        ENABLE
    }

    public enum LinkToDoxis {
        DISABLELINK,
        WIN,
        WEB,
        MOBILE,
        WINWEB,
        WINMOBIlE,
        WINWEBMOBILE,
        WEBMOBILE,
        WEBLOGIN,
        WINWEBLOGIN,
        WINWEBLOGINMOBILE,
        WEBLOGINMOBILE,
        WEBLOGINPASS,
        WINWEBLOGINPASS,
        WINWEBLOGINPASSMOBILE,
        WEBLOGINPASSMOBILE
    };


    /**
     * Init builder
     * @param doxis4Session Doxis4 Session object
     *                      @see com.ser.blueline.ISession
     * @param smtpMailConfig Name of global value list with configuration of SMTP+IMAP
     * @param doxis4ServerID Doxis4 server (node) ID - for WIN and MOBILE links
     * @param doxis4WebCubeLink Doxis4 webCube http address - for WEB link
     * @param mobileCubeHost Doxis4 mobileCube host - for MOBILE links
     * @param webCubeJSPAddress Address of webCube JSP pages
     * @param webCubeCustomImagesLibraryAddress Address of webCube custom images library
     */
    public TaskNotifcationBuilder(ISession doxis4Session, String smtpMailConfig, String doxis4ServerID, String doxis4WebCubeLink, String mobileCubeHost, String webCubeJSPAddress, String webCubeCustomImagesLibraryAddress) {
        this(defaultLanguage, defaultCountry, doxis4Session, smtpMailConfig, doxis4ServerID, doxis4WebCubeLink, mobileCubeHost, webCubeJSPAddress, webCubeCustomImagesLibraryAddress);
    }

    /**
     * Init builder
     * @param language Languange (default - en)
     * @param country Country (default - US)
     * @param doxis4Session Doxis4 Session object
     *                      @see com.ser.blueline.ISession
     * @param smtpMailConfig Name of global value list with configuration of SMTP+IMAP
     * @param doxis4ServerID Doxis4 server (node) ID - for WIN and MOBILE links
     * @param doxis4WebCubeLink Doxis4 webCube http address - for WEB link
     * @param mobileCubeHost Doxis4 mobileCube host - for MOBILE links
     * @param webCubeJSPAddress Address of webCube JSP pages
     * @param webCubeCustomImagesLibraryAddress Address of webCube custom images library
     */
    public TaskNotifcationBuilder(String language, String country, ISession doxis4Session, String smtpMailConfig, String doxis4ServerID, String doxis4WebCubeLink, String mobileCubeHost, String webCubeJSPAddress, String webCubeCustomImagesLibraryAddress) {
       this(language, country, null, doxis4Session, smtpMailConfig, doxis4ServerID, doxis4WebCubeLink, mobileCubeHost, webCubeJSPAddress, webCubeCustomImagesLibraryAddress);
    }
    /**
     * Init builder
     * @param language Languange (default - en)
     * @param country Country (default - US)
     * @param customLocale Custom locale for projects
     * @param doxis4Session Doxis4 Session object
     *                      @see com.ser.blueline.ISession
     * @param smtpMailConfig Name of global value list with configuration of SMTP+IMAP
     * @param doxis4ServerID Doxis4 server (node) ID - for WIN and MOBILE links
     * @param doxis4WebCubeLink Doxis4 webCube http address - for WEB link
     * @param mobileCubeHost Doxis4 mobileCube host - for MOBILE links
     * @param webCubeJSPAddress Address of webCube JSP pages
     * @param webCubeCustomImagesLibraryAddress Address of webCube custom images library
     */
    public TaskNotifcationBuilder(String language, String country, String customLocale, ISession doxis4Session, String smtpMailConfig, String doxis4ServerID, String doxis4WebCubeLink, String mobileCubeHost, String webCubeJSPAddress, String webCubeCustomImagesLibraryAddress) {
        Translation.Init(doxis4Session, false);
        InputStream stream = null;

        if (customLocale == null) stream = this.getClass().getResourceAsStream(String.format("locale_%s_%s.properties", language, country));
        else {
            try {
                stream = new java.io.FileInputStream(customLocale);
            } catch (FileNotFoundException fex) {}
        }
        try {
            Reader reader = new InputStreamReader(stream, "UTF-8");

            messages = new PropertyResourceBundle(reader);

        } catch (Exception ex) {
            Locale currentLocale = new Locale(language, country);
            messages = ResourceBundle.getBundle("locale", currentLocale);
        }

        this.doxis4Session = doxis4Session;
        this.smtpMailConfig = smtpMailConfig;
        this.doxis4ServerID = doxis4ServerID;
        this.doxis4WebCubeLink = doxis4WebCubeLink;
        if (!doxis4WebCubeLink.contains("locale=")) this.doxis4WebCubeLink = String.format("%s&locale=%s_%s", this.doxis4WebCubeLink, language, country);
        this.mobileCubeHost = mobileCubeHost;
        this.webCubeJSPAddress =webCubeJSPAddress;
        this.webCubeCustomImagesLibraryAddress = webCubeCustomImagesLibraryAddress;
    }

    /**
     * Collects all Task participants
     * @param task ITask object for which you need to get participants
     *             @see com.ser.blueline.bpm.ITask
     * @return List of string emails of participants
     */
    public List<String> CollectAllParticipants(ITask task) {
        IProcessInstance processInstance = task.getProcessInstance();
        Collection<ITask> prevTasks = processInstance.findTasks(TaskStatus.COMPLETED);

        List<String> addresses = new ArrayList<>(prevTasks.size());
        for (ITask prevTask : prevTasks) {
            String responsibleID=null;
            IWorkbasket workbasket = null;
            IReceivers receivers = task.getReceivers();
            if (receivers != null) {
                workbasket = receivers.getWorkbasket();
            }
            if (workbasket == null) {
                workbasket= prevTask.getCurrentWorkbasket();
            }

            String address = null;
            String orgName = null;


            IOrgaElement associatedOrgaElement = workbasket.getAssociatedOrgaElement();
            if (associatedOrgaElement != null) {

                IUser user = null;
                if (associatedOrgaElement.getOrgaElementType() == OrgaElementType.USER) {
                    user = (IUser)  workbasket.getAssociatedOrgaElement();
                } else if (associatedOrgaElement.getOrgaElementType() == OrgaElementType.UNIT) {
                    IUnit unit = (IUnit) workbasket.getAssociatedOrgaElement();
                    user = unit.getManager();
                } else if (associatedOrgaElement.getOrgaElementType() == OrgaElementType.GROUP) {
                    IGroup group = (IGroup) workbasket.getAssociatedOrgaElement();
                    user = group.getManager();
                }

                if (user != null) {
                    address = user.getEMailAddress();
                    StringBuilder orgNameBuilder = new StringBuilder();
                    if (user.getFirstName() != null && !"".equals(user.getFirstName())) {
                        orgNameBuilder.append(user.getFirstName()).append(" ");
                    }
                    if (user.getLastName() != null && !"".equals(user.getLastName())) {
                        orgNameBuilder.append(user.getLastName());
                    }
                    orgName = orgNameBuilder.toString();
                    responsibleID = user.getID();
                }

                if (orgName ==null) orgName = workbasket.getAssociatedOrgaElement().getName();
                if (responsibleID == null) responsibleID = workbasket.getAssociatedOrgaElement().getID();
            }
            if (address == null) {
                address = workbasket.getNotifyEMail();
            }
            if (address != null) {
                boolean alreadyExists=false;
                for (String containsAddress : addresses) {
                    if (containsAddress.equals(address)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Extracts mail recipient details from Doxis4 task, depending on sending method. Considers task receiver workbasket
     * and extracts manager of group, uni. Takes resulting user e-mail. If message is sent to group members, then all
     * user involved in receiver unit or group are considered (but no full name and user id)
     * @param sendMethod email sending method
     * @param task ITask object
     * @return wrapper class with e-mail addresses, ID of user in Doxis4, and recipient full name
     * @throws Exception
     */
    private RecipientDetails getRecipientDetails(SendMethod sendMethod, ITask task) throws IllegalArgumentException {
        String responsibleID=null;
        String orgName = "";
        List<String> addresses = new ArrayList<>();
        IWorkbasket workbasket = null;
        IUser user = null;
        if (task.getReceivers() != null) {
            workbasket = task.getReceivers().getWorkbasket();
        } else {
            workbasket= task.getCurrentWorkbasket();
        }

        String address = null;

        IOrgaElement associatedOrgaElement = workbasket.getAssociatedOrgaElement();

        if (associatedOrgaElement != null) {
            OrgaElementType orgaElementType = associatedOrgaElement.getOrgaElementType();
            if (orgaElementType == OrgaElementType.USER) {
                user = (IUser)  workbasket.getAssociatedOrgaElement();
            } else if (orgaElementType == OrgaElementType.UNIT) {
                IUnit unit = (IUnit) workbasket.getAssociatedOrgaElement();
                if (sendMethod == SendMethod.EMAIL) {
                    user = unit.getManager();
                } else if (sendMethod == SendMethod.EMAILTOGROUP) {
                    user = null;
                    IUser[] users = unit.getUserMembers();
                    for (IUser unitUser : users) {
                        if (StringUtils.isNotBlank(unitUser.getEMailAddress())) {
                            addresses.add(unitUser.getEMailAddress());
                        }
                    }
                }
            } else if (orgaElementType == OrgaElementType.GROUP) {
                IGroup group = (IGroup) workbasket.getAssociatedOrgaElement();
                if (sendMethod == SendMethod.EMAIL) {
                    user = group.getManager();
                } else if (sendMethod == SendMethod.EMAILTOGROUP) {
                    user = null;
                    IUser[] users = group.getUserMembers();
                    for (IUser groupUser : users) {
                        if (StringUtils.isNotBlank(groupUser.getEMailAddress())) {
                            addresses.add(groupUser.getEMailAddress());
                        }
                    }
                }
            }

            if (user != null) {
                address = user.getEMailAddress();
                orgName = user.getFullName();
                responsibleID = user.getID();
            }

            if (orgName == null) orgName = workbasket.getAssociatedOrgaElement().getName();
            if (responsibleID == null) responsibleID = workbasket.getAssociatedOrgaElement().getID();
        }
        if (addresses.size() == 0) {
            if (address == null) {
                address = workbasket.getNotifyEMail();
            }
            if (address == null || "".equals(address)) throw new IllegalArgumentException(messages.getString("CantFindAddresses"));

            addresses.add(address);
        }
        return new RecipientDetails(addresses, orgName, responsibleID);
    }

    /**
     * Send notification about task to multiple users (usually at the end of process to all participants)
     * @param sendMethod method of sending
     * @param contents list of files to send
     * @param addresses List of addresses to send notification
     * @param personName salutation of email
     * @param responsibleID ID of person to make decisions
     * @return String message of notification
     * @throws Exception if something goes wrong
     */
    public String SendFiles(SendMethod sendMethod, List<ContentFile> contents, List<String> addresses, Map<String, String> messageReplacements, String personName, String responsibleID) throws Exception {
        StringBuilder messageBuilder = new StringBuilder();
        messageReplacements.put("RecipientName", personName);
        String subject = ReplaceOccurances(messages.getString("TaskName"), messageReplacements);
        messageBuilder.append(ReplaceOccurances(messages.getString("MessageIntro"), messageReplacements));
        if (!"".equals(messages.getString("EmailFinish"))) {
            messageBuilder.append("<br /><br />").append(ReplaceOccurances(messages.getString("EmailFinish"), messageReplacements));
        }

        if (sendMethod == SendMethod.EMAIL || sendMethod == SendMethod.EMAILTOGROUP) {
            com.sersolutions.doxis4helpers.notifications.email.Sender.sendMail(doxis4Session, smtpMailConfig, addresses, subject, messageBuilder.toString(), contents);
        }

        return messageBuilder.toString();

    }

    /**
     * Send notification about task to responsible user or manager of Unit\Group\Role
     * @param sendMethod method of sending
     * @param showDecisions show decisions links in notification or not
     * @param linkToDoxis kind of links to Doxis4
     * @param task Doxis4 Task object
     *             @see com.ser.blueline.bpm.ITask
     * @param descriptors Map of Name - Value descriptors that must be showed in notification
     * @return String message of notification
     * @throws Exception if something goes wrong
     */
    public String SendNotification(SendMethod sendMethod, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, ITask task, Map<String, String> descriptors) throws Exception {
        RecipientDetails recipientDetails = getRecipientDetails(sendMethod, task);
        return SendNotification(sendMethod, showDecisions, linkToDoxis, task, descriptors, recipientDetails.addresses, recipientDetails.personName, recipientDetails.responsibleID);
    }

    /**
     * Send notification about task to multiple users (usually at the end of process to all participants)
     * @param sendMethod method of sending
     * @param showDecisions show decisions links in notification or not
     * @param linkToDoxis kind of links to Doxis4
     * @param task Doxis4 Task object
     *             @see com.ser.blueline.bpm.ITask
     * @param descriptors Map of Name - Value descriptors that must be showed in notification
     * @param addresses List of addresses to send notification
     * @param personName salutation of email
     * @param responsibleID ID of person to make decisions
     * @return String message of notification
     * @throws Exception if something goes wrong
     */
    public String SendNotification(SendMethod sendMethod, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, ITask task, Map<String, String> descriptors, List<String> addresses, String personName, String responsibleID) throws Exception{

        IProcessInstance processInstance = task.getProcessInstance();

        Date dateSheduled = task.getScheduledEndDate();
        DateFormat df = null;
        List<ContentFile> contentFiles = null;

        StringBuilder messageBuilder = new StringBuilder();

        String taskName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), task.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);

        messageBuilder.append(String.format(messages.getString("MessageIntro"), personName, taskName));

        if (descriptors != null) {
            for (String mapEntry : descriptors.keySet()) {
                String value = Descriptors.GetDescriptorValue(doxis4Session, processInstance, descriptors.get(mapEntry));
                if (value != null) {
                    Date testDate = Descriptors.GetValueAsDate(value);
                    if (testDate == null || value.length() < 8) {
                        messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), value));
                    } else {
                        df = new SimpleDateFormat(messages.getString("DateFormat"));
                        messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), df.format(testDate)));
                    }
                }
            }
        }

        String title = String.format(messages.getString("TaskName"), taskName);

        if (dateSheduled != null && !"".equals(messages.getString("Deadline"))) {
            df = new SimpleDateFormat(messages.getString("DateTimeFormat"));
            messageBuilder.append("<br />").append(String.format(messages.getString("Deadline"), df.format(dateSheduled)));

            Date currentDate = new Date();
            long diffInMillies = dateSheduled.getTime() - currentDate.getTime();

            Date diffDate = new Date((diffInMillies > 0)? diffInMillies : diffInMillies * -1);
            int days = (int)(diffInMillies / 1000 / 60 / 60 / 24) - diffDate.getMonth();
            int hours = (int) (diffInMillies / 1000 / 60 / 60) - (days * 24);
            int minutes = (int) (diffInMillies / 1000 / 60) - (hours * 60) - (days * 24 * 60);

            messageBuilder.append(String.format("<br />%s:", (diffInMillies > 0) ? messages.getString("DeadlineHaveTime") : messages.getString("DeadlineLostTime")));
            if (diffDate.getMonth() > 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMonth"),diffDate.getMonth()));
            if (days != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineDays"), Math.abs(days)));
            if (hours != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineHours"), Math.abs(hours)));
            if (minutes != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMinutes"), Math.abs(minutes)));

        }

        List<IPossibleDecision> possibleDecisions = task.findPossibleDecisions();
        if (showDecisions == ShowDecisions.ENABLE && webCubeJSPAddress != null && !"".equals(webCubeJSPAddress) && responsibleID != null && !"".equals(responsibleID)) {
            if (possibleDecisions.size() > 0) {
                messageBuilder.append("<br /><br />").append(messages.getString("DecisionInviteText")).append("<br /><table><tr>");
                for (IPossibleDecision possibleDecision : possibleDecisions) {
                    IDecision decision = possibleDecision.getDecision();
                    if (decision != null) {
                        String decisionName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), decision.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);

                        String iconAddress = null;
                        if (webCubeCustomImagesLibraryAddress != null && !"".equals(webCubeCustomImagesLibraryAddress)) {
                            if (decision.getDecisionDefinition().getIconID() != null) {
                                ICustomIcon customIcon = doxis4Session.getDocumentServer().getCustomIcon(doxis4Session, decision.getDecisionDefinition().getIconID());
                                iconAddress = webCubeCustomImagesLibraryAddress  +"/" + customIcon.getID() + ".png";
                            }
                        }
                        messageBuilder.append(String.format("<td><a href='%sConfirmTask.jsp?taskid=%s&decisionid=%s&responsibleid=%s'>%s</a></td>", webCubeJSPAddress,
                                task.getID(), decision.getDecisionDefinition().getID(), responsibleID, (iconAddress == null) ? decisionName : String.format("<img src='%s' /><br />%s", iconAddress, decisionName)));
                    }

                }
                messageBuilder.append("</tr></table>");
            }
        }

        if (linkToDoxis != LinkToDoxis.DISABLELINK) {
            messageBuilder.append("<br /><br />").append(messages.getString("OpenTaskLink"));

            if (linkToDoxis.toString().contains(LinkToDoxis.WIN.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToWin(task));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.WEB.toString())) {
                IUser responsibleUser = null;
                try {
                    responsibleUser = doxis4Session.getDocumentServer().getUser(doxis4Session, responsibleID);
                }
                catch (Exception ex) {}

                boolean login=false;
                boolean pass=false;
                if (linkToDoxis.toString().contains("LOGIN")) login = true;
                if (linkToDoxis.toString().contains("PASS")) pass    = true;

                messageBuilder.append("<br />").append(GenerateLinkToWeb(task, responsibleUser, login, pass));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.MOBILE.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToMobile(task));
            }
        }


        if (!"".equals(messages.getString("EmailFinish"))) {
            messageBuilder.append("<br /><br />").append(messages.getString("EmailFinish"));
        }

        if (sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
            contentFiles = new LinkedList<ContentFile>();
            for (ILink link : processInstance.getLoadedInformationObjectLinks().getLinks()) {
                IInformationObject target = link.getTargetInformationObject();
                if (target instanceof IDocument) {
                    IDocument document = (IDocument) target;
                    contentFiles.addAll(InformationObjects.GetAllContent(document));
                }
            }
        }

        if (sendMethod == SendMethod.EMAIL || sendMethod == SendMethod.EMAILTOGROUP ||
                sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
            com.sersolutions.doxis4helpers.notifications.email.Sender.sendMail(doxis4Session, smtpMailConfig, addresses, title, messageBuilder.toString(), contentFiles);
        }

        return messageBuilder.toString();

    }

    public String SendNotification(SendMethod sendMethod, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, ITask task, Map<String, String> descriptors, Map<String, String> messageReplacements) throws Exception {
        RecipientDetails recipientDetails = getRecipientDetails(sendMethod, task);
        messageReplacements.put("RecipientName", recipientDetails.getPersonName());
        return SendNotification(sendMethod, showDecisions, linkToDoxis, task, descriptors, messageReplacements, recipientDetails);
    }

    public String SendNotification(SendMethod sendMethod, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, ITask task, Map<String, String> descriptors, Map<String, String> messageReplacements, String recipientID) throws Exception {
        IUser recipient = doxis4Session.getDocumentServer().getUser(doxis4Session, recipientID);
        List<String> recipientAddresses = new ArrayList<>();
        recipientAddresses.add(recipient.getEMailAddress());
        messageReplacements.put("RecipientName", recipient.getFullName());
        RecipientDetails receiverDetails = getRecipientDetails(sendMethod, task); //receiver of task details
        messageReplacements.put("ReceiverName", receiverDetails.getPersonName());
        IUser receiver = doxis4Session.getDocumentServer().getUser(doxis4Session, receiverDetails.responsibleID);
        String receiverDepartment = "<ошибка>";
        if (receiver != null) {
            IUnit receiverUnit = receiver.getPrimaryUnitObject();
            if (receiverUnit != null) {
                receiverDepartment = receiverUnit.getName();
            }
        }
        messageReplacements.put("ReceiverDepartment", receiverDepartment);
        return SendNotification(sendMethod, showDecisions, linkToDoxis, task, descriptors, messageReplacements, new RecipientDetails(recipientAddresses, recipient.getFullName(), recipientID)); //recipient of mail details
    }

    /**
     * Send notification about task to multiple users (usually at the end of process to all participants)
     * @param sendMethod method of sending
     * @param showDecisions show decisions links in notification or not
     * @param linkToDoxis kind of links to Doxis4
     * @param task Doxis4 Task object
     *             @see com.ser.blueline.bpm.ITask
     * @param descriptors Map of Name - Value descriptors that must be showed in notification
     * @param messageReplacements Map of Name - Value pairs to be used in messages to replace all occurrences of %Name% to Value
     * @param recipientDetails recipient details
     * @return String message of notification
     * @throws Exception if something goes wrong
     */
    public String SendNotification(SendMethod sendMethod, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, ITask task, Map<String, String> descriptors, Map<String, String> messageReplacements, RecipientDetails recipientDetails) throws Exception {
        IProcessInstance processInstance = task.getProcessInstance();
        List<ContentFile> contentFiles = null;
        Date dateSheduled = task.getScheduledEndDate();
        DateFormat df = null;
        StringBuilder messageBuilder = new StringBuilder();
        String taskName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), task.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);
        messageReplacements.put("RecipientName", recipientDetails.personName);
        messageBuilder.append(ReplaceOccurances(messages.getString("MessageIntro"), messageReplacements));
        if (descriptors != null) {
            for (String mapEntry : descriptors.keySet()) {
                if (messageReplacements.containsKey(descriptors.get(mapEntry))) {
                    messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), messageReplacements.get(descriptors.get(mapEntry))));
                } else {
                    IDescriptor descriptorDef = Descriptors.GetDescriptorDefinition(doxis4Session, descriptors.get(mapEntry));
                    String value = Descriptors.GetDescriptorValue(doxis4Session, processInstance, descriptors.get(mapEntry));
                    if (value != null) {
                        if (value.length() >= 8 && (descriptorDef.getType() == IDescriptor.TYPE_DATE || descriptorDef.getType() == IDescriptor.TYPE_DATETIME)) {
                            Date testDate = Descriptors.GetValueAsDate(value);
                            df = new SimpleDateFormat(messages.getString("DateFormat"));
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), df.format(testDate)));
                        } else if (NumberUtils.isParsable(value) && (descriptorDef.getType() == IDescriptor.TYPE_INTEGER || descriptorDef.getType() == IDescriptor.TYPE_LONG || descriptorDef.getType() == IDescriptor.TYPE_FLOAT)) {
                            float val=0;
                            if (descriptorDef.getType() == IDescriptor.TYPE_INTEGER) val = Integer.parseInt(value);
                            if (descriptorDef.getType() == IDescriptor.TYPE_LONG) val = Long.parseLong(value);
                            if (descriptorDef.getType() == IDescriptor.TYPE_FLOAT) val = Float.parseFloat(value);
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), String.format("%,.2f", val)));
                        } else {
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), mapEntry, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), value));
                        }
                    }
                }

            }
        }

        String subject = ReplaceOccurances(messages.getString("TaskName"), messageReplacements);

        if (dateSheduled != null && !"".equals(messages.getString("Deadline"))) {
            df = new SimpleDateFormat(messages.getString("DateTimeFormat"));
            messageBuilder.append("<br />").append(String.format(messages.getString("Deadline"), df.format(dateSheduled)));

            Date currentDate = new Date();
            long diffInMillies = dateSheduled.getTime() - currentDate.getTime();

            Date diffDate = new Date((diffInMillies > 0)? diffInMillies : diffInMillies * -1);
            int days = (int)(diffInMillies / 1000 / 60 / 60 / 24) - diffDate.getMonth();
            int hours = (int) (diffInMillies / 1000 / 60 / 60) - (days * 24);
            int minutes = (int) (diffInMillies / 1000 / 60) - (hours * 60) - (days * 24 * 60);

            messageBuilder.append(String.format("<br />%s:", (diffInMillies > 0) ? messages.getString("DeadlineHaveTime") : messages.getString("DeadlineLostTime")));
            if (diffDate.getMonth() > 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMonth"),diffDate.getMonth()));
            if (days != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineDays"), Math.abs(days)));
            if (hours != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineHours"), Math.abs(hours)));
            if (minutes != 0) messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMinutes"), Math.abs(minutes)));

        }

        List<IPossibleDecision> possibleDecisions = task.findPossibleDecisions();
        if (showDecisions == ShowDecisions.ENABLE && webCubeJSPAddress != null && !"".equals(webCubeJSPAddress) && StringUtils.isNotBlank(recipientDetails.getResponsibleID())) {
            if (possibleDecisions.size() > 0) {
                messageBuilder.append("<br /><br />").append(messages.getString("DecisionInviteText")).append("<br /><table><tr>");
                for (IPossibleDecision possibleDecision : possibleDecisions) {
                    IDecision decision = possibleDecision.getDecision();
                    if (decision != null) {
                        String decisionName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), decision.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);

                        String iconAddress = null;
                        if (webCubeCustomImagesLibraryAddress != null && !"".equals(webCubeCustomImagesLibraryAddress)) {
                            if (decision.getDecisionDefinition().getIconID() != null) {
                                ICustomIcon customIcon = doxis4Session.getDocumentServer().getCustomIcon(doxis4Session, decision.getDecisionDefinition().getIconID());
                                iconAddress = webCubeCustomImagesLibraryAddress  +"/" + customIcon.getID() + ".png";
                            }
                        }
                        messageBuilder.append(String.format("<td><a href='%sConfirmTask.jsp?taskid=%s&decisionid=%s&responsibleid=%s'>%s</a></td>", webCubeJSPAddress,
                                task.getID(), decision.getDecisionDefinition().getID(), recipientDetails.getResponsibleID(), (iconAddress == null) ? decisionName : String.format("<img src='%s' /><br />%s", iconAddress, decisionName)));
                    }
                }
                messageBuilder.append("</tr></table>");
            }
        }

        if (linkToDoxis != LinkToDoxis.DISABLELINK) {
            messageBuilder.append("<br /><br />").append(messages.getString("OpenTaskLink"));

            if (linkToDoxis.toString().contains(LinkToDoxis.WIN.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToWin(task));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.WEB.toString())) {
                IUser receiver = null;
                try {
                    receiver = doxis4Session.getDocumentServer().getUser(doxis4Session, recipientDetails.getResponsibleID());
                } catch (Exception e) {
                    //TODO: logger.errror(e);
                }
                messageBuilder.append("<br />").append(GenerateLinkToWeb(task, receiver, linkToDoxis.toString().contains("LOGIN"), linkToDoxis.toString().contains("PASS")));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.MOBILE.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToMobile(task));
            }
        }

        if (StringUtils.isNotBlank(messages.getString("EmailFinish"))) {
            messageBuilder.append("<br /><br />").append(ReplaceOccurances(messages.getString("EmailFinish"), messageReplacements));
        }

        if (sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
            contentFiles = new LinkedList<ContentFile>();
            for (ILink link : processInstance.getLoadedInformationObjectLinks().getLinks()) {
                IInformationObject target = link.getTargetInformationObject();
                if (target instanceof IDocument) {
                    IDocument document = (IDocument) target;
                    contentFiles.addAll(InformationObjects.GetAllContent(document));
                }
            }
        }

        if (sendMethod == SendMethod.EMAIL || sendMethod == SendMethod.EMAILTOGROUP ||
                sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES){
            com.sersolutions.doxis4helpers.notifications.email.Sender.sendMail(doxis4Session, smtpMailConfig, recipientDetails.getAddresses(), subject, messageBuilder.toString(), contentFiles);
        }

        return messageBuilder.toString();
    }


    /**
     * More 'generic' implementation of Send notification about task to multiple users (usually at the end of process to all participants)
     * @param sendMethod method of sending
     * @param showDecisions show decisions links in notification or not
     * @param linkToDoxis kind of links to Doxis4
     * @param descriptors Map of Name - Value descriptors that must be showed in notification
     * @param messageReplacements Map of Name - Value pairs to be used in messages to replace all occurrences of %Name% to Value
     * @param recipientDetails recipient details
     * @return String message of notification
     * @throws Exception if something goes wrong
     */
    public String SendNotificationGeneric(SendMethod sendMethod, IInformationObject informationObject, ShowDecisions showDecisions, LinkToDoxis linkToDoxis, Map<String, String> descriptors, Map<String, String> messageReplacements, RecipientDetails recipientDetails) throws Exception {
        DateFormat df;
        List<ContentFile> contentFiles = null;
        //builder for message
        StringBuilder messageBuilder = new StringBuilder();
        //add recipient name to message replacements
        messageReplacements.put("RecipientName", recipientDetails.personName);
        //add message intro
        messageBuilder.append(ReplaceOccurances(messages.getString("MessageIntro"), messageReplacements));

        //adding descriptor list to message
        if (descriptors != null) {
            for (String descriptorEmailLabel : descriptors.keySet()) {
                if (messageReplacements.containsKey(descriptors.get(descriptorEmailLabel))) {
                    messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), descriptorEmailLabel, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), messageReplacements.get(descriptors.get(descriptorEmailLabel))));
                } else {
                    IDescriptor descriptorDef = Descriptors.GetDescriptorDefinition(doxis4Session, descriptors.get(descriptorEmailLabel));
                    String value = Descriptors.GetDescriptorValue(doxis4Session, informationObject, descriptors.get(descriptorEmailLabel));
                    if (value != null) {
                        if (value.length() >= 8 && (descriptorDef.getType() == IDescriptor.TYPE_DATE || descriptorDef.getType() == IDescriptor.TYPE_DATETIME)) {
                            Date testDate = Descriptors.GetValueAsDate(value);
                            df = new SimpleDateFormat(messages.getString("DateFormat"));
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), descriptorEmailLabel, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), df.format(testDate)));
                        } else if (NumberUtils.isParsable(value) && (descriptorDef.getType() == IDescriptor.TYPE_INTEGER || descriptorDef.getType() == IDescriptor.TYPE_LONG || descriptorDef.getType() == IDescriptor.TYPE_FLOAT)) {
                            float val = 0;
                            if (descriptorDef.getType() == IDescriptor.TYPE_INTEGER) val = Integer.parseInt(value);
                            if (descriptorDef.getType() == IDescriptor.TYPE_LONG) val = Long.parseLong(value);
                            if (descriptorDef.getType() == IDescriptor.TYPE_FLOAT) val = Float.parseFloat(value);
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), descriptorEmailLabel, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), String.format("%,.2f", val)));
                        } else {
                            messageBuilder.append("<br />").append(String.format(messages.getString("Descriptor"), Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), descriptorEmailLabel, Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL), value));
                        }
                    }
                }
            }
        }

        //additional processing if task
        if (informationObject instanceof ITask) {
            ITask task = ((ITask) informationObject);
            IProcessInstance processInstance = task.getProcessInstance();
            Date dateScheduled = task.getScheduledEndDate();
            String taskName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), task.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);

            //information about task expire date and time left for user action
            if (dateScheduled != null && !"".equals(messages.getString("Deadline"))) {
                df = new SimpleDateFormat(messages.getString("DateTimeFormat"));
                messageBuilder.append("<br />").append(String.format(messages.getString("Deadline"), df.format(dateScheduled)));

                Date currentDate = new Date();

                long diffInMillis = dateScheduled.getTime() - currentDate.getTime();

                Date diffDate = new Date((diffInMillis > 0) ? diffInMillis : diffInMillis * -1);
                int days = (int) (diffInMillis / 1000 / 60 / 60 / 24) - diffDate.getMonth();
                int hours = (int) (diffInMillis / 1000 / 60 / 60) - (days * 24);
                int minutes = (int) (diffInMillis / 1000 / 60) - (hours * 60) - (days * 24 * 60);

                messageBuilder.append(String.format("<br />%s:", (diffInMillis > 0) ? messages.getString("DeadlineHaveTime") : messages.getString("DeadlineLostTime")));
                if (diffDate.getMonth() > 0)
                    messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMonth"), diffDate.getMonth()));
                if (days != 0)
                    messageBuilder.append(" ").append(String.format(messages.getString("DeadlineDays"), Math.abs(days)));
                if (hours != 0)
                    messageBuilder.append(" ").append(String.format(messages.getString("DeadlineHours"), Math.abs(hours)));
                if (minutes != 0)
                    messageBuilder.append(" ").append(String.format(messages.getString("DeadlineMinutes"), Math.abs(minutes)));
            }

            //add decisions in e-mail
            List<IPossibleDecision> possibleDecisions = task.findPossibleDecisions();
            if (showDecisions == ShowDecisions.ENABLE && webCubeJSPAddress != null && !"".equals(webCubeJSPAddress) && StringUtils.isNotBlank(recipientDetails.getResponsibleID())) {
                if (possibleDecisions.size() > 0) {
                    messageBuilder.append("<br /><br />").append(messages.getString("DecisionInviteText")).append("<br /><table><tr>");
                    for (IPossibleDecision possibleDecision : possibleDecisions) {
                        IDecision decision = possibleDecision.getDecision();
                        if (decision != null) {
                            String decisionName = Translation.getTranslationFromDicitonary(messages.getString("LanguageID"), decision.getName(), Translation.GetTranslationModeIfNotFound.RETURN_ORIGINAL);

                            String iconAddress = null;
                            if (webCubeCustomImagesLibraryAddress != null && !"".equals(webCubeCustomImagesLibraryAddress)) {
                                if (decision.getDecisionDefinition().getIconID() != null) {
                                    ICustomIcon customIcon = doxis4Session.getDocumentServer().getCustomIcon(doxis4Session, decision.getDecisionDefinition().getIconID());
                                    iconAddress = webCubeCustomImagesLibraryAddress + "/" + customIcon.getID() + ".png";
                                }
                            }
                            messageBuilder.append(String.format("<td><a href='%sConfirmTask.jsp?taskid=%s&decisionid=%s&responsibleid=%s'>%s</a></td>", webCubeJSPAddress,
                                    task.getID(), decision.getDecisionDefinition().getID(), recipientDetails.getResponsibleID(), (iconAddress == null) ? decisionName : String.format("<img src='%s' /><br />%s", iconAddress, decisionName)));
                        }
                    }
                    messageBuilder.append("</tr></table>");
                }
            }

            //attach files
            if (sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
                contentFiles = new LinkedList<ContentFile>();
                for (ILink link : processInstance.getLoadedInformationObjectLinks().getLinks()) {
                    IInformationObject target = link.getTargetInformationObject();
                    if (target instanceof IDocument) {
                        IDocument document = (IDocument) target;
                        contentFiles.addAll(InformationObjects.GetAllContent(document));
                    }
                }
            }
        } else if (informationObject instanceof IDocument) {
            //attach files
            if (sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
                contentFiles = new LinkedList<ContentFile>(InformationObjects.GetAllContent(informationObject));
            }
        }

        //get e-mail message subject
        String subject = ReplaceOccurances(messages.getString("TaskName"), messageReplacements);

        if (linkToDoxis != LinkToDoxis.DISABLELINK) {
            messageBuilder.append("<br /><br />").append(messages.getString("OpenTaskLink"));

            if (linkToDoxis.toString().contains(LinkToDoxis.WIN.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToWin(informationObject));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.WEB.toString())) {
                IUser receiver = null;
                try {
                    receiver = doxis4Session.getDocumentServer().getUser(doxis4Session, recipientDetails.getResponsibleID());
                } catch (Exception e) {
                    //TODO: logger.errror(e);
                }
                messageBuilder.append("<br />").append(GenerateLinkToWeb(informationObject, receiver, linkToDoxis.toString().contains("LOGIN"), linkToDoxis.toString().contains("PASS")));
            }
            if (linkToDoxis.toString().contains(LinkToDoxis.MOBILE.toString())) {
                messageBuilder.append("<br />").append(GenerateLinkToMobile(informationObject));
            }
        }

        //add e-mail ending
        if (StringUtils.isNotBlank(messages.getString("EmailFinish"))) {
            messageBuilder.append("<br /><br />").append(ReplaceOccurances(messages.getString("EmailFinish"), messageReplacements));
        }

        if (sendMethod == SendMethod.EMAIL || sendMethod == SendMethod.EMAILTOGROUP ||
                sendMethod == SendMethod.EMAILWITHFILES || sendMethod == SendMethod.EMAILTOGROUPWITHFILES) {
            Sender.sendMail(doxis4Session, smtpMailConfig, recipientDetails.getAddresses(), subject, messageBuilder.toString(), contentFiles);
        }

        return messageBuilder.toString();
    }


    private String ReplaceOccurances(String messageString, Map<String, String> messageReplacements) {
        //messageReplacements.keySet().forEach(name -> name = new StringBuilder().append('%').append(name).append('%').toString());
        for (Map.Entry<String, String> entry : messageReplacements.entrySet()) {
            messageString = messageString.replaceAll(String.format("%%%s%%", entry.getKey()), entry.getValue());
        }
        return messageString;
    }

    public enum LinkFormat {
        Plain,
        HTML
    }

    /**
     * Generates link to IInformationObject to winCube (default link format is HTML)
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @return String link to InformationObject to winCube
     */
    public String GenerateLinkToWin(IInformationObject informationObject) {
        return GenerateLinkToWin(informationObject, LinkFormat.HTML);
    }

    /**
     * Generates link to IInformationObject to winCube
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @param linkFormat link format (Plain or HTML)
     * @return String link to InformationObject to winCube
     */
    public String GenerateLinkToWin(IInformationObject informationObject, LinkFormat linkFormat) {

        if (doxis4ServerID != null && !"".equals(doxis4ServerID)) {
            String link = String.format("iecm://%s/%s/%s.csb", doxis4ServerID, doxis4Session.getSystem().getInternalId(), informationObject.getID());
            //return String.format("<a href='%s'>%s</a>: %s", link, messages.getString("OpenWinClient"), link);
            if (linkFormat == LinkFormat.Plain) return link;
            return String.format("<a href='%sOpenLink.jsp?link=%s&linktext=%s'>%s</a>", webCubeJSPAddress, link, messages.getString("OpenWinClient"), messages.getString("OpenWinClient"));
        }
        return null;
    }

    /**
     * Generates link to IInformationObject to webCube (default link format is HTML)
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @return String link to InformationObject to webCube
     */
    public String GenerateLinkToWeb(IInformationObject informationObject){
        return GenerateLinkToWeb(informationObject, LinkFormat.HTML);
    }

    /**
     * Generates link to IInformationObject to webCube
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @param linkFormat link format (Plain or HTML)
     * @return String link to InformationObject to webCube
     */
    public String GenerateLinkToWeb(IInformationObject informationObject, LinkFormat linkFormat) {
        return GenerateLinkToWeb(informationObject, linkFormat, null, false, false);
    }
    public String GenerateLinkToWeb(IInformationObject informationObject, IUser inviteUser, boolean generateUser, boolean generatePassword) {
        return GenerateLinkToWeb(informationObject, LinkFormat.HTML, null, false, false);
    }
    public String GenerateLinkToWeb(IInformationObject informationObject, LinkFormat linkFormat, IUser inviteUser, boolean generateUser, boolean generatePassword) {
        if (doxis4WebCubeLink != null && !"".equals(doxis4WebCubeLink)) {
            StringBuilder linkBuilder = new StringBuilder();
            linkBuilder.append(doxis4WebCubeLink).append("&action=");
            if (informationObject.getInformationObjectType() == InformationObjectType.PROCESS_INSTANCE ||
                    informationObject.getInformationObjectType() == InformationObjectType.TASK) {
                linkBuilder.append("showtask");
            } else {
                linkBuilder.append("showdocument");
            }
            linkBuilder.append("&reusesession=1&system=").append(doxis4Session.getSystem().getName());
            if (inviteUser != null) {
                if (generateUser) {
                    linkBuilder.append("&user=").append(inviteUser.getLogin());
                }
                if (generatePassword) {
                    linkBuilder.append("&password=").append(inviteUser.getLogin());
                }
            }
            linkBuilder.append("&id=").append(informationObject.getID());

            if (linkFormat == LinkFormat.Plain) return linkBuilder.toString();
            return String.format("<a href='%s'>%s</a>", linkBuilder.toString(), messages.getString("OpenWebClient"));
        }
        return null;
    }

    /**
     * Generates link to IInformationObject to mobileCube
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @return String link to InformationObject to mobileCube
     */
    public String GenerateLinkToMobile(IInformationObject informationObject) {
        return GenerateLinkToMobile(informationObject, LinkFormat.HTML);
    }

    /**
     * Generates link to IInformationObject to mobileCube
     * @param informationObject InformationObject
     *        @see com.ser.blueline.IInformationObject
     * @param linkFormat link format (Plain or HTML)
     * @return String link to InformationObject to mobileCube
     */
    public String GenerateLinkToMobile(IInformationObject informationObject, LinkFormat linkFormat) {
        if (doxis4ServerID != null && !"".equals(doxis4ServerID)) {
            String linkAndroid = String.format("iecm://%s@%s/%s/%s.csb", doxis4ServerID, mobileCubeHost, doxis4Session.getSystem().getInternalId(), informationObject.getID());
            String linkIOS = String.format("iecm://%s/%s/%s.csb", mobileCubeHost, doxis4Session.getSystem().getInternalId(), informationObject.getID());
            if (linkFormat == LinkFormat.Plain) return linkAndroid;
            return String.format("%s: <a href='%sOpenLink.jsp?link=%s&linktext=%s'>%s</a> | <a href='%sOpenLink.jsp?link=%s&linktext=%s'>%s</a>",
                    messages.getString("OpenMobileClient"), webCubeJSPAddress, linkIOS, messages.getString("OpenMobileClient"), "iOS",
                                                                webCubeJSPAddress, linkAndroid, messages.getString("OpenMobileClient"), "Android");
        }
        return null;
    }
}
