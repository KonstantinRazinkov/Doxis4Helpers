package com.sersolutions.doxis4helpers.documents.word;

import com.ser.blueline.*;
import com.ser.blueline.metaDataComponents.DescriptorMappingAttributeType;
import com.ser.sedna.client.bluelineimpl.metadata.Descriptor;
import com.sersolutions.doxis4helpers.archiver.ZipProducer;
import com.sersolutions.doxis4helpers.commons.Descriptors;
import com.sersolutions.doxis4helpers.documents.doxis4templates.TemplateLoader;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.DescriptorMapping;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.Template;
import com.sersolutions.doxis4helpers.documents.word.datatypes.DocxControlsXml;
import com.sersolutions.doxis4helpers.documents.word.datatypes.DocxDocumentXml;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.management.AttributeNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Editor for controls inside word docx files
 */
public class DocxContentControlEditor {
    TemplateLoader templateLoader;
    Template template;
    ZipProducer wordFile;
    ISession doxis4Session;
    ConcurrentMap<String, String> fields;
    DocxDocumentXml documentXml;
    List<DocxControlsXml> itemXmls;
    ConcurrentMap<String, DocxControlsXml> itemXmlsMap;
    List<String> softExceptions = null;

    /**
     * Get word file as zip-archive
     * @return word file as zip-archive
     */
    public ZipProducer getWordFile() {
        return wordFile;
    }

    /**
     * Initialize editor
     * @param doxis4Session Doxis4 session object
     */
    public DocxContentControlEditor(ISession doxis4Session){
        this.doxis4Session = doxis4Session;
        templateLoader = new TemplateLoader(doxis4Session);
        template = new Template(doxis4Session);

        softExceptions = new ArrayList<>(100);
    }

