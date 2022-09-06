package com.sersolutions.doxis4helpers.archiver;

import com.ser.blueline.*;
import com.ser.blueline.signature.ISignature;
import com.ser.foldermanager.*;
import com.sersolutions.doxis4helpers.commons.Descriptors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Class for producing ZIP-files
 */
public class ZipProducer {

    ConcurrentMap<String, com.sersolutions.doxis4helpers.archiver.datatypes.ZipEntry> entries;

    ISession session;

    /**
     * Format that must be used for saving ZIP files
     */
    public enum FinishType {
        FILE, BASE64
    }

    /**
     * Init new ZIP producer
     * @param session Doxis4 Session Object
     *                @see com.ser.blueline.ISession
     */
    public ZipProducer(ISession session) {
        this.session = session;
        entries = new ConcurrentHashMap<>(1000);
    }

    /***
     * Init new ZIP producer and loads selected ZIP file for changing
     * @param session Doxis4 Session Object
     *                @see com.ser.blueline.ISession
     * @param file ZIP file to load
     */
    public ZipProducer(ISession session, byte[] file) throws Exception {
        this(session);

        ByteArrayInputStream bais = new ByteArrayInputStream(file);

        ZipInputStream zipInputStream = new ZipInputStream(bais);

        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] byteBuff = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
                out.write(byteBuff, 0, bytesRead);
            }

            out.close();
            entries.put(zipEntry.getName(), new com.sersolutions.doxis4helpers.archiver.datatypes.ZipEntry(out, zipEntry.getName()));
            zipInputStream.closeEntry();
        }
    }

    /**
     * Get entry from Zip-File
     * @param path Path of file inside Zip-File
     * @return Byte array of file from zip-file
     * @throws Exception
     */
    public byte[] GetEntry(String path) throws Exception {
        if (!entries.containsKey(path)) {
            throw new FileNotFoundException(String.format("Can't find anything at ZIP file by path '%s'", path));
        }
        return entries.get(path).getContent();
    }

    /**
     * Create ZIP-file
     * @param type Format of creation of ZIP
     *             @use FinishType
     * @return byte array of result ZIP file
     * @throws Exception if something goes wrong
     */
    public byte[] FinishZip(FinishType type) throws Exception {
        ByteArrayOutputStream baos;
        ZipOutputStream zipOutputStream;

        baos = new ByteArrayOutputStream();
        zipOutputStream = new ZipOutputStream(baos);

        for (com.sersolutions.doxis4helpers.archiver.datatypes.ZipEntry entry : entries.values()) {
            ZipEntry zipEntry = new ZipEntry(entry.getPath());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(entry.getContent());
            zipOutputStream.flush();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        byte[] outZip = baos.toByteArray();
        if (type == FinishType.FILE) return outZip;

        if (type == FinishType.BASE64) {
            byte[] outBase64 = Base64.getEncoder().encode(outZip);
            return outBase64;
        }
        return null;
    }

    /**
     * Load some file into ZIP
     * @param content byte array of file that must be added to ZIP
     * @param path path in ZIP file
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromByteArray(byte[] content, String path) throws Exception {
        if (entries.containsKey(path)) entries.get(path).setContent(content);
        else entries.put(path, new com.sersolutions.doxis4helpers.archiver.datatypes.ZipEntry(content, path));

        /*ZipEntry entry = new ZipEntry(path);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(content);
        zipOutputStream.flush();
        zipOutputStream.closeEntry();*/
    }

    /**
     * Load Doxis4 Documents into ZIP by Documents IDs
     * @param documentIDs Doxis4 Documents IDs
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromIDs(String[] documentIDs) throws Exception {
        if (documentIDs == null) return;
        if (documentIDs.length == 0) return;

        for (String documentID : documentIDs) {
            IInformationObject informationObject = session.getDocumentServer().getInformationObjectByID(documentID, session);
            if (informationObject instanceof IFolder) {
                IFolder folder = (IFolder) informationObject;
                LoadZIPFromFolder(folder, null, "");
            }
            if (informationObject instanceof IDocument) {
                IDocument document = (IDocument) informationObject;
                LoadZIPFromDocument(document, "");
            }
        }
    }

    public enum NameType {
        ClearFromDoxis4,
        NumberedFromDoxis4,
        ClearPath,
        NumberedPath,
        CommonFileName
    }

    /**
     * Load Doxis4 Document into ZIP
     * @param document Doxis4 Document object
     *                 @see com.ser.blueline.IDocument
     * @param path path in ZIP
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromDocument(IDocument document, String path) throws Exception {
        LoadZIPFromDocument(document, path, NameType.ClearFromDoxis4);
    }

    /**
     * Load Doxis4 Document into ZIP
     * @param document Doxis4 Document object
     *                 @see com.ser.blueline.IDocument
     * @param path path in ZIP
     * @param nameType How to name file in ZIP
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromDocument(IDocument document, String path, NameType nameType) throws Exception {
        LoadZIPFromDocument(document, path, nameType, false);
    }
  /**
     * Load Doxis4 Document into ZIP
     * @param document Doxis4 Document object
     *                 @see com.ser.blueline.IDocument
     * @param path path in ZIP
     * @param nameType How to name file in ZIP
     * @param extractSignatures Extract signatures from document
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromDocument(IDocument document, String path, NameType nameType, boolean extractSignatures) throws Exception {
        LoadZIPFromDocument(document, path, nameType, null, extractSignatures);
    }
    /**
     * Load Doxis4 Document into ZIP
     * @param document Doxis4 Document object
     *                 @see com.ser.blueline.IDocument
     * @param path path in ZIP
     * @param nameType How to name file in ZIP
     * @param extractSignatures Extract signatures from document
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromDocument(IDocument document, String path, NameType nameType, String commonFileName, boolean extractSignatures) throws Exception {
        IRepresentation documentRepresentation;
        IDocumentPart documentPart;
        String fileName ="";
        byte[] content;
        int lastDot = 0;
        int lastSplash = 0;

        if (path ==null) path= "";

        for (int representation = 0; representation < document.getRepresentationCount(); representation++) {
            documentRepresentation = document.getRepresentation(representation);
            for (int part = 0; part < documentRepresentation.getPartDocumentCount(); part++) {
                documentPart =  documentRepresentation.getPartDocument(part);
                switch (nameType) {
                    case ClearFromDoxis4:
                    case NumberedFromDoxis4:
                        fileName = documentPart.getFilename();
                        break;
                    case ClearPath:
                    case NumberedPath:
                        lastDot = documentPart.getFilename().lastIndexOf(".");
                        if (lastDot > 0) {
                            fileName = documentPart.getFilename().substring(lastDot, documentPart.getFilename().length());
                        } else {
                            fileName = "";
                        }
                        break;
                    case CommonFileName:
                        lastDot = documentPart.getFilename().lastIndexOf(".");
                        if (lastDot > 0) {
                            fileName = documentPart.getFilename().substring(lastDot, documentPart.getFilename().length());
                        }

                        String[] descriptors = commonFileName.split(";");
                        if (descriptors == null || descriptors.length == 0) {
                            fileName = String.format("%s.%s", commonFileName, fileName);
                        } else {
                            StringBuilder fileNameBuilder = new StringBuilder();
                            Boolean isFirst=true;
                            for (String descriptor : descriptors) {
                                if (isFirst) isFirst = false;
                                else fileNameBuilder.append(" - ");
                                fileNameBuilder.append(Descriptors.GetDescriptorValue(session, document, descriptor));
                            }
                            fileName = String.format("%s.%s", fileNameBuilder.toString(), fileName);
                            fileName = fileName.replace("/", "").replace("\\", "");
                        }
                        break;
                }
                if (nameType == NameType.NumberedFromDoxis4 || nameType == NameType.NumberedPath) fileName = String.format("%d - %s", part+1, fileName);
                content = documentPart.getRawData();
                LoadZIPFromByteArray(content, StringUtils.isNotBlank(FilenameUtils.getExtension(path)) ? path : path + fileName);
            }
            if (extractSignatures) {
                List<ISignature> signaturesToDownload = new ArrayList<>();
                for (int signatureNo = 0; signatureNo < documentRepresentation.getSignatureCount(); signatureNo++) {
                    ISignature signature = documentRepresentation.getSignature(signatureNo);
                    boolean needToAdd = true;
                    for (int i = 0; i < signaturesToDownload.size(); i++) {
                        ISignature oldSignature = signaturesToDownload.get(i);
                        if (signature.getSubjectName().equals(oldSignature.getSubjectName())) {
                            needToAdd = false;
                        }
                    }
                    if (needToAdd) signaturesToDownload.add(signature);
                }
                for (int signNo = 0; signNo < signaturesToDownload.size(); signNo++) {
                    ISignature signature = signaturesToDownload.get(signNo);
                    // Next lines (up to LoadZIPFromByteArray) check if we need to delete previous
                    // added duplicated signature (or maybe skip this one).
                    // Calculate current signature hash
                    String signatureGOSTHash = HashProducer.CalculateGOSTR34112012(signature.getRawData());

                    // Create list of entries (previous added files) to remove from archive if we have duplicated signature.
                    List<String> entriesToReplace = new ArrayList<>(3);

                    // Check every previous added file
                    for (String zipEntryKey : entries.keySet()) {
                        if (!zipEntryKey.endsWith(".sgn")) continue; // if it's not signature - we skip it
                        if (zipEntryKey.endsWith("-0.sgn"))
                            continue; // if this signature have #0 (first one) - we must not delete it!

                        // If this signature have different size - it must not be the same signature
                        if (signature.getRawData().length != entries.get(zipEntryKey).getContent().length) continue;

                        // Get that signature hash
                        String zipEntryGOSTHash = HashProducer.CalculateGOSTR34112012(entries.get(zipEntryKey).getContent());

                        // If we have same size and also hash - it means that we have same file
                        if (zipEntryGOSTHash.equals(signatureGOSTHash)) entriesToReplace.add(zipEntryKey);
                    }

                    // If we have found that we've added same signature before, but THIS signature is not #0 (first one)
                    // So we will skip this (not first) signature and will save an old one
                    if (entriesToReplace.size() > 0 && signNo > 0) continue;

                    // Remove all previous signatures, that came to replace list
                    for (String entryToReplace : entriesToReplace) {
                        entries.remove(entryToReplace);
                    }
                    LoadZIPFromByteArray(signature.getRawData(), String.format("%s%s-%d.sgn", path, fileName, signNo));
                }
            }
        }
    }

    public enum FolderSaverMode {
        AllFolders,
        FirstLeverlFolders,
        AllInOne
    }

    int lastFolderIndex=-1;

    /**
     * Load Doxis4 Folder (REC) into ZIP file
     * @param parent Parent Doxis4 Folder (REC)
     *               @see com.ser.foldermanager.IFolder
     * @param baseNode Doxis4 Folder node
     *               @see com.ser.foldermanager.INode
     * @param path path in ZIP
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromFolder(IFolder parent, INode baseNode, String path) throws  Exception {
        LoadZIPFromFolder(parent, baseNode, path, FolderSaverMode.AllFolders, NameType.ClearFromDoxis4, null, null);
    }

    /**
     * Load Doxis4 Folder (REC) into ZIP file
     * @param parent Parent Doxis4 Folder (REC)
     *               @see com.ser.foldermanager.IFolder
     * @param baseNode Doxis4 Folder node
     *               @see com.ser.foldermanager.INode
     * @param path path in
     * @param folderSaverMode how to save files by folder structure
     * @param nameType how to name files in document
     * @param descriptors list of descriptors to name documents
     * @param splitter splitter between descriptors
     * @throws Exception if something goes wrong
     */

    public void LoadZIPFromFolder(IFolder parent, INode baseNode, String path, FolderSaverMode folderSaverMode, NameType nameType, List<String> descriptors, String splitter) throws  Exception {
        LoadZIPFromFolder(parent, baseNode, path, FolderSaverMode.AllFolders, NameType.ClearFromDoxis4, null, null, null);
    }
    /**
     * Load Doxis4 Folder (REC) into ZIP file
     * @param parent Parent Doxis4 Folder (REC)
     *               @see com.ser.foldermanager.IFolder
     * @param baseNode Doxis4 Folder node
     *               @see com.ser.foldermanager.INode
     * @param path path in
     * @param folderSaverMode how to save files by folder structure
     * @param nameType how to name files in document
     * @param descriptors list of descriptors to name documents
     * @param splitter splitter between descriptors
     * @throws Exception if something goes wrong
     */
    public void LoadZIPFromFolder(IFolder parent, INode baseNode, String path, FolderSaverMode folderSaverMode, NameType nameType, String commonFileName, List<String> descriptors, String splitter) throws  Exception {
        IElements elems = null;
        IElement elem = null;
        int cnt = 0;
        int index = 0;
        INodes nodes = null;
        String elemLink = "";
        IDocument doc;
        String fileName = "";
        IRepresentation docRepresentation = null;
        IDocumentPart docPart = null;
        byte[] content = null;

        if (baseNode == null) {
            lastFolderIndex = -1;
            nodes = parent.getNodes2();
            cnt = nodes.getCount();
            if (cnt == 0) return;
            for (index = 0; index < cnt; index++) {
                baseNode = nodes.getItem(index);
                if (baseNode == null) continue;
                switch (folderSaverMode) {
                    case AllFolders:
                    case FirstLeverlFolders:
                        LoadZIPFromFolder(parent, baseNode, path + baseNode.getName() + "/", folderSaverMode, nameType, descriptors, splitter);
                        break;
                    case AllInOne:
                        LoadZIPFromFolder(parent, baseNode, path, folderSaverMode, nameType, descriptors, splitter);
                        break;
                }
            }
        } else {

            elems = baseNode.getElements();
            cnt = elems.getCount2();
            if (splitter == null) splitter = "#";

            for (index = 0; index < cnt; index++) {
                lastFolderIndex++;

                elem = elems.getItem2(index);
                elemLink = elem.getLink();

                doc = session.getDocumentServer().getDocument4ID(elemLink, session);

                StringBuilder newFilename = new StringBuilder();
                newFilename.append(path);
                if (nameType == NameType.NumberedFromDoxis4 || nameType == NameType.NumberedPath) {
                    if (folderSaverMode == FolderSaverMode.AllInOne) {
                        newFilename.append(lastFolderIndex+1).append(splitter);
                    } else {
                        newFilename.append(index+1).append(splitter);
                    }
                }
                if (nameType == NameType.ClearPath || nameType == NameType.NumberedPath) {
                    if (descriptors != null) {
                        for (String descriptor : descriptors) {
                            newFilename.append(Descriptors.GetDescriptorValue(session, doc, descriptor)).append(splitter);
                        }
                    }
                }

                if (nameType == NameType.NumberedFromDoxis4 || nameType == NameType.ClearFromDoxis4) LoadZIPFromDocument(doc, newFilename.toString(), NameType.ClearFromDoxis4);
                else LoadZIPFromDocument(doc, newFilename.toString(), nameType, commonFileName, false);
                /*
                for (int representation = 0; representation < doc.getRepresentationCount(); representation++)
                {
                    docRepresentation = doc.getRepresentation(representation);
                    for (int part = 0; part < docRepresentation.getPartDocumentCount(); part++)
                    {
                        docPart =  docRepresentation.getPartDocument(part);
                        fileName = docPart.getFilename();
                        content = docPart.getRawData();
                        LoadZIPFromByteArray(content, path + fileName);
                    }
                }*/
            }
        }
    }
}
