package edu.uob.cmdinterpreter;

import edu.uob.dbelements.ColumnHeader;

public class QueryCondition {

    private ColumnHeader attribute;
    private String operator;
    private String value;

    public QueryCondition() {
        super();
    }

    public QueryCondition(ColumnHeader attribute, String operator, String value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public String getAttribute() {
        return attribute.getColName();
    }

    public String getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

}

