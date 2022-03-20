package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.commands.*;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.exceptions.ParsingException;
import edu.uob.exceptions.ParsingException.*;
import edu.uob.exceptions.TokenizerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestParser {

    @Test
    public void test_parse_basicSelectStatement_returnsSelectCMD() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select * from people;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof SelectCMD);
        assertEquals(5, tokenizer.getTokens().size());
    }

    // TODO test SELECT with WHERE once conditions have been sorted



    // @Test TODO this input sequence should not be tokenized as no token types start with special characters
    public void test_tokenize_basicSelectStatementSyntaxError_ParsingExceptionThrown() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select *from people;");
        Parser parser = new Parser(tokenizer);

        assertThrows(ParsingException.class, ()-> parser.parse());

    }

    @Test
    public void test_parse_basicSelectStatementMissingSemiColon_InvalidGrammarExceptionThrown() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select * from people");
        Parser parser = new Parser(tokenizer);

        assertThrows(InvalidGrammarException.class, ()-> parser.parse());

    }

    @Test
    public void test_parse_basicUseStatement_returnsUseCMD() throws Exception{
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("USE DB;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof UseCMD);
        assertEquals(3, tokenizer.getTokens().size());
        assertEquals("DB", cmd.getTableNames().get(0));
    }

    @Test
    public void test_parse_basicUseStatementSyntaxError_InvalidGrammarExceptionThrown() throws Exception {

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("USE from DB;");
        Parser parser = new Parser(tokenizer);

        assertThrows(InvalidGrammarException.class, ()-> parser.parse());
    }

    @Test
    public void test_parse_basicCreateDatabase_cmdIsCreate() throws Exception {

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("create database dummyDb;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof CreateCMD);
    }

    @Test
    public void test_parse_basicCreateTable_cmdIsCreate() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("create table dummyTable;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof CreateCMD);
    }

    @Test
    public void test_parse_basicDropDatabase_cmdIsDrop() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("drop database testDb;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DropCMD);
    }

    @Test
    public void test_parse_basicDropTable_cmdIsDrop() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("DROP TABLE TABLENAME ;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DropCMD);
    }

    @Test
    public void test_parse_basicAlter_cmdIsAlter() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("ALTER TABLE Country DROP Id;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof AlterCMD);
    }

    @Test
    public void test_parse_basicInsert_cmdIsInsert() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("INSERT   INTO tablename Values(1, 'Bob', 'Bob@email.com');");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof InsertCMD);
    }

    // TODO test UPDATE once conditions have been sorted

    // TODO test DELETE once conditions have been sorted

    @Test
    public void test_parse_basicJoin_cmdIsJoin() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("join table1 and table1 on id and id;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof JoinCMD);
    }
}
