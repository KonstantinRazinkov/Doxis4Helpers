package com.sersolutions.doxis4helpers.documents.excel;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XLSXConverter {

    /**
     * Convert CSV byte array to XLSX
     * @param strSource byte array of csv file
     * @param extension XLSX file extension (most popular: ".xlsx")
     * @param CSV_FILE_DELIMITER CSV delimiter type (e.g. ","; ";" etc.)
     * @return XLSX Workbook
     * @see org.apache.poi.ss.usermodel.Workbook
     * @throws NumberFormatException if something wrong
     */
    public Workbook convertCsvToExcel(byte[] strSource, String extension, String CSV_FILE_DELIMITER)
        throws NumberFormatException {

        Workbook workBook = null;

        // Getting BufferedReader object

        InputStream is;
        BufferedReader br;
        try {
            is = new ByteArrayInputStream(strSource);
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            // Getting XSSFWorkbook or HSSFWorkbook object based on excel file format
            if (extension.equals(".xlsx")) {
                workBook = new XSSFWorkbook();
            } else {
                workBook = new HSSFWorkbook();
            }

            Sheet sheet = workBook.createSheet("Sheet");

            String nextLine;
            int rowNum = 0;
            while ((nextLine = br.readLine()) != null) {
                Row currentRow = sheet.createRow(rowNum++);
                String[] rowData = nextLine.split(String.valueOf(CSV_FILE_DELIMITER));
                for (int i = 0; i < rowData.length; i++) {
                    if (NumberUtils.isDigits(rowData[i])) {
                        currentRow.createCell(i).setCellValue(Integer.parseInt(rowData[i]));
                    } else if (NumberUtils.isNumber(rowData[i])) {
                        currentRow.createCell(i).setCellValue(Double.parseDouble(rowData[i]));
                    } else {
                        currentRow.createCell(i).setCellValue(rowData[i]);
                    }
                }
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return workBook;
    }
}
