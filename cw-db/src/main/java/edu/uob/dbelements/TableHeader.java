package edu.uob.dbelements;

import java.io.File;

public class TableHeader {

    private String tableName;
    private File fileLocation;

    public TableHeader(){
        super();
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

}
