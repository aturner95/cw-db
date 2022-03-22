package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.QueryCondition;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;

import static edu.uob.dbfilesystem.DBFileConstants.TABLE_EXT;

public class SelectCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        try {
            if (hasDatabase(server)) {
                int indexOfTable = 0;
                String tableName = getTableNames().get(indexOfTable);
                if (hasTable(server, tableName)) {

                    String tablePath = server.getDatabaseDirectory() + File.separator + tableName + TABLE_EXT;
                    Table table = new DBTableFile().readDBFileIntoEntity(tablePath);

                    if(table.getColHeadings() == null || table.getColHeadings().size() == 0){
                        return STATUS_OK;
                    }

                    Table result = null;

                    // TODO this is so janky... try DRY it up if you can, for now just get it working!
                    if(getConditions().size() > 0) {
                        // single condition
//                        if(getConditions().size() == 1){
//                            result = filterResultByCondition(table, getConditions().get(0));
//                        }
//                        else if(getConditions().size() == 2){
//                            // AND'ed condition
//                            if(BNFConstants.AND.equalsIgnoreCase(getConditionJoinOperators().get(0))){
//                                result = filterResultByAndConditions(table, getConditions().get(0), getConditions().get(1));
//                            // OR'ed condition
//                            } else {
//                                result = filterResultByOrConditions(table, getConditions().get(0), getConditions().get(1));
//                            }
//                        } else {
//                            throw new DBException("Sorry, haven't managed to implement nested conditions!");
//                        }

                        result = doConditions(table, result);

                        result = buildResultTable(result);
                    } else {
                        result = buildResultTable(table);
                    }

                    return STATUS_OK + System.lineSeparator() + result.toString();

                }
                throw new DBTableDoesNotExistException(getTableNames().get(0));
            }
            throw new DBTableDoesNotExistException(getDatabaseName());

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

}
