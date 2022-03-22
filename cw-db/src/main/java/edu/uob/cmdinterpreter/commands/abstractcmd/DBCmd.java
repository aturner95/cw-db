package edu.uob.cmdinterpreter.commands.abstractcmd;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.BNFConstants;
import edu.uob.cmdinterpreter.QueryCondition;
import edu.uob.cmdinterpreter.Token;
import edu.uob.cmdinterpreter.TokenType;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import edu.uob.exceptions.DBException;
import edu.uob.exceptions.QueryException.AttributeNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.uob.dbfilesystem.DBFileConstants.ROOT_DB_DIR;

public abstract class DBCmd {

    /* Variables */
    protected String commandParameter;
    protected String databaseName;
    protected List<String> tableNames;
    protected List<String> colNames;
    protected List<String> variables;
    protected List<QueryCondition> conditions;
    // NOTE: unable to built tree structure for nested conditions, so decided to provide a hack to a) parse nested b)
    // interpret only single nested conditions (e.g., (cond AND and) and (cond OR cond); these will be added in the
    // order in which they were parsed.
    protected List<String> conditionJoinOperators;

    public static final String STATUS_OK = "[OK]";
    public static final String STATUS_ERROR = "[ERROR] ";
    public static final String SPACE = " ";

    /* Constructors */
    public DBCmd(){
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        variables = new ArrayList<>();
        conditions = new ArrayList<>();
        conditionJoinOperators = new ArrayList<>();
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
        this.databaseName = ROOT_DB_DIR + File.separator + databaseName;
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
            if(!"./databases".equalsIgnoreCase(server.getDatabaseDirectory().getName())){
                return true;
            }
        }
        return false;
    }

    public boolean hasTable(DBServer server, String tableName) throws DBException {
        if(!usingRootDatabase(server)) {
            File db = new File(ROOT_DB_DIR + File.separator + server.getDatabaseDirectory().getName());
            File[] tables = db.listFiles();
            for (File table : tables) {
                int indexOfFileExt = table.getName().length() - 4;
                if (tableName.equalsIgnoreCase(table.getName().substring(0, indexOfFileExt))) {
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

    public boolean addCondition(ColumnHeader attribute, String operator, Token value){
        if(conditions != null){
            conditions.add(new QueryCondition(attribute, operator, value));
            return true;
        }
        return false;
    }



    public List<String> getConditionJoinOperators(){
        return conditionJoinOperators;
    }

    public void addConditionJoinOperator(String operator){
        getConditionJoinOperators().add(operator);
    }

    /* Methods */

    public Table readTableFromFile(DBServer server, String tableName) throws IOException, DBException {
        DBTableFile dbFile = new DBTableFile();
        File file = new File(server.getDatabaseDirectory() + File.separator + tableName);
        Table table;
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
            addValueToResult(result, row.getAttributes(), queryAttributeIndices);
        }

        return result;
    }

    private boolean isSelectStar(){
        if(getColNames().size() == 1 && BNFConstants.STAR_SYMBOL.equals(getColNames().get(0))){
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

    private void addValueToResult(Table resultSet, List<Attribute> row, List<Integer> queryAttributeIndices){
        int index = 0;
        List<Attribute> resultValues = new ArrayList<>();
        for(Attribute value: row){
            if(queryAttributeIndices.contains(Integer.valueOf(index))){
                resultValues.add(new Attribute(value.getValue()));
            }
            index++;
        }
        resultSet.getRows().add(new Record(resultValues));
    }

    public boolean usingRootDatabase(DBServer server) throws DBException {
        String rootDb = ROOT_DB_DIR;
        if(rootDb.equals(server.getDatabaseDirectory().getName())){
            throw new DBException("No database has been selected, (hint: USE <database>)");
        }
        return false;
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
        // TODO implement LIKE condition
//        if(BNFConstants.LIKE.equals(operator)){
//
//        }
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
        if(condition.getTokenType() == TokenType.LIT_CHAR){
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
// TODO implement LIKE condition
//    private boolean conditionLike(String value, Token condition){
//
//    }

}
