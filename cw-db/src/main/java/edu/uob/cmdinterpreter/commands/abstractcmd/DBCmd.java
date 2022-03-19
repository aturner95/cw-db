package edu.uob.cmdinterpreter.commands.abstractcmd;

import edu.uob.DBServer;

import java.io.File;
import java.util.List;

public abstract class DBCmd {

    /* Variables */
    protected String databaseName;
    protected List<String> tableNames;
    protected List<String> colNames;

    /* Constructors */
    public DBCmd(){
        super();
    }

    /* Methods */
    public abstract String query(DBServer server);

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName){
        this.databaseName = databaseName;
    }

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

    public boolean hasDatabase(DBServer server){
        return (server.getDatabaseDirectory().exists() && server.getDatabaseDirectory().isDirectory());
    }

    public boolean hasTable(DBServer server, String tableName){
        File db = new File(server.getDatabaseDirectory().getName());
        File [] tables = db.listFiles();
        for(File table: tables){
            int indexOfFileExt = table.getName().length() - 4;
            if(tableName.equalsIgnoreCase(table.getName().substring(0, indexOfFileExt))){
                return true;
            }
        }
        return false;
    }

}
