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
        return sequence;
    }

    @Override
    public String toString() {
        return "[" + tokenType.toString() + "] : " + sequence;
    }
}
