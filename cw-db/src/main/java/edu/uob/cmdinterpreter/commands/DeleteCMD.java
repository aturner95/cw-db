package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;

public class DeleteCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        // if the database is in context
        if(hasDatabase(server)){
            byte indexOfTable = 0;

            // if the database has the table
            // if(hasTable(server, getTableNames().get(indexOfTable))){

                // unpack the condition

                // find the entities that match these conditions
                // for each condition
                    // for each row in table rows
                        //

                // remove these rows
            // }
        }

        return null;
    }


}
