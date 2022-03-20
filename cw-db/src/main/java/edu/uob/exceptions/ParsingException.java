package edu.uob.exceptions;

public class ParsingException extends Exception {


    private static final long serialVersionUID = -1238092479832904L;

    public ParsingException(String message) {
        super(message);
    }

    public static class InvalidGrammarException extends ParsingException {
        private static final long serialVersionUID = -67583742802924349L;

        public InvalidGrammarException(String expectedGrammar) {
            super("Expected: grammar" + expectedGrammar);
        }
    }

    public static class TokenIndexOutOfBoundsException extends ParsingException {
        private static final long serialVersionUID = -895694787243649285L;

        public TokenIndexOutOfBoundsException(int tokenIndex, int tokensLength) {
            super("Token index " + tokenIndex + " exceeds token length: " + tokensLength);
        }
    }

}
