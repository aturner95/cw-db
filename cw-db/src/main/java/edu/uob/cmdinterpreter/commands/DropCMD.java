package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbfilesystem.DBFileConstants;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;
import java.io.FileNotFoundException;

public class DropCMD extends DBCmd {

    public DropCMD(String dropType){
        super(dropType);
    }

    public String getDropType(){
        return commandParameter;
    }

    @Override
    public String query(DBServer server) {

        try {
            if(hasDatabase(server)) {
                if (BNFConstants.DATABASE.equalsIgnoreCase(commandParameter)) {
                    dropDatabase(server);
                    return STATUS_OK;

                } if (BNFConstants.TABLE.equalsIgnoreCase(commandParameter)) {
                    dropTable(server);
                    return STATUS_OK;
                }
                throw new DBException();
            }
            throw new DBDoesNotExistException(getDatabaseName());

        }catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void dropDatabase(DBServer server) throws FileNotFoundException {
        File database = new File(getDatabaseName());
        if(server.getUseDatabaseDirectory()  != null) {
            if (server.getUseDatabaseDirectory().getName().equalsIgnoreCase(getDatabaseName())) {
                server.setUseDatabaseDirectory(null);
            }
        }
        File [] contents = database.listFiles();
        if(contents != null) {
            for (File dbfile : contents) {
                removeTableMetadata(database, dbfile, dbfile.getName().substring(0, dbfile.getName().length() - 4));
                dbfile.delete();
            }
        }
        deleteDirectory(database);
    }

    private void dropTable(DBServer server) throws Exception {
        File db = server.getUseDatabaseDirectory();
        byte indexOfTable = 0;
        File table = new File( db.toString() + File.separator + getTableNames().get(indexOfTable) + DBFileConstants.TABLE_EXT);
        if(table.exists() && table.isFile()){
            removeTableMetadata(db, table, getTableNames().get(indexOfTable));
            return;
        }
        throw new DBTableDoesNotExistException(table.getName());
    }

    private void deleteDirectory(File dir) {
        File[] contents = dir.listFiles();
        for (File dbfile : contents) {
            dbfile.delete();
        }
        dir.delete();
    }

    private void removeTableMetadata(File db, File table, String tablename) throws FileNotFoundException {
        if(table.exists() && table.isFile()){
            table.delete();
            DBTableFile dbFile = new DBTableFile();
            dbFile.removeTableFromMetadata(db.getName(), tablename);
            return;
        }
    }
}
