package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.TableHeader;
import edu.uob.dbfilesystem.DBFileConstants;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateCMD extends DBCmd {

    public CreateCMD(String createType){
        super(createType);
    }

    public String getCreateType() {
        return commandParameter;
    }

    @Override
    public String query(DBServer server) throws DBException, IOException {

        try {
            if (BNFConstants.DATABASE.equalsIgnoreCase(commandParameter)) {
                createDatabase();
                return STATUS_OK;
            }

            if(BNFConstants.TABLE.equalsIgnoreCase(commandParameter)) {
                createTable(server);
                return STATUS_OK;
            }
            throw new DBException();

        } catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void createDatabase() throws DBException {
        File database = new File("." + File.separator + getDatabaseName().toLowerCase(Locale.ROOT));
        if(!database.exists() && !database.isDirectory()){
            if(database.mkdir()){
                return;
            }
            throw new DBException();
        }
        throw new DBExistsException(getDatabaseName());
    }

    private void createTable(DBServer server) throws Exception {

        if(hasDatabase(server)){
            if(server.getUseDatabaseDirectory() != null) {
            String tableName = getTableNames().get(0);
            String dbName = server.getUseDatabaseDirectory().getName().toLowerCase(Locale.ROOT);
            File file = new File(dbName + File.separator + tableName.toLowerCase(Locale.ROOT) + DBFileConstants.TABLE_EXT);

                if (!file.exists()) {
                    try {
                        if (file.createNewFile()) {
                            Table table = new Table();
                            TableHeader header = new TableHeader();
                            header.setTableName(tableName);
                            header.setFileLocation(file);
                            table.setHeader(header);
                            if (getColNames().size() > 0) {
                                addAttributeList(table, getColNames());

                            } else {
                                addAttributeList(table, new ArrayList<>());
                            }
                            DBTableFile dbFile = new DBTableFile();
                            dbFile.storeEntityIntoDBFile(table);
                            dbFile.addTableToMetadata(dbName, tableName);
                            return;
                        }
                    } catch (IOException ioe) {
                        throw new IOException("Unable to write to table:" + tableName);
                    }
                }
                throw new DBTableExistsException(tableName);
            }
             throw new DBException("No database has been selected, (hint: USE <database>)");
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
