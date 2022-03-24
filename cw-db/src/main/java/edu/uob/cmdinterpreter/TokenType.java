package edu.uob.cmdinterpreter;

public enum TokenType {
    CT, // Command type: <list of words>
    ID, // Identifier: table name, column name
    KW, // Keyword: <list of words>
    LIT_NUM, // Literal number (integer and float)
    LIT_STR, // Literal String
    LIT_CHAR, // Literal Character
    LIT_BOOL, // Literal Boolean
    OP, // Operation
    SEP, // Separator
}
