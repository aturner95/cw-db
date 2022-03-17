package edu.uob.cmdinterpreter;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.abstractcmd.DBCmd;

public class UseCMD extends DBCmd {

    @Override
    protected String query(DBServer server) {
        return null;
    }
}
