package edu.uob;

import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;

import static edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd.STATUS_OK;
import static edu.uob.dbfilesystem.DBFileConstants.ROOT_DB_DIR;
import static org.junit.jupiter.api.Assertions.*;

final class DBTests {

  private DBServer server;
  private File tempDbDir;


  @BeforeEach
  void setup(@TempDir File db) {
    db = new File(ROOT_DB_DIR + db.getName());
    db.deleteOnExit();
    server = new DBServer(new File(ROOT_DB_DIR));
  }

  @AfterEach
  void teardown(){
    if(tempDbDir != null && tempDbDir.exists()){
      File[] files = tempDbDir.listFiles();
      if(files != null) {
        for (File file : files) {
          if(file != null && file.exists()) {
            assertTrue(file.delete());
          }
        }
      }
      assertTrue(tempDbDir.delete());
      tempDbDir = null;
    }

  }

  void clearDatabaseMetadata(String tablename) throws Exception{
    new DBTableFile().removeTableFromMetadata("dbtest", tablename);
  }


  @AfterEach
  void teardown(@TempDir File tempDbDir){
    if(tempDbDir.exists()){
      File[] files = tempDbDir.listFiles();
      if(files != null) {
        for (File file : files) {
          if(file != null && file.exists()) {
            assertTrue(file.delete());
          }
        }
      }
      assertTrue(tempDbDir.delete());
    }
  }

