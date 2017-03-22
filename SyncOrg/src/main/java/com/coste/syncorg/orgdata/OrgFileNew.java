package com.coste.syncorg.orgdata;

import com.coste.syncorg.orgdata.table.FileEntity;

import org.joda.time.DateTime;

public class OrgFileNew {
    private FileEntity fileEntity;


    public OrgFileNew() {
        this.fileEntity = new FileEntity();
    }

    public long getId() {
        return fileEntity.getId();
    }

    public Long getRootNodeId() {
        return fileEntity.getRootNodeId();
    }

    public String getFileName() {
        return fileEntity.getFileName();
    }

    public String getDisplayName() {
        return fileEntity.getDisplayName();
    }

    public String getComment() {
        return fileEntity.getComment();
    }

    public DateTime getCreated() {
        return fileEntity.getCreated();
    }

    public DateTime getLastModified() {
        return fileEntity.getLastModified();
    }

    public FileEntity setFileName(String fileName) {
        return fileEntity.setFileName(fileName);
    }

    public FileEntity setDisplayName(String displayName) {
        return fileEntity.setDisplayName(displayName);
    }

    public FileEntity setComment(String comment) {
        return fileEntity.setComment(comment);
    }

    public FileEntity setCreated(DateTime created) {
        return fileEntity.setCreated(created);
    }

    public FileEntity setLastModified(DateTime lastModified) {
        return fileEntity.setLastModified(lastModified);
    }
}
