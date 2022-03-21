package edu.uob.dbelements;

import edu.uob.dbelements.abstractelements.AbstractColumnData;

public class ColumnHeader extends AbstractColumnData {

    public ColumnHeader(String colName){
        this.data = colName;
    }

    public String getColName() {
        return data;
    }

    public void setColName(String colName) {
        this.data = colName;
    }

}
