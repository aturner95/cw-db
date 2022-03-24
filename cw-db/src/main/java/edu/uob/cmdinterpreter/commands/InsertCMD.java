package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException.*;

import java.util.ArrayList;
import java.util.List;

public class InsertCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        byte indexOfTable = 0;
        String tableName = getTableNames().get(indexOfTable);

        try {
            if (hasDatabase(server)) {
                if (hasTable(server, tableName)) {
                    DBTableFile dbFile = new DBTableFile();
                    Table table = readTableFromFile(server, tableName);

                    if(!hasCorrectAttributeList(table, getVariables())){
                        throw new DBInvalidAttributeListException(table.getColHeadings().size(), getVariables().size());
                    }

                    List<Record> data = table.getRows();

                    insertEntity(server, data, getVariables());
                    dbFile.storeEntityIntoDBFile(table);
                    return STATUS_OK;
                }
                throw new DBTableDoesNotExistException(tableName);
            }
            throw new DBDoesNotExistException(getDatabaseName());

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private boolean hasCorrectAttributeList(Table table, List<String> variables){
        return table.getColHeadings().size() == variables.size() + 1;
    }

    private void insertEntity(DBServer server, List<Record> data, List<String> variables) throws Exception {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getNextSeq(server.getUseDatabaseDirectory().getName(), getTableNames().get(0))));
        for(String var: variables){
            attributes.add(new Attribute(var));
        }
        data.add(new Record(attributes));
    }

    private String getNextSeq(String databaseName, String tableName) throws Exception {
        DBTableFile file = new DBTableFile();
        int seq = file.nextSeq(databaseName, tableName);
        return Integer.toString(seq);
    }

}
