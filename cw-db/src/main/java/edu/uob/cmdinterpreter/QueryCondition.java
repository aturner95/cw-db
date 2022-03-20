package edu.uob.cmdinterpreter;

import edu.uob.dbelements.ColumnHeader;

public class QueryCondition {

    ColumnHeader attribute;
    String operator;
    String value;

    public QueryCondition() {
        super();
    }

    public QueryCondition(ColumnHeader attribute, String operator, String value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

}

