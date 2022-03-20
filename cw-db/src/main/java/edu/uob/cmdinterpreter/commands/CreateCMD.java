package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CreateCMD extends DBCmd {

    public CreateCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) throws DBException, IOException {

        try {
            if (getDatabaseName() != null && getTableNames().size() == 0) {
                createDatabase();
                return STATUS_OK;
            }

            else if (getDatabaseName() == null && getTableNames().size() == 1) {
                createTable(server);
                return STATUS_OK;

            } else {
                throw new DBException();
            }

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void createDatabase() throws DBTableExistsException{
        File database = new File(getDatabaseName());
        if(!database.exists() && !database.isDirectory()){
            database.mkdir();
            return;
        }
        throw new DBTableExistsException(getDatabaseName());
    }

    private void createTable(DBServer server) throws DBException, IOException{

        if(hasDatabase(server)){
            String tableName = getTableNames().get(0);
            File table = new File(tableName);

            if(!table.exists()){
                try {
                    table = new File (server.getDatabaseDirectory() + File.separator + tableName + ".tab");
                    if(table.createNewFile()){
                        return;
                    }
                } catch(IOException ioe){
                    throw new IOException("Unable to write to table:" + tableName);
                }
            }
            throw new DBTableExistsException(tableName);
        }
        throw new DBDoesNotExistException(server.getDatabaseDirectory().getName());
    }

}
