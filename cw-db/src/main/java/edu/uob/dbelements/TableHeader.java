package edu.uob.dbelements;

import java.io.File;

public class TableHeader {

    private String tableName;
    private File fileLocation;
    private String description;
    private long nextId;

    public TableHeader(){

    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public File getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(File fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNextId() {
        return nextId;
    }

    public void incrementId() {
        this.nextId++;
    }
}
