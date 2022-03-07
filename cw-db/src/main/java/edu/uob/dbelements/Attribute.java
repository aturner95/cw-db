package edu.uob.dbelements;

import edu.uob.dbfilesystem.DBDataType;

public abstract class Attribute {

    private Object value;
    private DBDataType dataType;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public void setDataType(DBDataType dataType) {
        this.dataType = dataType;
    }
}
