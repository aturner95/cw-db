package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.commands.*;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;

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

    private String getCurrentTokenSeq() {
        return tokens.get(currentToken).getSequence();
    }

    private Token getCurrentToken() {
        return tokens.get(currentToken);
    }

    private void incrementToken() {
        // TODO throw exception
        if(currentToken < tokens.size()){
            currentToken++;
        }

    }

    private String lookAheadTokenSeq() {
        return tokens.get(currentToken + 1).getSequence();

    }

    public DBCmd parse() {
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
    private boolean isCommand() {
        if (isCommandType()) {
            if (BNFConstants.SEMI_COLON.equals(getCurrentTokenSeq())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <CommandType>  ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>
     *
     * @return
     */
    private boolean isCommandType(){
        if(BNFConstants.USE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isUse()) {
                cmd = new UseCMD();
                return true;
            }
        }
        if(BNFConstants.CREATE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isCreate()) {
                cmd = new CreateCMD();
                return true;
            }
        }
        if(BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isDrop()) {
                cmd = new DropCMD();
                return true;
            }
        }
        if(BNFConstants.ALTER.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isAlter()){
                cmd = new AlterCMD();
                return true;
            }
            return false;
        }
        if(BNFConstants.INSERT.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isInsert()){
                cmd = new InsertCMD();
                return true;
            }
            return false;
        }
        if(BNFConstants.SELECT.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isSelect()){
                cmd = new SelectCMD();
                return true;
            }
            return false;
        }
        if(BNFConstants.UPDATE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isUpdate()){
                cmd = new UpdateCMD();
                return true;
            }
            return false;
        }
        if(BNFConstants.DELETE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isDelete()){
                cmd = new DeleteCMD();
                return true;
            }
            return false;
        }
        if(BNFConstants.JOIN.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isJoin()){
                cmd = new JoinCMD();
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * <Use> ::=  "USE " <DatabaseName>
     *
     * @return
     */
    private boolean isUse(){
        if(BNFConstants.USE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            if(isDatabaseName()){
                return true;
            }
        }
        return false;
    }

    /**
     * <Create>  ::=  <CreateDatabase> | <CreateTable>
     *
     * @return
     */
    private boolean isCreate(){
        if (BNFConstants.CREATE.equalsIgnoreCase(getCurrentTokenSeq())) {
            incrementToken();
            if (isCreateDatabase()) {
                return true;
            }
            if (isCreateTable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * <CreateDatabase> ::=  "CREATE DATABASE " <DatabaseName>
     *
     * @return
     */
    private boolean isCreateDatabase(){
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
    private boolean isCreateTable(){
        if(BNFConstants.TABLE.equalsIgnoreCase(getCurrentTokenSeq())) {
            incrementToken();
            if (isTableName()) {
                return true;
            }

            if (isTableName()) {
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
    private boolean isDrop(){
        if(BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
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
        return false;
    }

    /**
     * <Alter> ::=  "ALTER TABLE " <TableName> " " <AlterationType> " " <AttributeName>
     *
     * @return
     */
    private boolean isAlter(){
        if(BNFConstants.ALTER.equalsIgnoreCase(getCurrentTokenSeq())){
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
        return false;
    }

    /**
     * <Insert>  ::=  "INSERT INTO " <TableName> " VALUES(" <ValueList> ")"
     *
     * @return
     */
    private boolean isInsert() {
        if(BNFConstants.INSERT.equalsIgnoreCase(getCurrentTokenSeq())){
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
        return false;
    }

    /**
     * <Select>  ::=  "SELECT " <WildAttribList> " FROM " <TableName> | "SELECT " <WildAttribList> " FROM " <TableName> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isSelect(){
        if(BNFConstants.SELECT.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            if(isWildAttribList()){
                if(BNFConstants.FROM.equalsIgnoreCase(getCurrentTokenSeq())){
                    incrementToken();
                    if(isTableName()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * <Update>  ::=  "UPDATE " <TableName> " SET " <NameValueList> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isUpdate(){
        if(BNFConstants.UPDATE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            if(isTableName()){
                if(BNFConstants.SET.equalsIgnoreCase(getCurrentTokenSeq())){
                    incrementToken();
                    if(isNameValueList()){
                        if(BNFConstants.WHERE.equalsIgnoreCase(getCurrentTokenSeq())){
                            // TODO condition
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * <Delete>  ::=  "DELETE FROM " <TableName> " WHERE " <Condition>
     *
     * @return
     */
    private boolean isDelete(){
        if(BNFConstants.DELETE.equalsIgnoreCase(getCurrentTokenSeq())){
            if(BNFConstants.FROM.equalsIgnoreCase(getCurrentTokenSeq())){
                if(isTableName()){
                    // TODO where condition
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * <Join>  ::=  "JOIN " <TableName> " AND " <TableName> " ON " <AttributeName> " AND " <AttributeName>
     *
     * @return
     */
    private boolean isJoin(){
        if(BNFConstants.JOIN.equalsIgnoreCase(getCurrentTokenSeq())){
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
        return false;
    }

//    /**
//     * <Digit>  ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
//     *
//     * @return
//     */
//    private boolean isDigit() {
//        if (getCurrentToken().getSequence().length() == 1) {
//            if (Character.isDigit(getCurrentToken().getSequence().charAt(0))) {
//                return true;
//            }
//        }
//        return false;
//    }

    // isUppercase
    // isLowercase


//    /**
//     * <Letter>  ::=  <Uppercase> | <Lowercase>
//     *
//     * @return
//     */
//    private boolean isLetter(){
//        if(getCurrentToken().getSequence().length() == 1) {
//            if(Character.isLetter(getCurrentToken().getSequence().charAt(0))){
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * <PlainText>  ::=  <Letter> | <Digit> | <Letter> <PlainText> | <Digit> <PlainText>
     *
     * @return
     */
    private boolean isPlainText(){
        char [] charSeq = getCurrentToken().getSequence().toCharArray();
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
    private boolean isSymbol(){
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

//    /**
//     * <Space> ::=  " "
//     *
//     * @return
//     */
//    private boolean isSpace(){
//        if(getCurrentToken().getSequence().length() == 1) {
//            if(Character.isSpaceChar(getCurrentToken().getSequence().charAt(0))){
//                incrementToken();
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * <NameValueList>  ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
     *
     * @return
     */
    private boolean isNameValueList(){
        if(isNameValuePair()){
            if(BNFConstants.COMMA.equals(getCurrentToken())){
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
    private boolean isNameValuePair(){
        if(isAttributeName()){
            if(BNFConstants.EQUALS_SYMBOL.equals(getCurrentToken())){
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
    private boolean isAlterationType(){
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
    private boolean isValueList(){
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

    // isDigitSequence

    /**
     * <IntegerLiteral> ::=  <DigitSequence> | "-" <DigitSequence> | "+" <DigitSequence>
     *
     * @return
     */
    private boolean isIntegerLiteral() {
        try {
            Integer.valueOf(getCurrentToken().getSequence());
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
    private boolean isFloatLiteral(){
        try {
          Float.valueOf(getCurrentToken().getSequence());
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
    private boolean isBooleanLiteral(){
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
    private boolean isCharLiteral() {
        if(getCurrentToken().getSequence().length() == 1) {
            if(Character.isSpaceChar(getCurrentToken().getSequence().charAt(0))){
                incrementToken();
                return true;
            }
            if(Character.isLetter(getCurrentToken().getSequence().charAt(0))){
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
    private boolean isStringLiteral() {
        String openingQuotation = String.valueOf(getCurrentToken().getSequence().charAt(0));
        String closingQuotation = String.valueOf(getCurrentToken().getSequence().charAt(getCurrentToken().getSequence().length() - 1));
        if(BNFConstants.SINGLE_QUOTATION.equals(openingQuotation) && BNFConstants.SINGLE_QUOTATION.equals(closingQuotation)){
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
    private boolean isValue(){
        if(isStringLiteral()){
            return true;
        }
        if(isBooleanLiteral()){
            return true;
        }
        if(isFloatLiteral()){
            return true;
        }
        if(isIntegerLiteral()){
            return true;
        }
        if(isCharLiteral()){
            return true;
        }
        if(BNFConstants.NULL.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        return false;
    }

    /**
     * <TableName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isTableName(){
        if(isPlainText()){
            return true;
        }
        return false;
    }

    /**
     * <AttributeName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isAttributeName(){
        if(isPlainText()){
            return true;
        }
        return false;
    }

    /**
     * <DatabaseName>  ::=  <PlainText>
     *
     * @return
     */
    private boolean isDatabaseName(){
        if(isPlainText()){
            return true;
        }
        return false;
    }

    /**
     * <WildAttribList> ::=  <AttributeList> | "*"
     *
     * @return
     */
    private boolean isWildAttribList(){
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
    private boolean isAttributeList(){
        if(isAttributeName()){
            return true;
        }
        if(isAttributeName()){
            if(BNFConstants.COMMA.equals(getCurrentTokenSeq())){
                incrementToken();
                if(isAttributeList()){
                    return true;
                }
            }
        }
        return false;
    }

    // isCondition

    /**
     * <Operator>  ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "
     *
     * @return
     */
    private boolean isOperator(){
        if(BNFConstants.ASSIGNMENT.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.EQUAL_TO.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.GREATER_THAN.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.LESS_THAN.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.GREATER_OR_EQUAL_TO.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.LESS_OR_EQUAL_TO.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.NOT_EQUAL_TO.equals(getCurrentToken())){
            return true;
        } if(BNFConstants.LIKE.equals(getCurrentToken())){
            return true;
        }
        return false;
    }

}
