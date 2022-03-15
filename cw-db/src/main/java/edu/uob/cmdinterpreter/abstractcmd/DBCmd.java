package edu.uob.cmdinterpreter.abstractcmd;

import edu.uob.DBServer;

import java.util.List;

public abstract class DBCmd {

    /* Variables */
    protected String DBName;
    protected String commandType;
    protected List<String> tableNames;
    protected List<String> colNames;


    /* Methods */
    protected abstract String query(DBServer server);

    protected boolean addTableName(String tableName){
        if(tableName != null && tableName != null){
            tableNames.add(tableName);
            return true;
        }
        return false;
    }

    protected boolean addColumnName(String colName){
        if(colNames != null && colName != null){
            colNames.add(colName);
            return true;
        }
        return false;
    }
}
