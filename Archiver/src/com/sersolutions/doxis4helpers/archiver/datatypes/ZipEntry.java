package com.sersolutions.doxis4helpers.archiver.datatypes;

import java.io.ByteArrayOutputStream;

/**
 * Class of one ZipEntry (file inside zip). Have own path, content and output stream for loading
 */
public class ZipEntry {
    private String path;
    private byte[] content;
    private ByteArrayOutputStream baos;

    /**
     * To initialize new ZipEntry (to archive file) you need to provide content and path inside zip-file
     * @param content Byte array of file that must be archived
     * @param path Path inside zip-file
     */
    public ZipEntry(byte[] content, String path) {
        setContent(content);
        setPath(path);
    }

    /**
     * To initialize new ZipEntry (to archive file) you need to provide content and path inside zip-file
     * @param outputStream Output stream of file that must be archived
     * @param path Path inside zip-file
     */
    public ZipEntry(ByteArrayOutputStream outputStream, String path) {
        setBaos(outputStream, true);
        setPath(path);
    }

    /**
     * Get path of the file inside Zip-File
     * @return path of the file inside Zip-File
     */
    public String getPath() {
        return path;
    }

    /**
     * Set path of the file inside Zip-File
     * @param path path of the file inside Zip-File
     */
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * Get file as byte array
     * @return byte array of file
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Set file by byte array
     * @param content byte array of file
     */
    public void setContent(byte[] content) {
        this.content = content;
    }


    /**
     * Get output stream of file
     * @return ByteArrayOutputStream of file
     * @throws Exception
     */
    public ByteArrayOutputStream getBaos() throws Exception {
        if (content != null) {
            baos = new ByteArrayOutputStream();
            baos.write(content);
        }
        return baos;
    }

    /**
     * Update file by ByteArrayOutputStream
     * @param baos ByteArrayOutputStream of file
     */
    public void setBaos(ByteArrayOutputStream baos)
    {
        setBaos(baos, true);
    }

    /**
     * Update file by ByteArrayOutputStream with updating byte array of file
     * @param baos ByteArrayOutputStream of file
     * @param clearcontent Do you need update byte array or not
     */
    public void setBaos(ByteArrayOutputStream baos, boolean clearcontent) {
        if (clearcontent) setContent(baos.toByteArray());
        this.baos = baos;
    }
}
