package edu.uob.cmdinterpreter;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.abstractcmd.DBCmd;

import java.util.ArrayList;

public class SelectCMD extends DBCmd {

    public SelectCMD(){
        // this.DBName = new String();
        // this.commandType = new String();
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) {
        return null;
    }
}
