package com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes;

/**
 * Special data type to store information about office document field at current moment
 */
public class TemplateField {
    String fieldName;
    int fieldPosition;

    TemplateField(String fieldName, int fieldPosition) {
        this.fieldName = fieldName;
        this.fieldPosition = fieldPosition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getFieldPosition() {
        return fieldPosition;
    }

    public void setFieldPosition(int fieldPosition) {
        this.fieldPosition = fieldPosition;
    }

}
