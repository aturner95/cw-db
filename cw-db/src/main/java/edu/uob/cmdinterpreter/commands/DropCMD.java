package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbfilesystem.DBFileConstants;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;

import java.io.File;

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
                    dropDatabase();
                    return STATUS_OK;

                } if (BNFConstants.TABLE.equalsIgnoreCase(commandParameter)) {
                    dropTable(server.getDatabaseDirectory());
                    return STATUS_OK;
                }
                throw new DBException();
            }
            throw new DBDoesNotExistException(getDatabaseName());

        }catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void dropDatabase() {
        File database = new File(getDatabaseName());
        deleteDirectory(database);
    }

    private void dropTable(File db) throws DBTableDoesNotExistException {
        byte indexOfTable = 0;
        File table = new File( db.toString() + File.separator + getTableNames().get(indexOfTable) + DBFileConstants.TABLE_EXT);
        if(table.exists() && table.isFile()){
            table.delete();
            return;
        }
        throw new DBTableDoesNotExistException(table.getName());
    }

    private void deleteDirectory(File dir){
        File [] contents = dir.listFiles();
        for(File dbfile: contents){
            dbfile.delete();
        }
        dir.delete();
    }
}
