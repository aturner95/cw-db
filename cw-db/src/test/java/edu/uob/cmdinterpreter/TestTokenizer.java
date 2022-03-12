package edu.uob.cmdinterpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
        for(Tokenizer.Token token: tokenizer.getTokens()){
            assertEquals(TokenType.CT, token.getTokenType());
        }
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_mispelt_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("user");
        assertEquals(0, tokenizer.getTokens().size());
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_noSpaces_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("deletejoin");
        assertEquals(0, tokenizer.getTokens().size());
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_emptyStringInput_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize("");
        assertEquals(0, tokenizer.getTokens().size());
    }

    @Test
    public void test_tokenize_invalidCommandTypeTokens_nullStringInput_tokenNotAdded(){
        tokenizer = new Tokenizer();
        tokenizer.tokenize(null);
        assertEquals(0, tokenizer.getTokens().size());
    }


}
