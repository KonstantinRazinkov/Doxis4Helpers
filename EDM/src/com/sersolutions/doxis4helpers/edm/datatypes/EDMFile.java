package com.sersolutions.doxis4helpers.edm.datatypes;

import Diadoc.Api.DiadocApi;
import Diadoc.Api.Proto.DocumentDirectionProtos;
import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;
import Diadoc.Api.Proto.OrganizationProtos;

import java.util.Arrays;

/**
 * Special data type to store information about file stored in EDM-system
 */
public class EDMFile extends EDMEventRecord {
    public static DiadocApi diadocApi;


    private String fileName;

    private String total;
    private String vat;
    private byte[] content;


    public EDMFile(byte[] content, String fileName, TechType techType) throws Exception
    {
        setContent(content);
        setFileName(fileName);
        setTechType(techType);
    }

    public EDMFile(String eventID, String messageID, DiadocMessage_GetApiProtos.Entity entity, DocumentProtos.Document documentInfo, byte[] content, TechType techType) throws Exception
    {
        this(eventID, messageID, entity, documentInfo, entity.getFileName(), content, techType);
    }
    public EDMFile(String eventID, String messageID, DiadocMessage_GetApiProtos.Entity entity, DocumentProtos.Document documentInfo, String fileName, byte[] content, TechType techType) throws Exception
    {
        super(eventID, messageID, documentInfo, entity);
        setContent(content);
        setFileName(fileName);
        setTechType(techType);
    }

    @Override
    public String toString() {
        return "EDMFile{" +
                "entityID='" + getEntityID() + '\'' +
                ", parentEntityID='" + getParentEntityID() + '\'' +
                ", PackageID='" + getPackageID() + '\'' +
                ", fileName='" + getFileName() + '\'' +
                ", techType='" + getTechType() + '\'' +
                ", docType='" + getDocType()+ '\'' +
                "}";
    }

    public String getFileName()
    {
        return fileName;
    }
    public byte[] getContent()
    {
        return this.content;
    }

    public void setFileName(String name) throws Exception
    {
        if (name == null) name = "";// throw new Exception("Document name cannot be null!");
        if (name.contains("\\"))
        {
            int index = name.lastIndexOf("\\");
            name = name.substring(index + 1);
        }

        if (name.contains("/"))
        {
            int index = name.lastIndexOf("/");
            name = name.substring(index + 1);
        }
        this.fileName = name;
    }
    public void setContent(byte[] content) throws Exception
    {
        if (content == null) throw new Exception("Content cannot be null!");
        if (content.length == 0) throw new Exception("Content cannot be 0 size");
        this.content = content;
    }

    public String getTotal() { return total;}
    public String getVat() {return vat;}

    public void setTotal(String total) { this.total = total; }

    public void setVat(String vat) { this.vat = vat; }

}
