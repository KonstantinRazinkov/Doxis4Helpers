package com.sersolutions.doxis4helpers.documents.word.datatypes;

import org.w3c.dom.Node;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.TemplateField;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.TemplateFieldCascade;
import org.w3c.dom.Document;

import javax.management.AttributeNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Special data type that stores information about controls from DOCX-files
 */
public class DocxControlsXml {
    public String nameSpace;
    public String rootName;
    org.w3c.dom.Document xmlDocument;

    /**
     * Analyse controls from xml file of values inside docx word file
     * @param xmlFile byte array with xml
     * @throws Exception
     */
    public DocxControlsXml(byte[] xmlFile) throws Exception {
        this(new ByteArrayInputStream(xmlFile));
    }

    /**
     * Analyse controls from xml file of values inside docx word file
     * @param xmlFile ByteArrayInputStream of xml file
     * @throws Exception
     */
    public DocxControlsXml(ByteArrayInputStream xmlFile) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        xmlDocument = documentBuilder.parse(xmlFile);
    }

    /**
     * Save changes of the file
     * @return ByteArrayOutputStream of changed xml file
     * @throws Exception
     */
    public ByteArrayOutputStream saveChanges() throws Exception {
        javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(xmlDocument);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        transformer.transform(source, result);
        return baos;
    }

    /**
     * Get node of the field from xml structure
     * @param cascade cascade field name information
     * @return node with field
     * @throws Exception
     */
    public Node getNodeByFieldCascade(TemplateFieldCascade cascade) throws Exception {
        Node actualNode = null;
        for(TemplateField field : cascade.getFields()) {
            if (actualNode == null) {
                actualNode = xmlDocument.getElementsByTagName(field.getFieldName()).item(field.getFieldPosition());
                continue;
            }
            for (int subNodeNo = 0; subNodeNo < actualNode.getChildNodes().getLength(); subNodeNo++) {
                Node subNode = actualNode.getChildNodes().item(subNodeNo);
                if (subNode.getNodeName().equals(field.getFieldName())) {
                    actualNode = subNode;
                    continue;
                }
            }
        }
        return actualNode;
    }

    /**
     * Change the value of control
     * @param fieldCascade cascade field name information
     * @param value value
     * @throws AttributeNotFoundException
     * @throws Exception
     */
    public void SetValue(TemplateFieldCascade fieldCascade, String value) throws AttributeNotFoundException, Exception {
        Node xmlNode = this.getNodeByFieldCascade(fieldCascade);
        if (xmlNode.getChildNodes().item(0) == null) {
            xmlNode.setTextContent(value);
        } else {
            xmlNode.getChildNodes().item(0).setNodeValue(value);
        }
    }


    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    public void setXmlDocument(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
    }
}
