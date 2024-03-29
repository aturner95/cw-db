package edu.uob.cmdinterpreter.commands.abstractcmd;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.QueryCondition;
import edu.uob.cmdinterpreter.Token;
import edu.uob.cmdinterpreter.TokenType;
import edu.uob.cmdinterpreter.NameValuePair;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.QueryException.AttributeNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class DBCmd {

    /* Variables */
    protected String commandParameter;
    protected String databaseName;
    protected List<String> tableNames;
    protected List<String> colNames;
    protected List<String> variables;
    protected List<QueryCondition> conditions;
    // NOTE: unable to built tree structure for nested conditions, so decided to provide a hack to a) parse nested b)
    // interpret only single nested conditions (e.g., "(cond AND cond)" and "(cond OR cond)"); these will be added in the
    // order in which they were parsed.
    protected List<String> conditionJoinOperators;
    protected List<NameValuePair> nameValueList;

    public static final String STATUS_OK = "[OK]";
    public static final String STATUS_ERROR = "[ERROR] ";

    /* Constructors */
    public DBCmd(){
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        variables = new ArrayList<>();
        conditions = new ArrayList<>();
        conditionJoinOperators = new ArrayList<>();
        nameValueList = new ArrayList<>();
    }

    public DBCmd(String commandParameter){
        this();
        this.commandParameter = commandParameter;
    }

    /* Getters and setters (or in this case, 'adders') */
    public abstract String query(DBServer server) throws Exception;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName){
        this.databaseName = databaseName.toLowerCase(Locale.ROOT);
    }

    public List<String> getTableNames(){
        return tableNames;
    }

    public void addTableName(String tableName){
        tableNames.add(tableName.toLowerCase(Locale.ROOT));
    }

    public List<String> getColNames(){
        return colNames;
    }

    public void addColumnName(String colName){
        colNames.add(colName);
    }

    public List<String> getVariables(){
        return variables;
    }

    public void addVariable(String variable){
        variables.add(variable);
    }

    public boolean hasDatabase(DBServer server) {
        if(server.getDatabaseDirectory().exists() && server.getDatabaseDirectory().isDirectory()){
                return true;
        }
        return false;
    }

    public boolean hasTable(DBServer server, String tableName) {
         if(server.getUseDatabaseDirectory() != null) {
            File db = new File(server.getUseDatabaseDirectory().getName());
            File[] tables = db.listFiles();
            for (File table : tables) {
                int indexOfFileExt = table.getName().length() - 4;
                if (tableName.equalsIgnoreCase(table.getName().toLowerCase(Locale.ROOT).substring(0, indexOfFileExt))) {
                    return true;
                }
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

    public List<QueryCondition> getConditions(){
        return conditions;
    }

    public void addCondition(ColumnHeader attribute, String operator, Token value){
        conditions.add(new QueryCondition(attribute, operator, value));
    }



    public List<String> getConditionJoinOperators(){
        return conditionJoinOperators;
    }

    public void addConditionJoinOperator(String operator){
        conditionJoinOperators.add(operator);
    }

    public List<NameValuePair> getNameValuePair() {
        return nameValueList;
    }

    public void addNameValuePair(String name, String value){
        nameValueList.add(new NameValuePair(name, value));
    }

    /* Methods */

    public Table readTableFromFile(DBServer server, String tableName) throws IOException, DBException {
        DBTableFile dbFile = new DBTableFile();
        File file = new File(server.getUseDatabaseDirectory() + File.separator + tableName);
        return dbFile.readDBFileIntoEntity(file.getPath() + ".tab");
    }

    public Table buildResultTable(Table queryTable) throws AttributeNotFoundException {

        Table result = new Table();
        result.setColHeadings(new ArrayList<>());
        List<Integer> queryAttributeIndices = new ArrayList<>();

        List<String> queryAttributes;
        if(isSelectStar()){
            queryAttributes = tableAttributesAsStrings(queryTable.getColHeadings());
        }  else {
            queryAttributes = getColNames();
        }

        for(String attribute: queryAttributes){
            addResultHeadings(result, attribute, tableAttributesAsStrings(queryTable.getColHeadings()), queryAttributeIndices);
        }

        for(Record row: queryTable.getRows()){
            addRowToResult(result, row.getAttributes(), queryAttributeIndices);
        }

        return result;
    }

    private boolean isSelectStar(){
        byte sizeOfWildcardSelect = 1;
        byte indexOfWildcardSelect = 0;
        if(getColNames().size() == sizeOfWildcardSelect
                && BNFConstants.STAR_SYMBOL.equals(getColNames().get(indexOfWildcardSelect))){
            return true;
        }
        return false;
    }

    private List<String> tableAttributesAsStrings(List<ColumnHeader> cols){
        List<String> asString = new ArrayList<>();
        for(ColumnHeader col: cols){
            asString.add(col.getColName());
        }
        return asString;
    }


    private void addResultHeadings(Table result, String queryAttribute, List<String> tableAttributes,
                                   List<Integer> indices) throws AttributeNotFoundException {
        int index = tableAttributes.indexOf(queryAttribute);
        if(index >= 0){
            result.getColHeadings().add(new ColumnHeader(tableAttributes.get(index)));
            indices.add(index);
            return;
        }
        throw new AttributeNotFoundException(queryAttribute);
    }

    private void addRowToResult(Table resultSet, List<Attribute> row, List<Integer> queryAttributeIndices){
        List<Attribute> newRow = new ArrayList<>();
        for(Integer i : queryAttributeIndices) {
            newRow.add(new Attribute(row.get(i).getValue()));
        }
        resultSet.getRows().add(new Record(newRow));
    }


    public Table doConditions(Table table, Table result) throws DBException {

        if(getConditions().size() == 1){
            result = filterResultByCondition(table, getConditions().get(0));
        }
        else if(getConditions().size() == 2){
            if(BNFConstants.AND.equalsIgnoreCase(getConditionJoinOperators().get(0))){
                result = filterResultByAndConditions(table, getConditions().get(0), getConditions().get(1));

            } else { // else OR
                result = filterResultByOrConditions(table, getConditions().get(0), getConditions().get(1));
            }
        } else {
            throw new DBException("Sorry, haven't managed to implement nested conditions!");
        }
        return result;
    }


    public Table filterResultByCondition(Table result, QueryCondition condition){

        Table filteredResult = new Table();
        filteredResult.setColHeadings(result.getColHeadings());

        int attrIndex = getAttributeIndex(result.getColHeadings(), condition);

        for(Record row: result.getRows()){
            if(isCondition(row.getAttributes().get(attrIndex).getValue(), condition.getOperator(), condition.getValue())){
                filteredResult.getRows().add(row);
            }
        }

        return filteredResult;
    }

    public Table filterResultByAndConditions(Table result, QueryCondition conditionA, QueryCondition conditionB){

        Table filteredResult = new Table();
        filteredResult.setColHeadings(result.getColHeadings());

        int attrIndexA = getAttributeIndex(result.getColHeadings(), conditionA);
        int attrIndexB = getAttributeIndex(result.getColHeadings(), conditionB);

        for(Record row: result.getRows()){
            if(isCondition(row.getAttributes().get(attrIndexA).getValue(), conditionA.getOperator(), conditionA.getValue())
            && isCondition(row.getAttributes().get(attrIndexB).getValue(), conditionB.getOperator(), conditionB.getValue())){
                filteredResult.getRows().add(row);
            }
        }

        return filteredResult;
    }

    public Table filterResultByOrConditions(Table result, QueryCondition conditionA, QueryCondition conditionB){

        Table filteredResult = new Table();
        filteredResult.setColHeadings(result.getColHeadings());

        int attrIndexA = getAttributeIndex(result.getColHeadings(), conditionA);
        int attrIndexB = getAttributeIndex(result.getColHeadings(), conditionB);

        for(Record row: result.getRows()){
            if(isCondition(row.getAttributes().get(attrIndexA).getValue(), conditionA.getOperator(), conditionA.getValue())
                    || isCondition(row.getAttributes().get(attrIndexB).getValue(), conditionB.getOperator(), conditionB.getValue())){
                filteredResult.getRows().add(row);
            }
        }

        return filteredResult;
    }

    private int getAttributeIndex(List<ColumnHeader> colHeader,QueryCondition condition){
        String queryAttribute = condition.getAttribute();
        int attrIndex = 0;
        for(ColumnHeader col: colHeader){
            if(col.getColName().equals(queryAttribute)){
                return attrIndex;
            }
            attrIndex++;
        }
        return attrIndex;
    }

    private boolean isCondition(String value, String operator, Token condition){
        if(BNFConstants.EQUAL_TO.equals(operator)){
            return conditionEqualTo(value,condition);
        }
        if(BNFConstants.GREATER_THAN.equals(operator)){
            return conditionGreaterThan(value,condition);
        }
        if(BNFConstants.LESS_THAN.equals(operator)){
            return conditionLessThan(value,condition);
        }
        if(BNFConstants.GREATER_OR_EQUAL_TO.equals(operator)){
            return conditionGreaterThanOrEqualTo(value,condition);
        }
        if(BNFConstants.LESS_OR_EQUAL_TO.equals(operator)){
            return conditionLessThanOrEqualTo(value,condition);
        }
        if(BNFConstants.NOT_EQUAL_TO.equals(operator)){
            return conditionNotEqualTo(value,condition);
        }
        if(BNFConstants.LIKE.equals(operator)){
            return conditionLike(value,condition);
        }
        return false;
    }

    private boolean conditionEqualTo(String value, Token condition){
        if(condition.getTokenType() == TokenType.LIT_NUM){
            if(Float.valueOf(condition.getSequence()).equals(Float.valueOf(value))){
                return true;
            }
        }
        if(condition.getTokenType() == TokenType.LIT_STR){
            if(condition.getSequence().equals(value)){
                return true;
            }
        }
        if(condition.getTokenType() == TokenType.LIT_CHAR){
            if(Character.valueOf(condition.getSequence().charAt(0)).equals(value)){
                return true;
            }
        }
        if(condition.getTokenType() == TokenType.LIT_BOOL){
            if(Boolean.valueOf(condition.getSequence()).equals((Boolean.valueOf(value)))){
                return true;
            }
        }
        return false;
    }

    private boolean conditionGreaterThan(String value, Token condition){
        if(condition.getTokenType() == TokenType.LIT_NUM){
            if(Float.valueOf(value) > Float.valueOf(condition.getSequence())){
                return true;
            }
        }

        return false;
    }

    private boolean conditionLessThan(String value, Token condition){
        if(condition.getTokenType() == TokenType.LIT_NUM){
            if(Float.valueOf(value) < Float.valueOf(condition.getSequence())){
                return true;
            }
        }

        return false;
    }

    private boolean conditionGreaterThanOrEqualTo(String value, Token condition){
        if(conditionGreaterThan(value, condition) || conditionEqualTo(value, condition)){
            return true;
        }
        return false;
    }

    private boolean conditionLessThanOrEqualTo(String value, Token condition){
        if(conditionLessThan(value, condition) || conditionEqualTo(value, condition)){
            return true;
        }
        return false;
    }

    private boolean conditionNotEqualTo(String value, Token condition){
        if(!conditionEqualTo(value, condition)){
            return true;
        }
        return false;
    }

    private boolean conditionLike(String value, Token condition){
        if(condition.getTokenType() == TokenType.LIT_STR){
            if(value.contains(condition.getSequence())){
                return true;
            }
        }
        return false;
    }

}
