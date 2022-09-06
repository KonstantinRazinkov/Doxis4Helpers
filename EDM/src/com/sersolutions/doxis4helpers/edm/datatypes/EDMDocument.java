package com.sersolutions.doxis4helpers.edm.datatypes;

import Diadoc.Api.Proto.Documents.DocumentProtos;
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;
import Diadoc.Api.Proto.OrganizationProtos;
import com.sersolutions.doxis4helpers.edm.diadoc.DiadocConnector;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Special data type for store information about document in EDM-system
 */
public class EDMDocument {
    private DiadocConnector diadocConnector;
    private List<EDMFile> fileList=new ArrayList<>(1);
    private List<EDMSignature> signatureList=new ArrayList<>(1);
    private List<EDMEventRecord> eventRecordList=new ArrayList<>(1);
    private String fileName;
    private byte[] content;
    private byte[] signature;
    private Boolean haveSign;
    private String signatureMethod;
    private String number;
    private String date;
    private String total;
    private String vat;

    private String fromBoxID;
    private String toBoxID;
    private String fromOrgINN;
    private String toOrgINN;
    private String fromOrgName;
    private String toOrgName;
    private DocumentProtos.Document documentInfo;

    private String author;
    private String target;
    private String requesttype;


    private String state;

    private Boolean isDraft;

    public enum Type
    {
        CONTRACT, ADDENDUM, INVOICE
    }
    private Type type;


    public void AddFile(byte[] content, String fileName, EDMFile.TechType techType) throws Exception
    {
        if (fileList == null) fileList = new ArrayList<>(10);
        fileList.add(new EDMFile(content, fileName, techType));
    }
    public void AddFile(String eventID, String messageID, DiadocMessage_GetApiProtos.Entity entity, byte[] content, EDMFile.TechType techType) throws Exception
    {
        if (fileList == null) fileList = new ArrayList<>(10);
        fileList.add(new EDMFile(eventID, messageID, entity, documentInfo, content, techType));
    }
    public void AddFile(String eventID, String messageID, DiadocMessage_GetApiProtos.Entity entity, String fileName, byte[] content, EDMFile.TechType techType) throws Exception
    {
        if (fileList == null) fileList = new ArrayList<>(10);
        fileList.add(new EDMFile(eventID, messageID,entity, documentInfo, fileName, content, techType));
    }
    public void AddSignature(byte[] signature) throws Exception
    {
        if (signatureList == null) signatureList = new ArrayList<>(10);
        signatureList.add(new EDMSignature(signature));
    }
    public void AddSignature(String currentBoxID, String eventID, String messageID, DiadocMessage_GetApiProtos.Entity entity, byte[] signature) throws Exception
    {
        if (signatureList == null) signatureList = new ArrayList<>(10);
        signatureList.add(new EDMSignature(currentBoxID, eventID, messageID, documentInfo, entity, signature));
    }
    public void AddEventRecord(String eventID, String messageID,  DiadocMessage_GetApiProtos.Entity entity) throws Exception
    {
        if (eventRecordList == null) eventRecordList = new ArrayList<>(10);
        eventRecordList.add(new EDMEventRecord(eventID, messageID, documentInfo, entity));
    }
    public List<EDMFile> getFileList()
    {
        return fileList;
    }
    public List<EDMSignature> getSignatureList()
    {
        return signatureList;
    }
    public List<EDMEventRecord> getEventRecordsList()
    {
        return eventRecordList;
    }

