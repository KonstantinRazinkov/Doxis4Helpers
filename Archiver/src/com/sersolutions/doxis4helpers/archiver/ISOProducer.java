package com.sersolutions.doxis4helpers.archiver;

import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.StreamHandler;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for producing ISO files
 */
public class ISOProducer {
    ISO9660RootDirectory isoRoot;
    private String volumeID = "DOXIS4-00000000";
    private String publisher = "DOXIS4";
    private String dataAuthor = "DOXIS4-AUTOMAT";

    /**
     * Init ISO producer
     * @param volumeID ID of Volume
     * @param publisher Name of publisher
     * @param dataAuthor Name of author
     */
    public ISOProducer(String volumeID, String publisher, String dataAuthor) {
        if (volumeID != null && !"".equals(volumeID)) {
            this.volumeID = volumeID;
        }
        if (publisher != null && !"".equals(publisher)) {
            this.publisher = volumeID;
        }
        if (dataAuthor != null && !"".equals(dataAuthor)) {
            this.dataAuthor = dataAuthor;
        }
        isoRoot = new ISO9660RootDirectory();
    }

    /**
     * Create ISO file at address
     * @param path Address for creating ISO file
     * @throws Exception if something goes wrong
     */
    public void Compile(String path) throws Exception {
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(true);
        iso9660Config.setInterchangeLevel(1);
        iso9660Config.restrictDirDepthTo8(false);
        iso9660Config.setPublisher(publisher);
        iso9660Config.setVolumeID(volumeID);
        iso9660Config.setDataPreparer(dataAuthor);
        //iso9660Config.setCopyrightFile(new File("Copyright.txt"));
        iso9660Config.forceDotDelimiter(false);

        JolietConfig jolietConfig = null;


        RockRidgeConfig rrConfig = null;
        // Rock Ridge support
        rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(false);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);
        // Joliet support
        jolietConfig = new JolietConfig();
        jolietConfig.setPublisher(publisher);
        jolietConfig.setVolumeID(volumeID);
        jolietConfig.setDataPreparer(dataAuthor);
        // jolietConfig.setCopyrightFile(new File("Copyright.txt"));
        jolietConfig.forceDotDelimiter(true);
        jolietConfig.setMaxCharsInFilename(200);

        StreamHandler streamHandler = new ISOImageFileHandler(new File(path));
        CreateISO iso = new CreateISO(streamHandler, isoRoot);
        iso.process(iso9660Config, rrConfig, jolietConfig, null);
    }

    /**
     * Add file to ISO
     * @param file File object
     *             @see java.io.File
     * @param path Path at ISO file
     * @throws Exception if something goes wrong
     */
    public void AddFile(File file, String path) throws Exception {
        ISO9660Directory directory = null;
        if (path != null && !"".equals(path)) {
            directory = CreateDirectory(isoRoot, path);
        }
        else {
            directory = isoRoot;
        }
        directory.addFile(file);
    }

    /**
     * Just create directory in ISO
     * @param sourceNode base directory in ISO
     *                   @see com.github.stephenc.javaisotools.iso9660.ISO9660Directory
     * @param path path for new directory
     * @return new ISO directory
     * @see com.github.stephenc.javaisotools.iso9660.ISO9660Directory
     */
    public static ISO9660Directory CreateDirectory(ISO9660Directory sourceNode, String path) {
        path = path.replace("\\", "/");
        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length()-1);
        }
        String[] pathes =  path.split("/");
        List<String> pathList = new LinkedList<String>();
        Collections.addAll(pathList, pathes);
        return CreateDirectory(sourceNode, pathList);
    }

    /**
     * Create multiple directories in ISO
     * @param sourceFolder base directory in ISO
     *                   @see com.github.stephenc.javaisotools.iso9660.ISO9660Directory
     * @param pathList list of paths for new directories
     * @return first of new ISO directories
     * @see com.github.stephenc.javaisotools.iso9660.ISO9660Directory
     */
    private static ISO9660Directory CreateDirectory(ISO9660Directory sourceFolder, List<String> pathList) {
        ISO9660Directory targetFolder = null;

        List<ISO9660Directory> currentDirectories = sourceFolder.getDirectories();

        for (ISO9660Directory dir : currentDirectories) {
            if (dir.getName().equals(pathList.get(0))) {
                targetFolder = dir;
                break;
            }
        }
        if (targetFolder == null) {
            targetFolder = sourceFolder.addDirectory(pathList.get(0));
            targetFolder.setName(pathList.get(0));
        }

        pathList.remove(0);
        if (pathList.size() > 0) {
            return CreateDirectory(targetFolder, pathList);
        }
        else {
            return targetFolder;
        }
    }
}
