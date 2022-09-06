package com.sersolutions.doxis4helpers.documents.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for creating XLSX cells from CellParams
 * @see com.sersolutions.doxis4helpers.documents.excel.CellParams
 * @see org.apache.poi.ss.usermodel.Cell
 */
public class CellProducer {
    private Sheet sheet;
    Map<Integer, Row> rows;
    Map<Integer, Map<Integer, Cell>> cells;
    Map<String, CellStyle> cellStyleMap;

    /**
     * Init CellProducer for special XLSX sheet
     * @param sheet XLSX sheet
     *              @see org.apache.poi.ss.usermodel.Sheet
     */
    public CellProducer(Sheet sheet){

        this.sheet = sheet;
        rows = new ConcurrentHashMap<Integer, Row>(100);
        cells = new ConcurrentHashMap<Integer, Map<Integer, Cell>>(100);
    }

    /**
     * Create XLSX Cell from CellParams
     * @param params CellParams
     * @return XLSX Cell
     */
    public Cell produceCell(CellParams params){
        Cell cell = getCell(params.getAddress());

        cell.setCellValue(params.getValue());
        cell.setCellStyle(produceCellStyle(params, params.getNeedRefresh()));

        if (params.getRegion()) {
            boolean alreadyMerged = false;
            CellRangeAddress region = CellRangeAddress.valueOf(params.getRegionAddress());
            CellRangeAddress oldMerge = null;

            for (int range = 0; range < sheet.getMergedRegions().size(); range++) {
                CellRangeAddress rangeRegion = sheet.getMergedRegion(range);
                if (rangeRegion.getFirstColumn() == region.getFirstColumn() && rangeRegion.getFirstRow() == region.getFirstRow()) {
                    oldMerge = rangeRegion;
                    alreadyMerged=true;
                    break;
                }

            }
            if (!alreadyMerged) sheet.addMergedRegion(region);

            if (params.getBorderStyle() != null) {

                RegionUtil.setBorderTop(params.getBorderStyle(), region, sheet);
                RegionUtil.setBorderLeft(params.getBorderStyle(), region, sheet);
                RegionUtil.setBorderRight(params.getBorderStyle(), region, sheet);
                RegionUtil.setBorderBottom(params.getBorderStyle(), region, sheet);
            }

            /*
            for (int col = region.getFirstColumn(); col <= region.getLastColumn(); col++)
            {
                for (int row = region.getFirstRow(); row <= region.getLastRow(); row++)
                {

                    if (col > region.getFirstColumn() && col < region.getLastColumn()
                            && row > region.getFirstRow() && row < region.getLastRow()) continue;
                    getCell(row, col).setCellStyle(produceCellStyle(params, false));
                }
            }*/
        }
        return cell;
    }

    /**
     * Adds HyperLink to Cell from CellParams
     * @param workbook Workbook
     * @param params CellParams
     * @param link String
     * @return XLSX Cell
     */
    public Cell addHyperLink(Workbook workbook, CellParams params, String link){
        Cell cell = getCell(params.getAddress());
        cell.setCellValue(params.getValue());
        Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(link);
        cell.setHyperlink(hyperlink);
        return cell;
    }

    /**
     * Get style of Cell from address
     * @param address address of cell
     * @return CellStyle of Cell
     * @see org.apache.poi.ss.usermodel.CellStyle
     */
    public CellStyle getCellStyle(String address){
        if (cellStyleMap == null) cellStyleMap = new ConcurrentHashMap<String, CellStyle>(1000);

        if (cellStyleMap.containsKey(address)) {
            return cellStyleMap.get(address);
        }

        return getCell(address).getCellStyle();
    }

