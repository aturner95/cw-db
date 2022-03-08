package edu.uob.dbelements;

import edu.uob.dbfilesystem.DBDataType;

public class Attribute {

    private String value;
    private DBDataType dataType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public void setDataType(DBDataType dataType) {
        this.dataType = dataType;
    }
}
