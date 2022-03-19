package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBTableFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlterCMD extends DBCmd {

    private String alterationType;

    public AlterCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    public AlterCMD(String alterationType){
        this();
        this.alterationType = alterationType;
    }



    @Override
    public String query(DBServer server) {

        byte indexOfTableName = 0, indexOfAttributeName = 0;
        String tableName = getTableNames().get(indexOfTableName);
        String attributeName = getColNames().get(indexOfAttributeName);

        if(hasDatabase(server)){
            if(hasTable(server, tableName)){
                DBTableFile dbFile = new DBTableFile();
                File file = new File(server.getDatabaseDirectory() + File.separator + tableName);
                Table table;
                try {
                    table = dbFile.readDBFileIntoEntity(file.getPath() + ".tab");
                    if (BNFConstants.ADD.equalsIgnoreCase(alterationType)) {
                        if(!hasAttribute(table, attributeName)){
                            addAttribute(table, attributeName);
                        }

                    } else if (BNFConstants.DROP.equalsIgnoreCase(alterationType)) {
                        if(hasAttribute(table, attributeName)){
                            removeAttribute(table, attributeName);
                        }
                    } else {
                        // TODO throw exception
                        return null;
                    }
                    dbFile.storeEntityIntoDBFile(table);
                    return new String();

                } catch(IOException ioe){
                    return null;
                }
            }
        }
        return null;
    }

    private void addAttribute(Table table, String attributeName){
        table.getColHeadings().add(new ColumnHeader(attributeName));
        populateNewData(table.getRows());
    }

    private void removeAttribute(Table table, String attributeName){
        List<ColumnHeader> headers = table.getColHeadings();
        int indexOf = getAttributeIndex(table.getColHeadings(), attributeName);
        headers.remove(indexOf);
        List<Record> rows = table.getRows();
        deleteExistingData(rows, indexOf);
    }

    private int getAttributeIndex(List<ColumnHeader> columnHeaders, String attribute){
        int counter = 0;
        for(ColumnHeader header: columnHeaders){
            if(attribute.equalsIgnoreCase(header.getColName())){
                return counter;
            }
            counter++;
        }
        return -1;
    }

    private void populateNewData(List<Record> data){
        for(Record row: data){
            row.addDefaultAttribute();
        }
    }

    private void deleteExistingData(List<Record> data, int index) {
        for (Record row : data) {
            row.getAttributes().remove(index);
        }
    }
}
