package com.sersolutions.doxis4helpers.documents;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
import com.ser.blueline.IDocument;
import com.ser.blueline.IDocumentPart;
import com.ser.blueline.IRepresentation;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;

/**
 * Class for counting pages in documents (Supported formats: PDF, TIFF, TIF, XLSX, XLS, DOCX, DOC, PPTX, PPT, JPG, JPEG, PNG, BMP)
 */
public class PagesCounter {

    /**
     * Get count of pages from file
     * @param file byte array of document
     * @param fileName name of document
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(byte[] file, String fileName) throws Exception {
        long pagesCount=0;
        String fileFormat = fileName.substring(fileName.length()-4).toUpperCase();
        Integer sheetNums=-1;
        switch (fileFormat) {
            case ".PDF":
                PdfReader pdfReader = new PdfReader(file);
                pagesCount += pdfReader.getNumberOfPages();
                pdfReader.close();
                break;
            case "TIFF":case ".TIF":
                pagesCount+= TiffImage.getNumberOfPages(new RandomAccessFileOrArray(file));
                break;
            case "XLSX":
                XSSFWorkbook xwb = new XSSFWorkbook(new ByteArrayInputStream(file));
                sheetNums = xwb.getNumberOfSheets();
                if (sheetNums > 0) {
                    for (int sheetNum = 0; sheetNum < sheetNums; sheetNum++) {
                        pagesCount+= xwb.getSheetAt(sheetNum).getRowBreaks().length + 1;
                    }
                }
                break;
            case ".XLS":
                HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(file));
                sheetNums = workbook.getNumberOfSheets();
                if (sheetNums > 0) {
                    for (int sheetNum = 0; sheetNum < sheetNums; sheetNum++) {
                        pagesCount+= workbook.getSheetAt(sheetNum).getRowBreaks().length + 1;
                    }
                }
                break;
            case "DOCX":
                //XSLFSlideShow xdocument = new XSLFSlideShow(OPCPackage.open(new ByteArrayInputStream(documentPart.getRawData())));
                XWPFDocument docx = new XWPFDocument(OPCPackage.open(new ByteArrayInputStream(file)));
                pagesCount+= docx.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
                break;
            case ".DOC":
                HWPFDocument wordDoc = new HWPFDocument(new ByteArrayInputStream(file));
                pagesCount +=  wordDoc.getSummaryInformation().getPageCount();
                break;
            case "PPTX":
                //XSLFSlideShow xdocument = new XSLFSlideShow(OPCPackage.open(new ByteArrayInputStream(documentPart.getRawData())));
                XMLSlideShow xslideShow = new XMLSlideShow(OPCPackage.open(new ByteArrayInputStream(file)));
                pagesCount+=  xslideShow.getSlides().size();
                break;
            case ".PPT":
                HSLFSlideShow document = new HSLFSlideShow(new ByteArrayInputStream(file));
                pagesCount+= document.getSlides().size();
                break;
            case ".JPG":case "JPEG":case ".PNG":case ".BMP":
                pagesCount+= 1;
                break;
            default:
                throw new Exception(String.format("Can't count pages because of unsupported format (%s)", fileFormat));

        }
        return pagesCount;
    }

    /**
     * Get count of pages from Document part from Doxis4
     * @param documentPart IDocumentPart of Document from Doxis4
     *                     @see com.ser.blueline.IDocumentPart
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(IDocumentPart documentPart) throws Exception {
       return Count(documentPart.getRawData(), documentPart.getFilename());
    }

    /**
     * Get count of pages from Document from Doxis4
     * @param document IDocument from Doxis4
     *                 @see com.ser.blueline.IDocument
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(IDocument document) throws Exception {
        return Count (document, 0, false);
    }

    /**
     * Get count of pages from default representation of Document from Doxis4
     * @param document IDocument from Doxis4
     *                 @see com.ser.blueline.IDocument
     * @param skipExceptions if need to skip exception
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(IDocument document, boolean skipExceptions) throws Exception {
        return Count (document, 0, skipExceptions);
    }

    /**
     * Get count of pages of selected representation of Document from Doxis4
     * @param document IDocument from Doxis4
     *                 @see com.ser.blueline.IDocument
     * @param representationNo number of representation
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(IDocument document, int representationNo) throws Exception {
        return Count (document, representationNo, false);
    }

    /**
     * Get count of pages of selected representation of Document from Doxis4
     * @param document IDocument from Doxis4
     *                 @see com.ser.blueline.IDocument
     * @param representationNo number of representation
     * @param skipExceptions if needed to skip exception
     * @return count of pages
     * @throws Exception if something goes wrong
     */
    public static long Count(IDocument document, int representationNo, boolean skipExceptions) throws Exception {
        if (document == null) throw new Exception("Document is null!");
        if (document.getRepresentationCount() < representationNo) throw new Exception(String.format("Document have not representation %d", representationNo));
        IRepresentation representation = document.getRepresentation(representationNo);
        long pagesCount = 0;
        for (int docPartNo = 0; docPartNo < representation.getPartDocumentCount(); docPartNo++) {
            try {
                pagesCount += Count(representation.getPartDocument(docPartNo));
            } catch (Exception ex) {
                if (!skipExceptions) throw ex;
            }
        }
        return pagesCount;
    }
}
