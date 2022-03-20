package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.util.ArrayList;

public class DropCMD extends DBCmd {

    public DropCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) {

        try {
            if(hasDatabase(server)) {
                if (getDatabaseName() != null && getTableNames().size() == 0) {
                    dropDatabase();
                    return STATUS_OK;

                } if (getDatabaseName() == null && getTableNames().size() == 1) {
                    dropTable();
                    return STATUS_OK;
                }
                throw new DBException();
            }
            throw new DBDoesNotExistException(getDatabaseName());

        }catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void dropDatabase() {
        File database = new File(getDatabaseName());
        deleteDirectory(database);
    }

    private void dropTable() throws DBTableDoesNotExistException {
        byte indexOfTable = 0;
        File table = new File(getTableNames().get(indexOfTable));
        if(table.exists() && table.isFile()){
            table.delete();
            return;
        }
        throw new DBTableDoesNotExistException(table.getName());
    }

    private void deleteDirectory(File dir){
        File [] contents = dir.listFiles();
        for(File dbfile: contents){
            dbfile.delete();
        }
        dir.delete();
    }
}
