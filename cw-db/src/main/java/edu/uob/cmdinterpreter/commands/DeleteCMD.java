package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;

import java.io.File;

import static edu.uob.dbfilesystem.DBFileConstants.TABLE_EXT;

public class DeleteCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        try {
            if (hasDatabase(server)) {
                int indexOfTable = 0;
                String tableName = getTableNames().get(indexOfTable);
                if (hasTable(server, tableName)) {
                    String tablePath = server.getUseDatabaseDirectory() + File.separator + tableName + TABLE_EXT;
                    Table table = new DBTableFile().readDBFileIntoEntity(tablePath);

                    Table dataToDelete = null;

                    dataToDelete = doConditions(table, dataToDelete);

                    if(dataToDelete.getRows().size() > 0) {

                        for (Record deleteRow : dataToDelete.getRows()) {

                            if (table.getRows().contains(deleteRow)) {
                                table.getRows().remove(deleteRow);
                            }
                        }
                        new DBTableFile().storeEntityIntoDBFile(table);
                    }

                    return STATUS_OK;
                }
                throw new DBException.DBTableDoesNotExistException(getTableNames().get(0));
            }
            throw new DBException.DBTableDoesNotExistException(getDatabaseName());

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

}
