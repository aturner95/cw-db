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

        byte indexOfTable = 0, indexOfPrimaryKey = 0;
        String tableName = getTableNames().get(indexOfTable);
        // String id = getVariables().get(indexOfPrimaryKey);

        try {
            if (hasDatabase(server)) {
                if (hasTable(server, tableName)) {
                    DBTableFile dbFile = new DBTableFile();
                    Table table = readTableFromFile(server, tableName);

                    if(!hasCorrectAttributeList(table, getVariables())){
                        throw new DBInvalidAttributeListException(table.getColHeadings().size(), getVariables().size());
                    }

                    List<Record> data = table.getRows();

                    // if (!hasEntityId(data, id)) {
                        insertEntity(data, getVariables());
                        dbFile.storeEntityIntoDBFile(table);
                        return STATUS_OK;

                    // } else {
                    //     throw new DBEntityExistsException(id);
                    // }
                }
                throw new DBTableDoesNotExistException(tableName);
            }
            throw new DBDoesNotExistException(getDatabaseName());

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

//    private boolean hasEntityId(List<Record> data, String id){
//        try{
//            if(Long.valueOf(id) instanceof Long) {
//                for (Record row : data) {
//                    if (id.equals(row.getId())) {
//                        return true;
//                    }
//                }
//            }
//        } catch(NumberFormatException nfe) {
//            return false;
//        }
//        return false;
//    }

    private boolean hasCorrectAttributeList(Table table, List<String> variables){
        return table.getColHeadings().size() == variables.size() + 1;
    }

    private void insertEntity(List<Record> data, List<String> variables){
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getNextSeq(data)));
        for(String var: variables){
            attributes.add(new Attribute(var));
        }
        data.add(new Record(attributes));
    }

    private String getNextSeq(List<Record> data){
        if(data.size() == 0){
            return "1";
        }
        Integer currentKey = Integer.valueOf(data.get(data.size() - 1).getId());
        return  Integer.toString(currentKey.intValue() + 1);
    }

}
