package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.QueryException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.uob.dbfilesystem.DBFileConstants.TABLE_EXT;


public class UpdateCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        try {
            if (hasDatabase(server)) {
                int indexOfTable = 0;
                String tableName = getTableNames().get(indexOfTable);
                if (hasTable(server, tableName)) {
                    String tablePath = server.getDatabaseDirectory() + File.separator + tableName + TABLE_EXT;
                    Table table = new DBTableFile().readDBFileIntoEntity(tablePath);

                    Table dataToUpdate = null;

                    dataToUpdate = doConditions(table, dataToUpdate);

                    // if the condition returns data to update
                    if(dataToUpdate.getRows().size() > 0) {

                        // for every row
                        for(Record row: table.getRows()){

                            // if the current row is in the list of required updates
                            if(dataToUpdate.getRows().contains(row)){

                                // for each name in the name Value pair list
                                for(NameValuePair nameValuePair: getNameValuePair()) {

                                    String name = nameValuePair.getName();
                                    String value = nameValuePair.getValue();

                                    // if the name exists in the table header
                                    if(table.getColHeadings().contains(new ColumnHeader(name))) {

                                        // get the index of the name in the column header
                                        int indexOfName = table.getColHeadings().indexOf(new ColumnHeader(name));

                                        // use index to update target value with data from name pair col
                                        row.getAttributes().get(indexOfName).setValue(value);

                                    } else {
                                        throw new QueryException.AttributeNotFoundException(name);
                                    }
                                }
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
