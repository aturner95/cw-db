package edu.uob;

import edu.uob.dbelements.Table;
import edu.uob.dbfilesystem.DBTableFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class DBTests {

  private DBServer server;
  private File tempDbDir;
  private static final String tempDbDirName = "dbtest";
  // private static final String tableFileExt = ".tab";

  // we make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup(@TempDir File dbDir) {
    // Notice the @TempDir annotation, this instructs JUnit to create a new temp directory somewhere
    // and proceeds to *delete* that directory when the test finishes.
    // You can read the specifics of this at
    // https://junit.org/junit5/docs/5.4.2/api/org/junit/jupiter/api/io/TempDir.html

    // If you want to inspect the content of the directory during/after a test run for debugging,
    // simply replace `dbDir` here with your own File instance that points to somewhere you know.
    // IMPORTANT: If you do this, make sure you rerun the tests using `dbDir` again to make sure it
    // still works and keep it that way for the submission.
    tempDbDir = new File(tempDbDirName);
    dbDir.deleteOnExit();
    server = new DBServer(dbDir);
  }

  @AfterEach
  void teardown(){
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

  // Here's a basic test for spawning a new server and sending an invalid command,
  // the spec dictates that the server respond with something that starts with `[ERROR]`
  @Test
  public void test_invalidCommand_isAnError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  // Add more unit tests or integration tests here.
  // Unit tests would test individual methods or classes whereas integration tests are geared
  // towards a specific usecase (i.e. creating a table and inserting rows and asserting whether the
  // rows are actually inserted)

  @Test
  public void test_handleCommand_invalidCommand_responseStatusIsError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  @Test
  public void test_handleCommand_validCreateDbCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(db.exists());
    assertTrue(db.isDirectory());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validUseCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertEquals("markbook", server.getDatabaseDirectory().getName());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validCreateTableCommand_statusOk() {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);

    // use Db and create table
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));

    // assert file exists, then teardown
    File table = new File("markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());
    teardown(db);
  }

  @Test
  public void test_handleCommand_insertCommand_fiveRowsInserted() throws Exception {
    // create database
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
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

    Table marks = new DBTableFile().readDBFileIntoEntity("markbook" + File.separator + "marks.tab");
    assertEquals(5, marks.getRows().size());

    teardown(db);
  }



  @Test
  public void test_handleCommand_validDropTableCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[OK]"));
    File table = new File("markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());

    assertTrue(server.handleCommand("DROP TABLE marks;").startsWith("[OK]"));
    teardown(db);
  }

  @Test
  public void test_handleCommand_validDropDbCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(server.handleCommand("DROP DATABASE markbook;").startsWith("[OK]"));
    assertFalse(db.exists());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validDropDbWithTableCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    File table = new File("markbook" + File.separator + "marks.tab");
    assertTrue(table.exists());
    assertTrue(table.isFile());

    assertTrue(server.handleCommand("DROP DATABASE markbook;").startsWith("[OK]"));
    assertFalse(db.exists());
    assertFalse(table.exists());
    teardown(db);
  }

  @Test
  public void test_handleCommand_validJoinCommand_statusOk() {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    File db = new File("markbook");
    setup(db);
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (studentId, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (1, 33, False);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES (2, 66, TRUE);").startsWith("[OK]"));

    assertTrue(server.handleCommand("CREATE TABLE students (name, course, year);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO students VALUES ('Amir', 'MSc', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO students VALUES ('Donte', 'MSc', 2);").startsWith("[OK]"));

    assertTrue(server.handleCommand("JOIN students AND marks on id AND studentId;").startsWith("[OK]"));
    teardown(db);
  }

}
