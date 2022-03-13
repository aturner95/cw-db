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

        /* --- define tokens information --- */

        // Command type (a list of special keywords, so have just placed them all into a single regular expression)
        addToken("\\bUSE\\b|\\bCREATE\\b|\\bDROP\\b|\\bALTER\\b|\\bINSERT\\b|\\bSELECT\\b|\\bUPDATE\\b|\\bDELETE\\b|\\bJOIN\\b", TokenType.CT);

        // Keywords (Unsure how extensive this list needs to be, but for now will just keep them all in one regular expression)
        addToken("\\bFROM\\b|\\bWHERE\\b|\\bIN\\b|\\bINTO\\b|\\bVALUES\\b", TokenType.KW);

        // Operations (in a list)
        addToken(("==|<=|>=|!=|<|>|=|\\bLIKE\\b"), TokenType.OP); // Operation

        // Literals (expressions for are quite messy so separating into String, number, character and boolean)
        addToken("\\\"([^\\\"]|\\.)*\\\"", TokenType.LIT); // String literal \\"([^\\"]|\\.)*\\"
        addToken("[+-]?([0-9]*[.])?[0-9]+", TokenType.LIT); // Number literal
        addToken("\\bTRUE\\b|\\bFALSE\\b", TokenType.LIT); // Boolean literal
        addToken("\\b[a-zA-Z]\\b", TokenType.LIT); // Character literal (letters)
        addToken("[!#$%&()*+,-\\./:;<=>?@[/]^_`{~}]", TokenType.LIT); // Character literal (special characters) "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"

        // Separator
        addToken(("\\s+"), TokenType.SEP);
        // Identifiers (Basically any word, so has the lowest precedence - expect this may change...!)
        addToken("\\w+", TokenType.ID);
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
            // System.out.println("input: " + string);
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

}
