package edu.uob.cmdinterpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class TestTokenizer {

    private Tokenizer tokenizer;

    @BeforeEach
    public void setup(){
        this.tokenizer = new Tokenizer();
    }

    @Test
    public void test_tokenize_validCommandTypeTokens_nineCommandTypeTokensAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("use CREATE Drop aLter inseRT select      Update delete JOIN ");

        assertEquals(9, tokenizer.getTokens().size());
        for(Token token: tokenizer.getTokens()){
            assertEquals(TokenType.CT, token.getTokenType());
        }
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_mispelt_tokenAddedButIsNotCT(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("user");
        assertEquals(1, tokenizer.getTokens().size());
        assertNotEquals(TokenType.CT, tokenizer.getTokens().get(0).getTokenType());
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_noSpaces_tokenAddedButIsNotCT(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("deletejoin");
        assertEquals(1, tokenizer.getTokens().size());
        assertNotEquals(TokenType.CT, tokenizer.getTokens().get(0).getTokenType());
    }


    @Test
    public void test_tokenize_validKeyword_threeTokensAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("from Where IN   ");

        assertEquals(3, tokenizer.getTokens().size());
        assertEquals(TokenType.KW, tokenizer.getTokens().get(0).getTokenType());
        assertEquals(TokenType.KW, tokenizer.getTokens().get(1).getTokenType());
        assertEquals(TokenType.KW, tokenizer.getTokens().get(2).getTokenType());

        assertEquals("from", tokenizer.getTokens().get(0).getSequence());
        assertEquals("Where", tokenizer.getTokens().get(1).getSequence());
        assertEquals("IN", tokenizer.getTokens().get(2).getSequence());
    }

    @Test
    public void test_tokenize_invalidKeyword_tokenAddedButIsNotKW(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("fromm");

        assertEquals(1, tokenizer.getTokens().size());
        assertNotEquals(TokenType.KW, tokenizer.getTokens().get(0).getTokenType());
    }

    @Test
    public void test_tokenize_validLiteralStrings_twoStringLiteralsAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("\"this is a String\"\"    This is another    string    \"");

        assertEquals(2, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("\"this is a String\"", tokenizer.getTokens().get(0).getSequence());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(1).getTokenType());
        assertEquals("\"    This is another    string    \"", tokenizer.getTokens().get(1).getSequence());
    }

    @Test
    public void test_tokenize_validLiteralStringsAmongstWords_twoStringLiteralsAdded2(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("\"this is a String\" these are not string literals \"    This is another    string    \"");

        assertEquals(7, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("\"this is a String\"", tokenizer.getTokens().get(0).getSequence());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(6).getTokenType());
        assertEquals("\"    This is another    string    \"", tokenizer.getTokens().get(6).getSequence());
    }

    @Test
    public void test_tokenize_validPositiveLiteralInteger_integerLiteralAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize(" 77  ");

        assertEquals(1, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("77", tokenizer.getTokens().get(0).getSequence());
    }

    @Test
    public void test_tokenize_validPositiveLiteralFloat_floatLiteralAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize(" 0.5  ");

        assertEquals(1, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("0.5", tokenizer.getTokens().get(0).getSequence());
    }

    @Test
    public void test_tokenize_validLiteralBoolean_trueLiteralAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize(" TRUE  ");

        assertEquals(1, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("TRUE", tokenizer.getTokens().get(0).getSequence());
    }

    @Test
    public void test_tokenize_validLiteralChar_letterLiteralAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("F");

        assertEquals(1, tokenizer.getTokens().size());
        assertEquals(TokenType.LIT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("F", tokenizer.getTokens().get(0).getSequence());
    }

    @Test
    public void test_tokenize_validOperators_sevenOperationTokensAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("== >= <=!= <>  LIKE");

        assertEquals(7, tokenizer.getTokens().size());
        for(Token token : tokenizer.getTokens()){
            assertEquals(TokenType.OP, token.getTokenType());
        }
        assertEquals("==", tokenizer.getTokens().get(0).getSequence());
        assertEquals(">=", tokenizer.getTokens().get(1).getSequence());
        assertEquals("<=", tokenizer.getTokens().get(2).getSequence());
        assertEquals("!=", tokenizer.getTokens().get(3).getSequence());
        assertEquals("<", tokenizer.getTokens().get(4).getSequence());
        assertEquals(">", tokenizer.getTokens().get(5).getSequence());
        assertEquals("LIKE", tokenizer.getTokens().get(6).getSequence());
    }

    @Test
    public void test_tokenize_invalidTokens_emptyStringInput_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("");
        assertEquals(0, tokenizer.getTokens().size());
    }

    @Test
    public void test_tokenize_invalidTokens_nullStringInput_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize(null);
        assertEquals(0, tokenizer.getTokens().size());
    }

    @Test
    public void test_tokenize_basicSelectStatement_sevenTokensAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("select * from people where id=3;");

        assertEquals(9, tokenizer.getTokens().size());

        assertEquals(TokenType.CT, tokenizer.getTokens().get(0).getTokenType());
        assertEquals("select", tokenizer.getTokens().get(0).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(1).getTokenType());
        assertEquals("*", tokenizer.getTokens().get(1).getSequence());

        assertEquals(TokenType.KW, tokenizer.getTokens().get(2).getTokenType());
        assertEquals("from", tokenizer.getTokens().get(2).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(3).getTokenType());
        assertEquals("people", tokenizer.getTokens().get(3).getSequence());

        assertEquals(TokenType.KW, tokenizer.getTokens().get(4).getTokenType());
        assertEquals("where", tokenizer.getTokens().get(4).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(5).getTokenType());
        assertEquals("id", tokenizer.getTokens().get(5).getSequence());

        assertEquals(TokenType.OP, tokenizer.getTokens().get(6).getTokenType());
        assertEquals("=", tokenizer.getTokens().get(6).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(7).getTokenType());
        assertEquals("3", tokenizer.getTokens().get(7).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(8).getTokenType());
        assertEquals(";", tokenizer.getTokens().get(8).getSequence());
    }

    @Test
    public void test_tokenize_basicInsertStatement_nineteenTokensAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("insert into People(Id, name, age) values(2, \"Bob\", 23);");

        assertEquals(19, tokenizer.getTokens().size());

        int tokenIndex = 0;
        assertEquals(TokenType.CT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("insert", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.KW, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("into", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("People", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("(", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("Id", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(",", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("name", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(",", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.ID, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("age", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(")", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.KW, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("values", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("(", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("2", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(",", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("\"Bob\"", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(",", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals("23", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(")", tokenizer.getTokens().get(tokenIndex++).getSequence());

        assertEquals(TokenType.LIT, tokenizer.getTokens().get(tokenIndex).getTokenType());
        assertEquals(";", tokenizer.getTokens().get(tokenIndex++).getSequence());


    }


}