    public void setContent(byte[] content) throws Exception
    {
        if (content == null) throw new Exception("Content cannot be null!");
        if (content.length == 0) throw new Exception("Content cannot be 0 size");
        this.content = content;
    }
    public void setDocName(String name) throws Exception
    {
        if (name == null) name = "";//throw new Exception("Document name cannot be null!");
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
    public void setDocNum(String num) throws Exception
    {
        if (num == null) throw new Exception("Document num cannot be null!");
        this.number = num;
    }
    public void setDocDate(String date) throws Exception
    {
        if (StringUtils.isBlank(date)) throw new Exception("Document date cannot be null!");
        SimpleDateFormat format;
        String dateString;
        Date   ddate = new Date();

        if (StringUtils.isNotBlank(date) && date.substring(6, date.length() - 2).equalsIgnoreCase("00")) date = date.replace("00", "20");
        try
        {
            format = new SimpleDateFormat("yyyyMMdd");
            ddate       = format.parse ( date );
        }
        catch (Exception e1)
        {
            try
            {
                format = new SimpleDateFormat("dd.MM.yyyy");
                ddate       = format.parse ( date );
            }
            catch (Exception e2)
            {
                try
                {
                    format = new SimpleDateFormat("yyyy-MM-dd");
                    ddate       = format.parse ( date );
                }
                catch (Exception e3)
                {
                    try
                    {
                        format = new SimpleDateFormat("MM/dd/yyyy");
                        ddate       = format.parse ( date );
                    }
                    catch (Exception e4)
                    {
                        throw new Exception("Can't parse document date!");
                    }
                }
            }
        }

        format = new SimpleDateFormat("dd.MM.yyyy");
        this.date = format.format(ddate);
    }
    public void setType(String type)
    {
        if (type == null || "".equals(type))
        {
            SetType(Type.CONTRACT);
            return;
        }
        if ("ADDENDUM".equalsIgnoreCase(type))
        {
            SetType(Type.ADDENDUM);
        }
        if ("INVOICE".equalsIgnoreCase(type))
        {
            SetType(Type.INVOICE);
        }
    }
    public void SetType(Type type)
    {
        this.type = type;
    }

    public void SetIsDraft(Boolean isDraft)
    {
        this.isDraft = isDraft;
    }

    public void SetState(String state)
    {
        this.state = state;
    }


    public void SetSignature(byte[] signature, String signatureMethod)
    {
        this.signature = signature;
        this.signatureMethod = signatureMethod;
        this.haveSign = true;
    }


    @Override
    public String toString() {
        return "EDMDocument{" +
                "Files='" + ((fileList != null)? fileList.size() : "") + '\'' +
                ", Signatures='" + ((signatureList != null)? signatureList.size() : "") + '\'' +
                "}";
    }


    public String getAuthor() {return author;}

    public void setAuthor(String author) {this.author = author;}

    public String getTarget() {return target;}

    public void setTarget(String target) {this.target = target;}

    public String getRequesttype() {return requesttype;}

    public void setRequesttype(String requesttype) {this.requesttype = requesttype;}

    public EDMDocument(String fileName, byte[] content, String number, String date, String type) throws Exception
    {
        this.haveSign = false;
        setContent(content);
        setDocName(fileName);
        setDocDate(date);
        setDocNum(number);
        setType(type);
    }
    public EDMDocument(String fileName, byte[] content, String number, String date, String type, byte[] signature, String signatureMethod) throws Exception
    {
        this(fileName, content, number, date, type);
        SetSignature(signature, signatureMethod);
    }


    public EDMDocument(DiadocConnector connector, String fromBoxID, String toBoxID, DocumentProtos.Document documentInfo, byte[] content) throws Exception
    {
        this.diadocConnector = connector;
        //setContent(content);
        setDocumentInfo(documentInfo);
        setDocName(documentInfo.getFileName());
        setDocNum(documentInfo.getDocumentNumber());
        if (StringUtils.isNotBlank(documentInfo.getDocumentDate())) {
            setDocDate(documentInfo.getDocumentDate());
        }

        switch (documentInfo.getDocumentType())
        {
            case Invoice:
                total = documentInfo.getInvoiceMetadata().getTotal();
                vat = documentInfo.getInvoiceMetadata().getVat();
                break;
            case InvoiceRevision:
                total = documentInfo.getInvoiceRevisionMetadata().getTotal();
                vat = documentInfo.getInvoiceRevisionMetadata().getVat();
                break;
            case InvoiceCorrection:
                total = documentInfo.getInvoiceCorrectionMetadata().getTotalInc();
                vat = documentInfo.getInvoiceCorrectionMetadata().getVatInc();
                break;
            case InvoiceCorrectionRevision:
                total = documentInfo.getInvoiceCorrectionRevisionMetadata().getTotalInc();
                vat = documentInfo.getInvoiceCorrectionRevisionMetadata().getVatInc();
                break;
            case XmlAcceptanceCertificate:
                total = documentInfo.getXmlAcceptanceCertificateMetadata().getTotal();
                vat = documentInfo.getXmlAcceptanceCertificateMetadata().getVat();
                break;
            case Torg12:
                total = documentInfo.getTorg12Metadata().getTotal();
                vat = documentInfo.getTorg12Metadata().getVat();
                break;
            case Torg13:
                total = documentInfo.getTorg13Metadata().getTotal();
                vat = documentInfo.getTorg13Metadata().getVat();
                break;
            case XmlTorg12:
                total = documentInfo.getXmlTorg12Metadata().getTotal();
                vat = documentInfo.getXmlTorg12Metadata().getVat();
                break;
            case AcceptanceCertificate:
                total = documentInfo.getAcceptanceCertificateMetadata().getTotal();
                vat = documentInfo.getAcceptanceCertificateMetadata().getVat();
                break;
            case Contract:
                total = documentInfo.getContractMetadata().getContractPrice();
                vat = "";
                break;
            case SupplementaryAgreement:
                total = documentInfo.getSupplementaryAgreementMetadata().getTotal();
                vat = "";
            case UniversalCorrectionDocument:
                total = documentInfo.getUniversalCorrectionDocumentMetadata().getTotalInc();
                vat = documentInfo.getUniversalCorrectionDocumentMetadata().getVatInc();
                break;
            case UniversalTransferDocument:
                total = documentInfo.getUniversalTransferDocumentMetadata().getTotal();
                vat = documentInfo.getUniversalTransferDocumentMetadata().getVat();
                break;
        }

        setType("1");

        setFromBoxID( fromBoxID);
        setToBoxID(toBoxID);

        state = "RECIEVEDNOTSIGNED";
    }


    public String getToBoxID() {
        return toBoxID;
    }

    public void setToBoxID(String toBoxID) throws Exception {
        this.toBoxID = toBoxID;
        toOrgName = "";
        toOrgINN = "";
        if (toBoxID != null && !"".equals(toBoxID))
        {
            OrganizationProtos.Organization recieverOrg = diadocConnector.GetOrganizationByBoxID(toBoxID);
            if (recieverOrg!= null)
            {
                if (recieverOrg.getFullName() != null) toOrgName = recieverOrg.getFullName();
                if (recieverOrg.getInn() != null) toOrgINN = recieverOrg.getInn();
            }
        }


    }

    public String getFromBoxID() {
        return fromBoxID;
    }

    public void setFromBoxID(String fromBoxID) throws Exception {
        this.fromBoxID = fromBoxID;
        fromOrgName = "";
        fromOrgINN = "";
        if (fromBoxID != null && !"".equals(fromBoxID))
        {
            OrganizationProtos.Organization senderOrg = diadocConnector.GetOrganizationByBoxID(fromBoxID);
            if (senderOrg != null)
            {
                if (senderOrg.getFullName() != null) fromOrgName = senderOrg.getFullName();
                if (senderOrg.getInn() != null) fromOrgINN = senderOrg.getInn();
            }
        }
    }

    public String getFileName() {return fileName;}
    public byte[] getContent() {return content;}
    public String getNumber() {return number;}
    public String getDate() {return date;}
    public Type getType() {return type;}
    public String getSenderBoxID() {return fromBoxID;}
    public String getSenderOrgINN() {return fromOrgINN;}
    public String getSenderOrgName() {return fromOrgName;}
    public String getRecieverBoxID() {return toBoxID;}
    public String getRecieverOrgINN() {return toOrgINN;}
    public String getRecieverOrgName() {return toOrgName;}
    public Boolean getIsDraft() {return isDraft;}
    public String getState() {return state;}
    public String getTotal() { return total;}
    public String getVat() {return vat;}

    public Boolean haveSignature() {return  haveSign;}
    public byte[] getSignature() {return signature;}
    public String getSignatureMethod() {return signatureMethod;}

    public DocumentProtos.Document getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentProtos.Document documentInfo) {
        this.documentInfo = documentInfo;
    }
}
