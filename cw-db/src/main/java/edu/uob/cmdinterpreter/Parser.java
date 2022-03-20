package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.commands.*;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.exceptions.ParsingException;
import edu.uob.exceptions.ParsingException.*;

import java.util.List;

public class Parser {

    private Tokenizer tokenizer;
    private List<Token> tokens;
    private int currentToken;
    private DBCmd cmd;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokens = tokenizer.getTokens();
        this.currentToken = 0;
    }

    private String getCurrentTokenSeq() throws TokenIndexOutOfBoundsException {
        return getCurrentToken().getSequence();
    }

    private Token getCurrentToken() throws TokenIndexOutOfBoundsException {
        return tokens.get(currentToken);
    }

    private Token getPreviousToken() throws TokenIndexOutOfBoundsException {
        if(currentToken - 1 > 0){
            return tokens.get(currentToken - 1);
        }
        throw new TokenIndexOutOfBoundsException(currentToken-1, tokens.size());
    }

    private String getPreviousTokenSeq() throws TokenIndexOutOfBoundsException {
        if(currentToken - 1 > 0){
            return tokens.get(currentToken - 1).getSequence();
        }
        throw new TokenIndexOutOfBoundsException((currentToken - 1), tokens.size());
    }

    private void incrementToken() throws TokenIndexOutOfBoundsException {
        if(currentToken < tokens.size()-1){
            currentToken++;
            return;
        }
        throw new TokenIndexOutOfBoundsException(currentToken, tokens.size());
    }

    private String lookAheadTokenSeq() throws TokenIndexOutOfBoundsException {
        if(currentToken + 1 < tokens.size()){
            return tokens.get(currentToken + 1).getSequence();
        }
        throw new TokenIndexOutOfBoundsException(currentToken + 1, tokens.size());
    }

    public DBCmd parse() throws ParsingException {
        if(isCommand()) {
            return cmd;
        }

        // TODO throw new parsing exception
        return null;
    }

    /**
     * <Command>  ::=  <CommandType> ";"
     *
     * @return
     */
    private boolean isCommand() throws ParsingException {

        int indexOfExpectedSemiColon = tokens.size() - 1;
        if(BNFConstants.SEMI_COLON.equals((tokens.get(indexOfExpectedSemiColon)).getSequence())) {
            if (isCommandType()) {
                if (currentToken < tokens.size()) {
                    if (BNFConstants.SEMI_COLON.equals(getCurrentTokenSeq())) {
                        return true;
                    }
                }

            }
        }
        throw new InvalidGrammarException("<Command>  ::=  <CommandType> \";\"");
    }

    /**
     * <CommandType>  ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>
     *
     * @return
     */
    private boolean isCommandType() throws ParsingException {
        if(BNFConstants.USE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isUse()) {
                return true;
            }
        }
        if(BNFConstants.CREATE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isCreate()) {
                return true;
            }
        }
        if(BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isDrop()) {
                return true;
            }
        }
        if(BNFConstants.ALTER.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isAlter()){
                return true;
            }
            return false;
        }
        if(BNFConstants.INSERT.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isInsert()){
                return true;
            }
            return false;
        }
        if(BNFConstants.SELECT.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isSelect()){
                return true;
            }
            return false;
        }
        if(BNFConstants.UPDATE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isUpdate()){
                return true;
            }
            return false;
        }
        if(BNFConstants.DELETE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isDelete()){
                return true;
            }
            return false;
        }
        if(BNFConstants.JOIN.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isJoin()){
                return true;
            }
            return false;
        }
        throw new InvalidGrammarException("<CommandType> ::= <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> " +
                "| <Update> | <Delete> | <Join>");
    }

    /**
     * <Use> ::=  "USE " <DatabaseName>
     *
     * @return
     */
    private boolean isUse() throws ParsingException {
        if(BNFConstants.USE.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new UseCMD();
            incrementToken();
            if(isDatabaseName()){
                return true;
            }
        }
        throw new InvalidGrammarException("<Use> ::=  \"USE \" <DatabaseName> ;");
    }

    /**
     * <Create>  ::=  <CreateDatabase> | <CreateTable>
     *
     * @return
     */
    private boolean isCreate() throws ParsingException {
        if (BNFConstants.CREATE.equalsIgnoreCase(getCurrentTokenSeq())) {
            cmd = new CreateCMD();
            incrementToken();
            if (isCreateDatabase()) {
                return true;
            }
            if (isCreateTable()) {
                return true;
            }
        }
        throw new InvalidGrammarException("<Create>  ::=  <CreateDatabase> | <CreateTable> ;");
    }

    /**
     * <CreateDatabase> ::=  "CREATE DATABASE " <DatabaseName>
     *
     * @return
     */
    private boolean isCreateDatabase() throws TokenIndexOutOfBoundsException {
        if (BNFConstants.DATABASE.equalsIgnoreCase(getCurrentTokenSeq())) {
            incrementToken();
            if (isDatabaseName()) {
                return true;
            }
        }

        return false;
    }

    /**
     * <CreateTable>  ::=  "CREATE TABLE " <TableName> | "CREATE TABLE " <TableName> "(" <AttributeList> ")"
     *
     * @return
     */
    private boolean isCreateTable() throws TokenIndexOutOfBoundsException {
        if(BNFConstants.TABLE.equalsIgnoreCase(getCurrentTokenSeq())) {
            incrementToken();
            if (isTableName()) {
//                return true;
//            }
//
//            if (isTableName()) {

                if (BNFConstants.SEMI_COLON.equalsIgnoreCase(getCurrentTokenSeq())) {
                    return true;
                }

                if (BNFConstants.LEFT_BRACKET.equals(getCurrentTokenSeq())) {
                    incrementToken();
                    if (isAttributeList()) {
                        if (BNFConstants.RIGHT_BRACKET.equals(getCurrentTokenSeq())) {
                            incrementToken();
                            return true;
                        }
                    }
                }
            }
        }

    return false;
    }


    /**
     * <Drop>  ::=  "DROP DATABASE " <DatabaseName> | "DROP TABLE " <TableName>
     *
     * @return
     */
    private boolean isDrop() throws ParsingException {
        if(BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new DropCMD();
            incrementToken();
            if(BNFConstants.DATABASE.equalsIgnoreCase(getCurrentTokenSeq())){
                incrementToken();
                if(isDatabaseName()){
                    return true;
                }
            }
            if(BNFConstants.TABLE.equalsIgnoreCase(getCurrentTokenSeq())){
                incrementToken();
                if(isTableName()){
                    return true;
                }
            }
        }
        throw new InvalidGrammarException("<Drop>  ::=  \"DROP DATABASE \" <DatabaseName> | \"DROP TABLE \" <TableName> ;");
    }

    /**
     * <Alter> ::=  "ALTER TABLE " <TableName> " " <AlterationType> " " <AttributeName>
     *
     * @return
     */
    private boolean isAlter() throws ParsingException {
        if(BNFConstants.ALTER.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new AlterCMD();
            incrementToken();
            if(BNFConstants.TABLE.equalsIgnoreCase(getCurrentTokenSeq())){
                incrementToken();
                if(isTableName()){
                    if(isAlterationType()){
                        if(isAttributeName()){
                            return true;
                        }
                    }
                }
            }
        }
        throw new InvalidGrammarException("<Alter> ::=  \"ALTER TABLE \" <TableName> \" \" <AlterationType> \" \" <AttributeName> ;");
    }

    /**
     * <Insert>  ::=  "INSERT INTO " <TableName> " VALUES(" <ValueList> ")"
     *
     * @return
     */
    private boolean isInsert() throws ParsingException {
        if(BNFConstants.INSERT.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new InsertCMD();
            incrementToken();
            if(BNFConstants.INTO.equalsIgnoreCase(getCurrentTokenSeq())){
                incrementToken();
                if(isTableName()){
                    if(BNFConstants.VALUES.equalsIgnoreCase(getCurrentTokenSeq())){
                        incrementToken();
                        if(BNFConstants.LEFT_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
                            incrementToken();
                            if(isValueList()){
                                if(BNFConstants.RIGHT_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
                                    incrementToken();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new InvalidGrammarException("<Insert>  ::=  \"INSERT INTO \" <TableName> \" VALUES(\" <ValueList> \")\" ;");
    }

    /**
     * <Select>  ::=  "SELECT " <WildAttribList> " FROM " <TableName> | "SELECT " <WildAttribList> " FROM " <TableName> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isSelect() throws ParsingException {
        if(BNFConstants.SELECT.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new SelectCMD();
            incrementToken();
            if(isWildAttribList()){
                if(BNFConstants.FROM.equalsIgnoreCase(getCurrentTokenSeq())){
                    incrementToken();
                    if(isTableName()){
                        if(BNFConstants.WHERE.equalsIgnoreCase(getCurrentTokenSeq())){
                            incrementToken();
                            if(isCondition()){
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                }
            }
        }
        throw new InvalidGrammarException("\"SELECT \" <WildAttribList> \" FROM \" <TableName> | \"SELECT \" " +
                "<WildAttribList> \" FROM \" <TableName> \" WHERE \" <Condition> ;");
    }

    /**
     * <Update>  ::=  "UPDATE " <TableName> " SET " <NameValueList> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isUpdate() throws ParsingException {
        if(BNFConstants.UPDATE.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new UpdateCMD();
            incrementToken();
            if(isTableName()){
                if(BNFConstants.SET.equalsIgnoreCase(getCurrentTokenSeq())){
                    incrementToken();
                    if(isNameValueList()){
                        if(BNFConstants.WHERE.equalsIgnoreCase(getCurrentTokenSeq())){
                            incrementToken();
                            if(isCondition()){
                                return true;
                            }
                        }
                    }
                }
            }
        }
        throw new InvalidGrammarException("<Update>  ::=  \" UPDATE \" <TableName> \" SET \" <NameValueList> \" WHERE \" <Condition>");
    }

    /**
     * <Delete>  ::=  "DELETE FROM " <TableName> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isDelete() throws ParsingException {
        if(BNFConstants.DELETE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            cmd = new DeleteCMD();
            if(BNFConstants.FROM.equalsIgnoreCase(getCurrentTokenSeq())){
                incrementToken();
                if(isTableName()){
                    if(BNFConstants.WHERE.equalsIgnoreCase(getCurrentTokenSeq())){
                        incrementToken();
                        if(isCondition()){
                            return true;
                        }
                    }
                }
            }
        }
        throw new InvalidGrammarException("<Delete>  ::=  \"DELETE FROM \" <TableName> \" WHERE \" <Condition>");
    }


    /**
     * <Join>  ::=  "JOIN " <TableName> " AND " <TableName> " ON " <AttributeName> " AND " <AttributeName>
     *
     * @return
     */
    private boolean isJoin() throws ParsingException {
        if(BNFConstants.JOIN.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd = new JoinCMD();
            incrementToken();
            if(isTableName()){
                if(BNFConstants.AND.equalsIgnoreCase(getCurrentTokenSeq())){
                    incrementToken();
                    if(isTableName()){
                        if(BNFConstants.ON.equalsIgnoreCase(getCurrentTokenSeq())){
                            incrementToken();
                            if(isAttributeName()){
                                if(BNFConstants.AND.equalsIgnoreCase(getCurrentTokenSeq())){
                                    incrementToken();
                                    if(isAttributeName()){
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new InvalidGrammarException("<Join>  ::=  \"JOIN \" <TableName> \" AND \" <TableName> \" ON \" <AttributeName> \" AND \" <AttributeName>");
    }


    /**
     * <PlainText>  ::=  <Letter> | <Digit> | <Letter> <PlainText> | <Digit> <PlainText>
     *
     * @return
     */
    private boolean isPlainText() throws TokenIndexOutOfBoundsException {
        char [] charSeq = getCurrentTokenSeq().toCharArray();
        for(int i = 0; i < charSeq.length; i++){
            if(!Character.isLetterOrDigit(charSeq[i])){
                return false;
            }
        }
        incrementToken();
        return true;
    }

    /**
     * <Symbol> ::=  "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/"
     * | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
     *
     * @return
     */
    private boolean isSymbol() throws TokenIndexOutOfBoundsException {
        if(BNFConstants.EXCLAMATION_MARK.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.HASH_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.DOLLAR_SIGN.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.PERCENT_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.AMPERSAND.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.LEFT_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.STAR_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.RIGHT_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.PLUS_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.COMMA.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.DASH_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.PERIOD.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.FORWARD_SLASH.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.COLON.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.SEMI_COLON.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.RIGHT_ANGLE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.EQUALS_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.LEFT_ANGLE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.QUESTION_MARK.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.RIGHT_ANGLE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.AT_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.LEFT_SQR_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.BACK_SLASH.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.RIGHT_SQR_BRACKET.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.HAT_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.UNDERSCORE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.TAG_SYMBOL.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.LEFT_BRACE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.RIGHT_BRACE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.TILDA.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        }
        return false;
    }

    /**
     * <NameValueList>  ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
     *
     * @return
     */
    private boolean isNameValueList() throws TokenIndexOutOfBoundsException {
        if(isNameValuePair()){
            if(BNFConstants.COMMA.equals(getCurrentTokenSeq())){
                incrementToken();
                if(isValueList()){
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * <NameValuePair>  ::=  <AttributeName> "=" <Value>
     *
     * @return
     */
    private boolean isNameValuePair() throws TokenIndexOutOfBoundsException {
        if(isAttributeName()){
            if(BNFConstants.EQUALS_SYMBOL.equals(getCurrentTokenSeq())){
                incrementToken();
                if(isValue()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <AlterationType> ::=  "ADD" | "DROP"s
     *
     * @return
     */
    private boolean isAlterationType() throws TokenIndexOutOfBoundsException {
        if(BNFConstants.ADD.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        } else if (BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        } return false;
    }

    /**
     * <ValueList>  ::=  <Value> | <Value> "," <ValueList>
     *
     * @return
     */
    private boolean isValueList() throws TokenIndexOutOfBoundsException {
        if(isValue()){
            if(BNFConstants.COMMA.equalsIgnoreCase(getCurrentTokenSeq())) {
                incrementToken();
                if(isValueList()) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * <IntegerLiteral> ::=  <DigitSequence> | "-" <DigitSequence> | "+" <DigitSequence>
     *
     * @return
     */
    private boolean isIntegerLiteral() throws TokenIndexOutOfBoundsException {
        try {
            Integer.valueOf(getCurrentTokenSeq());
        } catch (NumberFormatException nfe) {
            return false;
        }
        incrementToken();
        return true;

    }

    /**
     * <FloatLiteral> ::=  <DigitSequence> "." <DigitSequence>
     *     | "-" <DigitSequence> "." <DigitSequence>
     *     | "+" <DigitSequence> "." <DigitSequence>
     *
     * @return
     */
    private boolean isFloatLiteral() throws TokenIndexOutOfBoundsException {
        try {
          Float.valueOf(getCurrentTokenSeq());
        } catch(NumberFormatException nfe){
            return false;
        }
        incrementToken();
        return true;
    }

    /**
     * <BooleanLiteral> ::=  "TRUE" | "FALSE"
     *
     * @return
     */
    private boolean isBooleanLiteral() throws TokenIndexOutOfBoundsException {
        if(BNFConstants.TRUE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        } else if (BNFConstants.FALSE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        } return false;
    }

    /**
     * <CharLiteral>  ::=  <Space> | <Letter> | <Symbol>
     *
     * @return
     */
    private boolean isCharLiteral() throws TokenIndexOutOfBoundsException {
        if(getCurrentTokenSeq().length() == 1) {
            if(Character.isSpaceChar(getCurrentTokenSeq().charAt(0))){
                incrementToken();
                return true;
            }
            if(Character.isLetter(getCurrentTokenSeq().charAt(0))){
                incrementToken();
                return true;
            }
            if(isSymbol()){
                incrementToken();
                return true;
            }
        }
        return false;
    }

    /**
     * <StringLiteral>  ::=  "" | <CharLiteral> | <CharLiteral> <StringLiteral>
     *
     */
    private boolean isStringLiteral() throws TokenIndexOutOfBoundsException {
        if(getCurrentToken().isStringLiteral()){
            incrementToken();
            return true;
        }
        return false;
    }

    /**
     * <Value>  ::=  "'" <StringLiteral> "'" | <BooleanLiteral> | <FloatLiteral> | <IntegerLiteral> | "NULL"
     *
     * @return
     */
    private boolean isValue() throws TokenIndexOutOfBoundsException {
        if(isStringLiteral() || isBooleanLiteral() || isFloatLiteral() || isIntegerLiteral() || isCharLiteral()
                || BNFConstants.NULL.equalsIgnoreCase(getCurrentTokenSeq())){
            cmd.addVariable(getPreviousTokenSeq());
            return true;
        }
        return false;
    }

    /**
     * <TableName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isTableName() throws TokenIndexOutOfBoundsException {
        if(isPlainText()){
            cmd.addTableName(getPreviousTokenSeq());
            return true;
        }
        return false;
    }

    /**
     * <AttributeName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isAttributeName() throws TokenIndexOutOfBoundsException {
        if(isPlainText()){
            cmd.addColumnName(getPreviousTokenSeq());
            return true;
        }
        return false;
    }

    /**
     * <DatabaseName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isDatabaseName() throws TokenIndexOutOfBoundsException {
        if(isPlainText()){
            cmd.setDatabaseName(getPreviousTokenSeq());
            return true;
        }
        return false;
    }

    /**
     * <WildAttribList> ::=  <AttributeList> | "*"
     *
     * @return
     */
    private boolean isWildAttribList() throws TokenIndexOutOfBoundsException {
        if(isAttributeList()){
            return true;
        }
        if(BNFConstants.STAR_SYMBOL.equals(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        return false;
    }

    /**
     * <AttributeList>  ::=  <AttributeName> | <AttributeName> "," <AttributeList>
     *
     * @return
     */
    private boolean isAttributeList() throws TokenIndexOutOfBoundsException {
        if(isAttributeName()) {
            if (BNFConstants.COMMA.equals(getCurrentTokenSeq())) {
                incrementToken();
                if (isAttributeList()) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <Condition>      ::=  "(" <Condition> ")AND(" <Condition> ")" | "(" <Condition> ")OR(" <Condition> ")" | <AttributeName> <Operator> <Value>
     *
     * @return
     */
    private boolean isCondition() throws ParsingException {
        if(isBracketedCondition()){
            if(BNFConstants.AND.equals(getCurrentTokenSeq()) || BNFConstants.OR.equals(getCurrentTokenSeq())){
                incrementToken();
                if(isBracketedCondition()){
                    return true;
                }
            }
        }
        ColumnHeader attribute;
        String operator, value;
        if(isAttributeName()){
            attribute = new ColumnHeader(getPreviousTokenSeq());
            if(isOperator()){
                operator =  getCurrentTokenSeq();
                incrementToken();
                if(isValue()){
                    value = getPreviousTokenSeq();
                    cmd.addCondition(attribute, operator, value);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBracketedCondition() throws ParsingException {
        if(BNFConstants.LEFT_BRACKET.equals(getCurrentTokenSeq())){
            incrementToken();
            if(isCondition()){
                if(BNFConstants.RIGHT_BRACKET.equals(getCurrentTokenSeq())){
                    incrementToken();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <Operator>  ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "
     *
     * @return
     */
    private boolean isOperator() throws InvalidGrammarException, TokenIndexOutOfBoundsException {
//        if(BNFConstants.ASSIGNMENT.equals(getCurrentTokenSeq())){
//            return true; }
        if(BNFConstants.EQUAL_TO.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.GREATER_THAN.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.LESS_THAN.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.GREATER_OR_EQUAL_TO.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.LESS_OR_EQUAL_TO.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.NOT_EQUAL_TO.equals(getCurrentTokenSeq())){
            return true;
        } if(BNFConstants.LIKE.equals(getCurrentTokenSeq())){
            return true;
        }
        throw new InvalidGrammarException("<Operator>  ::=  \"==\" | \">\" | \"<\" | \">=\" | \"<=\" | \"!=\" | \" LIKE \"");
    }

}
