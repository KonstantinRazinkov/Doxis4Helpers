package com.sersolutions.doxis4helpers.commons.types;

import com.ser.blueline.signature.ISignature;

/**
 * Class for holding content file from Doxis4
 */
public class ContentFile {

    private String fileName;
    private byte[] content;
    private String mimeType;
    private String representationType;
    private Signature[] signatures;

    /**
     * Init content file with his name and byte array
     * @param fileName filename of document
     * @param content byte array with contents of document
     * @throws Exception if there will be some problems with name of byte array
     */
    public ContentFile(String fileName, byte[] content) throws Exception {
        this.signatures = null;
        if (content == null) {
            throw new Exception("Content cannot be null!");
        }
        if (content.length == 0) {
            throw new Exception("Content cannot be 0 size");
        }
        if (fileName == null || "".equals(fileName)) {
            throw new Exception("File name cannot be null or empty!");
        }

        this.content = content;
        this.fileName = fileName;

        this.mimeType = "";
        this.representationType = "";
    }


    /**
     * Init content file with his name and byte array
     * @param fileName filename of document
     * @param content byte array with contents of document
     * @param mimeType mimeType of document
     * @param representationType representation type of document
     * @throws Exception if there will be some problem with document filename, content or signatures
     */
    public ContentFile(String fileName, byte[] content, String mimeType, String representationType) throws Exception {
        this(fileName, content);
        this.mimeType = mimeType;
        this.representationType = representationType;
    }

    /**
     * Init content file with his name and byte array
     * @param fileName filename of document
     * @param content byte array with contents of document
     * @param mimeType mimeType of document
     * @param representationType representation type of document
     * @param doxis4Signatures signatures of document
     * @throws Exception if there will be some problem with document filename, content or signatures
     */
    public ContentFile(String fileName, byte[] content, String mimeType, String representationType, ISignature[] doxis4Signatures) throws Exception {
        this(fileName, content, mimeType, representationType);
        if (doxis4Signatures != null) {
            signatures = new Signature[doxis4Signatures.length];
            for (int indexSignature = 0; indexSignature < doxis4Signatures.length; indexSignature++) {
                signatures[0] = new Signature(doxis4Signatures[indexSignature]);
            }
        }
    }

    public String GetFileName() {return fileName;}
    public byte[] GetContent() {return content;}
    public String GetMimeType() {return mimeType;}
    public String GetRepresentationType() {return representationType;}
    public Signature[] GetSignatures()
    {
        return signatures;
    }
}
