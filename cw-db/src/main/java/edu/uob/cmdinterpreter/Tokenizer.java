package edu.uob.cmdinterpreter;

import edu.uob.exceptions.TokenizerException;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *   This is based on http://cogitolearning.co.uk/2013/04/writing-a-parser-in-java-the-tokenizer/ (12 March 2022)
 */
public class Tokenizer {

    private final LinkedList<TokenInfo> tokenInfos;
    private final LinkedList<Token> tokens;

    public Tokenizer(){
        tokenInfos = new LinkedList<>();
        tokens = new LinkedList<>();

        /* --- define tokens information --- */

        // Command type (a list of special keywords, so have just placed them all into a single regular expression)
        addToken("\\bUSE\\b|\\bCREATE\\b|\\bDROP\\b|\\bALTER\\b|\\bINSERT\\b|\\bSELECT\\b|\\bUPDATE\\b|\\bDELETE\\b" +
                "|\\bJOIN\\b", TokenType.CT);

        // Keywords (Unsure how extensive this list needs to be, but for now will just keep them all in one regular expression)
        addToken("\\bAND\\b|\\bDROP\\b|\\bDATABASE\\b|\\bDROP\\b|\\bFROM\\b|\\bIN\\b|\\bINTO\\b|\\bNULL\\b|\\bON\\b" +
                "|\\bOR\\b|\\bSET\\b|\\bTABLE\\b|\\bVALUES\\b|\\bWHERE\\b", TokenType.KW);

        // Operations (in a list)
        addToken(("==|<=|>=|!=|<|>|=|\\bLIKE\\b"), TokenType.OP); // Operation

        // Literals (expressions for are quite messy so separating into String, number, character and boolean)
        addToken("'([^']|\\.)*'", TokenType.LIT_STR); // String literal \\"([^\\"]|\\.)*\\"
        addToken("[+-]?([0-9]*[.])?[0-9]+", TokenType.LIT_NUM); // Number literal
        addToken("\\bTRUE\\b|\\bFALSE\\b", TokenType.LIT_BOOL); // Boolean literal
        addToken("\\b[a-zA-Z]\\b", TokenType.LIT_CHAR); // Character literal (letters)
        addToken("[!#$%&()*+,-\\./:;<=>?@[/]^_`{~}]", TokenType.LIT_CHAR); // Character literal (special characters)
        // addToken("\\bNULL\\b", TokenType.LIT); // NULL literal

        // Separator
        addToken(("\\s+"), TokenType.SEP);

        // Identifiers (Basically any word, so has the lowest precedence - expect this may change...!)
        //TODO really struggling to regex terms containing only alphanumeric characters...!
        addToken("\\b([a-zA-Z0-9])*\\b", TokenType.ID);
    }

    public LinkedList<Token> getTokens(){
        return tokens;
    }


    private void addToken(String regex, TokenType tokenType){
        Pattern.compile("\\b([(a-z)(A-Z)(0-9)])+\\b");
        tokenInfos.add(new TokenInfo(Pattern.compile("^(" + regex + ")", Pattern.CASE_INSENSITIVE), tokenType));
    }

    public boolean tokenize(String inputSequence) throws TokenizerException {

        if(inputSequence != null && !inputSequence.isEmpty()) {
            String string = inputSequence.strip();
            tokens.clear();

            while(!string.equals("")) {
                boolean match = false;

                for(TokenInfo info : tokenInfos) {
                    Matcher m = info.regex.matcher(string);

                    if(m.find()) {
                        match = true;
                        String tok = m.group().trim();
                        tokens.add(new Token(info.token, tok));

                        string = m.replaceFirst("");
                        string = string.strip();
                        break;
                    }
                }

                if(!match) {
                    throw new TokenizerException("Invalid token within input sequence: " + inputSequence);
                }
            }
            return true;
        }
        throw new TokenizerException("Input sequence is empty");
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
