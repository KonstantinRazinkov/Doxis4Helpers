package com.sersolutions.doxis4helpers.archiver.datatypes;

import java.util.ArrayList;
import java.util.List;

public class OneDriveFile {
    private String fileName;
    private String oneDriveLink;
    private String doxis4DocumentID;
    private OneDriveFileCheckedOutStatus status;
    public boolean justUpdated;

    public OneDriveFile(List<String> gvlLine) {
        this.fileName = gvlLine.get(0);
        this.oneDriveLink = gvlLine.get(1);
        this.doxis4DocumentID = gvlLine.get(2);

        switch ( gvlLine.get(3)) {
            case "CHECKEDOUT":
                this.status = OneDriveFileCheckedOutStatus.CHECKEDOUT;
                break;
            default:
                this.status = OneDriveFileCheckedOutStatus.CHECKEDIN;
                break;
        }
    }
    public List<String> getGVLLine() {
        ArrayList<String> line = new ArrayList<>(4);
        line.add(this.fileName);
        line.add(this.oneDriveLink);
        line.add(this.doxis4DocumentID);
        line.add(this.status.toString());
        return line;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOneDriveLink() {
        return oneDriveLink;
    }

    public void setOneDriveLink(String oneDriveLink) {
        this.oneDriveLink = oneDriveLink;
    }

    public String getDoxis4DocumentID() {
        return doxis4DocumentID;
    }

    public void setDoxis4DocumentID(String doxis4DocumentID) {
        this.doxis4DocumentID = doxis4DocumentID;
    }

    public OneDriveFileCheckedOutStatus getStatus() {
        return status;
    }

    public void setStatus(OneDriveFileCheckedOutStatus status) {
        this.status = status;
    }

    public boolean isJustUpdated() {
        return justUpdated;
    }

    public void setJustUpdated(boolean justUpdated) {
        this.justUpdated = justUpdated;
    }
}
