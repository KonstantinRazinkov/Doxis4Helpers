package com.sersolutions.doxis4helpers.documents.word.datatypes;

import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.TemplateField;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.TemplateFieldCascade;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.AttributeNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Special data type that stores DOCX document xml structure
 */
public class DocxDocumentXml {
    Document xmlDocument;
    ConcurrentMap<String, Node> sdtControls;
    ConcurrentMap<String, List<Node>> sdtControlsMultiple;
    ConcurrentMap<String, String> sdtControlsToXML;

    /**
     * Load xml from word docx document
     * @param xmlFile xml file as byte array
     * @throws Exception if something goes wrong
     */
    public DocxDocumentXml(byte[] xmlFile) throws Exception {
        this(new ByteArrayInputStream(xmlFile));
    }

    /**
     * Load xml from word docx document
     * @param xmlFile xml file as byte array input stream
     * @throws Exception if something goes wrong
     */
    public DocxDocumentXml(ByteArrayInputStream xmlFile) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        xmlDocument = documentBuilder.parse(xmlFile);
        xmlFile.close();

        sdtControls = CollectAllControlsByTag(xmlDocument);
        sdtControlsMultiple = CollectAllControlsByTagMultiple(xmlDocument);
        sdtControlsToXML = GetControlsToXMLMap(xmlDocument);
    }

    /**
     * Analyse xml of DOCX document and collect all links to controls
     * @param xmlDocument xml of word docx file
     * @return
     */
    ConcurrentMap<String, Node> CollectAllControlsByTag(Document xmlDocument) {
        ConcurrentMap<String, Node> result = null;
        NodeList tagList = xmlDocument.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tag");
        result = new ConcurrentHashMap<>(tagList.getLength());
        for (int tagNo = 0; tagNo < tagList.getLength(); tagNo++) {
            Node tag = tagList.item(tagNo);
            if (tag.getAttributes().getLength() <= 0) continue;
            if (tag.getAttributes().getNamedItem("w:val") == null) continue;
            String tagName = tag.getAttributes().getNamedItem("w:val").getNodeValue();
            result.put(tagName, tag.getParentNode().getParentNode());
        }

        return  result;
    }
    /**
     * Analyse xml of DOCX document and collect all links to controls
     * @param xmlDocument xml of word docx file
     * @return
     */
    ConcurrentMap<String, List<Node>> CollectAllControlsByTagMultiple(Document xmlDocument) {
        ConcurrentMap<String, List<Node>> result = null;
        NodeList tagList = xmlDocument.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tag");
        result = new ConcurrentHashMap<>(tagList.getLength());
        for (int tagNo = 0; tagNo < tagList.getLength(); tagNo++) {
            Node tag = tagList.item(tagNo);
            if (tag.getAttributes().getLength() <= 0) continue;
            if (tag.getAttributes().getNamedItem("w:val") == null) continue;
            String tagName = tag.getAttributes().getNamedItem("w:val").getNodeValue();
            List<Node> nodes=null;
            if (result.containsKey(tagName)) {
                nodes = result.get(tagName);
            } else {
                nodes = new ArrayList<>(10);
                result.put(tagName, nodes);
            }
            nodes.add(tag.getParentNode().getParentNode());
            //result.put(tagName, tag.getParentNode().getParentNode());
        }

        return  result;
    }


    /**
     * Analyse xml of DOCX document and collect a map of control name to xml attribute
     * @param xmlDocument xml of word docx file
     * @return
     */
    ConcurrentMap<String, String> GetControlsToXMLMap(Document xmlDocument) {
        ConcurrentMap<String, String> result = null;
        NodeList parentFieldsList = xmlDocument.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "sdtPr");
        result = new ConcurrentHashMap<>(parentFieldsList.getLength());
        for (int tagNo = 0; tagNo < parentFieldsList.getLength(); tagNo++) {
            Node parentField = parentFieldsList.item(tagNo);
            NodeList childNodeList = parentField.getChildNodes();
            if (childNodeList.getLength() <= 0) continue;
            Node alias = null;
            for (int i = 0; i < childNodeList.getLength(); i++){
                if (childNodeList.item(i).getLocalName().equals("alias")) alias = childNodeList.item(i);
            }
            if (alias == null) continue;
            if (alias.getAttributes().getLength() <= 0) continue;
            String aliasName = alias.getAttributes().getNamedItem("w:val").getNodeValue();
            Node dataBinding = null;
            for (int i = 0; i < childNodeList.getLength(); i++){
                if (childNodeList.item(i).getLocalName().equals("dataBinding")) dataBinding = childNodeList.item(i);
            }
            if (dataBinding == null) continue;
            if (dataBinding.getAttributes().getLength() <= 0) continue;
            String[] dataBindings = dataBinding.getAttributes().getNamedItem("w:xpath").getNodeValue().split("\\/(.*?):|\\[1\\]");
            String dataBindingName = dataBindings[dataBindings.length - 1];
            result.put(dataBindingName, aliasName);
        }
        return  result;
    }


    /**
     * Set value to control inside docx xml structure
     * @param fieldCascade cascade of field names
     * @param value value
     * @throws AttributeNotFoundException if something goes wrong
     */
    public void SetValue(TemplateFieldCascade fieldCascade, String value) throws AttributeNotFoundException {
        TemplateField lastField = fieldCascade.getFields().get(fieldCascade.getFields().size()-1);
        SetValue(lastField.getFieldName(), value);
    }
    /**
     * Set value to control inside docx xml structure
     * @param fieldCascade cascade of field names
     * @param value value
     * @throws AttributeNotFoundException if something goes wrong
     */
    public void SetValueMultiple(TemplateFieldCascade fieldCascade, String value) throws AttributeNotFoundException {
        TemplateField lastField = fieldCascade.getFields().get(fieldCascade.getFields().size()-1);
        SetValueMultiple(lastField.getFieldName(), value);
    }

    /**
     * Set value to control inside docx xml structure
     * @param fieldName field name of control
     * @param value value
     * @throws AttributeNotFoundException if something goes wrong
     */
    public void SetValue(String fieldName, String value) throws AttributeNotFoundException {
        Node tag = sdtControls.get(fieldName);
        if (tag == null) throw new AttributeNotFoundException(String.format("Can't find field '%s'", fieldName));
        int childNodeNo = 0;
        Node contentNode = null;
        Node pNode = null;
        Node rNode = null;
        Node tNode = null;

        for (childNodeNo = 0; childNodeNo < tag.getChildNodes().getLength(); childNodeNo++) {
            if ("sdtContent".equalsIgnoreCase(tag.getChildNodes().item(childNodeNo).getLocalName())) {
                contentNode = tag.getChildNodes().item(childNodeNo);
                break;
            }
        }
        if (contentNode == null) throw new AttributeNotFoundException(String.format("Can't find content node for field '%s'", fieldName));

        for (childNodeNo = 0; childNodeNo < contentNode.getChildNodes().getLength(); childNodeNo++) {
            if ("p".equalsIgnoreCase(contentNode.getChildNodes().item(childNodeNo).getLocalName())) {
                pNode = contentNode.getChildNodes().item(childNodeNo);
                break;
            }
        }
        if (pNode == null) pNode = contentNode;

        for (childNodeNo = 0; childNodeNo < pNode.getChildNodes().getLength(); childNodeNo++) {
            if ("r".equalsIgnoreCase(pNode.getChildNodes().item(childNodeNo).getLocalName())) {
                rNode = pNode.getChildNodes().item(childNodeNo);
                break;
            }
        }

        if (rNode == null) throw new AttributeNotFoundException(String.format("Can't find content R node for field '%s'", fieldName));

        for (childNodeNo = 0; childNodeNo < rNode.getChildNodes().getLength(); childNodeNo++) {
            if ("t".equalsIgnoreCase(rNode.getChildNodes().item(childNodeNo).getLocalName())) {
                tNode = rNode.getChildNodes().item(childNodeNo);
                break;
            }
        }

        if (tNode == null) throw new AttributeNotFoundException(String.format("Can't find content T node for field '%s'", fieldName));

        if (tNode.getChildNodes().item(0) == null) {
            tNode.setTextContent(value);
        } else {
            tNode.getChildNodes().item(0).setNodeValue(value);
        }
    }
    /**
     * Set value to control inside docx xml structure
     * @param fieldName field name of control
     * @param value value
     * @throws AttributeNotFoundException if something goes wrong
     */
    public void SetValueMultiple(String fieldName, String value) throws AttributeNotFoundException {
        boolean haveErrors=false;
        StringBuilder errors = new StringBuilder();
        List<Node> tags = sdtControlsMultiple.get(fieldName);
        if (tags == null) {
            String fieldNameControl = sdtControlsToXML.get(fieldName);
            tags = sdtControlsMultiple.get(fieldNameControl == null ? "" : fieldNameControl);
            if (tags == null) throw new AttributeNotFoundException(String.format("Can't find field '%s'", fieldName));
        }

        for (Node tag : tags) {
            try {
                int childNodeNo = 0;
                Node contentNode = null;
                Node pNode = null;
                Node rNode = null;
                Node tNode = null;

                for (childNodeNo = 0; childNodeNo < tag.getChildNodes().getLength(); childNodeNo++) {
                    if ("sdtContent".equalsIgnoreCase(tag.getChildNodes().item(childNodeNo).getLocalName())) {
                        contentNode = tag.getChildNodes().item(childNodeNo);
                        break;
                    }
                }
                if (contentNode == null) throw new AttributeNotFoundException(String.format("Can't find content node for field '%s'", fieldName));

                for (childNodeNo = 0; childNodeNo < contentNode.getChildNodes().getLength(); childNodeNo++) {
                    if ("p".equalsIgnoreCase(contentNode.getChildNodes().item(childNodeNo).getLocalName())) {
                        pNode = contentNode.getChildNodes().item(childNodeNo);
                        break;
                    }
                }
                if (pNode == null) pNode = contentNode;

                for (childNodeNo = 0; childNodeNo < pNode.getChildNodes().getLength(); childNodeNo++) {
                    if ("r".equalsIgnoreCase(pNode.getChildNodes().item(childNodeNo).getLocalName())) {
                        rNode = pNode.getChildNodes().item(childNodeNo);
                        break;
                    }
                }

                if (rNode == null) throw new AttributeNotFoundException(String.format("Can't find content R node for field '%s'", fieldName));

                for (childNodeNo = 0; childNodeNo < rNode.getChildNodes().getLength(); childNodeNo++) {
                    if ("t".equalsIgnoreCase(rNode.getChildNodes().item(childNodeNo).getLocalName())) {
                        tNode = rNode.getChildNodes().item(childNodeNo);
                        break;
                    }
                }

                if (tNode == null) throw new AttributeNotFoundException(String.format("Can't find content T node for field '%s'", fieldName));

                if (tNode.getChildNodes().item(0) == null) {
                    tNode.setTextContent(value);
                } else {
                    tNode.getChildNodes().item(0).setNodeValue(value);
                }
            }
            catch (AttributeNotFoundException ex) {
                if (haveErrors) errors.append("\n");
                else haveErrors = true;
                errors.append(ex.getMessage());
            }
        }
        if (haveErrors) throw  new AttributeNotFoundException(errors.toString());
    }

    /**
     * Save changes back to original xml
     * @return byte array output stream with xml that must be saved inside word file
     * @throws Exception if something goes wrong
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

    public Document getXmlDocument() {
        return xmlDocument;
    }

    public void setXmlDocument(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

}
