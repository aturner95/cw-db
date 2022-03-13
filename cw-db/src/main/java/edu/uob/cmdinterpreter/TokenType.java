package edu.uob.cmdinterpreter;

public enum TokenType {
    CT, // Command type: <list of words>
    ID, // Identifier: table name, column name
    KW, // Keyword: <list of words>
    LIT, // Literal: integer literal, String literal, character literal, float literal, boolean literal
    OP, // Operation
    SEP, // Separator
}
