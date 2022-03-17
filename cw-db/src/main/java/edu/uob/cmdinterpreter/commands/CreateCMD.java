package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class CreateCMD extends DBCmd {

    public CreateCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) {

        if(server != null && getDatabaseName() != null && getTableNames().size() == 0 && getColNames().size() == 0){
            return createDatabase();
        }

        if(server != null && getDatabaseName() == null && getTableNames().size() == 1 && getColNames().size() == 0){
            return createTable(server);
        }

        return null;
    }

    private String createDatabase(){
        File database = new File(getDatabaseName());
        if(!database.exists() && !database.isDirectory()){
            database.mkdir();
            return new String();

        } else {
            // TODO throw exception
            return null;
        }
    }

    private String createTable(DBServer server){

        // check if database exists
        if(server.getDatabaseDirectory().exists() && server.getDatabaseDirectory().isDirectory()){

            String tableName = getTableNames().get(0);
            File table = new File(tableName);
            // check if table exists
            if(!table.exists()){
                try {
                    table = new File (server.getDatabaseDirectory() + File.separator + tableName + ".tab");
                    if(table.createNewFile()){
                        return new String();
                    }
                } catch(IOException ioe){
                    // TODO handle exception
                    return null;
                }
            }
        }
        return null;
    }
}
