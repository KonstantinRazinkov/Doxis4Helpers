package com.sersolutions.doxis4helpers.edm.datatypes;

import com.ser.blueline.ISession;
import com.sersolutions.doxis4helpers.commons.GlobalValueLists;

/**
 * Special data type to process configuration loaded from Doxiz4
 */
public class Settings {
    String apiKey;
    String url;
    String login;
    String pass;
    String currentOrganizationInn;
    String currentOrganizationKpp;
    String documentClass;
    String documentClassDB;
    String folderClass;
    String folderClassDB;
    String folderNodeDocuments;
    String folderNodeTech;
    String folderNodeEvents;
    String descriptorClassNumber;
    String descriptorClassDate;
    String descriptorClassDiadocDate;
    String descriptorClassDiadocArrivalDate;
    String descriptorClassState;
    String descriptorClassTechType;
    String descriptorClassDocType;
    String descriptorClassDirection;
    String documentStateDescriptor;


    public Settings() {}

    public static Settings GenerateFromDoxis4(ISession session, String gvlName)
    {
        Settings settings = new Settings();

        settings.setApiKey(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "APIKEY" ,0, 1, true));
        settings.setUrl(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "URL", 0, 1, true));
        settings.setLogin(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "LOGIN", 0, 1, true));
        settings.setPass(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "PASS", 0, 1, true));
        settings.setCurrentOrganizationInn(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "CURRENTORGANIZATIONINN", 0, 1, true));
        settings.setCurrentOrganizationKpp(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "CURRENTORGANIZATIONKPP", 0, 1, true));
        settings.setDocumentClass(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTCLASS", 0, 1, true));
        settings.setDocumentClassDB(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTDATABASE", 0, 1, true));
        settings.setFolderClass(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "FOLDERCLASS", 0, 1, true));
        settings.setFolderClassDB(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "FOLDERDATABASE", 0, 1, true));
        settings.setFolderNodeDocuments(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "FOLDERNODEDOCUMENTS", 0, 1, true));
        settings.setFolderNodeTech(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "FOLDERNODETECH", 0, 1, true));
        settings.setFolderNodeEvents(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "FOLDERNODEEVENTS", 0, 1, true));
        settings.setDescriptorClassNumber(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTNUMBERDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassDiadocDate(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTDIADOCDATEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassDiadocArrivalDate(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTARRIVALDATEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassDate(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTDATEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassState(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTSTATEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassDocType(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTTYPEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassTechType(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTTECHTYPEDESCRIPTOR", 0, 1, true));
        settings.setDescriptorClassDirection(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTDIRECTIONDESCRIPTOR", 0, 1, true));
        settings.setDocumentStateDescriptor(GlobalValueLists.GetValueFromGlobalValueList(session, gvlName, "DOCUMENTSTATEDESCRIPTOR", 0, 1, true));

        return settings;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }


    public String getCurrentOrganizationInn() {
        return currentOrganizationInn;
    }

    public String getCurrentOrganizationKpp() {
        return currentOrganizationKpp;
    }

    public void setCurrentOrganizationInn(String currentOrganizationInn) {
        this.currentOrganizationInn = currentOrganizationInn;
    }

    public void setCurrentOrganizationKpp(String currentOrganizationKpp) {
        this.currentOrganizationKpp = currentOrganizationKpp;
    }

    public String getDocumentClass() {
        return documentClass;
    }

    public void setDocumentClass(String documentClass) {
        this.documentClass = documentClass;
    }

    public String getDocumentClassDB() {
        return documentClassDB;
    }

    public void setDocumentClassDB(String documentClassDB) {
        this.documentClassDB = documentClassDB;
    }

    public String getFolderClass() {
        return folderClass;
    }

    public void setFolderClass(String foldereClass) {
        this.folderClass = foldereClass;
    }

    public String getFolderClassDB() {
        return folderClassDB;
    }

    public void setFolderClassDB(String folderClassDB) {
        this.folderClassDB = folderClassDB;
    }

    public String getFolderNodeDocuments() {
        return folderNodeDocuments;
    }

    public void setFolderNodeDocuments(String folderNodeDocuments) {
        this.folderNodeDocuments = folderNodeDocuments;
    }

    public String getFolderNodeTech() {
        return folderNodeTech;
    }

    public void setFolderNodeTech(String folderNodeTech) {
        this.folderNodeTech = folderNodeTech;
    }

    public String getFolderNodeEvents() {
        return folderNodeEvents;
    }

    public void setFolderNodeEvents(String folderNodeEvents) {
        this.folderNodeEvents = folderNodeEvents;
    }

    public String getDescriptorClassNumber() {
        return descriptorClassNumber;
    }

    public void setDescriptorClassNumber(String descriptorClassNumber) {
        this.descriptorClassNumber = descriptorClassNumber;
    }

    public String getDescriptorClassDiadocDate() {
        return descriptorClassDiadocDate;
    }

    public String getDescriptorClassDiadocArrivalDate() {
        return descriptorClassDiadocArrivalDate;
    }

    public String getDescriptorClassDate() {
        return descriptorClassDate;
    }

    public void setDescriptorClassDiadocDate(String descriptorClassDiadocDate) {
        this.descriptorClassDiadocDate = descriptorClassDiadocDate;
    }

    public void setDescriptorClassDiadocArrivalDate(String descriptorClassDiadocArrivalDate) {
        this.descriptorClassDiadocArrivalDate = descriptorClassDiadocArrivalDate;
    }

    public void setDescriptorClassDate(String descriptorClassDate) {
        this.descriptorClassDate = descriptorClassDate;
    }

    public String getDescriptorClassState() {
        return descriptorClassState;
    }

    public void setDescriptorClassState(String descriptorClassState) {
        this.descriptorClassState = descriptorClassState;
    }

    public String getDescriptorClassTechType() {
        return descriptorClassTechType;
    }

    public void setDescriptorClassTechType(String descriptorClassTechType) {
        this.descriptorClassTechType = descriptorClassTechType;
    }

    public String getDescriptorClassDocType() {
        return descriptorClassDocType;
    }

    public void setDescriptorClassDocType(String descriptorClassDocType) {
        this.descriptorClassDocType = descriptorClassDocType;
    }

    public String getDescriptorClassDirection() {
        return descriptorClassDirection;
    }

    public void setDescriptorClassDirection(String descriptorClassDirection) {
        this.descriptorClassDirection = descriptorClassDirection;
    }

    public String getDocumentStateDescriptor() {
        return documentStateDescriptor;
    }

    public void setDocumentStateDescriptor(String documentStateDescriptor) {
        this.documentStateDescriptor = documentStateDescriptor;
    }
}
