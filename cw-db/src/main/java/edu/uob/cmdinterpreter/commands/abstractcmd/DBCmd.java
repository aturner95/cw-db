package edu.uob.cmdinterpreter.commands.abstractcmd;

import edu.uob.DBServer;

import java.util.List;

public abstract class DBCmd {

    /* Variables */
    protected List<String> tableNames;
    protected List<String> colNames;

    /* Constructors */
    public DBCmd(){
        super();
    }

    /* Methods */
    public abstract String query(DBServer server);

    public List<String> getTableNames(){
        return tableNames;
    }

    public boolean addTableName(String tableName){
        if(tableName != null && tableName != null){
            tableNames.add(tableName);
            return true;
        }
        return false;
    }

    public List<String> getColNames(){
        return colNames;
    }

    public boolean addColumnName(String colName){
        if(colNames != null && colName != null){
            colNames.add(colName);
            return true;
        }
        return false;
    }
}
