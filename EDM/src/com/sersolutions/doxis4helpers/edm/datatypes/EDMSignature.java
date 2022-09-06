package com.sersolutions.doxis4helpers.edm.datatypes;

import Diadoc.Api.DiadocApi;
import Diadoc.Api.Proto.Docflow.DocumentInfoProtos;
import Diadoc.Api.Proto.DocumentDirectionProtos;
import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;
import Diadoc.Api.Proto.OrganizationProtos;
import Diadoc.Api.Proto.SignatureInfoProtos;

/**
 * Special data type to store information about electronic signature of file from EDM-system
 */
public class EDMSignature extends  EDMEventRecord {


    public static DiadocApi diadocApi;
    public enum SignatureState
    {
        NOTSIGNED,
        SIGNED,
        DOUBLESIGNED
    }

    private byte[] content;

    public EDMSignature(byte[] content) throws Exception
    {
        setContent(content);
    }
    public EDMSignature(String currentBoxID, String eventID, String messageID, DocumentProtos.Document documentInfo, DiadocMessage_GetApiProtos.Entity entity, byte[] content) throws Exception
    {
        super(eventID, messageID, documentInfo, entity);
        setContent(content);
        setSignerBoxID(entity.getSignerBoxId());
        try {
            SignatureInfoProtos.SignatureInfo signatureInfo = diadocApi.getDocumentClient().getSignatureInfo(currentBoxID, messageID, entity.getEntityId());
            setAuthor(String.format("%s %s (%s)", signatureInfo.getFirstName(), signatureInfo.getSurname(), signatureInfo.getEmail()).trim());
        } catch (Exception signatureEx) {}
        if (getTechType().equals(TechType.unknown) ||
                getTechType().equals(TechType.unknownDocumentType) ||
                getTechType().equals(TechType.nonformalized)
    || getTechType().equals(TechType.original)) setTechType(TechType.signature);
        if (getDocType().equalsIgnoreCase("unkonown") ||
                getDocType().equalsIgnoreCase("UnknownDocumentType") ||
                getDocType().equalsIgnoreCase("Nonformalized")) setDocType("signature");
    }


    @Override
    public String toString() {
        return "EDMSignature{" +
                "entityID='" + getEntityID() + '\'' +
                ", parentEntityID='" + getParentEntityID() + '\'' +
                ", PackageID='" + getPackageID() + '\'' +
                "}";
    }

    public byte[] getContent()
    {
        return this.content;
    }
    public void setContent(byte[] content) throws Exception
    {
        if (content == null) throw new Exception("Content cannot be null!");
        if (content.length == 0) throw new Exception("Content cannot be 0 size");

        this.content = content;

    }

}
