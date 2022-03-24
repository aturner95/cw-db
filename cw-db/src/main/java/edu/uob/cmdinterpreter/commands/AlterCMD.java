package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.Token;
import edu.uob.cmdinterpreter.TokenType;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Table;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBFileConstants;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.DBException.*;
import edu.uob.exceptions.ParsingException;
import edu.uob.exceptions.ParsingException.InvalidGrammarException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AlterCMD extends DBCmd {

    public AlterCMD(){
        super();
    }

    public AlterCMD(String alterationType){
        super(alterationType);
    }

    public String getAlterationType() {
        return commandParameter;
    }

    public void setAlterationType(String alterationType){
        this.commandParameter = alterationType;
    }

    @Override
    public String query(DBServer server) {
        byte indexOfTableName = 0, indexOfAttributeName = 0;
        String tableName = getTableNames().get(indexOfTableName);
        String attributeName = getColNames().get(indexOfAttributeName);

        try {
            if (hasDatabase(server)) {
                if (hasTable(server, tableName)) {
                    DBTableFile dbFile = new DBTableFile();
                    File file = new File(server.getUseDatabaseDirectory() + File.separator + tableName);
                    Table table;
                    String filepath = file.getPath() + DBFileConstants.TABLE_EXT;

                    try {
                        table = readTableFromFile(server, tableName);
                        alterTable(table, attributeName, commandParameter);
                        dbFile.storeEntityIntoDBFile(table);
                        return STATUS_OK;

                    } catch (IOException ioe) {
                        throw new IOException("Unable to read file: " + filepath);
                    }
                }
                throw new DBTableDoesNotExistException(tableName);
            }
            throw new DBDoesNotExistException(server.getDatabaseDirectory().getName());

        }catch(Exception e){
            return STATUS_ERROR + e.getMessage();
        }
    }

    private void addAttribute(Table table, String attributeName){
        if(table.getColHeadings().size() == 0){
            table.getColHeadings().add(new ColumnHeader("id"));
        }
        table.getColHeadings().add(new ColumnHeader(attributeName));
        populateNewData(table.getRows());
    }

    private void removeAttribute(Table table, String attributeName) throws DBAttributeDoesNotExistException{
        List<ColumnHeader> headers = table.getColHeadings();
        int indexOf = getAttributeIndex(table.getColHeadings(), attributeName);
        if(indexOf < 0){
            throw new DBAttributeDoesNotExistException(attributeName);
        }
        headers.remove(indexOf);
        List<Record> rows = table.getRows();
        deleteExistingData(rows, indexOf);
    }

    private int getAttributeIndex(List<ColumnHeader> columnHeaders, String attribute){
        int counter = 0;
        for(ColumnHeader header: columnHeaders){
            if(attribute.equalsIgnoreCase(header.getColName())){
                return counter;
            }
            counter++;
        }
        return -1;
    }

    private void populateNewData(List<Record> data){
        for(Record row: data){
            row.addDefaultAttribute();
        }
    }

    private void deleteExistingData(List<Record> data, int index) {
        for (Record row : data) {
            row.getAttributes().remove(index);
        }
    }

    private void alterTable(Table table, String attributeName, String alterationType) throws DBException, ParsingException {
        if (BNFConstants.ADD.equalsIgnoreCase(alterationType)) {
            if (!hasAttribute(table, attributeName)) {
                addAttribute(table, attributeName);
                return;
            } else {
                throw new DBAttributeExistsException(attributeName);
            }
        } else if (BNFConstants.DROP.equalsIgnoreCase(alterationType)) {
            if (hasAttribute(table, attributeName)) {
                removeAttribute(table, attributeName);
                return;
            } else {
                throw new DBAttributeDoesNotExistException(attributeName);
            }
        }
        throw new InvalidGrammarException(new Token(TokenType.KW, getAlterationType()), "<Alter> ::=  \"ALTER TABLE \" "
                + "<TableName> \" \" <AlterationType> \" \" <AttributeName> ;");
    }

}
