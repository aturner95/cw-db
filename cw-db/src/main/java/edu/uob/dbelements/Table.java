package edu.uob.dbelements;

import java.io.File;
import java.util.List;

public class Table {

    private TableHeader tableHeader;
    private List<Column> colHeadings;
    private List<Record> rows;

    public Table(){

    }

    public Table(String tableName, File fileLocation){
        this.tableHeader = new TableHeader();
        this.tableHeader.setTableName(tableName);
        this.tableHeader.setFileLocation(fileLocation);

    }

    public TableHeader getHeader() {
        return tableHeader;
    }

    public void setHeader(TableHeader header) {
        this.tableHeader = header;
    }

    public List<Column> getColHeadings() {
        return colHeadings;
    }

    public void setColHeadings(List<Column> colHeadings) {
        this.colHeadings = colHeadings;
    }

    public List<Record> getRows() {
        return rows;
    }

    public void setRows(List<Record> rows) {
        this.rows = rows;
    }
}