  @Test
  public void test_invalidCommand_isAnError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }


  @Test
  public void test_handleCommand_invalidCommand_responseStatusIsError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  @Test
  public void test_handleCommand_validCreateDbCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator + "markbook");
    setup(db);
    assertTrue(db.exists());
    assertTrue(db.isDirectory());
    // database context should NOT be set to markbook after creating a database
    assertEquals("databases",server.getDatabaseDirectory().toString());
    assertTrue(db.delete());
  }

  @Test
  public void test_handleCommand_validUseCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator + "markbook");
    setup(db);
    assertTrue(db.exists());
    assertTrue(db.isDirectory());

    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertEquals("databases/markbook", server.getDatabaseDirectory().toString());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validCreateTableCommand_statusOk() throws FileNotFoundException {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator + "markbook");
    setup(db);

    // use Db and create table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));

    // assert file exists, then teardown
    File table = new File(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());

    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_invalidCreateTableCommandWithoutUse_statusError() {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // try creating a table without first selecting a Db
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[ERROR]"));

    // assert file exists, then teardown
    File table = new File(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertFalse(table.exists());
    teardown(db);
  }

  @Test
  public void test_handleCommand_invalidCreateThenValidCreate_statusErrorThenOK() throws FileNotFoundException {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // Then try creating a table without first selecting a Db, expected response is ERROR
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[ERROR]"));

    // Now try using markbook database, should be OK
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));

    // Now try creating, should be OK
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));

    // assert file exists, then teardown
    File table = new File(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_parse_trickyAlterOnTableWithNoAttributes_alterCmdBuilt() throws Exception{
    // create and use database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));

    // create a table with no attributes
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[OK]"));

    // Now try adding an attribute; only until we've added the first attribute is the primary key added
    assertTrue(server.handleCommand("ALTER TABLE marks ADD name;").startsWith("[OK]"));

    // grab the table and assert the id column has been added along with the name
    Table marks = new DBTableFile().readDBFileIntoEntity(ROOT_DB_DIR + File.separator +"markbook" + File.separator + "marks.tab");
    assertEquals(2, marks.getColHeadings().size());
    assertEquals("id", marks.getColHeadings().get(0).getColName());
    assertEquals("name", marks.getColHeadings().get(1).getColName());

    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_insertCommand_fiveRowsInserted() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // set up table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));

    // insert data, some will be OK some will ERROR, but Server should still run in either case
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (8, 'Paul', 23, FALSE);").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (Laura, 33, False);").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Amir', 24, TRUE);").startsWith("[OK]"));

    Table marks = new DBTableFile().readDBFileIntoEntity(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertEquals(5, marks.getRows().size());

    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_validDropTableCommand_statusOk() throws FileNotFoundException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    String dbPathName ="markbook";
    File db = new File(ROOT_DB_DIR + File.separator + dbPathName);
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[OK]"));
    File table = new File(ROOT_DB_DIR + File.separator + dbPathName + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());

    assertTrue(server.handleCommand("DROP TABLE marks;").startsWith("[OK]"));
    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_validDropDbCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);
    assertTrue(server.handleCommand("DROP DATABASE markbook;").startsWith("[OK]"));
    assertFalse(db.exists());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validDropDbWithTableCommand_statusOk() throws FileNotFoundException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator + "markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    File table = new File(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());

    assertTrue(server.handleCommand("DROP DATABASE markbook;").startsWith("[OK]"));
    assertFalse(db.exists());
    assertFalse(table.exists());
    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_validJoinCommand_statusOk() throws FileNotFoundException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (studentId, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (1, 33, False);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (2, 66, TRUE);").startsWith("[OK]"));

    assertTrue(server.handleCommand("CREATE TABLE students (name, course, year);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO students VALUES ('Amir', 'MSc', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO students VALUES ('Donte', 'MSc', 2);").startsWith("[OK]"));

    assertTrue(server.handleCommand("JOIN students AND marks on id AND studentId;").startsWith("[OK]"));
    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    new DBTableFile().removeTableFromMetadata("markbook", "students");
    teardown(db);
  }

  @Test
  public void test_handleCommand_selectCommand_statusOk() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // set up table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE student (name, grade, pass);").startsWith("[OK]"));

    // insert data, some will be OK some will ERROR, but Server should still run in either case
    assertTrue(server.handleCommand("INSERT INTO student VALUES ('Anna', 67, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO student VALUES ('Bob', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO student VALUES ('Clive', 49, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO student VALUES ('Diana', 35, FALSE);").startsWith("[OK]"));

    Table marks = new DBTableFile().readDBFileIntoEntity(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "student.tab");
    assertEquals(4, marks.getRows().size());

    assertTrue(server.handleCommand("select * from student;").startsWith("[OK]"));
    assertTrue(server.handleCommand("select id, name from student;").startsWith("[OK]"));
    assertTrue(server.handleCommand("select * from student where pass == TRUE;").startsWith("[OK]"));
    assertTrue(server.handleCommand("select name, grade from student where (pass == TRUE) OR (grade >= 50);").startsWith("[OK]"));
    assertTrue(server.handleCommand("select name, grade from student where (pass == TRUE) AND (grade >= 50);").startsWith("[OK]"));

    new DBTableFile().removeTableFromMetadata("markbook", "student");
    teardown(db);
  }

  @Test
  public void test_handleCommand_invalidSelectCommand_statusOk() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // use table and create table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD name;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD mark;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD pass;").startsWith("[OK]"));

    // try some selects
    // TODO grade is not an attribute of MARK, however error message is wrong!
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE (grade >=) OR (pass == TRUE)").startsWith("[ERROR]"));

    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }

  @Test
  public void test_handleCommand_joinCommand_statusOk() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // use table and create tables via different methods
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE coursework (task, grade);").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD name;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD mark;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD pass;").startsWith("[OK]"));

    // insert data into the two tables
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('DB', 1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 4);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('STAG', 2);").startsWith("[OK]"));

    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));

    // try some selects
    assertTrue(server.handleCommand("SELECT * FROM coursework WHERE task == 'OXO';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT name, pass FROM marks WHERE (mark >= 50) OR (pass == TRUE);").startsWith("[OK]"));

    // try join
    assertTrue(server.handleCommand("JOIN coursework AND marks ON grade AND id;").startsWith("[OK]"));

    new DBTableFile().removeTableFromMetadata("markbook", "coursework");
    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }



  @Test
  public void test_handleCommand_insertCommand_sequencePreservedAfterDelete() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File(ROOT_DB_DIR + File.separator +"markbook");
    setup(db);

    // set up table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));

    // insert data, some will be OK some will ERROR, but Server should still run in either case
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Amir', 24, TRUE);").startsWith("[OK]"));

    assertTrue(server.handleCommand("DELETE FROM marks WHERE name == 'Amir';").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Jules', 20, TRUE);").startsWith("[OK]"));

    Table marks = new DBTableFile().readDBFileIntoEntity(ROOT_DB_DIR + File.separator + "markbook" + File.separator + "marks.tab");
    assertEquals(5, marks.getRows().size());
    assertFalse(marks.toString().contains("5\tJules\t20\tTRUE"));
    assertTrue(marks.toString().contains("6\tJules\t20\tTRUE"));


    new DBTableFile().removeTableFromMetadata("markbook", "marks");
    teardown(db);
  }


}
