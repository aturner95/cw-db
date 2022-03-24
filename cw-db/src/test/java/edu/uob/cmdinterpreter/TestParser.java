package edu.uob.cmdinterpreter;

import edu.uob.cmdinterpreter.commands.*;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.exceptions.ParsingException;
import edu.uob.exceptions.ParsingException.*;
import org.junit.jupiter.api.Test;

import java.io.File;

// import static edu.uob.dbfilesystem.DBFileConstants.ROOT_DB_DIR;

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


    // @Test TODO this input sequence should not be tokenized as no token types start with special characters
    public void test_tokenize_basicSelectStatementSyntaxError_ParsingExceptionThrown() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select *from people;");
        Parser parser = new Parser(tokenizer);

        assertThrows(ParsingException.class, parser::parse);

    }

    @Test
    public void test_parse_basicSelectStatementMissingSemiColon_InvalidGrammarExceptionThrown() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select * from people");
        Parser parser = new Parser(tokenizer);

        assertThrows(ParsingException.class, parser::parse);

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
        assertEquals(/*ROOT_DB_DIR + File.separator + */"db", cmd.getDatabaseName());
    }

    @Test
    public void test_parse_basicUseStatementSyntaxError_InvalidGrammarExceptionThrown() throws Exception {

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("USE from DB;");
        Parser parser = new Parser(tokenizer);

        assertThrows(InvalidGrammarException.class, parser::parse);
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
        assertEquals("dummydb", cmd.getDatabaseName());
        assertEquals("DATABASE", ((CreateCMD) cmd).getCreateType());
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
        assertEquals("dummytable", cmd.getTableNames().get(0));
    }

    @Test
    public void test_parse_createTableWithAttributeList_cmdIsCreate() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("CREATE TABLE marks (name, mark, pass);");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof CreateCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("name", cmd.getColNames().get(0));
        assertEquals("mark", cmd.getColNames().get(1));
        assertEquals("pass", cmd.getColNames().get(2));
    }

    @Test
    public void test_parse_basicDropDatabase_dropCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("drop database testDb;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DropCMD);
        assertEquals("testdb", cmd.getDatabaseName());
        assertEquals("DATABASE", ((DropCMD) cmd).getDropType());
    }

    @Test
    public void test_parse_basicDropTable_dropCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("DROP TABLE TABLENAME ;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DropCMD);
        assertEquals("tablename", cmd.getTableNames().get(0));
        assertEquals("TABLE", ((DropCMD) cmd).getDropType());
    }

    @Test
    public void test_parse_basicAlter_alterCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("ALTER TABLE Country DROP Id;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof AlterCMD);
        assertEquals("country", cmd.getTableNames().get(0));
        assertEquals("DROP", ((AlterCMD) cmd).getAlterationType());
        assertEquals("Id", cmd.getColNames().get(0));
    }

    @Test
    public void test_parse_basicInsert_insertCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("INSERT   INTO tablename Values(1, 'Bob', 'Bob@email.com');");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof InsertCMD);
        assertEquals("tablename", cmd.getTableNames().get(0));
        assertEquals("1", cmd.getVariables().get(0));
        assertEquals("Bob", cmd.getVariables().get(1));
        assertEquals("Bob@email.com", cmd.getVariables().get(2));
    }

    @Test
    public void test_parse_basicInsert2_insertCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof InsertCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("Steve", cmd.getVariables().get(0));
        assertEquals("65", cmd.getVariables().get(1));
        assertEquals("TRUE", cmd.getVariables().get(2));
    }

    @Test
    public void test_parse_basicJoin_joinCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("join Marks and Students on studentId and id;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof JoinCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("students", cmd.getTableNames().get(1));
        assertEquals("studentId", cmd.getColNames().get(0));
        assertEquals("id", cmd.getColNames().get(1));
    }


    @Test
    public void test_parse_basicSelect1_selectCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof SelectCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("*", cmd.getColNames().get(0));
        assertEquals("pass", cmd.getConditions().get(0).getAttribute());
        assertEquals("==", cmd.getConditions().get(0).getOperator());
        assertEquals("FALSE", cmd.getConditions().get(0).getValue().getSequence());
        assertEquals("AND", cmd.getConditionJoinOperators().get(0));
        assertEquals("mark", cmd.getConditions().get(1).getAttribute());
        assertEquals(">", cmd.getConditions().get(1).getOperator());
        assertEquals("35", cmd.getConditions().get(1).getValue().getSequence());
    }

    @Test
    public void test_parse_basicSelect2_selectCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("SELECT * FROM marks WHERE name LIKE 've';");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof SelectCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("*", cmd.getColNames().get(0));
        assertEquals("name", cmd.getConditions().get(0).getAttribute());
        assertEquals("LIKE", cmd.getConditions().get(0).getOperator());
        assertEquals("ve", cmd.getConditions().get(0).getValue().getSequence());
    }

    @Test
    public void test_parse_basicDelete1_deleteCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("DELETE FROM marks WHERE mark<40;");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DeleteCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("mark", cmd.getConditions().get(0).getAttribute());
        assertEquals("<", cmd.getConditions().get(0).getOperator());
        assertEquals("40", cmd.getConditions().get(0).getValue().getSequence());
    }

    @Test
    public void test_parse_basicDelete2_deleteCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("DELETE FROM marks WHERE name == 'Dave';");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof DeleteCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("name", cmd.getConditions().get(0).getAttribute());
        assertEquals("==", cmd.getConditions().get(0).getOperator());
        assertEquals("Dave", cmd.getConditions().get(0).getValue().getSequence());
    }

    @Test
    public void test_parse_invalidDeleteWithNoCondition_deleteCmdBuilt() throws Exception{

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("DELETE FROM marks;");
        Parser parser = new Parser(tokenizer);

        assertThrows(ParsingException.class, parser::parse);
    }

    @Test
    public void test_parse_basicUpdate_updateCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("UPDATE marks SET mark = 38 WHERE name == 'Clive';");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof UpdateCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("mark", cmd.getNameValuePair().get(0).getName());
        assertEquals("38", cmd.getNameValuePair().get(0).getValue());
        assertEquals("name", cmd.getConditions().get(0).getAttribute());
        assertEquals("==", cmd.getConditions().get(0).getOperator());
        assertEquals("Clive", cmd.getConditions().get(0).getValue().getSequence());
    }

    @Test
    public void test_parse_trickyUpdate_updateCmdBuilt() throws Exception{

        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("UPDATE marks SET mark = 38, pass = FALSE WHERE (name=='Clive') AND (sausage LIKE 5);");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof UpdateCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("mark", cmd.getNameValuePair().get(0).getName());
        assertEquals("38", cmd.getNameValuePair().get(0).getValue());
        assertEquals("pass", cmd.getNameValuePair().get(1).getName());
        assertEquals("FALSE", cmd.getNameValuePair().get(1).getValue());
        assertEquals("name", cmd.getConditions().get(0).getAttribute());
        assertEquals("==", cmd.getConditions().get(0).getOperator());
        assertEquals("Clive", cmd.getConditions().get(0).getValue().getSequence());
        assertEquals("sausage", cmd.getConditions().get(1).getAttribute());
        assertEquals("LIKE", cmd.getConditions().get(1).getOperator());
        assertEquals("5", cmd.getConditions().get(1).getValue().getSequence());
    }

    @Test
    public void test_parse_invalidUpdate1_throwsInvalidGrammarException() throws Exception{

        Tokenizer tokenizer = new Tokenizer();
        // conditions require "==" operator, not "="
        tokenizer.tokenize("UPDATE marks SET mark = 38 WHERE name = 'Clive';");
        Parser parser = new Parser(tokenizer);

        assertThrows(InvalidGrammarException.class, parser::parse);
    }

    @Test
    public void test_parse_invalidUpdate2_throwsInvalidGrammarException() throws Exception{

        Tokenizer tokenizer = new Tokenizer();
        // UPDATE assignment requires "=" operator, not "=="
        tokenizer.tokenize("UPDATE marks SET mark == 38 WHERE name = 'Clive';");
        Parser parser = new Parser(tokenizer);

        assertThrows(InvalidGrammarException.class, parser::parse);
    }

    @Test
    public void test_parse_validSelectCmdWithtrickyCondition_selectCmdBuilt() throws Exception {
        // given
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize("select id, name from Marks where (pass==TRUE) OR ((course=='MSc') AND (grade>50));");
        Parser parser = new Parser(tokenizer);

        // when
        DBCmd cmd = parser.parse();

        // then
        assertTrue(cmd instanceof SelectCMD);
        assertEquals("marks", cmd.getTableNames().get(0));
        assertEquals("id", cmd.getColNames().get(0));
        assertEquals("name", cmd.getColNames().get(1));

        assertEquals("pass", cmd.getConditions().get(0).getAttribute());
        assertEquals("==", cmd.getConditions().get(0).getOperator());
        assertEquals("TRUE", cmd.getConditions().get(0).getValue().getSequence());

        assertEquals("OR", cmd.getConditionJoinOperators().get(0));

        assertEquals("course", cmd.getConditions().get(1).getAttribute());
        assertEquals("==", cmd.getConditions().get(1).getOperator());
        assertEquals("MSc", cmd.getConditions().get(1).getValue().getSequence());

        assertEquals("AND", cmd.getConditionJoinOperators().get(1));

        assertEquals("grade", cmd.getConditions().get(2).getAttribute());
        assertEquals(">", cmd.getConditions().get(2).getOperator());
        assertEquals("50", cmd.getConditions().get(2).getValue().getSequence());
    }

}
