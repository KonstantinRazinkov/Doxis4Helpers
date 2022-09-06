package com.sersolutions.doxis4helpers.documents.pdf;

import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.ser.blueline.IDocumentPart;
import com.ser.blueline.ISession;
import com.ser.blueline.imagingservice.IImagingServiceConnector;
import com.ser.blueline.imagingservice.LoadState;
import com.ser.blueline.imagingservice.TargetFormat;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Converter that uses Doxis4 Imaging Service (WITHOUT TEXT LAYER!!!)
 */
public class ImagingServiceConverter {

    /**
     * Library for creation final PDF file
     */
    public enum Library {
        itextpdf,
        pdfbox
    }

    /**
     * Get PDF document
     * @param doxis4Session Doxis4 session object
     * @param documentPart Doxis4 documentPart object
     * @param library Selected library
     * @param quality Quality of final file
     * @return PDF as byte array
     * @throws Exception
     */
    public static byte[] ConvertFromDocumentPart(ISession doxis4Session, IDocumentPart documentPart, Library library, int quality) throws Exception {
        if (doxis4Session.getImagingServiceURL() == null || "".equals(doxis4Session.getImagingServiceURL())) throw new Exception("Cant convert without Imaging Service URL");

        IImagingServiceConnector ics = documentPart.getImagingServiceConnector(doxis4Session);
        if (ics.getLoadState() != LoadState.LOADED) {

            ics.load(0);
            while (ics.getRenderObjectInfo().getPageCountProgress().toString().contains("PROGRESS")) {
                Thread.sleep(100);
            }
        }

        int pageNo=0;
        boolean hasnextpage = true;
        List<byte[]> images = new LinkedList<>();

        boolean breaked=false;
        while (ics.hasPage(pageNo) && !breaked) {
            InputStream image = ics.getStream(ics.renderPage(pageNo, -1, -1, quality, TargetFormat.PNG));
            if (image == null) breaked = true;
            images.add(IOUtils.toByteArray(image));
            pageNo++;
        }

        byte[] converted = null;
        switch (library) {
            case itextpdf:
                converted = ConvertFromImagesStreamsByITextPDF(images);
                ics.unload();
                return converted;
            case pdfbox:
                converted = ConvertFromImagesStreamsByPDFBox(images);
                ics.unload();
                return converted;
        }
        ics.unload();
        return null;
    }

    /**
     * Create PDF with iTextPDF
     * @param images list of images as byte arrays
     * @return PDF as byte array
     * @throws Exception
     */
    public static byte[] ConvertFromImagesStreamsByITextPDF(List<byte[]> images) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4, 20.0f, 20.0f, 20.0f, 150.0f);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        document.open();
        for (byte[] image : images) {
            Image iimage = Image.getInstance(image);
            if (iimage.getWidth() < iimage.getHeight())
                iimage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            else {
                iimage.scaleToFit(PageSize.A4.getHeight(), PageSize.A4.getWidth());
                iimage.setRotationDegrees(90);
            }
            document.setPageSize(PageSize.A4);
            document.newPage();
            iimage.setAbsolutePosition(0, 0);
            document.add(iimage);
        }
        document.close();
        pdfWriter.flush();
        return outputStream.toByteArray();
    }

    /**
     * Create PDF with PDFBox
     * @param images list of images as byte arrays
     * @return PDF as byte array
     * @throws Exception
     */

    public static byte[] ConvertFromImagesStreamsByPDFBox(List<byte[]> images) throws Exception {
        PDDocument document = new PDDocument();
        int pageNumber=0;
        for (byte[] image : images) {
            pageNumber++;
            BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(image));
            float width = bimg.getWidth();
            float height = bimg.getHeight();
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);

            PDImageXObject pdImage =  PDImageXObject.createFromByteArray(document, image, String.format("page-%d.png", pageNumber));
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                // contentStream.drawImage(ximage, 20, 20 );
                // better method inspired by http://stackoverflow.com/a/22318681/535646
                // reduce this value if the image is too large
                float scale = 1f;
                contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth() * scale, pdImage.getHeight() * scale);
                contentStream.close();
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        return outputStream.toByteArray();
    }
}
