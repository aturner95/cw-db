package edu.uob.dbelements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private TableHeader tableHeader;
    private List<ColumnHeader> colHeadings;
    private List<Record> rows;

    public Table(){
        this.rows = new ArrayList<>();
    }

    public Table(String tableName, File fileLocation){
        this.tableHeader = new TableHeader();
        this.tableHeader.setTableName(tableName);
        this.tableHeader.setFileLocation(fileLocation);
        this.rows = new ArrayList<>();

    }

    public TableHeader getHeader() {
        return tableHeader;
    }

    public void setHeader(TableHeader header) {
        this.tableHeader = header;
    }

    public List<ColumnHeader> getColHeadings() {
        return colHeadings;
    }

    public void setColHeadings(List<ColumnHeader> colHeadings) {
        this.colHeadings = colHeadings;
    }

    public List<Record> getRows() {
        return rows;
    }

    public void setRows(List<Record> rows) {
        this.rows = rows;
    }

    @Override
    public String toString(){
        StringBuilder asString = new StringBuilder();
        for(int headerIndex = 0; headerIndex < getColHeadings().size(); headerIndex++){
            asString.append(getColHeadings().get(headerIndex).getColName());
            if(headerIndex < getColHeadings().size() - 1){
                asString.append("\t");
            } else {
                asString.append("\n");
            }
        }
        for(int rowIndex = 0; rowIndex < getRows().size(); rowIndex++){
            List<Attribute> rowData = getRows().get(rowIndex).getAttributes();

            for(int attrIndex = 0; attrIndex < rowData.size(); attrIndex++){
                asString.append(rowData.get(attrIndex).getValue());
                if(attrIndex < rowData.size() - 1){
                    asString.append("\t");
                } else {
                    asString.append("\n");
                }
            }
        }
        return asString.toString();
    }
}
