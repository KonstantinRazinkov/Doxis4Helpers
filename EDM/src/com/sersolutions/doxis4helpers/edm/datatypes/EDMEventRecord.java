package com.sersolutions.doxis4helpers.edm.datatypes;

import Diadoc.Api.DiadocApi;
import Diadoc.Api.Proto.DocumentDirectionProtos;
import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.Events.*;
import Diadoc.Api.Proto.OrganizationProtos;
import de.ser.sst.shared.lang.StringUtils;

public class EDMEventRecord {

    public enum TechType
    {
        original	,
        printform	,
        protocol	,
        technicalxml	,
        signature	,
        resolution	,
        resolutionrequest	,
        resolutionrequestdenial	,
        unknown	,
        xmlSignatureRejection	,
        xmlAcceptanceCertificate	,
        xmlTorg12	,
        xmlTorg12BuyerTitle	,
        xmlAcceptanceCertificateBuyerTitle	,
        nonformalized	,
        invoice	,
        invoiceRevision	,
        invoiceCorrection	,
        invoiceCorrectionRevision	,
        torg12	,
        acceptanceCertificate	,
        trustConnectionRequest	,
        priceList	,
        priceListAgreement	,
        certificateRegistry	,
        reconciliationAct	,
        proformaInvoice	,
        contract	,
        torg13	,
        serviceDetails	,
        supplementaryAgreement	,
        universalTransferDocument	,
        universalTransferDocumentRevision	,
        universalCorrectionDocument	,
        universalCorrectionDocumentRevision	,
        unknownDocumentType	,
        invoiceReceipt 	,
        invoiceConfirmation	,
        invoiceCorrectionRequest	,
        attachmentComment 	,
        deliveryFailureNotification	,
        signatureRequestRejection	,
        signatureVerificationReport	,
        structuredData	,
        receipt	,
        revocationRequest	,
        roamingNotification	,
        customData	,
        moveDocument	,
        resolutionRouteAssignment	,
        resolutionRouteRemoval	,
        title	,
        cancellation	,
        edition	,
        deletionRestoration	,
        templateTransformation	,
        templateRefusal

    }


    public static DiadocApi diadocApi; private byte[] content;
    private String number;
    private String date;
    private String eventID;
    private String messageID;
    private String entityID;
    private String parentEntityID;
    private String rootEntityID;
    private String packageID;
    private String direction;
    private String author;
    private String target;
    private String requesttype;
    private TechType techType;
    private String docType;
    private String signerBoxID;

    public TechType getTechType()
    {
        return this.techType;
    }
    public void setTechType(TechType techType)
    {
        this.techType = techType;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }



    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getParentEntityID() {
        return parentEntityID;
    }

    public void setParentEntityID(String parentEntityID) {
        this.parentEntityID = parentEntityID;
    }

    public String getRootEntityID() {return rootEntityID;}

    public void setRootEntityID(String rootEntityID) {this.rootEntityID = rootEntityID;}

    public String getAuthor() {return author;}

    public void setAuthor(String author) {this.author = author;}

    public String getTarget() {return target;}

    public void setTarget(String target) {this.target = target;}

    public String getRequesttype() {return requesttype;}

    public void setRequesttype(String requesttype) {this.requesttype = requesttype;}

    public String getNumber() {
        return number;
    }

    public EDMEventRecord() throws Exception
    {

    }
    public EDMEventRecord(String eventID, String messageID, DocumentProtos.Document documentInfo, DiadocMessage_GetApiProtos.Entity entity) throws Exception
    {
        this();
        setEventID(eventID);
        setMessageID(messageID);
        setEntityID(entity.getEntityId());
        if (StringUtils.isNotBlank(entity.getParentEntityId()))
        {
            setParentEntityID(entity.getParentEntityId());
        }
        else
        {
            setParentEntityID(documentInfo.getEntityId());
        }

        setRootEntityID(documentInfo.getEntityId());

        setPackageID(entity.getPacketId());
        setNumber(documentInfo.getDocumentNumber());
        if (StringUtils.isNotBlank(documentInfo.getDocumentDate())) {
            setDate(documentInfo.getDocumentDate().substring(6, documentInfo.getDocumentDate().length() - 2).equalsIgnoreCase("00") ? documentInfo.getDocumentDate().replace("00", "20") : documentInfo.getDocumentDate());
        } else {
            setDate(documentInfo.getDocumentDate());
        }
        setDocType(documentInfo.getDocumentType().toString());
        setDirection(documentInfo.getDocumentDirection().toString());

        if (documentInfo.hasDocumentDirection())
        {
            setDirection(documentInfo.getDocumentDirection().toString());
        }
        else
        {
            if (diadocApi != null)
            {
                boolean isCurrentOrganisation=false;
                if (entity.getSignerBoxId() != null && !"".equals(entity.getSignerBoxId()))
                {
                    try
                    {
                        OrganizationProtos.Organization signerOrganization = diadocApi.getOrganizationClient().getBox(entity.getSignerBoxId()).getOrganization();
                        for (OrganizationProtos.Organization organization : diadocApi.getOrganizationClient().getMyOrganizations().getOrganizationsList())
                        {
                            if (signerOrganization.getOrgId().equals(organization.getOrgId()))
                            {
                                isCurrentOrganisation = true;
                                break;
                            }
                        }

                    }
                    catch (Exception ex)
                    {
                        isCurrentOrganisation = true;
                    }

                    if (isCurrentOrganisation)
                    {
                        setDirection(DocumentDirectionProtos.DocumentDirection.Outbound.toString());
                    }
                    else
                    {
                        setDirection(DocumentDirectionProtos.DocumentDirection.Inbound.toString());
                    }
                }
            }
        }

        switch (entity.getAttachmentType())
        {
            case Resolution:
                ResolutionInfoProtos.ResolutionInfo resolutionInfo = entity.getResolutionInfo();
                setAuthor(resolutionInfo.getAuthor());
                setRequesttype(resolutionInfo.getResolutionType().toString());
                setTechType(TechType.resolution);
                break;
            case ResolutionRequest:
                ResolutionRequestInfoProtos.ResolutionRequestInfo resolutionRequestInfo = entity.getResolutionRequestInfo();
                setAuthor(resolutionRequestInfo.getAuthor());
                setRequesttype(resolutionRequestInfo.getRequestType().toString());
                setTarget(resolutionRequestInfo.getTarget().getUser());
                setTechType(TechType.resolutionrequest);
                break;
            case ResolutionRequestDenial:
                ResolutionRequestDenialInfoProtos.ResolutionRequestDenialInfo resolutionRequestDenialInfo = entity.getResolutionRequestDenialInfo();
                setAuthor(resolutionRequestDenialInfo.getAuthor());
                setTechType(TechType.resolutionrequestdenial);
                //setRequesttype(resolutionRequestDenialInfo.getReq().toString());
                break;
            case XmlSignatureRejection:
                setTechType(TechType.resolutionrequestdenial);
                break;
            default:
                setTechType(TechType.unknown);
                break;
        }
    }


    @Override
    public String toString() {
        return "EDMEventRecord{" +
                "entityID='" + entityID + '\'' +
                ", parentEntityID='" + parentEntityID + '\'' +
                ", PackageID='" + packageID + '\'' +
                "}";
    }

    public byte[] getContent()
    {
        return this.content;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getHierarchy() { return (entityID.equalsIgnoreCase(parentEntityID))? "Parent" : "Child";}

    public String getSignerBoxID() {
        return signerBoxID;
    }

    public void setSignerBoxID(String signerBoxID) {
        this.signerBoxID = signerBoxID;
    }
}
