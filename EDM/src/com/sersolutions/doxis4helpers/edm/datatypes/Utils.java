package com.sersolutions.doxis4helpers.edm.datatypes;

import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos;

public class Utils {

    public static EDMEventRecord.TechType FromDiadocTechType(DiadocMessage_GetApiProtos.AttachmentType diadocTechType, EDMEventRecord.TechType defaultTechType)
    {
        switch (diadocTechType)
        {
            case Resolution:
                return EDMEventRecord.TechType.resolution;
            case ResolutionRequest:
                return EDMEventRecord.TechType.resolutionrequest;
            case ResolutionRequestDenial:
                return EDMEventRecord.TechType.resolutionrequestdenial;
            case UnknownAttachmentType:
                return EDMEventRecord.TechType.unknown;
            case XmlSignatureRejection:
                return EDMEventRecord.TechType.xmlSignatureRejection;
            case XmlAcceptanceCertificate:
                return EDMEventRecord.TechType.xmlAcceptanceCertificate;
            case XmlAcceptanceCertificateBuyerTitle:
                return EDMEventRecord.TechType.xmlAcceptanceCertificateBuyerTitle;
            case XmlTorg12:
                return EDMEventRecord.TechType.xmlTorg12;
            case XmlTorg12BuyerTitle:
                return EDMEventRecord.TechType.xmlTorg12BuyerTitle;
            case Nonformalized:
                return EDMEventRecord.TechType.nonformalized;
            case Invoice:
                return EDMEventRecord.TechType.invoice;
            case InvoiceRevision:
                return EDMEventRecord.TechType.invoiceRevision;
            case InvoiceCorrection:
                return EDMEventRecord.TechType.invoiceCorrection;
            case InvoiceCorrectionRevision:
                return EDMEventRecord.TechType.invoiceCorrectionRevision;
            case Torg12:
                return EDMEventRecord.TechType.torg12;
            case AcceptanceCertificate:
                return EDMEventRecord.TechType.acceptanceCertificate;
            case TrustConnectionRequest:
                return EDMEventRecord.TechType.trustConnectionRequest;
            case PriceList:
                return EDMEventRecord.TechType.priceList;
            case PriceListAgreement:
                return EDMEventRecord.TechType.priceListAgreement;
            case CertificateRegistry:
                return EDMEventRecord.TechType.certificateRegistry;
            case ReconciliationAct:
                return EDMEventRecord.TechType.reconciliationAct;
            case ProformaInvoice:
                return EDMEventRecord.TechType.proformaInvoice;
            case Contract:
                return EDMEventRecord.TechType.contract;
            case Torg13:
                return EDMEventRecord.TechType.torg13;
            case ServiceDetails:
                return EDMEventRecord.TechType.serviceDetails;
            case SupplementaryAgreement:
                return EDMEventRecord.TechType.supplementaryAgreement;
            case UniversalTransferDocument:
                return EDMEventRecord.TechType.universalTransferDocument;
            case UniversalTransferDocumentRevision:
                return EDMEventRecord.TechType.universalTransferDocument;
            case UniversalCorrectionDocument:
                return EDMEventRecord.TechType.universalCorrectionDocument;
            case UniversalCorrectionDocumentRevision:
                return EDMEventRecord.TechType.universalCorrectionDocumentRevision;
            case InvoiceReceipt:
                return EDMEventRecord.TechType.invoiceReceipt;
            case InvoiceConfirmation:
                return EDMEventRecord.TechType.invoiceConfirmation;
            case InvoiceCorrectionRequest:
                return EDMEventRecord.TechType.invoiceCorrectionRequest;
            case AttachmentComment:
                return EDMEventRecord.TechType.attachmentComment;
            case DeliveryFailureNotification:
                return EDMEventRecord.TechType.deliveryFailureNotification;
            case SignatureRequestRejection:
                return EDMEventRecord.TechType.signatureRequestRejection;
            case SignatureVerificationReport:
                return EDMEventRecord.TechType.signatureVerificationReport;
            case StructuredData:
                return EDMEventRecord.TechType.structuredData;
            case Receipt:
                return EDMEventRecord.TechType.receipt;
            case RevocationRequest:
                return EDMEventRecord.TechType.revocationRequest;
            case CustomData:
                return EDMEventRecord.TechType.customData;
            case MoveDocument:
                return EDMEventRecord.TechType.moveDocument;
            case ResolutionRouteAssignmentAttachment:
                return EDMEventRecord.TechType.resolutionRouteAssignment;
            case ResolutionRouteRemovalAttachment:
                return EDMEventRecord.TechType.resolutionRouteRemoval;
            case Title:
                return EDMEventRecord.TechType.title;
            case Cancellation:
                return EDMEventRecord.TechType.cancellation;
            case Edition:
                return EDMEventRecord.TechType.edition;
            default:
                if (defaultTechType == null) return EDMEventRecord.TechType.unknown;
                return defaultTechType;
        }
    }

}
