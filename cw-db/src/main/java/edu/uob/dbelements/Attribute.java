package edu.uob.dbelements;

import edu.uob.abstractelements.AbstractColumnData;
import edu.uob.dbfilesystem.DBDataType;

public class Attribute extends AbstractColumnData {

    // private String value;
    private DBDataType dataType;

    public Attribute(){

    }

    public Attribute(String value, DBDataType dataType){
        this.data = value;
        this.dataType = dataType;
    }

    public String getValue() {
        return data;
    }

    public void setValue(String value) {
        this.data = value;
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public void setDataType(DBDataType dataType) {
        this.dataType = dataType;
    }
}
