package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Table;
import edu.uob.exceptions.*;
import edu.uob.exceptions.DBException.*;

import java.io.IOException;

public class JoinCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        byte indexOfTableA = 0, indexOfTableB = 1;
        byte indexOfAttributeA = 0, indexOfAttributeB = 1;

        try {
            if (hasDatabase(server)) {
                String tableNameA = getTableNames().get(indexOfTableA);
                String tableNameB = getTableNames().get(indexOfTableB);

                if (hasTable(server, tableNameA) && hasTable(server, tableNameB)) {
                    String attributeNameA = getColNames().get(indexOfAttributeA);
                    String attributeNameB = getColNames().get(indexOfAttributeB);

                    Table tableOne = readTableFromFile(server, tableNameA);
                    Table tableTwo = readTableFromFile(server, tableNameB);

                    if (hasAttribute(tableOne, attributeNameA) && hasAttribute(tableTwo, attributeNameB)) {

                        // TODO here is where we construct the result of sorts..
                        return STATUS_OK;
                    }
                    String errorMsg = attributeNameA + " or " + attributeNameB;
                    throw new DBAttributeDoesNotExistException(errorMsg);
                }
                String errorMsg = tableNameA + " or " + tableNameB;
                throw new DBTableDoesNotExistException(errorMsg);
            }
            throw new DBDoesNotExistException(getDatabaseName());

        } catch (Exception e) {
            return STATUS_ERROR + e.getMessage();
        }
    }

}
