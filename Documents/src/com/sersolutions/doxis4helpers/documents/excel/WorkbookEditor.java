package com.sersolutions.doxis4helpers.documents.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for editing XLSX workbooks
 * WARNING: this class is in beta-version. It does not work 100% correct (meaning auto-height).
 */
public class WorkbookEditor {

    /**
     * Auto height for all sheets and all rows in workbook
     * @param workbook Workbook object
     * @see org.apache.poi.ss.usermodel.Workbook
     */
    static public void setRowsAutoHeight(Workbook workbook){
        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            Sheet sheet = workbook.getSheetAt(j);
            if (sheet != null) {
                for (int i = 0; i < sheet.getLastRowNum() - sheet.getFirstRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    setRowAutoHeight(sheet, row);
                }
            }
        }
    }

    /**
     * Auto height for all rows on workbook sheet
     * @param sheet Sheet object
     * @see org.apache.poi.ss.usermodel.Sheet
     */
    static public void setRowsAutoHeight(Sheet sheet){
        for (int i = 0; i < sheet.getLastRowNum() - sheet.getFirstRowNum(); i++){
            Row row = sheet.getRow(i);
            setRowAutoHeight(sheet, row);
        }
    }

    /**
     * Auto height for all rows on workbook sheet
     * @param workbook Workbook object
     * @param sheetName name of sheet
     * @see org.apache.poi.ss.usermodel.Workbook
     */
    static public void setRowsAutoHeight(Workbook workbook, String sheetName){
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet != null) {
            for (int i = 0; i < sheet.getLastRowNum() - sheet.getFirstRowNum(); i++) {
                Row row = sheet.getRow(i);
                setRowAutoHeight(sheet, row);
            }
        }
    }

    /**
     * Auto height for exact row on workbook sheet
     * @param workbook Workbook object
     * @param sheetName name of sheet
     * @param rowNum row number to auto-height
     * @see org.apache.poi.ss.usermodel.Workbook
     */
    static public void setRowAutoHeight(Workbook workbook, String sheetName, Integer rowNum){
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet != null) {
            Row row = sheet.getRow(rowNum);
            setRowAutoHeight(sheet, row);
        }
    }

    /**
     * Auto height for all rows on workbook sheet
     * @param sheet Sheet object
     * @param rowNum row number to auto-height
     * @see org.apache.poi.ss.usermodel.Sheet
     */
    static public void setRowAutoHeight(Sheet sheet, Integer rowNum){
        Row row = sheet.getRow(rowNum);
        setRowAutoHeight(sheet, row);
    }

    static public void setRowAutoHeight(Sheet sheet, Row row){
        String[] chars = "".split("");
        int length = 0;
        if (row != null) {
            float neededHeight = 0;
            String cellAddress = "";
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null) {
                    String cellValue = "";
                    List<String> cellRangeAddresses = new ArrayList<>();
                    for (CellRangeAddress cellRangeAddress : sheet.getMergedRegions()) {
                        cellRangeAddresses.add(cellRangeAddress.formatAsString());
                    }
                    for (String cellRangeAddress : cellRangeAddresses) {
                        if (cellRangeAddress.split(":")[0].equals(cell.getAddress().formatAsString())) cellAddress = cellRangeAddress;
                    }
                    if (StringUtils.isBlank(cellAddress)) cellAddress = cell.getAddress().formatAsString();
                    try {
                        cellValue = cell.getStringCellValue();
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    if (cellValue.length() > length) {
                        chars = cellValue.split("");
                        length = cellValue.length();
                    }
                    int firstRow, lastRow;
                    int cellsInRow = 1;
                    int cellsInColumn = 1;
                    int cellWidth = 0;
                    char firstLetter, lastLetter;
                    char[] letters;
                    int start_alphabet = 64;
                    if (cellAddress.contains(":")) {
                        firstRow = Integer.parseInt(cellAddress.split(":")[0].substring(1));
                        lastRow = Integer.parseInt(cellAddress.split(":")[1].substring(1));
                        cellsInRow = lastRow - firstRow + 1;
                        cellsInColumn = Character.codePointAt(cellAddress.split(":")[1].substring(0, 1), 0) -
                                Character.codePointAt(cellAddress.split(":")[0].substring(0, 1), 0) + 1;
                        firstLetter = cellAddress.split(":")[0].substring(0, 1).charAt(0);
                        lastLetter = cellAddress.split(":")[1].substring(0, 1).charAt(0);
                        letters = new char[lastLetter - firstLetter + 1];
                    } else {
                        firstLetter = cellAddress.substring(0, 1).charAt(0);
                        lastLetter = cellAddress.substring(0, 1).charAt(0);
                        letters = new char[1];
                    }
                    int num = 0;
                    for (char c = firstLetter; c <= lastLetter; c++) {
                        letters[num] = c;
                        num++;
                    }
                    for (int a = 0; a < letters.length; a++){
                        int colNum = (int)letters[a] - start_alphabet;
                        cellWidth = cellWidth + sheet.getColumnWidth(colNum - 1);
                    }
                    int colwidthinchars = (int) ((cellWidth * cellsInRow) / 256);
                    colwidthinchars = (int) Math.round(colwidthinchars * 5f / 4f);

                    int neededrows = 1;
                    int counter = 0;
                    for (int i = 0; i < chars.length; i++) {
                        counter++;
                        if (counter == colwidthinchars) {
                            neededrows++;
                            counter = 0;
                        } else if ("\n".equals(chars[i])) {
                            neededrows++;
                            counter = 0;
                        }
                    }


                    //get default row height
                    float defaultrowheight = sheet.getDefaultRowHeightInPoints();

                    if (neededHeight < (((float) neededrows * defaultrowheight) / cellsInRow)) neededHeight = ((float) neededrows * defaultrowheight) / cellsInRow;
                }
            }

            row.setHeightInPoints(neededHeight);
        }
    }

}
