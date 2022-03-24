package edu.uob.exceptions;

import edu.uob.cmdinterpreter.Token;

import java.io.Serial;

public class ParsingException extends Exception {

    @Serial
    private static final long serialVersionUID = -1238092479832904L;

    public ParsingException(String message) {
        super(message);
    }

    public static class InvalidGrammarException extends ParsingException {

        @Serial
        private static final long serialVersionUID = -67583742802924349L;

        public InvalidGrammarException(Token token, String expectedGrammar) {
            super("Unable to parse token: '" + token.toString() + "' for expected grammar '" + expectedGrammar + "'");
        }
    }

    public static class TokenIndexOutOfBoundsException extends ParsingException {

        @Serial
        private static final long serialVersionUID = -895694787243649285L;

        public TokenIndexOutOfBoundsException(int tokenIndex, int tokensLength) {
            super("Token index " + tokenIndex + " exceeds token length: " + tokensLength);
        }
    }

}
