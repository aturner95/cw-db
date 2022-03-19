package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBTableFile;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InsertCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        byte indexOfTable = 0, indexOfPrimaryKey = 0;
        String tableName = getTableNames().get(indexOfTable);
        String id = getVariables().get(indexOfPrimaryKey);

        if(hasDatabase(server) && hasTable(server, tableName)){
            try {
                DBTableFile dbFile = new DBTableFile();
                Table table = readTableFromFile(server, tableName);
                List<Record> data = table.getRows();
                if(!hasEntityId(data, id)){
                    insertEntity(data, getVariables());
                    dbFile.storeEntityIntoDBFile(table);
                    return new String();
                }

            } catch(IOException ioe){
                return null;
            }
        }
        return null;
    }

    private boolean hasEntityId(List<Record> data, String id){
        try{
            if(Long.valueOf(id) instanceof Long) {
                for (Record row : data) {
                    if (id.equals(row.getId())) {
                        return true;
                    }
                }
            }
        } catch(NumberFormatException nfe) {
            return false;
        }
        return false;
    }

    private void insertEntity(List<Record> data, List<String> variables){
        List<Attribute> attributes = new ArrayList<>();
        for(String var: variables){
            attributes.add(new Attribute(var));
        }
        data.add(new Record(attributes));
    }
}