    /**
     * Create XLSX CellStyle from CellParams
     * @param cellParams CellParams
     *                   @see com.sersolutions.doxis4helpers.documents.excel.CellParams
     * @param reproduce Reproduce style from original address (if this cell was cloned)
     * @return CellStyle
     * @see org.apache.poi.ss.usermodel.CellStyle
     */
    public CellStyle produceCellStyle(CellParams cellParams, boolean reproduce){
        CellStyle cellStyle = getCellStyle(cellParams.getOriginalAddress());
        if (!reproduce && cellStyle != null) {
            return cellStyle;
        }

        Font cellFont = sheet.getWorkbook().createFont();
        cellFont.setBold(cellParams.getBold());
        cellFont.setItalic(cellParams.getItalic());
        //cellFont.setUnderline(cellParams.getUnderline());
        if (cellParams.getFont() != null && !"".equals(cellParams.getFont())) cellFont.setFontName(cellParams.getFont());
        if (cellParams.getFontSize() > 0) cellFont.setFontHeightInPoints((short) cellParams.getFontSize());
        if (cellParams.getColor() != null) cellFont.setColor(cellParams.getColor().getIndex());
        cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setFont(cellFont);
        cellStyle.setWrapText(cellParams.getWrap());
        if (cellParams.getHorizontalAlignment() != null) cellStyle.setAlignment(cellParams.getHorizontalAlignment());
        if (cellParams.getVerticalAlignment() != null)  cellStyle.setVerticalAlignment(cellParams.getVerticalAlignment());

        if (cellParams.getBorderStyle() != null) {
            cellStyle.setBorderTop(cellParams.getBorderStyle());
            cellStyle.setBorderLeft(cellParams.getBorderStyle());
            cellStyle.setBorderRight(cellParams.getBorderStyle());
            cellStyle.setBorderBottom(cellParams.getBorderStyle());
        }

        if (cellStyleMap.containsKey(cellParams.getAddress())) {
            cellStyleMap.replace(cellParams.getAddress(), cellStyle);
        } else {
            cellStyleMap.put(cellParams.getAddress(), cellStyle);
        }

        return cellStyle;
    }

    /**
     * Parse alpha-numeric address (like A1, B2, ...) to Point
     * @param address alpha-numeric address
     * @return Pointed address
     *                @see java.awt.Point
     */
    public static Point parseAddress(String address){
        Point addr = new Point();
        addr.setLocation(0, 0);

        CellReference cellReference = new CellReference(address);
        addr.setLocation(cellReference.getCol(), cellReference.getRow());
        return addr;

        /*
        Pattern p = Pattern.compile("^\\p{Alpha}+");
        Matcher m = p.matcher(address);
        if (m.find())
        {
            addr.setLocation(AlphaToNumber(m.group(0)),0);
            p = Pattern.compile("\\d+");
            m = p.matcher(address);
            if (m.find())
            {
                addr.setLocation(addr.getX(), Integer.parseInt(m.group(0)));
            }
        }
        return addr;*/
    }

    /**
     * Get Row from row number
     * @param rowNo integer row number
     * @return Row
     *         @see org.apache.poi.ss.usermodel.Row
     */
    public Row getRow(int rowNo){
        if (rows.containsKey(rowNo)) return rows.get(rowNo);

        Row row = sheet.getRow(rowNo);
        if (row == null) row = sheet.createRow(rowNo);
        rows.put(rowNo, row);
        cells.put(rowNo, new ConcurrentHashMap<Integer, Cell>(100));

        return row;
    }

    /**
     * Get Cell from row and cell number
     * @param rowNo integer row number
     * @param cellNo integer cell number
     * @return Cell
     *         @see org.apache.poi.ss.usermodel.Cell
     */
    public Cell getCell(int rowNo, int cellNo){
        Row row = getRow(rowNo);
        if (cells.get(rowNo).containsKey(cellNo)) return cells.get(rowNo).get(cellNo);
        Cell cell = row.getCell(cellNo);
        if (cell == null) cell = row.createCell(cellNo);
        cells.get(rowNo).put(cellNo, cell);

        return cell;
    }

    /**
     * Get Cell from address (example: A1, B1...)
     * @param address cell address
     * @return Cell
     *         @see org.apache.poi.ss.usermodel.Cell
     */
    public Cell getCell(String address){
        Point addr = parseAddress(address);
        return getCell((int)addr.getY(), (int)addr.getX());
    }

    /**
     * Get letter column name (A, B,...AA, AB etc.) from its number
     * @param columnNumber column number
     * @return String column name
     */
    public String getExcelColumnName(int columnNumber){
        int dividend = columnNumber + 1;
        String columnName = "";
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnName = String.valueOf((char)(65 + modulo)) + columnName;
            dividend = (int)((dividend - modulo) / 26);
        }

        return columnName;
    }
}
