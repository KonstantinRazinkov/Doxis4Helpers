package com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data type to store and process List of fields from office-document
 */
public class TemplateFieldCascade {
    String nameSpace;

    ArrayList<TemplateField> fields;

    public String getNameSpace() {
        return nameSpace;
    }

    public ArrayList<TemplateField> getFields() {
        return fields;
    }

    /**
     * Parse namespace to get real address of the field
     * @param textFromDoxis4 namespace string from Doxis4 descriptor mapping table
     * @return String with nameSpace
     */
    public String parseNameSpace(String textFromDoxis4) {
        boolean foundNameSpace = false;
        Pattern p = Pattern.compile("'.*'");
        Matcher m = p.matcher(textFromDoxis4);
        if (m.find()) {
            foundNameSpace=true;
            nameSpace = m.group(0).replaceAll("'", "");
        }
        if (!foundNameSpace) nameSpace = textFromDoxis4;
        return nameSpace;
    }

    /**
     * Parse fields to get field cascade
     * @param textFromDoxis4 field string from Doxis4 descriptor mapping table
     * @return Array List of TemplateField
     *                       @see com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.TemplateField
     */
    public ArrayList<TemplateField> parseFields(String textFromDoxis4) {
        String[] fieldsForParse = textFromDoxis4.split("/");
        for (String fieldForParse : fieldsForParse) {
            try {

                if (fieldForParse.length() <=1) continue;

                String[] details = fieldForParse.split(":");
                details = details[1].split("\\[");
                if (this.fields == null) this.fields = new ArrayList<>(fieldsForParse.length);
                this.fields.add(new TemplateField(details[0], Integer.parseInt(details[1].replace("]", ""))-1));
            } catch (Exception ex) {}

        }

        if (this.fields == null) {
            this.fields = new ArrayList<>(1);
            this.fields.add(new TemplateField(textFromDoxis4, 0));
        }

        return this.fields;
    }
}
