package com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes;

import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.IDescriptorMapping;
import com.ser.blueline.metaDataComponents.IDescriptorMappingTable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Template {
    ISession doxis4Session;
    String templateName;
    String templateFileName;
    byte[] tempateFile;

    public Template(ISession doxis4Session)
    {
        this.doxis4Session = doxis4Session;
    }


    ConcurrentMap<String, DescriptorMapping> descriptorMappings;

    /**
     * Analyze descriptor mapping table from Doxis4 and store it to the memory
     * @param descriptorMappingTable IDescriptorMapping from Doxis4
     *                                @see com.ser.blueline.metaDataComponents.IDescriptorMapping
     * @return Map with mapping
     * @throws Exception if something goes wrong
     */
    public ConcurrentMap<String, DescriptorMapping> AnalyzeMappingTable(IDescriptorMappingTable descriptorMappingTable) throws Exception {
        templateName = descriptorMappingTable.getName();
        descriptorMappings = new ConcurrentHashMap<>(descriptorMappingTable.getCount());
        for (int item = 0; item < descriptorMappingTable.getCount(); item++) {
            IDescriptorMapping doxis4DescriptorMapping = descriptorMappingTable.getItem(item);
            DescriptorMapping descriptorMapping = new DescriptorMapping(doxis4Session);
            descriptorMapping.Analyze(doxis4DescriptorMapping);
            descriptorMappings.put(descriptorMapping.getDescriptorName(), descriptorMapping);
        }
        return descriptorMappings;
    }

    public byte[] getTempateFile() {
        return tempateFile;
    }

    public void setTempateFile(byte[] tempateFile) {
        this.tempateFile = tempateFile;
    }

    public ConcurrentMap<String, DescriptorMapping> getDescriptorMappings() {
        return descriptorMappings;
    }

    public void setDescriptorMappings(ConcurrentMap<String, DescriptorMapping> descriptorMappings) {
        this.descriptorMappings = descriptorMappings;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }
}