    /**
     * Load word docx file from some path
     * @param path path to file
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer Load(String path) throws Exception{
        return Load(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Load word docx file from memory
     * @param file file in byte array
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer Load(byte[] file) throws Exception{
        wordFile = new ZipProducer(doxis4Session, file);
        itemXmls = new ArrayList<>(10);
        itemXmlsMap = new ConcurrentHashMap<>(10);
        int itemNumber = 0;
        boolean hasItemNumber = true;
        while (hasItemNumber) {
            itemNumber++;
            try {
                DocxControlsXml docxControlsXml = new DocxControlsXml(wordFile.GetEntry(String.format("customXml/item%d.xml", itemNumber)));
                itemXmls.add(docxControlsXml);
                Attr xmlns = docxControlsXml.getXmlDocument().getDocumentElement().getAttributeNode("xmlns");
                if (xmlns != null) {
                    String namespace = docxControlsXml.getXmlDocument().getDocumentElement().getAttributeNode("xmlns").getNodeValue();
                    itemXmlsMap.put(namespace, docxControlsXml);
                }
            } catch (Exception ex) {
                hasItemNumber =false;
            }

        }
        if (itemNumber == 0) {
            throw new Exception("Can't load docx controls items xmls");
        }

        documentXml = new DocxDocumentXml(wordFile.GetEntry(String.format("word/document.xml")));

        return wordFile;
    }

    /**
     * Load word docx file from Doxis4 (first representation, first document part)
     * @param docID Doxis4 Document ID
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(String docID) throws Exception{
        StringBuilder errBuilder = new StringBuilder();
        IDocument document = doxis4Session.getDocumentServer().getDocument4ID(docID, doxis4Session);
        if (document == null) throw new Exception(String.format("Can't file doxis4 document by ID '%s'", docID));
        for (IRepresentation representation : document.getRepresentationList())
        {
            try {
                wordFile = LoadFromDoxis4(representation);
                return wordFile;
            } catch (Exception ex) {
                errBuilder.append(ex.getMessage()).append("\n");
            }
        }
        throw new Exception(errBuilder.toString());
    }

    /**
     * Load word docx file from Doxis4 (selected representation, first document part)
     * @param docID Doxis4 Document ID
     * @param representation number of representation that have target word file
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(String docID, int representation) throws Exception{
        IDocument document = doxis4Session.getDocumentServer().getDocument4ID(docID, doxis4Session);
        if (document == null) throw new Exception(String.format("Can't file doxis4 document by ID '%s'", docID));
        return LoadFromDoxis4(document, representation);
    }

    /**
     * Load word docx file from Doxis4
     * @param document Doxis4 Document object
     * @param representationNo number of representation that have target word file
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(IDocument document, int representationNo) throws Exception{
        IRepresentation representation = document.getRepresentation(representationNo);
        return LoadFromDoxis4(representation);
    }

    /**
     * Load word docx file from Doxis4 representation
     * @param representation Doxis4 representation
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(IRepresentation representation) throws Exception{
        StringBuilder errBuilder = new StringBuilder();
        for (int part = 0; part < representation.getPartDocumentCount(); part++) {
            try {
                wordFile = LoadFromDoxis4(representation.getPartDocument(part));
                return wordFile;
            } catch (Exception ex) {
                errBuilder.append(ex.getMessage()).append("\n");
            }
        }
        throw new Exception(errBuilder.toString());
    }

    /**
     * Load word docx file from Doxis4
     * @param docID Doxis4 Document ID
     * @param representation Representation number
     * @param partdocument PartDocument number
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(String docID, int representation, int partdocument) throws Exception{
        IDocument document = doxis4Session.getDocumentServer().getDocument4ID(docID, doxis4Session);
        if (document == null) throw new Exception(String.format("Can't file doxis4 document by ID '%s'", docID));
        return LoadFromDoxis4(document.getPartDocument(representation, partdocument));
    }

    /**
     * Load word docx file from Doxis4 Document part
     * @param documentPart Doxis4 Document Part
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer LoadFromDoxis4(IDocumentPart documentPart) throws Exception{
        try {
            wordFile = Load(documentPart.getRawData());
            template.setTempateFile(documentPart.getRawData());
            template.setTemplateFileName(documentPart.getFilename());
        } catch (Exception ex) {
            throw  new Exception(String.format("Can't load docx file from file '%s' (%s)", documentPart.getFilename(), ex.getMessage()));
        }
        return wordFile;
    }

    /**
     * Save changes in word file in memory
     * @return word file as zip-archive
     * @throws Exception
     */
    public ZipProducer commit() throws Exception{
        int itemNumber = 0;

        for (DocxControlsXml controlsXml : itemXmls) {
            itemNumber++;
            wordFile.LoadZIPFromByteArray(controlsXml.saveChanges().toByteArray(), String.format("customXml/item%d.xml", itemNumber));
        }
        wordFile.LoadZIPFromByteArray(documentXml.saveChanges().toByteArray(), "word/document.xml");
        return wordFile;
    }

    /**
     * Load template from Doxis4
     * @param templateID ID of template from Doxis4
     * @param loadType Loading type of template
     * @return template from Doxis4 as zip-archive
     * @throws Exception
     */

    public ZipProducer LoadDoxis4Template(String templateID, TemplateLoader.LoadType loadType) throws Exception{
        Template tmpTemplate = templateLoader.LoadTemplate(templateID, loadType);
        if (loadType == TemplateLoader.LoadType.OnlyFile || loadType == TemplateLoader.LoadType.FullLoad) {
            wordFile = Load(tmpTemplate.getTempateFile());
            template.setTempateFile(tmpTemplate.getTempateFile());
        }
        if (loadType == TemplateLoader.LoadType.OnlyMapping || loadType == TemplateLoader.LoadType.FullLoad) {
            template.setDescriptorMappings(tmpTemplate.getDescriptorMappings());
            template.setTemplateName(tmpTemplate.getTemplateName());
            fields = new ConcurrentHashMap<>(template.getDescriptorMappings().size()+10);
            for (DescriptorMapping descriptorMapping : template.getDescriptorMappings().values()) {
                fields.put(descriptorMapping.getDescriptorName(), "");
            }
        }
        return wordFile;
    }

