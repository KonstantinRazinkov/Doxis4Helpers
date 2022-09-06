package com.sersolutions.doxis4helpers.documents.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Class for working with XLSX workbooks
 */
public class WorkbookProducer {
    static public Workbook create()
    {
        return new XSSFWorkbook();
    }

    /**
     * Load XLSX workbook from file
     * @param file byte array of file
     * @return XLSX Workbook
     * @see org.apache.poi.ss.usermodel.Workbook
     * @throws Exception if method couldn't load XLSX Workbook from file
     */
    static public Workbook load(byte[] file) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(file);
        return new XSSFWorkbook(bais);
    }

    /**
     * Load XLSX workbook from file
     * @param fileName name of file that must be available by IO operations
     * @return XLSX Workbook
     * @see org.apache.poi.ss.usermodel.Workbook
     * @throws Exception if method couldn't load XLSX Workbook from file
     */
    static public Workbook load(String fileName) throws Exception {
        FileInputStream fileIn = new FileInputStream(fileName);
        return new XSSFWorkbook(fileIn);
    }

    /**
     * Save XLSX workbook to byte array
     * @param workbook XLSX Workbook
     *                 @see org.apache.poi.ss.usermodel.Workbook
     * @return byte array with file
     * @throws Exception if method couldn't save XLSX Workbook
     */
    static public byte[] saveToByteArray(Workbook workbook) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        return baos.toByteArray();
    }

    /**
     * Save XLSX workbook to byte array
     * @param workbook XLSX Workbook
     *                 @see org.apache.poi.ss.usermodel.Workbook
     * @param fileName name of file where XLSX Workbook must be saved
     * @return byte array with file
     * @throws Exception if method couldn't save XLSX Workbook
     */
    static public FileOutputStream saveToFile(Workbook workbook, String fileName) throws Exception {
        (new java.io.File(fileName)).mkdirs();
        (new java.io.File(fileName)).delete();
        FileOutputStream fileOut = new FileOutputStream(fileName);
        workbook.write(fileOut);
        fileOut.flush();
        return fileOut;
    }
}
