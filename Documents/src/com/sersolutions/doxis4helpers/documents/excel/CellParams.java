package com.sersolutions.doxis4helpers.documents.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * Class for working with cells of XLSX files
 */
public class CellParams {
    private String value;
    private String address;
    private String regionAddress;
    private String originalAddress;
    private boolean isRegion;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private String font;
    private int fontSize;
    private IndexedColors color;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private BorderStyle borderStyle;
    private boolean isWrap;
    private boolean needRefresh;


    /**
     * Init cell with address A1
     * @param value of cell
     */
    public CellParams(String value)
    {
        this(value, "A1");
    }

    /**
     * Init cell
     * @param address of cell
     * @param value of cell
     */
    public CellParams(String address, String value) {
        setValue(value);
        setAddress(address);
        color = IndexedColors.AUTOMATIC;
        setWrap(false);
    }

    /**
     * Clone cell to new object
     * @return cloned cell
     */
    @Override
    public CellParams clone()
    {
        return clone(getAddress());
    }

    /**
     * Clone cell to new cell object with other address
     * @param address new address
     * @return cloned cell
     */
    public CellParams clone (String address)
    {
        return clone(address, getValue());
    }

    /**
     * Clone cell to new cell object with other address and value
     * @param address new address
     * @param value new value
     * @return cloned cell
     */
    public CellParams clone (String address, String value)
    {
        return clone(address, value, true);
    }

    /**
     * Clone same cell to new cell object with old original address
     * @param saveOriginal save or not original address
     * @return cloned cell
     */
    public CellParams clone(boolean saveOriginal)
    {
        return clone(getAddress(), saveOriginal);
    }

    /**
     * Clone same cell to new cell object with saving of original address (to know that new cell was cloned)
     * @param address new address
     * @param saveOriginal save or not original address
     * @return cloned cell
     */
    public CellParams clone(String address, boolean saveOriginal)
    {
        return clone(address, getValue(), saveOriginal);
    }

    /**
     * Clone same cell to new cell object with saving of original address (to know that new cell was cloned)
     * @param address new address
     * @param value new value
     * @param saveOriginal save or not original address
     * @return cloned cell
     */
    public CellParams clone(String address, String value, boolean saveOriginal) {
        CellParams clone = new CellParams(address, value);
        clone.setBold(getBold());
        clone.setItalic(getItalic());
        clone.setUnderline(getUnderline());
        clone.setBorderStyle(getBorderStyle());
        clone.setFont(getFont());
        clone.setFontSize(getFontSize());
        clone.setColor(getColor());
        clone.setHorizontalAlignment(getHorizontalAlignment());
        clone.setVerticalAlignment(getVerticalAlignment());
        clone.setWrap(getWrap());
        if (saveOriginal) {
            clone.originalAddress = this.getAddress();
            clone.setNeedRefresh(false);
        } else {
            clone.originalAddress = clone.getAddress();
            clone.setNeedRefresh(true);
        }
        return clone;
    }

    public void setValue(String value) {
        if (value == null) value = "";
        this.value = value;
    }

    public void setAddress(String address) {
        if (address == null) address = "A1";
        this.address = address;
        if (this.address.contains(":")) {
            setRegionAddress(this.address);
            this.address = this.address.split(":")[0];
        }
    }

    private void setRegionAddress(String address) {
        this.regionAddress = address;
        setRegion(true);
    }

    private void setRegion(boolean isRegion)
    {
        this.isRegion = isRegion;
    }

    public void setBold(boolean isBold) {
        setNeedRefresh(true);
        this.isBold = isBold;
    }

    public void  setItalic(boolean isItalic) {
        setNeedRefresh(true);
        this.isItalic = isItalic;
    }

    public void setUnderline(boolean isUnderline) {
        setNeedRefresh(true);
        this.isUnderline = isUnderline;
    }

    public void setBorderStyle(BorderStyle borderStyle) {
        setNeedRefresh(true);
        this.borderStyle = borderStyle;
    }

    public void setFont (String font) {
        setNeedRefresh(true);
        this.font = font;
    }

    public void setFontSize(int fontSize) {
        setNeedRefresh(true);
        this.fontSize = fontSize;
    }

    public void setColor(IndexedColors color) {
        setNeedRefresh(true);
        this.color = color;
    }

    public void setHorizontalAlignment (HorizontalAlignment horizontalAlignment) {
        setNeedRefresh(true);
        this.horizontalAlignment = horizontalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        setNeedRefresh(true);
        this.verticalAlignment =verticalAlignment;
    }
    public void setNeedRefresh(boolean needRefresh)
    {
        this.needRefresh = needRefresh;
    }
    public void setWrap(boolean setWrap) {this.isWrap = setWrap;}

    public String getValue()
    {
        return this.value;
    }
    public String getAddress()
    {
        return this.address;
    }
    public String getRegionAddress()
    {
        return this.regionAddress;
    }
    public boolean getRegion()
    {
        return isRegion;
    }
    public String getOriginalAddress() {
        if (originalAddress == null) return  this.getAddress();
        return this.originalAddress;
    }
    public boolean getBold()
    {
        return this.isBold;
    }
    public boolean getItalic()
    {
        return this.isItalic;
    }
    public boolean getUnderline()
    {
        return this.isUnderline;
    }
    public BorderStyle getBorderStyle()
    {
        return this.borderStyle;
    }
    public String getFont()
    {
        return this.font;
    }
    public int  getFontSize()
    {
        return this.fontSize;
    }
    public IndexedColors getColor()
    {
        return this.color;
    }
    public HorizontalAlignment getHorizontalAlignment()
    {
        return this.horizontalAlignment;
    }
    public VerticalAlignment getVerticalAlignment()
    {
        return this.verticalAlignment;
    }
    public boolean getNeedRefresh()
    {
        return this.needRefresh;
    }
    public boolean getWrap() {return this.isWrap;}
}
