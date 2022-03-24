package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.DBDoesNotExistException;

import java.io.File;
import java.util.ArrayList;

public class UseCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        try {
            if (getDatabaseName() != null && !getDatabaseName().isEmpty()) {
                File dbDir = new File(getDatabaseName());
                if (dbDir.exists() && dbDir.isDirectory()) {
                    server.setUseDatabaseDirectory(dbDir);
                    return STATUS_OK;
                }
                throw new DBDoesNotExistException(getDatabaseName());
            }
            throw new DBException();

        } catch (Exception e) {
            return STATUS_ERROR + e.getMessage();
        }
    }
}
