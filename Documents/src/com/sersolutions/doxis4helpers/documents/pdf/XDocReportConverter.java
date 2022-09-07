package com.sersolutions.doxis4helpers.documents.pdf;

import com.ser.blueline.IDocumentPart;
import fr.opensagres.poi.xwpf.converter.core.IXWPFConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Converter that using Apache POI (ONLY FOR DOCX WORD-FILES!)
 */
public class XDocReportConverter {
    /**
     * Convert from Doxis4
     * @param documentPart Doxis4 Document part that contains DOCX file
     * @return PDF as byte array
     * @throws Exception if something goes wrong
     */
    public static byte[] ConvertFromDocumentPart(IDocumentPart documentPart) throws Exception {
        byte[] out = null;
        if (documentPart.getFilename()!= null && documentPart.getFilename().toLowerCase().contains(".docx")) {
            try {
                out = ConvertFromDocx(documentPart.getRawData());
            } catch (Exception ex) {
                out = null;
            }
        }
        return out;
    }

    /**
     * Convert from byte array of DOCX file
     * @param originalDocxFile byte array with DOCX-file
     * @return PDF as byte array
     * @throws Exception if something goes wrong
     */
    public static byte[] ConvertFromDocx(byte[] originalDocxFile) throws Exception {

        XWPFDocument docxFile=new XWPFDocument(new ByteArrayInputStream(originalDocxFile));

        ByteArrayOutputStream outStream = ConvertFromDocx(docxFile);
        byte[] out = outStream.toByteArray();
        return out;
    }

    /**
     * Convert Apache POI Word docx object to PDF
     * @param docxFile Apache POI Word docx object
     * @return PDF as byte array
     * @throws Exception if something goes wrong
     */
    public static ByteArrayOutputStream ConvertFromDocx(XWPFDocument docxFile) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        IXWPFConverter pdfConverter = fr.opensagres.poi.xwpf.converter.pdf.PdfConverter.getInstance();
        fr.opensagres.poi.xwpf.converter.pdf.PdfOptions  options = fr.opensagres.poi.xwpf.converter.pdf.PdfOptions.create();
        pdfConverter.convert(docxFile,outputStream, options);
        return outputStream;
    }

}
