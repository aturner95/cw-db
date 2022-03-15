package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.abstractcmd.DBCmd;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParser {

    @Test
    public void test_parse_basicSelectStatement_returnsSelectCMD(){
        List<Token> tokens = new LinkedList<>();
        tokens.add(new Token(TokenType.CT, "select"));
        tokens.add(new Token(TokenType.ID, "*"));
        tokens.add(new Token(TokenType.KW, "from"));
        tokens.add(new Token(TokenType.ID, "people"));
        tokens.add(new Token(TokenType.LIT, ";"));
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setTokens(tokens);

        Parser parser = new Parser(tokenizer);

        DBCmd cmd = parser.parse();
        assertTrue(cmd instanceof SelectCMD);
    }
}
