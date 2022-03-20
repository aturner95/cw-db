package edu.uob.cmdinterpreter.commands.abstractcmd;

import edu.uob.DBServer;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DBCmd {

    /* Variables */
    protected String databaseName;
    protected List<String> tableNames;
    protected List<String> colNames;
    protected List<String> variables;

    public static final String STATUS_OK = "[OK]";
    public static final String STATUS_ERROR = "[ERROR] ";
    public static final String SPACE = " ";

    /* Constructors */
    public DBCmd(){
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        variables = new ArrayList<>();
    }

    /* Methods */
    public abstract String query(DBServer server) throws Exception;

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

    public List<String> getVariables(){
        return variables;
    }

    public boolean addVariable(String variable){
        if(variables != null && variable != null){
            variables.add(variable);
            return true;
        }
        return false;
    }

    public boolean hasDatabase(DBServer server) {
        if(server.getDatabaseDirectory().exists() && server.getDatabaseDirectory().isDirectory()){
            return true;
        }
        return false;
    }

    public boolean hasTable(DBServer server, String tableName) {
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

    public boolean hasAttribute(Table table, String attributeName) {
        List<ColumnHeader> colHeads = table.getColHeadings();
        for(ColumnHeader header: colHeads){
            if(attributeName.equalsIgnoreCase(header.getColName())){
                return true;
            }
        }
        return false;
    }

    public Table readTableFromFile(DBServer server, String tableName) throws IOException {
        DBTableFile dbFile = new DBTableFile();
        File file = new File(server.getDatabaseDirectory() + File.separator + tableName);
        Table table;
        return dbFile.readDBFileIntoEntity(file.getPath() + ".tab");
    }

}
