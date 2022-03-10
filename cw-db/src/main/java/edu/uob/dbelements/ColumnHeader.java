package edu.uob.dbelements;

import edu.uob.abstractelements.AbstractColumnData;
import edu.uob.dbfilesystem.DBColumnType;
import edu.uob.dbfilesystem.DBDataType;

public class ColumnHeader extends AbstractColumnData {

    private int colNumber;
    // private String colName;
    private DBDataType dataType;
    private int dataFieldLength;
    private DBColumnType colType;
    private String description;
    private boolean notNull;

    public ColumnHeader(){

    }

    public ColumnHeader(int colNum, String colName, DBDataType dataType, int dataFieldLength, DBColumnType colType){
        this.colNumber = colNum;
        this.data = colName;
        this.dataType = dataType;
        this.dataFieldLength = dataFieldLength;
        this.colType = colType;
    }

    public int getColNumber() {
        return colNumber;
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
    }

    public String getColName() {
        return data;
    }

    public void setColName(String colName) {
        this.data = colName;
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public void setDataType(DBDataType dataType) {
        this.dataType = dataType;
    }

    public int getDataFieldLength() {
        return dataFieldLength;
    }

    public void setDataFieldLength(int dataFieldLength) {
        this.dataFieldLength = dataFieldLength;
    }

    public DBColumnType getColType() {
        return colType;
    }

    public void setColType(DBColumnType colType) {
        this.colType = colType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }
}
