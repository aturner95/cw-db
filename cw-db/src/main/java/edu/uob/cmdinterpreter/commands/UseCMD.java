package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class UseCMD extends DBCmd {

    public UseCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) {

        if(server != null && tableNames.size() == 1 && colNames.size() == 0) {
            byte databaseIndex = 0;
            File dbDir = new File(tableNames.get(databaseIndex));
            if (dbDir.exists() && dbDir.isDirectory()) {
                server.setDatabaseDirectory(dbDir);
                return new String();
            }
        }
        // TODO throw exception
        return null;
    }
}
