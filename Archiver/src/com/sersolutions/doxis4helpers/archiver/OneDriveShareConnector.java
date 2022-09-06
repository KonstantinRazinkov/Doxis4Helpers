package com.sersolutions.doxis4helpers.archiver;

import com.ser.blueline.IDocumentPart;
import com.ser.blueline.ISession;
import com.sersolutions.doxis4helpers.archiver.datatypes.OneDriveFile;
import com.sersolutions.doxis4helpers.archiver.datatypes.OneDriveFileCheckedOutStatus;
import com.sersolutions.doxis4helpers.archiver.datatypes.OneDriveFileTransferMethod;
import com.sersolutions.doxis4helpers.commons.GlobalValueLists;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OneDriveShareConnector {
    private static String gvlSettingsName = "ru.sersolutions.helpers.onedrive/OneDriveConnectionSettings";
    private static String gvlFilesName = "ru.sersolutions.helpers.onedrive/OneDriveSharedFiles";
    private ISession doxis4Session;
    private OneDriveFileTransferMethod fileTransferMethod;
    private String shareFolder;
    private String smbLogin;
    private String smbPass;
    private int filesCount;
    private List<OneDriveFile> listOneDriveFiles;

    private static OneDriveShareConnector singleton;
    public static OneDriveShareConnector getSingleton(ISession session) {
        if (singleton == null) {
            singleton = new OneDriveShareConnector(session);
        }
        return  singleton;
    }

    public OneDriveShareConnector(ISession doxis4Session) {
        this.doxis4Session = doxis4Session;
        List<List<String>> gvlSettings = GlobalValueLists.GetLinesFromGlobalValueList(doxis4Session, gvlSettingsName, "*", 0);
        switch (gvlSettings.get(0).get(1)) {
            case "Direct":
                this.fileTransferMethod = OneDriveFileTransferMethod.Direct;
                break;
            default:
                this.fileTransferMethod = OneDriveFileTransferMethod.SMB;
                break;
        }

        this.shareFolder = gvlSettings.get(1).get(1);
        this.filesCount = Integer.parseInt(gvlSettings.get(2).get(1));
        this.smbLogin = gvlSettings.get(3).get(1);
        this.smbPass = gvlSettings.get(4).get(1);

        listOneDriveFiles = new ArrayList<>(this.filesCount);
        List<List<String>> gvlFiles = GlobalValueLists.GetLinesFromGlobalValueList(doxis4Session, gvlFilesName, "*", 0);
        for (List<String> gvlFile : gvlFiles) {
            listOneDriveFiles.add(new OneDriveFile(gvlFile));
        }
    }

    public void saveFile(OneDriveFile oneDriveFile, IDocumentPart documentPart) throws Exception {

        byte[] contentfile =documentPart.getRawData();

        switch (fileTransferMethod) {
            case Direct:
                Files.write(Paths.get(String.format("%s%s", this.shareFolder, oneDriveFile.getFileName())), contentfile);
                break;
            case SMB:
                SmbFile newFile = new SmbFile(String.format("%s%s", this.shareFolder, oneDriveFile.getFileName()), new NtlmPasswordAuthentication("", this.smbLogin, this.smbPass));
                OutputStream out = newFile.getOutputStream();
                out.write(contentfile);
                out.flush();
                break;
        }
    }

    public byte[] loadFile(OneDriveFile oneDriveFile) throws Exception {
        byte[] contentfile =null;

        switch (fileTransferMethod) {
            case Direct:
                contentfile = Files.readAllBytes(Paths.get(String.format("%s%s", this.shareFolder, oneDriveFile.getFileName())));
                break;
            case SMB:
                SmbFile readFile = new SmbFile(String.format("%s%s", this.shareFolder, oneDriveFile.getFileName()), new NtlmPasswordAuthentication("", this.smbLogin, this.smbPass));
                contentfile = new byte[readFile.getContentLength()];//
                try (InputStream inputStream = readFile.getInputStream()) {
                    inputStream.read(contentfile);
                }

                break;
        }
        return contentfile;
    }

    public OneDriveFile checkIn(String documentID) throws Exception {
        OneDriveFile searchFile = null;
        try {
            searchFile = listOneDriveFiles.stream().filter(x -> documentID.equals(x.getDoxis4DocumentID())).findFirst().get();
            if (searchFile != null) {
                searchFile.setStatus(OneDriveFileCheckedOutStatus.CHECKEDIN);
                GlobalValueLists.SetValueToGlobalValueList(doxis4Session, gvlFilesName, searchFile.getFileName(), 0, 3, OneDriveFileCheckedOutStatus.CHECKEDIN.toString());
                return searchFile;
            }
        } catch (Exception ex) {

        }

        throw new Exception(String.format("Не удалось найти сведения о выгруженном файле с идентификатором '%s'", documentID));

    }

    public OneDriveFile checkOut(String documentID) throws Exception {
        OneDriveFile searchFile = null;
        try {
            searchFile = listOneDriveFiles.stream().filter(x -> documentID.equals(x.getDoxis4DocumentID())).findFirst().get();
            if (searchFile != null) {

                if (searchFile.getStatus() == OneDriveFileCheckedOutStatus.CHECKEDIN) {
                    searchFile.setStatus(OneDriveFileCheckedOutStatus.CHECKEDOUT);
                    GlobalValueLists.SetValueToGlobalValueList(doxis4Session, gvlFilesName, searchFile.getFileName(), 0, 3, OneDriveFileCheckedOutStatus.CHECKEDOUT.toString());
                    return searchFile;
                } else {
                    searchFile.setJustUpdated(false);
                    return searchFile;
                }
            }
        } catch (Exception ex) {

        }
        try {
            searchFile = listOneDriveFiles.stream().filter(x -> x.getStatus() != OneDriveFileCheckedOutStatus.CHECKEDOUT).findFirst().get();
            if (searchFile != null) {
                if (searchFile.getStatus() == OneDriveFileCheckedOutStatus.CHECKEDIN) {
                    searchFile.setStatus(OneDriveFileCheckedOutStatus.CHECKEDOUT);
                    searchFile.setDoxis4DocumentID(documentID);
                    searchFile.setJustUpdated(true);
                    GlobalValueLists.SetValueToGlobalValueList(doxis4Session, gvlFilesName, searchFile.getFileName(), 0, 3, documentID);
                    GlobalValueLists.SetValueToGlobalValueList(doxis4Session, gvlFilesName, searchFile.getFileName(), 0, 3, OneDriveFileCheckedOutStatus.CHECKEDOUT.toString());
                    return searchFile;
                }
            }
        }
        catch (Exception ex) {}

        throw new Exception(String.format("Вы достигли лимита одновременно используемых файлов %d", this.getFilesCount()));

    }

    public OneDriveFileTransferMethod getFileTransferMethod() {
        return fileTransferMethod;
    }

    public String getShareFolder() {
        return shareFolder;
    }


    public int getFilesCount() {
        return filesCount;
    }

    public List<OneDriveFile> getListOneDriveFiles() {
        return listOneDriveFiles;
    }
}
