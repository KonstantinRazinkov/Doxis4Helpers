package com.sersolutions.doxis4helpers.edm.diadoc.objects;

import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;
import Diadoc.Api.Proto.Events.DiadocMessage_PostApiProtos;
import Diadoc.Api.Proto.OrganizationProtos;
import com.google.protobuf.ByteString;
import com.ser.blueline.IInformationObject;
import com.sersolutions.doxis4helpers.commons.*;
import com.sersolutions.doxis4helpers.commons.types.*;
import com.sersolutions.doxis4helpers.edm.diadoc.DiadocConnector;
import com.sersolutions.doxis4helpers.edm.datatypes.EDMDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendMessageProcess {
    private DiadocConnector connector;
    private List<EDMDocument> EDMDocuments;

    private OrganizationProtos.Box senderBox;
    private OrganizationProtos.Box recieverBox;
    private String message;
    private Boolean isDraft;
    private String state;

    public SendMessageProcess(DiadocConnector connector) throws Exception
    {
        DiadocConnector.SelfCheck(connector);
        this.connector = connector;
        EDMDocuments = new ArrayList<EDMDocument>();
        this.isDraft = true;
    }


    public void AddDocument(byte[] content, String docName, String docNum, String docDate, String docType) throws Exception
    {
        EDMDocument newDoc = new EDMDocument(docName, content, docNum, docDate, docType);
        EDMDocuments.add(newDoc);
    }

    public void AddDocuments(IInformationObject source) throws Exception
    {
        AddDocuments(source, null, null, null);
    }
    public void AddDocuments(IInformationObject source, String docNumDescriptor, String docDateDescriptor, String docTypeDescriptor) throws Exception
    {
        if (source == null)
        {
            throw new Exception("Information object cannot be null!");
        }


        String docNum=null;
        String docDate=null;
        String docType=null;

        if (docNumDescriptor == null || "".equals(docNumDescriptor))
        {
            docNumDescriptor = "ObjectNumber";
        }
        if (docDateDescriptor == null || "".equals(docDateDescriptor))
        {
            docDateDescriptor = "ObjectDate";
        }
        if (docTypeDescriptor == null || "".equals(docTypeDescriptor))
        {
            docTypeDescriptor = "ObjectType";
        }

        List<ContentFile> contentFiles = InformationObjects.GetAllContent(source);

        Map<String,String> descriptors = Descriptors.GetAllDescriptors(source);

        for (Map.Entry<String, String> entry : descriptors.entrySet())
        {
            if (entry.getKey().toUpperCase().contains(docNumDescriptor.toUpperCase()))
            {
                docNum = entry.getValue();
            }
            if (entry.getKey().toUpperCase().contains(docDateDescriptor.toUpperCase()))
            {
                docDate = entry.getValue();
            }
            if (entry.getKey().toUpperCase().contains(docTypeDescriptor))
            {
                docType = entry.getValue();
            }
        }

        for (ContentFile contentFile : contentFiles)
        {

            EDMDocument newDoc = new EDMDocument(contentFile.GetFileName(), contentFile.GetContent(), docNum, docDate, docType);
            if (contentFile.GetSignatures() != null)
            {
                Signature signature = contentFile.GetSignatures()[0];
                newDoc.SetSignature(signature.GetSignature(), signature.GetSignatureType());
            }
            EDMDocuments.add(newDoc);
        }
    }

    public OrganizationProtos.Box GetSenderBox() {return senderBox;}
    public OrganizationProtos.Box GetRecieverBox() {return recieverBox;}
    public String GetMessage() {return message;}

    public void SetSenderBox(OrganizationProtos.Box box)
    {
        this.senderBox = box;
    }
    public void SetRecieverBox(OrganizationProtos.Box box)
    {
        this.recieverBox = box;
    }
    public void SetMessage(String message)
    {
        this.message = message;
    }

    public static Boolean Check(SendMessageProcess process) throws Exception
    {
        StringBuilder problems = new StringBuilder();
        if (process.EDMDocuments.size() == 0)
        {
            problems.append("\nDocuments loaded: 0");
        }
        if (process.senderBox == null)
        {
            problems.append("\nSender box not defined");
        }
        if (process.recieverBox == null)
        {
            problems.append("\nReciever box not defined");
        }
        if (problems.length() > 1)
        {
            throw new Exception(String.format("%s is not ready:%s", SendMessageProcess.class.getName(), problems.toString()));
        }
        return true;
    }

    public void SetState(String state)
    {
        this.state = state;
    }


    public void SetIsDraft(Boolean isDraft)
    {
        this.isDraft = isDraft;
        if (isDraft)
        {
            SetState("DRAFTNOTSIGNED");
        }
        else
        {
            SetState("OUTBOXNOTSIGNED");
        }
    }

    public String Send() throws Exception
    {
        return Send(this.isDraft);
    }
    public String Send(Boolean isDraft) throws Exception
    {
        if (!Check(this)) return null;

        SetIsDraft(isDraft);

        DiadocMessage_PostApiProtos.MessageToPost.Builder messageBuilder =  DiadocMessage_PostApiProtos.MessageToPost.newBuilder();
        messageBuilder.setFromBoxId(senderBox.getBoxId());
        messageBuilder.setToBoxId(recieverBox.getBoxId());
        messageBuilder.setIsDraft(this.isDraft);
        for (EDMDocument doc : EDMDocuments)
        {
            if (doc.getType() == EDMDocument.Type.CONTRACT)
            {

                DiadocMessage_PostApiProtos.NonformalizedAttachment.Builder attachmentBuilder = messageBuilder.addNonformalizedDocumentsBuilder();
                DiadocMessage_PostApiProtos.SignedContent.Builder signedContentBuilder = attachmentBuilder.getSignedContentBuilder();


                if (doc.haveSignature())
                {

                   // String shelfName = connector.GetApi().UploadFileToShelf(doc.getContent());
                //    signedContentBuilder.setSignature(ByteString.copyFrom(doc.getSignature()));
                    signedContentBuilder.setSignWithTestSignature(true);
                 //   signedContentBuilder.setNameOnShelf(shelfName);

                }
                else
                {
                    //signedContentBuilder.setContent(ByteString.copyFrom(doc.getContent()));
                }
                signedContentBuilder.setContent(ByteString.copyFrom(doc.getContent()));

                attachmentBuilder.setFileName(doc.getFileName());
                attachmentBuilder.setDocumentNumber(doc.getNumber());
                attachmentBuilder.setDocumentDate(doc.getDate());

                messageBuilder.setNonformalizedDocuments(0, attachmentBuilder);
            }
        }

        DiadocMessage_PostApiProtos.MessageToPost messageToPost = messageBuilder.build();
        DiadocMessage_GetApiProtos.Message postedMessage = connector.GetApi().getMessageClient().postMessage(messageToPost);
        
        return postedMessage.getMessageId();
    }


    public String GetState() {return state;}

}
