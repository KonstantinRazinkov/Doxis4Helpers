package com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes;

import com.ser.blueline.IDescriptor;
import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.DescriptorMappingAttributeType;
import com.ser.blueline.metaDataComponents.IDescriptorMapping;
import com.ser.blueline.metaDataComponents.IDescriptorMappingAttribute;
import com.sersolutions.doxis4helpers.commons.Descriptors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Special data type to store links between descriptors and office document fields
 */
public class DescriptorMapping {
    ISession doxis4Session;
    String descriptorID;
    String descriptorName;

    ConcurrentMap<DescriptorMappingAttributeType, TemplateFieldCascade> mappings;

    public DescriptorMapping(ISession doxis4Session)
    {
        this.doxis4Session = doxis4Session;
    }

    /**
     * Parse Doxis4 template mapping for saving actual mapping to memory
     * @param doxis4DescriptorMapping IDescriptorMapping from Doxis4
     *                                @see com.ser.blueline.metaDataComponents.IDescriptorMapping
     * @throws Exception if something goes wrong
     */
    public void Analyze(IDescriptorMapping doxis4DescriptorMapping) throws Exception {
        descriptorID = doxis4DescriptorMapping.getDescriptorId();
        IDescriptor descriptorDef = Descriptors.GetDescriptorDefinition(doxis4Session, descriptorID);
        descriptorName = descriptorDef.getName();

        mappings = new ConcurrentHashMap<>(doxis4DescriptorMapping.getAttributes().size());
        for (IDescriptorMappingAttribute descriptorMappingAttribute : doxis4DescriptorMapping.getAttributes()) {
            TemplateFieldCascade templateFieldCascade = new TemplateFieldCascade();
            templateFieldCascade.parseNameSpace(descriptorMappingAttribute.getXPathSelectionNamespaces());
            templateFieldCascade.parseFields(descriptorMappingAttribute.getAttribute());
            mappings.put(descriptorMappingAttribute.getType(), templateFieldCascade);
        }
    }

    public ConcurrentMap<DescriptorMappingAttributeType, TemplateFieldCascade> getMappings() {
        return mappings;
    }

    public String getDescriptorID() {
        return descriptorID;
    }

    public String getDescriptorName() {
        return descriptorName;
    }
}
