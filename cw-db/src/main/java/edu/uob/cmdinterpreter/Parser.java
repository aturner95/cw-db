package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.abstractcmd.DBCmd;

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
        currentToken++;
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
            return true;
        }
        if(BNFConstants.CREATE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.ALTER.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.INSERT.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.SELECT.equalsIgnoreCase(getCurrentTokenSeq())){
            if(isSelect()){
                return true;
            }
            return false;
        }
        if(BNFConstants.UPDATE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.DELETE.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        if(BNFConstants.JOIN.equalsIgnoreCase(getCurrentTokenSeq())){
            incrementToken();
            return true;
        }
        return false;
    }

    // isUse
    // isCreate
    // isCreateDatabase
    // isCreateTable
    // isDrop
    // isAlter
    // isInsert

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
                        cmd = new SelectCMD();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // isUpdate
    // isDelete
    // isJoin

    /**
     * <Digit>  ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     *
     * @return
     */
    private boolean isDigit() {
        if (getCurrentToken().getSequence().length() == 1) {
            if (Character.isDigit(getCurrentToken().getSequence().charAt(0))) {
                return true;
            }
        }
        return false;
    }

    // isUppercase
    // isLowercase


    /**
     * <Letter>  ::=  <Uppercase> | <Lowercase>
     *
     * @return
     */
    private boolean isLetter(){
        if(getCurrentToken().getSequence().length() == 1) {
            if(Character.isLetter(getCurrentToken().getSequence().charAt(0))){
                return true;
            }
        }
        return false;
    }

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

    /**
     * <Space> ::=  " "
     *
     * @return
     */
    private boolean isSpace(){
        if(getCurrentToken().getSequence().length() == 1) {
            if(Character.isSpaceChar(getCurrentToken().getSequence().charAt(0))){
                return true;
            }
        }
        return false;
    }
    // isNameValueList
    // isNameValuePair

    /**
     * <AlterationType> ::=  "ADD" | "DROP"s
     *
     * @return
     */
    private boolean isAlterationType(){
        if(BNFConstants.ADD.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.DROP.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } return false;
    }

    // isValueList
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
        return true;
    }

    /**
     * <BooleanLiteral> ::=  "TRUE" | "FALSE"
     *
     * @return
     */
    private boolean isBooleanLiteral(){
        if(BNFConstants.TRUE.equalsIgnoreCase(getCurrentTokenSeq())){
            return true;
        } else if (BNFConstants.FALSE.equalsIgnoreCase(getCurrentTokenSeq())){
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
                return true;
            }
            if(Character.isLetter(getCurrentToken().getSequence().charAt(0))){
                return true;
            }
            if(isSymbol()){
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
            return true;
        }
        return false;
    }

    // isValue

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
