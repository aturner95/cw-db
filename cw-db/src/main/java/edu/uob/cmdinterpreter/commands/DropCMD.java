package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;

import java.io.File;
import java.util.ArrayList;

public class DropCMD extends DBCmd {

    public DropCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) {

        if(server != null && getDatabaseName() != null && getTableNames().size() == 0){
            return dropDatabase();
        }
        if(server != null && getDatabaseName() == null && getTableNames().size() == 1){
            return dropTable();
        }

        // TODO throw exception
        return null;
    }

    private String dropDatabase(){
        File database = new File(getDatabaseName());
        if(database.exists() && database.isDirectory()){
            deleteDirectory(database);
            return new String();
        }
        // TODO throw exception
        return null;
    }

    private String dropTable(){
        byte indexOfTable = 0;
        File table = new File(getTableNames().get(indexOfTable));
        if(table.exists() && table.isFile()){
            table.delete();
            return new String();
        }
        return null;
    }

    private void deleteDirectory(File dir){
        File [] contents = dir.listFiles();
        for(File dbfile: contents){
            dbfile.delete();
        }
        dir.delete();
    }
}
