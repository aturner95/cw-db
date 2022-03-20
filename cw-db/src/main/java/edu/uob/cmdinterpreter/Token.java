package edu.uob.cmdinterpreter;

public class Token {
    private final TokenType tokenType;
    private final String sequence;

    public Token(TokenType tokenType, String sequence) {
        this.tokenType = tokenType;
        this.sequence = sequence;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getSequence() {
        if(isStringLiteral()){
            return sequence.substring(1, sequence.length()-1);
        }
        return sequence;
    }

    @Override
    public String toString() {
        return "[" + tokenType.toString() + "] : " + sequence;
    }

    public boolean isStringLiteral(){
        int indexOfFirstChar = 0;
        int indexOfLastChar = sequence.length() - 1;

        if(BNFConstants.SINGLE_QUOTATION.equals(Character.toString(sequence.charAt(indexOfFirstChar)))
                && BNFConstants.SINGLE_QUOTATION.equals(Character.toString(sequence.charAt(indexOfLastChar)))){
            return true;
        }
        return false;
    }
}
