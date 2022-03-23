package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.*;
import edu.uob.dbelements.Record;
import edu.uob.exceptions.DBException.*;
import edu.uob.exceptions.QueryException;

import java.util.ArrayList;

public class JoinCMD extends DBCmd {

    @Override
    public String query(DBServer server) {

        byte indexOfTableA = 0, indexOfTableB = 1;

        try {
            if (hasDatabase(server)) {
                String tableNameA = getTableNames().get(indexOfTableA);
                String tableNameB = getTableNames().get(indexOfTableB);

                if (hasTable(server, tableNameA) && hasTable(server, tableNameB)) {
                    String attributeNameA = getColNames().get(0);
                    String attributeNameB = getColNames().get(1);

                    Table tableA = readTableFromFile(server, tableNameA);
                    Table tableB = readTableFromFile(server, tableNameB);

                    if (hasAttribute(tableA, attributeNameA) && hasAttribute(tableB, attributeNameB)) {

                        int indexOfAttrA = getAttributeIndex(tableA, new Attribute(attributeNameA));
                        int indexOfAttrB = getAttributeIndex(tableB, new Attribute(attributeNameB));

                        Table result = new Table();
                        addColumnHeading(result, tableA, tableB, indexOfAttrA, indexOfAttrB);
                        doJoin(result, tableA, tableB, indexOfAttrA, indexOfAttrB);
                        result.setHeader(new TableHeader());

                        return STATUS_OK + System.lineSeparator() + result.toString();
                    }
                    String errorMsg = attributeNameA + " or " + attributeNameB;
                    throw new DBAttributeDoesNotExistException(errorMsg);
                }
                String errorMsg = tableNameA + " or " + tableNameB;
                throw new DBTableDoesNotExistException(errorMsg);
            }
            throw new DBDoesNotExistException(getDatabaseName());

        } catch (Exception e) {
            return STATUS_ERROR + e.getMessage();
        }
    }


    private void doJoin(Table result, Table tableA, Table tableB, int indexOfAttrA, int indexOfAttrB) throws QueryException.AttributeNotFoundException{

        result.setRows(new ArrayList<>());

        int newRowId = 1;

        for(Record rowA : tableA.getRows()){

            for(Record rowB : tableB.getRows()){

                if(rowA.getAttributes().get(indexOfAttrA).equals(rowB.getAttributes().get(indexOfAttrB))){

                    Record newRow = new Record(new ArrayList<>());

                    newRow.getAttributes().add(new Attribute(String.valueOf(newRowId++)));

                    addAttributesToJoin(newRow, rowA, indexOfAttrA);

                    addAttributesToJoin(newRow, rowB, indexOfAttrB);

                    result.getRows().add(newRow);
                }
            }
        }
    }

    private int getAttributeIndex(Table table, Attribute attribute) throws QueryException.AttributeNotFoundException {
        if(table.getColHeadings().contains(new ColumnHeader(attribute.getValue()))){
            return table.getColHeadings().indexOf(new ColumnHeader(attribute.getValue()));
        }
        throw new QueryException.AttributeNotFoundException(attribute.getValue());
    }

    private void addAttributesToJoin(Record targetRow, Record sourceRow, int indexOfAttribute){
        for(Attribute attribute : sourceRow.getAttributes()){
            if(!attribute.equals(sourceRow.getAttributes().get(indexOfAttribute)) && !attribute.equals(sourceRow.getAttributes().get(0))) {
                targetRow.getAttributes().add(attribute);
            }
        }
    }

    private void addColumnHeading(Table result, Table tableA, Table tableB, int indexOfAttributeA, int indexOfAttributeB){
        result.setColHeadings(new ArrayList<>());
        result.getColHeadings().add(new ColumnHeader("id"));

        for(int a = 0; a < tableA.getColHeadings().size(); a++){
            if(a != 0 && a != indexOfAttributeA){
                result.getColHeadings().add(tableA.getColHeadings().get(a));
            }
        }
        for(int b = 0; b < tableB.getColHeadings().size(); b++){
            if(b != 0 && b != indexOfAttributeB){
                result.getColHeadings().add(tableB.getColHeadings().get(b));
            }
        }
    }

}