    /**
     * Set value to control inside docx file
     * @param descriptorName Doxis4 descriptor name
     * @param value value must be set
     * @return
     */
    public String setFieldValue(String descriptorName, String value){
        if (fields.containsKey(descriptorName)) {
            fields.replace(descriptorName, value);
        } else {
            fields.put(descriptorName, value);
        }
        return value;
    }

    /**
     * Get map with values of Doxis4 document
     * @param document Doxis4 document object
     * @return map with values from Document
     * @throws Exception
     */
    public ConcurrentMap<String, String> getFieldValuesFromDoxis4Document(IDocument document) throws Exception{
        for (String key : fields.keySet()) {
            String value = Descriptors.GetDescriptorValue(doxis4Session, document, key);
            try {
                IDescriptor descriptorDef = Descriptors.GetDescriptorDefinition(doxis4Session, key);
                if (descriptorDef.getDescriptorType() == Descriptor.TYPE_DATE) {
                    Date someDate = Descriptors.GetValueAsDate(value);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    value = sdf.format(someDate);

                } else if (descriptorDef.getDescriptorType() == Descriptor.TYPE_FLOAT) {
                    float someFloat = Float.parseFloat(value);
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en","US"));
                    nf.setMaximumFractionDigits(2);
                    DecimalFormat df = (DecimalFormat)nf;
                    value = df.format(someFloat);
                }
                setFieldValue(key, value);
            } catch (Exception ex) {
                setFieldValue(key, value);

            }

        }
        return fields;
    }

    /**
     * Set map with values to Doxis4 document
     * @param document Doxis4 document object
     * @return map with values that must be stored to Document
     * @throws Exception
     */
    public ConcurrentMap<String, String> setFieldValuesToDoxis4Document(IDocument document) throws Exception{
        for (String key : fields.keySet()) {
            Descriptors.SetDescriptorToInformationObject(doxis4Session, document, key, getFieldValue(key));
        }
        return fields;
    }

    /**
     * Get map with values from controls of docx word file
     * @return map with values from word file
     * @throws Exception
     */
    public ConcurrentMap<String, String> getFieldValuesFromDocx() throws Exception{
        for (DescriptorMapping descriptorMapping : template.getDescriptorMappings().values()) {
            DocxControlsXml controlsXml = itemXmlsMap.get(descriptorMapping.getMappings().get(DescriptorMappingAttributeType.OFFICE_OPENXML).getNameSpace());
            Node xmlNode = controlsXml.getNodeByFieldCascade(descriptorMapping.getMappings().get(DescriptorMappingAttributeType.OFFICE_OPENXML));
            setFieldValue(descriptorMapping.getDescriptorName(), (xmlNode.getChildNodes().item(0)!= null)? xmlNode.getChildNodes().item(0).getNodeValue() : "");
        }
        return fields;
    }

    /**
     * Set map with values from controls of docx word file
     * @return map with values that must be set to word file
     * @throws Exception
     */
    public ConcurrentMap<String, String> setFieldValuesToDocx() throws Exception{
        for (DescriptorMapping descriptorMapping : template.getDescriptorMappings().values()) {
            DocxControlsXml controlsXml = itemXmlsMap.get(descriptorMapping.getMappings().get(DescriptorMappingAttributeType.OFFICE_OPENXML).getNameSpace());
            try {
                controlsXml.SetValue(descriptorMapping.getMappings().get(DescriptorMappingAttributeType.OFFICE_OPENXML), getFieldValue(descriptorMapping.getDescriptorName()));
                documentXml.SetValueMultiple(descriptorMapping.getMappings().get(DescriptorMappingAttributeType.OFFICE_OPENXML), getFieldValue(descriptorMapping.getDescriptorName()));
            } catch (AttributeNotFoundException anfe) {
                softExceptions.add(anfe.getMessage());
            }
        }
        return fields;
    }

    public String getFieldValue(String descriptorName)
    {
        return fields.getOrDefault(descriptorName, null);
    }

    public List<String> getSoftExceptions() {
        return softExceptions;
    }

    public boolean hasSoftExceptions()
    {
        return (softExceptions.size() > 0);
    }
}
