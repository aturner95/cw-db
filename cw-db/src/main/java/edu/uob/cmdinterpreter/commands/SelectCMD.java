package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
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
                    String tablePath = server.getUseDatabaseDirectory() + File.separator + tableName + TABLE_EXT;
                    Table table = new DBTableFile().readDBFileIntoEntity(tablePath);

                    if(table.getColHeadings() == null || table.getColHeadings().size() == 0){
                        return STATUS_OK;
                    }
                    Table result = null;

                    if(getConditions().size() > 0) {
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
