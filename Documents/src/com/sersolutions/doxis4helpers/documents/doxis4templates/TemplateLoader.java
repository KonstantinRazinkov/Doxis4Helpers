package com.sersolutions.doxis4helpers.documents.doxis4templates;

import com.ser.blueline.IDocument;
import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.IDocumentTemplate;
import com.sersolutions.doxis4helpers.commons.Tools;
import com.sersolutions.doxis4helpers.documents.doxis4templates.datatypes.Template;

import java.io.FileNotFoundException;

/**
 * Loader of office document templates
 */
public class TemplateLoader {
    ISession doxis4Session;

    public enum  LoadType {
        FullLoad,
        OnlyFile,
        OnlyMapping
    }

    public TemplateLoader (ISession doxis4Session)
    {
        this.doxis4Session = doxis4Session;
    }

    /**
     * Load template from Doxis4 to memory
     * @param templateID ID of document template from Doxis4
     * @param loadType load type
     * @return Loaded template
     * @throws Exception if something goes wrong
     */
    public Template LoadTemplate(String templateID, LoadType loadType) throws Exception {
        IDocumentTemplate doxis4Template = doxis4Session.getDocumentServer().getDocumentTemplate(templateID, doxis4Session);
        Template template = new Template(doxis4Session);
        if (loadType == LoadType.FullLoad || loadType == LoadType.OnlyFile) {
            switch (doxis4Template.getReferenceType()) {
                case IDocumentTemplate.RT_DOCUMENT:
                    IDocument docTemplate = doxis4Session.getDocumentServer().getDocument4ID(doxis4Template.getReference(), doxis4Session);
                    if (docTemplate == null) throw new Exception(String.format("Can't found template document in Doxis4 by ID: templateID= %s, docID=%s", templateID, doxis4Template.getReference()));
                    template.setTempateFile(docTemplate.getPartDocument(0,0).getRawData());
                    break;
                default:
                    try {
                        template.setTempateFile(Tools.GetFileFromAnywhere(doxis4Template.getReference()));
                    } catch (Exception ex) {
                        throw new Exception(String.format("Can't found template document in Doxis4 by path: templateID= %s, docPath=%s", templateID, doxis4Template.getReference()), ex);
                    }
            }
        }

        if (loadType == LoadType.FullLoad || loadType == LoadType.OnlyMapping) {
            template.AnalyzeMappingTable(doxis4Template.getDescriptorMappingTable());
        }

        return template;
    }
}
