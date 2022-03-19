package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.Table;

import java.io.IOException;

public class JoinCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        byte indexOfTableA = 0, indexOfTableB = 1;
        byte indexOfAttributeA = 0, indexOfAttributeB = 1;

        if(hasDatabase(server)){
            String tableNameA = getTableNames().get(indexOfTableA);
            String tableNameB = getTableNames().get(indexOfTableB);

            if(hasTable(server, tableNameA) && hasTable(server, tableNameB)){
                String attributeNameA = getColNames().get(indexOfAttributeA);
                String attributeNameB = getColNames().get(indexOfAttributeB);

                try {
                    Table tableOne = readTableFromFile(server, tableNameA);
                    Table tableTwo = readTableFromFile(server, tableNameB);

                    if (hasAttribute(tableOne, attributeNameA) && hasAttribute(tableTwo, attributeNameB)) {

                        // TODO here is where we construct the result of sorts..
                        return new String();
                    }
                } catch(IOException ioe){
                    return null;
                }
            }
        }
        return null;
    }
}
