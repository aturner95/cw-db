package edu.uob.dbelements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private TableHeader tableHeader;
    private List<ColumnHeader> colHeadings;
    private List<Record> rows;

    public Table(){
        // this.colHeadings = new ArrayList<>();
        this.rows = new ArrayList<Record>();
    }

    public Table(String tableName, File fileLocation){
        this.tableHeader = new TableHeader();
        this.tableHeader.setTableName(tableName);
        this.tableHeader.setFileLocation(fileLocation);
        // this.colHeadings = new ArrayList<>();
        this.rows = new ArrayList<Record>();

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
}

