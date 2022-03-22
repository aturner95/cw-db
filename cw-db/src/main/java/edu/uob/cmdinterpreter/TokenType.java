package edu.uob.cmdinterpreter;

public enum TokenType {
    CT, // Command type: <list of words>
    ID, // Identifier: table name, column name
    KW, // Keyword: <list of words>
    // LIT, // Literal: integer literal, String literal, character literal, float literal, boolean literal
    LIT_NUM,
    LIT_STR,
    LIT_CHAR,
    LIT_BOOL,
    OP, // Operation
    SEP, // Separator
}
