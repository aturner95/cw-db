package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.TableHeader;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateCMD extends DBCmd {

    public CreateCMD(){
        this.tableNames = new ArrayList<>();
        this.colNames = new ArrayList<>();
    }

    @Override
    public String query(DBServer server) throws DBException, IOException {

        try {
            if (getDatabaseName() != null && getTableNames().size() == 0) {
                createDatabase();
                return STATUS_OK;
            }

            else if (getDatabaseName() == null && getTableNames().size() == 1) {
                createTable(server);
                return STATUS_OK;

            } else {
                throw new DBException();
            }

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void createDatabase() throws DBTableExistsException{
        File database = new File(getDatabaseName());
        if(!database.exists() && !database.isDirectory()){
            database.mkdir();
            return;
        }
        throw new DBTableExistsException(getDatabaseName());
    }

    private void createTable(DBServer server) throws DBException, IOException{

        if(hasDatabase(server)){
            String tableName = getTableNames().get(0);
            File file = new File(tableName);

            if(!file.exists()){
                try {
                    String path = server.getDatabaseDirectory() + File.separator + tableName + ".tab";
                    file = new File (path);
                    if(file.createNewFile()){
                        Table table = new Table();
                        if(getColNames().size() > 0) {
                            TableHeader header = new TableHeader();
                            header.setTableName(tableName);
                            header.setFileLocation(file);
                            table.setHeader(header);
                            addAttributeList(table, getColNames());
                        }
                            DBTableFile dbFile = new DBTableFile();
                            dbFile.storeEntityIntoDBFile(table);
                            return;
                    }
                } catch(IOException ioe){
                    throw new IOException("Unable to write to table:" + tableName);
                }
            }
            throw new DBTableExistsException(tableName);
        }
        throw new DBDoesNotExistException(server.getDatabaseDirectory().getName());
    }

    private void addAttributeList(Table table, List<String> attributeList){

        List<ColumnHeader> columnHeaders = new ArrayList<>();
        columnHeaders.add(new ColumnHeader("id"));
        for(String attribute : attributeList){
            columnHeaders.add(new ColumnHeader(attribute));
        }
        table.setColHeadings(columnHeaders);
    }

}
