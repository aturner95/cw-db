package edu.uob.cmdinterpreter;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *   This is based on http://cogitolearning.co.uk/2013/04/writing-a-parser-in-java-the-tokenizer/ (12 March 2022)
 */
public class Tokenizer {

    private LinkedList<TokenInfo> tokenInfos;
    private LinkedList<Token> tokens;

    public Tokenizer(){
        tokenInfos = new LinkedList<TokenInfo>();
        tokens = new LinkedList<Token>();

        // define tokens information
        addToken("\\buse\\b|\\bcreate\\b|\\bdrop\\b|\\balter\\b|\\binsert\\b|\\bselect\\b|\\bupdate\\b|\\bdelete\\b|\\bjoin\\b", TokenType.CT);
    }

    public LinkedList<Token> getTokens(){
        return tokens;
    }

    public void addToken(String regex, TokenType tokenType){
        tokenInfos.add(new TokenInfo(Pattern.compile("^(" + regex + ")", Pattern.CASE_INSENSITIVE), tokenType));
    }

    public void tokenize(String inputSequence){

        if(inputSequence != null && !inputSequence.isEmpty()) {

            String string = inputSequence.strip();
            System.out.println("input: " + string);
            tokens.clear();

            while (!string.equals("")) {
                boolean match = false;

                for (TokenInfo info : tokenInfos) {
                    Matcher m = info.regex.matcher(string);
                    if (m.find()) {
                        match = true;
                        String tok = m.group().trim();
                        tokens.add(new Token(info.token, tok));
                        string = m.replaceFirst("");
                        string = string.strip();
                        break;
                    }
                }

                if (!match) {
                    // TODO throw ParserException
                    System.out.println("Unable to tokenise input:" + string);
                    return;
                }
            }
        }
    }



    private class TokenInfo {
        public final Pattern regex;
        public final TokenType token;

        public TokenInfo(Pattern regex, TokenType token){
            super();
            this.regex = regex;
            this.token = token;
        }
    }

    public class Token {

        private final TokenType tokenType;
        private final String sequence;

        public Token(TokenType tokenType, String sequence){
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
        public String toString(){
            return "[" + tokenType.toString() +  "] : " + sequence;
        }
    }

}
