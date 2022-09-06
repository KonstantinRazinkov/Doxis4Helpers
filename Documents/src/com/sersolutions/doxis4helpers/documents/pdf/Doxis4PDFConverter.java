package com.sersolutions.doxis4helpers.documents.pdf;

import com.ser.blueline.IDocument;
import com.ser.blueline.IDocumentPart;
import com.ser.blueline.ISession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Easy converter from any type of document to PDF
 */
public class Doxis4PDFConverter {
    private ISession doxis4Session;
    private String linkToImagingServiceConnector;
    static Logger logger = LogManager.getLogger (Doxis4PDFConverter.class.getName());
    ImagingServiceConverter.Library library = ImagingServiceConverter.Library.itextpdf;
    int quality = -1;
    boolean enablePOIConvert = false;

    /**
     * Initialize converter
     * @param doxis4Session Doxis4 session object
     * @throws Exception
     */
    public Doxis4PDFConverter(ISession doxis4Session) throws Exception {
        if (doxis4Session == null) throw new Exception("Can't initialize Doxis2PDFConverter without Doxis4 session");
        if (!doxis4Session.isValidOnServer()) throw new Exception("Doxis4 session is not valid!");

        this.doxis4Session = doxis4Session;
    }

    public ImagingServiceConverter.Library getLibrary() {
        return library;
    }

    /**
     * Change processing library
     * @param library you can select the preferable library (itextpdf or pdfbox)
     *                                              @see com.sersolutions.doxis4helpers.documents.pdf.ImagingServiceConverter.Library
     */
    public void setLibrary(ImagingServiceConverter.Library library) {
        this.library = library;
    }

    public int getQuality() {
        return quality;
    }

    /**
     * Set quality of output images from Doxis4 Imaging Service
     * @param quality quality in DPI
     */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isEnablePOIConvert() {
        return enablePOIConvert;
    }

    /**
     * Enable special convert mode by Apache POI (only for Word documents!)
     * @param enablePOIConvert boolean value
     */
    public void setEnablePOIConvert(boolean enablePOIConvert) {
        this.enablePOIConvert = enablePOIConvert;
    }

    /***
     * Easy to use function to get PDF file by link to Doxis4 document (first representation, first document part)
     * @param documentID Doxis4 Document ID
     * @return PDF as byte array
     * @throws Exception
     */
    public byte[] GetPDFFileFromDoxis4Document(String documentID) throws Exception {
        return GetPDFFileFromDoxis4Document(documentID, 0, 0);
    }

    /**
     * Get PDf file by link to Doxis4 document with number of representation and part document
     * @param documentID Doxis4 DocumentID
     * @param representation Number of representation (0-based index)
     * @param partdocument Number of document part (0-based index)
     * @return PDF as byte array
     * @throws Exception
     */
    public byte[] GetPDFFileFromDoxis4Document(String documentID, int representation, int partdocument) throws Exception {
        IDocument document = doxis4Session.getDocumentServer().getDocument4ID(documentID, doxis4Session);
        if (document == null) throw  new Exception("Can't find document by ID");
        return GetPDFFileFromDoxis4Document(document, representation, partdocument);
    }

    /**
     * Get PDF file by Doxis4 document object (first representation, first document part)
     * @param doxis4Document Doxix4 Document object
     * @return PDF as byte array
     * @throws Exception
     */
    public byte[] GetPDFFileFromDoxis4Document(IDocument doxis4Document) throws Exception {
        return GetPDFFileFromDoxis4Document(doxis4Document, 0, 0);
    }

    /**
     * Get PDF file by Doxis4 document object with number of representation and part document
     * @param doxis4Document Doxix4 Document object
     * @param representation Number of representation (0-based index)
     * @param partdocument Number of document part (0-based index)
     * @return PDF as byte array
     * @throws Exception
     */
    public byte[] GetPDFFileFromDoxis4Document(IDocument doxis4Document, int representation, int partdocument) throws Exception {
        return GetPDFFileFromDoxis4DocumentPart(doxis4Document.getPartDocument(representation, partdocument));
    }

    /**
     * Get PDF file by Doxis4 DocumentPart
     * @param documentPart Doxis4 DocumentPart object
     * @return PDF as byte array
     * @throws Exception
     */
    public byte[] GetPDFFileFromDoxis4DocumentPart(IDocumentPart documentPart) throws Exception {
        byte[] result = null;

        if (enablePOIConvert) {
            try {
                result = XDocReportConverter.ConvertFromDocumentPart(documentPart);
            } catch (Exception ex) {
                logger.error(String.format("Error while converting by Apache POI: %s", ex.getMessage()));
            } catch (Throwable throwable) {
                logger.error(String.format("Error while using Apache POI: %s", throwable.getMessage()));
            }
            if (result != null && result.length > 10) return result;
        }

        result = ImagingServiceConverter.ConvertFromDocumentPart(doxis4Session, documentPart, library, quality);
        return result;
    }
}
